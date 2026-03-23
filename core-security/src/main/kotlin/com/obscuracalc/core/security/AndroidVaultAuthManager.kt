package com.obscuracalc.core.security

import android.content.Context
import androidx.biometric.BiometricPrompt
import com.obscuracalc.core.security.model.AuthResult
import com.obscuracalc.core.security.model.BiometricAvailability
import com.obscuracalc.core.security.model.CredentialType
import com.obscuracalc.core.security.model.LockReason
import com.obscuracalc.core.security.model.SecuritySettings
import com.obscuracalc.core.security.model.StoredVaultConfig
import com.obscuracalc.core.security.model.VaultSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import java.util.Arrays
import javax.crypto.Cipher

class AndroidVaultAuthManager(
    private val context: Context,
    private var onWipeRequested: suspend () -> Unit = {},
) : VaultAuthManager {
    private val configStore = VaultConfigStore(context)
    private val keystoreKeyManager = KeystoreKeyManager()
    private val mutex = Mutex()
    private val mutableSessionState = MutableStateFlow(loadInitialState())
    private var sessionMasterKey: ByteArray? = null

    override val sessionState: StateFlow<VaultSessionState> = mutableSessionState.asStateFlow()

    fun registerWipeHandler(handler: suspend () -> Unit) {
        onWipeRequested = handler
    }

    override suspend fun isVaultConfigured(): Boolean = configStore.read() != null

    override suspend fun currentSettings(): SecuritySettings {
        return configStore.read()?.settings ?: SecuritySettings()
    }

    override suspend fun setupVault(
        credential: CharArray,
        credentialType: CredentialType,
        hiddenTrigger: CharArray?,
        lockTimeoutSeconds: Int,
        wipeAfterFailures: Int,
        decoyModeEnabled: Boolean,
    ): AuthResult = mutex.withLock {
        val credentialHashSalt = CredentialCrypto.randomBytes(16)
        val credentialWrapSalt = CredentialCrypto.randomBytes(16)
        val masterKey = CredentialCrypto.randomBytes(32)
        val wrappedDeviceKey = keystoreKeyManager.wrapWithDeviceKey(masterKey)
        val credentialHash = CredentialCrypto.deriveCredentialHash(credential, credentialHashSalt)
        val credentialWrapKey = CredentialCrypto.deriveWrappingKey(credential, credentialWrapSalt)
        val wrappedCredentialKey = AesGcm.wrapWithRawKey(masterKey, credentialWrapKey)
        val hasHiddenTrigger = hiddenTrigger != null && hiddenTrigger.isNotEmpty()
        val hiddenTriggerSalt = if (hasHiddenTrigger) CredentialCrypto.randomBytes(16) else null
        val hiddenTriggerHash = if (!hasHiddenTrigger) {
            null
        } else {
            CredentialCrypto.deriveCredentialHash(
                normalizeHiddenTrigger(hiddenTrigger!!),
                hiddenTriggerSalt!!
            )
        }
        val settings = SecuritySettings(
            hiddenTriggerEnabled = hasHiddenTrigger,
            lockTimeoutSeconds = lockTimeoutSeconds,
            wipeAfterFailures = wipeAfterFailures,
            decoyModeEnabled = decoyModeEnabled,
            biometricEnabled = false,
        )

        val storedConfig = StoredVaultConfig(
            credentialType = credentialType,
            credentialHash = credentialHash,
            credentialHashSalt = credentialHashSalt,
            credentialWrapSalt = credentialWrapSalt,
            wrappedMasterKeyByDevice = wrappedDeviceKey,
            wrappedMasterKeyByCredential = wrappedCredentialKey,
            wrappedMasterKeyByBiometric = null,
            hiddenTriggerHash = hiddenTriggerHash,
            hiddenTriggerSalt = hiddenTriggerSalt,
            settings = settings,
            failedAttempts = 0,
        )
        configStore.write(storedConfig)
        installSession(masterKey, storedConfig)
        credentialHash.fill(0)
        credentialWrapKey.fill(0)
        hiddenTriggerHash?.fill(0)
        CredentialCrypto.clear(credential)
        hiddenTrigger?.let(CredentialCrypto::clear)
        AuthResult.success()
    }

    override suspend fun unlockWithPin(pin: CharArray): AuthResult {
        return unlockWithCredential(pin, CredentialType.PIN)
    }

    override suspend fun unlockWithPassword(password: CharArray): AuthResult {
        return unlockWithCredential(password, CredentialType.PASSWORD)
    }

    override suspend fun lock(reason: LockReason) = mutex.withLock {
        clearSession()
        mutableSessionState.value = mutableSessionState.value.copy(isUnlocked = false)
    }

    override suspend fun updateSettings(transform: (SecuritySettings) -> SecuritySettings) =
        mutex.withLock {
            val existing = configStore.read() ?: return@withLock
            val transformed = transform(existing.settings)
            val updated = existing.copy(
                wrappedMasterKeyByBiometric = if (transformed.biometricEnabled) {
                    existing.wrappedMasterKeyByBiometric
                } else {
                    null
                },
                settings = transformed,
            )
            configStore.write(updated)
            mutableSessionState.value = mutableSessionState.value.copy(
                biometricEnabled = updated.settings.biometricEnabled,
            )
        }

    override suspend fun matchesHiddenTrigger(candidate: CharArray): Boolean {
        val config = configStore.read() ?: return false
        if (!config.settings.hiddenTriggerEnabled) {
            CredentialCrypto.clear(candidate)
            return false
        }
        val salt = config.hiddenTriggerSalt
        val expectedHash = config.hiddenTriggerHash
        if (salt == null || expectedHash == null) {
            CredentialCrypto.clear(candidate)
            return false
        }
        val actualHash =
            CredentialCrypto.deriveCredentialHash(normalizeHiddenTrigger(candidate), salt)
        CredentialCrypto.clear(candidate)
        return CredentialCrypto.constantTimeEquals(expectedHash, actualHash).also {
            actualHash.fill(0)
        }
    }

    override suspend fun clearVault() = mutex.withLock {
        clearVaultLocked()
    }

    override fun biometricAvailability(context: Context): BiometricAvailability {
        return when (
            androidx.biometric.BiometricManager.from(context)
                .canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
        ) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            else -> BiometricAvailability.TEMPORARILY_UNAVAILABLE
        }
    }

    override fun prepareBiometricEnrollmentCipher(): Cipher? {
        val sessionKey = sessionMasterKey ?: return null
        return if (sessionKey.isNotEmpty()) keystoreKeyManager.prepareBiometricEnrollmentCipher() else null
    }

    override suspend fun completeBiometricEnrollment(cipher: Cipher): AuthResult = mutex.withLock {
        val config =
            configStore.read() ?: return@withLock AuthResult.failure("Vault is not configured")
        val sessionKey = sessionMasterKey
            ?: return@withLock AuthResult.failure("Unlock the vault before enabling biometrics")
        val wrapped = keystoreKeyManager.completeBiometricWrap(cipher, sessionKey)
        val updated = config.copy(
            wrappedMasterKeyByBiometric = wrapped,
            settings = config.settings.copy(biometricEnabled = true),
        )
        configStore.write(updated)
        mutableSessionState.value = mutableSessionState.value.copy(biometricEnabled = true)
        AuthResult.success()
    }

    override fun prepareBiometricUnlockCipher(): Cipher? {
        val config = configStore.read() ?: return null
        val wrapped = config.wrappedMasterKeyByBiometric ?: return null
        return keystoreKeyManager.prepareBiometricUnlockCipher(wrapped)
    }

    override suspend fun unlockWithBiometric(cipher: Cipher): AuthResult = mutex.withLock {
        val config =
            configStore.read() ?: return@withLock AuthResult.failure("Vault is not configured")
        val wrapped = config.wrappedMasterKeyByBiometric
            ?: return@withLock AuthResult.failure("Biometric unlock is not enabled")
        val masterKey = runCatching {
            keystoreKeyManager.unwrapWithBiometricCipher(cipher, wrapped)
        }.getOrElse {
            return@withLock AuthResult.failure("Biometric unlock failed")
        }
        installSession(masterKey, config)
        resetFailures(config)
        AuthResult.success()
    }

    override fun biometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ObscuraCalc Vault")
            .setSubtitle("Authenticate to open your private space")
            .setNegativeButtonText("Cancel")
            .build()
    }

    override fun currentSessionKey(): ByteArray? = sessionMasterKey?.copyOf()

    private suspend fun unlockWithCredential(
        credential: CharArray,
        expectedType: CredentialType,
    ): AuthResult = mutex.withLock {
        val config =
            configStore.read() ?: return@withLock AuthResult.failure("Vault is not configured")
        if (config.credentialType != expectedType) {
            CredentialCrypto.clear(credential)
            return@withLock AuthResult.failure("Use the configured authentication method")
        }

        val actualHash =
            CredentialCrypto.deriveCredentialHash(credential, config.credentialHashSalt)
        if (!CredentialCrypto.constantTimeEquals(actualHash, config.credentialHash)) {
            actualHash.fill(0)
            CredentialCrypto.clear(credential)
            return@withLock handleFailedAttempt(config)
        }

        val wrappingKey = CredentialCrypto.deriveWrappingKey(credential, config.credentialWrapSalt)
        val masterKey = runCatching {
            AesGcm.unwrapWithRawKey(config.wrappedMasterKeyByCredential, wrappingKey)
        }.getOrElse {
            actualHash.fill(0)
            wrappingKey.fill(0)
            CredentialCrypto.clear(credential)
            return@withLock AuthResult.failure("Unable to unlock the vault")
        }
        actualHash.fill(0)

        runCatching {
            keystoreKeyManager.unwrapWithDeviceKey(config.wrappedMasterKeyByDevice)
        }.getOrElse {
            CredentialCrypto.clear(credential)
            wrappingKey.fill(0)
            Arrays.fill(masterKey, 0)
            return@withLock AuthResult.failure("This device cannot access the existing vault")
        }.also { deviceKey ->
            if (!MessageDigest.isEqual(deviceKey, masterKey)) {
                Arrays.fill(deviceKey, 0)
                wrappingKey.fill(0)
                Arrays.fill(masterKey, 0)
                CredentialCrypto.clear(credential)
                return@withLock AuthResult.failure("Credential and device key verification failed")
            }
            Arrays.fill(deviceKey, 0)
        }

        wrappingKey.fill(0)
        CredentialCrypto.clear(credential)
        installSession(masterKey, config)
        resetFailures(config)
        AuthResult.success()
    }

    private suspend fun handleFailedAttempt(config: StoredVaultConfig): AuthResult {
        val updatedAttempts = config.failedAttempts + 1
        val threshold = config.settings.wipeAfterFailures
        return if (threshold > 0 && updatedAttempts >= threshold) {
            clearVaultLocked()
            AuthResult.failure(
                message = "Too many failed attempts. Vault data was cleared on this device.",
                wipeTriggered = true,
            )
        } else {
            configStore.write(config.copy(failedAttempts = updatedAttempts))
            mutableSessionState.value = mutableSessionState.value.copy(isUnlocked = false)
            if (config.settings.decoyModeEnabled) {
                AuthResult.failure("Calculation complete")
            } else {
                AuthResult.failure("Authentication failed")
            }
        }
    }

    private fun installSession(masterKey: ByteArray, config: StoredVaultConfig) {
        clearSession()
        sessionMasterKey = masterKey
        mutableSessionState.value = VaultSessionState(
            isConfigured = true,
            isUnlocked = true,
            credentialType = config.credentialType,
            biometricEnabled = config.settings.biometricEnabled,
        )
    }

    private fun clearSession() {
        sessionMasterKey?.fill(0)
        sessionMasterKey = null
    }

    private suspend fun clearVaultLocked() {
        clearSession()
        configStore.clear()
        keystoreKeyManager.deleteAll()
        mutableSessionState.value = VaultSessionState()
        onWipeRequested()
    }

    private fun loadInitialState(): VaultSessionState {
        val config = configStore.read() ?: return VaultSessionState()
        return VaultSessionState(
            isConfigured = true,
            isUnlocked = false,
            credentialType = config.credentialType,
            biometricEnabled = config.settings.biometricEnabled,
        )
    }

    private fun normalizeHiddenTrigger(raw: CharArray): CharArray {
        return raw.concatToString().trim().replace(" ", "").lowercase().toCharArray()
    }

    private fun resetFailures(config: StoredVaultConfig) {
        if (config.failedAttempts != 0) {
            configStore.write(config.copy(failedAttempts = 0))
        }
    }
}
