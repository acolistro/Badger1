package com.example.badger.di

import android.content.Context
import androidx.room.Room
import com.example.badger.data.local.BadgerDatabase
import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BadgerDatabase {
        return Room.databaseBuilder(
            context,
            BadgerDatabase::class.java,
            "badger_database"
        )
            .addMigrations(BadgerDatabase.MIGRATION_1_2)  // Add migration
            .fallbackToDestructiveMigration() //allow database recreation
            .build()
    }

    @Provides
    fun provideListDao(database: BadgerDatabase): ListDao = database.listDao()

    @Provides
    fun provideUserDao(database: BadgerDatabase): UserDao = database.userDao()
}
