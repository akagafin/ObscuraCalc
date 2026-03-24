package com.obscuracalc.feature.vault

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.obscuracalc.core.security.VaultAuthManager
import com.obscuracalc.core.security.model.CredentialType
import kotlinx.coroutines.launch

@Composable
fun VaultAuthScreen(
    authManager: VaultAuthManager,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current as FragmentActivity
    val scope = rememberCoroutineScope()
    val sessionState by authManager.sessionState.collectAsState()
    var credential by remember { mutableStateOf("") }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Unlock Private Space", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Local vault access uses the credential you configured on this device. No network connection is required.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = credential,
                onValueChange = { credential = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (sessionState.credentialType == CredentialType.PIN) "PIN" else "Password",
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
            )
            Button(
                onClick = {
                    scope.launch {
                        val result = if (sessionState.credentialType == CredentialType.PIN) {
                            authManager.unlockWithPin(credential.toCharArray())
                        } else {
                            authManager.unlockWithPassword(credential.toCharArray())
                        }
                        credential = ""
                        if (result.success) {
                            onAuthenticated()
                        } else {
                            message = result.errorMessage
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Unlock")
            }
            if (sessionState.biometricEnabled) {
                AssistChip(
                    onClick = {
                        val cipher = authManager.prepareBiometricUnlockCipher() ?: return@AssistChip
                        val prompt = BiometricPrompt(
                            activity,
                            ContextCompat.getMainExecutor(activity),
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    val cryptoCipher = result.cryptoObject?.cipher ?: return
                                    scope.launch {
                                        val authResult =
                                            authManager.unlockWithBiometric(cryptoCipher)
                                        if (authResult.success) {
                                            onAuthenticated()
                                        } else {
                                            message = authResult.errorMessage
                                        }
                                    }
                                }

                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    message = errString.toString()
                                }

                                override fun onAuthenticationFailed() {
                                    message = "Biometric authentication failed"
                                }
                            },
                        )
                        prompt.authenticate(
                            authManager.biometricPromptInfo(),
                            BiometricPrompt.CryptoObject(cipher)
                        )
                    },
                    label = { Text("Use biometrics") },
                )
            }
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
