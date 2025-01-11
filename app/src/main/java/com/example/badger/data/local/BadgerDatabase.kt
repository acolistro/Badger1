package com.example.badger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.local.dao.ListItemDao
import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.local.entities.ListEntity
import com.example.badger.data.local.entities.ListItemEntity
import com.example.badger.data.local.entities.UserEntity
import com.example.badger.data.local.utils.Converters  // Custom Converters class

@Database(
    entities = [
        ListEntity::class,
        UserEntity::class,
        ListItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BadgerDatabase : RoomDatabase() {
    abstract fun listDao(): ListDao
    abstract fun userDao(): UserDao
    abstract fun listItemDao(): ListItemDao
}