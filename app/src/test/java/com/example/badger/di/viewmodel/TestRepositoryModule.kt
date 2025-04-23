// src/test/java/com/example/badger/di/TestRepositoryModule.kt
package com.example.badger.di

import com.example.badger.data.repository.PreferencesRepository
import com.example.badger.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return mock(UserRepository::class.java)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(): PreferencesRepository {
        return mock(PreferencesRepository::class.java)
    }
}
