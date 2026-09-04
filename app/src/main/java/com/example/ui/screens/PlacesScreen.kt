package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AccessibilityLevel
import com.example.data.model.NearbyPlace
import com.example.data.model.Place
import com.example.data.model.UserProfile
import com.example.data.remote.SupabaseProvider
import com.example.ui.components.BoatBookingTierComponent
import com.example.ui.components.GhatNavigationExplorerScreen
import com.example.ui.components.HomeScreenGoogleMapSection
import com.example.ui.components.PilgrimSafetyDashboard
import com.example.ui.components.PilgrimSafetyScreen
import com.example.ui.components.SpiritualSchedulesScreen
import com.example.ui.components.VendorRoleDrawerContent
import com.example.ui.screens.admin.PurohitAdminDashboardScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlacesUiState
import com.example.ui.viewmodel.PlacesViewModel
import com.prayagraj.app.ui.navigation.VendorAppNavigation
import kotlinx.coroutines.launch
import java.util.Locale

enum class BottomNavTab {
    HOME, GHATS, PANDAS, SAFETY, AI_GUIDE, MAP, ITINERARY, BOOKINGS, VERIFICATION, SETTINGS, PUROHIT_ADMIN, VENDOR_PORTAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    onSignOut: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToPandaDirectory: () -> Unit = {},
    onNavigateToBookingVerification: () -> Unit = {},
    viewModel: PlacesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    var showItinerarySheet by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var selectedServiceModal by remember { mutableStateOf<String?>(null) }
    var focusedPlaceOnMapId by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            VendorRoleDrawerContent(
                onSelectRoleLogin = { roleKey ->
                    currentTab = BottomNavTab.VENDOR_PORTAL
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = PolishBackground,
            topBar = {
                if (currentTab != BottomNavTab.PANDAS && currentTab != BottomNavTab.AI_GUIDE && currentTab != BottomNavTab.GHATS && currentTab != BottomNavTab.SAFETY && currentTab != BottomNavTab.BOOKINGS && currentTab != BottomNavTab.VERIFICATION && currentTab != BottomNavTab.PUROHIT_ADMIN && currentTab != BottomNavTab.VENDOR_PORTAL) {
                    ProfessionalAppBar(
                        cartCount = uiState.itineraryCart.size,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onProfileClick = { showAuthDialog = true },
                        onNavigateToMap = onNavigateToMap,
                        onNavigateToAiGuide = { currentTab = BottomNavTab.AI_GUIDE },
                        onNavigateToVerification = { currentTab = BottomNavTab.VERIFICATION },
                        onSignOutClick = { viewModel.logout(onSuccess = onSignOut) }
                    )
                }
            },
            bottomBar = {
                if (currentTab != BottomNavTab.VENDOR_PORTAL) {
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
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (currentTab == BottomNavTab.VENDOR_PORTAL) PaddingValues(0.dp) else innerPadding)
                    .background(PolishBackground)
            ) {
                if (currentTab != BottomNavTab.PANDAS && currentTab != BottomNavTab.AI_GUIDE && currentTab != BottomNavTab.GHATS && currentTab != BottomNavTab.SAFETY && currentTab != BottomNavTab.BOOKINGS && currentTab != BottomNavTab.VERIFICATION && currentTab != BottomNavTab.PUROHIT_ADMIN && currentTab != BottomNavTab.VENDOR_PORTAL) {
                    OfflineSyncBanner(networkStatus = networkStatus)
                }

                when (currentTab) {
                    BottomNavTab.HOME, BottomNavTab.ITINERARY -> {
                    HomeContentView(
                        uiState = uiState,
                        userPhone = userPhone,
                        userProfile = userProfile,
                        viewModel = viewModel,
                        onOpenItinerary = { showItinerarySheet = true },
                        onNavigateToAiGuide = { currentTab = BottomNavTab.AI_GUIDE },
                        onNavigateToGhats = { currentTab = BottomNavTab.GHATS },
                        onNavigateToSafety = { currentTab = BottomNavTab.SAFETY },
                        onNavigateToVerification = { currentTab = BottomNavTab.VERIFICATION },
                        onSelectService = { serviceName ->
                            if (serviceName.contains("AI", ignoreCase = true) || serviceName.contains("Guide", ignoreCase = true)) {
                                currentTab = BottomNavTab.AI_GUIDE
                            } else if (serviceName.contains("Panda", ignoreCase = true) || serviceName.contains("Purohit", ignoreCase = true)) {
                                currentTab = BottomNavTab.PANDAS
                            } else if (serviceName.contains("Ghat", ignoreCase = true) || serviceName.contains("Boat", ignoreCase = true)) {
                                currentTab = BottomNavTab.GHATS
                            } else if (serviceName.contains("Safety", ignoreCase = true) || serviceName.contains("Emergency", ignoreCase = true) || serviceName.contains("Scam", ignoreCase = true)) {
                                currentTab = BottomNavTab.SAFETY
                            } else if (serviceName.contains("Verif", ignoreCase = true) || serviceName.contains("QR", ignoreCase = true)) {
                                currentTab = BottomNavTab.VERIFICATION
                            } else {
                                selectedServiceModal = serviceName
                            }
                        },
                        onViewOnMap = { place ->
                            focusedPlaceOnMapId = place.id
                            currentTab = BottomNavTab.MAP
                        },
                        onExpandFullMap = { currentTab = BottomNavTab.MAP }
                    )
                }
                BottomNavTab.GHATS -> {
                    GhatNavigationExplorerScreen(
                        onNavigateToMapWithGhat = { ghatId ->
                            focusedPlaceOnMapId = ghatId
                            currentTab = BottomNavTab.MAP
                        },
                        onBack = { currentTab = BottomNavTab.HOME },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.SAFETY -> {
                    PilgrimSafetyScreen(
                        onBack = { currentTab = BottomNavTab.HOME },
                        onNavigateToOfficialBoats = { currentTab = BottomNavTab.GHATS },
                        onNavigateToVerifiedPurohits = { currentTab = BottomNavTab.PANDAS },
                        onNavigateToVerification = { currentTab = BottomNavTab.VERIFICATION },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.AI_GUIDE -> {
                    AiGuideScreen(
                        onBack = { currentTab = BottomNavTab.HOME },
                        onNavigateToPurohits = { currentTab = BottomNavTab.PANDAS },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.MAP -> {
                    val places = uiState.allPlaces.ifEmpty { uiState.filteredPlaces }
                    PrayagrajHeritageMapView(
                        allPlaces = places,
                        selectedPlaceId = focusedPlaceOnMapId,
                        itineraryPlaces = uiState.itineraryCart,
                        onAddToItinerary = { viewModel.addToItinerary(it) },
                        onRemoveFromItinerary = { viewModel.removeFromItinerary(it) },
                        onSelectPlace = { viewModel.selectPlaceDetails(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.PANDAS -> {
                    PandaDirectoryScreen(
                        onBack = { currentTab = BottomNavTab.HOME },
                        onNavigateToAiGuide = { currentTab = BottomNavTab.AI_GUIDE },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.BOOKINGS -> {
                    SpiritualSchedulesScreen(
                        onBack = { currentTab = BottomNavTab.HOME },
                        onNavigateToPurohits = { currentTab = BottomNavTab.PANDAS },
                        onNavigateToVerification = { currentTab = BottomNavTab.VERIFICATION },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.VERIFICATION -> {
                    BookingVerificationScreen(
                        onBack = { currentTab = BottomNavTab.HOME },
                        onNavigateToVerifiedPurohits = { currentTab = BottomNavTab.PANDAS },
                        onNavigateToOfficialBoats = { currentTab = BottomNavTab.GHATS },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                BottomNavTab.SETTINGS -> {
                    SettingsOverviewScreen()
                }
                BottomNavTab.PUROHIT_ADMIN -> {
                    PurohitAdminDashboardScreen(
                        onBack = { currentTab = BottomNavTab.HOME }
                    )
                }
                BottomNavTab.VENDOR_PORTAL -> {
                    VendorAppNavigation(
                        onExitVendorPortal = { currentTab = BottomNavTab.HOME }
                    )
                }
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
            onNavigateToMap = onNavigateToMap,
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
                if (serviceTitle.contains("Panda", ignoreCase = true) || serviceTitle.contains("Purohit", ignoreCase = true)) {
                    currentTab = BottomNavTab.PANDAS
                } else if (serviceTitle.contains("Stay", ignoreCase = true)) {
                    viewModel.selectCategory("heritage")
                }
            }
        )
    }

    // Supabase Phone OTP Authentication Dialog
    if (showAuthDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAuthDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PolishCardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                PhoneAuthScreen(
                    onDismissOrBack = { showAuthDialog = false }
                )
            }
        }
    }

    // Edit Dialog Controller
    if (isEditDialogOpen) {
        EditProfileDialog(
            initialName = userProfile?.fullName.orEmpty(),
            initialEmail = userProfile?.email.orEmpty(),
            isSaving = isSaving,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { name, email ->
                viewModel.saveProfile(name, email)
            }
        )
    }
}

@Composable
fun ProfessionalAppBar(
    cartCount: Int,
    onOpenDrawer: () -> Unit = {},
    onProfileClick: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToAiGuide: () -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
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
            // Sacred ॐ Avatar (Opens Partner / Vendor Role Drawer)
            Surface(
                shape = CircleShape,
                color = PolishPrimaryContainer,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onOpenDrawer() }
                    .testTag("open_vendor_drawer_button")
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

            Column(
                modifier = Modifier.clickable { onOpenDrawer() }
            ) {
                Text(
                    text = "Prayagraj Sangam Yatra",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = PolishTextPrimary
                )
                Text(
                    text = "PHASE 1 ALPHA • SERVICES ▾",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PolishPrimary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Anti-Fraud / Booking Verification Action Button
            IconButton(
                onClick = onNavigateToVerification,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_app_bar_verification_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = PolishGreenBg,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verify Booking",
                            tint = PolishGreen,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // AI Guide Action Button
            IconButton(
                onClick = onNavigateToAiGuide,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_app_bar_ai_guide_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = PolishPrimaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✨", fontSize = 16.sp)
                    }
                }
            }

            // Interactive Map Navigation Action
            IconButton(
                onClick = onNavigateToMap,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_app_bar_map_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "View Map & Directions",
                    tint = PolishPrimary
                )
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

            // Sign Out Action Button
            IconButton(
                onClick = onSignOutClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = PolishTextPrimary
                )
            }
        }
    }
}

@Composable
fun HomeContentView(
    uiState: PlacesUiState,
    userPhone: String?,
    userProfile: UserProfile?,
    viewModel: PlacesViewModel,
    onOpenItinerary: () -> Unit,
    onNavigateToAiGuide: () -> Unit = {},
    onNavigateToGhats: () -> Unit = {},
    onNavigateToSafety: () -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    onSelectService: (String) -> Unit,
    onViewOnMap: (Place) -> Unit = {},
    onExpandFullMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. User Profile Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_banner_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ProfileAvatar(
                            imageUrl = userProfile?.profilePicUrl,
                            isUploading = isUploadingAvatar,
                            onImageSelected = { uri ->
                                viewModel.uploadProfilePicture(context, uri)
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = userProfile?.fullName ?: "Pilgrim Devotee",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (userProfile?.role != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PolishPrimary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = userProfile.role.uppercase(Locale.ROOT),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PolishPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = userProfile?.phoneNumber ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            userProfile?.email?.let { email ->
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Edit Profile Action Button
                    IconButton(
                        onClick = { viewModel.openEditDialog() },
                        modifier = Modifier.testTag("edit_name_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = PolishPrimary
                        )
                    }
                }
            }
        }

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

        // 2.1 Interactive Google Maps - Sacred Ghats & Cultural Heritage Sites
        item {
            val allPlaces = uiState.allPlaces.ifEmpty { uiState.filteredPlaces }
            HomeScreenGoogleMapSection(
                places = allPlaces,
                itineraryPlaces = uiState.itineraryCart,
                onSelectPlaceDetails = { viewModel.selectPlaceDetails(it) },
                onToggleItinerary = { place ->
                    if (uiState.itineraryCart.any { it.id == place.id }) {
                        viewModel.removeFromItinerary(place.id)
                    } else {
                        viewModel.addToItinerary(place)
                    }
                },
                onExpandFullMap = onExpandFullMap,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2.2 Live Ghat Navigation & Distances Hero Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToGhats() }
                    .border(1.dp, PolishBorderDarker, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = PolishCardSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Ocean50,
                        border = BorderStroke(1.dp, Ocean200),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛶", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ghat Navigation & Distances",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Emerald50,
                                border = BorderStroke(1.dp, Emerald200)
                            ) {
                                Text(
                                    text = "LIVE STATUS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Kila Ghat ↔ Saraswati Ghat (2.1km) • Visual crowd density levels & real-time boat routes.",
                            fontSize = 11.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = PolishPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("→", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                        }
                    }
                }
            }
        }

        // 2.5 AI Anti-Exploitation & Pilgrimage Guide Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToAiGuide() }
                    .border(1.dp, PolishBorderDarker, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = PolishCardSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PolishPrimaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✨", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Prayag AI Pilgrimage Guide",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PolishGreenBg
                            ) {
                                Text(
                                    text = "PROTECT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Fair Dakshina tariffs, anti-exploitation rules & verified Panda verification advice.",
                            fontSize = 11.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = PolishPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("→", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                        }
                    }
                }
            }
        }

        // 2.6 Pilgrim Safety & Anti-Scam Cell Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToSafety() }
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFEF2F2)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛡️", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pilgrim Safety & Anti-Scam Cell",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDC2626)
                            ) {
                                Text(
                                    text = "HELPLINE 1920",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Fraudulent Panda alerts, boat overcharging warnings, legal rate cards & 24x7 SOS contacts.",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("→", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        }
                    }
                }
            }
        }

        // 2.7 Official Booking & QR Verification Card (Anti-Fraud Registry)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToVerification() }
                    .border(1.dp, PolishGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = PolishGreenBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, PolishGreen),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = PolishGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Booking Verification Portal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PolishGreen
                            ) {
                                Text(
                                    text = "ANTI-FRAUD",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Enter booking ID or scan pass QR to cross-reference with official Govt registry & prevent fraud.",
                            fontSize = 11.sp,
                            color = PolishGreen.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("→", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PolishGreen)
                        }
                    }
                }
            }
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
                                    description = nearby.description ?: "",
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
                                    description = nearby.description ?: "",
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
                        onSelectPlace = { viewModel.selectPlaceDetails(place) },
                        onViewOnMap = { onViewOnMap(place) }
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
            ServiceItem(icon = "🛶", label = "Ghats & Boats", onClick = { onSelectService("Ghat Navigation & Boat Booking") })
            ServiceItem(icon = "🕉️", label = "Verified Pandas", onClick = { onSelectService("Verified Pandas & Purohits") })
            ServiceItem(icon = "🛺", label = "E-Rickshaw", onClick = { onSelectService("Electric Rickshaw & Rides") })
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
        Triple("all", "All Places", "🏛️"),
        Triple("heritage", "Heritage", "🏰"),
        Triple("temple", "Temples", "🛕"),
        Triple("riverside", "Riverside", "🌊"),
        Triple("ghat", "Ghats", "🛶")
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
                    text = if (isNearbyActive) "GPS Radar ON" else "Radar (PostGIS)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNearbyActive) Color.White else PolishTeal
                )
            }
        }

