package com.obscuracalc.feature.vault

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.obscuracalc.core.vault.BackupService
import com.obscuracalc.core.vault.VaultRepository
import com.obscuracalc.core.vault.model.MediaKind
import com.obscuracalc.core.vault.model.VaultEntrySummary
import kotlinx.coroutines.launch

@Composable
fun rememberVaultHomeController(
    repository: VaultRepository,
    backupService: BackupService,
): VaultHomeController =
    remember(repository, backupService) { VaultHomeController(repository, backupService) }

@Composable
fun VaultHomeScreen(
    controller: VaultHomeController,
    modifier: Modifier = Modifier,
    onLockVault: () -> Unit,
) {
    val state = controller.state
    val scope = rememberCoroutineScope()
    var pendingExport by remember { mutableStateOf<VaultEntrySummary?>(null) }
    var backupDialogMode by remember { mutableStateOf<BackupDialogMode?>(null) }
    var pendingPassphrase by remember { mutableStateOf("") }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { scope.launch { controller.importFile(it) } }
        }
    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null && backupDialogMode == BackupDialogMode.RESTORE) {
                scope.launch {
                    controller.restoreBackup(uri, pendingPassphrase.toCharArray())
                    pendingPassphrase = ""
                    backupDialogMode = null
                }
            }
        }
    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
            if (uri != null && backupDialogMode == BackupDialogMode.CREATE) {
                scope.launch {
                    controller.createBackup(uri, pendingPassphrase.toCharArray())
                    pendingPassphrase = ""
                    backupDialogMode = null
                }
            }
        }
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
            val exportEntry = pendingExport ?: return@rememberLauncherForActivityResult
            if (uri != null) {
                scope.launch { controller.exportFile(exportEntry.id, uri) }
            }
            pendingExport = null
        }

    LaunchedEffect(Unit) {
        controller.refresh()
    }

    if (backupDialogMode != null) {
        BackupPassphraseDialog(
            title = if (backupDialogMode == BackupDialogMode.CREATE) "Create encrypted backup" else "Restore encrypted backup",
            passphrase = pendingPassphrase,
            onPassphraseChange = { pendingPassphrase = it },
            onDismiss = {
                pendingPassphrase = ""
                backupDialogMode = null
            },
            onConfirm = {
                if (backupDialogMode == BackupDialogMode.CREATE) {
                    backupLauncher.launch("obscuracalc-backup.ocb")
                } else {
                    restoreLauncher.launch(arrayOf("*/*"))
                }
            },
        )
    }

    state.previewEntry?.let { preview ->
        AlertDialog(
            onDismissRequest = controller::dismissPreview,
            confirmButton = {
                TextButton(onClick = controller::dismissPreview) {
                    Text("Close")
                }
            },
            title = { Text(preview.summary.displayName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (preview.summary.mediaKind) {
                        MediaKind.IMAGE -> {
                            val bitmap =
                                BitmapFactory.decodeByteArray(preview.bytes, 0, preview.bytes.size)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = preview.summary.displayName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp),
                                )
                            } else {
                                Text("Unable to decode image preview.")
                            }
                        }

                        MediaKind.VIDEO -> Text("Video preview is limited in this build. Export to another app if you need playback.")
                        MediaKind.AUDIO -> Text("Audio preview is limited in this build. Export to another app if you need playback.")
                        MediaKind.DOCUMENT -> Text("Document preview is not rendered inline. Export the file to read it elsewhere.")
                    }
                    Text("Type: ${preview.summary.mimeType}")
                    Text("Size: ${preview.summary.sizeBytes} bytes")
                }
            },
        )
    }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Private Space", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Stored files are encrypted locally inside app-private storage. ObscuraCalc does not clone arbitrary third-party apps or bypass Android sandbox boundaries.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                            Text("Import file")
                        }
                        OutlinedButton(onClick = { backupDialogMode = BackupDialogMode.CREATE }) {
                            Text("Create backup")
                        }
                        OutlinedButton(onClick = { backupDialogMode = BackupDialogMode.RESTORE }) {
                            Text("Restore backup")
                        }
                    }
                    AssistChip(onClick = onLockVault, label = { Text("Lock vault") })
                    state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        items(state.entries) { entry ->
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(entry.displayName, style = MaterialTheme.typography.titleMedium)
                    Text("${entry.mimeType} | ${entry.sizeBytes} bytes")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { scope.launch { controller.loadPreview(entry.id) } },
                            label = { Text("Preview") },
                        )
                        AssistChip(
                            onClick = {
                                pendingExport = entry
                                exportLauncher.launch(entry.displayName)
                            },
                            label = { Text("Export") },
                        )
                        AssistChip(
                            onClick = { scope.launch { controller.deleteFile(entry.id) } },
                            label = { Text("Delete") },
                        )
                    }
                }
            }
        }
    }
}

private enum class BackupDialogMode {
    CREATE,
    RESTORE,
}

@Composable
private fun BackupPassphraseDialog(
    title: String,
    passphrase: String,
    onPassphraseChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = passphrase,
                onValueChange = onPassphraseChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Passphrase") },
                visualTransformation = PasswordVisualTransformation(),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = passphrase.isNotBlank()) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
