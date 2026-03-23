package com.obscuracalc.core.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class KeystoreKeyManager {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun wrapWithDeviceKey(plain: ByteArray): ByteArray {
        val cipher = cipher().apply { init(Cipher.ENCRYPT_MODE, getOrCreateDeviceKey()) }
        val encrypted = cipher.doFinal(plain)
        return joinIvAndCiphertext(cipher.iv, encrypted)
    }

    fun unwrapWithDeviceKey(wrapped: ByteArray): ByteArray {
        val (iv, ciphertext) = splitIvAndCiphertext(wrapped)
        val cipher = cipher().apply {
            init(Cipher.DECRYPT_MODE, getOrCreateDeviceKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    fun prepareBiometricEnrollmentCipher(): Cipher {
        return cipher().apply { init(Cipher.ENCRYPT_MODE, getOrCreateBiometricKey()) }
    }

    fun prepareBiometricUnlockCipher(wrapped: ByteArray): Cipher {
        val (iv, _) = splitIvAndCiphertext(wrapped)
        return cipher().apply {
            init(Cipher.DECRYPT_MODE, getOrCreateBiometricKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
    }

    fun completeBiometricWrap(cipher: Cipher, plain: ByteArray): ByteArray {
        val encrypted = cipher.doFinal(plain)
        return joinIvAndCiphertext(cipher.iv, encrypted)
    }

    fun unwrapWithBiometricCipher(cipher: Cipher, wrapped: ByteArray): ByteArray {
        val (_, ciphertext) = splitIvAndCiphertext(wrapped)
        return cipher.doFinal(ciphertext)
    }

    fun deleteAll() {
        listOf(DEVICE_ALIAS, BIOMETRIC_ALIAS).forEach { alias ->
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        }
    }

    private fun getOrCreateDeviceKey(): SecretKey {
        return getExistingSecretKey(DEVICE_ALIAS) ?: generateKey(
            alias = DEVICE_ALIAS,
            userAuthenticationRequired = false,
        )
    }

    private fun getOrCreateBiometricKey(): SecretKey {
        return getExistingSecretKey(BIOMETRIC_ALIAS) ?: generateKey(
            alias = BIOMETRIC_ALIAS,
            userAuthenticationRequired = true,
        )
    }

    private fun getExistingSecretKey(alias: String): SecretKey? {
        return (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
    }

    private fun generateKey(
        alias: String,
        userAuthenticationRequired: Boolean,
    ): SecretKey {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        if (userAuthenticationRequired) {
            builder.setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG,
                )
            }
            builder.setInvalidatedByBiometricEnrollment(true)
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    private fun cipher(): Cipher = Cipher.getInstance("AES/GCM/NoPadding")

    private fun joinIvAndCiphertext(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        return ByteBuffer.allocate(4 + iv.size + ciphertext.size)
            .putInt(iv.size)
            .put(iv)
            .put(ciphertext)
            .array()
    }

    private fun splitIvAndCiphertext(payload: ByteArray): Pair<ByteArray, ByteArray> {
        val buffer = ByteBuffer.wrap(payload)
        val ivSize = buffer.int
        val iv = ByteArray(ivSize)
        buffer.get(iv)
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)
        return iv to ciphertext
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val DEVICE_ALIAS = "obscuracalc.vault.device"
        const val BIOMETRIC_ALIAS = "obscuracalc.vault.biometric"
        const val GCM_TAG_BITS = 128
    }
}
