package com.prayagraj.app.data.repository

import com.example.data.model.PurohitServiceDto
import com.example.data.model.VendorProfileDto
import com.example.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
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
            ritualName = "Triveni Sangam Snan & Maha Sankalp Vidhi",
            description = "Authentic Vedic Sankalp at holy Sangam confluence with Gangajal, milk, flowers & Vedic mantras.",
            fixedDakshina = 501.0,
            durationMinutes = 45,
            materialsIncluded = true
        ),
        PurohitServiceDto(
            id = "srv-102",
            purohitId = "purohit-demo-01",
            ritualName = "Pitru Tarpan & Pind Daan (Sangam Ghat)",
            description = "Complete ancestral rites according to Garud Purana with sesame, kusha, and holy oblations.",
            fixedDakshina = 1100.0,
            durationMinutes = 75,
            materialsIncluded = true
        ),
        PurohitServiceDto(
            id = "srv-103",
            purohitId = "purohit-demo-01",
            ritualName = "Rudrabhishek Puja & Ganga Aarti Darshan",
            description = "Special Shiva Abhishek with Panchamrit and bilva leaves followed by evening Ganga Deep Daan.",
            fixedDakshina = 2100.0,
            durationMinutes = 60,
            materialsIncluded = true
        ),
        PurohitServiceDto(
            id = "srv-104",
            purohitId = "purohit-demo-01",
            ritualName = "Veni Daan Ritual (Married Couples Rites)",
            description = "Traditional Prayag Veni Daan for longevity and spiritual marital bliss at Sangam.",
            fixedDakshina = 751.0,
            durationMinutes = 30,
            materialsIncluded = true
        )
    )

    fun getCurrentUserId(): String? = try {
        client.auth.currentUserOrNull()?.id ?: "purohit-demo-01"
    } catch (e: Exception) {
        "purohit-demo-01"
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
