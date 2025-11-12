package com.example.trucktrack.model.resendOTP

import com.google.gson.annotations.SerializedName

data class RequestOtp(
    @SerializedName("email") val email: String,
    @SerializedName("verificationType") val verificationType: Int
)