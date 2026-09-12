package com.prayagraj.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: String,
    val name: String,
    val description: String? = null,
    val category: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("accessibility_rating") val accessibilityRating: Int = 3,
    @SerialName("stairs_count") val stairsCount: Int = 0,
    @SerialName("recommended_duration_mins") val durationMins: Int = 45
)

@Serializable
data class NearbyPlace(
    val id: String,
    val name: String,
    val category: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("distance_meters") val distanceMeters: Double
)
