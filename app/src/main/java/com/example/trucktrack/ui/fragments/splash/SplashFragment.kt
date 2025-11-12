package com.example.trucktrack.ui.fragments.splash

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.trucktrack.core.BaseFragment
import com.example.trucktrack.databinding.FragmentSplashBinding
import com.example.trucktrack.util.Constants.KEY_LOGGED_IN
import com.example.trucktrack.util.mainNav
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {
    override fun onViewCreated() {
        lifecycleScope.launch {
            delay(1000)
            if (sharedPref.getBoolean(KEY_LOGGED_IN))
                mainNav.navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            else
                mainNav.navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
        }
    }

    override fun onViewClicked(view: View) {
        TODO("Not yet implemented")
    }

}