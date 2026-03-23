package com.obscuracalc.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
                    Text("ObscuraCalc", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Offline calculator and conversion tools designed to stay quiet, local, and honest about their limits.",
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
                        Text("Private Space Session", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Vault protection is user-space only. It encrypts app-held data at rest, but it does not replace the operating system sandbox or hardware-backed secure folders.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedTextField(
                            value = securitySettings.lockTimeoutSeconds.toString(),
                            onValueChange = { it.toIntOrNull()?.let(onUpdateLockTimeoutSeconds) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Lock timeout (seconds)") },
                        )
                        OutlinedTextField(
                            value = securitySettings.wipeAfterFailures.toString(),
                            onValueChange = { it.toIntOrNull()?.let(onUpdateWipeAfterFailures) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Wipe after failed attempts (0 disables)") },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Decoy response")
                                Text(
                                    "When enabled, failed authentication returns quietly to calculator behavior.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Switch(
                                checked = securitySettings.decoyModeEnabled,
                                onCheckedChange = onUpdateDecoyMode,
                            )
                        }
                        AssistChip(onClick = onLockVault, label = { Text("Lock now") })
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
                    Text("Legal & Documentation", style = MaterialTheme.typography.titleLarge)
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
                    Text("About", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Version $appVersion",
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
                        label = { Text("Version details") },
                    )
                    Text(
                        "No analytics, no advertising, no cloud sync, and no mandatory Google Play Services.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
