package com.example.trucktrack.ui.fragments.trip_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.model.tripSummary.TripSummaryResponse
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.Constants.KEY_ACCESS_TOKEN
import com.example.trucktrack.util.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripSummaryViewModel @Inject constructor(
    private val repository: MainRepository,
    private val sharedPref: SharedPref
) : ViewModel() {

    var tripId: Int? = null
    private val _tripSummaryResponse =
        MutableStateFlow<ApiResponse<TripSummaryResponse>>(ApiResponse.Stable())

    val tripSummaryResponse: StateFlow<ApiResponse<TripSummaryResponse>> =
        _tripSummaryResponse

    fun fetchTripSummary() {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.getTripSummary(
                    accessToken = "$BEARER ${sharedPref.getString(KEY_ACCESS_TOKEN)}",
                    tripId = tripId
                )
            }
            _tripSummaryResponse.value = results
        }
    }
}