package com.example.trucktrack.ui.fragments.trip_summary

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentTripSummaryBinding
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.getAddressFromLatLng
import com.example.trucktrack.util.showAPIErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TripSummaryFragment :
    BaseFragment<FragmentTripSummaryBinding>(FragmentTripSummaryBinding::inflate) {
    private val viewModel: TripSummaryViewModel by viewModels()
    private val args: TripSummaryFragmentArgs by navArgs()
    override fun onViewCreated() {
        viewModel.tripId = args.keyTripId
        viewModel.fetchTripSummary()
        initObserver()
    }

    override fun onViewClicked(view: View) {}

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
                    viewModel.tripSummaryResponse.collect { response ->
                        handleApiResponse(response) { data ->
                            mContext.getAddressFromLatLng(
                                data.data.originLat,
                                data.data.originLong
                            ) { location ->
                                binding.tvSource.text = location
                            }
                            mContext.getAddressFromLatLng(
                                data.data.endLat,
                                data.data.endLong
                            ) { location ->
                                binding.tvDestination.text = location
                            }
                            binding.tvDistanceTraveled.text = data.data.totalDistance
                            binding.tvFuelUsed.text = data.data.totalFuelLiters
                            binding.tvFuelCost.text = data.data.totalFuelPrice
                            binding.tvMileage.text = data.data.fuelEfficiency
                            binding.tvDeliveredWeight.text = data.data.cargoWeightDelivered
                        }
                    }
                }
            }
        }
    }

}