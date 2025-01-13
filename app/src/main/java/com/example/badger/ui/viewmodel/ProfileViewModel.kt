package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.model.User
import com.example.badger.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    _uiState.value = ProfileUiState.Success(currentUser)
                } else {
                    _events.emit(ProfileEvent.NavigateToLogin)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser() ?: return@launch
                val updatedUser = currentUser.copy(username = newUsername)
                userRepository.updateUser(updatedUser)
                _uiState.value = ProfileUiState.Success(updatedUser)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to update username")
            }
        }
    }

    fun refresh() {
        loadProfile()
    }
}

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEvent {
    data object NavigateToLogin : ProfileEvent()
}
