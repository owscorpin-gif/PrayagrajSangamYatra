package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccessibilityLevel
import com.example.data.model.NearbyPlace
import com.example.data.model.Place
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlacesUiState
import com.example.ui.viewmodel.PlacesViewModel
import java.util.Locale

enum class BottomNavTab {
    HOME, ITINERARY, BOOKINGS, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    viewModel: PlacesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showItinerarySheet by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var selectedServiceModal by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PolishBackground,
        topBar = {
            ProfessionalAppBar(
                cartCount = uiState.itineraryCart.size,
                onProfileClick = { showItinerarySheet = true }
            )
        },
        bottomBar = {
            ProfessionalBottomBar(
                currentTab = currentTab,
                cartCount = uiState.itineraryCart.size,
                onTabSelected = { tab ->
                    currentTab = tab
                    if (tab == BottomNavTab.ITINERARY) {
                        showItinerarySheet = true
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PolishBackground)
        ) {
            when (currentTab) {
                BottomNavTab.HOME, BottomNavTab.ITINERARY -> {
                    HomeContentView(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenItinerary = { showItinerarySheet = true },
                        onSelectService = { selectedServiceModal = it }
                    )
                }
                BottomNavTab.BOOKINGS -> {
                    BookingsOverviewScreen(
                        itinerary = uiState.itineraryCart,
                        onStartPlanning = {
                            currentTab = BottomNavTab.HOME
                            showItinerarySheet = true
                        }
                    )
                }
                BottomNavTab.SETTINGS -> {
                    SettingsOverviewScreen()
                }
            }
        }
    }

    // Place Details Modal Dialog
    uiState.selectedPlaceForDetails?.let { place ->
        PlaceDetailsDialog(
            place = place,
            isInItinerary = uiState.itineraryCart.any { it.id == place.id },
            onDismiss = { viewModel.selectPlaceDetails(null) },
            onToggleItinerary = {
                if (uiState.itineraryCart.any { it.id == place.id }) {
                    viewModel.removeFromItinerary(place.id)
                } else {
                    viewModel.addToItinerary(place)
                }
            }
        )
    }

    // Itinerary Cart Bottom Sheet
    if (showItinerarySheet) {
        ItineraryBottomSheet(
            itinerary = uiState.itineraryCart,
            onDismiss = { showItinerarySheet = false },
            onRemoveStop = { viewModel.removeFromItinerary(it) },
            onClear = { viewModel.clearItinerary() }
        )
    }

    // Service Details Dialog
    selectedServiceModal?.let { serviceTitle ->
        ServiceMarketplaceDialog(
            serviceName = serviceTitle,
            onDismiss = { selectedServiceModal = null },
            onAction = {
                selectedServiceModal = null
                if (serviceTitle.contains("Panda", ignoreCase = true)) {
                    viewModel.selectCategory("ghat")
                } else if (serviceTitle.contains("Stay", ignoreCase = true)) {
                    viewModel.selectCategory("heritage")
                }
            }
        )
    }
}

@Composable
fun ProfessionalAppBar(
    cartCount: Int,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sacred ॐ Avatar
            Surface(
                shape = CircleShape,
                color = PolishPrimaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "ॐ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }
            }

            Column {
                Text(
                    text = "Shubh Prayag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = PolishTextPrimary
                )
                Text(
                    text = "PHASE 1 ALPHA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PolishPrimary
                )
            }
        }

