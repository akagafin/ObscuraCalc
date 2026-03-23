# ObscuraCalc

ObscuraCalc is a privacy-focused, offline-first Android application that seamlessly integrates a high-performance scientific calculator, a comprehensive unit converter, and a military-grade encrypted private vault. Designed for users who demand absolute data sovereignty, ObscuraCalc disguises its secure storage behind a fully functional, innocent-looking calculator interface.

The project is engineered for security-conscious distribution, such as F-Droid, and strictly follows a **"No-Cloud, No-Tracking, No-Permission-Abuse"** philosophy.

---

## 🚀 Key Product Pillars

### 1. Professional Scientific Calculator
- **Deterministic Math Engine**: A robust calculation core providing precise results for complex nested arithmetic and scientific expressions.
- **Scientific Suite**: Full support for Trigonometry (`sin`, `cos`, `tan`), Logarithms (`log`, `ln`), Roots, Powers, Factorials, and more.
- **Advanced State Management**: Features persistent (but clearable) calculation history and standard memory registers (MS, MR, M+, M-).
- **Physical Constants**: A built-in library of universal mathematical and physical constants for scientific work.

### 2. Comprehensive Offline Converter
- **Multi-Category Support**: Instant, high-precision conversion for Length, Mass, Area, Volume, Temperature, Speed, Time, and Digital Storage.
- **Privacy-First Currency**: Exchange rates are managed entirely offline via manual entry or secure JSON/CSV imports. The app **does not request the `INTERNET` permission**, ensuring your financial interests are never tracked.

### 3. Stealth Private Space (The Vault)
- **Zero-Footprint Stealth**: Operates entirely behind a functional calculator. The vault entry point and configuration are invisible to the standard OS and casual observers.
- **Military-Grade Encryption**: All vault assets are protected using **AES-256-GCM** (Galois/Counter Mode), ensuring both data confidentiality and integrity.
- **Hardware-Level Security**: Encryption keys are protected by the **Android Keystore System**, utilizing the device's Trusted Execution Environment (TEE) or Secure Element (SE) where available.

---

## 🛠 Usage & Onboarding

### Initializing the Hidden Vault (First Time Setup)
To maintain the app's camouflage, the vault setup is not prominently displayed:
1. Launch ObscuraCalc and navigate to the **Settings** menu.
2. Scroll to the **About/Version** section at the bottom.
3. Tap the **"Version 0.1.0"** label **7 times** in rapid succession.
4. The Vault Setup Wizard will appear. Follow the prompts to create your primary **Numeric PIN** or **Alphanumeric Password**.

### Accessing Your Secured Data
Once configured, you can trigger the vault authentication screen through:
- **The Padlock Icon**: A discrete icon in the primary calculator toolbar.
- **Hidden Math Sequence**: Configure a custom math expression (e.g., `your_pin + "="`) to act as an instant "jump-to-vault" trigger directly from the keypad.
- **Biometric Authentication**: Securely unlock using Fingerprint or Face sensors via the system's `BiometricPrompt`.

---

## 🏗 Technical Architecture

ObscuraCalc utilizes a modern, multi-module Clean Architecture to ensure code quality, testability, and clear separation of concerns.

| Module | Responsibility | Tech Stack |
| :--- | :--- | :--- |
| `:app` | Entry point, Navigation, DI Container. | Jetpack Compose, Hilt |
| `:core-security` | AES-GCM logic, PBKDF2, Keystore integration. | Kotlin, Android Security |
| `:core-vault` | Encrypted blob storage and Room database. | Room, SQLCipher logic |
| `:core-calculator` | Deterministic math parser and engine. | Pure Kotlin (JUnit tested) |
| `:feature-vault` | File explorer, Media preview, and setup UI. | Compose, Media3 |
| `:feature-settings`| Stealth onboarding and security configuration. | Jetpack Compose |

---

## 🛡 Security Policy & Threat Model

### What ObscuraCalc Protects Against:
- **Casual Snooping**: Prevents unauthorized persons from viewing your private files.
- **Forensic Extraction**: Data remains encrypted even if the device's physical storage is accessed externally.
- **Malicious Apps**: Other apps cannot read vault data due to Android's sandbox and AES-256 encryption.

### What it Does Not Protect Against:
- **OS Compromise**: A rooted or compromised device may allow advanced actors to bypass certain protections.
- **Credential Loss**: If you forget your PIN or Password, the data is **mathematically impossible** to recover.

---

## 🔒 Advanced Privacy Features

- **Zero Network Footprint**: No `INTERNET` permission. Your data never leaves your device.
- **Purge-on-Lock**: Session keys are instantly wiped from RAM when the app is backgrounded or the screen turns off.
- **Privacy Window**: Uses `FLAG_SECURE` to prevent vault content from appearing in the "Recent Apps" screen or system screenshots.
- **Decoy Mode**: Support for a secondary "Decoy PIN" that opens an empty vault to mislead observers under duress.
- **Brute-Force Protection**: Optional auto-wipe of all vault data after a user-defined number of failed attempts.

---

## ⚠️ Critical Data Warning

**ObscuraCalc is a zero-knowledge local vault.**

1.  **No Cloud Recovery**: There are no remote servers. If you lose your credentials, **we cannot reset them**.
2.  **Uninstalling/Wiping**: Deleting the app or clearing its data will permanently remove the encrypted vault. **Perform regular encrypted backups** to external physical storage.
3.  **Encrypted Backups**: Backup archives are separately encrypted with a user-defined passphrase.

---

## 📜 License
Licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for the full legal text.
