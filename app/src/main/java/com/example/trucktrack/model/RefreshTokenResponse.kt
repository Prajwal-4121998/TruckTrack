package com.example.trucktrack.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TokenData
)

data class TokenData(
    @SerializedName("refreshToken") val refreshToken: String
)
