package com.prayagraj.app.data.repository

import com.example.data.remote.SupabaseProvider
import com.prayagraj.app.data.model.TourGuideProfileDto
import com.prayagraj.app.data.model.TourPackageDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TourRepository {
    private val client = SupabaseProvider.client

    // Seeded offline/fallback sample guide profiles
    private val localProfiles = mutableListOf(
        TourGuideProfileDto(
            id = "guide-prof-01",
            guideId = "guide-demo-01",
            licenseNumber = "UPT-PRY-2025-0784",
            experienceYears = 8,
            languagesSpoken = listOf("Hindi", "English", "Sanskrit", "Bengali"),
            badgeLevel = "UP Tourism Gold Approved",
            isVerified = true
        )
    )

    // Seeded offline/fallback sample tour packages
    private val localPackages = mutableListOf(
        TourPackageDto(
            id = "pkg-01",
            guideProfileId = "guide-prof-01",
            packageTitle = "Triveni Sangam Sacred Sunrise & Snan Parikrama",
            durationHours = 3,
            maxGroupSize = 10,
            pricePerPerson = 650.0,
            includedServices = listOf("Boat Transfer Assistance", "Sankalpa Puja Priest Coordination", "Historical Narration", "Life Jackets"),
            itineraryHighlights = "Kila Ghat assembly -> Confluence Boat Cruise -> Snan Ritual Guidance -> Akshayavat Darshan",
            isActive = true
        ),
        TourPackageDto(
            id = "pkg-02",
            guideProfileId = "guide-prof-01",
            packageTitle = "Akharas Heritage & Naga Sadhus Cultural Trail",
            durationHours = 4,
            maxGroupSize = 8,
            pricePerPerson = 1100.0,
            includedServices = listOf("Akhara Entry Facilitation", "Spiritual Discourse Translation", "Prasadam"),
            itineraryHighlights = "Juna Akhara Sector -> Niranjani Akhara Camp -> Evening Ganga Aarti at VIP Ghat",
            isActive = true
        ),
        TourPackageDto(
            id = "pkg-03",
            guideProfileId = "guide-prof-01",
            packageTitle = "Sacred Temples & Fort Heritage Walking Tour",
            durationHours = 2,
            maxGroupSize = 15,
            pricePerPerson = 450.0,
            includedServices = listOf("Audio Guide Support", "Bottled Water", "Temple Fast-Track Tips"),
            itineraryHighlights = "Bandhwa Bade Hanuman Ji -> Patalpuri Temple inside Fort -> Saraswati Ghat Sunset",
            isActive = true
        )
    )

    fun getCurrentUserId(): String? = try {
        client.auth.currentUserOrNull()?.id ?: "guide-demo-01"
    } catch (e: Exception) {
        "guide-demo-01"
    }

    suspend fun getGuideProfile(userId: String): TourGuideProfileDto? = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext localProfiles.find { it.guideId == userId || userId == "guide-demo-01" }
        }
        try {
            val remote = client.from("tour_guides")
                .select { filter { eq("guide_id", userId) } }
                .decodeSingleOrNull<TourGuideProfileDto>()
            remote ?: localProfiles.find { it.guideId == userId || userId == "guide-demo-01" }
        } catch (e: Exception) {
            localProfiles.find { it.guideId == userId || userId == "guide-demo-01" }
        }
    }

    suspend fun getPackagesForGuide(guideProfileId: String): List<TourPackageDto> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext localPackages.filter { it.guideProfileId == guideProfileId }.toList()
        }
        try {
            val remote = client.from("tour_packages")
                .select { filter { eq("guide_profile_id", guideProfileId) } }
                .decodeList<TourPackageDto>()
            if (remote.isNotEmpty()) remote else localPackages.filter { it.guideProfileId == guideProfileId }.toList()
        } catch (e: Exception) {
            localPackages.filter { it.guideProfileId == guideProfileId }.toList()
        }
    }

    suspend fun addTourPackage(tourPackage: TourPackageDto) = withContext(Dispatchers.IO) {
        val complete = if (tourPackage.id.isNullOrBlank()) {
            tourPackage.copy(id = UUID.randomUUID().toString())
        } else {
            tourPackage
        }
        localPackages.add(complete)

        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("tour_packages").insert(complete)
            } catch (e: Exception) {
                // Maintained locally
            }
        }
    }

    suspend fun togglePackageStatus(packageId: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        val index = localPackages.indexOfFirst { it.id == packageId }
        if (index != -1) {
            localPackages[index] = localPackages[index].copy(isActive = isActive)
        }

        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("tour_packages").update({
                    set("is_active", isActive)
                }) {
                    filter { eq("id", packageId) }
                }
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }
}
