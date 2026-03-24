import os
replacements = {
    '"androidx.core:core-ktx:1.13.1"': '"androidx.core:core-ktx:1.18.0"',
    '"androidx.lifecycle:lifecycle-runtime-ktx:2.8.7"': '"androidx.lifecycle:lifecycle-runtime-ktx:2.10.0"',
    '"androidx.lifecycle:lifecycle-runtime-compose:2.8.7"': '"androidx.lifecycle:lifecycle-runtime-compose:2.10.0"',
    '"androidx.lifecycle:lifecycle-process:2.8.7"': '"androidx.lifecycle:lifecycle-process:2.10.0"',
    '"androidx.activity:activity-compose:1.9.3"': '"androidx.activity:activity-compose:1.13.0"',
    '"androidx.fragment:fragment-ktx:1.8.5"': '"androidx.fragment:fragment-ktx:1.8.9"',
    '"androidx.navigation:navigation-compose:2.8.5"': '"androidx.navigation:navigation-compose:2.9.7"',
    '"androidx.compose:compose-bom:2024.10.01"': '"androidx.compose:compose-bom:2026.03.00"',
    '"com.google.android.material:material:1.12.0"': '"com.google.android.material:material:1.13.0"',
    '"androidx.test.ext:junit:1.2.1"': '"androidx.test.ext:junit:1.3.0"',
    '"androidx.test.espresso:espresso-core:3.6.1"': '"androidx.test.espresso:espresso-core:3.7.0"',
    '"androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"': '"androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0"',
    '"androidx.documentfile:documentfile:1.0.1"': '"androidx.documentfile:documentfile:1.1.0"',
    'targetSdk = 35': 'targetSdk = 36',
    'compileSdk = 35': 'compileSdk = 36'
}

for root, _, files in os.walk('.'):
    for f in files:
        if f == 'build.gradle.kts':
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
            original = content
            for k, v in replacements.items():
                content = content.replace(k, v)
            if content != original:
                with open(path, 'w', encoding='utf-8') as file:
                    file.write(content)
                print(f"Updated {path}")
