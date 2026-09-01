package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==========================================
// CUSTOM ENUMS MATCHING POSTGRESQL ENUMS
// ==========================================

@Serializable
enum class UserRole {
    @SerialName("pilgrim") PILGRIM,
    @SerialName("panda") PANDA,
    @SerialName("driver") DRIVER,
    @SerialName("admin") ADMIN
}

@Serializable
enum class VehicleType {
    @SerialName("ebike") EBIKE,
    @SerialName("erickshaw") ERICKSHAW,
    @SerialName("auto") AUTO,
    @SerialName("traveller") TRAVELLER,
    @SerialName("boat") BOAT
}

@Serializable
enum class BookingStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED
}

@Serializable
enum class PaymentStatus {
    @SerialName("pending") PENDING,
    @SerialName("paid") PAID,
    @SerialName("refunded") REFUNDED,
    @SerialName("failed") FAILED
}

@Serializable
enum class RitualCategory {
    @SerialName("snan") SNAN,
    @SerialName("pind_daan") PIND_DAAN,
    @SerialName("ganga_aarti") GANGA_AARTI,
    @SerialName("sankalp") SANKALP,
    @SerialName("havan") HAVAN,
    @SerialName("katha") KATHA,
    @SerialName("darshan") DARSHAN
}

@Serializable
enum class StayType {
    @SerialName("dharamshala") DHARAMSHALA,
    @SerialName("ashram") ASHRAM,
    @SerialName("hotel") HOTEL,
    @SerialName("homestay") HOMESTAY,
    @SerialName("tent_city") TENT_CITY
}

@Serializable
enum class AccessibilityLevel {
    @SerialName("easy") EASY,
    @SerialName("moderate") MODERATE,
    @SerialName("difficult") DIFFICULT,
    @SerialName("wheelchair_friendly") WHEELCHAIR_FRIENDLY
}

// ==========================================
// CORE DATA MODELS MATCHING POSTGRESQL SCHEMA
// ==========================================

/**
 * Represents a registered user linked to Supabase Auth.
 */
@Serializable
data class User(
    val id: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    val role: UserRole = UserRole.PILGRIM,
    @SerialName("language_preference") val languagePreference: String = "hi",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * User Profile data model matching the public.users database table.
 */
@Serializable
data class UserProfile(
    val id: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    val role: String = "PILGRIM",
    @SerialName("language_preference") val languagePreference: String = "hi",
    @SerialName("profile_pic_url") val profilePicUrl: String? = null
)

/**
 * Represents a verified Panda / Priest profile.
 */
@Serializable
data class PandaProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("accreditation_number") val accreditationNumber: String? = null,
    val languages: List<String> = emptyList(),
    val bio: String? = null,
    @SerialName("years_of_experience") val yearsOfExperience: Int = 0,
    val rating: Double = 5.0,
    @SerialName("total_reviews") val totalReviews: Int = 0,
    @SerialName("is_verified") val isVerified: Boolean = true,
    val specializations: List<String> = emptyList()
)

/**
 * Represents a Driver profile with live PostGIS coordinates.
 */
@Serializable
data class DriverProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("license_number") val licenseNumber: String,
    @SerialName("vehicle_type") val vehicleType: VehicleType,
    @SerialName("vehicle_number") val vehicleNumber: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("current_status") val currentStatus: String = "online",
    val rating: Double = 5.0
)


/**
 * Result model returned by the PostGIS `nearby_places` RPC database function.
 */
@Serializable
data class NearbyPlace(
    val id: String,
    val name: String,
    @SerialName("hindi_name") val hindiName: String? = null,
    val category: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("step_count") val stepCount: Int = 0,
    @SerialName("accessibility_level") val accessibilityLevel: String = "easy",
    @SerialName("opening_hours") val openingHours: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("distance_meters") val distanceMeters: Double = 0.0
)

/**
 * Parameters for calling the `nearby_places` RPC.
 */
@Serializable
data class NearbyPlacesParams(
    val lat: Double,
    val long: Double,
    @SerialName("radius_meters") val radiusMeters: Double = 5000.0
)

/**
 * Ritual service offerings by pandas.
 */
@Serializable
data class RitualService(
    val id: String,
    @SerialName("panda_id") val pandaId: String,
    val title: String,
    val category: RitualCategory,
    val description: String? = null,
    @SerialName("dakshina_amount") val dakshinaAmount: Double,
    @SerialName("samagri_included") val samagriIncluded: Boolean = true,
    @SerialName("boat_included") val boatIncluded: Boolean = false,
    @SerialName("duration_minutes") val durationMinutes: Int = 45
)

/**
 * Accommodation listing (Dharamshala, Ashram, Hotel).
 */
@Serializable
data class Accommodation(
    val id: String,
    val name: String,
    @SerialName("stay_type") val stayType: StayType,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("price_per_night") val pricePerNight: Double,
    val amenities: List<String> = emptyList(),
    @SerialName("contact_number") val contactNumber: String? = null,
    val rating: Double = 4.5
)

/**
 * Multi-stop itinerary cart.
 */
@Serializable
data class ItineraryCart(
    val id: String,
    @SerialName("pilgrim_id") val pilgrimId: String,
    val title: String = "My Prayagraj Yatra",
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Waypoint inside the pilgrim's itinerary cart.
 */
@Serializable
data class CartWaypoint(
    val id: String,
    @SerialName("cart_id") val cartId: String,
    @SerialName("place_id") val placeId: String,
    @SerialName("stop_order") val stopOrder: Int,
    @SerialName("planned_duration_minutes") val plannedDurationMinutes: Int = 60,
    val notes: String? = null
)

/**
 * Master booking ledger.
 */
@Serializable
data class Booking(
    val id: String,
    @SerialName("booking_code") val bookingCode: String,
    @SerialName("pilgrim_id") val pilgrimId: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("booking_status") val bookingStatus: BookingStatus = BookingStatus.PENDING,
    @SerialName("payment_status") val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    @SerialName("booking_date") val bookingDate: String,
    @SerialName("special_instructions") val specialInstructions: String? = null
)

/**
 * Line item in a booking (e.g. Ritual, Vehicle Ride, Stay).
 */
@Serializable
data class BookingLineItem(
    val id: String,
    @SerialName("booking_id") val bookingId: String,
    @SerialName("item_type") val itemType: String, // "ritual", "ride", "stay"
    @SerialName("reference_id") val referenceId: String,
    val quantity: Int = 1,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("subtotal_price") val subtotalPrice: Double
)
