package com.example.badger.data.local.dao

import androidx.room.*
import com.example.badger.data.local.entities.ListItemEntity
import com.example.badger.data.model.Enums.Priority
import kotlinx.coroutines.flow.Flow

@Dao
interface ListItemDao {
    @Query("SELECT * FROM list_items WHERE listId = :listId")
    fun getItemsForList(listId: String): Flow<List<ListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ListItemEntity)

    @Update
    suspend fun update(item: ListItemEntity)

    @Delete
    suspend fun delete(item: ListItemEntity)

    @Query("UPDATE list_items SET isCompleted = :isCompleted, completedBy = :completedBy, completedAt = :completedAt WHERE id = :itemId")
    suspend fun updateCompletionStatus(
        itemId: String,
        isCompleted: Boolean,
        completedBy: String?,
        completedAt: Long?
    )

    @Query("UPDATE list_items SET priority = :priority WHERE id = :itemId")
    suspend fun updatePriority(itemId: String, priority: Priority)
}