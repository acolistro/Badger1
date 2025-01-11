package com.example.badger.data.repository

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.model.SharedList
import com.example.badger.data.remote.RemoteDataSource
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    //private val listDao: ListDao,
    //  private val itemDao: ListItemDao,
    private val remoteDataSource: RemoteDataSource,
//    private val firebaseDataSource: FirebaseDataSource
) {
        suspend fun createUser(title: String): Result<SharedList> {
            return try {
                val list = remoteDataSource.createList(title)
                //listDao.insertList(list.toEntity())
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}