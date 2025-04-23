package com.example.badger.ui.state

sealed class SignUpUiState {
    object Initial : SignUpUiState()
    object Loading : SignUpUiState()
    object VerificationRequired : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()

    fun isLoading(): Boolean = this is Loading

    companion object {
        // Static method for null safety
        @JvmStatic
        fun isLoading(state: SignUpUiState?): Boolean = state is Loading
    }
}
