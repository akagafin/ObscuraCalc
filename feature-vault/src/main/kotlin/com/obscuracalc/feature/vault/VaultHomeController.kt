package com.obscuracalc.feature.vault

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.obscuracalc.core.vault.BackupService
import com.obscuracalc.core.vault.VaultRepository
import com.obscuracalc.core.vault.model.VaultEntrySummary
import com.obscuracalc.core.vault.model.VaultOpenedEntry

data class VaultHomeUiState(
    val entries: List<VaultEntrySummary> = emptyList(),
    val previewEntry: VaultOpenedEntry? = null,
    val isBusy: Boolean = false,
    val message: String? = null,
)

class VaultHomeController(
    private val repository: VaultRepository,
    private val backupService: BackupService,
) {
    var state by mutableStateOf(VaultHomeUiState())
        private set

    suspend fun refresh() {
        state = state.copy(isBusy = true, message = null)
        state = runCatching {
            state.copy(entries = repository.listEntries(), isBusy = false)
        }.getOrElse { error ->
            state.copy(isBusy = false, message = error.message ?: "Unable to refresh vault entries")
        }
    }

    suspend fun importFile(uri: Uri) {
        state = state.copy(isBusy = true, message = null)
        state = runCatching {
            repository.importFromUri(uri)
            state.copy(entries = repository.listEntries(), isBusy = false, message = "Imported into vault")
        }.getOrElse { error ->
            state.copy(isBusy = false, message = error.message ?: "Unable to import file")
        }
    }

    suspend fun exportFile(id: String, uri: Uri) {
        state = runCatching {
            repository.exportToUri(id, uri)
            state.copy(message = "Export complete")
        }.getOrElse { error ->
            state.copy(message = error.message ?: "Unable to export file")
        }
    }

    suspend fun deleteFile(id: String) {
        state = runCatching {
            if (state.previewEntry?.summary?.id == id) {
                clearPreview()
            }
            repository.deleteEntry(id)
            state.copy(entries = repository.listEntries(), message = "Entry deleted")
        }.getOrElse { error ->
            state.copy(message = error.message ?: "Unable to delete entry")
        }
    }

    suspend fun loadPreview(id: String) {
        state = runCatching {
            clearPreview()
            state.copy(previewEntry = repository.openEntry(id), message = null)
        }.getOrElse { error ->
            state.copy(message = error.message ?: "Unable to load preview")
        }
    }

    fun dismissPreview() {
        clearPreview()
        state = state.copy(previewEntry = null)
    }

    suspend fun createBackup(uri: Uri, passphrase: CharArray) {
        state = state.copy(isBusy = true, message = null)
        state = runCatching {
            backupService.createBackup(uri, passphrase)
            state.copy(isBusy = false, message = "Encrypted backup created")
        }.getOrElse { error ->
            state.copy(isBusy = false, message = error.message ?: "Unable to create backup")
        }
    }

    suspend fun restoreBackup(uri: Uri, passphrase: CharArray) {
        state = state.copy(isBusy = true, message = null)
        state = runCatching {
            backupService.restoreBackup(uri, passphrase)
            state.copy(
                entries = repository.listEntries(),
                isBusy = false,
                message = "Backup restored into the current vault",
            )
        }.getOrElse { error ->
            state.copy(isBusy = false, message = error.message ?: "Unable to restore backup")
        }
    }

    private fun clearPreview() {
        state.previewEntry?.bytes?.fill(0)
    }
}
