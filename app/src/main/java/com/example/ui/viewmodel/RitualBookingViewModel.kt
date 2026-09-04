package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainApplication
import com.example.data.local.DatabaseModule
import com.example.data.local.RitualBookingEntity
import com.example.data.repository.RitualBookingRepository
import com.example.ui.components.RitualBookingDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class ScheduleFilterTab {
    UPCOMING,
    PAST,
    ALL
}

data class RitualBookingUiState(
    val bookings: List<RitualBookingEntity> = emptyList(),
    val upcomingBookings: List<RitualBookingEntity> = emptyList(),
    val pastBookings: List<RitualBookingEntity> = emptyList(),
    val upcomingCount: Int = 0,
    val selectedTab: ScheduleFilterTab = ScheduleFilterTab.UPCOMING,
    val selectedBookingForDetail: RitualBookingEntity? = null,
    val isLoading: Boolean = false,
    val userFeedbackMessage: String? = null
)

class RitualBookingViewModel @JvmOverloads constructor(
    application: Application = MainApplication.instance,
    private val repository: RitualBookingRepository = DatabaseModule.provideRitualBookingRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RitualBookingUiState(isLoading = true))
    val uiState: StateFlow<RitualBookingUiState> = _uiState.asStateFlow()

    init {
        // Seed default sample bookings if database is empty
        viewModelScope.launch {
            repository.seedSampleBookingsIfEmpty()
        }

        // Observe all bookings from Room database
        repository.allBookings.onEach { allList ->
            val upcoming = allList.filter { it.isUpcoming }
            val past = allList.filter { !it.isUpcoming }
            _uiState.value = _uiState.value.copy(
                bookings = allList,
                upcomingBookings = upcoming,
                pastBookings = past,
                upcomingCount = upcoming.size,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }

    fun selectTab(tab: ScheduleFilterTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectBookingForDetail(booking: RitualBookingEntity?) {
        _uiState.value = _uiState.value.copy(selectedBookingForDetail = booking)
    }

    fun markBookingCompleted(bookingId: String) {
        viewModelScope.launch {
            repository.markCompleted(bookingId)
            _uiState.value = _uiState.value.copy(
                userFeedbackMessage = "Ritual marked as Completed. May blessings be upon you!"
            )
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
            _uiState.value = _uiState.value.copy(
                userFeedbackMessage = "Ritual booking cancelled."
            )
        }
    }

    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            repository.deleteBooking(bookingId)
            if (_uiState.value.selectedBookingForDetail?.id == bookingId) {
                _uiState.value = _uiState.value.copy(selectedBookingForDetail = null)
            }
            _uiState.value = _uiState.value.copy(
                userFeedbackMessage = "Booking record removed from local history."
            )
        }
    }

    fun saveBooking(details: RitualBookingDetails) {
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
            repository.saveBooking(entity)
        }
    }

    fun clearFeedbackMessage() {
        _uiState.value = _uiState.value.copy(userFeedbackMessage = null)
    }
}
