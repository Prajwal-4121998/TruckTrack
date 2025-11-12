package com.example.trucktrack.model.resetPassword

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ResetPasswordResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: String? = null,
)