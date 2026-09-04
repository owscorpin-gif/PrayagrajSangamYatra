package com.prayagraj.app.ui.screens.admin

import androidx.compose.runtime.Composable
import com.prayagraj.app.data.model.AccommodationDto
import com.prayagraj.app.data.model.RoomInventoryDto

@Composable
fun AccommodationAdminDashboardScreen(
    property: AccommodationDto? = null,
    rooms: List<RoomInventoryDto> = emptyList(),
    onAddRoom: (RoomInventoryDto) -> Unit = {},
    onBack: () -> Unit = {}
) {
    HotelAdminDashboardScreen(
        property = property,
        rooms = rooms,
        onAddRoom = onAddRoom,
        onBack = onBack
    )
}
