package com.example.badger.data.local.dao

import androidx.room.*
import com.example.badger.data.local.entities.ListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    @Query("SELECT * FROM lists WHERE isFavorite = 1")
    fun getFavoriteLists(): Flow<List<ListEntity>>

    @Query("SELECT * FROM lists")
    fun getAllLists(): Flow<List<ListEntity>>

    @Query("SELECT COUNT(*) FROM lists WHERE isFavorite = 1")
    suspend fun getFavoriteListsCount(): Int

    @Query("UPDATE lists SET isFavorite = :isFavorite WHERE id = :listId")
    suspend fun updateFavorite(listId: String, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<ListEntity>)

    @Update
    suspend fun updateList(list: ListEntity)

    @Delete
    suspend fun deleteList(list: ListEntity)

    @Query("DELETE FROM lists WHERE id = :listId")
    suspend fun deleteListById(listId: String)

    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListById(listId: String): ListEntity?

    @Query("DELETE FROM lists")
    suspend fun deleteAllLists()

    @Transaction
    suspend fun syncLists(lists: List<ListEntity>) {
        deleteAllLists()
        insertLists(lists)
    }
}
