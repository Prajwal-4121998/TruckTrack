package com.example.trucktrack.model.verify_otp

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Data
)

data class Data(
    @SerializedName("userInfo") val userInfo: UserInfo,
)

data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("image") val image: String,
    @SerializedName("role") val role: String,
    @SerializedName("isVerified") val isVerified: Boolean
)
