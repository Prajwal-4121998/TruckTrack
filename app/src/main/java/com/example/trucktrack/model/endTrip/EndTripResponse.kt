package com.example.trucktrack.model.endTrip

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EndTripResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Data
)

data class Data(
    @SerializedName("tripId") val tripId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("tripSummary") val tripSummary: TripSummary
)

data class TripSummary(
    @SerializedName("totalDistance") val totalDistance: String? = null,
    @SerializedName("totalFuelLiters") val totalFuelLiters: String? = null,
    @SerializedName("totalFuelPrice") val totalFuelPrice: String? = null,
    @SerializedName("fuelEfficiency") val fuelEfficiency: String? = null,
    @SerializedName("cargoWeightDelivered") val cargoWeightDelivered: String? = null,
)