package com.obscuracalc.feature.vault

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.obscuracalc.core.security.VaultAuthManager
import com.obscuracalc.core.security.model.BiometricAvailability
import com.obscuracalc.core.security.model.CredentialType
import kotlinx.coroutines.launch

@Composable
fun VaultSetupScreen(
    authManager: VaultAuthManager,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as FragmentActivity
    val scope = rememberCoroutineScope()
    val biometricAvailable = authManager.biometricAvailability(activity) == BiometricAvailability.AVAILABLE
    var credentialType by remember { mutableStateOf(CredentialType.PIN) }
    var credential by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var hiddenTrigger by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(false) }
    var decoyMode by remember { mutableStateOf(false) }
    var wipeAfterFailures by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Create Private Space", style = MaterialTheme.typography.headlineSmall)
            Text(
                "This vault encrypts app-held files at rest and locks on background or screen-off. It does not clone other Android apps or provide system-level isolation.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { credentialType = CredentialType.PIN }, label = { Text("PIN") })
                AssistChip(onClick = { credentialType = CredentialType.PASSWORD }, label = { Text("Password") })
            }
            OutlinedTextField(
                value = credential,
                onValueChange = { credential = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (credentialType == CredentialType.PIN) "Numeric PIN" else "Password") },
                visualTransformation = PasswordVisualTransformation(),
            )
            OutlinedTextField(
                value = confirmation,
                onValueChange = { confirmation = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm") },
                visualTransformation = PasswordVisualTransformation(),
            )
            OutlinedTextField(
                value = hiddenTrigger,
                onValueChange = { hiddenTrigger = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Optional calculator unlock sequence") },
            )
            OutlinedTextField(
                value = wipeAfterFailures.toString(),
                onValueChange = { wipeAfterFailures = it.toIntOrNull() ?: 0 },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Wipe after failed attempts (0 disables)") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Decoy response on failed unlock")
                Switch(checked = decoyMode, onCheckedChange = { decoyMode = it })
            }
            if (biometricAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Enable biometrics")
                    Switch(checked = enableBiometric, onCheckedChange = { enableBiometric = it })
                }
            }
            Button(
                onClick = {
                    if (credential.isBlank() || credential != confirmation) {
                        message = "Credentials must match."
                        return@Button
                    }
                    scope.launch {
                        val result = authManager.setupVault(
                            credential = credential.toCharArray(),
                            credentialType = credentialType,
                            hiddenTrigger = hiddenTrigger.takeIf { it.isNotBlank() }?.toCharArray(),
                            wipeAfterFailures = wipeAfterFailures,
                            decoyModeEnabled = decoyMode,
                        )
                        if (!result.success) {
                            message = result.errorMessage
                            return@launch
                        }
                        credential = ""
                        confirmation = ""
                        if (enableBiometric) {
                            val cipher = authManager.prepareBiometricEnrollmentCipher()
                            if (cipher != null) {
                                val prompt = BiometricPrompt(
                                    activity,
                                    ContextCompat.getMainExecutor(activity),
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            val successCipher = result.cryptoObject?.cipher ?: return
                                            scope.launch {
                                                val enrollmentResult = authManager.completeBiometricEnrollment(successCipher)
                                                if (enrollmentResult.success) {
                                                    hiddenTrigger = ""
                                                    onSetupComplete()
                                                } else {
                                                    message = enrollmentResult.errorMessage
                                                }
                                            }
                                        }

                                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                            message = errString.toString()
                                        }

                                        override fun onAuthenticationFailed() {
                                            message = "Biometric authentication failed"
                                        }
                                    },
                                )
                                prompt.authenticate(
                                    authManager.biometricPromptInfo(),
                                    BiometricPrompt.CryptoObject(cipher),
                                )
                            } else {
                                hiddenTrigger = ""
                                onSetupComplete()
                            }
                        } else {
                            hiddenTrigger = ""
                            onSetupComplete()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create vault")
            }
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
