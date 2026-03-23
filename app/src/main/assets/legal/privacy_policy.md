ObscuraCalc Privacy Policy

Last updated: March 22, 2026

1. Overview

ObscuraCalc is designed to work locally on your device. This Privacy Policy explains what
information the app does and does not process, how local data is stored, and what happens when you
choose to import, export, or back up files.

2. Information We Do Not Collect

ObscuraCalc does not collect personal data for remote processing. In particular, the app does not
include:

- analytics
- advertising SDKs
- telemetry
- crash reporting services
- user accounts
- profile tracking
- cloud sync
- mandatory Google Play Services

The app does not request internet access for normal operation.

3. Information Stored Locally on Your Device

If you use the vault feature, ObscuraCalc stores certain information locally on the device in order
to function:

- encrypted vault files
- encrypted metadata required to manage those files
- wrapped encryption keys
- hashed and derived credential material
- local app settings such as lock behavior and optional security preferences
- manually imported or entered currency rates

This information is stored only on the device unless you explicitly export or back it up.

4. Authentication Data

Your PIN or password is not intentionally stored in plaintext by the app. The app stores derived or
hashed forms needed to validate authentication and to wrap or unwrap vault keys.

If you enable biometric unlock, the app uses Android's public biometric APIs and device keystore
facilities to support authentication. Biometric templates themselves are managed by the operating
system, not by ObscuraCalc.

5. File Import and Export

When you import a file, the app reads the file you selected through Android's document picker and
encrypts it into app-managed storage.

When you export a file, the app writes decrypted content to the destination you explicitly choose.
After export, the file is outside ObscuraCalc's protected storage boundary and is subject to the
privacy and security properties of the destination app, storage provider, or filesystem you
selected.

6. Backups

ObscuraCalc supports manual encrypted backups. Backup archives are created only when you request
them and are protected with a passphrase you choose. The app does not upload backups anywhere
automatically.

You are responsible for protecting exported backups and remembering the backup passphrase.

7. Permissions

ObscuraCalc aims to request only the permissions it strictly needs.

- `USE_BIOMETRIC`
  Requested so the app can offer biometric unlock if you choose to enable it.

The app does not request broad storage access. File import and export use the system document picker
so you can choose exact files and destinations.

8. Network Activity

ObscuraCalc is designed to operate without network access. The app does not intentionally transmit
vault data, credentials, usage logs, or conversion data over the internet.

If you export files or backups to third-party apps or remote storage providers through Android's
document picker, those separate apps or services operate under their own privacy policies.

9. Security Limitations

While ObscuraCalc uses local encryption and authentication controls, no user-space Android app can
guarantee absolute privacy. The app does not claim protection against rooted devices, compromised
operating systems, OEM or kernel-level access, physical extraction, or lawful forensic access.

These limitations are important parts of the product's privacy posture and are not hidden.

10. Children

ObscuraCalc is a general-purpose utility application. It does not knowingly collect personal
information from children because it does not operate a remote data collection service in the
ordinary course of use.

11. Changes to This Policy

Future releases may update this Privacy Policy to reflect project changes, legal clarifications, or
documentation improvements. The version bundled with your installed release applies to that release.

12. Open Source Transparency

ObscuraCalc is intended for open-source distribution. The source code, bundled legal documents, and
published release artifacts are the best sources for understanding exactly what the app does.

13. Contact

If you need help interpreting a particular build, use the public source repository, release channel,
or distributor associated with the copy you installed. Because ObscuraCalc is distributed through
open-source channels, the maintainer and support path may vary by build.
