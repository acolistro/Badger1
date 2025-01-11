package com.example.badger.di

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.repository.ListRepository
import com.example.badger.data.repository.UserRepository
import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.remote.FirebaseDataSource
import com.example.badger.data.remote.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
        remoteDataSource: RemoteDataSource
    ): UserRepository {
        return UserRepository(userDao, remoteDataSource)
    }
}
