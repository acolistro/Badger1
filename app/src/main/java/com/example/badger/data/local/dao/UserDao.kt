package com.example.badger.data.local.dao

import androidx.room.*
import com.example.badger.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET favoriteListIds = :favoriteListIds WHERE id = :userId")
    suspend fun updateFavorites(userId: String, favoriteListIds: List<String>)

    @Query("SELECT favoriteListIds FROM users WHERE id = :userId")
    fun getFavoriteListIds(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun doesUserExist(email: String): Boolean
}
