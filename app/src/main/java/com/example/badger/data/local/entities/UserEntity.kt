package com.example.badger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.badger.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val username: String,
    val favoriteListIds: List<String> = emptyList()
) {
    // Convert Entity to Domain model
    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            username = username,
            favoriteListIds = favoriteListIds
        )
    }

    companion object {
        // Convert Domain model to Entity
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                email = user.email,
                username = user.username,
                favoriteListIds = user.favoriteListIds
            )
        }
    }
}
