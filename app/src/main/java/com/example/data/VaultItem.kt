package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val encryptedUsername: String,
    val encryptedPassword: String,
    val encryptedNotes: String,
    val category: String = "LOGIN",
    val updatedAt: Long = System.currentTimeMillis()
)

data class DecryptedVaultItem(
    val id: Long = 0L,
    val title: String,
    val username: String,
    val password: String,
    val notes: String,
    val category: String = "LOGIN",
    val updatedAt: Long = System.currentTimeMillis()
)
