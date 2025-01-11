package com.example.badger.ui.event

sealed class LoginEvent {
    object NavigateToDashboard : LoginEvent()
}
