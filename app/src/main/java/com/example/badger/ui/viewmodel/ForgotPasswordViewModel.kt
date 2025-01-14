package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.repository.UserRepository
import com.example.badger.ui.state.ForgotPasswordUiState
import com.example.badger.ui.event.ForgotPasswordEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events = _events.asSharedFlow()

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            try {
                userRepository.sendPasswordResetEmail(email)
                    .onSuccess {
                        _uiState.value = ForgotPasswordUiState.Success
                        // Wait a moment before navigating back
                        kotlinx.coroutines.delay(2000)
                        _events.emit(ForgotPasswordEvent.NavigateBack)
                    }
                    .onFailure { exception ->
                        _uiState.value = ForgotPasswordUiState.Error(
                            exception.message ?: "Failed to send reset email"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState.Error(
                    e.message ?: "Failed to send reset email"
                )
            }
        }
    }

    fun resetError() {
        _uiState.value = ForgotPasswordUiState.Initial
    }

    fun navigateBack() {
        viewModelScope.launch {
            _events.emit(ForgotPasswordEvent.NavigateBack)
        }
    }
}
