package com.example.badger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.badger.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val username: String,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val favoriteListIds: List<String> = listOf()
) {
    // Convert Entity to Domain model
    fun toDomain(): User {
        return User(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            email = email,
            username = username,
            emailVerified = emailVerified,
            phoneVerified = phoneVerified,
            favoriteListIds = favoriteListIds
        )
    }

    companion object {
        // Convert Domain model to Entity
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                phone = user.phone,
                email = user.email,
                username = user.username,
                emailVerified = user.emailVerified,
                phoneVerified = user.phoneVerified,
                favoriteListIds = user.favoriteListIds
            )
        }
    }
}
