package com.obscuracalc.core.security

import android.content.Context
import android.util.Base64
import com.obscuracalc.core.security.model.CredentialType
import com.obscuracalc.core.security.model.SecuritySettings
import com.obscuracalc.core.security.model.StoredVaultConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class VaultConfigStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): StoredVaultConfig? {
        val raw = preferences.getString(KEY_CONFIG, null) ?: return null
        val root = Json.parseToJsonElement(raw).jsonObject
        val settings = root["settings"]?.jsonObject ?: error("Missing settings object")
        return StoredVaultConfig(
            credentialType = CredentialType.valueOf(root.getString("credentialType")),
            credentialHash = root.getBytes("credentialHash"),
            credentialHashSalt = root.getBytes("credentialHashSalt"),
            credentialWrapSalt = root.getBytes("credentialWrapSalt"),
            wrappedMasterKeyByDevice = root.getBytes("wrappedMasterKeyByDevice"),
            wrappedMasterKeyByCredential = root.getBytes("wrappedMasterKeyByCredential"),
            wrappedMasterKeyByBiometric = root.getBytesOrNull("wrappedMasterKeyByBiometric"),
            hiddenTriggerHash = root.getBytesOrNull("hiddenTriggerHash"),
            hiddenTriggerSalt = root.getBytesOrNull("hiddenTriggerSalt"),
            settings = SecuritySettings(
                hiddenTriggerEnabled = settings.getBoolean("hiddenTriggerEnabled"),
                lockTimeoutSeconds = settings.getInt("lockTimeoutSeconds"),
                wipeAfterFailures = settings.getInt("wipeAfterFailures"),
                decoyModeEnabled = settings.getBoolean("decoyModeEnabled"),
                biometricEnabled = settings.getBoolean("biometricEnabled"),
            ),
            failedAttempts = root.getInt("failedAttempts"),
        )
    }

    fun write(config: StoredVaultConfig) {
        val payload = buildJsonObject {
            put("credentialType", JsonPrimitive(config.credentialType.name))
            put("credentialHash", JsonPrimitive(config.credentialHash.encode()))
            put("credentialHashSalt", JsonPrimitive(config.credentialHashSalt.encode()))
            put("credentialWrapSalt", JsonPrimitive(config.credentialWrapSalt.encode()))
            put("wrappedMasterKeyByDevice", JsonPrimitive(config.wrappedMasterKeyByDevice.encode()))
            put(
                "wrappedMasterKeyByCredential",
                JsonPrimitive(config.wrappedMasterKeyByCredential.encode())
            )
            put(
                "wrappedMasterKeyByBiometric",
                config.wrappedMasterKeyByBiometric?.let { JsonPrimitive(it.encode()) } ?: JsonNull,
            )
            put(
                "hiddenTriggerHash",
                config.hiddenTriggerHash?.let { JsonPrimitive(it.encode()) } ?: JsonNull,
            )
            put(
                "hiddenTriggerSalt",
                config.hiddenTriggerSalt?.let { JsonPrimitive(it.encode()) } ?: JsonNull,
            )
            put("failedAttempts", JsonPrimitive(config.failedAttempts))
            put(
                "settings",
                buildJsonObject {
                    put("hiddenTriggerEnabled", JsonPrimitive(config.settings.hiddenTriggerEnabled))
                    put("lockTimeoutSeconds", JsonPrimitive(config.settings.lockTimeoutSeconds))
                    put("wipeAfterFailures", JsonPrimitive(config.settings.wipeAfterFailures))
                    put("decoyModeEnabled", JsonPrimitive(config.settings.decoyModeEnabled))
                    put("biometricEnabled", JsonPrimitive(config.settings.biometricEnabled))
                },
            )
        }
        val committed = preferences.edit()
            .putString(KEY_CONFIG, payload.toString())
            .commit()
        check(committed) { "Failed to persist vault configuration" }
    }

    fun clear() {
        val committed = preferences.edit()
            .remove(KEY_CONFIG)
            .commit()
        check(committed) { "Failed to clear vault configuration" }
    }

    private fun JsonObject.getString(key: String): String =
        this[key]?.jsonPrimitive?.content ?: error("Missing $key")

    private fun JsonObject.getInt(key: String): Int =
        this[key]?.jsonPrimitive?.int ?: error("Missing $key")

    private fun JsonObject.getBoolean(key: String): Boolean =
        this[key]?.jsonPrimitive?.boolean ?: error("Missing $key")

    private fun JsonObject.getBytes(key: String): ByteArray =
        Base64.decode(getString(key), Base64.DEFAULT)

    private fun JsonObject.getBytesOrNull(key: String): ByteArray? =
        this[key]?.jsonPrimitive?.content?.let { Base64.decode(it, Base64.DEFAULT) }

    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private companion object {
        const val PREFS_NAME = "obscuracalc_vault_config"
        const val KEY_CONFIG = "vault_config_json"
    }
}
