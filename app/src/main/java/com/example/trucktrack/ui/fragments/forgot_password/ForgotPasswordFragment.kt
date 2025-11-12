package com.example.trucktrack.ui.fragments.forgot_password

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentForgotPasswordBinding
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.checkInternetConnection
import com.example.trucktrack.util.mainNav
import com.example.trucktrack.util.showAPIErrorSnackBar
import com.example.trucktrack.util.showNoInternetSnackBar
import com.example.trucktrack.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>(
    FragmentForgotPasswordBinding::inflate
) {
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onViewCreated() {
        binding.buttonContinue.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.edtEmail.doAfterTextChanged {
            binding.edtEmailError.text = null
        }
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

                        is ApiResponse.Loading -> {
                            binding.buttonContinue.text = resources.getString(R.string.empty)
                            viewModel.setViewAccessible(binding, false)
                        }

                        is ApiResponse.Failure -> {
                            binding.buttonContinue.text =
                                resources.getString(R.string.continue_string)
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
                            viewModel.performForgerPassword(binding.edtEmail.text.toString())
                        }
                    }
                }

                launch {
                    viewModel.forgotPasswordResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            data.message?.let { message -> mContext.showToast(message) }
                            data.data?.let { data ->
                                mainNav.navigate(
                                    ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToVerifyOtpFragment(
                                        false,
                                        binding.edtEmail.text.toString().trim(),
                                        data.userInfo.id
                                    )
                                )
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
            binding.buttonContinue -> {
                var errorCount = 0
                if (binding.edtEmail.text.isNullOrBlank()) {
                    binding.edtEmailError.text = resources.getString(R.string.please_enter_email_id)
                    errorCount++
                }

                if (binding.edtEmail.text.isNullOrEmpty().not()) {
                    val matcher =
                        viewModel.emailPattern.matcher(binding.edtEmail.text.toString().trim())
                    if (matcher.matches().not()) {
                        binding.edtEmailError.text =
                            resources.getString(R.string.please_enter_valid_email_id)
                        errorCount++
                    }
                }

                if (errorCount > 0) return

                if (mContext.checkInternetConnection()) {
                    viewModel.fetchRefreshToken()
                } else {
                    binding.root.showNoInternetSnackBar(resources.getString(R.string.no_internet_connection))
                }
            }

            binding.ivBack -> mainNav.popBackStack()
        }
    }
}