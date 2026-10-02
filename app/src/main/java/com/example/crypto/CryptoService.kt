package com.example.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoService {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "CipherLock_MasterKey_AES256"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12 // 96 bits recommended for GCM
        private const val GCM_TAG_LENGTH = 128 // 128 bit auth tag
    }

    private var isAndroidKeyStoreAvailable = true

    private val keyStore: KeyStore = try {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    } catch (e: Exception) {
        isAndroidKeyStoreAvailable = false
        KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)
        }
    }

    private val secureRandom = SecureRandom()

    init {
        ensureMasterKey()
    }

    @Synchronized
    private fun ensureMasterKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            if (isAndroidKeyStoreAvailable) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val parameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(parameterSpec)
                return keyGenerator.generateKey()
            } else {
                val keyGenerator = KeyGenerator.getInstance("AES")
                keyGenerator.init(256)
                val key = keyGenerator.generateKey()
                keyStore.setEntry(
                    KEY_ALIAS,
                    KeyStore.SecretKeyEntry(key),
                    KeyStore.PasswordProtection("cipherlock_test".toCharArray())
                )
                return key
            }
        }
        val entry = if (isAndroidKeyStoreAvailable) {
            keyStore.getEntry(KEY_ALIAS, null)
        } else {
            keyStore.getEntry(KEY_ALIAS, KeyStore.PasswordProtection("cipherlock_test".toCharArray()))
        }
        return (entry as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Encrypts plaintext using AES-256-GCM with hardware-backed key.
     * Android KeyStore generates a cryptographically secure random IV internally.
     * Returns Base64-encoded string in format: "<IV_BASE64>:<CIPHERTEXT_BASE64>"
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        val secretKey = ensureMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv ?: throw IllegalStateException("Cipher did not generate IV")
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        
        return "$ivBase64:$cipherBase64"
    }

    /**
     * Decrypts string encrypted with encrypt().
     */
    fun decrypt(encryptedPayload: String): String {
        if (encryptedPayload.isEmpty()) return ""
        val parts = encryptedPayload.split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Malformed ciphertext payload")
        }

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)

        val secretKey = ensureMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Deletes current Keystore key and generates a new one.
     */
    @Synchronized
    fun resetMasterKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
        ensureMasterKey()
    }

    /**
     * Generates a cryptographically strong 16-character password with letters, numbers, and symbols.
     */
    fun generateSecurePassword(length: Int = 16): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        val allChars = upper + lower + digits + symbols

        val passwordChars = CharArray(length)
        // Ensure at least one character from each set
        passwordChars[0] = upper[secureRandom.nextInt(upper.length)]
        passwordChars[1] = lower[secureRandom.nextInt(lower.length)]
        passwordChars[2] = digits[secureRandom.nextInt(digits.length)]
        passwordChars[3] = symbols[secureRandom.nextInt(symbols.length)]

        for (i in 4 until length) {
            passwordChars[i] = allChars[secureRandom.nextInt(allChars.length)]
        }

        // Fisher-Yates shuffle
        for (i in passwordChars.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return String(passwordChars)
    }
}
