package com.example.trucktrack.ui.fragments.forgot_password

import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.databinding.FragmentForgotPasswordBinding
import com.example.trucktrack.model.RefreshTokenResponse
import com.example.trucktrack.model.forgotPassword.ForgetPasswordRequest
import com.example.trucktrack.model.forgotPassword.ForgetPasswordResponse
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.rest.WebConstants.PLATFORM
import com.example.trucktrack.sealed.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: MainRepository,
) : ViewModel() {
    val emailPattern: Pattern = PatternsCompat.EMAIL_ADDRESS
    private val _tokenResponse =
        MutableStateFlow<ApiResponse<RefreshTokenResponse>>(ApiResponse.Stable())

    val tokenResponse: StateFlow<ApiResponse<RefreshTokenResponse>> = _tokenResponse

    private val _forgotPasswordResponse =
        MutableStateFlow<ApiResponse<ForgetPasswordResponse>>(ApiResponse.Stable())
    val forgotPasswordResponse: StateFlow<ApiResponse<ForgetPasswordResponse>> =
        _forgotPasswordResponse

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

    fun performForgerPassword(userEmail: String) {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.forgetPassword(
                    token,
                    ForgetPasswordRequest(
                        email = userEmail,
                    )
                )
            }
            _forgotPasswordResponse.value = results
        }
    }

    fun setViewAccessible(binding: FragmentForgotPasswordBinding, isAccessible: Boolean) {
        binding.buttonContinue.isEnabled = isAccessible
        binding.edtEmail.isEnabled = isAccessible
        binding.pbLogin.isVisible = !isAccessible
    }

    fun clearState() {
        _forgotPasswordResponse.value = ApiResponse.Stable()
    }
}