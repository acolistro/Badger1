package com.example.badger.ui.state

sealed class LoginUiState {
    data class Initial(val keepSignedIn: Boolean = false) : LoginUiState()
    data class Loading(val keepSignedIn: Boolean = false) : LoginUiState()
    data class VerificationRequired(val keepSignedIn: Boolean = false) : LoginUiState()
    data class VerificationEmailSent(val keepSignedIn: Boolean = false) : LoginUiState()
    data class Error(val message: String, val keepSignedIn: Boolean = false) : LoginUiState()

    // Helper methods for data binding
    companion object {
        @JvmStatic
        fun isLoading(state: LoginUiState?): Boolean {
            return state is Loading
        }

        @JvmStatic
        fun getKeepSignedIn(state: LoginUiState?): Boolean {
            return when (state) {
                is Initial -> state.keepSignedIn
                is Loading -> state.keepSignedIn
                is VerificationRequired -> state.keepSignedIn
                is VerificationEmailSent -> state.keepSignedIn
                is Error -> state.keepSignedIn
                null -> false
                else -> false  // Default case for any future states
            }
        }
    }
}

