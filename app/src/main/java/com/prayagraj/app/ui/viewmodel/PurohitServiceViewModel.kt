package com.prayagraj.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayagraj.app.data.local.PurohitFormDraft
import com.prayagraj.app.data.local.PurohitFormDraftStore
import com.prayagraj.app.data.model.PurohitServiceDto
import com.prayagraj.app.data.repository.PurohitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PurohitFormUiState {
    data object Idle : PurohitFormUiState
    data object Submitting : PurohitFormUiState
    data object Success : PurohitFormUiState
    data class Error(val message: String) : PurohitFormUiState
}

class PurohitServiceViewModel(
    private val repository: PurohitRepository = PurohitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PurohitFormUiState>(PurohitFormUiState.Idle)
    val uiState: StateFlow<PurohitFormUiState> = _uiState.asStateFlow()

    val draftState: StateFlow<PurohitFormDraft> = PurohitFormDraftStore.draftFlow

    fun saveDraft(
        poojaName: String,
        category: String,
        durationHours: String,
        baseDakshina: String,
        samagriIncluded: Boolean,
        samagriExtraCost: String,
        languages: List<String>,
        description: String
    ) {
        val draft = PurohitFormDraft(
            poojaName = poojaName,
            category = category,
            durationHours = durationHours,
            baseDakshina = baseDakshina,
            samagriIncluded = samagriIncluded,
            samagriExtraCost = samagriExtraCost,
            languages = languages,
            description = description
        )
        PurohitFormDraftStore.saveDraft(draft)
    }

    fun clearDraft() {
        PurohitFormDraftStore.clearDraft()
    }

    fun getInitialDraft(): PurohitFormDraft = PurohitFormDraftStore.getDraft()

    fun submitService(
        poojaName: String,
        category: String,
        durationHours: Double,
        baseDakshina: Double,
        samagriIncluded: Boolean,
        samagriExtraCost: Double,
        languages: List<String>,
        description: String
    ) {
        viewModelScope.launch {
            _uiState.value = PurohitFormUiState.Submitting
            val service = PurohitServiceDto(
                purohitId = "", // Set by repository
                poojaName = poojaName,
                category = category,
                durationHours = durationHours,
                baseDakshina = baseDakshina,
                samagriIncluded = samagriIncluded,
                samagriExtraCost = if (samagriIncluded) 0.0 else samagriExtraCost,
                languagesSupported = languages,
                description = description.ifBlank { null }
            )

            repository.createPoojaService(service)
                .onSuccess {
                    clearDraft()
                    _uiState.value = PurohitFormUiState.Success
                }
                .onFailure { _uiState.value = PurohitFormUiState.Error(it.localizedMessage ?: "Submission failed") }
        }
    }

    fun resetState() {
        _uiState.value = PurohitFormUiState.Idle
    }
}
