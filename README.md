# ObscuraCalc

ObscuraCalc adalah aplikasi Android offline-first yang menggabungkan kalkulator saintifik nyata, konverter unit dan mata uang offline, serta **Private Space** (Vault) terenkripsi untuk penyimpanan file aman.

Aplikasi ini dirancang untuk privasi total: tanpa analitik, tanpa iklan, tanpa telemetri, dan **TANPA IZIN INTERNET**.

## Fitur Utama

### 1. Kalkulator Saintifik
- Operasi aritmatika standar dan fungsi saintifik (`sin`, `cos`, `log`, `sqrt`, dll).
- Riwayat perhitungan dan register memori (MS, MR, M+, M-).
- Mode Derajat (DEG) dan Radian (RAD).

### 2. Konverter Offline
- Konversi Panjang, Massa, Area, Volume, Suhu, Kecepatan, Waktu, dan Ukuran Digital.
- Konverter Mata Uang dengan input kurs manual (menjaga privasi karena tidak ada update otomatis via internet).

### 3. Private Space (Vault) - Fitur Tersembunyi
Fitur ini memungkinkan Anda menyimpan file secara terenkripsi di dalam folder privat aplikasi yang tidak bisa diakses oleh aplikasi galeri atau file manager standar.

#### Cara Mengaktifkan Vault (Setup Awal):
Karena ini adalah aplikasi penyamaran, menu setup tidak muncul secara mencolok:
1.  Buka **Settings**.
2.  Scroll ke paling bawah hingga menemukan informasi **Version**.
3.  Ketuk (Tap) pada teks **"Version 0.1.0"** sebanyak **7 kali** berturut-turut.
4.  Layar setup Vault akan terbuka. Silakan buat PIN atau Password Anda.

#### Cara Membuka Vault:
- **Metode Normal**: Melalui ikon Gembok di Top Bar atau menu navigasi.
- **Hidden Trigger**: Jika dikonfigurasi, Anda bisa mengetikkan urutan angka rahasia (misal: `123456`) langsung di layar kalkulator lalu tekan `=` untuk memicu layar login.

## Keamanan & Privasi

### Enkripsi Data
- **AES-256-GCM**: Semua file di dalam Vault dienkripsi menggunakan standar militer.
- **Android Keystore**: Kunci enkripsi dilindungi oleh sistem perangkat keras perangkat (jika tersedia), sehingga tidak bisa dicuri dengan copy-paste file sistem.
- **Biometric Unlock**: Mendukung Sidik Jari atau Face Unlock yang aman.

### Fitur Perlindungan Lanjut
- **Auto-Lock**: Vault otomatis terkunci saat aplikasi keluar, layar mati, atau berpindah aplikasi.
- **Wipe After Failures**: (Opsional) Menghapus seluruh isi Vault secara otomatis jika salah memasukkan PIN berkali-kali (Default: Mati).
- **Decoy Mode**: Jika salah PIN, aplikasi akan berpindah kembali ke kalkulator seolah-olah tidak terjadi apa-apa, untuk mengecoh orang yang mencoba mengintip.
- **Privacy Window**: Mencegah aplikasi muncul di *Recent Apps* (screenshot otomatis sistem Android) saat Vault sedang terbuka.

## PENTING: Peringatan Kehilangan Data

**HARAP BACA DENGAN TELITI:**
1.  **Lupa PIN/Password**: Karena enkripsi bersifat lokal dan tidak ada server, **TIDAK ADA fitur "Lupa Password"**. Jika Anda lupa, data di dalam Vault tidak dapat dipulihkan.
2.  **Uninstal Aplikasi**: Menghapus aplikasi akan menghapus seluruh data di dalam Vault. Pastikan melakukan **Export** atau **Backup** file penting sebelum menghapus aplikasi.
3.  **Wipe Feature**: Jika fitur "Wipe After Failures" diaktifkan, pastikan Anda ingat PIN Anda. Data yang terhapus karena salah PIN tidak bisa dikembalikan.
4.  **Backup Manual**: Lakukan backup secara rutin ke penyimpanan eksternal melalui menu Vault. File backup juga akan dienkripsi dengan password tambahan yang Anda buat.

## Izin (Permissions)

ObscuraCalc hanya meminta izin yang benar-benar diperlukan:
- `USE_BIOMETRIC`: Hanya digunakan jika Anda mengaktifkan Fingerprint/Face Unlock.
- **TIDAK ADA IZIN INTERNET**: Menjamin data Anda tidak akan pernah terkirim ke server mana pun.

## Lisensi
ObscuraCalc dilisensikan di bawah Apache License 2.0.
