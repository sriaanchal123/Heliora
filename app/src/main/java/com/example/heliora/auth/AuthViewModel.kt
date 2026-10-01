package com.example.heliora.auth

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    fun register(name: String, email: String, password: String, role: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.registerUser(name, email, password, role, imageUri)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull(), name)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginUser(email, password)
            if (result.isSuccess) {
                val data = result.getOrNull()
                _authState.value = AuthState.Success(data?.first, data?.second ?: "User")
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?, val name: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
