package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.ItineraryDao
import com.example.data.local.ItineraryStopEntity
import com.example.data.local.PrayagrajDatabase
import com.example.data.local.toDomainModel
import com.example.data.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PersistedItineraryRepository(
    private val database: PrayagrajDatabase,
    private val itineraryDao: ItineraryDao
) {
    // Flow emitting itinerary places ordered by sequenceOrder
    val orderedItinerary: Flow<List<Place>> = itineraryDao.getOrderedItinerary().map { list ->
        list.map { it.place.toDomainModel() }
    }

    suspend fun addStop(placeId: String) {
        val currentCount = itineraryDao.getStopCount()
        itineraryDao.insertStop(
            ItineraryStopEntity(placeId = placeId, sequenceOrder = currentCount)
        )
    }

    suspend fun removeStop(placeId: String) {
        database.withTransaction {
            itineraryDao.removeStop(placeId)
            reindexStops()
        }
    }

    suspend fun moveStopUp(currentList: List<Place>, index: Int) {
        if (index <= 0 || index >= currentList.size) return
        swapStops(currentList[index].id, index, currentList[index - 1].id, index - 1)
    }

    suspend fun moveStopDown(currentList: List<Place>, index: Int) {
        if (index < 0 || index >= currentList.size - 1) return
        swapStops(currentList[index].id, index, currentList[index + 1].id, index + 1)
    }

    private suspend fun swapStops(id1: String, pos1: Int, id2: String, pos2: Int) {
        database.withTransaction {
            itineraryDao.insertStop(ItineraryStopEntity(placeId = id1, sequenceOrder = pos2))
            itineraryDao.insertStop(ItineraryStopEntity(placeId = id2, sequenceOrder = pos1))
        }
    }

    // Ensures gapless sequence indices (0, 1, 2, ...) after deletion
    private suspend fun reindexStops() {
        val currentStops = itineraryDao.getOrderedItineraryList()
        // Re-assign sequential indices
        val updatedEntities = currentStops.mapIndexed { index, stop ->
            ItineraryStopEntity(placeId = stop.place.id, sequenceOrder = index)
        }
        itineraryDao.insertAllStops(updatedEntities)
    }
}
