package com.prayagraj.app.data.repository

import com.prayagraj.app.data.model.NearbyPlace
import com.prayagraj.app.data.model.Place
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseManager {
    private const val SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co"
    private const val SUPABASE_KEY = "YOUR_SUPABASE_ANON_KEY"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}

class PlacesRepository(private val client: SupabaseClient = SupabaseManager.client) {

    suspend fun getAllPlaces(): List<Place> = withContext(Dispatchers.IO) {
        client.from("places").select().decodeList<Place>()
    }

    suspend fun getNearbyPlaces(lat: Double, lng: Double, radiusMeters: Int = 5000): List<NearbyPlace> = 
        withContext(Dispatchers.IO) {
            val params = buildJsonObject {
                put("user_lat", lat)
                put("user_lng", lng)
                put("radius_meters", radiusMeters)
            }
            client.postgrest.rpc("nearby_places", params).decodeList<NearbyPlace>()
        }
}
