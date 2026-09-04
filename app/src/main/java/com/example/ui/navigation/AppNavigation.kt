package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BookingVerificationScreen
import com.example.ui.screens.ItineraryMapScreen
import com.example.ui.screens.PandaDirectoryScreen
import com.example.ui.screens.PhoneAuthScreen
import com.example.ui.screens.PlacesScreen
import com.example.ui.screens.PrayagrajMapScreen
import com.example.ui.viewmodel.PlacesViewModel

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Places : Screen("places")
    data object PlacesMap : Screen("places_map")
    data object ItineraryMap : Screen("itinerary_map")
    data object PandaDirectory : Screen("panda_directory")
    data object BookingVerification : Screen("booking_verification")
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Places.route,
    placesViewModel: PlacesViewModel? = null
) {
    val navController = rememberNavController()
    val vm: PlacesViewModel = placesViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by vm.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Auth Screen Route
        composable(Screen.Auth.route) {
            PhoneAuthScreen(
                onLoginSuccess = {
                    // Navigate to Places screen and clear Auth screen from the backstack
                    navController.navigate(Screen.Places.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Places Directory Route
        composable(Screen.Places.route) {
            PlacesScreen(
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Places.route) { inclusive = true }
                    }
                },
                onNavigateToMap = {
                    navController.navigate(Screen.PlacesMap.route)
                },
                onNavigateToPandaDirectory = {
                    navController.navigate(Screen.PandaDirectory.route)
                },
                onNavigateToBookingVerification = {
                    navController.navigate(Screen.BookingVerification.route)
                },
                viewModel = vm
            )
        }

        // 3. Prayagraj Heritage & Spiritual Map Route
        composable(Screen.PlacesMap.route) {
            val places = uiState.allPlaces.ifEmpty { uiState.filteredPlaces }
            PrayagrajMapScreen(
                allPlaces = places,
                onBack = {
                    navController.popBackStack()
                },
                onSelectPlaceDetails = { place ->
                    vm.selectPlaceDetails(place)
                }
            )
        }

        // 4. Itinerary Route Planner Map Route
        composable(Screen.ItineraryMap.route) {
            ItineraryMapScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Verified Pandas & Purohits Directory Route
        composable(Screen.PandaDirectory.route) {
            PandaDirectoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 6. Booking Verification & Anti-Fraud Registry Route
        composable(Screen.BookingVerification.route) {
            BookingVerificationScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToVerifiedPurohits = {
                    navController.navigate(Screen.PandaDirectory.route)
                },
                onNavigateToOfficialBoats = {
                    navController.popBackStack()
                }
            )
        }
    }
}

