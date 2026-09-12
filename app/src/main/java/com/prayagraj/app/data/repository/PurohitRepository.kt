package com.prayagraj.app.data.repository

import com.prayagraj.app.data.model.PurohitServiceDto
import com.prayagraj.app.data.remote.SupabaseProvider
import com.example.data.model.VendorProfileDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PurohitRepository {
    private val postgrest = SupabaseProvider.postgrest
    private val auth = SupabaseProvider.auth
    private val client = SupabaseProvider.client
    private val json = Json { ignoreUnknownKeys = true }

    private val fallbackProfile = VendorProfileDto(
        id = "purohit-demo-01",
        fullName = "Pt. Ramakant Mishra Shastri",
        phoneNumber = "+91 94150 28471",
        role = "PUROHIT",
        isVerified = true,
        registrationId = "PRY-KMB-2025-0142"
    )

    private val localFallbackServices = mutableListOf(
        PurohitServiceDto(
            id = "srv-101",
            purohitId = "purohit-demo-01",
            poojaName = "Triveni Sangam Snan & Maha Sankalp Vidhi",
            category = "Sangam Snan",
            durationHours = 0.75,
            baseDakshina = 501.0,
            samagriIncluded = true,
            samagriExtraCost = 0.0,
            languagesSupported = listOf("Hindi", "Sanskrit"),
            description = "Authentic Vedic Sankalp at holy Sangam confluence with Gangajal, milk, flowers & Vedic mantras."
        ),
        PurohitServiceDto(
            id = "srv-102",
            purohitId = "purohit-demo-01",
            poojaName = "Pitru Tarpan & Pind Daan (Sangam Ghat)",
            category = "Pind Daan",
            durationHours = 1.25,
            baseDakshina = 1100.0,
            samagriIncluded = true,
            samagriExtraCost = 250.0,
            languagesSupported = listOf("Hindi", "Sanskrit"),
            description = "Complete ancestral rites according to Garud Purana with sesame, kusha, and holy oblations."
        ),
        PurohitServiceDto(
            id = "srv-103",
            purohitId = "purohit-demo-01",
            poojaName = "Rudrabhishek Puja & Ganga Aarti Darshan",
            category = "Abhishek",
            durationHours = 1.0,
            baseDakshina = 2100.0,
            samagriIncluded = true,
            samagriExtraCost = 500.0,
            languagesSupported = listOf("Hindi", "Sanskrit"),
            description = "Special Shiva Abhishek with Panchamrit and bilva leaves followed by evening Ganga Deep Daan."
        ),
        PurohitServiceDto(
            id = "srv-104",
            purohitId = "purohit-demo-01",
            poojaName = "Veni Daan Ritual (Married Couples Rites)",
            category = "Veni Daan",
            durationHours = 0.5,
            baseDakshina = 751.0,
            samagriIncluded = true,
            samagriExtraCost = 0.0,
            languagesSupported = listOf("Hindi", "Sanskrit"),
            description = "Traditional Prayag Veni Daan for longevity and spiritual marital bliss at Sangam."
        )
    )

    fun getCurrentUserId(): String? = try {
        auth.currentUserOrNull()?.id ?: "purohit-demo-01"
    } catch (e: Exception) {
        "purohit-demo-01"
    }

    suspend fun createPoojaService(service: PurohitServiceDto): Result<PurohitServiceDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("User unauthenticated")

                val dtoWithUser = service.copy(purohitId = userId)

                postgrest["purohit_services"]
                    .insert(dtoWithUser) { select() }
                    .decodeSingle<PurohitServiceDto>()
            }
        }

    // Save or Update Profile
    suspend fun saveProfile(profile: VendorProfileDto): Result<VendorProfileDto> = withContext(Dispatchers.IO) {
        runCatching {
            if (SupabaseProvider.isConfigured()) {
                client.from("vendor_profiles")
                    .upsert(profile) { select() }
                    .decodeSingle<VendorProfileDto>()
            } else {
                profile
            }
        }
    }

    // Fetch Profile
    suspend fun getProfile(userId: String): VendorProfileDto? = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext fallbackProfile.copy(id = userId)
        }
        try {
            client.from("vendor_profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<VendorProfileDto>() ?: fallbackProfile.copy(id = userId)
        } catch (e: Exception) {
            fallbackProfile.copy(id = userId)
        }
    }

    // Fetch Initial Services
    suspend fun fetchServices(purohitId: String): List<PurohitServiceDto> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext localFallbackServices.toList()
        }
        try {
            val remoteList = client.from("purohit_services")
                .select { filter { eq("purohit_id", purohitId) } }
                .decodeList<PurohitServiceDto>()
            if (remoteList.isNotEmpty()) remoteList else localFallbackServices.toList()
        } catch (e: Exception) {
            localFallbackServices.toList()
        }
    }

    // Add New Service
    suspend fun addService(service: PurohitServiceDto) = withContext(Dispatchers.IO) {
        localFallbackServices.add(service)
        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("purohit_services").insert(service)
            } catch (e: Exception) {
                // Fallback gracefully
            }
        }
    }

    // Realtime Service Updates Flow
    fun observeServicesRealtime(purohitId: String): Flow<List<PurohitServiceDto>> = flow {
        val currentList = fetchServices(purohitId).toMutableList()
        emit(currentList.toList())

        if (SupabaseProvider.isConfigured()) {
            try {
                val channel = client.channel("purohit_services_changes")
                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "purohit_services"
                }

                channel.subscribe()

                changeFlow.collect { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val newService = json.decodeFromJsonElement(
                                PurohitServiceDto.serializer(),
                                action.record
                            )
                            if (newService.purohitId == purohitId) {
                                currentList.add(newService)
                                emit(currentList.toList())
                            }
                        }
                        is PostgresAction.Delete -> {
                            val refreshed = fetchServices(purohitId)
                            emit(refreshed)
                        }
                        else -> {
                            val refreshed = fetchServices(purohitId)
                            emit(refreshed)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to initial emitted list
            }
        }
    }.flowOn(Dispatchers.IO)
}
