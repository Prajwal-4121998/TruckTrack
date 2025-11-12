package com.example.trucktrack.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationUtils @Inject constructor(
    private val context: Context
) {

    private val destination = LatLng(22.7196, 75.8577)

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(interval: Long = 3000L): Flow<Location> = callbackFlow {

        fun checkDestinationReached(location: Location): Boolean {
            val distance = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                destination.latitude,
                destination.longitude,
                distance
            )
            return distance[0] <= 100
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, interval
        ).setMinUpdateIntervalMillis(interval).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    trySend(location)

                    if (checkDestinationReached(location)) {
                        fusedClient.removeLocationUpdates(this)
                        close()
                        break
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Cancel listener when flow is closed
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }
}
