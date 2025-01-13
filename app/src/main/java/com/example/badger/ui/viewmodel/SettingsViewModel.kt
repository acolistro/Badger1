package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Inject DataStore or SharedPreferences for settings
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // TODO: Load settings from DataStore/SharedPreferences
            _uiState.value = SettingsUiState.Success(
                notificationsEnabled = true,
                darkModeEnabled = false
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Save to DataStore/SharedPreferences
            updateState { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Save to DataStore/SharedPreferences
            updateState { it.copy(darkModeEnabled = enabled) }
        }
    }

    private fun updateState(update: (SettingsUiState.Success) -> SettingsUiState.Success) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            _uiState.value = update(currentState)
        }
    }
}

sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Success(
        val notificationsEnabled: Boolean,
        val darkModeEnabled: Boolean
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
