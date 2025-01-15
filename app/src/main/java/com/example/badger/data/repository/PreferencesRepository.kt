package com.example.badger.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setKeepSignedIn(keep: Boolean) {
        prefs.edit()
            .putBoolean(KEY_KEEP_SIGNED_IN, keep)
            .apply()
    }

    fun shouldKeepSignedIn(): Boolean {
        return prefs.getBoolean(KEY_KEEP_SIGNED_IN, false)
    }

    companion object {
        private const val PREFS_NAME = "BadgerPrefs"
        private const val KEY_KEEP_SIGNED_IN = "keep_signed_in"
    }
}
