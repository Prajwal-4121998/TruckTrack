package com.example.trucktrack.ui.fragments.add_fuel_entry

import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trucktrack.R
import com.example.trucktrack.core.safeApiCall
import com.example.trucktrack.databinding.FragmentAddFuelEntryBinding
import com.example.trucktrack.model.addFuelEntry.AddFuelResponse
import com.example.trucktrack.rest.MainRepository
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.Constants.BEARER
import com.example.trucktrack.util.Constants.KEY_ACCESS_TOKEN
import com.example.trucktrack.util.SharedPref
import com.example.trucktrack.util.createPartFromString
import com.example.trucktrack.util.setTopToBottomOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddFuelViewModel @Inject constructor(
    private val repository: MainRepository,
    private val sharedPref: SharedPref
) : ViewModel() {

    private var isImageSelected = false

    var fuelImageFile: File? = null

    private val _addFuelEntryResponse =
        MutableStateFlow<ApiResponse<AddFuelResponse>>(ApiResponse.Stable())

    val addFuelEntryResponse: StateFlow<ApiResponse<AddFuelResponse>> = _addFuelEntryResponse

    fun addFuelEntry(
        tripId: String,
        latitude: String,
        longitude: String,
        litres: String,
        pricePerLiter: String,
        totalPrice: String
    ) {
        var requestFuelImage: MultipartBody.Part? = null
        fuelImageFile?.let { image ->
            val reqFile: RequestBody = image.asRequestBody("image/*".toMediaTypeOrNull())
            requestFuelImage =
                MultipartBody.Part.createFormData("fuelImage", image.name, reqFile)
        }

        viewModelScope.launch {
            _addFuelEntryResponse.value = ApiResponse.Loading()

            val result = safeApiCall {
                repository.addFuelEntry(
                    accessToken = "$BEARER ${sharedPref.getString(KEY_ACCESS_TOKEN)}",
                    tripId.createPartFromString(),
                    latitude.createPartFromString(),
                    longitude.createPartFromString(),
                    litres.createPartFromString(),
                    pricePerLiter.createPartFromString(),
                    totalPrice.createPartFromString(),
                    requestFuelImage
                )
            }

            _addFuelEntryResponse.value = result
        }
    }


    fun setImageSelection(binding: FragmentAddFuelEntryBinding) {
        isImageSelected = true
        binding.viewSelectFile.isVisible = false
        binding.clViewFile.isVisible = true
        binding.ivClose.isVisible = true
        binding.buttonAddFuelEntry.setTopToBottomOf(R.id.cl_view_file)
    }

    fun removeImageSelection(binding: FragmentAddFuelEntryBinding) {
        isImageSelected = false
        binding.viewSelectFile.isVisible = true
        binding.clViewFile.isVisible = false
        binding.ivClose.isVisible = false
        binding.buttonAddFuelEntry.setTopToBottomOf(R.id.view_select_file)
    }

    fun isImageSelected() = isImageSelected
}