package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a scheduled or custom itinerary stop.
 */
@Entity(tableName = "itinerary_stops")
data class ItineraryEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val name: String,
    val category: String = "general",
    val stopOrder: Int = 0,
    val plannedDurationMinutes: Int = 60,
    val notes: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null,
    val isCompleted: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
