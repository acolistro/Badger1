package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.model.SharedList
import com.example.badger.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val listRepository: ListRepository
) : ViewModel() {
    private val _favoriteLists = MutableStateFlow<List<SharedList>>(emptyList())
    val favoriteLists: StateFlow<List<SharedList>> = _favoriteLists.asStateFlow()

    init {
        viewModelScope.launch {
//            listRepository.getFavoriteLists()
//                .collect { lists ->
//                    _favoriteLists.value = lists
//                }
        }
    }

    fun toggleFavorite(listId: String, favorite: Boolean) {
        viewModelScope.launch {
            listRepository.toggleFavorite(listId, favorite)
        }
    }
}