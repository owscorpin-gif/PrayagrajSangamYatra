package com.example.data.repository

import com.example.data.model.Place

/**
 * Offline-first repository interface for fetching, caching, and synchronizing Prayagraj places.
 */
interface OfflineFirstPlacesRepository : PlacesRepository {
    suspend fun fetchAndSyncRemotePlaces(): List<Place>
}
