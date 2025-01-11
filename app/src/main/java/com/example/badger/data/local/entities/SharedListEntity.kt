package com.example.badger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.badger.data.model.SharedList

@Entity(tableName = "shared_lists")
data class SharedListEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdBy: String,
    val createdAt: Long,
    val lastModifiedBy: String,
    val lastModifiedAt: Long,
    val isFavorite: Boolean
) {
    fun toDomain(): SharedList {
        return SharedList(
            id = id,
            title = title,
            createdBy = createdBy,
            createdAt = createdAt,
            lastModifiedBy = lastModifiedBy,
            lastModifiedAt = lastModifiedAt,
            sharedWithUsers = emptyList(), // You'll need to implement this
            items = emptyList(), // You'll need to implement this
            isFavorite = isFavorite
        )
    }
}
