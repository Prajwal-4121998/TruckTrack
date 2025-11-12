package com.example.trucktrack.ui.fragments.sign_up

import android.app.Activity
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentSignUpBinding
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.Constants.PRIVACY_POLICY_URL
import com.example.trucktrack.util.Constants.TERMS_CONDITION_URL
import com.example.trucktrack.util.Constants.TYPE_CAMERA
import com.example.trucktrack.util.Constants.TYPE_GALLERY
import com.example.trucktrack.util.REQUIRED_PERMISSIONS
import com.example.trucktrack.util.checkInternetConnection
import com.example.trucktrack.util.mainNav
import com.example.trucktrack.util.showAPIErrorSnackBar
import com.example.trucktrack.util.showImageSelectionDialog
import com.example.trucktrack.util.showNoInternetSnackBar
import com.example.trucktrack.util.showToast
import com.github.drjacky.imagepicker.ImagePicker
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate) {
    private val viewModel: SignUpViewModel by viewModels()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data
                uri?.path?.let { path ->
                    binding.ivProfileImage.setImageURI(it.data?.data)
                    viewModel.profileImageFile = File(path)
                }
            }
        }

    override fun onViewCreated() {
        initClickListener()
        initTextWatcher()
        binding.cbAgree.buttonTintList = null
        setLinkerText()
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
                            viewModel.registerUser(
                                binding.edtName.text.toString(),
                                binding.edtEmail.text.toString(),
                                binding.edtPassword.text.toString()
                            )
                        }
                    }
                }

                launch {
                    viewModel.signupResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            binding.buttonContinue.text = getString(R.string.continue_string)
                            viewModel.setViewAccessible(binding, true)
                            data.message.let { message -> mContext.showToast(message) }
                            data.data.userInfo.let { userInfo ->
                                viewModel.storeUserData(userInfo)
                                mainNav.navigate(
                                    SignUpFragmentDirections.actionSignUpFragmentToVerifyOtpFragment(
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

    override fun onViewClicked(view: View) {
        when (view) {
            binding.ivEditProfileImage -> {
                mContext.showImageSelectionDialog {
                    askPermission(it)
                }
            }

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

            binding.ivBack, binding.tvLogin -> mainNav.popBackStack()

            binding.buttonContinue -> {
                var errorCount = 0

                if (binding.edtName.text.isNullOrBlank()) {
                    binding.edtNameError.text = resources.getString(R.string.please_enter_name)
                    errorCount++
                }

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
                if (binding.edtPassword.text.isNullOrBlank()) {
                    binding.edtPasswordError.text =
                        resources.getString(R.string.please_enter_password)
                    errorCount++
                }
                if (binding.edtPassword.text.isNullOrEmpty().not()) {

                    if (viewModel.passwordPattern.matches(
                            binding.edtPassword.text.toString().trim()
                        ).not()
                    ) {
                        binding.edtPasswordError.text =
                            resources.getString(R.string.please_enter_valid_password)
                        errorCount++
                    }
                }

                if (binding.cbAgree.isChecked.not()) {
                    binding.root.showAPIErrorSnackBar(
                        resources.getString(R.string.required_terms_privacy), true
                    )
                    errorCount++
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

    private fun initClickListener() {
        binding.ivBack.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.buttonContinue.setOnClickListener(this)
        binding.tvShowPassword.setOnClickListener(this)
        binding.ivEditProfileImage.setOnClickListener(this)
    }

    private fun initTextWatcher() {
        with(binding) {
            edtName.doAfterTextChanged {
                edtNameError.text = null
            }
            edtEmail.doAfterTextChanged {
                edtEmailError.text = null
            }
            edtPassword.doAfterTextChanged {
                edtPasswordError.text = null
            }
        }
    }

    private fun setLinkerText() {
        val termsOfService = object : ClickableSpan() {
            override fun onClick(widget: View) {
                mainNav.navigate(
                    SignUpFragmentDirections.actionSignUpFragmentToWebLoaderFragment(
                        TERMS_CONDITION_URL, resources.getString(R.string.terms_condition)
                    )
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color =
                    ContextCompat.getColor(requireContext(), R.color.white)     // setup color
                ds.isUnderlineText = true
            }
        }
        val privacyPolicy = object : ClickableSpan() {
            override fun onClick(widget: View) {
                mainNav.navigate(
                    SignUpFragmentDirections.actionSignUpFragmentToWebLoaderFragment(
                        PRIVACY_POLICY_URL, resources.getString(R.string.privacy_policy)
                    )
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color =
                    ContextCompat.getColor(requireContext(), R.color.white)     // setup color
                ds.isUnderlineText = true
            }
        }
        val spannable =
            SpannableString(resources.getString(R.string.i_agree_with_terms_and_conditions_and_privacy_policy))
        spannable.setSpan(termsOfService, 13, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(privacyPolicy, 36, 50, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvAgree.text = spannable
        binding.tvAgree.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun askPermission(type: String) {
        PermissionX.init(this).permissions(REQUIRED_PERMISSIONS)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    resources.getString(R.string.core_fundamental_permissions_title),
                    resources.getString(R.string.ok),
                    resources.getString(R.string.cancel)
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    resources.getString(R.string.necessary_permissions_title),
                    resources.getString(R.string.ok),
                    resources.getString(R.string.cancel)
                )
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    when (type) {
                        TYPE_GALLERY -> pickImageFromGallery()
                        TYPE_CAMERA -> pickImageFromCamera()
                    }
                }
            }
    }

    private fun pickImageFromGallery() {
        launcher.launch(
            ImagePicker.Companion.with(mContext as Activity).galleryOnly().galleryMimeTypes(
                arrayOf(
                    "image/png", "image/jpg", "image/jpeg"
                )
            ).cropSquare().createIntent()
        )
    }

    private fun pickImageFromCamera() {
        launcher.launch(
            ImagePicker.Companion.with(mContext as Activity).cameraOnly().cropSquare()
                .createIntent()
        )
    }

}