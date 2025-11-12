package com.example.trucktrack.ui.fragments.reset_password

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentResetPasswordBinding
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
class ResetPasswordFragment : BaseFragment<FragmentResetPasswordBinding>(
    FragmentResetPasswordBinding::inflate
) {

    private val viewModel: ResetPasswordViewModel by viewModels()

    private val args: ResetPasswordFragmentArgs by navArgs()

    override fun onViewCreated() {
        initClickListener()
        initTextWatcher()
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
                            binding.buttonSave.text = resources.getString(R.string.empty)
                            viewModel.setViewAccessible(binding, false)
                        }

                        is ApiResponse.Failure -> {
                            binding.buttonSave.text = resources.getString(R.string.save)
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
                            viewModel.resetPasswordAPI(
                                args.keyUserId,
                                binding.etNewPassword.text.toString()
                            )
                        }
                    }
                }

                launch {
                    viewModel.resetPasswordResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            data.message?.let { message -> mContext.showToast(message) }
                            mainNav.navigate(ResetPasswordFragmentDirections.actionResetPasswordFragmentToLoginFragment())
                        }
                    }
                }
            }
        }
    }


    private fun initTextWatcher() {
        binding.etNewPassword.doAfterTextChanged {
            binding.tvNewPasswordError.text = null
        }
        binding.etConfirmPassword.doAfterTextChanged {
            binding.tvConfirmPasswordError.text = null
        }
    }

    private fun initClickListener() {
        binding.ivBack.setOnClickListener(this)
        binding.tvShowNewPassword.setOnClickListener(this)
        binding.tvShowPassword.setOnClickListener(this)
        binding.buttonSave.setOnClickListener(this)
    }

    override fun onViewClicked(view: View) {
        when (view) {
            binding.ivBack -> mainNav.popBackStack()
            binding.tvShowNewPassword -> {
                if (binding.tvShowNewPassword.text.toString() == mContext.getString(R.string.show)) {
                    binding.etNewPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    binding.tvShowNewPassword.text = mContext.getString(R.string.hide)
                } else {
                    binding.etNewPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    binding.tvShowNewPassword.text = mContext.getString(R.string.show)
                }
                binding.etNewPassword.text?.let { binding.etNewPassword.setSelection(it.length) }
            }

            binding.tvShowPassword -> {
                if (binding.tvShowPassword.text.toString() == mContext.getString(R.string.show)) {
                    binding.etConfirmPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    binding.tvShowPassword.text = mContext.getString(R.string.hide)
                } else {
                    binding.etConfirmPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    binding.tvShowPassword.text = mContext.getString(R.string.show)
                }
                binding.etConfirmPassword.text?.let { binding.etConfirmPassword.setSelection(it.length) }
            }

            binding.buttonSave -> {
                var errorCount = 0

                if (binding.etNewPassword.text.isNullOrBlank()) {
                    binding.tvNewPasswordError.text =
                        resources.getString(R.string.please_enter_new_password)
                    errorCount++
                }
                if (binding.etNewPassword.text.isNullOrEmpty().not()) {

                    if (viewModel.passwordPattern.matches(
                            binding.etNewPassword.text.toString().trim()
                        ).not()
                    ) {
                        binding.tvNewPasswordError.text =
                            resources.getString(R.string.please_enter_valid_password)
                        errorCount++
                    }
                }
                if (binding.etConfirmPassword.text.isNullOrBlank()) {
                    binding.tvConfirmPasswordError.text =
                        resources.getString(R.string.please_enter_confirm_password)
                    errorCount++
                }

                if (binding.etConfirmPassword.text.isNullOrBlank().not()) {
                    if ((binding.etNewPassword.text.toString()
                            .trim() == binding.etConfirmPassword.text.toString()
                            .trim()).not()
                    ) {
                        binding.tvConfirmPasswordError.text =
                            resources.getString(R.string.please_enter_same_password)
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
        }
    }
}