package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VendorProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String,
    val role: String = "PUROHIT",
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("registration_id") val registrationId: String = ""
)

@Serializable
data class PurohitServiceDto(
    val id: String? = null,
    @SerialName("purohit_id") val purohitId: String,
    @SerialName("ritual_name") val ritualName: String,
    val description: String,
    @SerialName("fixed_dakshina") val fixedDakshina: Double,
    @SerialName("duration_minutes") val durationMinutes: Int = 45,
    @SerialName("materials_included") val materialsIncluded: Boolean = true
)

typealias AccommodationDto = com.prayagraj.app.data.model.AccommodationDto
typealias RoomInventoryDto = com.prayagraj.app.data.model.RoomInventoryDto
typealias FleetVehicleDto = com.prayagraj.app.data.model.FleetVehicleDto
typealias RouteFareDto = com.prayagraj.app.data.model.RouteFareDto

