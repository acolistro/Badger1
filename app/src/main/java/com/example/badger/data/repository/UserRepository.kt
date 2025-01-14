package com.example.badger.data.repository

import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.local.entities.UserEntity
import com.example.badger.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Auth Methods
    suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Sign in succeeded but user is null")
        createOrUpdateUser(firebaseUser)
    }

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Sign up succeeded but user is null")

        val newUser = User(
            id = firebaseUser.uid,
            email = email,
            username = username,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            emailVerified = false,
            phoneVerified = false,
            favoriteListIds = emptyList()
        )
        saveUser(newUser)
        newUser
    }

    private suspend fun createOrUpdateUser(firebaseUser: FirebaseUser): User {
        // Reload user to get latest verification status
        firebaseUser.reload().await()

        val existingUser = userDao.getUserById(firebaseUser.uid)
        return if (existingUser != null) {
            val updatedUser = existingUser.toDomain().copy(
                emailVerified = firebaseUser.isEmailVerified
            )
            saveUser(updatedUser)
            updatedUser
        } else {
            // Get user data from Firestore
            val firestoreUser = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val newUser = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                username = firestoreUser.getString("username")
                    ?: firebaseUser.displayName
                    ?: firebaseUser.email?.substringBefore('@')
                    ?: "User",
                firstName = firestoreUser.getString("firstName") ?: "",
                lastName = firestoreUser.getString("lastName") ?: "",
                phone = firestoreUser.getString("phone") ?: "",
                emailVerified = firebaseUser.isEmailVerified,
                phoneVerified = firestoreUser.getBoolean("phoneVerified") ?: false,
                favoriteListIds = (firestoreUser.get("favoriteListIds") as? List<String>) ?: emptyList()
            )
            saveUser(newUser)
            newUser
        }
    }

    suspend fun resendEmailVerification(): Result<Unit> = runCatching {
        val currentUser = auth.currentUser ?: throw IllegalStateException("No user is signed in")
        currentUser.sendEmailVerification().await()
    }

    suspend fun checkEmailVerification(): Result<Boolean> = runCatching {
        val currentUser = auth.currentUser ?: throw IllegalStateException("No user is signed in")
        currentUser.reload().await()
        val isVerified = currentUser.isEmailVerified

        if (isVerified) {
            updateVerificationStatus(currentUser.uid, emailVerified = true)
        }

        isVerified
    }

    suspend fun updateVerificationStatus(userId: String, emailVerified: Boolean? = null, phoneVerified: Boolean? = null) {
        val user = getUserById(userId) ?: return
        val updatedUser = user.copy(
            emailVerified = emailVerified ?: user.emailVerified,
            phoneVerified = phoneVerified ?: user.phoneVerified
        )
        updateUser(updatedUser)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    // Database Methods
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
        firestore.collection("users")
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(UserEntity.fromDomain(user))
        // Also update Firestore
        firestore.collection("users")
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(UserEntity.fromDomain(user))
        auth.currentUser?.delete()?.await()
        // Also delete from Firestore
        firestore.collection("users")
            .document(user.id)
            .delete()
            .await()
    }

    suspend fun addToFavorites(userId: String, listId: String) {
        val user = getUserById(userId) ?: return
        val currentFavorites = user.favoriteListIds
        if (currentFavorites.size >= 3) {
            throw IllegalStateException("Maximum number of favorites (3) reached")
        }
        val updatedFavorites = currentFavorites + listId
        userDao.updateFavorites(userId, updatedFavorites)
        // Update Firestore
        firestore.collection("users")
            .document(userId)
            .update("favoriteListIds", updatedFavorites)
            .await()
    }

    suspend fun removeFromFavorites(userId: String, listId: String) {
        val user = getUserById(userId) ?: return
        val currentFavorites = user.favoriteListIds
        val updatedFavorites = currentFavorites.filterNot { it == listId }
        userDao.updateFavorites(userId, updatedFavorites)
        firestore.collection("users")
            .document(userId)
            .update("favoriteListIds", updatedFavorites)
            .await()
    }

    fun getFavoriteListIds(userId: String): Flow<List<String>> {
        return userDao.getFavoriteListIds(userId)
    }

    suspend fun doesUserExist(email: String): Boolean {
        return userDao.doesUserExist(email)
    }
}
