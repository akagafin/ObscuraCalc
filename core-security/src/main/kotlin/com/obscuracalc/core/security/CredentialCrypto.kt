package com.obscuracalc.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object CredentialCrypto {
    private const val HASH_ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private val random = SecureRandom()

    fun randomBytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)

    fun deriveCredentialHash(credential: CharArray, salt: ByteArray): ByteArray {
        return deriveKey(credential, salt)
    }

    fun deriveWrappingKey(credential: CharArray, salt: ByteArray): ByteArray {
        return deriveKey(credential, salt)
    }

    fun constantTimeEquals(left: ByteArray, right: ByteArray): Boolean {
        return MessageDigest.isEqual(left, right)
    }

    fun clear(chars: CharArray) {
        chars.fill('\u0000')
    }

    private fun deriveKey(credential: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(credential, salt, HASH_ITERATIONS, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
