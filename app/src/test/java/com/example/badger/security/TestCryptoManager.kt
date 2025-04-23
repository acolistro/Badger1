package com.example.badger.test.security

import android.content.Context
import com.example.badger.security.CryptoManager
import org.mockito.kotlin.mock

/**
 * Test implementation of CryptoManager for predictable encryption/decryption in tests.
 * Since the original CryptoManager is final, we use delegation instead of inheritance.
 */
class TestCryptoManagerWrapper(private val context: Context) {

    companion object {
        private const val ENC_PREFIX = "ENC("
        private const val ENC_SUFFIX = ")"
        private const val TEST_KEY_PREFIX = "test_key_for_"
    }

    // In-memory storage for secure preferences in tests
    private val securePrefs = mutableMapOf<String, String>()

    /**
     * Factory method to create with a mock context
     */
    companion object {
        fun createWithMockContext(): TestCryptoManagerWrapper {
            val mockContext = mock<Context>()
            return TestCryptoManagerWrapper(mockContext)
        }
    }

    /**
     * Simulated encryption for testing
     */
    fun encryptString(plaintext: String, associatedData: String? = null): String {
        val associatedDataSuffix = associatedData?.let { ":$it" } ?: ""
        return "$ENC_PREFIX$plaintext$associatedDataSuffix$ENC_SUFFIX"
    }

    /**
     * Simulated decryption for testing
     */
    fun decryptString(encryptedText: String, associatedData: String? = null): String {
        if (!encryptedText.startsWith(ENC_PREFIX) || !encryptedText.endsWith(ENC_SUFFIX)) {
            throw IllegalArgumentException("Invalid test encrypted text format")
        }

        val content = encryptedText.substring(ENC_PREFIX.length, encryptedText.length - ENC_SUFFIX.length)
        val associatedDataSuffix = associatedData?.let { ":$it" } ?: ""

        return if (content.endsWith(associatedDataSuffix)) {
            content.substring(0, content.length - associatedDataSuffix.length)
        } else {
            content
        }
    }

    /**
     * Simulated key-based encryption for testing
     */
    fun encryptWithKey(data: String, keyBase64: String): String {
        return "$ENC_PREFIX$data:key=$keyBase64$ENC_SUFFIX"
    }

    /**
     * Simulated key-based decryption for testing
     */
    fun decryptWithKey(encryptedData: String, keyBase64: String): String {
        if (!encryptedData.startsWith(ENC_PREFIX) || !encryptedData.endsWith(ENC_SUFFIX)) {
            throw IllegalArgumentException("Invalid test encrypted data format")
        }

        val content = encryptedData.substring(ENC_PREFIX.length, encryptedData.length - ENC_SUFFIX.length)
        val expectedKeySuffix = ":key=$keyBase64"

        return if (content.endsWith(expectedKeySuffix)) {
            content.substring(0, content.length - expectedKeySuffix.length)
        } else {
            throw IllegalArgumentException("Key mismatch in test decryption")
        }
    }

    /**
     * Generate a predictable key for testing
     */
    fun generateRandomKey(): String {
        return "$TEST_KEY_PREFIX${System.currentTimeMillis()}"
    }

    /**
     * Get from in-memory preferences for testing
     */
    fun getSecurePreference(key: String, defaultValue: String? = null): String? {
        return securePrefs[key] ?: defaultValue
    }

    /**
     * Store in in-memory preferences for testing
     */
    fun storeSecurePreference(key: String, value: String) {
        securePrefs[key] = value
    }
}