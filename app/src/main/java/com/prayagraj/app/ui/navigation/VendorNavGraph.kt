package com.prayagraj.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prayagraj.app.data.auth.VendorAuthState
import com.prayagraj.app.data.auth.VendorType
import com.prayagraj.app.ui.screens.admin.AccommodationAdminDashboardScreen
import com.prayagraj.app.ui.screens.admin.PurohitAdminDashboardScreen
import com.prayagraj.app.ui.screens.admin.TourGuideAdminDashboardScreen
import com.prayagraj.app.ui.screens.admin.TransportAdminDashboardScreen
import com.prayagraj.app.ui.screens.onboarding.VendorOnboardingScreen
import com.prayagraj.app.ui.viewmodel.VendorAuthViewModel

@Composable
fun VendorNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: VendorAuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is VendorAuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is VendorAuthState.Unauthenticated -> {
            // Render Onboarding or Login Entry Screen
            VendorOnboardingScreen(
                onOnboardingComplete = { vendorType ->
                    val route = getRouteForVendorType(vendorType)
                    navController.navigate(route) {
                        popUpTo(VendorScreen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        is VendorAuthState.Authenticated -> {
            val startDestination = getRouteForVendorType(state.vendorType)

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(VendorScreen.Onboarding.route) {
                    VendorOnboardingScreen(
                        onOnboardingComplete = { vendorType ->
                            val route = getRouteForVendorType(vendorType)
                            navController.navigate(route) {
                                popUpTo(VendorScreen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(VendorScreen.PurohitDashboard.route) {
                    PurohitAdminDashboardScreen(
                        onLogout = { authViewModel.logout() }
                    )
                }

                composable(VendorScreen.AccommodationDashboard.route) {
                    AccommodationAdminDashboardScreen(
                        onBack = { authViewModel.logout() }
                    )
                }

                composable(VendorScreen.TransportDashboard.route) {
                    TransportAdminDashboardScreen(
                        vehicles = emptyList(),
                        selectedVehicleFares = emptyList(),
                        onAddRouteFare = {},
                        onBack = { authViewModel.logout() }
                    )
                }

                composable(VendorScreen.TourGuideDashboard.route) {
                    TourGuideAdminDashboardScreen(
                        guideProfile = null,
                        packages = emptyList(),
                        onAddPackage = {},
                        onToggleActive = { _, _ -> },
                        onBack = { authViewModel.logout() }
                    )
                }
            }
        }

        is VendorAuthState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.message)
            }
        }
    }
}

/**
 * Maps VendorType enum to corresponding Screen route.
 */
private fun getRouteForVendorType(vendorType: VendorType): String {
    return when (vendorType) {
        VendorType.PUROHIT -> VendorScreen.PurohitDashboard.route
        VendorType.ACCOMMODATION -> VendorScreen.AccommodationDashboard.route
        VendorType.TRANSPORT -> VendorScreen.TransportDashboard.route
        VendorType.TOUR_GUIDE -> VendorScreen.TourGuideDashboard.route
    }
}
