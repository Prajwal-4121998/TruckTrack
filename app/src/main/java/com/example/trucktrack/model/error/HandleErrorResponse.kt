package com.example.trucktrack.model.error

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HandleErrorResponse(
    @SerializedName("responseCode") val responseCode: Int,
    @SerializedName("responseMessage") val responseMessage: String
)