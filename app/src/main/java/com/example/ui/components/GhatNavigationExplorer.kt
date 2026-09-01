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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

/**
 * Visual Crowd Density Badge with pulse dot and level description
 */
@Composable
fun CrowdStatusIndicator(
    level: CrowdDensityLevel,
    percentage: Int? = null,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val (bg, border, text, label, icon) = when (level) {
        CrowdDensityLevel.LOW -> Quintuple(
            Emerald50,
            Emerald200,
            Emerald700,
            "Low Crowd",
            "🟢"
        )
        CrowdDensityLevel.MODERATE -> Quintuple(
            Color(0xFFFEF3C7),
            Color(0xFFFDE68A),
            Color(0xFFD97706),
            "Moderate",
            "🟡"
        )
        CrowdDensityLevel.HIGH -> Quintuple(
            Saffron100,
            Saffron300,
            Saffron800,
            "High Crowd",
            "🟠"
        )
        CrowdDensityLevel.SEVERE -> Quintuple(
            PolishRedBg,
            Color(0xFFFECACA),
            PolishRed,
            "Heavy Surge",
            "🔴"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.testTag("crowd_status_badge_${level.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = if (isCompact) 6.dp else 10.dp,
                vertical = if (isCompact) 3.dp else 5.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 6.dp else 8.dp)
                    .clip(CircleShape)
                    .background(text)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (percentage != null && !isCompact) "$label ($percentage%)" else label,
                fontSize = if (isCompact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = text
            )
        }
    }
}

/**
 * Visual Crowd Density Meter Bar (0 to 100%)
 */
