package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "itinerary_stops",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItineraryStopEntity(
    @PrimaryKey
    val placeId: String,
    val sequenceOrder: Int
)

// Embedded data class combining Place details with Sequence position
data class OrderedItineraryStop(
    @Embedded val place: PlaceEntity,
    val sequenceOrder: Int
)
