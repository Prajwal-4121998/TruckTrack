package com.example.trucktrack.ui.fragments.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.model.OsrmRouteResponse
import com.example.trucktrack.model.Station
import com.example.trucktrack.model.endTrip.EndTripResponse
import com.example.trucktrack.model.trip.StartTripRequest
import com.example.trucktrack.model.trip.StartTripResponse
import com.example.trucktrack.model.updateTripStatus.UpdateTripStatusResponse
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.Constants.DEFAULT_TASK_ID
import com.example.trucktrack.util.Constants.END_LAT
import com.example.trucktrack.util.Constants.END_LONG
import com.example.trucktrack.util.Constants.KEY_ACCESS_TOKEN
import com.example.trucktrack.util.Constants.ORIGIN_LAT
import com.example.trucktrack.util.Constants.ORIGIN_LONG
import com.example.trucktrack.util.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val sharedPref: SharedPref
) : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations

    private val _routeState = MutableStateFlow<OsrmRouteResponse?>(null)
    val routeState: StateFlow<OsrmRouteResponse?> = _routeState

    private val _tripStartResponse =
        MutableStateFlow<ApiResponse<StartTripResponse>>(ApiResponse.Stable())

    val tripStartResponse: StateFlow<ApiResponse<StartTripResponse>> = _tripStartResponse

    private val _tripStatusUpdateResponse =
        MutableSharedFlow<ApiResponse<UpdateTripStatusResponse>>(replay = 0)

    val tripStatusUpdateResponse = _tripStatusUpdateResponse.asSharedFlow()


    private val _endTripResponse =
        MutableStateFlow<ApiResponse<EndTripResponse>>(ApiResponse.Stable())

    val endTripResponse: StateFlow<ApiResponse<EndTripResponse>> =
        _endTripResponse

    var tripId: Int? = null

    var isStartTripCalled: Boolean = false


    fun loadNearbyStations(lat: Double, lon: Double, range: Int, amenity: String) {
        viewModelScope.launch {
            _stations.value = try {
                repository.getNearbyStations(lat, lon, range, amenity)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    fun fetchRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double) {
        viewModelScope.launch {
            _routeState.value = try {
                repository.getRoute(startLat, startLon, endLat, endLon)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun performTripStart() {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.startTrip(
                    accessToken = "$BEARER ${sharedPref.getString(KEY_ACCESS_TOKEN)}",
                    StartTripRequest(
                        taskId = DEFAULT_TASK_ID,
                        originLat = ORIGIN_LAT,
                        originLong = ORIGIN_LONG,
                        endLat = END_LAT,
                        endLong = END_LONG
                    )
                )
            }
            _tripStartResponse.value = results
        }
    }

    fun performTripStatusUpdate() {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.updateTripStatus(
                    accessToken = "$BEARER ${sharedPref.getString(KEY_ACCESS_TOKEN)}",
                    tripId = tripId
                )
            }
            _tripStatusUpdateResponse.emit(results)
        }
    }

    fun performEndTrip() {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.endTrip(
                    accessToken = "$BEARER ${sharedPref.getString(KEY_ACCESS_TOKEN)}",
                    tripId = tripId
                )
            }
            _endTripResponse.value = results
        }
    }
}
