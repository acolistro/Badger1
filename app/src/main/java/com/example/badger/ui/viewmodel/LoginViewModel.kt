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
                    .onSuccess {
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

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val username = email.substringBefore('@') // Default username
                userRepository.signUp(email, password, username)
                    .onSuccess {
                        _events.emit(LoginEvent.NavigateToDashboard)
                    }
                    .onFailure { exception ->
                        _uiState.value = LoginUiState.Error(exception.message ?: "Sign up failed")
                    }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun resetError() {
        _uiState.value = LoginUiState.Initial
    }
}
