package com.obscuracalc.core.vault

import android.content.Context
import android.net.Uri
import com.obscuracalc.core.vault.model.VaultEntrySummary
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class EncryptedBackupService(
    private val context: Context,
    private val repository: RoomVaultRepository,
) : BackupService {
    override suspend fun createBackup(destinationUri: Uri, passphrase: CharArray) {
        val destination = context.contentResolver.openOutputStream(destinationUri)
            ?: throw FileNotFoundException("Unable to open backup destination")
        val entries = repository.listEntries()
        destination.use { output ->
            val salt = VaultCrypto.randomBytes(16)
            val backupKey = VaultCrypto.deriveBackupKey(passphrase, salt)
            try {
                output.write(MAGIC)
                output.write(salt)
                VaultCrypto.encryptStream(output, backupKey).use { cipherOutput ->
                    ZipOutputStream(cipherOutput).use { zip ->
                        zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
                        zip.write(buildManifest(entries).encodeToByteArray())
                        zip.closeEntry()
                        entries.forEach { entry ->
                            zip.putNextEntry(ZipEntry(fileEntryName(entry.id)))
                            repository.copyDecryptedEntryToStream(entry.id, zip)
                            zip.closeEntry()
                        }
                    }
                }
            } finally {
                passphrase.fill('\u0000')
                backupKey.fill(0)
            }
        }
    }

    override suspend fun restoreBackup(sourceUri: Uri, passphrase: CharArray) {
        val source = context.contentResolver.openInputStream(sourceUri)
            ?: throw FileNotFoundException("Unable to open backup source")
        source.use { input ->
            val header = ByteArray(MAGIC.size)
            DataInputStream(input).readFully(header)
            require(header.contentEquals(MAGIC)) { "Unsupported backup format" }
            val salt = ByteArray(16)
            DataInputStream(input).readFully(salt)
            val backupKey = VaultCrypto.deriveBackupKey(passphrase, salt)
            try {
                val fileEntries = mutableMapOf<String, BackupManifestEntry>()
                VaultCrypto.decryptStream(input, backupKey).use { cipherInput ->
                    ZipInputStream(cipherInput).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            when {
                                entry.name == MANIFEST_ENTRY -> {
                                    parseManifest(zip).forEach { manifestEntry ->
                                        fileEntries[manifestEntry.id] = manifestEntry
                                    }
                                }

                                entry.name.startsWith("files/") -> {
                                    val id = entry.name.removePrefix("files/").removeSuffix(".bin")
                                    val manifest = fileEntries[id]
                                        ?: throw IllegalStateException("Missing manifest for $id")
                                    repository.importFromStream(
                                        displayName = manifest.displayName,
                                        mimeType = manifest.mimeType,
                                        sizeBytes = manifest.sizeBytes,
                                        importedAt = manifest.importedAtEpochMillis,
                                        source = zip,
                                    )
                                }
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                }
            } finally {
                passphrase.fill('\u0000')
                backupKey.fill(0)
            }
        }
    }

    private fun buildManifest(entries: List<VaultEntrySummary>): String {
        val json = buildJsonObject {
            put(
                "entries",
                buildJsonArray {
                    entries.forEach { entry ->
                        add(
                            buildJsonObject {
                                put("id", JsonPrimitive(entry.id))
                                put("displayName", JsonPrimitive(entry.displayName))
                                put("mimeType", JsonPrimitive(entry.mimeType))
                                put("sizeBytes", JsonPrimitive(entry.sizeBytes))
                                put(
                                    "importedAtEpochMillis",
                                    JsonPrimitive(entry.importedAtEpochMillis)
                                )
                                put("mediaKind", JsonPrimitive(entry.mediaKind.name))
                            },
                        )
                    }
                },
            )
        }
        return json.toString()
    }

    private fun parseManifest(inputStream: InputStream): List<BackupManifestEntry> {
        val root =
            Json.parseToJsonElement(inputStream.bufferedReader().use { it.readText() }).jsonObject
        return root["entries"]?.jsonArray?.map { element ->
            val obj = element.jsonObject
            BackupManifestEntry(
                id = obj["id"]!!.jsonPrimitive.content,
                displayName = obj["displayName"]!!.jsonPrimitive.content,
                mimeType = obj["mimeType"]!!.jsonPrimitive.content,
                sizeBytes = obj["sizeBytes"]!!.jsonPrimitive.long,
                importedAtEpochMillis = obj["importedAtEpochMillis"]!!.jsonPrimitive.long,
            )
        }.orEmpty()
    }

    private fun fileEntryName(id: String): String = "files/$id.bin"

    private companion object {
        val MAGIC = byteArrayOf(0x4F, 0x43, 0x42, 0x4B, 0x31)
        const val MANIFEST_ENTRY = "manifest.json"
    }
}

private data class BackupManifestEntry(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val importedAtEpochMillis: Long,
)
