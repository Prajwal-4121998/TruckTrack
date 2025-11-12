package com.example.trucktrack.model.tripSummary

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TripSummaryResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TripSummary
)

@Keep
data class TripSummary(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("tripId") val tripId: Int? = null,
    @SerializedName("originLat") val originLat: Double? = null,
    @SerializedName("originLong") val originLong: Double? = null,
    @SerializedName("endLat") val endLat: Double? = null,
    @SerializedName("endLong") val endLong: Double? = null,
    @SerializedName("totalDistance") val totalDistance: String? = null,
    @SerializedName("totalFuelLiters") val totalFuelLiters: String? = null,
    @SerializedName("totalFuelPrice") val totalFuelPrice: String? = null,
    @SerializedName("fuelEfficiency") val fuelEfficiency: String? = null,
    @SerializedName("cargoWeightDelivered") val cargoWeightDelivered: String? = null
)
