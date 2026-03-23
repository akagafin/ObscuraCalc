package com.obscuracalc.core.vault

import android.net.Uri
import com.obscuracalc.core.vault.model.VaultEntrySummary
import com.obscuracalc.core.vault.model.VaultOpenedEntry

interface VaultRepository {
    suspend fun importFromUri(uri: Uri): VaultEntrySummary
    suspend fun exportToUri(id: String, uri: Uri)
    suspend fun listEntries(): List<VaultEntrySummary>
    suspend fun openEntry(id: String): VaultOpenedEntry
    suspend fun deleteEntry(id: String)
    suspend fun wipeAll()
}
