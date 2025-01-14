package com.example.badger.ui.event

sealed class LoginEvent {
    object NavigateToDashboard : LoginEvent()
    object NavigateToSignUp : LoginEvent()
    object NavigateToForgotPassword : LoginEvent()
}
