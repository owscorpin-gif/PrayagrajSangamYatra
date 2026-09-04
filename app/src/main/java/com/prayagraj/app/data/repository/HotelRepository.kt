package com.prayagraj.app.data.repository

import com.example.data.remote.SupabaseProvider
import com.prayagraj.app.data.model.AccommodationDto
import com.prayagraj.app.data.model.RoomInventoryDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class HotelRepository {
    private val client = SupabaseProvider.client

    private val fallbackAccommodation = AccommodationDto(
        id = "acc-sangam-01",
        ownerId = "hotelier-demo-01",
        propertyName = "Sri Prayag Teerth Dharamshala & Yatri Niwas",
        propertyType = "Dharamshala",
        address = "Near Daraganj Ghat, Sangam Marg, Prayagraj, UP 211006",
        registrationLicenseId = "UP-TOU-PRY-2024-551",
        contactNumber = "+91 98390 12345",
        isVerified = true
    )

    private val localRoomInventories = mutableListOf(
        RoomInventoryDto(
            id = "room-cat-01",
            propertyId = "acc-sangam-01",
            roomCategory = "Standard Non-AC Yatri Room",
            totalRooms = 24,
            availableRooms = 18,
            baseTariff = 650.0,
            peakMelaTariff = 1200.0,
            amenities = listOf("Clean Linen", "24/7 Water", "Attached Bathroom", "Ceiling Fan")
        ),
        RoomInventoryDto(
            id = "room-cat-02",
            propertyId = "acc-sangam-01",
            roomCategory = "Deluxe AC Family Room (4 Bed)",
            totalRooms = 12,
            availableRooms = 5,
            baseTariff = 1800.0,
            peakMelaTariff = 3500.0,
            amenities = listOf("Clean Linen", "Hot Water Geyser", "Air Conditioning", "Wi-Fi", "RO Drinking Water")
        ),
        RoomInventoryDto(
            id = "room-cat-03",
            propertyId = "acc-sangam-01",
            roomCategory = "Sangam View Super Deluxe Suite",
            totalRooms = 6,
            availableRooms = 2,
            baseTariff = 2800.0,
            peakMelaTariff = 5500.0,
            amenities = listOf("Panoramic Sangam View", "Clean Linen", "Air Conditioning", "Private Balcony", "Pooja Thali Kit")
        ),
        RoomInventoryDto(
            id = "room-cat-04",
            propertyId = "acc-sangam-01",
            roomCategory = "Community Pilgrim Hall (Dormitory Bed)",
            totalRooms = 50,
            availableRooms = 32,
            baseTariff = 250.0,
            peakMelaTariff = 500.0,
            amenities = listOf("Single Bed & Blanket", "Locker Facility", "Shared Bathrooms", "Safe Luggage Counter")
        )
    )

    fun getCurrentUserId(): String? = try {
        client.auth.currentUserOrNull()?.id ?: "hotelier-demo-01"
    } catch (e: Exception) {
        "hotelier-demo-01"
    }

    suspend fun getPropertyByOwner(ownerId: String): AccommodationDto? = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext fallbackAccommodation.copy(ownerId = ownerId)
        }
        try {
            client.from("accommodations")
                .select { filter { eq("owner_id", ownerId) } }
                .decodeSingleOrNull<AccommodationDto>() ?: fallbackAccommodation.copy(ownerId = ownerId)
        } catch (e: Exception) {
            fallbackAccommodation.copy(ownerId = ownerId)
        }
    }

    suspend fun getRoomsForProperty(propertyId: String): List<RoomInventoryDto> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext localRoomInventories.filter { it.propertyId == propertyId || propertyId == "acc-sangam-01" }.toList()
        }
        try {
            val remoteRooms = client.from("room_inventories")
                .select { filter { eq("property_id", propertyId) } }
                .decodeList<RoomInventoryDto>()
            if (remoteRooms.isNotEmpty()) remoteRooms else localRoomInventories.toList()
        } catch (e: Exception) {
            localRoomInventories.toList()
        }
    }

    suspend fun addRoomCategory(room: RoomInventoryDto) = withContext(Dispatchers.IO) {
        val completeRoom = if (room.id.isNullOrBlank()) {
            room.copy(id = UUID.randomUUID().toString())
        } else {
            room
        }
        localRoomInventories.add(completeRoom)

        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("room_inventories").insert(completeRoom)
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }

    suspend fun updateRoomTariff(roomId: String, baseTariff: Double, peakTariff: Double) = withContext(Dispatchers.IO) {
        val index = localRoomInventories.indexOfFirst { it.id == roomId }
        if (index != -1) {
            localRoomInventories[index] = localRoomInventories[index].copy(
                baseTariff = baseTariff,
                peakMelaTariff = peakTariff
            )
        }

        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("room_inventories").update({
                    set("base_tariff", baseTariff)
                    set("peak_mela_tariff", peakTariff)
                }) {
                    filter { eq("id", roomId) }
                }
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }
}
