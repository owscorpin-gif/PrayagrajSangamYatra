package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.util.LocationUtils
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PlacesViewModel

class MainActivity : ComponentActivity() {

    private val placesViewModel: PlacesViewModel by viewModels()

    // ActivityResultLauncher for fine and coarse location permissions
    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted || coarseLocationGranted) {
                // Permission granted: Automatically fetch current location and refresh proximity distances
                placesViewModel.fetchCurrentLocation(this@MainActivity, enableSort = false)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Location permission denied. Proximity distances unavailable.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and request fine-grained location permission if not already granted
        requestLocationPermissionIfNeeded()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(placesViewModel = placesViewModel)
                }
            }
        }
    }

    /**
     * Initiates the location permission request flow if permissions are not yet granted.
     * If already granted, immediately fetches current user coordinates.
     */
    fun requestLocationPermissionIfNeeded() {
        if (LocationUtils.hasLocationPermission(this)) {
            placesViewModel.fetchCurrentLocation(this, enableSort = false)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
