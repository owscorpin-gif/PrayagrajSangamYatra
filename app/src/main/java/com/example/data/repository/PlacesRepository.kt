package com.example.data.repository

import com.example.data.model.NearbyPlace
import com.example.data.model.Place

/**
 * Repository interface for fetching Prayagraj master places and querying nearby locations via PostGIS RPC.
 */
interface PlacesRepository {
    suspend fun getPlaces(category: String? = null): Result<List<Place>>
    suspend fun getNearbyPlaces(lat: Double, lng: Double, radiusMeters: Double = 5000.0): Result<List<NearbyPlace>>
    suspend fun getPlaceById(id: String): Result<Place?>
}
