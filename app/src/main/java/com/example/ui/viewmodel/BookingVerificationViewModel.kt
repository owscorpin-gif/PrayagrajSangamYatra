package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainApplication
import com.example.data.local.DatabaseModule
import com.example.data.model.OfficialBookingRecord
import com.example.data.model.VerificationHistoryItem
import com.example.data.model.VerificationStatus
import com.example.data.repository.BookingVerificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class VerificationMode {
    ENTER_ID,
    SCAN_QR,
    RECENT_HISTORY
}

enum class HistoryFilter(val label: String, val icon: String) {
    ALL("All Checks", "📋"),
    VERIFIED_ONLY("Official Verified", "🛡️"),
    FRAUD_ALERTS("Fraud Alerts", "⚠️"),
    EXPIRED_ONLY("Expired", "⏳")
}

data class BookingVerificationUiState(
    val inputBookingId: String = "",
    val activeMode: VerificationMode = VerificationMode.ENTER_ID,
    val isVerifying: Boolean = false,
    val verificationResult: OfficialBookingRecord? = null,
    val historyList: List<VerificationHistoryItem> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val isFlashOn: Boolean = false,
    val isCameraScanning: Boolean = true,
    val userMessage: String? = null
) {
    val filteredHistoryList: List<VerificationHistoryItem>
        get() = when (selectedFilter) {
            HistoryFilter.ALL -> historyList
            HistoryFilter.VERIFIED_ONLY -> historyList.filter { it.status == VerificationStatus.OFFICIALLY_VERIFIED }
            HistoryFilter.FRAUD_ALERTS -> historyList.filter {
                it.status == VerificationStatus.FLAGGED_BLACKLISTED_TOUT ||
                it.status == VerificationStatus.SUSPICIOUS_UNREGISTERED_FRAUD ||
                it.isBlacklisted
            }
            HistoryFilter.EXPIRED_ONLY -> historyList.filter {
                it.status == VerificationStatus.EXPIRED || it.status == VerificationStatus.CANCELLED
            }
        }
}

class BookingVerificationViewModel @JvmOverloads constructor(
    application: Application = MainApplication.instance,
    private val repository: BookingVerificationRepository = DatabaseModule.provideBookingVerificationRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BookingVerificationUiState())
    val uiState: StateFlow<BookingVerificationUiState> = _uiState.asStateFlow()

    init {
        // Observe verification history from Room database Flow
        repository.verificationHistory.onEach { history ->
            _uiState.value = _uiState.value.copy(historyList = history)
        }.launchIn(viewModelScope)
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputBookingId = text)
    }

    fun switchMode(mode: VerificationMode) {
        _uiState.value = _uiState.value.copy(activeMode = mode)
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(isFlashOn = !_uiState.value.isFlashOn)
    }

    fun toggleCameraScanning(active: Boolean) {
        _uiState.value = _uiState.value.copy(isCameraScanning = active)
    }

    fun setSampleId(sampleId: String) {
        _uiState.value = _uiState.value.copy(inputBookingId = sampleId)
        verifyEnteredId(sampleId)
    }

    /**
     * Loads a previous validation result directly from Room storage or registry
     * allowing instant access to full security certificate without network delay.
     */
    fun selectHistoryItem(item: VerificationHistoryItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                inputBookingId = item.bookingId,
                isVerifying = true
            )
            val fullRecord = repository.getFullHistoryRecord(item.bookingId) ?: repository.verifyBookingId(item.bookingId)
            _uiState.value = _uiState.value.copy(
                isVerifying = false,
                verificationResult = fullRecord,
                userMessage = "Loaded certificate for ${item.bookingId}"
            )
        }
    }

    fun deleteHistoryItem(bookingId: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(bookingId)
            _uiState.value = _uiState.value.copy(userMessage = "Removed $bookingId from history.")
        }
    }

    fun verifyEnteredId(idOverride: String? = null) {
        val query = idOverride ?: _uiState.value.inputBookingId
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(userMessage = "Please enter a Booking ID or Reservation Code.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, userMessage = null)
            // Realistic verification latency to simulate secure cryptographic hash lookup against Govt registry
            delay(350)
            val result = repository.verifyBookingId(query)
            _uiState.value = _uiState.value.copy(
                isVerifying = false,
                verificationResult = result
            )
        }
    }

    fun verifyQrPayload(rawPayload: String) {
        if (rawPayload.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, isCameraScanning = false)
            delay(400)
            val result = repository.parseAndVerifyQrPayload(rawPayload)
            _uiState.value = _uiState.value.copy(
                isVerifying = false,
                verificationResult = result,
                inputBookingId = result.bookingId,
                activeMode = VerificationMode.ENTER_ID
            )
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            verificationResult = null,
            userMessage = null,
            isCameraScanning = true
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(userMessage = "Verification history cleared.")
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }
}

