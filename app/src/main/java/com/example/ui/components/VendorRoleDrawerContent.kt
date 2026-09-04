package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class VendorCategoryItem(
    val title: String,
    val roleKey: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun VendorRoleDrawerContent(
    onSelectRoleLogin: (roleKey: String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val vendorCategories = listOf(
        VendorCategoryItem("Tirth Purohit Portal", "PUROHIT", Icons.Default.SelfImprovement, "Ritual & Puja Management"),
        VendorCategoryItem("Hotel & Dharamshala", "HOTEL_OWNER", Icons.Default.Hotel, "Room Listings & Tariff"),
        VendorCategoryItem("Vehicle & Boat Owner", "VEHICLE_OWNER", Icons.Default.DirectionsBoat, "Auto, Boat & Taxi Fare Cards"),
        VendorCategoryItem("Tour & Pilgrimage Guide", "TOUR_GUIDE", Icons.Default.Explore, "Guided Tour Packages"),
        VendorCategoryItem("Restaurant & Bhojanalaya", "RESTAURANT_OWNER", Icons.Default.Restaurant, "Pure Veg Dining & Mess")
    )

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Prayagraj Service Portals",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Partner & Vendor Sign-In",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            vendorCategories.forEach { category ->
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text(category.title, style = MaterialTheme.typography.titleMedium)
                            Text(category.description, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    icon = { Icon(category.icon, contentDescription = null) },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onSelectRoleLogin(category.roleKey)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
