package com.example.badger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.badger.data.local.dao.ListItemDao
import com.example.badger.data.local.entities.ListEntity
import com.example.badger.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Dao
interface ListDao {
    @Query("SELECT * FROM lists WHERE id IN (:listIds)")
    fun getFavoriteLists(listIds: List<String>): Flow<List<ListEntity>>

    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListById(listId: String): ListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity)

    @Update
    suspend fun updateList(list: ListEntity)

    @Delete
    suspend fun deleteList(list: ListEntity)

    @Query("UPDATE lists SET isFavorite = :isFavorite WHERE id = :listId")
    suspend fun updateFavoriteStatus(listId: String, isFavorite: Boolean)
}

