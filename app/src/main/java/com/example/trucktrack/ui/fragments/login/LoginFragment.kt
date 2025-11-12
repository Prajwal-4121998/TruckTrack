package com.example.trucktrack.ui.fragments.login

import android.annotation.SuppressLint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.trucktrack.BuildConfig
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentLoginBinding
import com.example.trucktrack.model.login.LoginRequest
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.Constants.KEY_LOGGED_IN
import com.example.trucktrack.util.Constants.PRIVACY_POLICY_URL
import com.example.trucktrack.util.Constants.TERMS_CONDITION_URL
import com.example.trucktrack.util.checkInternetConnection
import com.example.trucktrack.util.mainNav
import com.example.trucktrack.util.showAPIErrorSnackBar
import com.example.trucktrack.util.showNoInternetSnackBar
import com.example.trucktrack.util.showUserNotVerified
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val viewModel: LoginViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated() {
        binding.cbRemember.buttonTintList = null
        initTextWatcher()
        initClickListener()
        initObserver()
        if (BuildConfig.DEBUG) {
            binding.edtEmail.setText("pda@narola.email")
            binding.edtPassword.setText("Pass123#")
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                suspend fun <T> handleApiResponse(
                    response: ApiResponse<T>,
                    onSuccess: suspend (T) -> Unit
                ) {
                    when (response) {
                        is ApiResponse.Stable -> Unit

                        is ApiResponse.Loading -> {
                            binding.buttonContinue.text = ""
                            viewModel.setViewAccessible(binding, false)
                        }

                        is ApiResponse.Failure -> {
                            binding.buttonContinue.text = getString(R.string.continue_string)
                            viewModel.setViewAccessible(binding, true)
                            binding.root.showAPIErrorSnackBar(response.data.responseMessage)
                        }

                        is ApiResponse.Success -> {
                            onSuccess(response.data)
                        }
                    }
                }

                launch {
                    viewModel.tokenResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            viewModel.token = "$BEARER ${data.data.refreshToken}"
                            viewModel.performLogin(
                                LoginRequest(
                                    binding.edtEmail.text.toString(),
                                    binding.edtPassword.text.toString()
                                )
                            )
                        }
                    }
                }

                launch {
                    viewModel.loginResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            binding.buttonContinue.text = getString(R.string.continue_string)
                            viewModel.setViewAccessible(binding, true)
                            viewModel.storeAccessToken(data.data.tokenData)
                            data.data.userInfo.let { userInfo ->
                                viewModel.storeUserData(userInfo)
                                if (userInfo.isVerified) {
                                    sharedPref.put(KEY_LOGGED_IN, true)
                                    viewModel.setRememberMe(binding)
                                    mainNav.navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
                                } else {
                                    binding.root.showUserNotVerified(resources.getString(R.string.user_not_verified)) {
                                        mainNav.navigate(
                                            LoginFragmentDirections.actionLoginFragmentToVerifyOtpFragment(
                                                true,
                                                binding.edtEmail.text.toString(),
                                                userInfo.id
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initClickListener() {
        binding.buttonContinue.setOnClickListener(this)
        binding.tvSignUp.setOnClickListener(this)
        binding.tvShowPassword.setOnClickListener(this)
        binding.tvForgetPassword.setOnClickListener(this)
        binding.tvPrivacyPolicy.setOnClickListener(this)
        binding.tvTermsCondition.setOnClickListener(this)
    }

    private fun initTextWatcher() {
        with(binding) {
            edtEmail.doAfterTextChanged {
                edtEmailError.text = null
            }
            edtPassword.doAfterTextChanged {
                tvPasswordError.text = null
            }
        }
    }

    override fun onViewClicked(view: View) {
        when (view) {
            binding.tvShowPassword -> {
                if (binding.tvShowPassword.text.toString() == mContext.getString(R.string.show)) {
                    binding.edtPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    binding.tvShowPassword.text = mContext.getString(R.string.hide)
                } else {
                    binding.edtPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    binding.tvShowPassword.text = mContext.getString(R.string.show)
                }
                binding.edtPassword.text?.let { binding.edtPassword.setSelection(it.length) }
            }

            binding.buttonContinue -> {
                var errorCount = 0
                if (binding.edtEmail.text.isNullOrBlank()) {
                    binding.edtEmailError.text = resources.getString(R.string.please_enter_email_id)
                    errorCount++
                }
                if (binding.edtPassword.text.isNullOrBlank()) {
                    binding.tvPasswordError.text =
                        resources.getString(R.string.please_enter_password)
                    errorCount++
                }
                if (errorCount > 0) return

                if (mContext.checkInternetConnection()) {
                    viewModel.fetchRefreshToken()
                } else {
                    binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                }
            }

            binding.tvSignUp -> mainNav.navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
            binding.tvForgetPassword -> mainNav.navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
            binding.tvPrivacyPolicy -> mainNav.navigate(
                LoginFragmentDirections.actionLoginFragmentToWebLoaderFragment3(
                    PRIVACY_POLICY_URL, resources.getString(R.string.privacy_policy)
                )
            )

            binding.tvTermsCondition -> mainNav.navigate(
                LoginFragmentDirections.actionLoginFragmentToWebLoaderFragment3(
                    TERMS_CONDITION_URL, resources.getString(R.string.terms_condition)
                )
            )
        }
    }
}