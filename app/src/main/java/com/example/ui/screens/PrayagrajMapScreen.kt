package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Place
import com.example.data.repository.DirectionsRepository
import com.example.data.repository.ItineraryManager
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCardSurface
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishGreenBg
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPurpleBadge
import com.example.ui.theme.PolishPurpleText
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Prayagraj Sangam Geographical Coordinates
 */
val PRAYAGRAJ_SANGAM_LATLNG = LatLng(25.4237, 81.8825)

/**
 * Enhanced Google Maps Component displaying markers for heritage, spiritual, ghats, and cultural sites across Prayagraj.
 */
@Composable
fun PrayagrajHeritageMapView(
    allPlaces: List<Place>,
    selectedPlaceId: String? = null,
    onSelectPlace: (Place) -> Unit = {},
    onAddToItinerary: (Place) -> Unit = {},
    onRemoveFromItinerary: (String) -> Unit = {},
    itineraryPlaces: List<Place> = emptyList(),
    directionsRepository: DirectionsRepository = remember { DirectionsRepository() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var activeSelectedPlace by remember(selectedPlaceId, allPlaces) {
        mutableStateOf(allPlaces.find { it.id == selectedPlaceId } ?: allPlaces.firstOrNull())
    }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showRoutePolyline by remember { mutableStateOf(true) }
    var roadPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isLoadingRoute by remember { mutableStateOf(false) }

    // Filter places based on active category & search query
    val displayedPlaces = remember(allPlaces, selectedCategory, searchQuery) {
        allPlaces.filter { place ->
            val matchesCategory = when (selectedCategory.lowercase()) {
                "all", "" -> true
                "temple", "temples" -> place.category.equals("temple", ignoreCase = true)
                "ghat", "riverside" -> place.category.equals("ghat", ignoreCase = true) ||
                        place.tags.any { it.contains("ghat", true) || it.contains("boat", true) || it.contains("aarti", true) }
                "heritage" -> place.category.equals("heritage", ignoreCase = true) ||
                        place.tags.any { it.contains("heritage", true) || it.contains("museum", true) || it.contains("park", true) }
                else -> place.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                place.name.contains(searchQuery, ignoreCase = true) ||
                        (place.hindiName?.contains(searchQuery, ignoreCase = true) ?: false) ||
                        (place.description?.contains(searchQuery, ignoreCase = true) ?: false)
            }
            matchesCategory && matchesSearch
        }
    }

    // Default Camera centered on Prayagraj Sangam
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(PRAYAGRAJ_SANGAM_LATLNG, 13f)
    }

    // Update road route polyline when itinerary places change
    LaunchedEffect(itineraryPlaces, showRoutePolyline) {
        if (showRoutePolyline && itineraryPlaces.size >= 2) {
            isLoadingRoute = true
            val points = directionsRepository.getRoadRoutePoints(itineraryPlaces)
            roadPoints = if (points.isNotEmpty()) {
                points
            } else {
                itineraryPlaces.map { LatLng(it.latitude, it.longitude) }
            }
            isLoadingRoute = false
        } else {
            roadPoints = emptyList()
        }
    }

    // Camera animation when selectedPlaceId or activeSelectedPlace changes
    LaunchedEffect(activeSelectedPlace) {
        activeSelectedPlace?.let { place ->
            coroutineScope.launch {
                try {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(place.latitude, place.longitude),
                            15.5f
                        ),
                        durationMs = 800
                    )
                } catch (e: Exception) {
                    // Ignore if map not ready
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("prayagraj_google_map_container")) {
        // Google Map Composable
        GoogleMap(
            modifier = Modifier.fillMaxSize().testTag("google_map_view"),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapClick = {
                // Deselect place when clicking on empty map area
                activeSelectedPlace = null
            }
        ) {
            // PostGIS Radar circle representation around Sangam
            Circle(
                center = PRAYAGRAJ_SANGAM_LATLNG,
                radius = 3500.0,
                fillColor = Color(0x1A6750A4),
                strokeColor = Color(0x666750A4),
                strokeWidth = 2f
            )

            // Render Markers for all filtered Prayagraj Sites
            displayedPlaces.forEach { place ->
                val isSelected = activeSelectedPlace?.id == place.id
                val isInItinerary = itineraryPlaces.any { it.id == place.id }
                val markerHue = getCategoryMarkerHue(place.category)

                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = place.name,
                    snippet = "${place.hindiName ?: place.category} • Tap info to Navigate",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        activeSelectedPlace = place
                        onSelectPlace(place)
                        false
                    },
                    onInfoWindowClick = {
                        launchGoogleMapsNavigationToPlace(context, place)
                    }
                )
            }

            // Road Network Route Polyline for Itinerary Stops
            if (roadPoints.isNotEmpty()) {
                Polyline(
                    points = roadPoints,
                    color = PolishPrimary,
                    width = 12f,
                    geodesic = true,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND
                )
            }
        }

        // Top Layer: Quick Search Bar and Category Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            // Search Input Field
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PolishCardSurface.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PolishPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search ${displayedPlaces.size} temples, ghats, forts...",
                                fontSize = 13.sp,
                                color = PolishTextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("map_search_field")
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = PolishTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterCategories = listOf(
                    Triple("all", "All Sites (${allPlaces.size})", "🏛️"),
                    Triple("heritage", "Heritage", "🏰"),
                    Triple("temple", "Temples", "🛕"),
                    Triple("riverside", "Riverside", "🌊"),
                    Triple("ghat", "Ghats", "🛶")
                )

                filterCategories.forEach { (key, label, icon) ->
                    val isSelected = selectedCategory.equals(key, ignoreCase = true) ||
                            (key == "riverside" && selectedCategory.equals("ghat", ignoreCase = true))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PolishPrimary else PolishCardSurface.copy(alpha = 0.95f),
                        shadowElevation = 3.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PolishPrimary else PolishBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedCategory = if (isSelected && key != "all") "all" else key
                            }
                            .testTag("map_filter_chip_$key")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = icon, fontSize = 12.sp)
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PolishTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Floating Map Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Re-center on Prayagraj Sangam FAB
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(PRAYAGRAJ_SANGAM_LATLNG, 13.5f),
                            durationMs = 600
                        )
                    }
                },
                modifier = Modifier.size(44.dp).testTag("recenter_sangam_fab"),
                shape = CircleShape,
                containerColor = PolishCardSurface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on Sangam",
                    tint = PolishPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Map Type Toggle FAB
            FloatingActionButton(
                onClick = {
                    mapType = when (mapType) {
                        MapType.NORMAL -> MapType.HYBRID
                        MapType.HYBRID -> MapType.TERRAIN
                        else -> MapType.NORMAL
                    }
                },
                modifier = Modifier.size(44.dp).testTag("map_type_toggle_fab"),
                shape = CircleShape,
                containerColor = PolishCardSurface,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Toggle Map Layer",
                    tint = PolishPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Fit All Markers in View FAB
            if (displayedPlaces.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val builder = LatLngBounds.builder()
                                displayedPlaces.forEach {
                                    builder.include(LatLng(it.latitude, it.longitude))
                                }
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngBounds(builder.build(), 120),
                                    durationMs = 600
                                )
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    },
                    modifier = Modifier.size(44.dp).testTag("fit_all_markers_fab"),
                    shape = CircleShape,
                    containerColor = PolishCardSurface,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Fit All Markers",
                        tint = PolishPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Loading Road Route Badge
        if (isLoadingRoute) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = PolishPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculating Pilgrimage Route...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishPrimary
                    )
                }
            }
        }

        // Bottom Selected Place Preview Card
        AnimatedVisibility(
            visible = activeSelectedPlace != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            activeSelectedPlace?.let { place ->
                val isInItinerary = itineraryPlaces.any { it.id == place.id }

                ElevatedCard(
                    shape = RoundedCornerShape(22.dp),
                    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                        containerColor = PolishCardSurface
                    ),
                    elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
                        defaultElevation = 8.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_place_preview_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Place Image or Icon Thumbnail
                            if (!place.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = place.imageUrl,
                                    contentDescription = place.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(68.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = PolishPrimaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = getCategoryEmoji(place.category),
                                            fontSize = 28.sp
                                        )
                                    }
                                }
                            }

                            // Place Name & Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = place.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { activeSelectedPlace = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = PolishTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                place.hindiName?.let {
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PolishPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PolishPurpleBadge
                                    ) {
                                        Text(
                                            text = place.category.uppercase(Locale.ROOT),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PolishPurpleText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    place.openingHours?.let {
                                        Text(
                                            text = "• $it",
                                            fontSize = 11.sp,
                                            color = PolishTextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Description snippet
                        place.description?.let { desc ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = PolishTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Navigate in Google Maps, Itinerary toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Navigate Button (Launches default Google Maps app with turn-by-turn directions from user's current location)
                            Button(
                                onClick = {
                                    launchGoogleMapsNavigationToPlace(context, place)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PolishPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.1f).testTag("map_marker_navigate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Navigate to ${place.name}",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Navigate",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // 2. Toggle Itinerary Button
                            OutlinedButton(
                                onClick = {
                                    if (isInItinerary) {
                                        onRemoveFromItinerary(place.id)
                                    } else {
                                        onAddToItinerary(place)
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isInItinerary) PolishGreenBg else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isInItinerary) PolishGreen else PolishBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("map_preview_itinerary_button")
                            ) {
                                Icon(
                                    imageVector = if (isInItinerary) Icons.Default.Check else Icons.Default.Route,
                                    contentDescription = null,
                                    tint = if (isInItinerary) PolishGreen else PolishTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isInItinerary) "In Itinerary" else "+ Add Stop",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isInItinerary) PolishGreen else PolishTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Launches the default Google Maps app with turn-by-turn navigation from the user's current location to the selected site.
 */
fun launchGoogleMapsNavigationToPlace(context: Context, place: Place) {
    val navUri = Uri.parse("google.navigation:q=${place.latitude},${place.longitude}&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback to browser Google Maps directions
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}&travelmode=driving")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        try {
            context.startActivity(webIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open navigation", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Returns distinct Google Maps Marker Hue for different heritage & spiritual categories.
 */
private fun getCategoryMarkerHue(category: String): Float {
    return when (category.lowercase()) {
        "temple" -> BitmapDescriptorFactory.HUE_ORANGE      // Saffron / Orange for Spiritual Temples
        "ghat", "riverside" -> BitmapDescriptorFactory.HUE_AZURE  // Azure / Cyan for Riverside & Sangam Ghats
        "heritage" -> BitmapDescriptorFactory.HUE_YELLOW    // Yellow / Gold for Forts & Monuments
        else -> BitmapDescriptorFactory.HUE_ROSE            // Rose / Violet for Cultural sites
    }
}

/**
 * Returns emoji icon corresponding to the place category.
 */
private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "temple" -> "🛕"
        "ghat", "riverside" -> "🌊"
        "heritage" -> "🏰"
        else -> "🏛️"
    }
}

/**
 * Standalone Full-screen Prayagraj Interactive Map Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayagrajMapScreen(
    allPlaces: List<Place>,
    onBack: () -> Unit = {},
    selectedPlaceId: String? = null,
    onSelectPlaceDetails: (Place) -> Unit = {}
) {
    val liveSelectedPlaces by ItineraryManager.selectedPlaces.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prayagraj Heritage & Spiritual Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${allPlaces.size} Sacred & Historical Landmarks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("map_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (liveSelectedPlaces.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PolishPrimaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "${liveSelectedPlaces.size} Stops Route",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishBackground
                )
            )
        }
    ) { paddingValues ->
        PrayagrajHeritageMapView(
            allPlaces = allPlaces,
            selectedPlaceId = selectedPlaceId,
            itineraryPlaces = liveSelectedPlaces,
            onAddToItinerary = { ItineraryManager.addPlace(it) },
            onRemoveFromItinerary = { ItineraryManager.removePlace(it) },
            onSelectPlace = onSelectPlaceDetails,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
