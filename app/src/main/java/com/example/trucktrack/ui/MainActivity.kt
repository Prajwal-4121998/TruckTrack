package com.example.trucktrack.ui

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.trucktrack.R
import com.example.trucktrack.databinding.ActivityMainBinding
import com.example.trucktrack.util.Constants.KEY_LOGGED_IN
import com.example.trucktrack.util.SharedPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var sharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ensured light icons on status bar
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        sharedPref = SharedPref(this)
        setNavigationGraph()
    }

    private fun setNavigationGraph() {
        navHostFragment =
            supportFragmentManager.findFragmentById(binding.navHostController.id) as NavHostFragment
        navController = navHostFragment.navController

        val startDestinationID = if (VERSION.SDK_INT >= VERSION_CODES.S) {
            if (sharedPref.getBoolean(KEY_LOGGED_IN)) R.id.mainFragment else R.id.loginFragment
        } else {
            R.id.splashFragment
        }

        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(startDestinationID)
        navController.setGraph(graph, intent.extras)
    }
}