package com.example.trucktrack.rest

import LoginResponse
import com.example.trucktrack.model.OsrmRouteResponse
import com.example.trucktrack.model.OverpassResponse
import com.example.trucktrack.model.RefreshTokenResponse
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
import com.example.trucktrack.rest.WebConstants.ADD_FUEL_ENTRY
import com.example.trucktrack.rest.WebConstants.END_TRIP
import com.example.trucktrack.rest.WebConstants.FORGOT_PASSWORD
import com.example.trucktrack.rest.WebConstants.GET_TOKEN
import com.example.trucktrack.rest.WebConstants.LOGIN
import com.example.trucktrack.rest.WebConstants.OSRM_ROUTE_PATH
import com.example.trucktrack.rest.WebConstants.OVERPASS_API_ENDPOINT_URL
import com.example.trucktrack.rest.WebConstants.REGISTER
import com.example.trucktrack.rest.WebConstants.RESEND_OTP
import com.example.trucktrack.rest.WebConstants.RESET_PASSWORD
import com.example.trucktrack.rest.WebConstants.START_TRIP
import com.example.trucktrack.rest.WebConstants.TRIP_SUMMARY
import com.example.trucktrack.rest.WebConstants.UPDATE_TRIP_STATUS
import com.example.trucktrack.rest.WebConstants.VERIFY_OTP
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AppApiInterface {
    @FormUrlEncoded
    @POST(OVERPASS_API_ENDPOINT_URL)
    suspend fun getFuelStations(
        @Field("data") query: String
    ): OverpassResponse

    @GET(OSRM_ROUTE_PATH)
    suspend fun getRoute(
        @Path("startLon") startLon: Double,
        @Path("startLat") startLat: Double,
        @Path("endLon") endLon: Double,
        @Path("endLat") endLat: Double,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): OsrmRouteResponse

    @GET(GET_TOKEN)
    suspend fun getRefreshToken(
        @Header("platform") platform: String?,
    ): RefreshTokenResponse


    @POST(LOGIN)
    suspend fun performLogin(
        @Header("authorization") authorization: String?,
        @Body requestBody: LoginRequest
    ): LoginResponse

    @Multipart
    @POST(REGISTER)
    suspend fun registerUser(
        @Header("authorization") authorization: String?,
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): SignupResponse

    @POST(VERIFY_OTP)
    suspend fun verifyRegisterOTP(
        @Header("authorization") authorization: String?,
        @Body requestBody: VerifyOtpRequest
    ): VerifyOtpResponse

    @POST(RESEND_OTP)
    suspend fun resendOTP(
        @Header("authorization") authorization: String?,
        @Body requestBody: RequestOtp
    ): ResendOtpResponse

    @POST(FORGOT_PASSWORD)
    suspend fun forgetPassword(
        @Header("authorization") authorization: String?,
        @Body requestBody: ForgetPasswordRequest
    ): ForgetPasswordResponse

    @POST(RESET_PASSWORD)
    suspend fun resetPassword(
        @Header("authorization") authorization: String?,
        @Body requestBody: ResetPasswordRequest
    ): ResetPasswordResponse

    @PUT(START_TRIP)
    suspend fun startTrip(
        @Header("authorization") authorization: String?,
        @Body requestBody: StartTripRequest
    ): StartTripResponse

    @POST(UPDATE_TRIP_STATUS)
    suspend fun updateTripStatus(
        @Header("authorization") authorization: String?,
        @Query("tripId") tripId: Int?
    ): UpdateTripStatusResponse

    @POST(END_TRIP)
    suspend fun endTrip(
        @Header("authorization") authorization: String?,
        @Query("tripId") tripId: Int?
    ): EndTripResponse

    @Multipart
    @PUT(ADD_FUEL_ENTRY)
    suspend fun addFuelEntry(
        @Header("authorization") authorization: String?,
        @Part("tripId") tripId: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("litres") litres: RequestBody?,
        @Part("pricePerLiter") pricePerLiter: RequestBody?,
        @Part("totalPrice") totalPrice: RequestBody?,
        @Part fuelImage: MultipartBody.Part?
    ): AddFuelResponse

    @GET(TRIP_SUMMARY)
    suspend fun getTripSummary(
        @Header("authorization") authorization: String?,
        @Query("tripId") tripId: Int?
    ): TripSummaryResponse
}
