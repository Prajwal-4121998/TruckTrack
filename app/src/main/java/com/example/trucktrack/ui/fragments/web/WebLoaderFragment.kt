package com.example.trucktrack.ui.fragments.web

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentWebLoaderBinding
import com.example.trucktrack.util.mainNav

class WebLoaderFragment :
    BaseFragment<FragmentWebLoaderBinding>(FragmentWebLoaderBinding::inflate) {

    private val args: WebLoaderFragmentArgs by navArgs()
    val isViewLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onViewCreated() {
        binding.tvTitle.text = args.keyTitle
        binding.ivBack.setOnClickListener(this)
        initObserver()
        loadWebView()
    }

    private fun initObserver() {
        isViewLoaded.observe(viewLifecycleOwner) {
            binding.webView.isVisible = it
            binding.progressBar.isVisible = !it
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        binding.webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest?
            ): Boolean {
                request?.let {
                    view.loadUrl(it.url.toString())
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                isViewLoaded.postValue(true)
            }
        }

        binding.webView.loadUrl(args.keyUrl)
    }

    override fun onViewClicked(view: View) {
        when (view) {
            binding.ivBack -> mainNav.popBackStack()
        }
    }
}