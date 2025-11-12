package com.example.trucktrack.ui.fragments.sign_up

import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.databinding.FragmentSignUpBinding
import com.example.trucktrack.model.RefreshTokenResponse
import com.example.trucktrack.model.signup.SignupResponse
import com.example.trucktrack.model.signup.UserInfo
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.rest.WebConstants.PLATFORM
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.KEY_USER_DATA
import com.example.trucktrack.util.Constants.KEY_USER_ID
import com.example.trucktrack.util.SharedPref
import com.example.trucktrack.util.createPartFromString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: MainRepository,
    private val sharedPref: SharedPref
) : ViewModel() {
    val emailPattern: Pattern = PatternsCompat.EMAIL_ADDRESS
    val passwordPattern =
        Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{6,15}$")

    private val _tokenResponse =
        MutableStateFlow<ApiResponse<RefreshTokenResponse>>(ApiResponse.Stable())

    val tokenResponse: StateFlow<ApiResponse<RefreshTokenResponse>> = _tokenResponse

    var token: String? = null

    var profileImageFile: File? = null

    private val _signupResponse =
        MutableStateFlow<ApiResponse<SignupResponse>>(ApiResponse.Stable())
    val signupResponse: StateFlow<ApiResponse<SignupResponse>> = _signupResponse

    fun fetchRefreshToken() {
        viewModelScope.launch {
            _tokenResponse.value = ApiResponse.Loading()

            val result = safeApiCall {
                repository.refreshToken(PLATFORM)
            }

            _tokenResponse.value = result
        }
    }

    fun registerUser(name: String, email: String, password: String) {
        token?.let { token ->
            val requestName = name.trim().createPartFromString()
            val requestEmail = email.trim().createPartFromString()
            val requestPassword = password.trim().createPartFromString()

            var requestProfileImage: MultipartBody.Part? = null
            profileImageFile?.let { image ->
                val reqFile: RequestBody = image.asRequestBody("image/*".toMediaTypeOrNull())
                requestProfileImage =
                    MultipartBody.Part.createFormData("profileImage", image.name, reqFile)
            }

            viewModelScope.launch {
                _signupResponse.value = ApiResponse.Loading()

                val result = safeApiCall {
                    repository.registerUser(
                        token,
                        requestName,
                        requestEmail,
                        requestPassword,
                        requestProfileImage
                    )
                }

                _signupResponse.value = result
            }
        }
    }

    fun setViewAccessible(
        binding: FragmentSignUpBinding,
        isAccessible: Boolean,
    ) {
        binding.buttonContinue.isEnabled = isAccessible
        binding.edtName.isEnabled = isAccessible
        binding.edtEmail.isEnabled = isAccessible
        binding.edtPassword.isEnabled = isAccessible
        binding.cbAgree.isEnabled = isAccessible
        binding.pbLogin.isVisible = !isAccessible

    }

    fun storeUserData(response: UserInfo) {
        response.let { user ->
            sharedPref.put(KEY_USER_DATA, user)
            sharedPref.put(KEY_USER_ID, user.id)
        }
    }
}