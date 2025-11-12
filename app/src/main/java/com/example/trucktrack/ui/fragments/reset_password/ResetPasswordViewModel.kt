package com.example.trucktrack.ui.fragments.reset_password

import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.databinding.FragmentResetPasswordBinding
import com.example.trucktrack.model.RefreshTokenResponse
import com.example.trucktrack.model.resetPassword.ResetPasswordRequest
import com.example.trucktrack.model.resetPassword.ResetPasswordResponse
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.rest.WebConstants.PLATFORM
import com.example.trucktrack.sealed.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repository: MainRepository,
) : ViewModel() {
    val passwordPattern =
        Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{6,15}$")

    private val _tokenResponse =
        MutableStateFlow<ApiResponse<RefreshTokenResponse>>(ApiResponse.Stable())

    val tokenResponse: StateFlow<ApiResponse<RefreshTokenResponse>> = _tokenResponse

    private val _resetPasswordResponse =
        MutableStateFlow<ApiResponse<ResetPasswordResponse>>(ApiResponse.Stable())
    val resetPasswordResponse: StateFlow<ApiResponse<ResetPasswordResponse>> =
        _resetPasswordResponse

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

    fun resetPasswordAPI(userId: Int, newPassword: String) {
        viewModelScope.launch {
            val results = safeApiCall {
                repository.resetPassword(
                    token,
                    ResetPasswordRequest(
                        userId,
                        newPassword,
                    )
                )
            }
            _resetPasswordResponse.value = results
        }
    }

    fun setViewAccessible(binding: FragmentResetPasswordBinding, isAccessible: Boolean) {
        binding.buttonSave.isEnabled = isAccessible
        binding.etConfirmPassword.isEnabled = isAccessible
        binding.etNewPassword.isVisible = !isAccessible
        binding.ivBack.isVisible = !isAccessible
        binding.pbLogin.isVisible = !isAccessible
    }
}