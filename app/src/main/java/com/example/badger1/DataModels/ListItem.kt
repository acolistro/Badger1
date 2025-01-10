package com.example.badger1.DataModels

import com.example.badger1.DataModels.Enums.Priority

data class ListItem(
    val id: String,
    val content: String,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val createdBy: String,
    val createdAt: Long,
    val completedBy: String? = null,
    val completedAt: Long? = null
)