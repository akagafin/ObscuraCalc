package com.obscuracalc.core.vault.model

enum class MediaKind {
    DOCUMENT,
    IMAGE,
    VIDEO,
    AUDIO,
}

data class VaultEntrySummary(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val importedAtEpochMillis: Long,
    val mediaKind: MediaKind,
)

data class VaultOpenedEntry(
    val summary: VaultEntrySummary,
    val bytes: ByteArray,
)
