package com.example.badger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.data.repository.PreferencesRepository
import com.example.badger.data.repository.UserRepository
import com.example.badger.ui.state.LoginUiState
import com.example.badger.ui.event.LoginEvent
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository,
    @Named("auth") private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial(
        keepSignedIn = preferencesRepository.shouldKeepSignedIn()
    ))
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val keepSignedIn = LoginUiState.getKeepSignedIn(_uiState.value)
            _uiState.update { LoginUiState.Loading(keepSignedIn) }

            try {
                // Store the preference
                preferencesRepository.setKeepSignedIn(keepSignedIn)

                // If not keeping signed in, sign out when the app closes
                if (!keepSignedIn) {
                    auth.addAuthStateListener { firebaseAuth ->
                        if (firebaseAuth.currentUser != null) {
                            firebaseAuth.signOut()
                        }
                    }
                }

                userRepository.signIn(email, password)
                    .onSuccess { user ->
                        if (!user.emailVerified) {
                            _uiState.update { LoginUiState.VerificationRequired(keepSignedIn) }
                            return@launch
                        }
                        _events.emit(LoginEvent.NavigateToDashboard)
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            LoginUiState.Error(
                                message = exception.message ?: "Login failed",
                                keepSignedIn = keepSignedIn
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    LoginUiState.Error(
                        message = e.message ?: "Login failed",
                        keepSignedIn = keepSignedIn
                    )
                }
            }
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            val keepSignedIn = LoginUiState.getKeepSignedIn(_uiState.value)
            try {
                userRepository.updateEmailVerificationStatus()
                    .onSuccess {
                        _events.emit(LoginEvent.NavigateToDashboard)
                    }
                    .onFailure { exception ->
                        _uiState.update { LoginUiState.Error("Email not verified yet", keepSignedIn) }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    LoginUiState.Error(
                        message = e.message ?: "Verification check failed",
                        keepSignedIn = keepSignedIn
                    )
                }
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val keepSignedIn = LoginUiState.getKeepSignedIn(_uiState.value)
            try {
                userRepository.resendEmailVerification()
                    .onSuccess {
                        _uiState.update { LoginUiState.VerificationEmailSent(keepSignedIn) }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            LoginUiState.Error(
                                message = exception.message ?: "Failed to resend verification email",
                                keepSignedIn = keepSignedIn
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    LoginUiState.Error(
                        message = e.message ?: "Failed to resend verification email",
                        keepSignedIn = keepSignedIn
                    )
                }
            }
        }
    }

    fun resetError() {
        val keepSignedIn = LoginUiState.getKeepSignedIn(_uiState.value)
        _uiState.update { LoginUiState.Initial(keepSignedIn) }
    }

    fun navigateToSignUp() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToSignUp)
        }
    }

    fun navigateToForgotPassword() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToForgotPassword)
        }
    }

    fun onKeepSignedInChanged(checked: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is LoginUiState.Initial -> currentState.copy(keepSignedIn = checked)
                is LoginUiState.Loading -> currentState.copy(keepSignedIn = checked)
                is LoginUiState.VerificationRequired -> currentState.copy(keepSignedIn = checked)
                is LoginUiState.VerificationEmailSent -> currentState.copy(keepSignedIn = checked)
                is LoginUiState.Error -> currentState.copy(keepSignedIn = checked)
                else -> LoginUiState.Initial(keepSignedIn = checked)
            }
        }
    }
}