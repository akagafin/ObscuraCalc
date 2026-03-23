package com.obscuracalc.core.security

import android.content.Context
import com.obscuracalc.core.security.model.AuthResult
import com.obscuracalc.core.security.model.BiometricAvailability
import com.obscuracalc.core.security.model.CredentialType
import com.obscuracalc.core.security.model.LockReason
import com.obscuracalc.core.security.model.SecuritySettings
import com.obscuracalc.core.security.model.VaultSessionState
import kotlinx.coroutines.flow.StateFlow
import javax.crypto.Cipher

interface SessionKeyProvider {
    fun currentSessionKey(): ByteArray?
}

interface VaultAuthManager : SessionKeyProvider {
    val sessionState: StateFlow<VaultSessionState>

    suspend fun isVaultConfigured(): Boolean
    suspend fun currentSettings(): SecuritySettings
    suspend fun setupVault(
        credential: CharArray,
        credentialType: CredentialType,
        hiddenTrigger: CharArray? = null,
        lockTimeoutSeconds: Int = 0,
        wipeAfterFailures: Int = 0,
        decoyModeEnabled: Boolean = false,
    ): AuthResult

    suspend fun unlockWithPin(pin: CharArray): AuthResult
    suspend fun unlockWithPassword(password: CharArray): AuthResult
    suspend fun lock(reason: LockReason = LockReason.EXPLICIT)
    suspend fun updateSettings(transform: (SecuritySettings) -> SecuritySettings)
    suspend fun matchesHiddenTrigger(candidate: CharArray): Boolean
    suspend fun clearVault()

    fun biometricAvailability(context: Context): BiometricAvailability
    fun prepareBiometricEnrollmentCipher(): Cipher?
    suspend fun completeBiometricEnrollment(cipher: Cipher): AuthResult
    fun prepareBiometricUnlockCipher(): Cipher?
    suspend fun unlockWithBiometric(cipher: Cipher): AuthResult
    fun biometricPromptInfo(): androidx.biometric.BiometricPrompt.PromptInfo
}
