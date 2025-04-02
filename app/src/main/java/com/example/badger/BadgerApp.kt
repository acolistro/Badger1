package com.example.badger

import android.app.Application
import com.example.badger.security.KeyManager
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BadgerApp : Application() {

    @Inject
    lateinit var keyManager: KeyManager

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        Firebase.initialize(this)

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize E2EE for current user if logged in
        initializeE2EEForCurrentUser()
    }

    private fun initializeE2EEForCurrentUser() {
        val currentUser = firebaseAuth.currentUser ?: return

        // Initialize user keys in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                keyManager.initializeUserKeys(currentUser.uid)
                Timber.d("E2EE initialized for user ${currentUser.uid}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize E2EE for current user")
            }
        }
    }
}
