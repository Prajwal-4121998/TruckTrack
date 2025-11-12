plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.trucktrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.trucktrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_OVERPASS_URL", properties["BASE_OVERPASS_URL"] as String)
        buildConfigField("String", "BASE_OSRM_URL", properties["BASE_OSRM_URL"] as String)
        buildConfigField("String", "BASE_URL", properties["BASE_URL"] as String)
        buildConfigField("String", "BASE_NGROCK_URL", properties["BASE_NGROCK_URL"] as String)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //splash-screen api
    implementation(libs.androidx.core.splashscreen)

    //navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //sdp-ssp
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    //snack-bar
    implementation(libs.tsnackbar)

    //circle-image
    implementation(libs.circleimageview)

    //permission-x
    implementation(libs.permissionx)

    //image-picker-crop
    implementation(libs.imagepicker)

    //otp-view
    implementation(libs.otpview)

    //loader
    implementation(libs.android.spinkit)

    // Retrofit & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.converter.scalars)
    implementation(libs.logging.interceptor)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //PlayService
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    //image-loading library
    implementation(libs.coil)
}