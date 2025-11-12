package com.example.trucktrack.model.forgotPassword

import com.google.gson.annotations.SerializedName

data class ForgetPasswordResponse(
    @SerializedName("data") val data: Data? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: String? = null
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
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("isFromSignUp") val isFromSignUp: Boolean
)