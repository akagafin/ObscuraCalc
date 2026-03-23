# ObscuraCalc

ObscuraCalc is an offline-first Android application that combines a real scientific calculator, offline unit and currency conversion, and an authenticated private space for encrypted file storage.

The project is designed for privacy-respecting distribution channels such as F-Droid and direct APK delivery. It does not include analytics, advertising, telemetry, cloud sync, user accounts, or mandatory Google Play Services.

## Status

This repository contains a fresh multi-module Kotlin/Compose implementation scaffolded for long-term open-source maintenance.

The local workspace used to generate this project did not include Java, Gradle, Git, or Android SDK tooling, so build validation could not be completed here. Gradle scripts and wrapper metadata are included, but `gradle-wrapper.jar` was not available to vendor from this environment and must be generated in a full Android development setup before the wrapper can run end-to-end.

## Project Overview

ObscuraCalc has three product surfaces:

1. A genuinely usable scientific calculator with history, memory registers, constants, and testable deterministic expression parsing.
2. Fully offline converters for length, mass, area, volume, temperature, speed, time, digital size, and manually managed currency rates.
3. A user-configured private space that encrypts app-held files at rest and locks automatically when app context changes.

## Feature List

### Calculator

- Standard arithmetic and parentheses
- Scientific functions: `sin`, `cos`, `tan`, inverse trig, `log`, `ln`, `exp`, `sqrt`, powers, constants
- Degree/radian toggle
- Memory store, recall, add, subtract, clear
- Deterministic parser in `core-calculator` with JVM tests

### Converter

- Offline conversion categories:
  - Length
  - Mass
  - Area
  - Volume
  - Temperature
  - Speed
  - Time
  - Digital size
- Currency conversion with:
  - Manual base currency selection
  - Manual rate entry
  - JSON import
  - CSV import
  - No automatic updates

### Private Space

- PIN or password based vault setup
- Optional biometric unlock via `BiometricPrompt`
- Optional hidden calculator input trigger after vault setup
- Automatic locking on background, screen off, explicit lock, and process loss
- AES-256-GCM encrypted file storage in app-private internal storage
- Manual import from system storage
- Manual export to system storage
- Manual encrypted backup and restore
- Advanced off-by-default wipe and decoy options

## Privacy Philosophy

ObscuraCalc keeps privacy simple:

- Everything works offline.
- There is no network permission.
- No personal data is collected.
- No usage metrics are sent anywhere.
- Local encryption protects vault content at rest inside app-controlled storage.
- Exporting or sharing files happens only when the user explicitly chooses a destination.

## Security Summary

ObscuraCalc is a user-space Android application. It can provide meaningful protection against casual snooping and unauthorized access by other people who use the same device, but it is not a system-level secure folder and does not claim absolute secrecy.

### What it does

- Stores vault files inside app-private storage
- Encrypts vault content at rest with AES-256-GCM
- Generates keystore-backed wrapping keys on device
- Requires authentication before vault access
- Locks on app background and screen-off events
- Avoids network-based exfiltration by not requesting internet access

### What it does not do

- Clone arbitrary third-party Android apps
- Bypass Android sandbox rules
- Protect against a rooted or compromised operating system
- Prevent lawful forensic access
- Guarantee recovery if the user forgets credentials or destroys backups

The full threat model is in [docs/THREAT_MODEL.md](/C:/Users/admin/Documents/projecto/docs/THREAT_MODEL.md).

## Permissions

ObscuraCalc intentionally requests as little as possible.

- `android.permission.USE_BIOMETRIC`
  Used only if the user enables biometric unlock for the vault.

The app does not request `INTERNET`, storage, contacts, camera, location, microphone, or advertising permissions.

File import and export use the Storage Access Framework, which lets the user choose files and destinations without broad storage access.

## Architecture

High-level architecture documentation lives in [docs/ARCHITECTURE.md](/C:/Users/admin/Documents/projecto/docs/ARCHITECTURE.md).

Module summary:

- `app`: navigation, theme, lifecycle lock hooks, application container
- `core-calculator`: parser, evaluator, memory register, JVM tests
- `core-converter`: unit conversion logic and currency import
- `core-security`: credential handling, keystore integration, session lock state
- `core-vault`: encrypted blob storage, metadata index, backup/restore
- `feature-calculator`: calculator UI
- `feature-converter`: converter UI and local currency rate persistence
- `feature-vault`: vault setup, auth, explorer, preview, backup UI
- `feature-settings`: settings and hidden onboarding path
- `feature-legal`: in-app legal document rendering

## Installation

### F-Droid

When an F-Droid build is published:

1. Open F-Droid.
2. Search for `ObscuraCalc`.
3. Review the requested permissions.
4. Install and launch.

### Direct APK

1. Build a release APK in a local Android development environment.
2. Verify the signing key and release notes.
3. Install the APK manually.

## Local Development

Typical setup:

1. Install Android Studio or a compatible Android SDK + JDK 17 toolchain.
2. Regenerate the Gradle wrapper jar if needed with a local Gradle install.
3. Open the project and sync dependencies.
4. Run unit tests in `core-calculator` and `core-converter`.
5. Run instrumentation tests on a device or emulator for vault flows.

## Usage Overview

### Everyday Tools

- Launching the app opens the calculator.
- The calculator remains fully usable when the vault is locked.
- The converter is available from normal navigation.

### Private Space

- Initial vault setup is intentionally non-prominent and should be documented carefully for users.
- After setup, users may enable a hidden calculator sequence as an unlock path.
- Vault files remain inaccessible until a configured credential or biometric flow succeeds.

### Backups

- Backups are manual.
- Backup archives are separately encrypted with a user-chosen passphrase.
- Restores import data into the currently configured vault.

## Backup and Data Loss Warning

Losing a vault credential or forgetting a backup passphrase may permanently prevent access to stored files.

Wipe-after-failures is intentionally disabled by default because it can cause irreversible data loss. Users should only enable it after understanding the consequences and creating a tested backup.

## Legal Notice

ObscuraCalc is not Samsung Secure Folder, Apple Secure Enclave, or a system-level secure container. It does not claim anonymity, invulnerability, or protection against forensic or state-level adversaries.

Users remain responsible for:

- Choosing strong credentials
- Keeping the underlying device trustworthy
- Maintaining working backups
- Understanding the documented threat model and limitations

## License

ObscuraCalc is licensed under the Apache License 2.0.

See [LICENSE](/C:/Users/admin/Documents/projecto/LICENSE) for the full text.
