package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.crypto.CryptoService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaultRepository(
    private val vaultDao: VaultDao,
    private val cryptoService: CryptoService,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cipherlock_secure_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENCRYPTED_PIN = "enc_pin"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_AUTO_LOCK_SECONDS = "auto_lock_seconds"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    // Expose all items flow
    val allItems: Flow<List<VaultItem>> = vaultDao.getAllItems()

    suspend fun getItemDecrypted(id: Long): DecryptedVaultItem? {
        val item = vaultDao.getItemById(id) ?: return null
        return DecryptedVaultItem(
            id = item.id,
            title = item.title,
            username = safeDecrypt(item.encryptedUsername),
            password = safeDecrypt(item.encryptedPassword),
            notes = safeDecrypt(item.encryptedNotes),
            category = item.category,
            updatedAt = item.updatedAt
        )
    }

    suspend fun saveItem(
        id: Long = 0L,
        title: String,
        username: String,
        password: String,
        notes: String,
        category: String = "LOGIN"
    ): Long {
        val encryptedUser = cryptoService.encrypt(username)
        val encryptedPass = cryptoService.encrypt(password)
        val encryptedNotes = cryptoService.encrypt(notes)

        val item = VaultItem(
            id = id,
            title = title.ifBlank { "UNTITLED ENTRY" },
            encryptedUsername = encryptedUser,
            encryptedPassword = encryptedPass,
            encryptedNotes = encryptedNotes,
            category = category,
            updatedAt = System.currentTimeMillis()
        )

        return if (id == 0L) {
            vaultDao.insertItem(item)
        } else {
            vaultDao.updateItem(item)
            id
        }
    }

    suspend fun deleteItem(id: Long) {
        vaultDao.deleteItemById(id)
    }

    suspend fun wipeAllVault() {
        vaultDao.deleteAll()
        cryptoService.resetMasterKey()
        prefs.edit().clear().apply()
    }

    private fun safeDecrypt(ciphertext: String): String {
        return try {
            if (ciphertext.isEmpty()) "" else cryptoService.decrypt(ciphertext)
        } catch (e: Exception) {
            "[DECRYPTION ERROR]"
        }
    }

    // PIN & Security Preferences
    fun isPinConfigured(): Boolean {
        val encPin = prefs.getString(KEY_ENCRYPTED_PIN, null)
        return !encPin.isNullOrEmpty()
    }

    fun setPin(pin: String) {
        val encryptedPin = cryptoService.encrypt(pin)
        prefs.edit().putString(KEY_ENCRYPTED_PIN, encryptedPin).apply()
    }

    fun verifyPin(pinInput: String): Boolean {
        val encPin = prefs.getString(KEY_ENCRYPTED_PIN, null) ?: return false
        return try {
            val decryptedPin = cryptoService.decrypt(encPin)
            decryptedPin == pinInput
        } catch (e: Exception) {
            false
        }
    }

    fun isOnboardingDone(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
    }

    fun getAutoLockSeconds(): Int {
        return prefs.getInt(KEY_AUTO_LOCK_SECONDS, 30) // default 30s
    }

    fun setAutoLockSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_AUTO_LOCK_SECONDS, seconds).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun generatePassword(): String {
        return cryptoService.generateSecurePassword(16)
    }
}
