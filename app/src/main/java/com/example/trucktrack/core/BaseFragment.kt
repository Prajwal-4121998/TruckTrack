package com.example.trucktrack.core

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.trucktrack.sealed.ApiResponse
import com.example.trucktrack.util.SharedPref
import com.example.trucktrack.util.ViewInflater
import com.example.trucktrack.util.showAPIErrorSnackBar

abstract class BaseFragment<VB : ViewBinding>(private val viewInflater: ViewInflater<VB>) :
    Fragment(), View.OnClickListener {

    private lateinit var _binding: ViewBinding
    lateinit var sharedPref: SharedPref
    lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @Suppress("UNCHECKED_CAST")
    val binding: VB
        get() = _binding as VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = viewInflater.invoke(layoutInflater)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        requireActivity().window.statusBarColor = Color.TRANSPARENT
        sharedPref = SharedPref(mContext)
        onViewCreated()
    }

    override fun onClick(view: View?) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis >= previousClickTimeMillis + DELAY_MILLIS) {
            previousClickTimeMillis = currentTimeMillis
            view?.let {
                onViewClicked(it)
            }
        }
    }

    abstract fun onViewCreated()
    abstract fun onViewClicked(view: View)

    companion object {
        private const val DELAY_MILLIS = 200L
        private var previousClickTimeMillis = 0L
    }

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
}