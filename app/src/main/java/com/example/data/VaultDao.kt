package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllItems(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: Long): VaultItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem): Long

    @Update
    suspend fun updateItem(item: VaultItem)

    @Delete
    suspend fun deleteItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM vault_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun getCount(): Int
}
