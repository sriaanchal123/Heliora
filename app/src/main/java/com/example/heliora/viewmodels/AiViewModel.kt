package com.example.heliora.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heliora.models.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
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
                if (apiKey.startsWith("AIzaSy")) {
                    val response = generativeModel.generateContent(text)
                    val aiResponseText = response.text ?: getSmartHealthFallback(text)
                    _chatHistory.value = _chatHistory.value + ChatMessage(aiResponseText, false)
                } else {
                    delay(800)
                    _chatHistory.value = _chatHistory.value + ChatMessage(getSmartHealthFallback(text), false)
                }
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(getSmartHealthFallback(text), false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getSmartHealthFallback(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("fever") || q.contains("bukhar") -> 
                "For fever: Stay hydrated, rest, and monitor your body temperature. Common over-the-counter medicine like Paracetamol (500mg) helps reduce fever. If temperature exceeds 102°F or persists for more than 3 days, please consult a physician."
                
            q.contains("headache") || q.contains("sir dard") || q.contains("head ache") -> 
                "For headache: Drink plenty of water, reduce screen exposure, and rest in a quiet room. Light head massage or Paracetamol can provide relief. Seek immediate medical attention if accompanied by neck stiffness or sudden vision changes."
                
            q.contains("cough") || q.contains("khasi") || q.contains("cold") -> 
                "For cold & cough: Warm saline gargles, steam inhalation, and honey with warm water help soothe throat irritation. Stay warm and rest well. Consult a doctor if you experience shortness of breath."
                
            q.contains("stomach") || q.contains("pet dard") || q.contains("acidity") -> 
                "For stomach discomfort or acidity: Eat light, non-spicy meals. Warm water or antacid syrup can relieve acidity. Avoid lying down immediately after meals. Consult a gastroenterologist if severe abdominal pain persists."
                
            else -> 
                "Hello! I am HelioRa AI Health Advisor. Based on your symptom query, I recommend monitoring your symptoms, staying well-hydrated, and resting. Always consult a certified doctor or specialist for a detailed medical evaluation."
        }
    }
}
