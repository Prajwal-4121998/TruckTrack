package com.example.trucktrack.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.androidadvance.topsnackbar.TSnackbar
import com.example.trucktrack.R
import com.example.trucktrack.databinding.BottomSheetStationStopBinding
import com.example.trucktrack.model.Station
import com.example.trucktrack.util.Constants.MAP_PADDING
import com.example.trucktrack.util.Constants.TYPE_CAMERA
import com.example.trucktrack.util.Constants.TYPE_GALLERY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

typealias ViewInflater<T> = (LayoutInflater) -> T

fun Context.checkInternetConnection(): Boolean {
    val result: Boolean
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val activeNetwork =
        connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    result = when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
    return result
}

fun View.showNoInternetSnackBar(message: String) {
    val snackBar = TSnackbar.make(this, message, TSnackbar.LENGTH_SHORT)
    snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorYellow))
    val textView: TextView =
        snackBar.view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, R.color.blackTextColor))
    snackBar.setIconLeft(R.drawable.ic_no_internet_connection, 24f)
    snackBar.setIconPadding(20)
    if (SDK_INT >= VANILLA_ICE_CREAM) {
        snackBar.view.applyTopMarginForAndroid15()
    }
    snackBar.show()
}

fun View.applyTopMarginForAndroid15() {
    this.post {
        val layoutParams = this.layoutParams as? FrameLayout.LayoutParams
        layoutParams?.let { lp ->
            lp.topMargin = getStatusBarHeight()
            this.layoutParams = lp
        }
    }
}

fun View.getStatusBarHeight(): Int {
    val insets = ViewCompat.getRootWindowInsets(this)
    return insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
}

fun Context.showImageSelectionDialog(clickListener: (type: String) -> Unit) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_select_image)
    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.setCanceledOnTouchOutside(true)

    val buttonCamera = dialog.findViewById<MaterialTextView>(R.id.tv_camera)
    val buttonGallery = dialog.findViewById<MaterialTextView>(R.id.tv_gallery)

    buttonCamera?.setOnClickListener {
        clickListener(TYPE_CAMERA)
        dialog.dismiss()
    }

    buttonGallery?.setOnClickListener {
        clickListener(TYPE_GALLERY)
        dialog.dismiss()
    }

    dialog.show()
}

//fun Context.showFuelStationDialog(clickListener: (type: Boolean) -> Unit) {
//    val dialog = Dialog(this)
//    dialog.setContentView(R.layout.dialog_fuel_station)
//    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
//    dialog.window?.setLayout(
//        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
//    )
//    dialog.setCanceledOnTouchOutside(true)
//
//    val buttonYes = dialog.findViewById<MaterialTextView>(R.id.tv_yes)
//    val buttonNo = dialog.findViewById<MaterialTextView>(R.id.tv_no)
//
//    buttonYes?.setOnClickListener {
//        clickListener(true)
//        dialog.dismiss()
//    }
//
//    buttonNo?.setOnClickListener {
//        dialog.dismiss()
//    }
//
//    dialog.show()
//}

fun Context.showFuelStationDialog(clickListener: (type: Boolean) -> Unit) {
    val activity = this as? Activity ?: return

    if (activity.isFinishing || activity.isDestroyed) return

    val dialog = Dialog(activity)

    dialog.setContentView(R.layout.dialog_fuel_station)
    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.setCanceledOnTouchOutside(true)

    val buttonYes = dialog.findViewById<MaterialTextView>(R.id.tv_yes)
    val buttonNo = dialog.findViewById<MaterialTextView>(R.id.tv_no)

    buttonYes?.setOnClickListener {
        clickListener(true)
        dialog.dismiss()
    }

    buttonNo?.setOnClickListener {
        dialog.dismiss()
    }

    if (activity is LifecycleOwner) {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                if (dialog.isShowing) dialog.dismiss()
            }
        })
    }

    dialog.show()
}

val REQUIRED_PERMISSIONS
    get() = if (SDK_INT <= Build.VERSION_CODES.P) {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    } else if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayListOf(
            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA
        )
    } else {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
        )
    }

fun View.showAPIErrorSnackBar(message: String, isCustomError: Boolean = false) {
    val snackBar = TSnackbar.make(this, message, TSnackbar.LENGTH_SHORT)
    val color = if (isCustomError) R.color.colorYellow else R.color.colorRed
    snackBar.view.setBackgroundColor(ContextCompat.getColor(context, color))
    val textView: TextView =
        snackBar.view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
    snackBar.setIconLeft(R.drawable.ic_error, 24f)
    snackBar.setIconPadding(20)
    if (SDK_INT >= VANILLA_ICE_CREAM) {
        snackBar.view.applyTopMarginForAndroid15()
    }
    snackBar.show()
}

