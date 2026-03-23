ObscuraCalc Threat Model

Last updated: March 22, 2026

Summary

ObscuraCalc is a user-space Android application that encrypts app-held vault data at rest and requires authentication before access. It is designed to reduce risk in everyday local privacy scenarios, not to provide absolute protection.

Assets Protected

- Vault-stored files
- Wrapped encryption keys
- User credential material in hashed or derived form
- Vault metadata and related app-held private data

Threats Considered

- Casual device snooping
- Lost or stolen device scenarios where the OS remains intact
- Unauthorized access by acquaintances with temporary physical access
- Malware limited to ordinary Android sandbox boundaries

Threats Not Covered

- Rooted or compromised operating systems
- OEM, bootloader, or kernel-level attackers
- Nation-state adversaries
- Physical hardware extraction
- Lawful forensic access

Security Guarantees

- Encryption of vault data at rest
- Authentication-gated access
- Automatic lock on screen-off, background, explicit lock, and process loss
- No intentional network exfiltration by the app itself

Limitations

- User-space only
- No OEM privileges
- No arbitrary third-party app cloning
- No claim of absolute security or anonymity

User Responsibilities

- Maintain strong credentials
- Keep the device trustworthy
- Protect exported files and backups
- Understand the consequences of enabling wipe-after-failures
