ObscuraCalc Terms of Service

Last updated: March 22, 2026

1. Introduction

ObscuraCalc is an offline Android application that provides calculator tools, converters, and an
optional private space for locally stored files. These Terms of Service explain the conditions under
which you may use the application and related source code distribution.

By installing, building, or using ObscuraCalc, you agree to these terms to the extent permitted by
applicable law. If you do not agree, do not use the application.

2. License and Open Source Status

ObscuraCalc is distributed under the Apache License 2.0. Your rights to copy, modify, and distribute
the software are governed by that license. Nothing in these terms replaces the Apache License where
the license applies directly to the software.

These terms address product use, project maintenance expectations, and important limitations that
users should understand before relying on the application for sensitive storage.

3. Intended Use

ObscuraCalc is intended for lawful personal and professional use as:

- a scientific calculator
- an offline conversion tool
- a local encrypted storage space for files managed by the app

The application is not marketed or provided as a system-level secure container, anti-forensics tool,
or anonymity service.

4. No Accounts or Online Services

ObscuraCalc does not require an account and does not operate an online service for normal use.
Features are designed to function locally on the device without mandatory cloud infrastructure.

5. Security Scope and Limitations

ObscuraCalc attempts to protect vault data stored by the app through local authentication controls
and encryption at rest. These protections are meaningful only within the limits of a normal Android
user-space application.

ObscuraCalc does not promise:

- absolute security
- invisibility to a compromised operating system
- protection from rooted devices
- protection from OEM, kernel, or physical hardware attackers
- protection from lawful search, seizure, or forensic analysis
- secure isolation of arbitrary third-party Android applications

Any statement in the app, documentation, or source repository should be interpreted consistently
with these limitations.

6. User Responsibilities

You are responsible for:

- choosing and protecting your credential
- keeping your device reasonably secure
- understanding the documented threat model
- managing backups and testing restore procedures
- reviewing settings before enabling destructive options such as wipe-after-failures

If you forget your vault credential or lose access to backups, your data may become permanently
inaccessible.

7. Data Import, Export, and Backup

Imported files are re-encrypted into app-managed storage. Exporting a file writes it to a
destination you choose through Android's document picker. Once exported, the file is outside
ObscuraCalc's protection boundary.

Backups are manual. Backup archives are encrypted with a passphrase that you choose. Loss of that
passphrase may prevent restoration. Restoring a backup imports data into the currently configured
vault and may overwrite or duplicate content depending on future implementation details and user
actions.

8. Advanced Security Options

Certain features, such as wipe-after-failed-attempts or decoy responses, may increase the chance of
data loss or user confusion if used carelessly. These features are optional and disabled by default.
You assume responsibility for the consequences of enabling them.

9. Acceptable Use

You may use, modify, and distribute ObscuraCalc as permitted by the Apache License 2.0 and
applicable law. You may not use the project name, documentation, or user interface to falsely claim
capabilities that the software does not provide.

You should not represent ObscuraCalc as a substitute for platform-provided secure storage products,
military-grade security tools, or guaranteed forensic resistance.

10. Third-Party Components

ObscuraCalc may include open-source third-party libraries under their respective licenses. Those
licenses remain applicable to the components they govern.

11. No Warranty

To the maximum extent permitted by applicable law, ObscuraCalc is provided on an "AS IS" and "AS
AVAILABLE" basis, without warranties of any kind, express or implied. This includes, without
limitation, implied warranties of merchantability, fitness for a particular purpose, title,
non-infringement, security, availability, or accuracy.

12. Limitation of Liability

To the maximum extent permitted by applicable law, the authors, contributors, distributors, and
maintainers of ObscuraCalc will not be liable for any indirect, incidental, special, consequential,
exemplary, or punitive damages, or for any loss of data, profits, goodwill, or device functionality
arising from the use of or inability to use the software.

This limitation applies even if the possibility of such damages was known or reasonably foreseeable.

13. Changes

The project may evolve over time. Future releases may change behavior, documentation, or features.
Updated terms may be included with later releases. Continued use of a later release indicates
acceptance of the terms bundled with that release.

14. Contact and Source of Truth

Because ObscuraCalc is distributed as open-source software, the source repository and release bundle
associated with your copy are the primary sources of truth for licensing, notices, and
documentation. If your build was provided by a distributor, you should review both the bundled
documents and the distributor's presentation.

15. Entire Understanding

These terms, together with the Apache License 2.0 and any bundled notices, form the general
understanding for use of ObscuraCalc. If part of these terms is found unenforceable, the remaining
sections remain in effect to the extent allowed by law.
