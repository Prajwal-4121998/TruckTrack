package com.example.trucktrack.ui.fragments.login

import LoginResponse
import TokenData
import UserInfo
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.databinding.FragmentLoginBinding
import com.example.trucktrack.model.RefreshTokenResponse
import com.example.trucktrack.model.login.LoginRequest
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.rest.WebConstants.PLATFORM
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.KEY_ACCESS_TOKEN
import com.example.trucktrack.util.Constants.KEY_EMAIL_ID
import com.example.trucktrack.util.Constants.KEY_PASSWORD
import com.example.trucktrack.util.Constants.KEY_REMEMBER_ME
import com.example.trucktrack.util.Constants.KEY_USER_DATA
import com.example.trucktrack.util.Constants.KEY_USER_ID
import com.example.trucktrack.util.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: MainRepository,
    private val sharedPref: SharedPref
) : ViewModel() {

    private val _tokenResponse =
        MutableStateFlow<ApiResponse<RefreshTokenResponse>>(ApiResponse.Stable())

    val tokenResponse: StateFlow<ApiResponse<RefreshTokenResponse>> = _tokenResponse

    var token: String? = null

    private val _loginResponse =
        MutableStateFlow<ApiResponse<LoginResponse>>(ApiResponse.Stable())

    val loginResponse: StateFlow<ApiResponse<LoginResponse>> = _loginResponse
    fun fetchRefreshToken() {
        viewModelScope.launch {
            _tokenResponse.value = ApiResponse.Loading()

            val result = safeApiCall {
                repository.refreshToken(PLATFORM)
            }

            _tokenResponse.value = result
        }
    }

    fun performLogin(loginRequest: LoginRequest) {
        viewModelScope.launch {
            _loginResponse.value = ApiResponse.Loading()

            val result = safeApiCall {
                repository.performLogin(token, loginRequest)
            }

            _loginResponse.value = result
        }
    }

    fun setViewAccessible(binding: FragmentLoginBinding, isAccessible: Boolean) {
        binding.buttonContinue.isEnabled = isAccessible
        binding.edtEmail.isEnabled = isAccessible
        binding.edtPassword.isEnabled = isAccessible
        binding.cbRemember.isEnabled = isAccessible
        binding.pbLogin.isVisible = !isAccessible
    }

    fun storeUserData(response: UserInfo) {
        response.let { user ->
            sharedPref.put(KEY_USER_DATA, user)
            sharedPref.put(KEY_USER_ID, user.id)
        }
    }

    fun storeAccessToken(tokenData: TokenData) {
        tokenData.accessToken.let {
            sharedPref.put(KEY_ACCESS_TOKEN, it)
        }
    }

    fun setRememberMe(binding: FragmentLoginBinding) {
        sharedPref.put(KEY_REMEMBER_ME, binding.cbRemember.isChecked)
        if (binding.cbRemember.isChecked) {
            sharedPref.put(KEY_EMAIL_ID, binding.edtEmail.text.toString())
            sharedPref.put(KEY_PASSWORD, binding.edtPassword.text.toString())
        }
    }

}