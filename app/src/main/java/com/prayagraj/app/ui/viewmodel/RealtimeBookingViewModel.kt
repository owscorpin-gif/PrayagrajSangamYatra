package com.prayagraj.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayagraj.app.data.model.BookingRequestDto
import com.prayagraj.app.data.repository.RealtimeBookingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BookingDashboardUiState {
    data object Loading : BookingDashboardUiState
    data class Success(val bookings: List<BookingRequestDto>) : BookingDashboardUiState
    data class Error(val message: String) : BookingDashboardUiState
}

class RealtimeBookingViewModel(
    private val repository: RealtimeBookingRepository = RealtimeBookingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingDashboardUiState>(BookingDashboardUiState.Loading)
    val uiState: StateFlow<BookingDashboardUiState> = _uiState.asStateFlow()

    private val _newBookingAlert = MutableSharedFlow<BookingRequestDto>()
    val newBookingAlert: SharedFlow<BookingRequestDto> = _newBookingAlert.asSharedFlow()

    init {
        startRealtimeSubscription()
    }

    private fun startRealtimeSubscription() {
        viewModelScope.launch {
            var previousCount = -1
            repository.observeBookingRequests().collect { list ->
                // Trigger alert event if a new record is added after initial load
                if (previousCount in 0..<list.size) {
                    _newBookingAlert.emit(list.first())
                }
                previousCount = list.size
                _uiState.value = BookingDashboardUiState.Success(list)
            }
        }
    }

    fun respondToBooking(bookingId: String, accept: Boolean) {
        val status = if (accept) "ACCEPTED" else "REJECTED"
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
                .onSuccess {
                    val currentState = _uiState.value
                    if (currentState is BookingDashboardUiState.Success) {
                        val updatedList = currentState.bookings.map {
                            if (it.id == bookingId) it.copy(status = status) else it
                        }
                        _uiState.value = BookingDashboardUiState.Success(updatedList)
                    }
                }
        }
    }
}
