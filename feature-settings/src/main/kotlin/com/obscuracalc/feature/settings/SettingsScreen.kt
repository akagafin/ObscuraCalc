package com.obscuracalc.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.obscuracalc.core.security.model.SecuritySettings

enum class LegalDocDestination(val title: String, val assetPath: String) {
    TERMS("Terms of Service", "legal/terms_of_service.md"),
    PRIVACY("Privacy Policy", "legal/privacy_policy.md"),
    THREAT_MODEL("Threat Model", "legal/threat_model.md"),
    LICENSE("Apache License 2.0", "legal/apache_license_2_0.txt"),
}

@Composable
fun SettingsScreen(
    securitySettings: SecuritySettings,
    vaultConfigured: Boolean,
    vaultUnlocked: Boolean,
    appVersion: String,
    modifier: Modifier = Modifier,
    onOpenLegalDoc: (LegalDocDestination) -> Unit,
    onVersionTapThresholdReached: () -> Unit,
    onUpdateLockTimeoutSeconds: (Int) -> Unit = {},
    onUpdateWipeAfterFailures: (Int) -> Unit = {},
    onUpdateDecoyMode: (Boolean) -> Unit = {},
    onLockVault: () -> Unit = {},
) {
    var versionTapCount by remember { mutableIntStateOf(0) }
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.settings_app_description),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (vaultUnlocked) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_private_space_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.settings_private_space_desc),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedTextField(
                            value = securitySettings.lockTimeoutSeconds.toString(),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    onUpdateLockTimeoutSeconds(0)
                                } else {
                                    newValue.toIntOrNull()?.let(onUpdateLockTimeoutSeconds)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.settings_lock_timeout)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        OutlinedTextField(
                            value = securitySettings.wipeAfterFailures.toString(),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    onUpdateWipeAfterFailures(0)
                                } else {
                                    newValue.toIntOrNull()?.let(onUpdateWipeAfterFailures)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.settings_wipe_failures)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.settings_decoy_response))
                                Text(
                                    text = stringResource(R.string.settings_decoy_response_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Switch(
                                checked = securitySettings.decoyModeEnabled,
                                onCheckedChange = onUpdateDecoyMode,
                            )
                        }
                        AssistChip(
                            onClick = onLockVault,
                            label = { Text(stringResource(R.string.settings_lock_now)) }
                        )
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_legal_docs),
                        style = MaterialTheme.typography.titleLarge
                    )
                    LegalDocDestination.entries.forEach { destination ->
                        AssistChip(
                            onClick = { onOpenLegalDoc(destination) },
                            label = { Text(destination.title) },
                        )
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_about),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_version, appVersion),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    AssistChip(
                        onClick = {
                            versionTapCount += 1
                            if (!vaultConfigured && versionTapCount >= 7) {
                                versionTapCount = 0
                                onVersionTapThresholdReached()
                            }
                        },
                        label = { Text(stringResource(R.string.settings_version_details)) },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.settings_source_code),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "https://github.com/akagafin/ObscuraCalc",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://github.com/akagafin/ObscuraCalc")
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.settings_privacy_footer),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
