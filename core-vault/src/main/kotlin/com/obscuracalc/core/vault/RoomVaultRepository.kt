package com.obscuracalc.core.vault

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.obscuracalc.core.security.SessionKeyProvider
import com.obscuracalc.core.vault.db.VaultDatabase
import com.obscuracalc.core.vault.db.VaultEntryEntity
import com.obscuracalc.core.vault.model.MediaKind
import com.obscuracalc.core.vault.model.VaultEntrySummary
import com.obscuracalc.core.vault.model.VaultOpenedEntry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.long
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.OutputStream
import java.util.UUID

class RoomVaultRepository(
    private val context: Context,
    private val database: VaultDatabase,
    private val sessionKeyProvider: SessionKeyProvider,
) : VaultRepository {
    private val contentResolver: ContentResolver = context.contentResolver
    private val blobDirectory: File = File(context.filesDir, "vault/blobs").apply { mkdirs() }

    override suspend fun importFromUri(uri: Uri): VaultEntrySummary {
        val document = DocumentFile.fromSingleUri(context, uri)
            ?: throw IllegalArgumentException("Unable to open import source")
        val displayName = document.name ?: "Imported file"
        val mimeType = document.type ?: "application/octet-stream"
        val input = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to read import source")
        return input.use {
            importFromStream(
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = document.length(),
                importedAt = System.currentTimeMillis(),
                source = it,
            )
        }
    }

    override suspend fun exportToUri(id: String, uri: Uri) {
        val output = contentResolver.openOutputStream(uri)
            ?: throw IllegalArgumentException("Unable to open export destination")
        output.use { out ->
            copyDecryptedEntryToStream(id, out)
        }
    }

    override suspend fun listEntries(): List<VaultEntrySummary> {
        return database.vaultEntryDao().listAll()
            .map { decryptMetadata(it).toSummary(it.id) }
            .sortedByDescending { it.importedAtEpochMillis }
    }

    override suspend fun openEntry(id: String): VaultOpenedEntry {
        val entity = database.vaultEntryDao().findById(id) ?: throw IllegalArgumentException("Entry not found")
        val metadata = decryptMetadata(entity)
        val bytes = decryptBlob(entity.blobFileName, metadata.fileKey).use { it.readBytes() }
        return VaultOpenedEntry(
            summary = metadata.toSummary(entity.id),
            bytes = bytes,
        )
    }

    override suspend fun deleteEntry(id: String) {
        val entity = database.vaultEntryDao().findById(id) ?: return
        File(blobDirectory, entity.blobFileName).delete()
        database.vaultEntryDao().deleteById(id)
    }

    override suspend fun wipeAll() {
        blobDirectory.listFiles()?.forEach(File::delete)
        database.vaultEntryDao().clear()
    }

    internal suspend fun importFromStream(
        displayName: String,
        mimeType: String,
        sizeBytes: Long,
        importedAt: Long,
        source: java.io.InputStream,
    ): VaultEntrySummary {
        val sessionKey = requireSessionKey()
        val entryId = UUID.randomUUID().toString()
        val blobFileName = "${UUID.randomUUID()}.blob"
        val fileKey = VaultCrypto.randomBytes(32)
        val blobFile = File(blobDirectory, blobFileName)
        try {
            blobFile.outputStream().use { output ->
                VaultCrypto.encryptStream(output, fileKey).use { encryptedOutput ->
                    source.copyTo(encryptedOutput)
                }
            }
            val metadata = PlainVaultMetadata(
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                importedAtEpochMillis = importedAt,
                mediaKind = detectMediaKind(mimeType),
                fileKey = VaultCrypto.wrap(fileKey, sessionKey),
            )
            val encryptedMetadata = VaultCrypto.wrap(metadata.toJson().encodeToByteArray(), sessionKey)
            database.vaultEntryDao().upsert(
                VaultEntryEntity(
                    id = entryId,
                    encryptedMetadata = encryptedMetadata,
                    blobFileName = blobFileName,
                ),
            )
            return metadata.toSummary(entryId)
        } finally {
            fileKey.fill(0)
            sessionKey.fill(0)
        }
    }

    internal suspend fun copyDecryptedEntryToStream(id: String, outputStream: OutputStream): VaultEntrySummary {
        val entity = database.vaultEntryDao().findById(id) ?: throw IllegalArgumentException("Entry not found")
        val metadata = decryptMetadata(entity)
        decryptBlob(entity.blobFileName, metadata.fileKey).use { input ->
            input.copyTo(outputStream)
        }
        return metadata.toSummary(entity.id)
    }

    private fun decryptMetadata(entity: VaultEntryEntity): PlainVaultMetadata {
        val sessionKey = requireSessionKey()
        return try {
            val payload = VaultCrypto.unwrap(entity.encryptedMetadata, sessionKey)
            PlainVaultMetadata.fromJson(payload.decodeToString()).also {
                payload.fill(0)
            }
        } finally {
            sessionKey.fill(0)
        }
    }

    private fun decryptBlob(blobFileName: String, wrappedFileKey: ByteArray): java.io.InputStream {
        val sessionKey = requireSessionKey()
        val fileKey = VaultCrypto.unwrap(wrappedFileKey, sessionKey)
        val input = File(blobDirectory, blobFileName).inputStream()
        return try {
            VaultCrypto.decryptStream(input, fileKey)
        } finally {
            sessionKey.fill(0)
            fileKey.fill(0)
        }
    }

    private fun detectMediaKind(mimeType: String): MediaKind {
        return when {
            mimeType.startsWith("image/") -> MediaKind.IMAGE
            mimeType.startsWith("video/") -> MediaKind.VIDEO
            mimeType.startsWith("audio/") -> MediaKind.AUDIO
            else -> MediaKind.DOCUMENT
        }
    }

    private fun requireSessionKey(): ByteArray {
        return sessionKeyProvider.currentSessionKey()
            ?: throw IllegalStateException("Vault is locked")
    }
}

internal data class PlainVaultMetadata(
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val importedAtEpochMillis: Long,
    val mediaKind: MediaKind,
    val fileKey: ByteArray,
) {
    fun toSummary(id: String): VaultEntrySummary {
        return VaultEntrySummary(
            id = id,
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            importedAtEpochMillis = importedAtEpochMillis,
            mediaKind = mediaKind,
        )
    }

    fun toJson(): String {
        return buildJsonObject {
            put("displayName", JsonPrimitive(displayName))
            put("mimeType", JsonPrimitive(mimeType))
            put("sizeBytes", JsonPrimitive(sizeBytes))
            put("importedAtEpochMillis", JsonPrimitive(importedAtEpochMillis))
            put("mediaKind", JsonPrimitive(mediaKind.name))
            put("fileKey", JsonPrimitive(android.util.Base64.encodeToString(fileKey, android.util.Base64.NO_WRAP)))
        }.toString()
    }

    companion object {
        fun fromJson(raw: String): PlainVaultMetadata {
            val root = Json.parseToJsonElement(raw).jsonObject
            return PlainVaultMetadata(
                displayName = root.getValue("displayName").jsonPrimitive.content,
                mimeType = root.getValue("mimeType").jsonPrimitive.content,
                sizeBytes = root.getValue("sizeBytes").jsonPrimitive.long,
                importedAtEpochMillis = root.getValue("importedAtEpochMillis").jsonPrimitive.long,
                mediaKind = MediaKind.valueOf(root.getValue("mediaKind").jsonPrimitive.content),
                fileKey = android.util.Base64.decode(
                    root.getValue("fileKey").jsonPrimitive.content,
                    android.util.Base64.DEFAULT,
                ),
            )
        }
    }
}
