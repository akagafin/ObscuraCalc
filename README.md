# ObscuraCalc

> A privacy-focused, offline-first Android app that combines a scientific calculator, unit converter, and AES-256-GCM encrypted private vault — all hidden behind a fully functional calculator interface.

![Status](https://img.shields.io/badge/status-pre--alpha-orange)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-green)
![No Internet](https://img.shields.io/badge/network-no%20INTERNET%20permission-brightgreen)

> **⚠️ Status: Pre-Alpha — Under Active Development**
> Core features are still being implemented. Not yet suitable for production use or storing sensitive data.

---

## What Is ObscuraCalc?

ObscuraCalc is built around one philosophy: **your data, your device, your rules.**

Most "private vault" apps phone home, abuse permissions, or rely on a fake calculator that doesn't actually work. ObscuraCalc is engineered differently — no cloud sync, no analytics, no unnecessary permissions. The vault is concealed inside a fully operational scientific calculator, not just a prop.

Designed for distribution on **F-Droid** and similar privacy-respecting channels.

---

## Features

### Scientific Calculator
- Full expression parser with support for nested arithmetic and operator precedence
- Scientific functions: `sin`, `cos`, `tan`, `log`, `ln`, roots, powers, factorials
- Persistent calculation history (manually clearable)
- Memory registers: `MS`, `MR`, `M+`, `M-`
- Built-in library of mathematical and physical constants

### Offline Unit Converter
- Categories: Length, Mass, Area, Volume, Temperature, Speed, Time, Digital Storage
- Currency rates managed **entirely offline** — updated via manual input or imported from a local JSON/CSV file
- No `INTERNET` permission requested. Ever.

### Encrypted Private Vault
- Concealed entry point — invisible to the OS app drawer and casual observers
- **AES-256-GCM** (authenticated encryption) — ensures both confidentiality and integrity of stored data
- Encryption keys managed by the **Android Keystore System**, backed by the device's TEE or Secure Element where available
- Supports files, photos, notes, and other private assets

---

## Security & Privacy

### Privacy Features

| Feature | Description |
|---|---|
| Zero Network Footprint | No `INTERNET` permission. Data never leaves the device. |
| Purge-on-Lock | Session keys are wiped from RAM when the app is backgrounded or the screen turns off. |
| Privacy Window | `FLAG_SECURE` prevents vault content from appearing in the Recent Apps screen or system screenshots. |
| Decoy Mode | A secondary PIN opens a separate empty vault. See legal notice below. |
| Brute-Force Protection | Optional auto-wipe of vault data after a configurable number of failed attempts. |

### Threat Model

**ObscuraCalc protects against:**
- Casual snooping by someone with physical access to your device
- Forensic extraction from device storage (data is encrypted at rest)
- Data theft by malicious apps (Android sandbox + AES-256-GCM)

**ObscuraCalc does NOT protect against:**
- A rooted or OS-compromised device — advanced actors may be able to bypass protections at the OS level
- Credential loss — if you forget your PIN or password, the data is cryptographically unrecoverable. There is no reset mechanism.

---

## ⚖️ Legal Notice: Decoy Mode

Decoy Mode allows a secondary credential to open a separate, empty vault. This feature is designed **exclusively for protection against personal coercion** — for example, being pressured by a threatening individual to unlock your device.

**This feature must not be used to mislead or obstruct law enforcement officers or judicial processes.** Doing so may constitute obstruction of justice or providing false information under applicable law, regardless of what app you use. The developer bears no liability for misuse of this feature.

By enabling Decoy Mode, you acknowledge that you understand this distinction and accept full responsibility for how it is used.

---

## ⚠️ Critical Data Warning

ObscuraCalc is a **zero-knowledge local vault**. This means:

1. **No cloud recovery.** There are no remote servers. Losing your credentials means losing your data — permanently.
2. **Uninstalling or clearing app data destroys the vault.** Export encrypted backups regularly to external physical storage.
3. **Backup archives** are separately encrypted with a user-defined passphrase.

**Back up before you experiment.**

---

## Technical Architecture

ObscuraCalc uses a multi-module Clean Architecture for separation of concerns, testability, and long-term maintainability.

| Module | Responsibility | Stack |
|---|---|---|
| `:app` | Entry point, navigation graph, DI container | Jetpack Compose, Hilt |
| `:core-security` | AES-256-GCM, PBKDF2, Android Keystore integration | Kotlin, Android Security |
| `:core-vault` | Encrypted blob storage, Room database | Room, SQLCipher |
| `:core-calculator` | Deterministic math parser and expression engine | Pure Kotlin (JUnit tested) |
| `:feature-vault` | File explorer, media preview, vault setup UI | Compose, Media3 |
| `:feature-settings` | Stealth onboarding, security configuration | Jetpack Compose |

---

## Building from Source

**Requirements:** Android Studio Hedgehog or later, JDK 17+, Android SDK 26+

```bash
git clone https://github.com/akagafin/ObscuraCalc.git
cd ObscuraCalc
./gradlew assembleDebug
```

Install to a connected device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Vault Access & Stealth Configuration

Documentation for vault entry and stealth setup is kept in [`SECURITY.md`](./SECURITY.md) and is intentionally not indexed here, to preserve the effectiveness of the concealment mechanism.

---

## Roadmap

- [ ] Core calculator engine (parser + evaluator)
- [ ] Unit converter with offline currency import
- [ ] Vault storage with AES-256-GCM encryption
- [ ] Stealth entry point and PIN/password setup
- [ ] Biometric authentication (BiometricPrompt)
- [ ] Decoy Mode
- [ ] Encrypted backup and restore
- [ ] F-Droid submission

---

## 🌐 Export Control Notice

This software includes cryptographic functionality and may be subject to export control laws in certain jurisdictions.

ObscuraCalc uses only standard, publicly recognized cryptographic algorithms (AES-256-GCM) as implemented by the Android platform's built-in security libraries. It does not implement any proprietary or non-standard cryptography.

As an open source project with publicly available source code using standard cryptography, this software is not subject to U.S. Export Administration Regulations (EAR) reporting requirements under 15 C.F.R. § 742.15(b), as amended by the March 2021 BIS final rule.

However, users and distributors are responsible for compliance with the export and import laws of their own jurisdiction. This software must not be used in violation of any applicable law, including in countries subject to U.S. embargo or sanctions.

Source code is publicly available at: https://github.com/akagafin/ObscuraCalc

---

## License

Licensed under the **Apache License 2.0**. See [`LICENSE`](./LICENSE) for the full legal text.

---

## Contributing

Contributions and bug reports are welcome. For security-related disclosures, please open a **private GitHub advisory** rather than a public issue.
