package com.example.data.repository

import com.example.MainApplication
import com.example.data.local.PlaceDao
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.model.AccessibilityLevel
import com.example.data.model.NearbyPlace
import com.example.data.model.Place
import com.example.data.remote.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.*

/**
 * Implementation of [PlacesRepository] communicating directly with Supabase Postgrest & PostGIS RPC,
 * backed by a local Room database cache for offline availability.
 */
class PlacesRepositoryImpl(
    private val postgrestProvider: () -> Postgrest = { SupabaseProvider.postgrest },
    private val clientProvider: () -> SupabaseClient = { SupabaseProvider.client },
    customDao: PlaceDao? = null
) : OfflineFirstPlacesRepository {

    private val postgrest: Postgrest get() = postgrestProvider()
    private val client: SupabaseClient get() = clientProvider()

    private val placeDao: PlaceDao? = customDao ?: try {
        MainApplication.database.placeDao()
    } catch (e: Throwable) {
        null
    }

    override fun getCachedPlacesFlow(): Flow<List<Place>>? {
        return placeDao?.getAllPlacesFlow()?.map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getCachedPlaces(category: String?): List<Place> = withContext(Dispatchers.IO) {
        val cached = if (!category.isNullOrBlank() && !category.equals("all", ignoreCase = true)) {
            placeDao?.getPlacesByCategory(category)?.map { it.toDomainModel() } ?: emptyList()
        } else {
            placeDao?.getAllPlacesList()?.map { it.toDomainModel() } ?: emptyList()
        }

        if (cached.isNotEmpty()) {
            cached
        } else {
            // If Room is empty, seed it with default Prayagraj curated dataset
            seedDefaultPlacesToRoom()
            filterLocalPlaces(category)
        }
    }

    override suspend fun cachePlaces(places: List<Place>) = withContext(Dispatchers.IO) {
        if (places.isNotEmpty()) {
            try {
                placeDao?.insertPlaces(places.map { it.toEntity() })
            } catch (e: Exception) {
                // Ignore caching errors
            }
        }
    }

    override suspend fun fetchAndSyncRemotePlaces(): List<Place> = withContext(Dispatchers.IO) {
        if (SupabaseProvider.isConfigured()) {
            try {
                val remote = postgrest.from("places").select {
                    order(column = "name", order = Order.ASCENDING)
                }.decodeList<Place>()
                if (remote.isNotEmpty()) {
                    cachePlaces(remote)
                    return@withContext remote
                }
            } catch (e: Exception) {
                // Ignore remote network error and fallback to cache
            }
        }
        val cached = getCachedPlaces()
        if (cached.isEmpty()) {
            seedDefaultPlacesToRoom()
            getCachedPlaces()
        } else {
            cached
        }
    }

    suspend fun getPlaces(): List<Place> = withContext(Dispatchers.IO) {
        try {
            val remote = client.from("places")
                .select()
                .decodeList<Place>()
            if (remote.isNotEmpty()) {
                cachePlaces(remote)
                remote
            } else {
                getCachedPlaces()
            }
        } catch (e: Exception) {
            getCachedPlaces()
        }
    }

    override suspend fun getPlaces(category: String?): Result<List<Place>> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            val localCached = getCachedPlaces(category)
            return@withContext Result.success(localCached)
        }

        try {
            val result = postgrest.from("places").select {
                if (!category.isNullOrBlank() && !category.equals("all", ignoreCase = true)) {
                    filter {
                        eq("category", category)
                    }
                }
                order(column = "name", order = Order.ASCENDING)
            }.decodeList<Place>()

            if (result.isNotEmpty()) {
                // Cache freshly fetched places into Room database
                cachePlaces(result)
                Result.success(result)
            } else {
                // Fallback to Room cached Prayagraj data if remote returns empty
                val localCached = getCachedPlaces(category)
                Result.success(localCached)
            }
        } catch (e: Exception) {
            // Graceful offline fallback to Room database cache
            val localCached = getCachedPlaces(category)
            Result.success(localCached)
        }
    }

    override suspend fun getNearbyPlaces(
        lat: Double,
        lng: Double,
        radiusMeters: Double
    ): Result<List<NearbyPlace>> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext Result.success(computeLocalNearby(lat, lng, radiusMeters))
        }

        try {
            val params = buildJsonObject {
                put("lat", lat)
                put("long", lng)
                put("radius_meters", radiusMeters)
            }
            val result = postgrest.rpc(
                function = "nearby_places",
                parameters = params
            ).decodeList<NearbyPlace>()

            if (result.isNotEmpty()) {
                Result.success(result)
            } else {
                Result.success(computeLocalNearby(lat, lng, radiusMeters))
            }
        } catch (e: Exception) {
            // Graceful fallback to locally computed distance estimation from Room cached places
            Result.success(computeLocalNearby(lat, lng, radiusMeters))
        }
    }

    override suspend fun getPlaceById(id: String): Result<Place?> = withContext(Dispatchers.IO) {
        // Check Room local cache first or remote
        if (SupabaseProvider.isConfigured()) {
            try {
                val place = postgrest.from("places").select {
                    filter { eq("id", id) }
                    single()
                }.decodeSingleOrNull<Place>()

                if (place != null) {
                    try {
                        placeDao?.insertPlace(place.toEntity())
                    } catch (e: Exception) {
                        // ignore
                    }
                    return@withContext Result.success(place)
                }
            } catch (e: Exception) {
                // proceed to cache check
            }
        }

        // Room Cache Check
        try {
            val cachedEntity = placeDao?.getPlaceById(id)
            if (cachedEntity != null) {
                return@withContext Result.success(cachedEntity.toDomainModel())
            }
        } catch (e: Exception) {
            // ignore
        }

        // Fallback to static prepopulated database
        val fallback = PREPOPULATED_PRAYAGRAJ_PLACES.find { it.id == id }
        Result.success(fallback)
    }

    private suspend fun seedDefaultPlacesToRoom() {
        try {
            placeDao?.insertPlaces(PREPOPULATED_PRAYAGRAJ_PLACES.map { it.toEntity() })
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun filterLocalPlaces(category: String?): List<Place> {
        return if (category.isNullOrBlank() || category.equals("all", ignoreCase = true)) {
            PREPOPULATED_PRAYAGRAJ_PLACES
        } else {
            PREPOPULATED_PRAYAGRAJ_PLACES.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    private suspend fun computeLocalNearby(lat: Double, lng: Double, radiusMeters: Double): List<NearbyPlace> {
        val places = try {
            val roomPlaces = placeDao?.getAllPlacesList()?.map { it.toDomainModel() }
            if (!roomPlaces.isNullOrEmpty()) roomPlaces else PREPOPULATED_PRAYAGRAJ_PLACES
        } catch (e: Exception) {
            PREPOPULATED_PRAYAGRAJ_PLACES
        }

        return places.mapNotNull { place ->
            val dist = calculateHaversineDistanceMeters(lat, lng, place.latitude, place.longitude)
            if (dist <= radiusMeters) {
                NearbyPlace(
                    id = place.id,
                    name = place.name,
                    hindiName = place.hindiName,
                    category = place.category,
                    description = place.description,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    stepCount = place.stepCount,
                    accessibilityLevel = place.accessibilityLevel.name.lowercase(),
                    openingHours = place.openingHours,
                    imageUrl = place.imageUrl,
                    distanceMeters = dist
                )
            } else null
        }.sortedBy { it.distanceMeters }
    }

    private fun calculateHaversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    companion object {
        /**
         * Curated Prayagraj Master Database matching PostGIS coordinates
         */
        val PREPOPULATED_PRAYAGRAJ_PLACES = listOf(
            Place(
                id = "11111111-1111-1111-1111-111111111101",
                name = "Triveni Sangam",
                hindiName = "त्रिवेणी संगम",
                category = "ghat",
                description = "The holy confluence of the Ganga, Yamuna, and mythical Saraswati rivers. The sacred epicenter for Kumbh Mela and holy Snan.",
                latitude = 25.4290,
                longitude = 81.8845,
                stepCount = 12,
                accessibilityLevel = AccessibilityLevel.EASY,
                openingHours = "Open 24 Hours (Best at Sunrise/Sunset)",
                imageUrl = "https://images.unsplash.com/photo-1596176530529-78163a4f7af2",
                featured = true,
                tags = listOf("Holy Dip", "Boat Ride", "Aarti", "Spiritual")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111102",
                name = "Bade Hanuman Ji Temple",
                hindiName = "बड़े हनुमान जी (लेटे हनुमान मंदिर)",
                category = "temple",
                description = "Iconic ancient subterranean temple housing the colossal 20-foot reclining idol of Lord Hanuman, submerged annually by river Ganga.",
                latitude = 25.4325,
                longitude = 81.8790,
                stepCount = 18,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "05:00 AM - 10:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1609342122563-a43ac8917a3a",
                featured = true,
                tags = listOf("Reclining Idol", "Sindoor Arpan", "Ancient")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111103",
                name = "Alopi Devi Shaktipeeth",
                hindiName = "अलोपी देवी शक्तिपीठ",
                category = "temple",
                description = "One of the 51 revered Shaktipeeths where the 'Doli' (palanquin) of Goddess Sati is worshipped with eternal rituals.",
                latitude = 25.4412,
                longitude = 81.8680,
                stepCount = 8,
                accessibilityLevel = AccessibilityLevel.WHEELCHAIR_FRIENDLY,
                openingHours = "06:00 AM - 09:30 PM",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136",
                featured = true,
                tags = listOf("Shaktipeeth", "Navratri Special", "Palanquin")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111104",
                name = "Akshayavat & Patalpuri Temple",
                hindiName = "अक्षयवट एवं पातालपुरी मंदिर",
                category = "temple",
                description = "The immortal indestructible Banyan tree mentioned in Vedic scriptures, situated inside the historic Allahabad Fort complex.",
                latitude = 25.4302,
                longitude = 81.8765,
                stepCount = 24,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "07:00 AM - 05:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1582510003544-4d00b7f74220",
                featured = true,
                tags = listOf("Immortal Banyan Tree", "Fort Complex", "Vedic")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111105",
                name = "Nag Vasuki Temple",
                hindiName = "नाग वासुकी मंदिर",
                category = "temple",
                description = "Ancient temple dedicated to King of Serpents, Vasuki, located on the northern banks of Ganga near Daraganj.",
                latitude = 25.4520,
                longitude = 81.8720,
                stepCount = 35,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "05:30 AM - 08:30 PM",
                imageUrl = "https://images.unsplash.com/photo-1567157577867-05ccb1388e66",
                featured = false,
                tags = listOf("Nag Panchami", "Daraganj", "Ancient Shrine")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111106",
                name = "Anand Bhavan & Swaraj Bhavan",
                hindiName = "आनंद भवन एवं स्वराज भवन",
                category = "heritage",
                description = "Historic ancestral estate of the Nehru-Gandhi family turned museum, housing pivotal artifacts of the Indian Freedom Movement and a modern Planetarium.",
                latitude = 25.4578,
                longitude = 81.8592,
                stepCount = 10,
                accessibilityLevel = AccessibilityLevel.WHEELCHAIR_FRIENDLY,
                openingHours = "09:30 AM - 05:00 PM (Closed Mondays)",
                entryFee = 70.0,
                imageUrl = "https://images.unsplash.com/photo-1590077428593-a55bb07c4665",
                featured = true,
                tags = listOf("Heritage", "Museum", "Freedom Movement", "Planetarium")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111107",
                name = "Chandrashekhar Azad Park (Alfred Park)",
                hindiName = "चंद्रशेखर आजाद पार्क",
                category = "heritage",
                description = "Sprawling 133-acre lush historic park where revolutionary hero Chandrashekhar Azad achieved martyrdom. Home to Allahabad Museum & Victoria Memorial.",
                latitude = 25.4540,
                longitude = 81.8480,
                stepCount = 0,
                accessibilityLevel = AccessibilityLevel.EASY,
                openingHours = "05:00 AM - 08:00 PM",
                entryFee = 10.0,
                imageUrl = "https://images.unsplash.com/photo-1519331379826-f10be5486c6f",
                featured = false,
                tags = listOf("Azad Memorial", "Lush Greenery", "Jogging", "Museum")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111108",
                name = "Shankar Viman Mandapam",
                hindiName = "शंकर विमान मंडपम",
                category = "temple",
                description = "Striking 130-foot tall four-tiered South Indian Dravidian architectural marvel located close to the Sangam banks.",
                latitude = 25.4310,
                longitude = 81.8810,
                stepCount = 45,
                accessibilityLevel = AccessibilityLevel.DIFFICULT,
                openingHours = "06:00 AM - 08:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675",
                featured = true,
                tags = listOf("Dravidian Architecture", "Kanchi Kamakoti", "Panoramic View")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111109",
                name = "Saraswati Ghat & Yamuna Aarti",
                hindiName = "सरस्वती घाट",
                category = "ghat",
                description = "Picturesque paved riverfront promenade along Yamuna river, famous for evening maha-aarti and illuminated motorboat rides.",
                latitude = 25.4340,
                longitude = 81.8650,
                stepCount = 15,
                accessibilityLevel = AccessibilityLevel.EASY,
                openingHours = "Open 24 Hours (Aarti at 06:30 PM)",
                imageUrl = "https://images.unsplash.com/photo-1600100397608-f010f443b2a3",
                featured = true,
                tags = listOf("Ganga-Yamuna Aarti", "Sunset Promenade", "Boating")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111110",
                name = "Khusro Bagh",
                hindiName = "खुसरो बाग",
                category = "heritage",
                description = "Grand walled Mughal garden complex housing intricate sandstone mausoleums of Prince Khusro, Shah Begum, and Nithar Begum.",
                latitude = 25.4418,
                longitude = 81.8242,
                stepCount = 6,
                accessibilityLevel = AccessibilityLevel.WHEELCHAIR_FRIENDLY,
                openingHours = "06:00 AM - 07:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1599839575945-a9e5af0c3fa5",
                featured = true,
                tags = listOf("Mughal Architecture", "Historic Tombs", "Heritage Garden")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111111",
                name = "Mankameshwar Temple",
                hindiName = "मनकामेश्वर महादेव मंदिर",
                category = "temple",
                description = "Historic Shiva temple nestled on the banks of Yamuna river near Saraswati Ghat, frequented for fulfilling heart desires.",
                latitude = 25.4320,
                longitude = 81.8610,
                stepCount = 12,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "05:00 AM - 09:30 PM",
                imageUrl = "https://images.unsplash.com/photo-1609342122563-a43ac8917a3a",
                featured = true,
                tags = listOf("Shiva Shrine", "Yamuna Shore", "Pradosh Vrat")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111112",
                name = "Allahabad Fort & Ashoka Pillar",
                hindiName = "प्रयागराज किला एवं अशोक स्तम्भ",
                category = "heritage",
                description = "Monumental fortress built in 1583 overlooking the confluence of Ganga and Yamuna, housing the ancient polished Ashoka edict pillar.",
                latitude = 25.4295,
                longitude = 81.8760,
                stepCount = 20,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "07:00 AM - 05:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1590077428593-a55bb07c4665",
                featured = true,
                tags = listOf("Mughal Fort", "Ashoka Edict", "Confluence View")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111113",
                name = "Someshwar Mahadev Temple",
                hindiName = "सोमेश्वर महादेव मंदिर (अड़ैल)",
                category = "temple",
                description = "Revered ancient subterranean Shiva temple on the southern bank of Yamuna in Arail, associated with Chandra Deva's penance.",
                latitude = 25.4190,
                longitude = 81.8740,
                stepCount = 30,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "05:30 AM - 09:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136",
                featured = false,
                tags = listOf("Arail Ghat", "Subterranean Lingam", "Chandra Deva")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111114",
                name = "Dashashwamedh Ghat",
                hindiName = "दशाश्वमेध घाट",
                category = "ghat",
                description = "Ancient sacred ghat where according to mythology Lord Brahma executed ten horse sacrifices (Dash-Ashwamedha Yagna).",
                latitude = 25.4450,
                longitude = 81.8700,
                stepCount = 22,
                accessibilityLevel = AccessibilityLevel.MODERATE,
                openingHours = "Open 24 Hours",
                imageUrl = "https://images.unsplash.com/photo-1596176530529-78163a4f7af2",
                featured = false,
                tags = listOf("Vedic Yagna", "Ganga Aarti", "Holy Dip")
            ),
            Place(
                id = "11111111-1111-1111-1111-111111111115",
                name = "All Saints Cathedral (Patthar Girja)",
                hindiName = "ऑल सेंट्स कैथेड्रल (पत्थर गिरजा)",
                category = "heritage",
                description = "Magnificent 19th-century Victorian Gothic revival style cathedral in Civil Lines crafted with fine cream and red sandstone.",
                latitude = 25.4528,
                longitude = 81.8340,
                stepCount = 5,
                accessibilityLevel = AccessibilityLevel.WHEELCHAIR_FRIENDLY,
                openingHours = "08:30 AM - 05:30 PM",
                imageUrl = "https://images.unsplash.com/photo-1582510003544-4d00b7f74220",
                featured = false,
                tags = listOf("Gothic Revival", "Stained Glass", "Civil Lines Heritage")
            )
        )
    }
}
