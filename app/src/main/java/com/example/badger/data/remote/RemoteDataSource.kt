package com.example.badger.data.remote

import com.example.badger.data.model.SharedList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val listsCollection = firestore.collection("lists")

    suspend fun createList(title: String): Result<SharedList> = runCatching {
        val userId = getCurrentUserId()
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

        val docRef = listsCollection.document()
        val listWithId = list.copy(id = docRef.id)
        docRef.set(listWithId).await()

        listWithId
    }

    suspend fun getList(listId: String): Result<SharedList> = runCatching {
        val doc = listsCollection.document(listId).get().await()
        doc.toObject(SharedList::class.java)
            ?: throw IllegalStateException("List not found")
    }

    suspend fun getAllLists(userId: String): List<SharedList> {
        val snapshot = listsCollection
            .whereArrayContains("sharedWithUsers", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(SharedList::class.java)
        }
    }

    suspend fun updateList(list: SharedList): Result<Unit> = runCatching {
        val userId = getCurrentUserId()
        val updatedList = list.copy(
            lastModifiedAt = System.currentTimeMillis(),
            lastModifiedBy = userId
        )
        listsCollection.document(list.id).set(updatedList).await()
    }

    suspend fun updateFavorite(listId: String, favorite: Boolean): Result<Unit> = runCatching {
        listsCollection.document(listId)
            .update(
                mapOf(
                    "isFavorite" to favorite,
                    "lastModifiedAt" to System.currentTimeMillis(),
                    "lastModifiedBy" to getCurrentUserId()
                )
            )
            .await()
    }

    suspend fun deleteList(listId: String): Result<Unit> = runCatching {
        listsCollection.document(listId).delete().await()
    }

    suspend fun shareList(listId: String, userId: String): Result<Unit> = runCatching {
        val list = getList(listId).getOrThrow()
        if (userId !in list.sharedWithUsers) {
            listsCollection.document(listId)
                .update(
                    mapOf(
                        "sharedWithUsers" to list.sharedWithUsers + userId,
                        "lastModifiedAt" to System.currentTimeMillis(),
                        "lastModifiedBy" to getCurrentUserId()
                    )
                )
                .await()
        }
    }

    suspend fun removeUserFromList(listId: String, userId: String): Result<Unit> = runCatching {
        val list = getList(listId).getOrThrow()
        if (userId in list.sharedWithUsers && userId != list.createdBy) {
            listsCollection.document(listId)
                .update(
                    mapOf(
                        "sharedWithUsers" to list.sharedWithUsers - userId,
                        "lastModifiedAt" to System.currentTimeMillis(),
                        "lastModifiedBy" to getCurrentUserId()
                    )
                )
                .await()
        }
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")
    }
}
