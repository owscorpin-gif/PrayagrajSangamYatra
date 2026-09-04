package com.prayagraj.app.data.auth

import com.prayagraj.app.data.model.VendorProfileDto

enum class VendorType {
    PUROHIT,
    ACCOMMODATION,
    TRANSPORT,
    TOUR_GUIDE
}

sealed interface VendorAuthState {
    data object Loading : VendorAuthState
    data object Unauthenticated : VendorAuthState
    data class Authenticated(
        val userId: String,
        val email: String?,
        val vendorType: VendorType,
        val profile: VendorProfileDto
    ) : VendorAuthState
    data class Error(val message: String) : VendorAuthState
}
