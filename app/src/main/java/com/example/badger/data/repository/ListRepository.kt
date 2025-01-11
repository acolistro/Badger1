package com.example.badger.data.repository

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.remote.RemoteDataSource
import com.example.badger.data.model.SharedList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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

    suspend fun toggleFavorite(listId: String, favorite: Boolean): Result<Unit> {
        // Check if we're trying to add a favorite
        if (favorite) {
            // Get current favorite count
            val currentFavorites = listDao.getFavoriteListsCount()
            if (currentFavorites >= 3) {
                return Result.failure(Exception("Maximum of 3 favorite lists allowed"))
            }
        }

        return try {
            // Update local database
            listDao.updateFavorite(listId, favorite)
            // Update remote
            remoteDataSource.updateFavorite(listId, favorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteListsCount(): Int {
        return listDao.getFavoriteListsCount()
    }
}
