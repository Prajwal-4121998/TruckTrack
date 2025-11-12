package com.example.trucktrack.core

import com.example.trucktrack.model.error.HandleErrorResponse
import com.example.trucktrack.sealed.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResponse<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            ApiResponse.Success(response)
        } catch (e: HttpException) {
            val code = e.code()
            val message = try {
                val errorJson = e.response()?.errorBody()?.string()
                val jsonObject = JSONObject(errorJson ?: "{}")
                jsonObject.optString(
                    "message",
                    genericMessageForCode(code)
                )
            } catch (_: Exception) {
                genericMessageForCode(code)
            }

            ApiResponse.Failure(
                HandleErrorResponse(
                    responseCode = code,
                    responseMessage = message
                )
            )
        } catch (e: Exception) {
            ApiResponse.Failure(
                HandleErrorResponse(
                    responseCode = 500,
                    responseMessage = e.message ?: "Something went wrong"
                )
            )
        }
    }
}

private fun genericMessageForCode(code: Int): String = when (code) {
    400 -> "Bad Request"
    401 -> "Unauthorized"
    403 -> "Forbidden"
    404 -> "Not Found"
    422 -> "Validation Error"
    500 -> "Internal Server Error"
    else -> "Something went wrong"
}
