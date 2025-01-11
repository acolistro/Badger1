package com.example.badger.data.remote

import com.example.badger.data.model.SharedList
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createList(title: String): SharedList {
        val userId = getCurrentUserId() // You'll need to implement this
        val timestamp = System.currentTimeMillis()

        val list = SharedList(
            id = "",
            title = title,
            createdBy = userId,
            createdAt = timestamp,
            lastModifiedBy = userId,
            lastModifiedAt = timestamp,
            sharedWithUsers = listOf(userId),
            items = emptyList(),
            isFavorite = false
        )

        val docRef = firestore.collection("lists").document()
        val listWithId = list.copy(id = docRef.id)
        docRef.set(listWithId).await()

        return listWithId
    }

    suspend fun updateFavorite(listId: String, favorite: Boolean) {
        firestore.collection("lists").document(listId)
            .update("isFavorite", favorite)
            .await()
    }

    private fun getCurrentUserId(): String {
        // Implement getting current user ID from Firebase Auth
        // For now, returning a placeholder
        return "current_user_id"
    }
}
