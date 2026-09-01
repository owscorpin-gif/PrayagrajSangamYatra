package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.util.LocationUtils
import com.example.ui.viewmodel.PlacesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayagrajPlacesScreen(
    onSignOut: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    viewModel: PlacesViewModel = viewModel()
) {
    val context = LocalContext.current
    val places by viewModel.places.collectAsState()
    val selectedPlaces by viewModel.selectedPlaces.collectAsState()
    val isLoadingPlaces by viewModel.isLoadingPlaces.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val sortByProximity by viewModel.sortByProximity.collectAsState()
    val placeRatings by viewModel.placeRatings.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    // Permission launcher for location access
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.fetchCurrentLocation(context, enableSort = true)
        } else {
            Toast.makeText(
                context,
                "Location permission denied. Cannot calculate proximity.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prayagraj Places",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (userLocation != null) {
                            Text(
                                text = if (sortByProximity) "Sorted by closest to you" else "GPS location active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Quick GPS Fetch Action
                    IconButton(
                        onClick = {
                            if (LocationUtils.hasLocationPermission(context)) {
                                viewModel.fetchCurrentLocation(context, enableSort = false)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("top_bar_gps_button")
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Fetch GPS Location",
                                tint = if (userLocation != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (selectedPlaces.isNotEmpty()) {
                        IconButton(
                            onClick = onNavigateToMap,
                            modifier = Modifier.testTag("top_app_bar_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "View Itinerary Map"
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.logout(onSuccess = onSignOut) },
                        modifier = Modifier.testTag("top_app_bar_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Display Itinerary Navigation Bar when at least 1 place is selected
            if (selectedPlaces.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("itinerary_bottom_bar"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedPlaces.size} place(s) selected",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = onNavigateToMap,
                            modifier = Modifier.testTag("view_itinerary_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("View Itinerary Map")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OfflineSyncBanner(networkStatus = networkStatus)

            // Proximity Sorting & Controls Filter Header
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sort by Proximity Chip
                    FilterChip(
                        selected = sortByProximity,
                        onClick = {
                            if (!sortByProximity) {
                                if (LocationUtils.hasLocationPermission(context)) {
                                    viewModel.toggleSortByProximity(context)
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            } else {
                                viewModel.toggleSortByProximity(context)
                            }
                        },
                        label = {
                            Text(
                                text = if (sortByProximity) "Sorted by Nearest" else "Sort by Distance",
                                fontWeight = if (sortByProximity) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (sortByProximity) Icons.Default.Check else Icons.Default.NearMe,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("sort_by_proximity_chip")
                    )

                    if (userLocation == null && !isLocating) {
                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("enable_location_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Get My Location",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            if (isLoadingPlaces) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("places_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        val isSelected = selectedPlaces.any { it.id == place.id }
                        val distanceText = viewModel.getFormattedDistance(place)

                        PlaceCard(
                            place = place,
                            isSelected = isSelected,
                            distanceText = distanceText,
                            userRating = placeRatings[place.id] ?: 0,
                            onRatingChanged = { rating ->
                                viewModel.setPlaceRating(place.id, rating)
                            },
                            onToggleSelection = { viewModel.toggleItinerarySelection(place) }
                        )
                    }
                }
            }
        }
    }
}
