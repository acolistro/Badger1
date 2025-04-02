package com.example.badger.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Named
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class]
)
object TestFirebaseModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return mock(FirebaseFirestore::class.java)
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideFirebaseAuth(): FirebaseAuth {
        return mock(FirebaseAuth::class.java)
    }
}
