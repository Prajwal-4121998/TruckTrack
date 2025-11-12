package com.example.trucktrack.util

import android.app.Activity
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.trucktrack.R


val Activity.mainNav: NavController
    get() = findNavController(R.id.nav_host_controller)

val Fragment.mainNav: NavController
    get() = findNavController(R.id.nav_host_controller)

private fun Fragment.findNavController(@IdRes navHostControllerId: Int): NavController {
    return requireActivity().findNavController(navHostControllerId)
}