        categories.forEach { (key, label, icon) ->
            val isSelected = selectedCategory.equals(key, ignoreCase = true) ||
                (key == "riverside" && selectedCategory.equals("ghat", ignoreCase = true))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) PolishPrimary else PolishCardSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) PolishPrimary else PolishBorder
                ),
                modifier = Modifier
                    .clickable { onSelectCategory(key) }
                    .testTag("category_chip_$key")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = icon, fontSize = 12.sp)
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else PolishTextPrimary
                    )
                }
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
    onSelectPlace: () -> Unit,
    onViewOnMap: () -> Unit = {}
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PolishSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderDarker),
                            modifier = Modifier
                                .clickable { onViewOnMap() }
                                .testTag("view_on_map_${place.id}")
                        ) {
                            Text(
                                text = "🗺️ Map",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PolishPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

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
                .padding(horizontal = 4.dp),
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
                icon = "🛶",
                label = "Ghats",
                isSelected = currentTab == BottomNavTab.GHATS,
                onClick = { onTabSelected(BottomNavTab.GHATS) }
            )
            BottomNavItem(
                icon = "🕉️",
                label = "Purohits",
                isSelected = currentTab == BottomNavTab.PANDAS,
                onClick = { onTabSelected(BottomNavTab.PANDAS) }
            )
            BottomNavItem(
                icon = "🛡️",
                label = "Safety",
                isSelected = currentTab == BottomNavTab.SAFETY,
                onClick = { onTabSelected(BottomNavTab.SAFETY) }
            )
            BottomNavItem(
                icon = "✨",
                label = "AI Guide",
                isSelected = currentTab == BottomNavTab.AI_GUIDE,
                onClick = { onTabSelected(BottomNavTab.AI_GUIDE) }
            )
            BottomNavItem(
                icon = "🗺️",
                label = "Map",
                isSelected = currentTab == BottomNavTab.MAP,
                onClick = { onTabSelected(BottomNavTab.MAP) }
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
                icon = "🛡️",
                label = "Verify",
                isSelected = currentTab == BottomNavTab.VERIFICATION,
                onClick = { onTabSelected(BottomNavTab.VERIFICATION) }
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
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) PolishPrimaryContainer else Color.Transparent,
            modifier = Modifier.size(width = 46.dp, height = 28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (badgeCount > 0 && !isSelected) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = PolishPrimary,
                                contentColor = Color.White
                            ) {
                                Text(badgeCount.toString(), fontSize = 8.sp)
                            }
                        }
                    ) {
                        Text(text = icon, fontSize = 15.sp)
                    }
                } else {
                    Text(text = icon, fontSize = 15.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
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
    var supabaseUrl by remember { mutableStateOf(SupabaseProvider.supabaseUrl) }
    var supabaseAnonKey by remember { mutableStateOf(SupabaseProvider.supabaseAnonKey) }
    var saveStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Yatra Settings & Cloud Config",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary
        )

        // Supabase Connection Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PolishCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Supabase Backend Connection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                    Surface(shape = RoundedCornerShape(6.dp), color = PolishGreenBg) {
                        Text("PostGIS Ready", fontSize = 10.sp, color = PolishGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Text(
                    text = "Provide your Supabase project credentials to synchronize live with PostgreSQL and PostGIS RPC:",
                    fontSize = 11.sp,
                    color = PolishTextSecondary
                )

                OutlinedTextField(
                    value = supabaseUrl,
                    onValueChange = { supabaseUrl = it },
                    label = { Text("Supabase Project URL", fontSize = 12.sp) },
                    placeholder = { Text("https://xyzcompany.supabase.co", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = supabaseAnonKey,
                    onValueChange = { supabaseAnonKey = it },
                    label = { Text("Supabase Anon Public Key", fontSize = 12.sp) },
                    placeholder = { Text("eyJhbGciOiJIUzI1Ni...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        SupabaseProvider.updateConfig(supabaseUrl.trim(), supabaseAnonKey.trim())
                        saveStatus = "Supabase configuration updated successfully!"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Text("Save Connection Config")
                }

                saveStatus?.let {
                    Text(text = it, fontSize = 11.sp, color = PolishGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Supabase Phone OTP Authentication Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PolishCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Supabase Phone OTP Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimary
                )
                Text(
                    text = "Sign in using your 10-digit mobile number to access verified bookings, customized cart sync, and priest consultations.",
                    fontSize = 11.sp,
                    color = PolishTextSecondary
                )
                PhoneAuthScreen()
            }
        }

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
                    Text("App Version", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Phase 1 (Prayagraj Sangam Yatra)", fontSize = 12.sp, color = PolishTextSecondary, fontWeight = FontWeight.Bold)
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
    if (serviceName.contains("Boat", ignoreCase = true) || serviceName.contains("Ghat", ignoreCase = true)) {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PolishSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Official Boat Booking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PolishTextTertiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    BoatBookingTierComponent()
                }
            }
        }
    } else {
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
    onNavigateToMap: () -> Unit = {},
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
                        .heightIn(max = 280.dp),
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

                Spacer(modifier = Modifier.height(12.dp))

                // View Route on Map Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onNavigateToMap()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).padding(end = 6.dp)
                    )
                    Text("View Route On Interactive Map")
                }

                Spacer(modifier = Modifier.height(8.dp))

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

@Composable
fun ProfileAvatar(
    imageUrl: String?,
    isUploading: Boolean,
    onImageSelected: (Uri) -> Unit
) {
    // Android Photo Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(enabled = !isUploading) {
                imagePickerLauncher.launch("image/*")
            }
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Avatar",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Camera Icon Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change Picture",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Uploading Loader Overlay
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 3.dp
            )
        }
    }
}


// End of PlacesScreen

