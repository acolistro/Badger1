package com.example.badger.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * CryptoManager handles encryption and decryption operations using standard Android cryptography.
 */
class CryptoManager @Inject constructor(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "badger_master_key"
        private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val ENCRYPTED_PREFS_FILE = "badger_secure_prefs"
        private const val KEY_IV_SEPARATOR = "::"
    }

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Creates or retrieves the master key from Android Keystore
     * @return The master secret key
     */
    fun getMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // Check if key exists
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            // Create key if it doesn't exist
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        // Retrieve existing key
        val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * Generates a random key for AES encryption
     * @return Base64 encoded random key
     */
    fun generateRandomKey(): String {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGenerator.init(256)
        val key = keyGenerator.generateKey()
        return Base64.encodeToString(key.encoded, Base64.NO_WRAP)
    }

    /**
     * Generates a random IV (Initialization Vector) for AES encryption
     * @return Random IV bytes
     */
    private fun generateIv(): ByteArray {
        val iv = ByteArray(12) // 12 bytes IV for GCM
        SecureRandom().nextBytes(iv)
        return iv
    }

    /**
     * Encrypts a string using AES-GCM
     * @param plaintext The string to encrypt
     * @param associatedData Optional authentication data
     * @return Base64 encoded string containing IV and ciphertext
     */
    fun encryptString(plaintext: String, associatedData: String? = null): String {
        val masterKey = getMasterKey()
        val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8)
        val iv = generateIv()

        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec)

        // Add associated data if provided
        associatedData?.let {
            cipher.updateAAD(it.toByteArray(StandardCharsets.UTF_8))
        }

        val encrypted = cipher.doFinal(plaintextBytes)

        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts a string using AES-GCM
     * @param encryptedText Base64 encoded string containing IV and ciphertext
     * @param associatedData Optional authentication data
     * @return Original plaintext
     */
    fun decryptString(encryptedText: String, associatedData: String? = null): String {
        val masterKey = getMasterKey()
        val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

        // Extract IV and ciphertext
        val iv = ByteArray(12)
        val ciphertext = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        System.arraycopy(combined, iv.size, ciphertext, 0, ciphertext.size)

        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)

        // Add associated data if provided
        associatedData?.let {
            cipher.updateAAD(it.toByteArray(StandardCharsets.UTF_8))
        }

        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, StandardCharsets.UTF_8)
    }

    /**
     * Encrypts data with a specific key
     * @param data Data to encrypt
     * @param keyBase64 Base64 encoded encryption key
     * @return Base64 encoded string containing IV and ciphertext
     */
    fun encryptWithKey(data: String, keyBase64: String): String {
        val key = Base64.decode(keyBase64, Base64.NO_WRAP)
        val secretKey = SecretKeySpec(key, "AES")
        val plaintextBytes = data.toByteArray(StandardCharsets.UTF_8)
        val iv = generateIv()

        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val encrypted = cipher.doFinal(plaintextBytes)

        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts data with a specific key
     * @param encryptedData Base64 encoded string containing IV and ciphertext
     * @param keyBase64 Base64 encoded decryption key
     * @return Original plaintext
     */
    fun decryptWithKey(encryptedData: String, keyBase64: String): String {
        val key = Base64.decode(keyBase64, Base64.NO_WRAP)
        val secretKey = SecretKeySpec(key, "AES")
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

        // Extract IV and ciphertext
        val iv = ByteArray(12)
        val ciphertext = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        System.arraycopy(combined, iv.size, ciphertext, 0, ciphertext.size)

        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, StandardCharsets.UTF_8)
    }

    /**
     * Stores an encrypted value in secure shared preferences
     * @param key The preference key
     * @param value The value to encrypt and store
     */
    fun storeSecurePreference(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    /**
     * Retrieves an encrypted value from secure shared preferences
     * @param key The preference key
     * @param defaultValue Default value if key not found
     * @return The decrypted value or defaultValue if not found
     */
    fun getSecurePreference(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }

    /**
     * Derives a key from a password using a salt
     * @param password The password to derive from
     * @param salt Random salt bytes
     * @return Base64 encoded derived key
     */
    fun deriveKeyFromPassword(password: String, salt: ByteArray): String {
        // In a real implementation, you'd use PBKDF2 with many iterations
        // This is a simplified version for demonstration
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
        val saltedPassword = ByteArray(passwordBytes.size + salt.size)
        System.arraycopy(passwordBytes, 0, saltedPassword, 0, passwordBytes.size)
        System.arraycopy(salt, 0, saltedPassword, passwordBytes.size, salt.size)

        // Use the master key to encrypt the salted password
        val encrypted = encryptString(Base64.encodeToString(saltedPassword, Base64.NO_WRAP))

        // Use the first 32 bytes as the derived key
        val derivedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val keyBytes = ByteArray(32)
        System.arraycopy(derivedBytes, 0, keyBytes, 0, keyBytes.size.coerceAtMost(derivedBytes.size))

        return Base64.encodeToString(keyBytes, Base64.NO_WRAP)
    }
}
