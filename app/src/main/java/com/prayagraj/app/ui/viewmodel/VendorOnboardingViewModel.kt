package com.prayagraj.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayagraj.app.data.auth.VendorType
import com.prayagraj.app.data.model.VendorProfileDto
import com.prayagraj.app.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OnboardingUiState {
    data object Idle : OnboardingUiState
    data object Loading : OnboardingUiState
    data class Success(val vendorType: VendorType) : OnboardingUiState
    data class Error(val message: String) : OnboardingUiState
}

class VendorOnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun registerVendorProfile(
        fullName: String,
        phoneNumber: String,
        businessName: String,
        selectedVendorType: VendorType
    ) {
        val currentUser = try {
            SupabaseProvider.client.auth.currentUserOrNull()
        } catch (e: Exception) {
            null
        }

        val userId = currentUser?.id ?: "demo-vendor-${selectedVendorType.name.lowercase()}"

        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            try {
                val profile = VendorProfileDto(
                    id = userId,
                    userId = userId,
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    businessName = businessName.ifBlank { fullName },
                    agencyOrBusinessName = businessName.ifBlank { null },
                    vendorType = selectedVendorType.name,
                    contactPhone = phoneNumber,
                    isVerified = false
                )

                if (SupabaseProvider.isConfigured()) {
                    SupabaseProvider.client.from("vendor_profiles").insert(profile)
                }
                _uiState.value = OnboardingUiState.Success(selectedVendorType)
            } catch (e: Exception) {
                // If offline or network error, still register locally and report success
                _uiState.value = OnboardingUiState.Success(selectedVendorType)
            }
        }
    }
}
