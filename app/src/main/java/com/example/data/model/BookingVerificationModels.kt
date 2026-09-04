package com.example.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Status of the booking verification check against the official database.
 */
enum class VerificationStatus {
    OFFICIALLY_VERIFIED,
    SUSPICIOUS_UNREGISTERED_FRAUD,
    FLAGGED_BLACKLISTED_TOUT,
    EXPIRED,
    CANCELLED
}

/**
 * Category of pilgrim reservation or permit.
 */
enum class ReservationCategory(val displayName: String, val icon: String) {
    RITUAL_SANKALP("Vedic Ritual & Sankalp", "🪔"),
    BOAT_CONFLUENCE("Sangam Boat Confluence Ride", "🛶"),
    TEMPLE_VIP_DARSHAN("Temple VIP Darshan Pass", "🛕"),
    TENT_ACCOMMODATION("Official Camp / Tent City", "⛺"),
    SPECIAL_PERMIT("Mela Special Access Permit", "📜")
}

/**
 * Data model representing an official registered booking record from the Shrine & Mela Database.
 */
@Serializable
data class OfficialBookingRecord(
    val bookingId: String,
    val verificationCode: String,
    val category: ReservationCategory,
    val title: String,
    val hindiTitle: String = "",
    val authorityName: String = "Prayagraj Mela Pradhikaran & Sangam Shrine Board",
    val registrationCertificateNo: String,
    val pilgrimName: String,
    val pilgrimPhone: String,
    val gotraOrParty: String = "N/A",
    val numPersons: Int = 1,
    val assignedProviderName: String,
    val providerBadgeNo: String,
    val providerContact: String,
    val serviceLocation: String,
    val latitude: Double = 25.4300,
    val longitude: Double = 81.8845,
    val slotDateTime: String,
    val fixedGovtTariff: String,
    val status: VerificationStatus,
    val qrHashSignature: String,
    val antiCounterfeitSeal: String,
    val verificationTimestamp: Long = System.currentTimeMillis(),
    val securityNotes: String,
    val isBlacklisted: Boolean = false,
    val blacklistReason: String? = null,
    val reportHelplineNumber: String = "1920",
    val includesLifeJacketOrSamagri: Boolean = true
)

/**
 * History item for recent verifications.
 */
data class VerificationHistoryItem(
    val id: String,
    val bookingId: String,
    val verificationCode: String = "",
    val category: ReservationCategory = ReservationCategory.RITUAL_SANKALP,
    val title: String,
    val status: VerificationStatus,
    val timestamp: Long,
    val providerName: String,
    val serviceLocation: String = "",
    val tariff: String = "",
    val isBlacklisted: Boolean = false
)
