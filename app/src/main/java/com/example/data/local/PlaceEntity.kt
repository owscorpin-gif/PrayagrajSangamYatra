package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AccessibilityLevel
import com.example.data.model.Place

@Entity(tableName = "cached_places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null,
    val timings: String? = null,
    val address: String? = null,
    val isSelected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val hindiName: String? = null,
    val stepCount: Int = 0,
    val accessibilityLevel: AccessibilityLevel = AccessibilityLevel.EASY,
    val openingHours: String? = null,
    val entryFee: Double = 0.0,
    val featured: Boolean = false,
    val tags: List<String> = emptyList()
)

fun PlaceEntity.toDomainModel(): Place {
    return Place(
        id = id,
        name = name,
        hindiName = hindiName,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        address = address,
        timings = timings,
        imageUrl = imageUrl,
        stepCount = stepCount,
        accessibilityLevel = accessibilityLevel,
        openingHours = openingHours,
        entryFee = entryFee,
        featured = featured,
        tags = tags
    )
}

fun Place.toEntity(isSelected: Boolean = false): PlaceEntity {
    return PlaceEntity(
        id = id,
        name = name,
        hindiName = hindiName,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        address = address,
        timings = timings,
        imageUrl = imageUrl,
        stepCount = stepCount,
        accessibilityLevel = accessibilityLevel,
        openingHours = openingHours,
        entryFee = entryFee,
        featured = featured,
        tags = tags,
        isSelected = isSelected,
        updatedAt = System.currentTimeMillis()
    )
}
