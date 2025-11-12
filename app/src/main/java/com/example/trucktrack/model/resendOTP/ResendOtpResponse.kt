package com.example.trucktrack.model.resendOTP

import com.google.gson.annotations.SerializedName

data class ResendOtpResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: String? = null,
)
