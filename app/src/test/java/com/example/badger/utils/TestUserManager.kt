package com.example.badger.utils

import com.example.badger.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class TestUserManager @Inject constructor(
    @Named("auth") private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TEST_PASSWORD = "BadgerTest123!"
        private const val USERS_COLLECTION = "users"
    }

    fun createTestUser(index: Int = 1): Pair<User, String> {
        val uuid = UUID.randomUUID().toString()

        val user = User(
            id = uuid,
            firstName = "Test",
            lastName = "User$index",
            email = "test.user$index@badgerapp.test",
            phone = "+1555${String.format("%07d", index)}",
            username = "testuser$index",
            emailVerified = false,
            phoneVerified = false,
            favoriteListIds = emptyList()
        )

        return user to TEST_PASSWORD
    }

    suspend fun registerTestUser(index: Int = 1): User {
        val (user, password) = createTestUser(index)

        // Create Firebase Auth account
        val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()

        // Store user data in Firestore
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()

        return user
    }

    suspend fun cleanupTestUser(user: User) {
        try {
            // Delete from Firestore
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .delete()
                .await()

            // Delete from Auth
            auth.currentUser?.delete()?.await()
        } catch (e: Exception) {
            println("Error cleaning up test user: ${e.message}")
        }
    }

    suspend fun loginTestUser(user: User) {
        auth.signInWithEmailAndPassword(user.email, TEST_PASSWORD).await()
    }
}
