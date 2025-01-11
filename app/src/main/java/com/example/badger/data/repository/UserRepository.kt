package com.example.badger.data.repository

import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.local.entities.UserEntity
import com.example.badger.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth
) {
    // Auth Methods
    suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Sign in succeeded but user is null")
        createOrUpdateUser(firebaseUser)
    }

    suspend fun signUp(email: String, password: String, username: String): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Sign up succeeded but user is null")

        val newUser = User(
            id = firebaseUser.uid,
            email = email,
            username = username
        )
        saveUser(newUser)
        newUser
    }

    suspend fun signOut() {
        auth.signOut()
    }

    // User Data Methods
    private suspend fun createOrUpdateUser(firebaseUser: FirebaseUser): User {
        val existingUser = userDao.getUserById(firebaseUser.uid)
        return if (existingUser != null) {
            existingUser.toDomain()
        } else {
            val newUser = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "User"
            )
            saveUser(newUser)
            newUser
        }
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return userDao.getUserById(firebaseUser.uid)?.toDomain()
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomain()
    }

    suspend fun saveUser(user: User) {
        userDao.insertUser(UserEntity.fromDomain(user))
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(UserEntity.fromDomain(user))
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(UserEntity.fromDomain(user))
        auth.currentUser?.delete()?.await()
    }

    // Favorites Management
    suspend fun addToFavorites(userId: String, listId: String) {
        val user = getUserById(userId) ?: return
        val currentFavorites = user.favoriteListIds
        val updatedFavorites = currentFavorites + listId
        userDao.updateFavorites(userId, updatedFavorites)
    }

    suspend fun removeFromFavorites(userId: String, listId: String) {
        val user = getUserById(userId) ?: return
        val currentFavorites = user.favoriteListIds
        val updatedFavorites = currentFavorites.filterNot { it == listId }
        userDao.updateFavorites(userId, updatedFavorites)
    }

    fun getFavoriteListIds(userId: String): Flow<List<String>> {
        return userDao.getFavoriteListIds(userId)
    }

    suspend fun doesUserExist(email: String): Boolean {
        return userDao.doesUserExist(email)
    }

    // Helper Methods
    private operator fun List<String>.plus(element: String): List<String> {
        return if (element in this) this else this + element
    }

    private operator fun List<String>.minus(element: String): List<String> {
        return filterNot { it == element }
    }
}
