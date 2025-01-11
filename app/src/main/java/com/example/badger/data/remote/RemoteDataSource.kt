package com.example.badger.data.remote

import com.example.badger.data.model.SharedList
import com.example.badger.data.model.ListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
        doc.toObject(SharedList::class.java) ?: throw IllegalStateException("List not found")
    }

    suspend fun updateList(list: SharedList): Result<Unit> = runCatching {
        val userId = getCurrentUserId()
        val updatedList = list.copy(
            lastModifiedBy = userId,
            lastModifiedAt = System.currentTimeMillis()
        )
        listsCollection.document(list.id).set(updatedList).await()
    }

    suspend fun deleteList(listId: String): Result<Unit> = runCatching {
        listsCollection.document(listId).delete().await()
    }

    suspend fun updateFavorite(listId: String, favorite: Boolean): Result<Unit> = runCatching {
        listsCollection.document(listId)
            .update("isFavorite", favorite)
            .await()
    }

    suspend fun addItemToList(listId: String, item: ListItem): Result<Unit> = runCatching {
        val list = getList(listId).getOrThrow()
        val updatedItems = list.items + item

        listsCollection.document(listId)
            .update(
                mapOf(
                    "items" to updatedItems,
                    "lastModifiedBy" to getCurrentUserId(),
                    "lastModifiedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun removeItemFromList(listId: String, itemId: String): Result<Unit> = runCatching {
        val list = getList(listId).getOrThrow()
        val updatedItems = list.items.filterNot { it.id == itemId }

        listsCollection.document(listId)
            .update(
                mapOf(
                    "items" to updatedItems,
                    "lastModifiedBy" to getCurrentUserId(),
                    "lastModifiedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun shareListWithUser(listId: String, userEmail: String): Result<Unit> = runCatching {
        val list = getList(listId).getOrThrow()
        // Note: You might want to add a users collection and lookup by email
        if (userEmail !in list.sharedWithUsers) {
            val updatedSharedWith = list.sharedWithUsers + userEmail
            listsCollection.document(listId)
                .update("sharedWithUsers", updatedSharedWith)
                .await()
        }
    }

    suspend fun removeUserFromList(listId: String, userEmail: String): Result<Unit> = runCatching {
        val list = getList(listId).getOrThrow()
        if (userEmail in list.sharedWithUsers && userEmail != list.createdBy) {
            val updatedSharedWith = list.sharedWithUsers - userEmail
            listsCollection.document(listId)
                .update("sharedWithUsers", updatedSharedWith)
                .await()
        }
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")
    }
}
