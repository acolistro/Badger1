package com.example.badger.data.repository

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.remote.RemoteDataSource
import com.example.badger.data.model.SharedList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListRepository @Inject constructor(
    private val listDao: ListDao,
  //  private val itemDao: ListItemDao,
    private val remoteDataSource: RemoteDataSource,
//    private val firebaseDataSource: FirebaseDataSource
) {
    suspend fun createList(title: String): Result<SharedList> {
        return try {
            val list = remoteDataSource.createList(title)
            //listDao.insertList(list.toEntity())
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    fun getFavoriteLists(): Flow<List<SharedList>> {
//        return listDao.getFavoriteLists()
//            .map { lists -> lists.map { it.toDomain() } }
//    }

    suspend fun toggleFavorite(listId: String, favorite: Boolean) {
       // listDao.updateFavorite(listId, favorite)
        remoteDataSource.updateFavorite(listId, favorite)
    }

}