fun View.showUserNotVerified(message: String, onVerifyNow: () -> Unit) {
    val snackBar = TSnackbar.make(this, message, TSnackbar.LENGTH_SHORT)
    val color = R.color.colorRed
    snackBar.view.setBackgroundColor(ContextCompat.getColor(context, color))
    val textView: TextView =
        snackBar.view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
    snackBar.setIconLeft(R.drawable.ic_error, 24f)
    snackBar.setIconPadding(20)
    if (SDK_INT >= VANILLA_ICE_CREAM) {
        snackBar.view.applyTopMarginForAndroid15()
    }
    snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
    snackBar.setAction("Verify Now") {
        onVerifyNow.invoke()
    }
    snackBar.show()
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun GoogleMap.animateCameraToIncludeStations(userLatLng: LatLng, stations: List<Station>) {
    if (stations.isEmpty()) {
        animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        return
    }

    val boundsBuilder = LatLngBounds.Builder().apply {
        include(userLatLng)
        stations.forEach { include(LatLng(it.lat, it.lon)) }
    }

    try {
        val bounds = boundsBuilder.build()

        val latDiff = bounds.northeast.latitude - bounds.southwest.latitude
        val lngDiff = bounds.northeast.longitude - bounds.southwest.longitude
        if (latDiff < 0.001 && lngDiff < 0.001) {
            animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        } else {
            animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING))
        }
    } catch (_: Exception) {
        animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
    }
}

//fun GoogleMap.animateCameraToInclude(
//    userLatLng: LatLng,
//    stations: List<Station> = emptyList(),
//    extraPoints: List<LatLng> = emptyList() // source, via, destination
//) {
//    val boundsBuilder = LatLngBounds.Builder().apply {
//        include(userLatLng)
//        stations.forEach { include(LatLng(it.lat, it.lon)) }
//        extraPoints.forEach { include(it) }
//    }
//
//    val bounds = boundsBuilder.build()
//    animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING))
//}

fun Context.toBitmapDescriptor(vectorResId: Int): BitmapDescriptor {
    val vectorDrawable: Drawable = ContextCompat.getDrawable(this, vectorResId)
        ?: throw IllegalArgumentException(getString(R.string.drawable_resource_not_found))

    vectorDrawable.setBounds(
        0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
    )

    val bitmap: Bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun View.showMapSnackBar(message: String) {
    val snackBar = TSnackbar.make(this, message, TSnackbar.LENGTH_SHORT)
    snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorYellow))
    val textView: TextView =
        snackBar.view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text)
    textView.setTextColor(ContextCompat.getColor(context, R.color.blackTextColor))
    snackBar.setIconLeft(R.drawable.ic_location_reached, 24f)
    snackBar.setIconPadding(20)
    if (SDK_INT >= VANILLA_ICE_CREAM) {
        snackBar.view.applyTopMarginForAndroid15()
    }
    snackBar.show()
}

@SuppressLint("SetTextI18n")
fun Fragment.showStationPopup(station: Station, durationMillis: Long) {
    val durationMinutes = durationMillis / 1000 / 60
    val staticFuelAmount = 30.5
    val staticFuelPrice = 3355

    val bottomSheetDialog = BottomSheetDialog(requireContext())
    val binding = BottomSheetStationStopBinding.inflate(LayoutInflater.from(requireContext()))

    binding.tvStationName.text = station.name ?: "Fuel Station"
    binding.tvDuration.text = "Duration: $durationMinutes min"
    binding.tvFuelAmount.text = "Fuel Filled: $staticFuelAmount L"
    binding.tvTotalFuelPrice.text = "Total Fuel Price: $staticFuelPrice Rs"

    binding.buttonContinue.setOnClickListener {
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()
}

fun String.createPartFromString(): RequestBody {
    return toRequestBody("text/plain".toMediaTypeOrNull())
}

fun Context.showReplaceImageDialog(clickListener: () -> Unit) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_replace_image)
    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.setCanceledOnTouchOutside(true)

    val buttonCancel = dialog.findViewById<MaterialTextView>(R.id.tv_cancel)
    val buttonReplace = dialog.findViewById<MaterialTextView>(R.id.tv_replace)

    buttonCancel?.setOnClickListener {
        dialog.dismiss()
    }

    buttonReplace?.setOnClickListener {
        clickListener.invoke()
        dialog.dismiss()
    }

    dialog.show()
}

fun View.setTopToBottomOf(targetId: Int) {
    val parent = this.parent
    if (parent is ConstraintLayout) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)
        constraintSet.connect(
            this.id,
            ConstraintSet.TOP,
            targetId,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(parent)
    } else {
        throw IllegalStateException("View must be inside a ConstraintLayout")
    }
}

fun Context.getAddressFromLatLng(
    latitude: Double?,
    longitude: Double?,
    callback: (String) -> Unit
) {
    if (latitude == null || longitude == null) {
        callback("Unknown location")
        return
    }

    val geocoder = Geocoder(this, Locale.getDefault())

    try {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                val locationText = addresses.toReadableAddress()
                callback(locationText)
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val locationText = addresses.toReadableAddress()
            callback(locationText)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        callback("Unknown location")
    }
}

private fun List<Address>?.toReadableAddress(): String {
    if (this.isNullOrEmpty()) return "Unknown location"

    val address = this[0]
    val city = address.locality ?: address.subAdminArea
    val state = address.adminArea
    val country = address.countryName

    return when {
        city != null && state != null -> "$city, $state"
        city != null -> city
        state != null -> state
        country != null -> country
        else -> "Unknown location"
    }
}
