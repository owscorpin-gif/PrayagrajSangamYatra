package com.prayagraj.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VendorProfileDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("business_name") val businessName: String = "",
    @SerialName("agency_or_business_name") val agencyOrBusinessName: String? = null,
    @SerialName("phone_number") val phoneNumber: String = "",
    @SerialName("vendor_type") val vendorType: String,
    @SerialName("license_id") val licenseId: String? = null,
    @SerialName("contact_phone") val contactPhone: String = "",
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AccommodationDto(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("property_name") val propertyName: String,
    @SerialName("property_type") val propertyType: String, // Dharamshala / Hotel
    val address: String,
    @SerialName("registration_license_id") val registrationLicenseId: String = "",
    @SerialName("contact_number") val contactNumber: String,
    @SerialName("is_verified") val isVerified: Boolean = false
)

@Serializable
data class RoomInventoryDto(
    val id: String? = null,
    @SerialName("property_id") val propertyId: String,
    @SerialName("room_category") val roomCategory: String,
    @SerialName("total_rooms") val totalRooms: Int,
    @SerialName("available_rooms") val availableRooms: Int,
    @SerialName("base_tariff") val baseTariff: Double,
    @SerialName("peak_mela_tariff") val peakMelaTariff: Double,
    val amenities: List<String> = listOf("Clean Linen", "Water")
)

@Serializable
data class PurohitService(
    val id: String = java.util.UUID.randomUUID().toString(),
    val purohitId: String = "",
    val ritualName: String,
    val description: String = "",
    val fixedDakshina: Double,
    val durationMinutes: Int = 45,
    val materialsIncluded: Boolean = true
)

@Serializable
data class FleetVehicleDto(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    @SerialName("vehicle_type") val vehicleType: String, // MOTOR_BOAT, E_RICKSHAW, etc.
    @SerialName("registration_number") val registrationNumber: String,
    val capacity: Int,
    @SerialName("is_verified") val isVerified: Boolean = false
)

@Serializable
data class RouteFareDto(
    val id: String? = null,
    @SerialName("vehicle_id") val vehicleId: String,
    val origin: String,
    val destination: String,
    @SerialName("standard_fare") val standardFare: Double,
    @SerialName("gov_capped_max_fare") val govCappedMaxFare: Double,
    @SerialName("is_shared_service") val isSharedService: Boolean = true
)

@Serializable
data class TourGuideProfileDto(
    val id: String? = null,
    @SerialName("guide_id") val guideId: String,
    @SerialName("license_number") val licenseNumber: String,
    @SerialName("experience_years") val experienceYears: Int,
    @SerialName("languages_spoken") val languagesSpoken: List<String> = listOf("Hindi", "English"),
    @SerialName("badge_level") val badgeLevel: String = "UP Tourism Approved",
    @SerialName("is_verified") val isVerified: Boolean = false
)

@Serializable
data class TourPackageDto(
    val id: String? = null,
    @SerialName("guide_profile_id") val guideProfileId: String,
    @SerialName("package_title") val packageTitle: String,
    @SerialName("duration_hours") val durationHours: Int,
    @SerialName("max_group_size") val maxGroupSize: Int,
    @SerialName("price_per_person") val pricePerPerson: Double,
    @SerialName("included_services") val includedServices: List<String> = listOf(),
    @SerialName("itinerary_highlights") val itineraryHighlights: String,
    @SerialName("is_active") val isActive: Boolean = true
)

