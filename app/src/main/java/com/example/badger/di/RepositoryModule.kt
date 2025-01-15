package com.example.badger.di

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.repository.ListRepository
import com.example.badger.data.repository.UserRepository
import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.remote.RemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideListRepository(
        listDao: ListDao,
        remoteDataSource: RemoteDataSource
    ): ListRepository {
        return ListRepository(listDao, remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        @Named("auth") auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository {
        return UserRepository(userDao, auth, firestore)
    }
}
