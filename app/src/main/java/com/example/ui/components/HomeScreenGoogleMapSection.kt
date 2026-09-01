package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.AccessibilityLevel
import com.example.data.model.Place
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Ocean100
import com.example.ui.theme.Ocean200
import com.example.ui.theme.Ocean50
import com.example.ui.theme.Ocean700
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishBorderDarker
import com.example.ui.theme.PolishCardSurface
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron200
import com.example.ui.theme.Saffron50
import com.example.ui.theme.Saffron700
import com.example.ui.theme.Saffron800
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
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
 * Sacred Triveni Sangam Coordinates (Epicenter of Prayagraj)
 */
val SANGAM_EPICENTER = LatLng(25.4290, 81.8845)

/**
 * Sacred Heritage Circuit Route Coordinates connecting the major pilgrimage sites:
 * Sangam -> Bade Hanuman Ji -> Akshayavat / Fort -> Shankar Viman -> Saraswati Ghat -> Mankameshwar -> Alopi Devi -> Anand Bhavan -> Khusro Bagh
 */
val PRAYAGRAJ_SACRED_CIRCUIT = listOf(
    LatLng(25.4290, 81.8845), // Triveni Sangam
    LatLng(25.4310, 81.8810), // Shankar Viman Mandapam
    LatLng(25.4325, 81.8790), // Bade Hanuman Ji
    LatLng(25.4302, 81.8765), // Akshayavat & Fort
    LatLng(25.4340, 81.8650), // Saraswati Ghat
    LatLng(25.4320, 81.8610), // Mankameshwar
    LatLng(25.4412, 81.8680), // Alopi Devi Shaktipeeth
    LatLng(25.4520, 81.8720), // Nag Vasuki Temple
    LatLng(25.4578, 81.8592), // Anand Bhavan & Swaraj Bhavan
    LatLng(25.4540, 81.8480), // Chandrashekhar Azad Park
    LatLng(25.4418, 81.8242)  // Khusro Bagh
)

/**
 * Embedded Google Maps Section on the Home Screen.
 * Displays interactive map with Sacred Ghats, Ancient Temples, and Cultural Heritage sites in Prayagraj.
 */
