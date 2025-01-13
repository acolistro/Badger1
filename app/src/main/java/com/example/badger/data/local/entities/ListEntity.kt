package com.example.badger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.badger.data.model.ListItem
import com.example.badger.data.model.SharedList

@Entity(tableName = "lists")
data class ListEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdBy: String,
    val createdAt: Long,
    val lastModifiedBy: String,
    val lastModifiedAt: Long,
    val sharedWithUsers: List<String>,
    val items: List<ListItem>,
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
            sharedWithUsers = sharedWithUsers,
            items = items,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromDomain(sharedList: SharedList): ListEntity {
            return ListEntity(
                id = sharedList.id,
                title = sharedList.title,
                createdBy = sharedList.createdBy,
                createdAt = sharedList.createdAt,
                lastModifiedBy = sharedList.lastModifiedBy,
                lastModifiedAt = sharedList.lastModifiedAt,
                sharedWithUsers = sharedList.sharedWithUsers,
                items = sharedList.items,
                isFavorite = sharedList.isFavorite
            )
        }
    }
}
