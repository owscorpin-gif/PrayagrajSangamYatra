package com.prayagraj.app.ui.screens.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.prayagraj.app.data.auth.VendorType

data class VendorRoleOption(
    val type: VendorType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val vendorRoles = listOf(
    VendorRoleOption(
        type = VendorType.PUROHIT,
        title = "Purohit / Pandit Ji",
        subtitle = "Pooja, Snan Rituals & Samagri Services",
        icon = Icons.Default.SelfImprovement
    ),
    VendorRoleOption(
        type = VendorType.ACCOMMODATION,
        title = "Accommodation Owner",
        subtitle = "Hotels, Dharamshalas, Tents & Homestays",
        icon = Icons.Default.Hotel
    ),
    VendorRoleOption(
        type = VendorType.TRANSPORT,
        title = "Boat & Vehicle Operator",
        subtitle = "Motorboats, Hand-row Boats, Taxis & E-Rickshaws",
        icon = Icons.Default.DirectionsBoat
    ),
    VendorRoleOption(
        type = VendorType.TOUR_GUIDE,
        title = "Tour & Heritage Guide",
        subtitle = "Local Sightseeing & Pilgrimage Circuit Packages",
        icon = Icons.Default.Explore
    )
)
