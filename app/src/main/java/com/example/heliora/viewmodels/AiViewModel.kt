package com.example.heliora.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heliora.models.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {
    private val apiKey = ""
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hi! I am HelioRa AI. How can I help you with your health today?", false)
    ))
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _chatHistory.value = _chatHistory.value + userMessage

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = generativeModel.generateContent(text)
                val aiResponseText = response.text ?: "I'm sorry, I couldn't generate a response."
                
                val aiResponse = ChatMessage(aiResponseText, false)
                _chatHistory.value = _chatHistory.value + aiResponse
            } catch (e: Exception) {
                val errorResponse = ChatMessage("Error: ${e.localizedMessage}. Please check your API key or connection.", false)
                _chatHistory.value = _chatHistory.value + errorResponse
            } finally {
                _isLoading.value = false
            }
        }
    }
}
