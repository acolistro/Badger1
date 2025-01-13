package com.example.badger.data.repository

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.local.entities.ListEntity
import com.example.badger.data.remote.RemoteDataSource
import com.example.badger.data.model.SharedList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRepository @Inject constructor(
    private val listDao: ListDao,
    private val remoteDataSource: RemoteDataSource,
) {
    suspend fun createList(title: String): Result<SharedList> {
        return remoteDataSource.createList(title)
    }

    fun getFavoriteLists(userId: String): Flow<List<SharedList>> {
        return listDao.getFavoriteLists()
            .map { lists -> lists.map { it.toDomain() } }
    }

    fun getAllLists(userId: String): Flow<List<SharedList>> {
        return listDao.getAllLists()
            .map { lists -> lists.map { it.toDomain() } }
    }

    suspend fun getListById(listId: String): Result<SharedList> {
        return try {
            val list = listDao.getListById(listId)?.toDomain()
                ?: return Result.failure(Exception("List not found"))
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(listId: String, favorite: Boolean): Result<Unit> {
        if (favorite) {
            val currentFavorites = listDao.getFavoriteListsCount()
            if (currentFavorites >= 3) {
                return Result.failure(Exception("Maximum of 3 favorite lists allowed"))
            }
        }

        return try {
            listDao.updateFavorite(listId, favorite)
            remoteDataSource.updateFavorite(listId, favorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteList(listId: String, listEntity: ListEntity): Result<Unit> {
        return try {
            listDao.deleteList(listEntity)
            remoteDataSource.deleteList(listId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateList(list: SharedList): Result<Unit> {
        return try {
            listDao.updateList(list.toEntity())
            remoteDataSource.updateList(list)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteListsCount(): Int {
        return listDao.getFavoriteListsCount()
    }

    suspend fun syncLists(userId: String) {
        try {
            val remoteLists = remoteDataSource.getAllLists(userId)
            listDao.insertLists(remoteLists.map { it.toEntity() })
        } catch (e: Exception) {
            // Handle sync failure
            throw e
        }
    }
}
