package com.example.data.repository

import com.example.data.model.NearbyPlace
import com.example.data.model.Place
import com.example.data.remote.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * SupabaseManager singleton providing access to SupabaseClient and installed modules including Storage.
 */
object SupabaseManager {
    val client: SupabaseClient
        get() = SupabaseProvider.client
}

/**
 * Repository interface for fetching Prayagraj master places with Room database offline caching
 * and querying nearby locations via PostGIS RPC.
 */
interface PlacesRepository {
    suspend fun getPlaces(category: String? = null): Result<List<Place>>
    suspend fun getNearbyPlaces(lat: Double, lng: Double, radiusMeters: Double = 5000.0): Result<List<NearbyPlace>>
    suspend fun getPlaceById(id: String): Result<Place?>
    suspend fun getCachedPlaces(category: String? = null): List<Place>
    suspend fun cachePlaces(places: List<Place>)
    fun getCachedPlacesFlow(): Flow<List<Place>>? = null
}

/**
 * Fetch all places directly from the Supabase "places" table.
 */
suspend fun getPlaces(client: SupabaseClient = SupabaseManager.client): List<Place> = withContext(Dispatchers.IO) {
    client.from("places")
        .select()
        .decodeList<Place>()
}
