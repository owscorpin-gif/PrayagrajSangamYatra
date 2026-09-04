package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiGuideMessage
import com.example.data.model.MessageSender
import com.example.data.repository.PilgrimageAiGuideRepository
import com.example.data.repository.PilgrimageAiGuideRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiGuideUiState(
    val messages: List<AiGuideMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val showTariffSheet: Boolean = false,
    val quickPrompts: List<String> = listOf(
        "What is fair Dakshina for Pind Daan?",
        "How do I verify a Panda at Triveni Sangam?",
        "Is Sankalpa dakshina mandatory upfront?",
        "How to avoid ritual overcharging?",
        "Standard fare for Sangam boat rides?",
        "Rudrabhishek Puja cost & rules?"
    )
)

class AiGuideViewModel @JvmOverloads constructor(
    private val repository: PilgrimageAiGuideRepository = PilgrimageAiGuideRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiGuideUiState())
    val uiState: StateFlow<AiGuideUiState> = _uiState.asStateFlow()

    init {
        // Welcome message
        val welcomeMessage = AiGuideMessage(
            sender = MessageSender.AI,
            text = """🙏 **Namaste! I am your Prayagraj Pilgrimage AI Advisor.**

My role is to ensure your sacred yatra is peaceful, authentic, and free from financial exploitation.

✨ **I can help you with:**
• Fair community-standard Dakshina ranges for all rituals
• Official Panda & Tirth Purohit verification guidelines
• Upfront agreement tips before taking Sankalpa
• Emergency pilgrim helpline assistance

Select a quick question below or ask me anything about Prayagraj rituals!""",
            showVerifiedPurohitAction = true
        )
        _uiState.update { it.copy(messages = listOf(welcomeMessage)) }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun toggleTariffSheet(show: Boolean) {
        _uiState.update { it.copy(showTariffSheet = show) }
    }

    fun sendMessage(userText: String? = null) {
        val messageToSend = (userText ?: _uiState.value.inputText).trim()
        if (messageToSend.isBlank() || _uiState.value.isLoading) return

        val userMessage = AiGuideMessage(
            sender = MessageSender.USER,
            text = messageToSend
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val aiResponse = repository.getAiResponse(
                    userMessage = messageToSend,
                    history = _uiState.value.messages
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + aiResponse,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                val errorMessage = AiGuideMessage(
                    sender = MessageSender.AI,
                    text = "I am currently running in offline advisory mode. You can check the official standard Dakshina tariff table anytime or tap below to view verified Purohits.",
                    showVerifiedPurohitAction = true
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearChat() {
        val welcomeMessage = AiGuideMessage(
            sender = MessageSender.AI,
            text = "Conversation cleared. How can I assist with your pilgrimage rituals or Dakshina transparency today?",
            showVerifiedPurohitAction = true
        )
        _uiState.update { it.copy(messages = listOf(welcomeMessage)) }
    }
}
