package com.example.badger.data.model

import com.example.badger.data.local.entities.ListEntity

data class SharedList(
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
    fun toEntity() = ListEntity.fromDomain(this)
}