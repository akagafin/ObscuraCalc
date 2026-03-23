package com.obscuracalc.core.vault

import android.net.Uri

interface BackupService {
    suspend fun createBackup(destinationUri: Uri, passphrase: CharArray)
    suspend fun restoreBackup(sourceUri: Uri, passphrase: CharArray)
}
