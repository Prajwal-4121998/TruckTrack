package com.example.trucktrack.model.login

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LoginRequest(
    @SerializedName("email") var email: String,
    @SerializedName("password") var password: String
)