package com.example.trucktrack.ui.fragments.add_fuel_entry

import android.app.Activity
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.trucktrack.R
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentAddFuelEntryBinding
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.TYPE_CAMERA
import com.example.trucktrack.util.Constants.TYPE_GALLERY
import com.example.trucktrack.util.REQUIRED_PERMISSIONS
import com.example.trucktrack.util.checkInternetConnection
import com.example.trucktrack.util.mainNav
import com.example.trucktrack.util.showAPIErrorSnackBar
import com.example.trucktrack.util.showImageSelectionDialog
import com.example.trucktrack.util.showNoInternetSnackBar
import com.example.trucktrack.util.showReplaceImageDialog
import com.github.drjacky.imagepicker.ImagePicker
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class AddFuelEntryFragment :
    BaseFragment<FragmentAddFuelEntryBinding>(FragmentAddFuelEntryBinding::inflate) {

    private val viewModel: AddFuelViewModel by viewModels()
    private val args: AddFuelEntryFragmentArgs by navArgs()
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data
                uri?.path?.let { path ->
                    binding.imgData.load(it.data?.data)
                    viewModel.fuelImageFile = File(path)
                    viewModel.setImageSelection(binding)
                }
            }
        }

    override fun onViewCreated() {
        initClickListener()
        initTextWatcher()
        initObserver()
    }

    override fun onViewClicked(view: View) {
        when (view) {
            binding.ivBack -> mainNav.popBackStack()
            binding.viewSelectFile -> {
                mContext.showImageSelectionDialog {
                    askPermission(it)
                }
            }

            binding.ivClose -> {
                mContext.showReplaceImageDialog {
                    viewModel.fuelImageFile = null
                    viewModel.removeImageSelection(binding)
                }
            }

            binding.buttonAddFuelEntry -> {
                var errorCount = 0

                if (binding.etLiters.text.isNullOrBlank()) {
                    binding.etLitersError.text = getString(R.string.please_enter_a_valid_quantity)
                    errorCount++
                }

                if (binding.etPricePerLiter.text.isNullOrBlank()) {
                    binding.etPricePerLiterError.text =
                        getString(R.string.please_enter_a_valid_price_per_liter)
                    errorCount++
                }

                if (binding.etTotalPrice.text.isNullOrBlank()) {
                    binding.etTotalPriceError.text =
                        getString(R.string.please_enter_a_valid_total_price)
                    errorCount++
                }

                if (errorCount > 0) return

                if (viewModel.isImageSelected()) {
                    if (mContext.checkInternetConnection()) {
                        args.let {
                            viewModel.addFuelEntry(
                                tripId = args.keyTripId.toString(),
                                latitude = args.keyLat.toString(),
                                longitude = args.keyLong.toString(),
                                litres = binding.etLiters.text.toString(),
                                pricePerLiter = binding.etPricePerLiter.text.toString(),
                                totalPrice = binding.etTotalPrice.text.toString()
                            )
                        }
                    } else {
                        binding.root.showNoInternetSnackBar(
                            resources.getString(R.string.no_internet_connection)
                        )
                    }
                } else {
                    binding.root.showAPIErrorSnackBar(resources.getString(R.string.please_upload_image_first))
                }
            }
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
                            binding.buttonAddFuelEntry.text = ""
                            binding.pbLogin.isVisible = true
                        }

                        is ApiResponse.Failure -> {
                            binding.buttonAddFuelEntry.text = getString(R.string.continue_string)
                            binding.pbLogin.isVisible = true
                            binding.root.showAPIErrorSnackBar(response.data.responseMessage)
                        }

                        is ApiResponse.Success -> {
                            onSuccess(response.data)
                        }
                    }
                }

                launch {
                    viewModel.addFuelEntryResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            mainNav.popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun initClickListener() {
        binding.ivBack.setOnClickListener(this)
        binding.viewSelectFile.setOnClickListener(this)
        binding.ivClose.setOnClickListener(this)
        binding.buttonAddFuelEntry.setOnClickListener(this)
    }

    private fun initTextWatcher() {
        with(binding) {
            etLiters.doAfterTextChanged {
                etLitersError.text = null
            }
            etPricePerLiter.doAfterTextChanged {
                etPricePerLiterError.text = null
            }
            etTotalPrice.doAfterTextChanged {
                etTotalPriceError.text = null
            }
        }
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
            ).crop().createIntent()
        )
    }

    private fun pickImageFromCamera() {
        launcher.launch(
            ImagePicker.Companion.with(mContext as Activity).cameraOnly().crop()
                .createIntent()
        )
    }
}