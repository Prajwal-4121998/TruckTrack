package com.example.trucktrack.ui.fragments.main

import android.Manifest
import android.animation.ValueAnimator
import android.content.ContentValues.TAG
import android.location.Location.distanceBetween
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentMainBinding
import com.example.trucktrack.model.Station
import com.example.trucktrack.util.Constants
import com.example.trucktrack.util.Constants.END_LAT
import com.example.trucktrack.util.Constants.END_LONG
import com.example.trucktrack.util.Constants.STATION_RADIUS_METERS
import com.example.trucktrack.util.LocationUtils
import com.example.trucktrack.util.animateCameraToIncludeStations
import com.example.trucktrack.util.checkInternetConnection
import com.example.trucktrack.util.mainNav
import com.example.trucktrack.util.showFuelStationDialog
import com.example.trucktrack.util.showMapSnackBar
import com.example.trucktrack.util.showNoInternetSnackBar
import com.example.trucktrack.util.showStationPopup
import com.example.trucktrack.util.toBitmapDescriptor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate),
    OnMapReadyCallback {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var googleMap: GoogleMap
    private var userMarker: Marker? = null
    private var routeDrawn = false
    private var currentStations: List<Station> = emptyList()
    private var userLatLng: LatLng? = null
    private var routePath: List<LatLng> = emptyList()

    @Inject
    lateinit var locationUtils: LocationUtils
    private lateinit var source: LatLng
    private lateinit var destination: LatLng
    private var currentStationInRange: Station? = null
    private var stationEnterTime: Long = 0L

    override fun onViewCreated() {
        setupMap()
        initClickListener()
    }

    private fun initClickListener() {
        binding.buttonUpdateTripStatus.setOnClickListener(this)
        binding.buttonEndTrip.setOnClickListener(this)
    }

    override fun onViewClicked(view: View) {
        when (view) {
            binding.buttonUpdateTripStatus -> {
                if (mContext.checkInternetConnection()) {
                    viewModel.performTripStatusUpdate()
                } else {
                    binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                }
            }

            binding.buttonEndTrip -> {
                if (mContext.checkInternetConnection()) {
                    viewModel.performEndTrip()
                } else {
                    binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                }
            }
        }
    }

    private fun setupMap() {
        binding.mapView.apply {
            onCreate(null)
            getMapAsync(this@MainFragment)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener { marker ->
            if (marker == userMarker) {
                userLatLng?.let { onUserMarkerClicked(it) }
                true
            } else false
        }
        askLocationPermission()
    }

    private fun askLocationPermission() {
        PermissionX.init(this).permissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    if (mContext.checkInternetConnection()) {
                        if (!viewModel.isStartTripCalled) {
                            viewModel.performTripStart()
                        }
                    } else {
                        binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                    }
                    initObserver()
                    startTrackingUserLocation()
                }
            }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tripStartResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            binding.root.showMapSnackBar(data.message.toString())
                            data.data.let {
                                viewModel.tripId = it.id
                                source = LatLng(it.originLat, it.originLong)
                                destination = LatLng(it.endLat, it.endLong)
                                binding.buttonUpdateTripStatus.isVisible = true
                                binding.buttonUpdateTripStatus.text = getString(R.string.pause_trip)
                                fetchRouteOnce()
                                viewModel.isStartTripCalled = true
                            }
                        }
                    }
                }

                launch {
                    viewModel.tripStatusUpdateResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            if (data.data.status == getString(R.string.paused)) {
                                binding.root.showMapSnackBar(data.message.toString())
                                binding.buttonUpdateTripStatus.text =
                                    getString(R.string.resume_trip)
                            } else {
                                binding.buttonUpdateTripStatus.text = getString(R.string.pause_trip)
                                mContext.showFuelStationDialog {
                                    if (it) {
                                        userLatLng.let { it ->
                                            mainNav.navigate(
                                                MainFragmentDirections.actionMainFragmentToAddFuelEntryFragment(
                                                    viewModel.tripId!!,
                                                    it?.latitude.toString(),
                                                    it?.longitude.toString()
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.endTripResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            binding.root.showMapSnackBar(data.message.toString())
                            mainNav.navigate(
                                MainFragmentDirections.actionMainFragmentToTripSummaryFragment(
                                    viewModel.tripId!!
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchRouteOnce() {
        if (routeDrawn) return
        routeDrawn = true

        viewModel.fetchRoute(
            startLat = source.latitude,
            startLon = source.longitude,
            endLat = destination.latitude,
            endLon = destination.longitude
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routeState.collectLatest { response ->
                    response?.routes?.firstOrNull()?.geometry?.coordinates?.let { coords ->
                        routePath = coords.map { LatLng(it[1], it[0]) }
                        drawPolyline(routePath)
                    }
                }
            }
        }
    }

    private fun startTrackingUserLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationUtils.getLocationUpdates(interval = 5000L).collectLatest { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    userLatLng = newLatLng
                    // Animate marker along route
                    updateUserLocationAnimated(newLatLng)
                    // Fetch nearby stations
                    fetchNearbyStations(newLatLng)
                    checkUserStationStatus(newLatLng)
                    val reached = checkUserReachedDestination(
                        newLatLng,
                        LatLng(END_LAT.toDouble(), END_LONG.toDouble())
                    )
                    if (reached) {
                        binding.root.showMapSnackBar(getString(R.string.tracking_stopped_destination_reached))
                        this.cancel()
                        binding.buttonUpdateTripStatus.isVisible = false
                        binding.buttonEndTrip.isVisible = true
                        binding.buttonEndTrip.text = getString(R.string.end_trip)
                    }
                }
            }
        }
    }

    private fun updateUserLocationAnimated(newLatLng: LatLng) {
        if (userMarker == null) {
            userMarker = googleMap.addMarker(
                MarkerOptions().position(newLatLng).title(Constants.USER_MARKER_TITLE)
                    .icon(requireContext().toBitmapDescriptor(R.drawable.ic_current_point))
            )

            // Add source & destination markers
            googleMap.addMarker(
                MarkerOptions().position(source).title("Source: Mumbai")
                    .icon(requireContext().toBitmapDescriptor(R.drawable.ic_start_point))
            )
            googleMap.addMarker(
                MarkerOptions().position(destination).title("Destination: Indore")
                    .icon(requireContext().toBitmapDescriptor(R.drawable.ic_end_point))
            )

            // Draw route if available
            if (routePath.isNotEmpty()) drawPolyline(routePath)

            // Animate camera to show user + stations
            googleMap.animateCameraToIncludeStations(
                userLatLng = newLatLng, stations = currentStations
            )
            return
        }

        val startPosition = userMarker!!.position
        val endPosition = newLatLng

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 2000
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val lat =
                startPosition.latitude + fraction * (endPosition.latitude - startPosition.latitude)
            val lng =
                startPosition.longitude + fraction * (endPosition.longitude - startPosition.longitude)
            userMarker!!.position = LatLng(lat, lng)
        }
        valueAnimator.start()

        // Animate camera to include user + stations
        googleMap.animateCameraToIncludeStations(
            userLatLng = newLatLng, stations = currentStations
        )
    }

    private fun fetchNearbyStations(latLng: LatLng) {
        lifecycleScope.launch {
            viewModel.loadNearbyStations(
                lat = latLng.latitude,
                lon = latLng.longitude,
                range = Constants.NEARBY_STATIONS_RANGE,
                amenity = Constants.AMENITY
            )

            viewModel.stations.collectLatest { stations ->
                currentStations = stations
                redrawMarkers()
            }
        }
    }

    private fun redrawMarkers() {
        googleMap.clear()

        // User marker
        userLatLng?.let {
            userMarker = googleMap.addMarker(
                MarkerOptions().position(it).title(Constants.USER_MARKER_TITLE)
                    .icon(requireContext().toBitmapDescriptor(R.drawable.ic_current_point))
            )
        }

        // Nearby stations
        currentStations.forEach { station ->
            Log.d(TAG, "redrawMarkers: $station")
            googleMap.addMarker(
                MarkerOptions().position(LatLng(station.lat, station.lon))
                    .title(station.name ?: "Fuel Station")
                    .icon(requireContext().toBitmapDescriptor(R.drawable.ic_fuel_station))
            )
        }

        // Source & destination markers
        googleMap.addMarker(
            MarkerOptions().position(source).title("Source: Mumbai")
                .icon(requireContext().toBitmapDescriptor(R.drawable.ic_start_point))
        )
        googleMap.addMarker(
            MarkerOptions().position(destination).title("Destination: Indore")
                .icon(requireContext().toBitmapDescriptor(R.drawable.ic_end_point))
        )

        // Draw route
        if (routePath.isNotEmpty()) drawPolyline(routePath)

        // Animate camera to include user + stations
        userLatLng?.let {
            googleMap.animateCameraToIncludeStations(
                userLatLng = it, stations = currentStations
            )
        }
    }

    private fun drawPolyline(path: List<LatLng>) {
        googleMap.addPolyline(
            PolylineOptions().addAll(path).color(requireContext().getColor(R.color.blue_route))
                .width(8f)
        )
    }

    private fun checkUserStationStatus(userLatLng: LatLng) {
        val nearbyStation = currentStations.firstOrNull { isUserAtStation(userLatLng, it) }

        if (nearbyStation != null) {
            if (currentStationInRange == null || currentStationInRange?.id != nearbyStation.id) {
                currentStationInRange = nearbyStation
                stationEnterTime = System.currentTimeMillis()
            }
        } else {
            if (currentStationInRange != null) {
                val exitTime = System.currentTimeMillis()
                val duration = exitTime - stationEnterTime
                showStationPopup(currentStationInRange!!, duration)
                currentStationInRange = null
                stationEnterTime = 0L
            }
        }
    }

    private fun checkUserReachedDestination(
        userLatLng: LatLng,
        destinationLatLng: LatLng
    ): Boolean {
        val distance = FloatArray(1)
        distanceBetween(
            userLatLng.latitude, userLatLng.longitude,
            destinationLatLng.latitude, destinationLatLng.longitude,
            distance
        )
        val distanceInMeters = distance[0]
        return if (distanceInMeters <= 200) {
            true
        } else {
            false
        }
    }


    private fun isUserAtStation(userLatLng: LatLng, station: Station): Boolean {
        val results = FloatArray(1)
        distanceBetween(
            userLatLng.latitude, userLatLng.longitude, station.lat, station.lon, results
        )
        return results[0] <= STATION_RADIUS_METERS
    }

    private fun onUserMarkerClicked(latLng: LatLng) {
        // Animate camera to include user + nearby stations
        googleMap.animateCameraToIncludeStations(
            userLatLng = latLng, stations = currentStations
        )
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
