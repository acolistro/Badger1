package com.example.badger.data.repository

import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.local.entities.ListEntity
import com.example.badger.data.model.ListItem
import com.example.badger.data.model.SharedList
import com.example.badger.data.remote.RemoteDataSource
import com.example.badger.security.CryptoManager
import com.example.badger.security.KeyManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Enhanced ListRepository that handles E2EE operations for lists
 */
@Singleton
class EncryptedListRepository @Inject constructor(
    private val listDao: ListDao,
    private val remoteDataSource: RemoteDataSource,
    private val cryptoManager: CryptoManager,
    private val keyManager: KeyManager,
    @Named("auth") private val auth: FirebaseAuth
) {
    /**
     * Creates a new encrypted list
     * @param title The unencrypted title of the list
     * @return Result containing the created SharedList with decrypted data
     */
    suspend fun createList(title: String): Result<SharedList> {
        return try {
            // First create the list in remote to get an ID
            val result = remoteDataSource.createList("encrypted")

            if (result.isSuccess) {
                val list = result.getOrThrow()

                // Generate a unique encryption key for this list
                val listKey = keyManager.createListKey(list.id)

                // Encrypt the real title
                val encryptedTitle = cryptoManager.encryptWithKey(title, listKey)

                // Update the list with the encrypted title
                val encryptedList = list.copy(
                    title = encryptedTitle
                )

                // Update remote with encrypted data
                remoteDataSource.updateList(encryptedList)

                // Store in local database
                listDao.insertList(encryptedList.toEntity())

                // Return the decrypted list to the caller
                Result.success(list.copy(title = title))
            } else {
                result
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create encrypted list")
            Result.failure(e)
        }
    }

    /**
     * Gets all lists and decrypts them
     * @param userId The current user ID
     * @return Flow of decrypted SharedList objects
     */
    fun getAllLists(userId: String): Flow<List<SharedList>> {
        return listDao.getAllLists()
            .map { lists ->
                lists.map { encryptedEntity ->
                    try {
                        decryptListEntity(encryptedEntity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to decrypt list ${encryptedEntity.id}")
                        // Return entity with placeholder for decryption failure
                        encryptedEntity.toDomain().copy(
                            title = "[Encrypted - Key not available]"
                        )
                    }
                }
            }
    }

    /**
     * Gets favorite lists and decrypts them
     * @param userId The current user ID
     * @return Flow of decrypted SharedList objects marked as favorites
     */
    fun getFavoriteLists(userId: String): Flow<List<SharedList>> {
        return listDao.getFavoriteLists()
            .map { lists ->
                lists.map { encryptedEntity ->
                    try {
                        decryptListEntity(encryptedEntity)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to decrypt favorite list ${encryptedEntity.id}")
                        // Return entity with placeholder for decryption failure
                        encryptedEntity.toDomain().copy(
                            title = "[Encrypted - Key not available]"
                        )
                    }
                }
            }
    }

    /**
     * Gets a specific list by ID and decrypts it
     * @param listId The list ID
     * @return Result containing the decrypted SharedList
     */
    suspend fun getListById(listId: String): Result<SharedList> {
        return try {
            val encryptedEntity = listDao.getListById(listId)
                ?: return Result.failure(Exception("List not found"))

            Result.success(decryptListEntity(encryptedEntity))
        } catch (e: Exception) {
            Timber.e(e, "Failed to get list by ID: $listId")
            Result.failure(e)
        }
    }

    /**
     * Updates a list with encrypted data
     * @param list The SharedList with unencrypted data
     * @return Result indicating success or failure
     */
    suspend fun updateList(list: SharedList): Result<Unit> {
        return try {
            // Get the list key for encryption
            val listKey = keyManager.getListKey(list.id)

            // Encrypt sensitive fields
            val encryptedTitle = cryptoManager.encryptWithKey(list.title, listKey)
            val encryptedItems = list.items.map { item ->
                encryptListItem(item, listKey)
            }

            // Create encrypted list
            val encryptedList = list.copy(
                title = encryptedTitle,
                items = encryptedItems
            )

            // Update local database
            listDao.updateList(encryptedList.toEntity())

            // Update remote
            remoteDataSource.updateList(encryptedList)

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update list: ${list.id}")
            Result.failure(e)
        }
    }

    /**
     * Toggles a list's favorite status
     * @param listId The list ID
     * @param favorite The new favorite status
     * @return Result indicating success or failure
     */
    suspend fun toggleFavorite(listId: String, favorite: Boolean): Result<Unit> {
        if (favorite) {
            val currentFavorites = listDao.getFavoriteListsCount()
            if (currentFavorites >= 3) {
                return Result.failure(Exception("Maximum of 3 favorite lists allowed"))
            }
        }

        return try {
            listDao.updateFavorite(listId, favorite)
            remoteDataSource.updateFavorite(listId, favorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle favorite status for list: $listId")
            Result.failure(e)
        }
    }

    /**
     * Deletes a list
     * @param listId The list ID
     * @param listEntity The encrypted list entity
     * @return Result indicating success or failure
     */
    suspend fun deleteList(listId: String, listEntity: ListEntity): Result<Unit> {
        return try {
            listDao.deleteList(listEntity)
            remoteDataSource.deleteList(listId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete list: $listId")
            Result.failure(e)
        }
    }

    /**
     * Gets the count of favorite lists
     * @return The number of favorite lists
     */
    suspend fun getFavoriteListsCount(): Int {
        return listDao.getFavoriteListsCount()
    }

    /**
     * Synchronizes lists from remote source
     * @param userId The user ID
     */
    suspend fun syncLists(userId: String) {
        try {
            // Get all remote lists
            val remoteLists = remoteDataSource.getAllLists(userId)

            // Store them locally (they're already encrypted)
            listDao.insertLists(remoteLists.map { it.toEntity() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync lists")
            throw e
        }
    }

    /**
     * Shares a list with another user
     * @param listId The list ID to share
     * @param targetUserId The user ID to share with
     * @return Result indicating success or failure
     */
    suspend fun shareList(listId: String, targetUserId: String): Result<Unit> {
        return try {
            // Share the encryption key with the target user
            keyManager.shareListWithUser(listId, targetUserId)

            // Update remote permissions
            remoteDataSource.shareList(listId, targetUserId)

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to share list $listId with user $targetUserId")
            Result.failure(e)
        }
    }

    /**
     * Revokes a user's access to a list
     * @param listId The list ID
     * @param userId The user ID to revoke access from
     * @return Result indicating success or failure
     */
    suspend fun revokeAccess(listId: String, userId: String): Result<Unit> {
        return try {
            // Remove the user's access to the encryption key
            keyManager.revokeListAccess(listId, userId)

            // Update remote permissions
            remoteDataSource.removeUserFromList(listId, userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to revoke access to list $listId from user $userId")
            Result.failure(e)
        }
    }

    /**
     * Rotates a list's encryption key (for security)
     * @param listId The list ID
     * @param reencryptContent Whether to re-encrypt all content with the new key
     * @return Result indicating success or failure
     */
    suspend fun rotateListKey(listId: String, reencryptContent: Boolean = true): Result<Unit> {
        return try {
            // Get the current list
            val currentList = getListById(listId).getOrThrow()

            // Get all users who have access
            val users = currentList.sharedWithUsers

            // Generate a new key and give all users access
            val newListKey = keyManager.rotateListKey(listId, users)

            if (reencryptContent) {
                // Re-encrypt list content with new key
                val decryptedTitle = currentList.title // Already decrypted
                val encryptedTitle = cryptoManager.encryptWithKey(decryptedTitle, newListKey)

                // Re-encrypt items
                val encryptedItems = currentList.items.map { item ->
                    encryptListItem(item, newListKey)
                }

                // Update list with newly encrypted content
                val updatedList = currentList.copy(
                    title = encryptedTitle,
                    items = encryptedItems
                )

                // Save to local DB and remote
                listDao.updateList(updatedList.toEntity())
                remoteDataSource.updateList(updatedList)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to rotate key for list $listId")
            Result.failure(e)
        }
    }

    /**
     * Decrypts a list entity
     * @param encryptedEntity The encrypted list entity
     * @return Decrypted SharedList
     */
    private suspend fun decryptListEntity(encryptedEntity: ListEntity): SharedList {
        // Get the list key
        val listKey = keyManager.getListKey(encryptedEntity.id)

        // Decrypt the title
        val decryptedTitle = cryptoManager.decryptWithKey(encryptedEntity.title, listKey)

        // Decrypt items
        val decryptedItems = encryptedEntity.items.map { encryptedItem ->
            decryptListItem(encryptedItem, listKey)
        }

        // Return decrypted SharedList
        return encryptedEntity.toDomain().copy(
            title = decryptedTitle,
            items = decryptedItems
        )
    }

    /**
     * Encrypts a list item
     * @param item The list item to encrypt
     * @param listKey The list encryption key
     * @return Encrypted list item
     */
    private fun encryptListItem(item: ListItem, listKey: String): ListItem {
        // Encrypt content field
        val encryptedContent = cryptoManager.encryptWithKey(item.content, listKey)

        // Return item with encrypted content
        return item.copy(
            content = encryptedContent
        )
    }

    /**
     * Decrypts a list item
     * @param item The list item to decrypt
     * @param listKey The list encryption key
     * @return Decrypted list item
     */
    private fun decryptListItem(item: ListItem, listKey: String): ListItem {
        // Decrypt content field
        val decryptedContent = cryptoManager.decryptWithKey(item.content, listKey)

        // Return item with decrypted content
        return item.copy(
            content = decryptedContent
        )
    }
}