        // Profile / Itinerary Badge Icon Button
        Surface(
            shape = CircleShape,
            color = PolishPurpleContainer,
            modifier = Modifier
                .size(40.dp)
                .clickable { onProfileClick() }
                .testTag("itinerary_cart_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (cartCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = PolishPrimary,
                                contentColor = Color.White
                            ) {
                                Text(cartCount.toString(), fontSize = 10.sp)
                            }
                        }
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 18.sp,
                            color = PolishPurpleText
                        )
                    }
                } else {
                    Text(
                        text = "👤",
                        fontSize = 18.sp,
                        color = PolishPurpleText
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContentView(
    uiState: PlacesUiState,
    viewModel: PlacesViewModel,
    onOpenItinerary: () -> Unit,
    onSelectService: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Bar with Professional Polish Rounded Border
        item {
            ProfessionalSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                isNearbyActive = uiState.isNearbyMode,
                onToggleNearby = { viewModel.toggleNearbyMode(it) }
            )
        }

        // 2. Hero Itinerary Planning Card
        item {
            HeroItineraryCard(
                itineraryCount = uiState.itineraryCart.size,
                onStartPlanning = onOpenItinerary
            )
        }

        // 3. Service Marketplace
        item {
            ServiceMarketplaceSection(onSelectService = onSelectService)
        }

        // 4. Nearby / Category Filter Row
        item {
            CategoryAndRadarBar(
                selectedCategory = uiState.selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) },
                isNearbyActive = uiState.isNearbyMode,
                onToggleNearby = { viewModel.toggleNearbyMode(it) }
            )
        }

        // 5. PostGIS Radius Selector (when radar is active)
        if (uiState.isNearbyMode) {
            item {
                PostgisRadiusSelector(
                    currentRadius = uiState.radiusMeters,
                    onRadiusSelected = { viewModel.updateRadius(it) },
                    userLat = uiState.userLat,
                    userLng = uiState.userLng
                )
            }
        }

        // 6. Places Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isNearbyMode) "Nearby Heritage Sites (PostGIS)" else "Sacred Heritage Sites",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Text(
                    text = if (uiState.isNearbyMode) "Radius: ${String.format(Locale.US, "%.0f", uiState.radiusMeters / 1000)}km" else "View Map",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimary,
                    modifier = Modifier.clickable {
                        viewModel.toggleNearbyMode(!uiState.isNearbyMode)
                    }
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PolishPrimary)
                }
            }
        } else if (uiState.isNearbyMode) {
            if (uiState.nearbyPlaces.isEmpty()) {
                item {
                    EmptyPlacesView(message = "No places found in this radius. Try selecting 10km or 25km above.")
                }
            } else {
                items(uiState.nearbyPlaces, key = { it.id }) { nearby ->
                    val isInItinerary = uiState.itineraryCart.any { it.id == nearby.id }
                    NearbyPlacePolishCard(
                        nearby = nearby,
                        isInItinerary = isInItinerary,
                        onAddItinerary = {
                            val place = uiState.allPlaces.find { it.id == nearby.id }
                                ?: Place(
                                    id = nearby.id,
                                    name = nearby.name,
                                    hindiName = nearby.hindiName,
                                    category = nearby.category,
                                    description = nearby.description,
                                    latitude = nearby.latitude,
                                    longitude = nearby.longitude,
                                    stepCount = nearby.stepCount,
                                    accessibilityLevel = AccessibilityLevel.EASY,
                                    openingHours = nearby.openingHours,
                                    imageUrl = nearby.imageUrl
                                )
                            if (isInItinerary) {
                                viewModel.removeFromItinerary(place.id)
                            } else {
                                viewModel.addToItinerary(place)
                            }
                        },
                        onSelectPlace = {
                            val place = uiState.allPlaces.find { it.id == nearby.id }
                                ?: Place(
                                    id = nearby.id,
                                    name = nearby.name,
                                    hindiName = nearby.hindiName,
                                    category = nearby.category,
                                    description = nearby.description,
                                    latitude = nearby.latitude,
                                    longitude = nearby.longitude,
                                    stepCount = nearby.stepCount,
                                    accessibilityLevel = AccessibilityLevel.EASY,
                                    openingHours = nearby.openingHours,
                                    imageUrl = nearby.imageUrl
                                )
                            viewModel.selectPlaceDetails(place)
                        }
                    )
                }
            }
        } else {
            if (uiState.filteredPlaces.isEmpty()) {
                item {
                    EmptyPlacesView(message = "No sacred sites match your search.")
                }
            } else {
                items(uiState.filteredPlaces, key = { it.id }) { place ->
                    val isInItinerary = uiState.itineraryCart.any { it.id == place.id }
                    MasterPlacePolishCard(
                        place = place,
                        isInItinerary = isInItinerary,
                        onToggleItinerary = {
                            if (isInItinerary) {
                                viewModel.removeFromItinerary(place.id)
                            } else {
                                viewModel.addToItinerary(place)
                            }
                        },
                        onSelectPlace = { viewModel.selectPlaceDetails(place) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfessionalSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isNearbyActive: Boolean,
    onToggleNearby: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorderDarker, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = PolishSurface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔍",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("places_search_input"),
                placeholder = {
                    Text(
                        "Search Ghats, Pandas, or Hotels...",
                        fontSize = 14.sp,
                        color = PolishTextTertiary
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = PolishTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HeroItineraryCard(
    itineraryCount: Int,
    onStartPlanning: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onStartPlanning() },
        shape = RoundedCornerShape(28.dp),
        color = PolishPrimaryContainer,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Decorative background circle from design
            Surface(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp),
                shape = CircleShape,
                color = PolishPrimaryLight.copy(alpha = 0.45f)
            ) {}

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Plan Your Sangam\nPilgrimage",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp,
                        color = PolishOnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (itineraryCount > 0) "$itineraryCount sacred stops selected" else "Multi-stop itinerary planning",
                        fontSize = 13.sp,
                        color = PolishSubtext.copy(alpha = 0.85f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PolishPrimary,
                    modifier = Modifier.clickable { onStartPlanning() }
                ) {
                    Text(
                        text = if (itineraryCount > 0) "View Itinerary ($itineraryCount)" else "Start Booking",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceMarketplaceSection(
    onSelectService: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Services",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PolishTextPrimary
            )
            Text(
                text = "View All",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PolishPrimary,
                modifier = Modifier.clickable { onSelectService("All Services Marketplace") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ServiceItem(icon = "🕉️", label = "Verified Pandas", onClick = { onSelectService("Verified Pandas & Purohits") })
            ServiceItem(icon = "🛺", label = "E-Rickshaw", onClick = { onSelectService("Electric Rickshaw & Rides") })
            ServiceItem(icon = "🏨", label = "Stay", onClick = { onSelectService("Dharamshala & Ashrams") })
            ServiceItem(icon = "🍲", label = "Satvik Food", onClick = { onSelectService("Prasadam & Satvik Bhojan") })
        }
    }
}

@Composable
fun ServiceItem(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, PolishBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = PolishCardSurface,
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = PolishTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines = 2
        )
    }
}

@Composable
fun CategoryAndRadarBar(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    isNearbyActive: Boolean,
    onToggleNearby: (Boolean) -> Unit
) {
    val categories = listOf(
        "all" to "All Sites",
        "ghat" to "Ghats",
        "temple" to "Temples",
        "heritage" to "Heritage"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // GPS Radar Filter Chip
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isNearbyActive) PolishTeal else PolishSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isNearbyActive) PolishTeal else PolishBorderDarker
            ),
            modifier = Modifier
                .clickable { onToggleNearby(!isNearbyActive) }
                .testTag("gps_radar_toggle")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isNearbyActive) "📡" else "📍", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isNearbyActive) "GPS Radar ON" else "PostGIS Radar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNearbyActive) Color.White else PolishTeal
                )
            }
        }

        categories.forEach { (key, label) ->
            val isSelected = selectedCategory.equals(key, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) PolishPrimary else PolishCardSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) PolishPrimary else PolishBorder
                ),
                modifier = Modifier.clickable { onSelectCategory(key) }
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else PolishTextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun PostgisRadiusSelector(
    currentRadius: Double,
    onRadiusSelected: (Double) -> Unit,
    userLat: Double,
    userLng: Double
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorderDarker, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = PolishSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛰️", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Prayagraj PostGIS Coordinates: ${String.format(Locale.US, "%.3f", userLat)}° N, ${String.format(Locale.US, "%.3f", userLng)}° E",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    2000.0 to "2 km",
                    5000.0 to "5 km",
                    10000.0 to "10 km",
                    25000.0 to "25 km"
                ).forEach { (meters, label) ->
                    val isSelected = currentRadius == meters
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PolishTeal else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PolishTeal else PolishBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRadiusSelected(meters) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else PolishTextPrimary,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MasterPlacePolishCard(
    place: Place,
    isInItinerary: Boolean,
    onToggleItinerary: () -> Unit,
    onSelectPlace: () -> Unit
) {
    val placeIcon = when (place.category.lowercase()) {
        "ghat" -> "🌊"
        "temple" -> "🛕"
        "heritage" -> "🏰"
        else -> "🏛️"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorder, RoundedCornerShape(20.dp))
            .clickable { onSelectPlace() }
            .testTag("place_card_${place.id}"),
        shape = RoundedCornerShape(20.dp),
        color = PolishCardSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Square Icon Box
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(14.dp),
                color = PolishSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = placeIcon, fontSize = 26.sp)
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Category / Accessibility Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishPurpleBadge
                    ) {
                        Text(
                            text = "${place.stepCount} steps",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPurpleText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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

                place.description?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = PolishTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.openingHours ?: "Open Daily",
                        fontSize = 10.sp,
                        color = PolishTextTertiary
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isInItinerary) PolishPrimaryContainer else PolishSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isInItinerary) PolishPrimary else PolishBorderDarker
                        ),
                        modifier = Modifier.clickable { onToggleItinerary() }
                    ) {
                        Text(
                            text = if (isInItinerary) "✓ Added" else "+ Add",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInItinerary) PolishPrimary else PolishTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyPlacePolishCard(
    nearby: NearbyPlace,
    isInItinerary: Boolean,
    onAddItinerary: () -> Unit,
    onSelectPlace: () -> Unit
) {
    val placeIcon = when (nearby.category.lowercase()) {
        "ghat" -> "🌊"
        "temple" -> "🛕"
        "heritage" -> "🏰"
        else -> "🏛️"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorder, RoundedCornerShape(20.dp))
            .clickable { onSelectPlace() },
        shape = RoundedCornerShape(20.dp),
        color = PolishCardSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(14.dp),
                color = PolishSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = placeIcon, fontSize = 26.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nearby.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // PostGIS Distance Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishPurpleBadge
                    ) {
                        Text(
                            text = formatDistance(nearby.distanceMeters),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPurpleText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                nearby.hindiName?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishPrimary
                    )
                }

                nearby.description?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = PolishTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${nearby.stepCount} steps • ${nearby.accessibilityLevel.replace('_', ' ')}",
                        fontSize = 10.sp,
                        color = PolishTextTertiary
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isInItinerary) PolishPrimaryContainer else PolishSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isInItinerary) PolishPrimary else PolishBorderDarker
                        ),
                        modifier = Modifier.clickable { onAddItinerary() }
                    ) {
                        Text(
                            text = if (isInItinerary) "✓ Added" else "+ Add",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInItinerary) PolishPrimary else PolishTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalBottomBar(
    currentTab: BottomNavTab,
    cartCount: Int,
    onTabSelected: (BottomNavTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(1.dp, PolishBorderDarker, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)),
        color = PolishSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = "🏠",
                label = "Home",
                isSelected = currentTab == BottomNavTab.HOME,
                onClick = { onTabSelected(BottomNavTab.HOME) }
            )
            BottomNavItem(
                icon = "🛤️",
                label = "Itinerary",
                isSelected = currentTab == BottomNavTab.ITINERARY,
                badgeCount = cartCount,
                onClick = { onTabSelected(BottomNavTab.ITINERARY) }
            )
            BottomNavItem(
                icon = "📖",
                label = "Bookings",
                isSelected = currentTab == BottomNavTab.BOOKINGS,
                onClick = { onTabSelected(BottomNavTab.BOOKINGS) }
            )
            BottomNavItem(
                icon = "⚙️",
                label = "Settings",
                isSelected = currentTab == BottomNavTab.SETTINGS,
                onClick = { onTabSelected(BottomNavTab.SETTINGS) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) PolishPrimaryContainer else Color.Transparent,
            modifier = Modifier.size(width = 54.dp, height = 30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (badgeCount > 0 && !isSelected) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = PolishPrimary,
                                contentColor = Color.White
                            ) {
                                Text(badgeCount.toString(), fontSize = 9.sp)
                            }
                        }
                    ) {
                        Text(text = icon, fontSize = 16.sp)
                    }
                } else {
                    Text(text = icon, fontSize = 16.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PolishPrimary else PolishTextSecondary
        )
    }
}

