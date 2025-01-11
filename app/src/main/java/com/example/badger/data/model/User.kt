package com.example.badger.data.model

data class User(
    val id: String,
    val email: String,
    val username: String,
    val favoriteListIds: List<String> = listOf()
)
