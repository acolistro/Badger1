package com.example.badger1.DataModels

data class User(
    val id: String,
    val email: String,
    val username: String,
    val favoriteListIds: List<String> = listOf()
)
