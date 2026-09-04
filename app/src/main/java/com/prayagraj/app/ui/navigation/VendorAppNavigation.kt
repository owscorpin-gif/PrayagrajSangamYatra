package com.prayagraj.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prayagraj.app.data.auth.VendorAuthState
import com.prayagraj.app.data.auth.VendorType
import com.prayagraj.app.data.model.*
import com.prayagraj.app.data.repository.HotelRepository
import com.prayagraj.app.data.repository.TourRepository
import com.prayagraj.app.data.repository.TransportRepository
import com.prayagraj.app.ui.screens.admin.HotelAdminDashboardScreen
import com.prayagraj.app.ui.screens.admin.PurohitAdminDashboardScreen
import com.prayagraj.app.ui.screens.admin.TourGuideAdminDashboardScreen
import com.prayagraj.app.ui.screens.admin.TransportAdminDashboardScreen
import com.prayagraj.app.ui.screens.onboarding.VendorOnboardingScreen
import com.prayagraj.app.ui.viewmodel.VendorAuthViewModel
import kotlinx.coroutines.launch

@Composable
fun VendorAppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: VendorAuthViewModel = viewModel(),
    onExitVendorPortal: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Repositories for state binding
    val tourRepo = remember { TourRepository() }
    val transportRepo = remember { TransportRepository() }
    val hotelRepo = remember { HotelRepository() }

    when (val state = authState) {
        is VendorAuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is VendorAuthState.Unauthenticated -> {
            VendorOnboardingScreen(
                onOnboardingComplete = { vendorType ->
                    val destination = when (vendorType) {
                        VendorType.PUROHIT -> VendorScreen.PurohitDashboard.route
                        VendorType.ACCOMMODATION -> VendorScreen.AccommodationDashboard.route
                        VendorType.TRANSPORT -> VendorScreen.TransportDashboard.route
                        VendorType.TOUR_GUIDE -> VendorScreen.TourGuideDashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(VendorScreen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        is VendorAuthState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        is VendorAuthState.Authenticated -> {
            val startDest = remember(state.vendorType) {
                when (state.vendorType) {
                    VendorType.PUROHIT -> VendorScreen.PurohitDashboard.route
                    VendorType.ACCOMMODATION -> VendorScreen.AccommodationDashboard.route
                    VendorType.TRANSPORT -> VendorScreen.TransportDashboard.route
                    VendorType.TOUR_GUIDE -> VendorScreen.TourGuideDashboard.route
                }
            }

            NavHost(
                navController = navController,
                startDestination = startDest
            ) {
                // 1. Onboarding fallback route
                composable(VendorScreen.Onboarding.route) {
                    VendorOnboardingScreen(
                        onOnboardingComplete = { type ->
                            val dest = when (type) {
                                VendorType.PUROHIT -> VendorScreen.PurohitDashboard.route
                                VendorType.ACCOMMODATION -> VendorScreen.AccommodationDashboard.route
                                VendorType.TRANSPORT -> VendorScreen.TransportDashboard.route
                                VendorType.TOUR_GUIDE -> VendorScreen.TourGuideDashboard.route
                            }
                            navController.navigate(dest)
                        }
                    )
                }

                // 2. Purohit / Pandit Ji Dashboard
                composable(VendorScreen.PurohitDashboard.route) {
                    var services by remember {
                        mutableStateOf(
                            listOf(
                                PurohitService(
                                    ritualName = "Triveni Sangam Maha Snan & Sankalpa",
                                    description = "Sacred snan ceremony with Vedic mantras and Ganga aashirwad",
                                    fixedDakshina = 501.0,
                                    durationMinutes = 45,
                                    materialsIncluded = true
                                ),
                                PurohitService(
                                    ritualName = "Pitru Shanti & Tarpan Puja",
                                    description = "Ancestral liberation tarpan performed on holy sandbank",
                                    fixedDakshina = 1100.0,
                                    durationMinutes = 60,
                                    materialsIncluded = true
                                )
                            )
                        )
                    }

                    PurohitAdminDashboardScreen(
                        purohitName = state.profile.businessName.ifBlank { state.profile.fullName.ifBlank { "Acharya Shastri Ji" } },
                        registrationId = state.profile.licenseId ?: "UPT-PUR-2025-014",
                        services = services,
                        onAddService = { newService ->
                            services = services + newService
                        },
                        onEditService = { updated ->
                            services = services.map { if (it.id == updated.id) updated else it }
                        },
                        onDeleteService = { id ->
                            services = services.filterNot { it.id == id }
                        },
                        onBack = onExitVendorPortal
                    )
                }

                // 3. Accommodation / Hotel / Dharamshala Dashboard
                composable(VendorScreen.AccommodationDashboard.route) {
                    var property by remember {
                        mutableStateOf(
                            AccommodationDto(
                                id = "prop-01",
                                ownerId = state.userId,
                                propertyName = state.profile.businessName.ifBlank { "Triveni Sangam Grand Heritage Dharamshala" },
                                propertyType = "Dharamshala",
                                address = "Sector 4, Triveni Marg, Mela Area, Prayagraj",
                                registrationLicenseId = state.profile.licenseId ?: "UPT-HOT-2025-442",
                                contactNumber = state.profile.contactPhone.ifBlank { "+91 94150 99881" },
                                isVerified = state.profile.isVerified
                            )
                        )
                    }

                    var rooms by remember {
                        mutableStateOf(
                            listOf(
                                RoomInventoryDto(
                                    id = "room-01",
                                    propertyId = "prop-01",
                                    roomCategory = "Deluxe Pilgrim Suite (AC)",
                                    totalRooms = 30,
                                    availableRooms = 8,
                                    baseTariff = 2200.0,
                                    peakMelaTariff = 4500.0,
                                    amenities = listOf("Attached Bath", "AC", "Ganga Jal Supply", "Clean Linen")
                                ),
                                RoomInventoryDto(
                                    id = "room-02",
                                    propertyId = "prop-01",
                                    roomCategory = "Standard Twin Non-AC",
                                    totalRooms = 40,
                                    availableRooms = 14,
                                    baseTariff = 1100.0,
                                    peakMelaTariff = 2200.0,
                                    amenities = listOf("Hot Water", "Fan", "Daily Cleaning")
                                ),
                                RoomInventoryDto(
                                    id = "room-03",
                                    propertyId = "prop-01",
                                    roomCategory = "Community Yatri Dormitory",
                                    totalRooms = 22,
                                    availableRooms = 5,
                                    baseTariff = 400.0,
                                    peakMelaTariff = 850.0,
                                    amenities = listOf("Locker", "Common Bath", "Mattress", "Filtered Water")
                                )
                            )
                        )
                    }

                    HotelAdminDashboardScreen(
                        property = property,
                        rooms = rooms,
                        onAddRoom = { newRoom ->
                            rooms = rooms + newRoom
                        },
                        onBack = onExitVendorPortal
                    )
                }

                // 4. Transport & Boat Admin Dashboard
                composable(VendorScreen.TransportDashboard.route) {
                    var vehicles by remember {
                        mutableStateOf(
                            listOf(
                                FleetVehicleDto(
                                    id = "veh-01",
                                    ownerId = state.userId,
                                    title = "Sangam Ganga Mayya Nav (Boat #14)",
                                    vehicleType = "MOTOR_BOAT",
                                    registrationNumber = "UP-70-MB-2025-014",
                                    capacity = 12,
                                    isVerified = true
                                ),
                                FleetVehicleDto(
                                    id = "veh-02",
                                    ownerId = state.userId,
                                    title = "Pawan Putra E-Rickshaw Shuttle",
                                    vehicleType = "E_RICKSHAW",
                                    registrationNumber = "UP-70-ER-2025-108",
                                    capacity = 4,
                                    isVerified = true
                                )
                            )
                        )
                    }

                    var fares by remember {
                        mutableStateOf(
                            listOf(
                                RouteFareDto(
                                    id = "fare-01",
                                    vehicleId = "veh-01",
                                    origin = "Kila Ghat",
                                    destination = "Sangam Confluence Point",
                                    standardFare = 150.0,
                                    govCappedMaxFare = 200.0,
                                    isSharedService = true
                                ),
                                RouteFareDto(
                                    id = "fare-02",
                                    vehicleId = "veh-01",
                                    origin = "Saraswati Ghat",
                                    destination = "Sangam Confluence Point",
                                    standardFare = 180.0,
                                    govCappedMaxFare = 220.0,
                                    isSharedService = true
                                ),
                                RouteFareDto(
                                    id = "fare-03",
                                    vehicleId = "veh-01",
                                    origin = "VIP Ghat",
                                    destination = "Arail Ghat",
                                    standardFare = 120.0,
                                    govCappedMaxFare = 150.0,
                                    isSharedService = true
                                )
                            )
                        )
                    }

                    TransportAdminDashboardScreen(
                        vehicles = vehicles,
                        selectedVehicleFares = fares,
                        onAddRouteFare = { newFare ->
                            fares = fares + newFare
                        },
                        onBack = onExitVendorPortal
                    )
                }

                // 5. Tour & Heritage Guide Admin Dashboard
                composable(VendorScreen.TourGuideDashboard.route) {
                    var guideProfile by remember {
                        mutableStateOf<TourGuideProfileDto?>(
                            TourGuideProfileDto(
                                id = "guide-01",
                                guideId = state.userId,
                                licenseNumber = state.profile.licenseId ?: "UPT-PRY-2025-0784",
                                experienceYears = 8,
                                languagesSpoken = listOf("Hindi", "English", "Sanskrit", "Bengali"),
                                badgeLevel = "UP Tourism Gold Approved",
                                isVerified = true
                            )
                        )
                    }

                    var tourPackages by remember {
                        mutableStateOf(
                            listOf(
                                TourPackageDto(
                                    id = "pkg-01",
                                    guideProfileId = "guide-01",
                                    packageTitle = "Triveni Sangam Sacred Sunrise & Snan Parikrama",
                                    durationHours = 3,
                                    maxGroupSize = 10,
                                    pricePerPerson = 650.0,
                                    includedServices = listOf("Boat Transfer Assistance", "Sankalpa Puja Priest Coordination", "Historical Narration", "Life Jackets"),
                                    itineraryHighlights = "Kila Ghat assembly -> Confluence Boat Cruise -> Snan Ritual Guidance -> Akshayavat Darshan",
                                    isActive = true
                                ),
                                TourPackageDto(
                                    id = "pkg-02",
                                    guideProfileId = "guide-01",
                                    packageTitle = "Akharas Heritage & Naga Sadhus Cultural Trail",
                                    durationHours = 4,
                                    maxGroupSize = 8,
                                    pricePerPerson = 1100.0,
                                    includedServices = listOf("Akhara Entry Facilitation", "Spiritual Discourse Translation", "Prasadam"),
                                    itineraryHighlights = "Juna Akhara Sector -> Niranjani Akhara Camp -> Evening Ganga Aarti at VIP Ghat",
                                    isActive = true
                                )
                            )
                        )
                    }

                    TourGuideAdminDashboardScreen(
                        guideProfile = guideProfile,
                        packages = tourPackages,
                        onAddPackage = { newPkg ->
                            tourPackages = tourPackages + newPkg
                            coroutineScope.launch {
                                tourRepo.addTourPackage(newPkg)
                            }
                        },
                        onToggleActive = { pkgId, currentStatus ->
                            val updated = !currentStatus
                            tourPackages = tourPackages.map {
                                if (it.id == pkgId) it.copy(isActive = updated) else it
                            }
                            coroutineScope.launch {
                                tourRepo.togglePackageStatus(pkgId, updated)
                            }
                        },
                        onBack = onExitVendorPortal
                    )
                }
            }
        }
    }
}
