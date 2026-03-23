package com.obscuracalc.core.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object AesGcm {
    private val secureRandom = SecureRandom()

    fun wrapWithRawKey(plain: ByteArray, rawKey: ByteArray): ByteArray {
        val iv = ByteArray(12).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(rawKey, "AES"), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(4 + iv.size + encrypted.size)
            .putInt(iv.size)
            .put(iv)
            .put(encrypted)
            .array()
    }

    fun unwrapWithRawKey(wrapped: ByteArray, rawKey: ByteArray): ByteArray {
        val buffer = ByteBuffer.wrap(wrapped)
        val ivSize = buffer.int
        val iv = ByteArray(ivSize)
        buffer.get(iv)
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(rawKey, "AES"), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }
}
