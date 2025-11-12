package com.example.trucktrack.rest

object WebConstants {
    const val OVERPASS_API_ENDPOINT_URL = "api/interpreter"
    const val OSRM_ROUTE_PATH = "route/v1/driving/{startLon},{startLat};{endLon},{endLat}"
    const val REQUEST_TIMEOUT = 30L
    const val KEY_DEVICE_TYPE = "android"
    var KEY_DEVICE_TOKEN = "1234"
    const val KEY_IS_TEST_DATA = "1" /*if (BuildConfig.DEBUG) "1" else "0"*/
    const val ENDPOINT_URL = "/device/v1"
    const val ENDPOINT_AUTH_MODULE = "/auth/"

    const val ENDPOINT_HOME_MODULE = "/home/"
    const val GET_TOKEN = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "refreshToken"

    const val LOGIN  = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "login"
    const val REGISTER = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "register"
    const val VERIFY_OTP = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "verifyOtp"
    const val RESEND_OTP = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "resendOtp"
    const val FORGOT_PASSWORD = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "forgotPassword"

    const val RESET_PASSWORD = ENDPOINT_URL + ENDPOINT_AUTH_MODULE + "resetPassword"

    const val START_TRIP = ENDPOINT_URL +ENDPOINT_HOME_MODULE + "startTrip"
    const val UPDATE_TRIP_STATUS = ENDPOINT_URL +ENDPOINT_HOME_MODULE + "pauseRestartTrip"
    const val END_TRIP = ENDPOINT_URL +ENDPOINT_HOME_MODULE + "endTrip"
    const val ADD_FUEL_ENTRY = ENDPOINT_URL + ENDPOINT_HOME_MODULE + "addFuelEntry"
    const val TRIP_SUMMARY = ENDPOINT_URL + ENDPOINT_HOME_MODULE + "fetchTripSummary"
    const val PLATFORM = "android"
}