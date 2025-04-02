package com.example.badger.di

import android.content.Context
import com.example.badger.security.CryptoManager
import com.example.badger.security.KeyManager
import com.example.badger.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager {
        return CryptoManager(context)
    }

    @Provides
    @Singleton
    fun provideKeyManager(
        cryptoManager: CryptoManager,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userRepository: UserRepository
    ): KeyManager {
        return KeyManager(cryptoManager, firebaseAuth, firestore, userRepository)
    }
}
