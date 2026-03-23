package com.obscuracalc.core.vault.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_entries")
data class VaultEntryEntity(
    @PrimaryKey val id: String,
    val encryptedMetadata: ByteArray,
    val blobFileName: String,
)
