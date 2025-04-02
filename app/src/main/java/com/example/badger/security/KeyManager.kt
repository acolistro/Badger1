package com.example.badger.security

import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.badger.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Named

/**
 * KeyManager handles encryption key generation, storage, and sharing between users.
 */
class KeyManager @Inject constructor(
    private val cryptoManager: CryptoManager,
    @Named("auth") private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {
    companion object {
        private const val KEY_COLLECTION = "encryption_keys"
        private const val USER_PUBLIC_KEYS = "public_keys"
        private const val LIST_KEYS = "list_keys"
        private const val RSA_ALGORITHM = "RSA"
        private const val RSA_KEY_SIZE = 2048
        private const val RSA_CIPHER_TRANSFORM = "RSA/ECB/PKCS1Padding"
        private const val PRIVATE_KEY_PREF_PREFIX = "private_key_"
    }

    /**
     * Generates a random key for AES-256 encryption
     * @return Base64 encoded random key
     */
    fun generateRandomKey(): String {
        return cryptoManager.generateRandomKey()
    }

    /**
     * Generates an RSA key pair for asymmetric encryption
     * @return KeyPair containing public and private keys
     */
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        keyPairGenerator.initialize(RSA_KEY_SIZE, SecureRandom())
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Stores the user's public key in Firestore
     * Private key is stored locally in encrypted form
     * @param userId The user ID
     * @param keyPair The generated key pair
     */
    suspend fun storeKeyPair(userId: String, keyPair: KeyPair) {
        // Store public key in Firestore
        val publicKeyBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)

        firestore.collection(USER_PUBLIC_KEYS)
            .document(userId)
            .set(mapOf("publicKey" to publicKeyBase64))
            .await()

        // Store private key locally (encrypted)
        val privateKeyBase64 = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        val encryptedPrivateKey = cryptoManager.encryptString(privateKeyBase64, "user:$userId:privateKey")
        cryptoManager.storeSecurePreference("${PRIVATE_KEY_PREF_PREFIX}$userId", encryptedPrivateKey)
    }

    /**
     * Retrieves a user's public key from Firestore
     * @param userId The user ID
     * @return Base64 encoded public key or null if not found
     */
    suspend fun getPublicKey(userId: String): String? {
        val doc = firestore.collection(USER_PUBLIC_KEYS)
            .document(userId)
            .get()
            .await()

        return doc.getString("publicKey")
    }

    /**
     * Retrieves the current user's private key
     * @return The private key or null if not found
     */
    private fun getPrivateKey(userId: String): java.security.PrivateKey? {
        try {
            val encryptedPrivateKey = cryptoManager.getSecurePreference("${PRIVATE_KEY_PREF_PREFIX}$userId")
                ?: return null

            val privateKeyBase64 = cryptoManager.decryptString(encryptedPrivateKey, "user:$userId:privateKey")
            val privateKeyBytes = Base64.decode(privateKeyBase64, Base64.NO_WRAP)

            val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
            val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            return keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve private key for user $userId")
            return null
        }
    }

    /**
     * Encrypts data with the user's public key
     * @param data The data to encrypt
     * @param userPublicKeyBase64 The user's public key in Base64
     * @return Encrypted data in Base64
     */
    private fun encryptWithPublicKey(data: String, userPublicKeyBase64: String): String {
        val publicKeyBytes = Base64.decode(userPublicKeyBase64, Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    /**
     * Decrypts data with the user's private key
     * @param encryptedData The encrypted data in Base64
     * @param userId The user ID
     * @return Decrypted data
     */
    private fun decryptWithPrivateKey(encryptedData: String, userId: String): String {
        val privateKey = getPrivateKey(userId)
            ?: throw IllegalStateException("Private key not found for user $userId")

        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)

        val cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted)
    }

    /**
     * Creates and stores encryption keys for a new list
     * @param listId The ID of the new list
     * @return Symmetric key for list encryption
     */
    suspend fun createListKey(listId: String): String {
        // Generate a new symmetric key for this list
        val listKey = generateRandomKey()

        // Get current user ID
        val currentUserId = getCurrentUserId()

        // Store the encrypted list key for the creator
        storeListKeyForUser(listId, currentUserId, listKey)

        return listKey
    }

    /**
     * Stores an encrypted list key for a specific user
     * In E2EE, list keys are encrypted with each user's public key before storage
     * @param listId The list ID
     * @param userId The user ID to share with
     * @param listKey The symmetric key for the list
     */
    suspend fun storeListKeyForUser(listId: String, userId: String, listKey: String) {
        try {
            // Get the user's public key
            val publicKeyBase64 = getPublicKey(userId)
                ?: throw IllegalStateException("Public key not found for user $userId")

            // Encrypt the list key with the user's public key
            val encryptedListKey = encryptWithPublicKey(listKey, publicKeyBase64)

            // Store the encrypted key in Firestore
            firestore.collection(KEY_COLLECTION)
                .document(listId)
                .collection("user_keys")
                .document(userId)
                .set(mapOf("encryptedKey" to encryptedListKey))
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to store list key for user $userId")
            throw e
        }
    }

    /**
     * Retrieves and decrypts a list key for the current user
     * @param listId The list ID
     * @return The decrypted symmetric key for the list
     */
    suspend fun getListKey(listId: String): String {
        val currentUserId = getCurrentUserId()
        return getListKeyForUser(listId, currentUserId)
    }

    /**
     * Retrieves and decrypts a list key for a specific user
     * @param listId The list ID
     * @param userId The user ID
     * @return The decrypted symmetric key for the list
     */
    suspend fun getListKeyForUser(listId: String, userId: String): String {
        try {
            // Get the encrypted key from Firestore
            val doc = firestore.collection(KEY_COLLECTION)
                .document(listId)
                .collection("user_keys")
                .document(userId)
                .get()
                .await()

            val encryptedListKey = doc.getString("encryptedKey")
                ?: throw IllegalStateException("Encrypted key not found for user $userId on list $listId")

            // Decrypt the list key using the user's private key
            return decryptWithPrivateKey(encryptedListKey, userId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get list key for user $userId on list $listId")
            throw e
        }
    }

    /**
     * Shares a list with another user by encrypting the list key for them
     * @param listId The list ID to share
     * @param targetUserId The user ID to share with
     */
    suspend fun shareListWithUser(listId: String, targetUserId: String) {
        try {
            // 1. Get the list key (current user must have access)
            val listKey = getListKey(listId)

            // 2. Encrypt and store the key for the target user
            storeListKeyForUser(listId, targetUserId, listKey)
        } catch (e: Exception) {
            Timber.e(e, "Failed to share list $listId with user $targetUserId")
            throw e
        }
    }

    /**
     * Revokes a user's access to a list by removing their encrypted key
     * @param listId The list ID
     * @param userId The user ID to revoke access from
     */
    suspend fun revokeListAccess(listId: String, userId: String) {
        firestore.collection(KEY_COLLECTION)
            .document(listId)
            .collection("user_keys")
            .document(userId)
            .delete()
            .await()
    }

    /**
     * Rotates a list's encryption key (for periodic security updates)
     * @param listId The list ID
     * @param userIds List of user IDs who should have access to the new key
     * @return The new list key
     */
    suspend fun rotateListKey(listId: String, userIds: List<String>): String {
        // Generate a new key
        val newListKey = generateRandomKey()

        // Store it for each user
        userIds.forEach { userId ->
            storeListKeyForUser(listId, userId, newListKey)
        }

        return newListKey
    }

    /**
     * Helper method to get the current user ID
     * @return Current user ID
     * @throws IllegalStateException if no user is logged in
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No user is signed in")
    }

    /**
     * Initialize a user's encryption keys when they first sign up
     * Generates and stores necessary keys
     * @param userId The user ID
     */
    suspend fun initializeUserKeys(userId: String) {
        // Generate a key pair
        val keyPair = generateKeyPair()

        // Store both public and private keys
        storeKeyPair(userId, keyPair)
    }
}
