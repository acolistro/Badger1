package com.example.badger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val isFavorite: Boolean = false,
    val sharedWithUsers: List<String> = emptyList()
) {
    // Convert Entity to Domain model
    fun toDomain(): SharedList {
        return SharedList(
            id = id,
            title = title,
            createdBy = createdBy,
            createdAt = createdAt,
            lastModifiedBy = lastModifiedBy,
            lastModifiedAt = lastModifiedAt,
            sharedWithUsers = sharedWithUsers,
            items = emptyList(),  // Items will be loaded separately
            isFavorite = isFavorite
        )
    }

    companion object {
        // Convert Domain model to Entity
        fun fromDomain(sharedList: SharedList): ListEntity {
            return ListEntity(
                id = sharedList.id,
                title = sharedList.title,
                createdBy = sharedList.createdBy,
                createdAt = sharedList.createdAt,
                lastModifiedBy = sharedList.lastModifiedBy,
                lastModifiedAt = sharedList.lastModifiedAt,
                isFavorite = sharedList.isFavorite,
                sharedWithUsers = sharedList.sharedWithUsers
            )
        }
    }
}
