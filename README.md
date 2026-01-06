# Multimedia Studio (Praktikum #8)

Aplikasi Android berbasis **Jetpack Compose** untuk pengelolaan multimedia yang mencakup fitur **Perekam Audio**, **Pemutar Audio & Video**, serta **Integrasi Kamera & Galeri**.  
Proyek ini dikembangkan sebagai tugas **Praktikum Modul #8 Mata Kuliah Mobile Programming**.

---

## 📱 Fitur Utama

### 1. 🎙️ Audio Recorder & Player
- **Perekam Suara:** Merekam audio menggunakan `MediaRecorder` dan menyimpannya secara lokal dengan format `.mp4`.
- **Pemutar Audio:** Memutar file rekaman menggunakan **ExoPlayer** dengan kontrol *Play*, *Pause*, dan *Seekbar* interaktif.
- **Manajemen File:** Daftar rekaman yang dapat di-*rename* (ubah nama) dan dihapus (*delete*).

### 2. 🎬 Video Player Interaktif
- **Pemutar Video:** Memutar video dari penyimpanan lokal menggunakan **Media3 ExoPlayer**.
- **Gesture Support:** Mendukung **Pinch-to-Zoom** dan **Pan (geser)** untuk memperbesar dan menggeser video saat diputar.
- **Fullscreen Mode:** Mendukung mode layar penuh dengan orientasi landscape.

### 3. 📸 Camera & Gallery
- **Ambil & Pilih Media:** Mengambil foto melalui kamera atau memilih foto/video dari galeri perangkat.
- **Preview Canggih:** Pratinjau gambar dengan dukungan gesture zoom dan geser tanpa berpindah layar.
- **Simpan ke Galeri:** Menyimpan foto hasil tangkapan aplikasi ke galeri utama perangkat menggunakan **Scoped Storage (MediaStore)**.

---

## 🛠️ Teknologi & Library

- **Bahasa:** Kotlin 2.0+
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Media:**
  - `androidx.media3:media3-exoplayer` (Audio & Video Player)
  - `android.media.MediaRecorder` (Perekam Audio)
- **Icons:** `androidx.compose.material:material-icons-extended`
- **Navigation:** Navigation Compose

---

## 📂 Struktur Proyek

Struktur folder disusun berdasarkan pendekatan **feature-based** untuk memudahkan pengembangan dan pemeliharaan aplikasi:

```text
id.antasari.p8_multimedia_nimanda
├── ui/
│   ├── gallery/    # Screen Kamera & Galeri
│   ├── home/       # Halaman Utama (Dashboard)
│   ├── player/     # Screen Pemutar Audio
│   ├── recorder/   # Screen Perekam Audio
│   ├── video/      # Screen Pemutar Video
│   ├── theme/      # Konfigurasi Tema & Warna
│   └── NavGraph.kt # Pengaturan Navigasi
├── util/           # Helper untuk manajemen file
└── MainActivity.kt


🚀 Cara Menjalankan Aplikasi

Clone repository ini ke komputer Anda.

Buka project menggunakan Android Studio (disarankan versi terbaru).

Tunggu proses Gradle Sync hingga selesai.

Pastikan koneksi internet stabil untuk mengunduh dependensi (Compose & ExoPlayer).

Hubungkan perangkat Android atau jalankan melalui Emulator.

Berikan izin Microphone dan Camera saat aplikasi pertama kali dijalankan.

📝 Informasi Akademik

Mata Kuliah : Mobile Programming
Program Studi : S1 Teknologi Informasi
Universitas : UIN Antasari Banjarmasin

Nama Mahasiswa : M.Azhiem Fadillah
NIM : 230104040128
Dosen Pengampu : Muhayat, M.IT
