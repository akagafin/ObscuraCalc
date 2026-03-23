# ObscuraCalc Threat Model

Last updated: March 22, 2026

## Summary

ObscuraCalc is a user-space Android application that provides authenticated access control and local encryption for app-held vault data. It is designed to reduce risk from casual snooping, opportunistic access, and ordinary device-sharing scenarios. It is not a system-level secure container and does not claim absolute protection.

## Assets Protected

- Vault-stored files imported into app-private storage
- Vault encryption keys while stored in wrapped form
- User credentials in hashed or derived form
- Vault metadata and app-held private data related to stored content

## Threats Considered

### Casual Device Snooping

The app is designed to reduce risk from someone casually opening the device and browsing within the app without authorization. Vault content remains encrypted at rest and unavailable unless the user successfully authenticates.

### Lost or Stolen Device

If the device is lost or stolen and the operating system remains intact, vault files stored by ObscuraCalc stay encrypted at rest inside app-private storage. Automatic locking on background and screen-off reduces the chance of an unlocked session remaining exposed.

### Unauthorized Access by Acquaintances

The app is intended to help when friends, coworkers, or family members have temporary physical access to the device but do not know the configured vault credential.

### Malware Within the Standard Android App Sandbox

The app attempts to minimize exposure by avoiding network access, keeping vault files in app-private storage, and not exposing content to media scanners or other apps unless the user manually exports a file.

## Threats Explicitly Not Covered

ObscuraCalc does not claim protection against:

- Rooted devices
- Compromised or malicious operating systems
- OEM, bootloader, or kernel-level attackers
- Nation-state or intelligence-service adversaries
- Physical chip-off or hardware extraction attacks
- Lawful forensic access under applicable law
- Coercion, shoulder-surfing, or unsafe user behavior

## Security Guarantees

Within the threat model above, ObscuraCalc aims to provide these guarantees:

- Vault data is encrypted at rest before it is stored by the application.
- Vault access is gated by authentication.
- The unlocked session is cleared on explicit lock, background transitions, screen-off, and process loss.
- The app performs no network transmission because it does not require internet access.
- Files remain in app-private storage unless the user explicitly exports them.

## Limitations

- Protection is limited to user-space application boundaries.
- The app has no OEM or system privileges.
- The app cannot clone arbitrary third-party Android applications.
- The app cannot stop a compromised device owner environment from observing input or memory.
- Automatic wipe can only remove app-managed files and keys; it cannot guarantee forensic-grade erasure of underlying flash storage.
- Exported files leave the vault boundary and become subject to the destination chosen by the user.

## Design Choices That Reduce Risk

- No analytics, advertising, telemetry, or cloud sync
- No network permission
- Keystore-wrapped master key storage
- Credential-derived wrapping key with PBKDF2-HMAC-SHA256
- Per-file encryption keys
- Manual, separately passphrase-protected backups
- Honest UI and documentation that avoid overstating security claims

## User Responsibilities

Users remain responsible for:

- Choosing and remembering strong credentials
- Keeping the device OS trustworthy and updated
- Avoiding rooted or compromised devices when privacy matters
- Creating and testing backups before enabling destructive policies
- Understanding that exported files are outside the vault once exported

## Residual Risk Statement

ObscuraCalc meaningfully improves privacy for local app-held files in ordinary Android user-space scenarios. It should be understood as a practical privacy tool, not as a replacement for a trusted operating system, verified hardware security boundary, or formal secure enclave product.
