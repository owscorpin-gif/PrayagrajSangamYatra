package com.prayagraj.app.data.repository

import com.prayagraj.app.data.model.AccommodationDto
import com.prayagraj.app.data.model.RoomInventoryDto
import com.prayagraj.app.data.remote.SupabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccommodationRepository {

    private val postgrest = SupabaseProvider.postgrest
    private val auth = SupabaseProvider.auth

    suspend fun getPropertyByOwner(): Result<AccommodationDto?> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User unauthenticated")

            postgrest["accommodations"]
                .select { filter { eq("owner_id", userId) } }
                .decodeSingleOrNull<AccommodationDto>()
        }
    }

    suspend fun saveAccommodationProperty(
        propertyName: String,
        propertyType: String,
        address: String,
        contactNumber: String,
        licenseId: String?,
        imageUrl: String? = null
    ): Result<AccommodationDto> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User unauthenticated")

            val dto = AccommodationDto(
                ownerId = userId,
                propertyName = propertyName,
                propertyType = propertyType,
                address = address,
                contactNumber = contactNumber,
                registrationLicenseId = licenseId?.ifEmpty { null },
                imageUrl = imageUrl
            )

            postgrest["accommodations"]
                .insert(dto) { select() }
                .decodeSingle<AccommodationDto>()
        }
    }

    suspend fun getRoomsForProperty(propertyId: String): Result<List<RoomInventoryDto>> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest["room_inventories"]
                .select { filter { eq("property_id", propertyId) } }
                .decodeList<RoomInventoryDto>()
        }
    }

    suspend fun addRoomCategory(
        propertyId: String,
        categoryName: String,
        totalRooms: Int,
        baseTariff: Double,
        peakTariff: Double,
        amenities: List<String>
    ): Result<RoomInventoryDto> = withContext(Dispatchers.IO) {
        runCatching {
            val roomDto = RoomInventoryDto(
                propertyId = propertyId,
                roomCategory = categoryName,
                totalRooms = totalRooms,
                availableRooms = totalRooms,
                baseTariff = baseTariff,
                peakMelaTariff = peakTariff,
                amenities = amenities
            )

            postgrest["room_inventories"]
                .insert(roomDto) { select() }
                .decodeSingle<RoomInventoryDto>()
        }
    }
}
