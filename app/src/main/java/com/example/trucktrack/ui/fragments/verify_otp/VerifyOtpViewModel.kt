package com.example.trucktrack.ui.fragments.verify_otp

import android.os.CountDownTimer
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.R
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.databinding.FragmentVerifyOtpBinding
import com.example.trucktrack.model.RefreshTokenResponse
import com.example.trucktrack.model.resendOTP.RequestOtp
import com.example.trucktrack.model.resendOTP.ResendOtpResponse
import com.example.trucktrack.model.verify_otp.VerifyOtpRequest
import com.example.trucktrack.model.verify_otp.VerifyOtpResponse
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.rest.WebConstants.PLATFORM
import com.example.trucktrack.sealed.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val repository: MainRepository,
) : ViewModel() {
    var keyFromSignUp: Boolean = false
    lateinit var userEmail: String
    private val _tokenResponse =
        MutableStateFlow<ApiResponse<RefreshTokenResponse>>(ApiResponse.Stable())

    val tokenResponse: StateFlow<ApiResponse<RefreshTokenResponse>> = _tokenResponse

    val _verifyOtpResponse =
        MutableStateFlow<ApiResponse<VerifyOtpResponse>>(ApiResponse.Stable())
    val verifyOtpResponse: StateFlow<ApiResponse<VerifyOtpResponse>> = _verifyOtpResponse

    private val _resendOtpResponse =
        MutableStateFlow<ApiResponse<ResendOtpResponse>>(ApiResponse.Stable())
    val resendOtpResponse: StateFlow<ApiResponse<ResendOtpResponse>> = _resendOtpResponse

    var token: String? = null

    fun fetchRefreshToken() {
        viewModelScope.launch {
            _tokenResponse.value = ApiResponse.Loading()

            val result = safeApiCall {
                repository.refreshToken(PLATFORM)
            }

            _tokenResponse.value = result
        }
    }

    fun callVerifyOTP(otp: String) {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.verifyOTP(
                    token,
                    VerifyOtpRequest(
                        email = userEmail,
                        otp = otp,
                        verificationType = if (keyFromSignUp) 0 else 1
                    )
                )
            }
            _verifyOtpResponse.value = results
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.resendOTP(
                    token,
                    RequestOtp(
                        email = userEmail,
                        verificationType = if (keyFromSignUp) 0 else 1
                    )
                )
            }
            _resendOtpResponse.value = results
        }
    }

    fun resendTimer(binding: FragmentVerifyOtpBinding) {
        binding.tvResend.visibility = View.VISIBLE
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                val timeLeftFormatted =
                    binding.tvResend.context.getString(R.string.resend_in) + " " + String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        minutes,
                        seconds
                    )
                binding.tvResend.text = timeLeftFormatted
                binding.tvResend.isEnabled = false
            }

            override fun onFinish() {
                binding.tvResend.text = binding.tvResend.context.getString(R.string.resend_otp)
                binding.tvResend.isEnabled = true
            }
        }.start()
    }

    fun clearState() {
        _verifyOtpResponse.value = ApiResponse.Stable()
        _resendOtpResponse.value = ApiResponse.Stable()
    }
}