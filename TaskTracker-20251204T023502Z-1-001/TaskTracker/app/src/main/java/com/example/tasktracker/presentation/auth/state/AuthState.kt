package com.example.tasktracker.presentation.auth.state

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object ResetPasswordSent : AuthState()
    data class Error(val message: String) : AuthState()
}