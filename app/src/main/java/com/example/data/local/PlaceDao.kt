package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for reading and writing cached places/locations in Room.
 */
@Dao
interface PlaceDao {

    @Query("SELECT * FROM cached_places ORDER BY name ASC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM cached_places ORDER BY name ASC")
    fun getAllPlacesFlow(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM cached_places ORDER BY name ASC")
    suspend fun getAllPlacesList(): List<PlaceEntity>

    @Query("SELECT * FROM cached_places WHERE isSelected = 1")
    fun getItineraryStops(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM cached_places WHERE isSelected = 1")
    suspend fun getItineraryStopsList(): List<PlaceEntity>

    @Query("SELECT * FROM cached_places WHERE LOWER(category) = LOWER(:category) ORDER BY name ASC")
    suspend fun getPlacesByCategory(category: String): List<PlaceEntity>

    @Query("SELECT * FROM cached_places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: String): PlaceEntity?

    @Query("SELECT COUNT(*) FROM cached_places")
    suspend fun getPlacesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Update
    suspend fun updatePlace(place: PlaceEntity)

    @Query("UPDATE cached_places SET isSelected = :isSelected WHERE id = :placeId")
    suspend fun updateSelectionState(placeId: String, isSelected: Boolean)

    @Query("UPDATE cached_places SET isSelected = 0")
    suspend fun clearAllSelections()

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("DELETE FROM cached_places WHERE id = :id")
    suspend fun deletePlaceById(id: String)

    @Query("DELETE FROM cached_places")
    suspend fun deleteAll()

    @Query("DELETE FROM cached_places")
    suspend fun clearAllPlaces()
}
