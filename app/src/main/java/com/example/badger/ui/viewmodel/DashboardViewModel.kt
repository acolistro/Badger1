package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.model.SharedList
import com.example.badger.data.model.User
import com.example.badger.data.repository.ListRepository
import com.example.badger.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events = _events.asSharedFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _events.emit(DashboardEvent.NavigateToLogin)
                    return@launch
                }

                listRepository.getFavoriteLists(currentUser.id)
                    .onStart { _uiState.value = DashboardUiState.Loading }
                    .catch { error ->
                        _uiState.value = DashboardUiState.Error(error.message ?: "Failed to load lists")
                    }
                    .collect { lists ->
                        _uiState.value = DashboardUiState.Success(
                            user = currentUser,
                            favoriteLists = lists
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun toggleFavorite(listId: String, favorite: Boolean) {
        viewModelScope.launch {
            try {
                listRepository.toggleFavorite(listId, favorite)
                // Refresh will happen automatically through Flow collection
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Failed to update favorite")
            }
        }
    }

    fun createNewList() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToCreateList)
        }
    }

    fun openList(listId: String) {
        viewModelScope.launch {
            _events.emit(DashboardEvent.NavigateToList(listId))
        }
    }

    fun refresh() {
        loadDashboard()
    }
}

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val user: User,
        val favoriteLists: List<SharedList>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

sealed class DashboardEvent {
    data object NavigateToLogin : DashboardEvent()
    data object NavigateToCreateList : DashboardEvent()
    data class NavigateToList(val listId: String) : DashboardEvent()
}