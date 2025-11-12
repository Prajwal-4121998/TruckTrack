package com.example.trucktrack.model.trip

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StartTripRequest(
    @SerializedName("taskId") private val taskId: Int,
    @SerializedName("originLat") private val originLat: String,
    @SerializedName("originLong") private val originLong: String,
    @SerializedName("endLat") private val endLat: String,
    @SerializedName("endLong") private val endLong: String,
)
