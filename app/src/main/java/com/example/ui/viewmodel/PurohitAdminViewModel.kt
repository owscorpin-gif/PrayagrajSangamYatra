package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PurohitServiceDto
import com.example.data.model.VendorProfileDto
import com.example.data.repository.PurohitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PurohitUiState(
    val isLoading: Boolean = true,
    val profile: VendorProfileDto? = null,
    val services: List<PurohitServiceDto> = emptyList(),
    val errorMessage: String? = null
)

class PurohitAdminViewModel(
    private val repository: PurohitRepository = PurohitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurohitUiState())
    val uiState: StateFlow<PurohitUiState> = _uiState

    init {
        loadPurohitData()
    }

    fun loadPurohitData() {
        val userId = repository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val profile = repository.getProfile(userId)
                _uiState.value = _uiState.value.copy(profile = profile, isLoading = false)
                
                // Observe Realtime database table changes
                repository.observeServicesRealtime(userId).collect { updatedServices ->
                    _uiState.value = _uiState.value.copy(services = updatedServices)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load data"
                )
            }
        }
    }

    fun addService(name: String, dakshina: Double, description: String, materialsIncluded: Boolean = true, durationMinutes: Int = 45) {
        val userId = repository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val newService = PurohitServiceDto(
                    purohitId = userId,
                    ritualName = name,
                    fixedDakshina = dakshina,
                    description = description,
                    materialsIncluded = materialsIncluded,
                    durationMinutes = durationMinutes
                )
                repository.addService(newService)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun updateService(service: PurohitServiceDto) {
        viewModelScope.launch {
            try {
                repository.updateService(service)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteService(serviceId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
