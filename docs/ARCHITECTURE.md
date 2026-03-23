# ObscuraCalc Architecture

## Kotlin Project Structure Overview

ObscuraCalc is organized as a multi-module Android project so the calculator, converters, security primitives, vault storage layer, and Compose UI surfaces remain independently testable.

### Modules

- `app`
  - Application entry point
  - Navigation graph
  - Dynamic theme
  - Lifecycle-driven auto-lock coordination
  - Service container wiring
- `core-calculator`
  - Pure Kotlin scientific expression engine
  - Precision-aware formatting
  - Memory register helpers
- `core-converter`
  - Pure Kotlin unit conversion engines
  - Currency JSON/CSV import logic
- `core-security`
  - Credential derivation with PBKDF2-HMAC-SHA256
  - Android Keystore wrapping keys
  - Vault session state
  - Biometric enrollment and unlock helpers
- `core-vault`
  - Encrypted file blob storage
  - Encrypted metadata index
  - Room database for opaque entry records
  - Backup and restore service
- `feature-calculator`
  - Calculator controller and Compose keypad
- `feature-converter`
  - Converter screen and local currency rate storage
- `feature-vault`
  - Vault setup, authentication, file explorer, preview, and backup UI
- `feature-settings`
  - Settings screen, legal entry points, hidden onboarding gesture
- `feature-legal`
  - Asset-backed legal document renderer

## Module Separation Rationale

The separation is intentionally conservative:

- Logic-heavy modules are pure Kotlin where possible to support fast unit testing.
- Android-specific security and storage concerns live behind interfaces so the UI layer does not manipulate keys or ciphertext directly.
- Feature UI modules remain mostly declarative and delegate sensitive work to `core-security` and `core-vault`.

This keeps security-critical code paths smaller and easier to audit.

## Security Architecture Rationale

### Data at Rest

Vault files are stored in app-private internal storage. Each imported file receives its own random data-encryption key (DEK). The file contents are encrypted with AES-256-GCM before being written to disk.

The DEK is then wrapped by the in-memory vault master key (VMK), and the resulting wrapped DEK is stored only inside encrypted metadata.

### Vault Master Key

The vault master key is randomly generated on setup and persisted only in wrapped form:

- one copy wrapped by an Android Keystore AES key
- one copy wrapped by a credential-derived key
- optionally one copy wrapped by a biometric-gated key

This allows the app to verify both device binding and user authentication without hardcoding secrets.

### Session Handling

The VMK exists in memory only while the vault is unlocked. The app attempts best-effort cleanup by clearing byte arrays on explicit lock, background transitions, and wipe flows. Process death and device reboot also clear session access because the live VMK is never written in plaintext.

### Threat Model Boundaries

The design explicitly does not claim:

- kernel-level isolation
- secure hardware enclave ownership
- arbitrary Android app cloning
- protection from rooted or compromised systems

Those limits are enforced in product copy, threat model documentation, and UI wording.

## UI Hierarchy Description

### Locked State

- Calculator is the default route.
- Converter remains visible in standard navigation.
- Settings contains general app and legal information.
- Vault access is absent from normal navigation.

### Hidden Entry Path

- Before a vault exists, setup can be reached through a non-prominent onboarding gesture from settings.
- After setup, users may define a hidden calculator input sequence.
- Matching that sequence routes to the authentication screen without exposing a permanent vault button.

### Unlocked State

- A temporary vault destination appears in navigation for the current session.
- Vault UI allows import, preview, export, delete, backup, restore, and explicit lock.
- Settings exposes advanced security options only while the vault is already unlocked.

## Code Examples

### Calculator Engine

```kotlin
class ExpressionEngine(
    private val mathContext: MathContext = MathContext(16, RoundingMode.HALF_EVEN),
) {
    fun evaluate(expression: String, angleMode: AngleMode): CalcResult {
        return runCatching {
            val value = Parser(expression, angleMode, mathContext).parse()
            CalcResult(expression, value, format(value))
        }.getOrElse {
            CalcResult(expression, null, "Error", it.message ?: "Invalid expression")
        }
    }
}
```

### Authentication Flow

```kotlin
override suspend fun unlockWithPassword(password: CharArray): AuthResult {
    val config = configStore.read() ?: return AuthResult.failure("Vault is not configured")
    val actualHash = CredentialCrypto.deriveCredentialHash(password, config.credentialHashSalt)
    if (!CredentialCrypto.constantTimeEquals(actualHash, config.credentialHash)) {
        return handleFailedAttempt(config)
    }

    val wrappingKey = CredentialCrypto.deriveWrappingKey(password, config.credentialWrapSalt)
    val masterKey = AesGcm.unwrapWithRawKey(config.wrappedMasterKeyByCredential, wrappingKey)
    installSession(masterKey, config)
    return AuthResult.success()
}
```

### Encrypted File Storage

```kotlin
val fileKey = VaultCrypto.randomBytes(32)
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
```

## Notes for Maintainers

- Keep the no-network posture intact unless the project goals change publicly and explicitly.
- Treat product copy as part of the security boundary. Misleading wording is a defect.
- Prefer auditability over novelty in future changes.
