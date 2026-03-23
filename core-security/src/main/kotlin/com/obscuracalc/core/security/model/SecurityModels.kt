package com.obscuracalc.core.security.model

enum class CredentialType {
    PIN,
    PASSWORD,
}

enum class LockReason {
    APP_BACKGROUND,
    SCREEN_OFF,
    EXPLICIT,
    PROCESS_END,
    AUTH_FAILURE,
}

enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    NONE_ENROLLED,
    TEMPORARILY_UNAVAILABLE,
}

data class SecuritySettings(
    val hiddenTriggerEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 0,
    val wipeAfterFailures: Int = 0,
    val decoyModeEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
)

data class VaultSessionState(
    val isConfigured: Boolean = false,
    val isUnlocked: Boolean = false,
    val credentialType: CredentialType? = null,
    val biometricEnabled: Boolean = false,
)

data class AuthResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val wipeTriggered: Boolean = false,
) {
    companion object {
        fun success() = AuthResult(success = true)
        fun failure(message: String, wipeTriggered: Boolean = false) = AuthResult(
            success = false,
            errorMessage = message,
            wipeTriggered = wipeTriggered,
        )
    }
}

internal data class StoredVaultConfig(
    val credentialType: CredentialType,
    val credentialHash: ByteArray,
    val credentialHashSalt: ByteArray,
    val credentialWrapSalt: ByteArray,
    val wrappedMasterKeyByDevice: ByteArray,
    val wrappedMasterKeyByCredential: ByteArray,
    val wrappedMasterKeyByBiometric: ByteArray?,
    val hiddenTriggerHash: ByteArray?,
    val hiddenTriggerSalt: ByteArray?,
    val settings: SecuritySettings,
    val failedAttempts: Int,
)
