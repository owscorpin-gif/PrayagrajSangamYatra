package com.prayagraj.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PurohitFormDraft(
    val poojaName: String = "",
    val category: String = "Kumbh Snan Sankalp",
    val durationHours: String = "2.0",
    val baseDakshina: String = "1100",
    val samagriIncluded: Boolean = true,
    val samagriExtraCost: String = "500",
    val languages: List<String> = listOf("Hindi", "Sanskrit"),
    val description: String = ""
) {
    fun isNotEmpty(): Boolean =
        poojaName.isNotBlank() || description.isNotBlank() || baseDakshina != "1100" || durationHours != "2.0"
}

object PurohitFormDraftStore {
    private const val PREFS_NAME = "purohit_form_draft_prefs"
    private const val KEY_DRAFT = "key_purohit_form_draft"

    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences? by lazy {
        try {
            MainApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } catch (e: Throwable) {
            null
        }
    }

    private val _draftFlow = MutableStateFlow(loadDraftInternal())
    val draftFlow: StateFlow<PurohitFormDraft> = _draftFlow.asStateFlow()

    private fun loadDraftInternal(): PurohitFormDraft {
        val serialized = prefs?.getString(KEY_DRAFT, null) ?: return PurohitFormDraft()
        return try {
            json.decodeFromString<PurohitFormDraft>(serialized)
        } catch (e: Exception) {
            PurohitFormDraft()
        }
    }

    fun getDraft(): PurohitFormDraft = _draftFlow.value

    fun saveDraft(draft: PurohitFormDraft) {
        _draftFlow.value = draft
        prefs?.edit()?.putString(KEY_DRAFT, json.encodeToString(draft))?.apply()
    }

    fun clearDraft() {
        _draftFlow.value = PurohitFormDraft()
        prefs?.edit()?.remove(KEY_DRAFT)?.apply()
    }
}
