package com.example.trucktrack.model.updateTripStatus

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateTripStatusResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Data
)

data class Data(
    @SerializedName("status") val status: String? = null,
)