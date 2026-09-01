package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {

    // Retrieve stops strictly ordered by sequence position
    @Query("""
        SELECT cached_places.*, itinerary_stops.sequenceOrder 
        FROM cached_places 
        INNER JOIN itinerary_stops ON cached_places.id = itinerary_stops.placeId 
        ORDER BY itinerary_stops.sequenceOrder ASC
    """)
    fun getOrderedItinerary(): Flow<List<OrderedItineraryStop>>

    @Query("""
        SELECT cached_places.*, itinerary_stops.sequenceOrder 
        FROM cached_places 
        INNER JOIN itinerary_stops ON cached_places.id = itinerary_stops.placeId 
        ORDER BY itinerary_stops.sequenceOrder ASC
    """)
    suspend fun getOrderedItineraryList(): List<OrderedItineraryStop>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStop(stop: ItineraryStopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStops(stops: List<ItineraryStopEntity>)

    @Query("DELETE FROM itinerary_stops WHERE placeId = :placeId")
    suspend fun removeStop(placeId: String)

    @Query("DELETE FROM itinerary_stops")
    suspend fun clearItinerary()

    @Query("SELECT COUNT(*) FROM itinerary_stops")
    suspend fun getStopCount(): Int
}
