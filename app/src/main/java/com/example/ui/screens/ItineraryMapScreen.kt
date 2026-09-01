package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Place
import com.example.data.repository.DirectionsRepository
import com.example.data.repository.ItineraryManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Renders the road network polyline decoded from Google Directions API or falls back
 * to straight-line connecting coordinates if road data is not available.
 */
@Composable
fun RoadNetworkMapScreen(
    selectedPlaces: List<Place>,
    directionsRepository: DirectionsRepository = remember { DirectionsRepository() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var roadPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isLoadingRoute by remember { mutableStateOf(false) }

    // Fetch road network path whenever selected places change
    LaunchedEffect(selectedPlaces) {
        if (selectedPlaces.size >= 2) {
            isLoadingRoute = true
            val fetchedRoadPoints = directionsRepository.getRoadRoutePoints(selectedPlaces)
            roadPoints = if (fetchedRoadPoints.isNotEmpty()) {
                fetchedRoadPoints
            } else {
                // Fallback to straight-line LatLng points when Directions API is offline / unconfigured
                selectedPlaces.map { LatLng(it.latitude, it.longitude) }
            }
            isLoadingRoute = false
        } else {
            roadPoints = emptyList()
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    // Auto zoom and fit bounds when selected places change
    LaunchedEffect(selectedPlaces) {
        if (selectedPlaces.isNotEmpty()) {
            try {
                if (selectedPlaces.size == 1) {
                    val place = selectedPlaces.first()
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(place.latitude, place.longitude),
                            14f
                        )
                    )
                } else {
                    val builder = LatLngBounds.builder()
                    selectedPlaces.forEach { place ->
                        builder.include(LatLng(place.latitude, place.longitude))
                    }
                    val bounds = builder.build()
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                }
            } catch (e: Exception) {
                // Map layout may still be initializing
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("itinerary_google_map"),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                mapToolbarEnabled = true
            )
        ) {
            selectedPlaces.forEachIndexed { index, place ->
                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = "${index + 1}. ${place.name}",
                    snippet = "${place.category} • Tap info to Navigate",
                    onInfoWindowClick = {
                        launchGoogleMapsSingleNavigation(context, place)
                    }
                )
            }

            // Draw real road route polyline
            if (roadPoints.isNotEmpty()) {
                Polyline(
                    points = roadPoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 14f,
                    geodesic = true,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND
                )
            }
        }

        // Loading road route overlay badge
        if (isLoadingRoute) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .testTag("loading_road_route_indicator")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculating Road Route...",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MapWithItineraryPolyline(
    selectedPlaces: List<Place>,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier
) {
    RoadNetworkMapScreen(
        selectedPlaces = selectedPlaces,
        modifier = modifier
    )
}

@Composable
fun AutoFitItineraryMap(
    selectedPlaces: List<Place>,
    directionsRepository: DirectionsRepository = remember { DirectionsRepository() },
    modifier: Modifier = Modifier
) {
    RoadNetworkMapScreen(
        selectedPlaces = selectedPlaces,
        directionsRepository = directionsRepository,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryMapScreen(
    onBack: () -> Unit = {},
    selectedPlacesParam: List<Place>? = null,
    directionsRepository: DirectionsRepository = remember { DirectionsRepository() }
) {
    val context = LocalContext.current
    val liveSelectedPlaces by ItineraryManager.selectedPlaces.collectAsState()
    val selectedPlaces = selectedPlacesParam ?: liveSelectedPlaces

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Itinerary Route Planner", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (selectedPlaces.size >= 2) "${selectedPlaces.size} Stops • Road Network Route"
                            else "${selectedPlaces.size} Stops selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("itinerary_map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (selectedPlaces.isNotEmpty()) {
                        TextButton(
                            onClick = { ItineraryManager.clearItinerary() },
                            modifier = Modifier.testTag("clear_itinerary_button")
                        ) {
                            Text("Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = selectedPlaces.isNotEmpty(),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(250)
                    ) + fadeOut(animationSpec = tween(150))
                ) {
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = { launchGoogleMapsDirections(context, selectedPlaces) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .testTag("launch_google_maps_route_button")
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Route in Google Maps (${selectedPlaces.size} Stops)")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Upper Map Section with Real Road Network Polyline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
            ) {
                RoadNetworkMapScreen(
                    selectedPlaces = selectedPlaces,
                    directionsRepository = directionsRepository
                )
            }

            HorizontalDivider()

            // Lower Sequence Reordering List Section with Slide-In Animation
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                AnimatedContent(
                    targetState = selectedPlaces.isNotEmpty(),
                    transitionSpec = {
                        if (targetState) {
                            (slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                            ) + fadeIn(animationSpec = tween(300))).togetherWith(
                                fadeOut(animationSpec = tween(150))
                            )
                        } else {
                            fadeIn(animationSpec = tween(300)).togetherWith(
                                slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(250)
                                ) + fadeOut(animationSpec = tween(150))
                            )
                        }
                    },
                    label = "ItinerarySheetTransition",
                    modifier = Modifier.fillMaxSize()
                ) { hasStops ->
                    if (hasStops) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("reorderable_itinerary_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "Adjust Stop Sequence",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Road Network",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            itemsIndexed(
                                items = selectedPlaces,
                                key = { _, place -> place.id }
                            ) { index, place ->
                                ReorderableItineraryItem(
                                    place = place,
                                    stepNumber = index + 1,
                                    isFirst = index == 0,
                                    isLast = index == selectedPlaces.size - 1,
                                    onMoveUp = { ItineraryManager.moveUp(index) },
                                    onMoveDown = { ItineraryManager.moveDown(index) },
                                    onRemove = { ItineraryManager.removePlace(place.id) }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No stops added to itinerary yet.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReorderableItineraryItem(
    place: Place,
    stepNumber: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("itinerary_stop_item_$stepNumber"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Step Number Badge & Place Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = place.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Navigate / Up / Down / Remove Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { launchGoogleMapsSingleNavigation(context, place) },
                    modifier = Modifier.testTag("navigate_stop_$stepNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate to ${place.name}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.testTag("move_up_$stepNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (!isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.testTag("move_down_$stepNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (!isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_stop_$stepNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Stop",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Launches the default Google Maps app with turn-by-turn navigation from the user's current location to the selected site.
 */
fun launchGoogleMapsSingleNavigation(context: Context, place: Place) {
    val navUri = Uri.parse("google.navigation:q=${place.latitude},${place.longitude}&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}&travelmode=driving")
        val browserIntent = Intent(Intent.ACTION_VIEW, webUri)
        try {
            context.startActivity(browserIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open navigation", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Constructs a Google Maps Multi-stop directions URI and launches Google Maps navigation.
 */
fun launchGoogleMapsDirections(context: Context, places: List<Place>) {
    if (places.isEmpty()) return
    val origin = "${places.first().latitude},${places.first().longitude}"
    val destination = "${places.last().latitude},${places.last().longitude}"

    val waypoints = if (places.size > 2) {
        places.subList(1, places.size - 1)
            .joinToString("|") { "${it.latitude},${it.longitude}" }
    } else null

    val uriString = buildString {
        append("https://www.google.com/maps/dir/?api=1")
        append("&origin=$origin")
        append("&destination=$destination")
        if (!waypoints.isNullOrEmpty()) {
            append("&waypoints=$waypoints")
        }
        append("&travelmode=driving")
    }

    val uri = Uri.parse(uriString)
    val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(mapIntent)
    } catch (e: ActivityNotFoundException) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(browserIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open map application", Toast.LENGTH_SHORT).show()
        }
    }
}
