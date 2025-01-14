package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.repository.UserRepository
import com.example.badger.ui.state.LoginUiState
import com.example.badger.ui.event.LoginEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                userRepository.signIn(email, password)
                    .onSuccess { user ->
                        if (!user.emailVerified) {
                            _uiState.value = LoginUiState.VerificationRequired
                            return@launch
                        }
                        _events.emit(LoginEvent.NavigateToDashboard)
                    }
                    .onFailure { exception ->
                        _uiState.value = LoginUiState.Error(exception.message ?: "Login failed")
                    }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            try {
                userRepository.updateEmailVerificationStatus()
                    .onSuccess {
                        _events.emit(LoginEvent.NavigateToDashboard)
                    }
                    .onFailure { exception ->
                        _uiState.value = LoginUiState.Error("Email not verified yet")
                    }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Verification check failed")
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            try {
                userRepository.resendEmailVerification()
                    .onSuccess {
                        _uiState.value = LoginUiState.VerificationEmailSent
                    }
                    .onFailure { exception ->
                        _uiState.value = LoginUiState.Error(exception.message ?: "Failed to resend verification email")
                    }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Failed to resend verification email")
            }
        }
    }

    fun resetError() {
        _uiState.value = LoginUiState.Initial
    }

    fun navigateToSignUp() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToSignUp)
        }
    }

    fun navigateToForgotPassword() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToForgotPassword)
        }
    }
}
