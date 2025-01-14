package com.example.badger.ui.state

sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    object VerificationRequired : LoginUiState()
    object VerificationEmailSent : LoginUiState()
    data class Error(val message: String) : LoginUiState()

    // Helper method for data binding
    companion object {
        @JvmStatic
        fun isLoading(state: LoginUiState?): Boolean {
            return state is Loading
        }
    }
}
