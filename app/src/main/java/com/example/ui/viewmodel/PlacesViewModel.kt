package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.NearbyPlace
import com.example.data.model.Place
import com.example.data.repository.PlacesRepository
import com.example.data.repository.PlacesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Places Explorer and Itinerary Builder.
 */
data class PlacesUiState(
    val isLoading: Boolean = false,
    val selectedCategory: String = "all", // "all", "ghat", "temple", "heritage"
    val searchQuery: String = "",
    val isNearbyMode: Boolean = false,
    val userLat: Double = 25.4358, // Prayagraj Junction / Civil Lines default GPS
    val userLng: Double = 81.8463,
    val radiusMeters: Double = 5000.0,
    val allPlaces: List<Place> = emptyList(),
    val filteredPlaces: List<Place> = emptyList(),
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val itineraryCart: List<Place> = emptyList(),
    val errorMessage: String? = null,
    val selectedPlaceForDetails: Place? = null
)

/**
 * ViewModel managing Supabase master places catalog, PostGIS nearby RPC execution, and Itinerary cart.
 */
class PlacesViewModel(
    private val repository: PlacesRepository = PlacesRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()

    init {
        loadPlaces()
    }

    /**
     * Loads the master places list from Supabase Postgrest.
     */
    fun loadPlaces(category: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val cat = category ?: _uiState.value.selectedCategory
            val result = repository.getPlaces(if (cat == "all") null else cat)
            
            result.onSuccess { places ->
                _uiState.update { state ->
                    val filtered = filterByQuery(places, state.searchQuery)
                    state.copy(
                        isLoading = false,
                        allPlaces = places,
                        filteredPlaces = filtered
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load places"
                    )
                }
            }
        }
    }

    /**
     * Executes the PostGIS RPC function `nearby_places` with user latitude, longitude, and radius.
     */
    fun fetchNearbyPlaces(lat: Double, lng: Double, radiusMeters: Double = 5000.0) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    errorMessage = null,
                    userLat = lat,
                    userLng = lng,
                    radiusMeters = radiusMeters,
                    isNearbyMode = true
                ) 
            }
            val result = repository.getNearbyPlaces(lat, lng, radiusMeters)
            result.onSuccess { nearby ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nearbyPlaces = nearby
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to query nearby places"
                    )
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadPlaces(category)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = filterByQuery(state.allPlaces, query)
            state.copy(searchQuery = query, filteredPlaces = filtered)
        }
    }

    fun toggleNearbyMode(enabled: Boolean) {
        _uiState.update { it.copy(isNearbyMode = enabled) }
        if (enabled) {
            val state = _uiState.value
            fetchNearbyPlaces(state.userLat, state.userLng, state.radiusMeters)
        }
    }

    fun updateRadius(radiusMeters: Double) {
        _uiState.update { it.copy(radiusMeters = radiusMeters) }
        val state = _uiState.value
        fetchNearbyPlaces(state.userLat, state.userLng, radiusMeters)
    }

    fun addToItinerary(place: Place) {
        _uiState.update { state ->
            if (state.itineraryCart.none { it.id == place.id }) {
                state.copy(itineraryCart = state.itineraryCart + place)
            } else state
        }
    }

    fun removeFromItinerary(placeId: String) {
        _uiState.update { state ->
            state.copy(itineraryCart = state.itineraryCart.filter { it.id != placeId })
        }
    }

    fun clearItinerary() {
        _uiState.update { it.copy(itineraryCart = emptyList()) }
    }

    fun selectPlaceDetails(place: Place?) {
        _uiState.update { it.copy(selectedPlaceForDetails = place) }
    }

    private fun filterByQuery(places: List<Place>, query: String): List<Place> {
        if (query.isBlank()) return places
        val q = query.trim().lowercase()
        return places.filter {
            it.name.lowercase().contains(q) ||
            (it.hindiName?.lowercase()?.contains(q) == true) ||
            it.category.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }
}
