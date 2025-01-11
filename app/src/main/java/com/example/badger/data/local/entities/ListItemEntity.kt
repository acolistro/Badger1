package com.example.badger.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "list_items",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ListItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val content: String,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val createdBy: String,
    val createdAt: Long,
    val completedBy: String? = null,
    val completedAt: Long? = null
)

enum class Priority {
    LOW, NORMAL, HIGH, URGENT
}