@Composable
fun HomeScreenGoogleMapSection(
    places: List<Place>,
    itineraryPlaces: List<Place> = emptyList(),
    onSelectPlaceDetails: (Place) -> Unit = {},
    onToggleItinerary: (Place) -> Unit = {},
    onExpandFullMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryFilter by remember { mutableStateOf("all") }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showCircuitRoute by remember { mutableStateOf(true) }
    var isIllustratedMode by remember { mutableStateOf(false) }

    // Filter places based on selected category tab
    val displayedPlaces = remember(places, selectedCategoryFilter) {
        if (places.isEmpty()) emptyList() else {
            when (selectedCategoryFilter) {
                "ghat" -> places.filter { it.category.equals("ghat", ignoreCase = true) || it.tags.any { tag -> tag.contains("ghat", true) || tag.contains("boat", true) } }
                "temple" -> places.filter { it.category.equals("temple", ignoreCase = true) }
                "heritage" -> places.filter { it.category.equals("heritage", ignoreCase = true) || it.tags.any { tag -> tag.contains("heritage", true) || tag.contains("museum", true) } }
                else -> places
            }
        }
    }

    // Camera state initialized to Triveni Sangam
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(SANGAM_EPICENTER, 13.2f)
    }

    // Pan camera when a place is selected
    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            coroutineScope.launch {
                try {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(place.latitude, place.longitude),
                            15.8f
                        ),
                        durationMs = 750
                    )
                } catch (e: Exception) {
                    // Ignore if map not ready
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_google_map_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = BorderStroke(1.dp, PolishBorderDarker),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Title, Live GPS Badge, and Expand Fullscreen Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PolishPrimaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🗺️", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Sacred Ghats & Heritage Map",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PolishTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Emerald50,
                                border = BorderStroke(1.dp, Emerald200)
                            ) {
                                Text(
                                    text = "LIVE GPS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Interactive Google Maps • Sangam, Ghats & Temples",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onExpandFullMap,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PolishPrimaryContainer,
                        contentColor = PolishPrimary
                    ),
                    modifier = Modifier.testTag("home_map_expand_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand Fullscreen Map",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Full Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapCategoryFilterChip(
                    title = "All Sites (${places.size})",
                    icon = "📍",
                    isSelected = selectedCategoryFilter == "all",
                    onClick = { selectedCategoryFilter = "all" }
                )
                MapCategoryFilterChip(
                    title = "Sacred Ghats",
                    icon = "🛶",
                    isSelected = selectedCategoryFilter == "ghat",
                    onClick = { selectedCategoryFilter = "ghat" }
                )
                MapCategoryFilterChip(
                    title = "Ancient Temples",
                    icon = "🕉️",
                    isSelected = selectedCategoryFilter == "temple",
                    onClick = { selectedCategoryFilter = "temple" }
                )
                MapCategoryFilterChip(
                    title = "Cultural Heritage",
                    icon = "🏛️",
                    isSelected = selectedCategoryFilter == "heritage",
                    onClick = { selectedCategoryFilter = "heritage" }
                )
            }

            // Quick Landmark Jump Carousel
            Text(
                text = "QUICK JUMP TO SACRED SITES",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PolishTextTertiary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val keyLandmarks = listOf(
                    "Triveni Sangam" to "🌊",
                    "Bade Hanuman Ji" to "🕉️",
                    "Saraswati Ghat" to "🛶",
                    "Anand Bhavan" to "🏛️",
                    "Akshayavat" to "🌳",
                    "Khusro Bagh" to "🏰",
                    "Alopi Devi" to "🔱",
                    "Chandrashekhar Azad Park" to "🌿",
                    "Allahabad Fort" to "🛡️",
                    "Mankameshwar" to "🛕"
                )

                items(keyLandmarks) { (landmarkName, emoji) ->
                    val matchingPlace = places.find { it.name.contains(landmarkName, ignoreCase = true) }
                    val isSelected = selectedPlace?.id == matchingPlace?.id

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) PolishPrimary else PolishCardSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PolishPrimary else PolishBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                matchingPlace?.let {
                                    selectedPlace = it
                                } ?: run {
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(SANGAM_EPICENTER, 14f)
                                        )
                                    }
                                }
                            }
                            .testTag("map_quick_jump_${landmarkName.lowercase().replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(emoji, fontSize = 12.sp)
                            Text(
                                text = landmarkName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PolishTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Interactive Google Maps Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, PolishBorderDarker, RoundedCornerShape(18.dp))
                    .testTag("home_google_maps_viewport")
            ) {
                // Google Map Compose View
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = mapType,
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = true,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        rotationGesturesEnabled = true,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        tiltGesturesEnabled = false
                    ),
                    onMapClick = {
                        selectedPlace = null
                    }
                ) {
                    // Sacred Sangam Confluence Radar Zone
                    Circle(
                        center = SANGAM_EPICENTER,
                        radius = 2800.0,
                        fillColor = Color(0x156750A4),
                        strokeColor = Color(0x666750A4),
                        strokeWidth = 2f
                    )

                    // Heritage Pilgrim Circuit Polyline
                    if (showCircuitRoute) {
                        Polyline(
                            points = PRAYAGRAJ_SACRED_CIRCUIT,
                            color = PolishPrimary.copy(alpha = 0.85f),
                            width = 10f,
                            geodesic = true,
                            startCap = RoundCap(),
                            endCap = RoundCap(),
                            jointType = JointType.ROUND
                        )
                    }

                    // Render Markers for Sacred Sites & Heritage Monuments
                    displayedPlaces.forEach { place ->
                        val isMarkerSelected = selectedPlace?.id == place.id
                        val markerHue = getMarkerHueForCategory(place.category)

                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            title = place.name,
                            snippet = "${place.hindiName ?: place.category.uppercase(Locale.ROOT)} • Tap info to Navigate",
                            icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                            onClick = {
                                selectedPlace = place
                                false
                            },
                            onInfoWindowClick = {
                                val navUri = Uri.parse("google.navigation:q=${place.latitude},${place.longitude}&mode=d")
                                val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}&travelmode=driving")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            }
                        )
                    }
                }

                // Floating Map Control Buttons (Recenter, Map Type, Circuit Route)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Recenter on Triveni Sangam
                    Surface(
                        shape = CircleShape,
                        color = PolishSurface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(SANGAM_EPICENTER, 13.5f),
                                        durationMs = 600
                                    )
                                }
                                selectedPlace = null
                            }
                            .testTag("home_map_recenter_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Recenter on Sangam",
                                tint = PolishPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Map Type Toggle (Normal -> Satellite -> Terrain)
                    Surface(
                        shape = CircleShape,
                        color = PolishSurface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                mapType = when (mapType) {
                                    MapType.NORMAL -> MapType.HYBRID
                                    MapType.HYBRID -> MapType.TERRAIN
                                    else -> MapType.NORMAL
                                }
                            }
                            .testTag("home_map_type_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Toggle Map Type",
                                tint = PolishPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Sacred Circuit Polyline Toggle
                    Surface(
                        shape = CircleShape,
                        color = if (showCircuitRoute) PolishPrimaryContainer else PolishSurface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                showCircuitRoute = !showCircuitRoute
                            }
                            .testTag("home_map_route_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = "Toggle Pilgrim Route",
                                tint = if (showCircuitRoute) PolishPrimary else PolishTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Map Legend Overlay on Top-Left
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishSurface.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, PolishBorder),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFF00B0FF), modifier = Modifier.size(8.dp)) {}
                            Text("Ghats", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PolishTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFFF9800), modifier = Modifier.size(8.dp)) {}
                            Text("Temples", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PolishTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFFFD600), modifier = Modifier.size(8.dp)) {}
                            Text("Heritage", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PolishTextSecondary)
                        }
                    }
                }

                // Interactive Bottom Overlay when a Marker is Selected
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectedPlace != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                ) {
                    selectedPlace?.let { place ->
                        val isInItinerary = itineraryPlaces.any { it.id == place.id }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PolishCardSurface.copy(alpha = 0.97f),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, PolishBorderDarker),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Place Thumbnail
                                        if (!place.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = place.imageUrl,
                                                contentDescription = place.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.dp, PolishBorder, RoundedCornerShape(10.dp))
                                            )
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = PolishPrimaryContainer,
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(getCategoryEmojiIcon(place.category), fontSize = 22.sp)
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = place.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = PolishTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            place.hindiName?.let { hindi ->
                                                Text(
                                                    text = hindi,
                                                    fontSize = 10.sp,
                                                    color = PolishPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "${place.category.uppercase(Locale.ROOT)} • ${place.openingHours ?: "Open"}",
                                                fontSize = 10.sp,
                                                color = PolishTextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { selectedPlace = null },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Card",
                                            tint = PolishTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Quick Action Buttons: Navigate in Google Maps, Itinerary, Details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Navigate in Google Maps App (Turn-by-turn directions from user's current location)
                                    Button(
                                        onClick = {
                                            val gmmIntentUri = Uri.parse("google.navigation:q=${place.latitude},${place.longitude}&mode=d")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }
                                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(mapIntent)
                                            } else {
                                                // Fallback to browser Google Maps
                                                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}&travelmode=driving")
                                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1.1f).testTag("map_marker_navigate_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = "Navigate to ${place.name}",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Navigate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    // 2. Toggle Itinerary
                                    OutlinedButton(
                                        onClick = { onToggleItinerary(place) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isInItinerary) PolishPrimaryContainer else Color.Transparent
                                        ),
                                        modifier = Modifier.weight(1f).testTag("map_marker_itinerary_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isInItinerary) Icons.Default.Check else Icons.Default.NearMe,
                                            contentDescription = null,
                                            tint = if (isInItinerary) PolishPrimary else PolishTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isInItinerary) "Added" else "Add Stop",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isInItinerary) PolishPrimary else PolishTextPrimary
                                        )
                                    }

                                    // 3. View Full Details Dialog
                                    OutlinedButton(
                                        onClick = { onSelectPlaceDetails(place) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(0.9f).testTag("map_marker_details_button")
                                    ) {
                                        Text("Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Metrics Bar: Summary counts and coordinate telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val ghatsCount = places.count { it.category.equals("ghat", ignoreCase = true) }
                val templesCount = places.count { it.category.equals("temple", ignoreCase = true) }
                val heritageCount = places.count { it.category.equals("heritage", ignoreCase = true) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🛶 $ghatsCount Ghats",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ocean700
                    )
                    Text(
                        text = "🕉️ $templesCount Temples",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Saffron800
                    )
                    Text(
                        text = "🏛️ $heritageCount Monuments",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTextSecondary
                    )
                }

                Text(
                    text = "Sangam: 25.429°N, 81.884°E",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextTertiary
                )
            }
        }
    }
}

@Composable
private fun MapCategoryFilterChip(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PolishPrimary else PolishBackground,
        border = BorderStroke(
            1.dp,
            if (isSelected) PolishPrimary else PolishBorder
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("map_filter_chip_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 12.sp)
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else PolishTextPrimary
            )
        }
    }
}

private fun getMarkerHueForCategory(category: String): Float {
    return when (category.lowercase()) {
        "ghat", "riverside" -> BitmapDescriptorFactory.HUE_AZURE  // Azure / Blue for Sacred Ghats
        "temple" -> BitmapDescriptorFactory.HUE_ORANGE           // Saffron / Orange for Ancient Temples
        "heritage" -> BitmapDescriptorFactory.HUE_YELLOW         // Gold / Yellow for Cultural Heritage
        else -> BitmapDescriptorFactory.HUE_ROSE                 // Rose for other cultural sites
    }
}

private fun getCategoryEmojiIcon(category: String): String {
    return when (category.lowercase()) {
        "ghat", "riverside" -> "🛶"
        "temple" -> "🕉️"
        "heritage" -> "🏛️"
        else -> "📍"
    }
}