@Composable
fun BookingsOverviewScreen(
    itinerary: List<Place>,
    onStartPlanning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Active Bookings & Darshan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = PolishCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Booking #PY-2026-8841",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PolishGreenBg
                    ) {
                        Text(
                            text = "CONFIRMED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sangam Snan & Vedic Sankalp Puja",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PolishPrimary
                )
                Text(
                    text = "Purohit: Acharya Vidyadhar Shastri • E-Rickshaw Pickup scheduled at Sangam Gate 3",
                    fontSize = 11.sp,
                    color = PolishTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = PolishBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Amount: ₹1,500", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Date: Tomorrow, 05:30 AM", fontSize = 11.sp, color = PolishTextTertiary)
                }
            }
        }

        if (itinerary.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = PolishPrimaryContainer
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Draft Yatra Route Pending",
                        fontWeight = FontWeight.Bold,
                        color = PolishOnPrimaryContainer
                    )
                    Text(
                        text = "You have ${itinerary.size} places waiting in your multi-stop itinerary cart.",
                        fontSize = 12.sp,
                        color = PolishSubtext
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onStartPlanning,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                    ) {
                        Text("Checkout Itinerary")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsOverviewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Yatra Settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PolishCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language Preference", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("हिन्दी / English", fontSize = 12.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                }
                Divider(color = PolishBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PostGIS Location Radius", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("5.0 km default", fontSize = 12.sp, color = PolishTeal, fontWeight = FontWeight.Bold)
                }
                Divider(color = PolishBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Supabase Cloud Sync", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Connected", fontSize = 12.sp, color = PolishGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ServiceMarketplaceDialog(
    serviceName: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = serviceName, fontWeight = FontWeight.Bold, color = PolishPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Seamlessly book verified pilgrimage services through Prayagraj Yatra's unified marketplace.",
                    fontSize = 13.sp,
                    color = PolishTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishPrimaryContainer
                ) {
                    Text(
                        text = "✓ Certified Rates & Verified Purohits\n✓ Zero surge electric vehicle dispatch\n✓ Real-time PostGIS GPS tracking",
                        fontSize = 12.sp,
                        color = PolishOnPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Text("Explore Offerings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PolishTextSecondary)
            }
        }
    )
}

@Composable
fun PlaceDetailsDialog(
    place: Place,
    isInItinerary: Boolean,
    onDismiss: () -> Unit,
    onToggleItinerary: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = place.name, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                place.hindiName?.let {
                    Text(text = it, color = PolishPrimary, fontSize = 14.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                place.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = PolishTextSecondary)
                }

                Divider(color = PolishBorder)

                // Opening Hours
                place.openingHours?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕒 ", fontSize = 13.sp)
                        Text(text = it, fontSize = 12.sp, color = PolishTextPrimary)
                    }
                }

                // PostGIS Coordinates
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍 ", fontSize = 13.sp)
                    Text(
                        text = "PostGIS: ${String.format(Locale.US, "%.4f", place.latitude)}° N, ${String.format(Locale.US, "%.4f", place.longitude)}° E",
                        fontSize = 12.sp,
                        color = PolishTeal,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Step count & Accessibility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PolishPurpleBadge
                    ) {
                        Text(
                            text = "${place.stepCount} Steps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPurpleText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PolishGreenBg
                    ) {
                        Text(
                            text = place.accessibilityLevel.name.replace('_', ' '),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onToggleItinerary,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Text(if (isInItinerary) "Remove from Itinerary" else "Add to Itinerary")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PolishTextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryBottomSheet(
    itinerary: List<Place>,
    onDismiss: () -> Unit,
    onRemoveStop: (String) -> Unit,
    onClear: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pilgrim Itinerary Route",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                    Text(
                        text = "${itinerary.size} Multi-stop waypoints planned",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishTextSecondary
                    )
                }

                if (itinerary.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Clear All", color = PolishRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (itinerary.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No stops in your itinerary yet.\nTap '+ Add' on any heritage card to build your pilgrimage.",
                        textAlign = TextAlign.Center,
                        color = PolishTextSecondary
                    )
                }
            } else {
                val totalSteps = itinerary.sumOf { it.stepCount }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PolishPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🚶 ", fontSize = 16.sp)
                            Text(
                                text = "Total Yatra Steps: $totalSteps",
                                fontWeight = FontWeight.Bold,
                                color = PolishOnPrimaryContainer
                            )
                        }
                        Text(
                            text = "Est: ${itinerary.size * 45} mins",
                            fontSize = 12.sp,
                            color = PolishSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itinerary.withIndex().toList(), key = { it.value.id }) { (index, stop) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, PolishBorder, RoundedCornerShape(12.dp)),
                            color = PolishCardSurface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PolishPrimary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stop.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = PolishTextPrimary
                                    )
                                    Text(
                                        text = "${stop.stepCount} steps • ${stop.category}",
                                        fontSize = 11.sp,
                                        color = PolishTextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveStop(stop.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = PolishTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Book Electric Rides & Purohit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmptyPlacesView(message: String = "No places match your search criteria.") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🏛️", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = PolishTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDistance(distanceMeters: Double): String {
    return if (distanceMeters < 1000) {
        "${distanceMeters.toInt()}m"
    } else {
        String.format(Locale.US, "%.1fkm", distanceMeters / 1000.0)
    }
}
