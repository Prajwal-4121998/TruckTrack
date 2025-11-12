package com.example.trucktrack.model.addFuelEntry

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AddFuelResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: FuelData? = null
)

data class FuelData(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("tripId") val tripId: Int? = null,
    @SerializedName("photoUri") val photoUri: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("litres") val litres: Double? = null,
    @SerializedName("pricePerLiter") val pricePerLiter: Double? = null,
    @SerializedName("totalPrice") val totalPrice: Double? = null
)
