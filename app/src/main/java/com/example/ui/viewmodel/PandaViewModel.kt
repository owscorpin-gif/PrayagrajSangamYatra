package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainApplication
import com.example.data.local.DatabaseModule
import com.example.data.local.RitualBookingEntity
import com.example.data.model.PandaServiceOffering
import com.example.data.model.VerifiedPanda
import com.example.data.repository.PandaRepository
import com.example.data.repository.PandaRepositoryImpl
import com.example.data.repository.RitualBookingRepository
import com.example.ui.components.RitualBookingDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PandaDirectoryUiState(
    val searchQuery: String = "",
    val selectedRitualCategory: String = "All Rituals",
    val selectedGhat: String = "all",
    val selectedLanguage: String = "All",
    val bahiKhataOnly: Boolean = false,
    val sortBy: String = "rating", // "rating", "experience", "reviews", "name"
    val minRating: Double = 0.0,
    val pandas: List<VerifiedPanda> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPandaForDetails: VerifiedPanda? = null,
    val bookingPanda: VerifiedPanda? = null,
    val selectedServiceForBooking: PandaServiceOffering? = null,
    val lastConfirmedBookingCode: String? = null,
    val isFilterSheetOpen: Boolean = false,
    val isScheduleHistorySheetOpen: Boolean = false,
    val upcomingBookingsCount: Int = 0
)

class PandaViewModel(
    application: Application = MainApplication.instance,
    private val repository: PandaRepository = PandaRepositoryImpl(),
    private val ritualBookingRepository: RitualBookingRepository = DatabaseModule.provideRitualBookingRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PandaDirectoryUiState(isLoading = true))
    val uiState: StateFlow<PandaDirectoryUiState> = _uiState.asStateFlow()

    val availableRitualCategories: List<String> = repository.getAvailableRitualCategories()
    val availableGhats: List<Pair<String, String>> = repository.getAvailableGhats()
    val availableLanguages: List<String> = repository.getAvailableLanguages()

    init {
        refreshPandas()
        // Seed sample bookings if empty
        viewModelScope.launch {
            ritualBookingRepository.seedSampleBookingsIfEmpty()
        }
        // Observe upcoming bookings count for badges
        ritualBookingRepository.upcomingCount.onEach { count ->
            _uiState.value = _uiState.value.copy(upcomingBookingsCount = count)
        }.launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshPandas()
    }

    fun selectRitualCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedRitualCategory = category)
        refreshPandas()
    }

    fun selectGhat(ghat: String) {
        _uiState.value = _uiState.value.copy(selectedGhat = ghat)
        refreshPandas()
    }

    fun selectLanguage(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
        refreshPandas()
    }

    fun toggleBahiKhataOnly(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(bahiKhataOnly = enabled)
        refreshPandas()
    }

    fun setSortBy(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        refreshPandas()
    }

    fun setMinRating(rating: Double) {
        _uiState.value = _uiState.value.copy(minRating = rating)
        refreshPandas()
    }

    fun toggleFilterSheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isFilterSheetOpen = open)
    }

    fun toggleScheduleHistorySheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isScheduleHistorySheetOpen = open)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedRitualCategory = "All Rituals",
            selectedGhat = "all",
            selectedLanguage = "All",
            bahiKhataOnly = false,
            minRating = 0.0,
            sortBy = "rating"
        )
        refreshPandas()
    }

    fun selectPandaForDetails(panda: VerifiedPanda?) {
        _uiState.value = _uiState.value.copy(selectedPandaForDetails = panda)
    }

    fun startBooking(panda: VerifiedPanda, service: PandaServiceOffering? = null) {
        _uiState.value = _uiState.value.copy(
            bookingPanda = panda,
            selectedServiceForBooking = service ?: panda.services.firstOrNull()
        )
    }

    fun dismissBooking() {
        _uiState.value = _uiState.value.copy(
            bookingPanda = null,
            selectedServiceForBooking = null
        )
    }

    fun recordRitualBooking(details: RitualBookingDetails) {
        viewModelScope.launch {
            val entity = RitualBookingEntity(
                id = details.bookingId,
                bookingCode = "PRY-PUROHIT-${details.bookingId.takeLast(4)}",
                pandaId = details.pandaId,
                pandaName = details.pandaName,
                pandaHindiName = "",
                pandaPhone = details.pilgrimPhone,
                ghatLocation = details.ghatLocation,
                ritualTitle = details.ritualTitle,
                ritualHindiTitle = "",
                ritualCategory = "Snan & Sankalp",
                bookingDate = details.selectedDate,
                timeSlot = details.selectedTimeSlot,
                pilgrimName = details.pilgrimName,
                pilgrimPhone = details.pilgrimPhone,
                gotra = details.gotra,
                numPersons = details.numPersons,
                estimatedDakshina = details.estimatedDakshina,
                samagriRequired = details.samagriRequired,
                specialNotes = details.specialNotes,
                bookingTimestamp = System.currentTimeMillis(),
                status = "CONFIRMED"
            )
            ritualBookingRepository.saveBooking(entity)
        }
    }

    fun confirmBooking(
        panda: VerifiedPanda,
        service: PandaServiceOffering,
        bookingDate: String,
        timeSlot: String,
        devoteeCount: Int,
        notes: String
    ) {
        val randomDigits = (1000..9999).random()
        val code = "PRY-PUROHIT-$randomDigits"
        val bookingId = "PY-$randomDigits"

        viewModelScope.launch {
            val entity = RitualBookingEntity(
                id = bookingId,
                bookingCode = code,
                pandaId = panda.id,
                pandaName = panda.name,
                pandaHindiName = panda.hindiName,
                pandaPhone = panda.phoneNumber,
                ghatLocation = panda.ghatLocation,
                ritualTitle = service.title,
                ritualHindiTitle = service.hindiTitle ?: "",
                ritualCategory = service.category,
                bookingDate = bookingDate,
                timeSlot = timeSlot,
                pilgrimName = "Devotee",
                pilgrimPhone = panda.phoneNumber,
                gotra = "Kashyapa",
                numPersons = devoteeCount,
                estimatedDakshina = service.dakshinaGuide,
                samagriRequired = service.samagriIncluded,
                specialNotes = notes,
                bookingTimestamp = System.currentTimeMillis(),
                status = "CONFIRMED"
            )
            ritualBookingRepository.saveBooking(entity)
        }

        _uiState.value = _uiState.value.copy(
            bookingPanda = null,
            selectedServiceForBooking = null,
            lastConfirmedBookingCode = code
        )
    }

    fun dismissSuccessConfirmation() {
        _uiState.value = _uiState.value.copy(lastConfirmedBookingCode = null)
    }

    private fun refreshPandas() {
        val currentState = _uiState.value
        val ritualCategory = if (currentState.selectedRitualCategory == "All Rituals") null else currentState.selectedRitualCategory
        val ghatCategory = if (currentState.selectedGhat == "all") null else currentState.selectedGhat
        val language = if (currentState.selectedLanguage == "All") null else currentState.selectedLanguage

        repository.filterPandas(
            query = currentState.searchQuery,
            ritualCategory = ritualCategory,
            ghatCategory = ghatCategory,
            language = language,
            bahiKhataOnly = currentState.bahiKhataOnly,
            minRating = currentState.minRating,
            sortBy = currentState.sortBy
        ).onEach { filteredList ->
            _uiState.value = _uiState.value.copy(
                pandas = filteredList,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }
}
