package com.example.ui.viewmodel

import android.content.Context
import android.location.Location
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainApplication
import com.example.data.model.NearbyPlace
import com.example.data.model.Place
import com.example.data.model.UserProfile
import com.example.data.remote.ConnectivityObserver
import com.example.data.remote.NetworkConnectivityObserver
import com.example.data.remote.NetworkStatus
import com.example.data.repository.AuthRepository
import com.example.data.repository.ItineraryManager
import com.example.data.repository.OfflineFirstPlacesRepository
import com.example.data.repository.PlacesRepository
import com.example.data.repository.PlacesRepositoryImpl
import com.example.data.util.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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
class PlacesViewModel @JvmOverloads constructor(
    private val repository: OfflineFirstPlacesRepository = PlacesRepositoryImpl(),
    private val connectivityObserver: ConnectivityObserver = try {
        NetworkConnectivityObserver(MainApplication.instance)
    } catch (e: Throwable) {
        object : ConnectivityObserver {
            override fun observe() = flowOf(NetworkStatus.Available)
        }
    },
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val networkStatus: StateFlow<NetworkStatus> = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus.Unavailable
        )

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()

    private val _userPhone = MutableStateFlow<String?>(null)
    val userPhone: StateFlow<String?> = _userPhone.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Dialog state management
    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places.asStateFlow()

    private val _isLoadingPlaces = MutableStateFlow(true)
    val isLoadingPlaces: StateFlow<Boolean> = _isLoadingPlaces.asStateFlow()

    val selectedPlaces: StateFlow<List<Place>> = ItineraryManager.selectedPlaces

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _sortByProximity = MutableStateFlow(false)
    val sortByProximity: StateFlow<Boolean> = _sortByProximity.asStateFlow()

    private val _placeRatings = MutableStateFlow<Map<String, Int>>(emptyMap())
    val placeRatings: StateFlow<Map<String, Int>> = _placeRatings.asStateFlow()

    fun setPlaceRating(placeId: String, rating: Int) {
        _placeRatings.update { currentMap ->
            val updated = currentMap.toMutableMap()
            if (rating <= 0) {
                updated.remove(placeId)
            } else {
                updated[placeId] = rating.coerceIn(1, 5)
            }
            updated
        }
    }

    fun toggleItinerarySelection(place: Place) {
        ItineraryManager.togglePlaceSelection(place)
        _uiState.update { it.copy(itineraryCart = ItineraryManager.selectedPlaces.value) }
    }

    /**
     * Calculates distance between two coordinates in meters using the Haversine formula:
     * a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
     * c = 2 * atan2(√a, √(1−a))
     * d = R * c
     */
    fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val originLatRad = Math.toRadians(lat1)
        val destLatRad = Math.toRadians(lat2)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(originLatRad) * Math.cos(destLatRad) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusMeters * c
    }

    /**
     * Sorts a list of places based on proximity to the specified user coordinates using the Haversine formula.
     */
    fun sortPlacesByProximity(
        userLat: Double,
        userLng: Double,
        placesList: List<Place> = _places.value
    ): List<Place> {
        return placesList.sortedBy { place ->
            calculateHaversineDistance(
                lat1 = userLat,
                lon1 = userLng,
                lat2 = place.latitude,
                lon2 = place.longitude
            )
        }
    }

    /**
     * Calculates distance in meters from the user's current location to a given place using Haversine.
     */
    fun getDistanceToPlace(place: Place): Float? {
        val loc = _userLocation.value ?: return null
        return calculateHaversineDistance(
            lat1 = loc.latitude,
            lon1 = loc.longitude,
            lat2 = place.latitude,
            lon2 = place.longitude
        ).toFloat()
    }

    /**
     * Formats distance to a human-readable string (e.g., "450 m", "2.3 km").
     */
    fun getFormattedDistance(place: Place): String? {
        val dist = getDistanceToPlace(place) ?: return null
        return LocationUtils.formatDistance(dist)
    }

    /**
     * Fetches the user's current GPS location via Google Play Services Fused Location.
     */
    fun fetchCurrentLocation(context: Context, enableSort: Boolean = false) {
        viewModelScope.launch {
            _isLocating.value = true
            try {
                val loc = LocationUtils.getCurrentLocation(context)
                if (loc != null) {
                    _userLocation.value = loc
                    _uiState.update { it.copy(userLat = loc.latitude, userLng = loc.longitude) }
                    if (enableSort) {
                        _sortByProximity.value = true
                    }
                    applySortingAndFiltering()
                }
            } catch (e: Exception) {
                // Ignore or log location fetch failure
            } finally {
                _isLocating.value = false
            }
        }
    }

    /**
     * Toggles sorting places list by proximity to user's location.
     */
    fun toggleSortByProximity(context: Context) {
        if (!_sortByProximity.value) {
            if (_userLocation.value == null) {
                fetchCurrentLocation(context, enableSort = true)
            } else {
                _sortByProximity.value = true
                applySortingAndFiltering()
            }
        } else {
            _sortByProximity.value = false
            applySortingAndFiltering()
        }
    }

    private fun applySortingAndFiltering() {
        val rawPlaces = _uiState.value.allPlaces.ifEmpty { _places.value }
        val category = _uiState.value.selectedCategory
        val categoryFiltered = when (category.lowercase()) {
            "all", "" -> rawPlaces
            "temple", "temples" -> rawPlaces.filter { it.category.equals("temple", ignoreCase = true) }
            "ghat", "riverside", "riversides" -> rawPlaces.filter {
                it.category.equals("ghat", ignoreCase = true) ||
                it.tags.any { tag -> tag.contains("boat", true) || tag.contains("ghat", true) || tag.contains("aarti", true) }
            }
            "heritage" -> rawPlaces.filter {
                it.category.equals("heritage", ignoreCase = true) ||
                it.tags.any { tag -> tag.contains("heritage", true) || tag.contains("museum", true) || tag.contains("park", true) }
            }
            else -> rawPlaces.filter { it.category.equals(category, ignoreCase = true) }
        }
        val filtered = filterByQuery(categoryFiltered, _uiState.value.searchQuery)
        val loc = _userLocation.value

        val finalPlaces = if (_sortByProximity.value && loc != null) {
            sortPlacesByProximity(
                userLat = loc.latitude,
                userLng = loc.longitude,
                placesList = filtered
            )
        } else {
            filtered
        }

        _places.value = finalPlaces
        _uiState.update { it.copy(filteredPlaces = finalPlaces) }
    }

    init {
        loadUserProfile()
        loadProfile()
        loadPlaces()
        observeNetworkAndSync()
    }

    private fun observeNetworkAndSync() {
        viewModelScope.launch {
            networkStatus.collectLatest { status ->
                if (status == NetworkStatus.Available) {
                    // Trigger Room cache update from remote Firebase / Supabase / REST API when back online
                    syncRemoteDataToRoom()
                }
            }
        }
    }

    private suspend fun syncRemoteDataToRoom() {
        try {
            // Fetch remote updates and update Room database
            val synced = repository.fetchAndSyncRemotePlaces()
            if (synced.isNotEmpty() && _uiState.value.allPlaces.isEmpty()) {
                _uiState.update { it.copy(allPlaces = synced) }
                applySortingAndFiltering()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads the current authenticated or demo user phone number.
     */
    fun loadUserProfile() {
        val phone = authRepository.getCurrentUserPhone()
        _userPhone.value = phone
    }

    /**
     * Loads the full user profile including name and phone.
     */
    fun loadProfile() {
        viewModelScope.launch {
            val profile = authRepository.getProfile()
            _userProfile.value = profile
            if (profile?.phoneNumber != null) {
                _userPhone.value = profile.phoneNumber
            }
        }
    }

    fun openEditDialog() {
        _isEditDialogOpen.value = true
    }

    fun closeEditDialog() {
        _isEditDialogOpen.value = false
    }

    fun saveProfile(newName: String, newEmail: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val emailParam = newEmail.ifBlank { null }
                authRepository.updateProfile(fullName = newName, email = emailParam)
                loadProfile() // Refresh local profile state
                _isEditDialogOpen.value = false
            } catch (e: Exception) {
                // Handle or log save error
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun uploadProfilePicture(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            try {
                authRepository.uploadAvatar(context, uri)
                loadProfile() // Reload profile to update UI with new image URL
            } catch (e: Exception) {
                // Handle upload failure
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    /**
     * Updates the user's display name and reloads the profile.
     */
    fun updateName(newName: String) {
        viewModelScope.launch {
            authRepository.updateProfile(fullName = newName, email = null)
            loadProfile()
        }
    }

    /**
     * Loads the master places list from Supabase Postgrest.
     */
    fun loadPlaces(category: String? = null) {
        viewModelScope.launch {
            _isLoadingPlaces.value = true
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val cat = category ?: _uiState.value.selectedCategory
            val result = repository.getPlaces(if (cat == "all") null else cat)
            
            result.onSuccess { placesList ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        allPlaces = placesList
                    )
                }
                applySortingAndFiltering()
            }.onFailure { error ->
                try {
                    val directPlaces = authRepository.getPlaces()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allPlaces = directPlaces
                        )
                    }
                    applySortingAndFiltering()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to load places"
                        )
                    }
                }
            }
            _isLoadingPlaces.value = false
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
        ItineraryManager.addPlace(place)
        _uiState.update { state ->
            if (state.itineraryCart.none { it.id == place.id }) {
                state.copy(itineraryCart = state.itineraryCart + place)
            } else state
        }
    }

    fun removeFromItinerary(placeId: String) {
        ItineraryManager.removePlace(placeId)
        _uiState.update { state ->
            state.copy(itineraryCart = state.itineraryCart.filter { it.id != placeId })
        }
    }

    fun clearItinerary() {
        ItineraryManager.clearItinerary()
        _uiState.update { it.copy(itineraryCart = emptyList()) }
    }

    fun selectPlaceDetails(place: Place?) {
        _uiState.update { it.copy(selectedPlaceForDetails = place) }
    }

    /**
     * Terminates the current authentication session and executes the navigation callback.
     */
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                onSuccess()
            } catch (e: Exception) {
                // Navigate away even if network call encounters issues
                onSuccess()
            }
        }
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
