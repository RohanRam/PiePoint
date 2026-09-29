package com.piepoint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piepoint.app.data.model.*
import com.piepoint.app.data.repository.AiRepository
import com.piepoint.app.data.repository.RealAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiViewModel(
    private val repository: AiRepository = RealAiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    /**
     * Send a user message. Everything routes through the chat endpoint,
     * which decides whether to return a pizza suggestion or just text.
     */
    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty() || _uiState.value.isLoading) return

        // Add user message immediately
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = trimmed
        )
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            repository.chat(_uiState.value.messages).fold(
                onSuccess = { (reply, pizza) ->
                    val assistantMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = reply,
                        pizza = pizza
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + assistantMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = error.message?.takeIf { it.isNotBlank() }
                            ?: "Sorry, something went wrong. Please check your connection and try again. 😔"
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + errorMessage,
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