@Composable
fun CrowdMeterBar(
    percentage: Int,
    level: CrowdDensityLevel,
    modifier: Modifier = Modifier
) {
    val barColor = when (level) {
        CrowdDensityLevel.LOW -> Emerald500
        CrowdDensityLevel.MODERATE -> Color(0xFFF59E0B)
        CrowdDensityLevel.HIGH -> Saffron600
        CrowdDensityLevel.SEVERE -> PolishRed
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Ghat Capacity Usage",
                fontSize = 11.sp,
                color = PolishTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$percentage% Full",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(PolishBorderDarker.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (percentage.coerceIn(5, 100) / 100f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * Comprehensive Ghat Distance Matrix Card (e.g. Kila Ghat to Saraswati Ghat)
 */
@Composable
fun GhatDistancePairCard(
    pair: GhatDistancePair,
    onViewRoute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ghat_distance_card_${pair.originGhatId}_to_${pair.destinationGhatId}"),
        shape = RoundedCornerShape(16.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Origin to Destination Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pair.originName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = pair.originHindi,
                        fontSize = 11.sp,
                        color = PolishTextTertiary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PolishPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "to",
                            tint = PolishPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = pair.destinationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PolishTextPrimary,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = pair.destinationHindi,
                        fontSize = 11.sp,
                        color = PolishTextTertiary,
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transport Options Comparison (Riverboat vs Walking vs E-Rickshaw)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Riverboat Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Ocean50,
                    border = BorderStroke(1.dp, Ocean200),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🛶 Boat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ocean700)
                        Text(
                            text = "${pair.riverDistanceKm} km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ocean700
                        )
                        Text(
                            text = "~${pair.boatDurationMinutes} mins",
                            fontSize = 10.sp,
                            color = Ocean600
                        )
                    }
                }

                // Walking Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🚶 Walk", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald700)
                        Text(
                            text = "${pair.walkingDistanceKm} km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald700
                        )
                        Text(
                            text = "~${pair.walkingDurationMinutes} mins",
                            fontSize = 10.sp,
                            color = Emerald600
                        )
                    }
                }

                // E-Rickshaw Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🛺 E-Auto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        Text(
                            text = "Road",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                        Text(
                            text = "~${pair.erickshawDurationMinutes} mins",
                            fontSize = 10.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Note snippet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PolishBackground, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(text = "💡 ", fontSize = 11.sp)
                Text(
                    text = pair.highlightNote,
                    fontSize = 11.sp,
                    color = PolishTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Interactive Ghat Details & Live Status Card
 */
@Composable
fun InteractiveGhatCard(
    ghat: GhatDetailInfo,
    onSelectForDistanceOrigin: () -> Unit = {},
    onSelectForDistanceDest: () -> Unit = {},
    onViewOnMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_ghat_card_${ghat.id}"),
        shape = RoundedCornerShape(18.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Ghat Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ghat.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = ghat.hindiName,
                        fontSize = 12.sp,
                        color = PolishPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "River: ${ghat.river}",
                        fontSize = 11.sp,
                        color = PolishTextTertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                CrowdStatusIndicator(
                    level = ghat.crowdDensity,
                    percentage = ghat.crowdPercentage
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Crowd Capacity Bar
            CrowdMeterBar(
                percentage = ghat.crowdPercentage,
                level = ghat.crowdDensity
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Real-time Key Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishBackground,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Wait Time", fontSize = 10.sp, color = PolishTextTertiary)
                        Text(
                            text = "${ghat.waitingTimeMinutes} mins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishBackground,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Cleanliness", fontSize = 10.sp, color = PolishTextTertiary)
                        Text(
                            text = "⭐ ${ghat.cleanlinessRating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishBackground,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Safety Score", fontSize = 10.sp, color = PolishTextTertiary)
                        Text(
                            text = "🛡️ ${ghat.safetyRating}/5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Facilities and Features
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PolishBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalParking, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = ghat.parkingLocation, fontSize = 11.sp, color = PolishTextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ElectricRickshaw, contentDescription = null, tint = PolishTeal, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = ghat.batteryAutoPickupPoint, fontSize = 11.sp, color = PolishTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WbTwilight, contentDescription = null, tint = Saffron600, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Aarti: ${ghat.aartiTimings}", fontSize = 11.sp, color = Saffron800, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectForDistanceOrigin,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PolishPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Calc Distance", fontSize = 11.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onViewOnMap,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "View Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Full Ghat Distance Calculator & Live Status Explorer Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhatNavigationExplorerScreen(
    onNavigateToMapWithGhat: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allGhats = remember { PRAYAGRAJ_GHATS }
    val allDistances = remember { GHAT_INTER_DISTANCES }

    var selectedOriginGhatId by remember { mutableStateOf("kila-ghat") }
    var selectedDestGhatId by remember { mutableStateOf("saraswati-ghat") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Route & Distance Calculator, 1: Live Crowd Status Matrix
    var searchQuery by remember { mutableStateOf("") }
    var crowdFilter by remember { mutableStateOf<CrowdDensityLevel?>(null) }

    val originGhat = remember(selectedOriginGhatId) { allGhats.find { it.id == selectedOriginGhatId } ?: allGhats.first() }
    val destGhat = remember(selectedDestGhatId) { allGhats.find { it.id == selectedDestGhatId } ?: allGhats[1] }

    // Find custom distance pair or calculate dynamically
    val activeDistancePair = remember(selectedOriginGhatId, selectedDestGhatId) {
        allDistances.find {
            (it.originGhatId == selectedOriginGhatId && it.destinationGhatId == selectedDestGhatId) ||
            (it.originGhatId == selectedDestGhatId && it.destinationGhatId == selectedOriginGhatId)
        } ?: run {
            // Dynamic fallback distance based on coordinates
            val dRiver = 2.0
            val dWalk = 2.5
            GhatDistancePair(
                originGhatId = originGhat.id,
                originName = originGhat.name,
                originHindi = originGhat.hindiName,
                destinationGhatId = destGhat.id,
                destinationName = destGhat.name,
                destinationHindi = destGhat.hindiName,
                riverDistanceKm = dRiver,
                walkingDistanceKm = dWalk,
                boatDurationMinutes = (dRiver * 7).toInt() + 5,
                walkingDurationMinutes = (dWalk * 12).toInt(),
                erickshawDurationMinutes = (dWalk * 4).toInt() + 4,
                routeType = "Waterway & Bund Connection",
                highlightNote = "Direct pilgrim corridor connecting ${originGhat.name} and ${destGhat.name}."
            )
        }
    }

    val filteredGhats = remember(allGhats, searchQuery, crowdFilter) {
        allGhats.filter { ghat ->
            val matchSearch = if (searchQuery.isBlank()) true else {
                ghat.name.contains(searchQuery, ignoreCase = true) ||
                ghat.hindiName.contains(searchQuery, ignoreCase = true) ||
                ghat.river.contains(searchQuery, ignoreCase = true)
            }
            val matchCrowd = crowdFilter == null || ghat.crowdDensity == crowdFilter
            matchSearch && matchCrowd
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ghat Navigation & Distances",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PolishGreenBg
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Prayagraj Riverfront Distances & Real-Time Crowd Indicators",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PolishSurface)
            )
        },
        containerColor = PolishBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // Tab Selector: Route Calculator vs Official Boat Booking vs Live Crowd Matrix
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PolishSurface,
                    contentColor = PolishPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, PolishBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "📏 Distances",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "🛶 Boat Booking",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "🚦 Crowd Status",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Section: Interactive Pairwise Distance Selector
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PolishCardSurface,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Origin & Destination Ghats",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Calculate exact water route distances, boat durations, and walking paths",
                                fontSize = 11.sp,
                                color = PolishTextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Origin Selector Row
                            Text(text = "Origin Ghat (From):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PolishTextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allGhats.forEach { ghat ->
                                    val isSelected = ghat.id == selectedOriginGhatId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedOriginGhatId = ghat.id },
                                        label = { Text(ghat.name, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PolishPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Swap Button
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = PolishPrimaryContainer,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            val temp = selectedOriginGhatId
                                            selectedOriginGhatId = selectedDestGhatId
                                            selectedDestGhatId = temp
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = "Swap Ghats",
                                            tint = PolishPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Destination Selector Row
                            Text(text = "Destination Ghat (To):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PolishTextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allGhats.forEach { ghat ->
                                    val isSelected = ghat.id == selectedDestGhatId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedDestGhatId = ghat.id },
                                        label = { Text(ghat.name, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PolishTeal,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Distance Card Result
                item {
                    GhatDistancePairCard(
                        pair = activeDistancePair,
                        onViewRoute = { onNavigateToMapWithGhat(selectedOriginGhatId) }
                    )
                }

                // Visual Key Comparison Banner (Kila Ghat <-> Saraswati Ghat highlight)
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PolishPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Key Corridor: Kila Ghat ↔ Saraswati Ghat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PolishOnPrimaryContainer
                                )
                                Text(
                                    text = "2.1 km by riverboat (~15 mins) | 2.4 km by paved walkway (~28 mins) | 10 mins by E-Rickshaw along Yamuna Bund Road.",
                                    fontSize = 11.sp,
                                    color = PolishSubtext
                                )
                            }
                        }
                    }
                }

                // Pre-calculated Popular Ghat Circuits
                item {
                    Text(
                        text = "Popular Riverfront Distance Routes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(allDistances) { pair ->
                    GhatDistancePairCard(
                        pair = pair,
                        onViewRoute = { onNavigateToMapWithGhat(pair.originGhatId) }
                    )
                }
            } else if (selectedTab == 1) {
                // Section: Official Boat Booking & Fare Tiers
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Anti-Overcharging Notice (Kumbh 1920)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Never pay more than official rates (Shared ₹50–₹100, Private ₹600–₹1200). Mandatory life jacket required. Dial 1920 for instant police dispute assistance.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1920"))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("1920", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    BoatBookingTierComponent(
                        onBookCounter = { fare, counterName ->
                            // Handled within dialog with generated token
                        }
                    )
                }

                item {
                    Text(
                        text = "Verified Boatmen Directory & Direct Booking",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(SAMPLE_VERIFIED_BOATMEN) { boatman ->
                    VerifiedBoatmanCard(
                        boatman = boatman,
                        onCallClick = { /* Real phone intent */ },
                        onBookRideClick = { /* Book ride */ }
                    )
                }
            } else {
                // Section: Live Crowd Matrix & Ghat Directory
                item {
                    // Search & Crowd filter
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PolishCardSurface,
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search ghats by name or river...", fontSize = 12.sp) },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PolishPrimary)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = crowdFilter == null,
                                    onClick = { crowdFilter = null },
                                    label = { Text("All (${allGhats.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = crowdFilter == CrowdDensityLevel.LOW,
                                    onClick = { crowdFilter = if (crowdFilter == CrowdDensityLevel.LOW) null else CrowdDensityLevel.LOW },
                                    label = { Text("🟢 Low Crowd", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = crowdFilter == CrowdDensityLevel.MODERATE,
                                    onClick = { crowdFilter = if (crowdFilter == CrowdDensityLevel.MODERATE) null else CrowdDensityLevel.MODERATE },
                                    label = { Text("🟡 Moderate", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = crowdFilter == CrowdDensityLevel.HIGH,
                                    onClick = { crowdFilter = if (crowdFilter == CrowdDensityLevel.HIGH) null else CrowdDensityLevel.HIGH },
                                    label = { Text("🟠 High Crowd", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = crowdFilter == CrowdDensityLevel.SEVERE,
                                    onClick = { crowdFilter = if (crowdFilter == CrowdDensityLevel.SEVERE) null else CrowdDensityLevel.SEVERE },
                                    label = { Text("🔴 Severe Surge", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Ghats List with Live Status Indicators
                items(filteredGhats, key = { it.id }) { ghat ->
                    InteractiveGhatCard(
                        ghat = ghat,
                        onSelectForDistanceOrigin = {
                            selectedOriginGhatId = ghat.id
                            selectedTab = 0
                        },
                        onViewOnMap = {
                            onNavigateToMapWithGhat(ghat.id)
                        }
                    )
                }
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
