package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Master catalog Place data model matching Supabase database table schema.
 */
@Serializable
data class Place(
    val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val timings: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("hindi_name") val hindiName: String? = null,
    @SerialName("step_count") val stepCount: Int = 0,
    @SerialName("accessibility_level") val accessibilityLevel: AccessibilityLevel = AccessibilityLevel.EASY,
    @SerialName("opening_hours") val openingHours: String? = null,
    @SerialName("entry_fee") val entryFee: Double = 0.0,
    val featured: Boolean = false,
    val tags: List<String> = emptyList()
)
