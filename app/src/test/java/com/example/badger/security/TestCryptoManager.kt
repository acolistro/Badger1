package com.example.badger.test.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.badger.security.CryptoManager
import org.mockito.Mockito.mock

/**
 * Test implementation of CryptoManager for predictable encryption/decryption in tests.
 * Instead of actually encrypting, it just adds a prefix/suffix to make the "encrypted" content predictable.
 */
class TestCryptoManager(context: Context) : CryptoManager(context) {

    companion object {
        private const val ENC_PREFIX = "ENC("
        private const val ENC_SUFFIX = ")"
        private const val TEST_KEY_PREFIX = "test_key_for_"
    }

    // In-memory storage for secure preferences in tests
    private val securePrefs = mutableMapOf<String, String>()

    /**
     * Factory method to create TestCryptoManager with a mock context.
     */
    companion object {
        fun createWithMockContext(): TestCryptoManager {
            val mockContext = mock(Context::class.java)
            return TestCryptoManager(mockContext)
        }
    }

    /**
     * Override the encryption method to use a predictable pattern
     */
    override fun encryptString(plaintext: String, associatedData: String?): String {
        val associatedDataSuffix = associatedData?.let { ":$it" } ?: ""
        return "$ENC_PREFIX$plaintext$associatedDataSuffix$ENC_SUFFIX"
    }

    /**
     * Override the decryption method to reverse our predictable pattern
     */
    override fun decryptString(encryptedText: String, associatedData: String?): String {
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
     * Override key-based encryption for testing
     */
    override fun encryptWithKey(data: String, keyBase64: String): String {
        return "$ENC_PREFIX$data:key=$keyBase64$ENC_SUFFIX"
    }

    /**
     * Override key-based decryption for testing
     */
    override fun decryptWithKey(encryptedData: String, keyBase64: String): String {
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
     * Override random key generation for predictable tests
     */
    override fun generateRandomKey(): String {
        return TEST_KEY_PREFIX + System.currentTimeMillis()
    }

    /**
     * Override secure preferences retrieval for testing
     */
    override fun getSecurePreference(key: String, defaultValue: String?): String? {
        return securePrefs[key] ?: defaultValue
    }

    /**
     * Override secure preferences storage for testing
     */
    override fun storeSecurePreference(key: String, value: String) {
        securePrefs[key] = value
    }
}
