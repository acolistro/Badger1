package com.example.badger.di

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.remote.RemoteDataSource
import com.example.badger.data.repository.EncryptedListRepository
import com.example.badger.security.CryptoManager
import com.example.badger.security.KeyManager
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptedRepositoryModule {

    @Provides
    @Singleton
    fun provideEncryptedListRepository(
        listDao: ListDao,
        remoteDataSource: RemoteDataSource,
        cryptoManager: CryptoManager,
        keyManager: KeyManager,
        @Named("auth") auth: FirebaseAuth
    ): EncryptedListRepository {
        return EncryptedListRepository(
            listDao,
            remoteDataSource,
            cryptoManager,
            keyManager,
            auth
        )
    }
}
