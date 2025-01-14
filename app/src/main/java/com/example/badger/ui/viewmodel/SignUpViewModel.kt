package com.example.badger.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badger.ui.state.SignUpUiState
import com.example.badger.util.ValidationUtil
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Initial)
    val uiState: StateFlow<SignUpUiState> = _uiState

    private var storedVerificationId: String? = null
    private var phoneNumber: String? = null

    fun signUp(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        activity: Activity
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = SignUpUiState.Loading

                // Validate inputs
                when {
                    !ValidationUtil.isValidName(firstName) -> {
                        _uiState.value = SignUpUiState.Error("Invalid first name")
                        return@launch
                    }
                    !ValidationUtil.isValidName(lastName) -> {
                        _uiState.value = SignUpUiState.Error("Invalid last name")
                        return@launch
                    }
                    !ValidationUtil.isValidUsername(username) -> {
                        _uiState.value = SignUpUiState.Error("Invalid username")
                        return@launch
                    }
                    !ValidationUtil.isValidEmail(email) -> {
                        _uiState.value = SignUpUiState.Error("Invalid email")
                        return@launch
                    }
                    !ValidationUtil.isValidPhone(phone) -> {
                        _uiState.value = SignUpUiState.Error("Invalid phone number")
                        return@launch
                    }
                    !ValidationUtil.isValidPassword(password) -> {
                        _uiState.value = SignUpUiState.Error(
                            "Password must contain at least 8 characters, " +
                                    "one uppercase letter, one number, and one special character"
                        )
                        return@launch
                    }
                    !ValidationUtil.passwordsMatch(password, confirmPassword) -> {
                        _uiState.value = SignUpUiState.Error("Passwords do not match")
                        return@launch
                    }
                }

                // Check if username is already taken
                val usernameDoc = firestore.collection("usernames").document(username).get().await()
                if (usernameDoc.exists()) {
                    _uiState.value = SignUpUiState.Error("Username already taken")
                    return@launch
                }

                // Create user with email and password
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                // Send email verification
                authResult.user?.sendEmailVerification()?.await()

                // Save user data
                val userId = authResult.user?.uid ?: throw IllegalStateException("User ID is null")
                val userData = hashMapOf(
                    "firstName" to ValidationUtil.sanitizeInput(firstName),
                    "lastName" to ValidationUtil.sanitizeInput(lastName),
                    "username" to username,
                    "email" to email,
                    "phone" to phone,
                    "emailVerified" to false,
                    "phoneVerified" to false
                )

                firestore.collection("users").document(userId).set(userData).await()
                firestore.collection("usernames").document(username).set(hashMapOf("uid" to userId)).await()

                // Store phone number for verification
                phoneNumber = phone

                // Start phone verification
                initiatePhoneVerification(phone, activity)

            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    private fun initiatePhoneVerification(phoneNumber: String, activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    try {
                        auth.currentUser?.linkWithCredential(credential)?.await()
                        _uiState.value = SignUpUiState.Success
                    } catch (e: Exception) {
                        _uiState.value = SignUpUiState.Error("Failed to link phone: ${e.message}")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.value = SignUpUiState.Error("Phone verification failed: ${e.message}")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                _uiState.value = SignUpUiState.VerificationRequired
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneCode(code: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SignUpUiState.Loading

                val verificationId = storedVerificationId ?: throw IllegalStateException("Verification ID is null")
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                linkPhoneCredential(credential)

            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error("Phone verification failed: ${e.message}")
            }
        }
    }

    private fun linkPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                auth.currentUser?.linkWithCredential(credential)?.await()

                // Update phone verification status
                auth.currentUser?.uid?.let { userId ->
                    firestore.collection("users").document(userId)
                        .update("phoneVerified", true)
                        .await()
                }

                _uiState.value = SignUpUiState.Success

            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error("Failed to link phone: ${e.message}")
            }
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    _uiState.value = SignUpUiState.Error("User not found")
                    return@launch
                }

                // Reload user to get latest email verification status
                user.reload().await()

                if (user.isEmailVerified) {
                    _uiState.value = SignUpUiState.Success
                } else {
                    _uiState.value = SignUpUiState.VerificationRequired
                }

            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error("Failed to check verification status: ${e.message}")
            }
        }
    }
}
