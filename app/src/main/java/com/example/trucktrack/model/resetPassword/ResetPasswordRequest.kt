package com.example.trucktrack.model.resetPassword

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ResetPasswordRequest(
    @SerializedName("userId") private val userId: Int,
    @SerializedName("password") private val password: String,
)
