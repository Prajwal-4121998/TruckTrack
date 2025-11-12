package com.example.trucktrack.sealed

import com.example.trucktrack.model.error.HandleErrorResponse


sealed class ApiResponse<T> {
    class Success<T>(val data: T) : ApiResponse<T>()
    class Failure<T>(val data: HandleErrorResponse) : ApiResponse<T>()
    class Loading<T> : ApiResponse<T>()
    class Stable<T> : ApiResponse<T>()
}