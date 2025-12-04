package com.example.tasktracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.presentation.auth.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            when (val result = authRepository.signIn(email, password)) {
                is com.example.tasktracker.data.model.Result.Success -> {
                    _uiState.value = AuthState.Success
                }
                is com.example.tasktracker.data.model.Result.Failure -> {
                    _uiState.value = AuthState.Error(result.exception.message ?: "Login failed")
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            when (val result = authRepository.sendPasswordReset(email)) {
                is com.example.tasktracker.data.model.Result.Success -> {
                    _uiState.value = AuthState.ResetPasswordSent
                }
                is com.example.tasktracker.data.model.Result.Failure -> {
                    _uiState.value = AuthState.Error(result.exception.message ?: "Failed to send reset email")
                }
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthState.Idle
    }
}

class LoginViewModelFactory(private val authRepository: AuthRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}