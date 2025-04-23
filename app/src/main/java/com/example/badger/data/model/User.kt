package com.example.badger.data.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val username: String,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val favoriteListIds: List<String> = listOf()
)
