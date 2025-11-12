package com.example.trucktrack.model.trip

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StartTripResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Data
)

data class Data(
    @SerializedName("id") val id: Int,
    @SerializedName("taskId") val taskId: Int,
    @SerializedName("originLat") val originLat: Double,
    @SerializedName("originLong") val originLong: Double,
    @SerializedName("endLat") val endLat: Double,
    @SerializedName("endLong") val endLong: Double,
    @SerializedName("status") val status: String,
)