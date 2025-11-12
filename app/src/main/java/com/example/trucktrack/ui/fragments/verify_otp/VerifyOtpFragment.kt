package com.example.trucktrack.ui.fragments.verify_otp

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentVerifyOtpBinding
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.Constants.KEY_LOGGED_IN
import com.example.trucktrack.util.checkInternetConnection
import com.example.trucktrack.util.hideKeyboard
import com.example.trucktrack.util.mainNav
import com.example.trucktrack.util.showAPIErrorSnackBar
import com.example.trucktrack.util.showNoInternetSnackBar
import com.example.trucktrack.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyOtpFragment :
    BaseFragment<FragmentVerifyOtpBinding>(FragmentVerifyOtpBinding::inflate) {
    private val viewModel: VerifyOtpViewModel by viewModels()

    private val args: VerifyOtpFragmentArgs by navArgs()

    override fun onViewCreated() {
        viewModel.userEmail = args.keyEmail
        viewModel.keyFromSignUp = args.keyFromSignup
        binding.tvEmail.text = args.keyEmail
        binding.otpView.setOtpCompletionListener {
            mContext.hideKeyboard(binding.otpView)
        }
        viewModel.resendTimer(binding)
        iniClickListener()
        viewModel.fetchRefreshToken()
        initObserver()
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

                        is ApiResponse.Loading -> {}

                        is ApiResponse.Failure -> {
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
                        }
                    }
                }

                launch {
                    viewModel.verifyOtpResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            if (args.keyFromSignup) {
                                sharedPref.put(KEY_LOGGED_IN, true)
                                mainNav.navigate(VerifyOtpFragmentDirections.actionVerifyOtpFragmentToMainFragment())
                            } else {
                                mainNav.navigate(
                                    VerifyOtpFragmentDirections.actionVerifyOtpFragmentToResetPasswordFragment(
                                        args.keyUserId
                                    )
                                )
                            }
                        }
                    }
                }

                launch {
                    viewModel.resendOtpResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            binding.pbResendOtp.visibility = View.GONE
                            binding.tvResend.visibility = View.VISIBLE
                            data.message?.let { message ->
                                binding.root.showAPIErrorSnackBar(message, true)
                            }
                            viewModel.clearState()
                        }
                    }
                }
            }
        }
    }

    override fun onViewClicked(view: View) {
        when (view) {
            binding.ivBack -> mainNav.popBackStack()

            binding.buttonVerify -> {
                if (binding.otpView.text?.length == 4) {
                    if (mContext.checkInternetConnection()) {
                        viewModel.callVerifyOTP(binding.otpView.text.toString())
                    } else {
                        binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                    }
                } else {
                    mContext.showToast(resources.getString(R.string.please_enter_valid_otp))
                }
            }

            binding.tvResend -> {
                if (mContext.checkInternetConnection()) {
                    viewModel.resendTimer(binding)
                    viewModel.fetchRefreshToken()
                    viewModel.resendOtp()
                } else {
                    binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                }
            }
        }
    }

    private fun iniClickListener() {
        binding.tvResend.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.buttonVerify.setOnClickListener(this)
    }

}