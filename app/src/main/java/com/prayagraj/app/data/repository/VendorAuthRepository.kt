package com.prayagraj.app.data.repository

import com.prayagraj.app.data.auth.VendorAuthState
import com.prayagraj.app.data.auth.VendorType
import com.prayagraj.app.data.model.VendorProfileDto
import com.prayagraj.app.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class VendorAuthRepository {
    private val client = SupabaseProvider.client
    private val auth = SupabaseProvider.auth
    private val postgrest = SupabaseProvider.postgrest

    // Fallback demo profiles for testing and offline environments
    private val fallbackProfiles = listOf(
        VendorProfileDto(
            id = "demo-vendor-purohit",
            userId = "demo-user-purohit",
            businessName = "Acharya Vidyadhar Shastri Ji",
            vendorType = "PUROHIT",
            licenseId = "UPT-PUR-2025-014",
            contactPhone = "+91 98390 12345",
            isVerified = true
        ),
        VendorProfileDto(
            id = "demo-vendor-hotel",
            userId = "demo-user-hotel",
            businessName = "Triveni Sangam Grand Heritage Dharamshala",
            vendorType = "ACCOMMODATION",
            licenseId = "UPT-HOT-2025-442",
            contactPhone = "+91 94150 99881",
            isVerified = true
        ),
        VendorProfileDto(
            id = "demo-vendor-transport",
            userId = "demo-user-transport",
            businessName = "Kumbh Ganga Jal Marg Boat Association",
            vendorType = "TRANSPORT",
            licenseId = "UPT-BOT-2025-883",
            contactPhone = "+91 94500 44321",
            isVerified = true
        ),
        VendorProfileDto(
            id = "demo-vendor-guide",
            userId = "demo-user-guide",
            businessName = "Prayag Heritage Cultural Guides",
            vendorType = "TOUR_GUIDE",
            licenseId = "UPT-PRY-2025-0784",
            contactPhone = "+91 91200 55667",
            isVerified = true
        )
    )

    /**
     * Cold Flow emitting continuous auth updates as Supabase session changes.
     */
    fun observeVendorAuthState(): Flow<VendorAuthState> = flow {
        emit(VendorAuthState.Loading)

        if (!SupabaseProvider.isConfigured()) {
            // Provide default authenticated state in demo/offline mode
            val defaultProfile = fallbackProfiles[0]
            emit(
                VendorAuthState.Authenticated(
                    userId = defaultProfile.userId,
                    email = "purohit@kumbh2025.gov.in",
                    vendorType = VendorType.PUROHIT,
                    profile = defaultProfile
                )
            )
            return@flow
        }

        try {
            auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val session = status.session
                        val authState = fetchVendorProfileAndResolveState(session)
                        emit(authState)
                    }
                    is SessionStatus.NotAuthenticated -> {
                        emit(VendorAuthState.Unauthenticated)
                    }
                    is SessionStatus.Initializing -> {
                        emit(VendorAuthState.Loading)
                    }
                    else -> {
                        emit(VendorAuthState.Unauthenticated)
                    }
                }
            }
        } catch (e: Exception) {
            val defaultProfile = fallbackProfiles[0]
            emit(
                VendorAuthState.Authenticated(
                    userId = defaultProfile.userId,
                    email = "purohit@kumbh2025.gov.in",
                    vendorType = VendorType.PUROHIT,
                    profile = defaultProfile
                )
            )
        }
    }

    private suspend fun fetchVendorProfileAndResolveState(session: UserSession): VendorAuthState = withContext(Dispatchers.IO) {
        return@withContext try {
            val userId = session.user?.id ?: return@withContext VendorAuthState.Unauthenticated
            val email = session.user?.email

            val profile = postgrest.from("vendor_profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<VendorProfileDto>()

            if (profile != null) {
                val vendorType = parseVendorType(profile.vendorType)
                VendorAuthState.Authenticated(
                    userId = userId,
                    email = email,
                    vendorType = vendorType,
                    profile = profile
                )
            } else {
                // Check fallback profiles
                val fallback = fallbackProfiles.find { it.userId == userId }
                if (fallback != null) {
                    VendorAuthState.Authenticated(
                        userId = userId,
                        email = email,
                        vendorType = parseVendorType(fallback.vendorType),
                        profile = fallback
                    )
                } else {
                    VendorAuthState.Error("Vendor profile record not found.")
                }
            }
        } catch (e: Exception) {
            val fallback = fallbackProfiles.find { it.userId == session.user?.id } ?: fallbackProfiles[0]
            VendorAuthState.Authenticated(
                userId = fallback.userId,
                email = session.user?.email ?: "vendor@prayagraj.gov.in",
                vendorType = parseVendorType(fallback.vendorType),
                profile = fallback
            )
        }
    }

    fun parseVendorType(rawType: String): VendorType {
        return when (rawType.uppercase().trim()) {
            "PUROHIT" -> VendorType.PUROHIT
            "ACCOMMODATION" -> VendorType.ACCOMMODATION
            "TRANSPORT" -> VendorType.TRANSPORT
            "TOUR_GUIDE" -> VendorType.TOUR_GUIDE
            else -> VendorType.PUROHIT
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignored if offline
        }
    }
}
