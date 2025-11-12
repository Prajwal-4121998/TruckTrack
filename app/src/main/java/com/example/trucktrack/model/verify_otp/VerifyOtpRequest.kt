package com.example.trucktrack.model.verify_otp

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VerifyOtpRequest(
    @SerializedName("email") private val email: String,
    @SerializedName("otp") private val otp: String,
    @SerializedName("verificationType") private val verificationType: Int,

)
