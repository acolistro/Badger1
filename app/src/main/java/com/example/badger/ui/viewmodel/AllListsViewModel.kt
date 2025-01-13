package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.model.SharedList
import com.example.badger.data.repository.ListRepository
import com.example.badger.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllListsViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AllListsUiState>(AllListsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AllListsEvent>()
    val events = _events.asSharedFlow()

    init {
        loadLists()
    }

    private fun loadLists() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser() ?: run {
                    _events.emit(AllListsEvent.NavigateToLogin)
                    return@launch
                }

                // TODO: Implement getLists() in ListRepository
                // listRepository.getLists(currentUser.id)
                //     .onStart { _uiState.value = AllListsUiState.Loading }
                //     .catch { error ->
                //         _uiState.value = AllListsUiState.Error(error.message ?: "Failed to load lists")
                //     }
                //     .collect { lists ->
                //         _uiState.value = AllListsUiState.Success(lists)
                //     }
            } catch (e: Exception) {
                _uiState.value = AllListsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refresh() {
        loadLists()
    }

    fun openList(listId: String) {
        viewModelScope.launch {
            _events.emit(AllListsEvent.NavigateToList(listId))
        }
    }
}

sealed class AllListsUiState {
    data object Loading : AllListsUiState()
    data class Success(val lists: List<SharedList>) : AllListsUiState()
    data class Error(val message: String) : AllListsUiState()
}

sealed class AllListsEvent {
    data object NavigateToLogin : AllListsEvent()
    data class NavigateToList(val listId: String) : AllListsEvent()
}
