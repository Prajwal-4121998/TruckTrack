package com.example.trucktrack.rest

import LoginResponse
import com.example.trucktrack.di.OsrmApiQualifier
import com.example.trucktrack.di.OverpassApiQualifier
import com.example.trucktrack.model.OsrmRouteResponse
import com.example.trucktrack.model.RefreshTokenResponse
import com.example.trucktrack.model.Station
import com.example.trucktrack.model.addFuelEntry.AddFuelResponse
import com.example.trucktrack.model.endTrip.EndTripResponse
import com.example.trucktrack.model.forgotPassword.ForgetPasswordRequest
import com.example.trucktrack.model.forgotPassword.ForgetPasswordResponse
import com.example.trucktrack.model.login.LoginRequest
import com.example.trucktrack.model.resendOTP.RequestOtp
import com.example.trucktrack.model.resendOTP.ResendOtpResponse
import com.example.trucktrack.model.resetPassword.ResetPasswordRequest
import com.example.trucktrack.model.resetPassword.ResetPasswordResponse
import com.example.trucktrack.model.signup.SignupResponse
import com.example.trucktrack.model.trip.StartTripRequest
import com.example.trucktrack.model.trip.StartTripResponse
import com.example.trucktrack.model.tripSummary.TripSummaryResponse
import com.example.trucktrack.model.updateTripStatus.UpdateTripStatusResponse
import com.example.trucktrack.model.verify_otp.VerifyOtpRequest
import com.example.trucktrack.model.verify_otp.VerifyOtpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class MainRepository @Inject constructor(
    @OverpassApiQualifier private val overpassApi: AppApiInterface,
    @OsrmApiQualifier private val osrmApi: AppApiInterface,
    private val mainApi: AppApiInterface
) {
    suspend fun getNearbyStations(
        lat: Double, lon: Double, range: Int = 5000, amenity: String
    ): List<Station> {
        val query = """
            [out:json][timeout:25];
            (
              node["amenity"=$amenity](around:$range,$lat,$lon);
            );
            out body;
        """.trimIndent()

        val response = overpassApi.getFuelStations(query)
        return response.elements.mapNotNull {
            val latE = it.lat
            val lonE = it.lon
            if (latE != null && lonE != null) Station(
                it.id, it.tags?.get("name"), latE, lonE
            ) else null
        }
    }

    suspend fun getRoute(
        startLat: Double, startLon: Double, endLat: Double, endLon: Double
    ): OsrmRouteResponse = osrmApi.getRoute(startLon, startLat, endLon, endLat)

    suspend fun refreshToken(
        platform: String?
    ): RefreshTokenResponse = mainApi.getRefreshToken(platform)

    suspend fun performLogin(
        token: String?,
        loginRequest: LoginRequest
    ): LoginResponse =
        mainApi.performLogin(token, loginRequest)

    suspend fun registerUser(
        token: String?,
        requestName: RequestBody,
        requestEmail: RequestBody,
        requestPassword: RequestBody,
        requestProfileImage: MultipartBody.Part?,
    ): SignupResponse =
        mainApi.registerUser(
            token,
            requestName,
            requestEmail,
            requestPassword,
            requestProfileImage
        )

    suspend fun verifyOTP(token: String?, verifyOtpRequest: VerifyOtpRequest): VerifyOtpResponse =
        mainApi.verifyRegisterOTP(token, verifyOtpRequest)

    suspend fun resendOTP(token: String?, requestOtp: RequestOtp): ResendOtpResponse =
        mainApi.resendOTP(token, requestOtp)

    suspend fun forgetPassword(
        token: String?,
        forgetPasswordRequest: ForgetPasswordRequest
    ): ForgetPasswordResponse =
        mainApi.forgetPassword(token, forgetPasswordRequest)

    suspend fun resetPassword(
        token: String?,
        resetPasswordRequest: ResetPasswordRequest
    ): ResetPasswordResponse =
        mainApi.resetPassword(token, resetPasswordRequest)

    suspend fun startTrip(
        accessToken: String?,
        startTripRequest: StartTripRequest
    ): StartTripResponse =
        mainApi.startTrip(accessToken, startTripRequest)

    suspend fun updateTripStatus(
        accessToken: String?,
        tripId: Int?
    ): UpdateTripStatusResponse = mainApi.updateTripStatus(accessToken, tripId)

    suspend fun endTrip(
        accessToken: String?,
        tripId: Int?
    ): EndTripResponse = mainApi.endTrip(accessToken, tripId)

    suspend fun addFuelEntry(
        accessToken: String?,
        tripId: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        litres: RequestBody,
        pricePerLiter: RequestBody,
        totalPrice: RequestBody,
        fuelImage: MultipartBody.Part?,
    ): AddFuelResponse =
        mainApi.addFuelEntry(
            accessToken,
            tripId,
            latitude,
            longitude,
            litres,
            pricePerLiter,
            totalPrice,
            fuelImage
        )

    suspend fun getTripSummary(
        accessToken: String?,
        tripId: Int?
    ): TripSummaryResponse = mainApi.getTripSummary(accessToken, tripId)

}
