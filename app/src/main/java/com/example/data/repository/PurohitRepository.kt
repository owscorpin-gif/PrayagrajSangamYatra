package com.example.data.repository

import com.example.data.model.PurohitServiceDto
import com.example.data.model.VendorProfileDto
import com.example.data.remote.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository managing Tirth Purohit profile data and ritual offerings with Supabase and local cache.
 */
class PurohitRepository(
    private val clientProvider: () -> SupabaseClient = { SupabaseProvider.client }
) {
    private val fallbackProfile = VendorProfileDto(
        id = "purohit-demo-01",
        fullName = "Pt. Ramakant Mishra Shastri",
        phoneNumber = "+91 94150 28471",
        role = "PUROHIT",
        isVerified = true,
        registrationId = "PRY-KMB-2025-0142"
    )

    private val localServicesFlow = MutableStateFlow(
        listOf(
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
    )

    fun getCurrentUserId(): String? {
        return try {
            clientProvider().auth.currentUserOrNull()?.id ?: "purohit-demo-01"
        } catch (e: Exception) {
            "purohit-demo-01"
        }
    }

    suspend fun getProfile(userId: String): VendorProfileDto = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext fallbackProfile.copy(id = userId)
        }
        try {
            clientProvider().from("vendor_profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingle<VendorProfileDto>()
        } catch (e: Exception) {
            fallbackProfile.copy(id = userId)
        }
    }

    fun observeServicesRealtime(userId: String): Flow<List<PurohitServiceDto>> {
        return localServicesFlow.asStateFlow()
    }

    suspend fun addService(service: PurohitServiceDto): Unit = withContext(Dispatchers.IO) {
        val completeService = if (service.id.isNullOrBlank()) {
            service.copy(id = UUID.randomUUID().toString())
        } else {
            service
        }

        val updated = localServicesFlow.value.toMutableList()
        updated.add(completeService)
        localServicesFlow.value = updated

        if (SupabaseProvider.isConfigured()) {
            try {
                clientProvider().from("purohit_services").insert(completeService)
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }

    suspend fun updateService(service: PurohitServiceDto): Unit = withContext(Dispatchers.IO) {
        val updated = localServicesFlow.value.map {
            if (it.id == service.id) service else it
        }
        localServicesFlow.value = updated

        if (SupabaseProvider.isConfigured()) {
            try {
                service.id?.let { sid ->
                    clientProvider().from("purohit_services").update(service) {
                        filter { eq("id", sid) }
                    }
                }
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }

    suspend fun deleteService(serviceId: String): Unit = withContext(Dispatchers.IO) {
        val updated = localServicesFlow.value.filterNot { it.id == serviceId }
        localServicesFlow.value = updated

        if (SupabaseProvider.isConfigured()) {
            try {
                clientProvider().from("purohit_services").delete {
                    filter { eq("id", serviceId) }
                }
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }
}
