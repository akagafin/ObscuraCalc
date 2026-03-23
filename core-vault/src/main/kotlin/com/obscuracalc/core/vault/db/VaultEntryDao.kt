package com.obscuracalc.core.vault.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VaultEntryDao {
    @Query("SELECT * FROM vault_entries ORDER BY id DESC")
    suspend fun listAll(): List<VaultEntryEntity>

    @Query("SELECT * FROM vault_entries WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): VaultEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: VaultEntryEntity)

    @Query("DELETE FROM vault_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM vault_entries")
    suspend fun clear()
}
