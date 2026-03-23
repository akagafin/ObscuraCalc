package com.obscuracalc.app

import android.app.Application
import com.obscuracalc.core.security.AndroidVaultAuthManager
import com.obscuracalc.core.vault.EncryptedBackupService
import com.obscuracalc.core.vault.RoomVaultRepository
import com.obscuracalc.core.vault.db.VaultDatabase

class AppContainer(application: Application) {
    val authManager = AndroidVaultAuthManager(application)
    private val vaultDatabase = VaultDatabase.build(application)
    val vaultRepository = RoomVaultRepository(
        context = application,
        database = vaultDatabase,
        sessionKeyProvider = authManager,
    )
    val backupService = EncryptedBackupService(
        context = application,
        repository = vaultRepository,
    )

    init {
        authManager.registerWipeHandler {
            vaultRepository.wipeAll()
        }
    }
}
