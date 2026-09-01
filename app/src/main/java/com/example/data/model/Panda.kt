package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PandaServiceOffering(
    val id: String,
    val title: String,
    @SerialName("hindi_title") val hindiTitle: String? = null,
    val category: String, // "Snan & Sankalp", "Pind Daan & Tarpan", "Rudrabhishek", "Vedic Havan", "Ganga Aarti", "Asthi Visarjan", "Mundan Sanskar"
    val description: String,
    @SerialName("dakshina_guide") val dakshinaGuide: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 45,
    @SerialName("samagri_included") val samagriIncluded: Boolean = true,
    @SerialName("boat_included") val boatIncluded: Boolean = false
)

@Serializable
data class VerifiedPanda(
    val id: String,
    val name: String,
    @SerialName("hindi_name") val hindiName: String,
    val title: String,
    @SerialName("accreditation_id") val accreditationId: String,
    @SerialName("ghat_location") val ghatLocation: String,
    @SerialName("ghat_category") val ghatCategory: String, // "sangam", "daraganj", "arail", "saraswati", "rasoolabad"
    @SerialName("years_of_experience") val yearsOfExperience: Int,
    @SerialName("clan_lineage") val clanLineage: String,
    @SerialName("bahi_khata_available") val bahiKhataAvailable: Boolean = true,
    @SerialName("bahi_khata_regions") val bahiKhataRegions: List<String> = emptyList(),
    val rating: Double = 4.9,
    @SerialName("total_reviews") val totalReviews: Int = 150,
    @SerialName("is_verified") val isVerified: Boolean = true,
    @SerialName("verified_authority") val verifiedAuthority: String = "Prayag Tirtha Purohit Maha Sabha & Tourism Board",
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("whatsapp_number") val whatsappNumber: String,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val languages: List<String> = listOf("Hindi", "Sanskrit"),
    val bio: String,
    val services: List<PandaServiceOffering> = emptyList(),
    val availability: String = "Available for Rituals"
)
