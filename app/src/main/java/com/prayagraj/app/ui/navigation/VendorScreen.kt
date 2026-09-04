package com.prayagraj.app.ui.navigation

sealed class VendorScreen(val route: String) {
    data object Onboarding : VendorScreen("vendor_onboarding")
    data object PurohitDashboard : VendorScreen("purohit_dashboard")
    data object AccommodationDashboard : VendorScreen("accommodation_dashboard")
    data object TransportDashboard : VendorScreen("transport_dashboard")
    data object TourGuideDashboard : VendorScreen("tour_guide_dashboard")
}
