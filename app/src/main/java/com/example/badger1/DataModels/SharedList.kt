package com.example.badger1.DataModels

data class SharedList(
    val id: String,
    val title: String,
    val createdBy: String,
    val createdAt: Long,
    val lastModifiedBy: String,
    val lastModifiedAt: Long,
    val sharedWithUsers: List<String>,
    val items: List<ListItem>,
    val isFavorite: Boolean = false
)
