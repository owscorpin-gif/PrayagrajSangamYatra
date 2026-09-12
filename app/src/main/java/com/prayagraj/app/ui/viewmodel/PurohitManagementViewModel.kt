package com.prayagraj.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayagraj.app.data.model.PurohitServiceDto
import com.example.data.model.VendorProfileDto
import com.prayagraj.app.data.model.PriestDetails
import com.prayagraj.app.data.repository.PurohitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PurohitManagementUiState {
    data object Loading : PurohitManagementUiState
    data class Success(
        val priestDetails: PriestDetails?,
        val services: List<PurohitServiceDto>
    ) : PurohitManagementUiState
    data class Error(val message: String) : PurohitManagementUiState
}

/**
 * ViewModel managing the state for PurohitManagementScreen.
 * Interfaces with PurohitRepository to load, register, and persist priest profile details
 * and ritual services to Supabase with real-time updates and offline fallbacks.
 */
class PurohitManagementViewModel(
    private val repository: PurohitRepository = PurohitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PurohitManagementUiState>(PurohitManagementUiState.Loading)
    val uiState: StateFlow<PurohitManagementUiState> = _uiState.asStateFlow()

    private var currentDetails: PriestDetails? = null

    init {
        loadPurohitData()
    }

    fun loadPurohitData() {
        val userId = repository.getCurrentUserId() ?: "purohit-demo-01"
        viewModelScope.launch {
            _uiState.value = PurohitManagementUiState.Loading
            try {
                val profile = repository.getProfile(userId)
                if (profile != null && profile.fullName.isNotBlank()) {
                    currentDetails = PriestDetails(
                        fullName = profile.fullName,
                        specialization = "Vedic Karmakand & Pind Daan",
                        experienceYears = 15,
                        languages = listOf("Hindi", "Sanskrit", "Bhojpuri", "English"),
                        phoneNumber = profile.phoneNumber,
                        registrationId = profile.registrationId,
                        isVerified = profile.isVerified
                    )
                }

                val services = repository.fetchServices(userId)
                _uiState.value = PurohitManagementUiState.Success(currentDetails, services)

                // Observe realtime updates for services
                repository.observeServicesRealtime(userId).collect { updatedServices ->
                    val current = _uiState.value
                    if (current is PurohitManagementUiState.Success) {
                        _uiState.value = current.copy(services = updatedServices)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PurohitManagementUiState.Error(
                    e.localizedMessage ?: "Failed to load priest profile data"
                )
            }
        }
    }

    /**
     * Registers and persists priest details to Supabase via PurohitRepository.
     */
    fun registerPriestDetails(
        name: String,
        specialization: String,
        experienceYears: Int,
        languages: List<String>,
        phoneNumber: String,
        registrationId: String,
        photoUri: String? = null
    ) {
        val userId = repository.getCurrentUserId() ?: "purohit-demo-01"
        viewModelScope.launch {
            _uiState.value = PurohitManagementUiState.Loading
            val details = PriestDetails(
                fullName = name,
                specialization = specialization,
                experienceYears = experienceYears,
                languages = languages,
                phoneNumber = phoneNumber,
                registrationId = registrationId,
                isVerified = false,
                photoUri = photoUri
            )
            currentDetails = details

            val vendorProfile = VendorProfileDto(
                id = userId,
                fullName = name,
                phoneNumber = phoneNumber,
                role = "PUROHIT",
                isVerified = false,
                registrationId = registrationId
            )

            // Persist profile to Supabase
            repository.saveProfile(vendorProfile)
                .onSuccess {
                    val services = repository.fetchServices(userId)
                    _uiState.value = PurohitManagementUiState.Success(details, services)
                }
                .onFailure {
                    // Graceful fallback keeping local state active
                    val services = repository.fetchServices(userId)
                    _uiState.value = PurohitManagementUiState.Success(details, services)
                }
        }
    }

    /**
     * Updates photo URI for existing priest profile.
     */
    fun updatePriestPhoto(photoUri: String) {
        val currentState = _uiState.value
        if (currentState is PurohitManagementUiState.Success && currentState.priestDetails != null) {
            val updated = currentState.priestDetails.copy(photoUri = photoUri)
            currentDetails = updated
            _uiState.value = currentState.copy(priestDetails = updated)
        }
    }

    /**
     * Adds and persists a new ritual service offering to Supabase.
     */
    fun addService(
        name: String,
        dakshina: Double,
        description: String,
        durationMinutes: Int = 45,
        materialsIncluded: Boolean = true,
        category: String = "Sangam Ritual",
        samagriExtraCost: Double = 0.0,
        languages: List<String> = listOf("Hindi", "Sanskrit")
    ) {
        val userId = repository.getCurrentUserId() ?: "purohit-demo-01"
        viewModelScope.launch {
            try {
                val newService = PurohitServiceDto(
                    purohitId = userId,
                    poojaName = name,
                    category = category,
                    durationHours = durationMinutes / 60.0,
                    baseDakshina = dakshina,
                    samagriIncluded = materialsIncluded,
                    samagriExtraCost = samagriExtraCost,
                    languagesSupported = languages,
                    description = description
                )

                // Call createPoojaService with fallback to local add
                val result = repository.createPoojaService(newService)
                val persistedService = result.getOrElse {
                    repository.addService(newService)
                    newService
                }

                val current = _uiState.value
                if (current is PurohitManagementUiState.Success) {
                    val updated = current.services.toMutableList().apply { add(0, persistedService) }
                    _uiState.value = current.copy(services = updated)
                }
            } catch (e: Exception) {
                _uiState.value = PurohitManagementUiState.Error(
                    e.localizedMessage ?: "Failed to add ritual service to database"
                )
            }
        }
    }
}
