package com.example.trucktrack.model.forgotPassword

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ForgetPasswordRequest(
    @SerializedName("email") private val email: String
)
