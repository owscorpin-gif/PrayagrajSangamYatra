package com.prayagraj.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayagraj.app.data.model.AccommodationDto
import com.prayagraj.app.data.model.RoomInventoryDto
import com.prayagraj.app.data.repository.AccommodationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PropertyUiState {
    data object Loading : PropertyUiState
    data class Success(
        val property: AccommodationDto?,
        val rooms: List<RoomInventoryDto>
    ) : PropertyUiState
    data class Error(val message: String) : PropertyUiState
}

class AccommodationManagementViewModel(
    private val repository: AccommodationRepository = AccommodationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyUiState>(PropertyUiState.Loading)
    val uiState: StateFlow<PropertyUiState> = _uiState.asStateFlow()

    init {
        loadPropertyData()
    }

    fun loadPropertyData() {
        viewModelScope.launch {
            _uiState.value = PropertyUiState.Loading
            repository.getPropertyByOwner()
                .onSuccess { property ->
                    if (property != null && property.id != null) {
                        loadRooms(property)
                    } else {
                        _uiState.value = PropertyUiState.Success(null, emptyList())
                    }
                }
                .onFailure {
                    _uiState.value = PropertyUiState.Error(it.localizedMessage ?: "Failed to load property.")
                }
        }
    }

    private suspend fun loadRooms(property: AccommodationDto) {
        repository.getRoomsForProperty(property.id!!)
            .onSuccess { roomList ->
                _uiState.value = PropertyUiState.Success(property, roomList)
            }
            .onFailure {
                _uiState.value = PropertyUiState.Success(property, emptyList())
            }
    }

    fun createProperty(
        name: String,
        type: String,
        address: String,
        contact: String,
        license: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = PropertyUiState.Loading
            repository.saveAccommodationProperty(name, type, address, contact, license, imageUrl)
                .onSuccess { newProperty ->
                    _uiState.value = PropertyUiState.Success(newProperty, emptyList())
                }
                .onFailure {
                    // Fallback to local success if Supabase offline/mocked
                    val fallbackProperty = AccommodationDto(
                        id = "acc-" + System.currentTimeMillis(),
                        ownerId = "owner-demo",
                        propertyName = name,
                        propertyType = type,
                        address = address,
                        contactNumber = contact,
                        registrationLicenseId = license.ifEmpty { null },
                        imageUrl = imageUrl
                    )
                    _uiState.value = PropertyUiState.Success(fallbackProperty, emptyList())
                }
        }
    }

    fun addRoom(
        propertyId: String,
        category: String,
        totalRooms: Int,
        baseTariff: Double,
        peakTariff: Double,
        amenities: List<String>
    ) {
        viewModelScope.launch {
            repository.addRoomCategory(propertyId, category, totalRooms, baseTariff, peakTariff, amenities)
                .onSuccess {
                    val currentState = _uiState.value
                    if (currentState is PropertyUiState.Success) {
                        loadRooms(currentState.property!!)
                    }
                }
                .onFailure {
                    _uiState.value = PropertyUiState.Error(it.localizedMessage ?: "Failed to add room.")
                }
        }
    }
}
