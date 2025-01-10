package com.example.badger1.Database

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import com.example.badger1.DataModels.Enums.Priority

// Room Database
@Database(entities = [SharedListEntity::class, ListItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sharedListDao(): SharedListDao
    abstract fun listItemDao(): ListItemDao
}

@Entity(tableName = "shared_lists")
data class SharedListEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdBy: String,
    val createdAt: Long,
    val lastModifiedBy: String,
    val lastModifiedAt: Long,
    val isFavorite: Boolean
)

@Entity(tableName = "list_items")
data class ListItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val content: String,
    val isCompleted: Boolean,
    val priority: Priority,
    val createdBy: String,
    val createdAt: Long,
    val completedBy: String?,
    val completedAt: Long?
)
