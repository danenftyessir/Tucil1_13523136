# 🎮 IQ Puzzler Pro Solver

Selamat datang di aplikasi IQ Puzzler Pro Solver! Sebuah solusi canggih untuk permainan papan IQ Puzzler Pro menggunakan algoritma brute force.

## Deskripsi

Program ini menyelesaikan teka-teki IQ Puzzler Pro dengan menerapkan pendekatan brute force murni. Program secara sistematis mengeksplorasi semua kemungkinan penempatan komponen untuk menemukan solusi valid dalam tiga konfigurasi berbeda:
- Mode standar: Mengisi seluruh papan dengan komponen puzzle
- Mode kustom: Membuat pola tertentu menggunakan komponen  

Solver ini dilengkapi antarmuka baris perintah dan antarmuka grafis yang memungkinkan pengguna memvisualisasikan proses penyelesaian secara langsung.

## Instalasi

Untuk menjalankan program ini, Anda memerlukan:

- Java Development Kit (JDK) 17 atau lebih tinggi
- JavaFX SDK 23.0.2 atau lebih tinggi

Langkah instalasi:
1. Unduh dan pasang JDK 
2. Unduh dan pasang JavaFX 
3. Kloning repositori ini:
   ```bash
   git clone https://github.com/danenftyessir/Tucil1_13523136.git
   cd Tucil1_13523136
   ```

## Kompilasi

Untuk mengkompilasi program, jalankan perintah berikut pada terminal:

```bash
javac --module-path "C:/Java/javafx-sdk-23.0.2/lib" --add-modules javafx.controls,javafx.swing,javafx.graphics -d bin src/*.java
```

**Catatan**: Sesuaikan path JavaFX SDK dengan lokasi instalasi di komputer Anda.

## Run Program

Jalankan program dengan perintah:

```bash
java --module-path "C:/Java/javafx-sdk-23.0.2/lib" --add-modules javafx.controls,javafx.swing,javafx.graphics -cp bin src.Main
```

Setelah program berjalan, Anda akan diminta memilih mode:
1. Terminal Mode - Antarmuka berbasis teks
2. GUI Mode - Antarmuka grafis yang interaktif

### Cara Penggunaan Mode Terminal:
1. Pilih opsi 1 saat diminta memilih mode
2. Masukkan path file test yang ingin diselesaikan
3. Program akan mencari solusi dan menampilkan hasilnya
4. Pilih apakah ingin menyimpan solusi ke file txt

### Cara Penggunaan Mode GUI:
1. Pilih opsi 2 saat diminta memilih mode
2. Klik tombol 'Load Puzzle' untuk memilih file test
3. Pilih mode penyelesaian (Default/Custom)
4. Sesuaikan pengaturan:
   - Solving Speed: kecepatan visualisasi
   - Cell Size: ukuran tampilan papan
   - Debug Mode: tampilkan log detail
   - Show Steps: tampilkan proses penyelesaian
5. Klik 'Start Solving' untuk memulai pencarian solusi
6. Gunakan 'Save as Image' untuk menyimpan solusi dalam format PNG

##  Fitur Unggulan

- Visualisasi proses penyelesaian secara langsung
- Simpan solusi dalam bentuk gambar atau berkas teks 
- Dukungan untuk konfigurasi teka-teki kustom
- Pelacakan metrik kinerja:
  - Waktu eksekusi
  - Jumlah iterasi
  - Status penyelesaian
- Mode debug untuk langkah penyelesaian detail
- Antarmuka grafis responsif dan intuitif
- Mendukung berbagai ukuran papan dan konfigurasi

## Struktur Repository

```
Tucil1_13523136/
├── bin/
│   └── src/
│   │   ├── Main.class
│   │   ├── Board.class
│   │   ├── Piece.class
│   │   ├── Solver.class
│   │   └── GUI.class
├── doc/
│   ├── Spesifikasi Tugas Kecil 1 Stima 2024/2025
│   ├── Tucil1_13523136.docx.pdf             
├── src/             
│   ├── Main.java      # Entry point program
│   ├── Board.java     # Implementasi papan permainan
│   ├── Piece.java     # Implementasi komponen puzzle
│   ├── Solver.java    # Algoritma brute force
│   └── GUI.java       # Antarmuka grafis
├── test/              # Berkas test
│   ├── hasil1.txt
│   ├── hasil2.txt 
│   ├── hasil3.txt
│   ├── outputimage1.png  
│   ├── outputimage2.png
│   ├── outputimage3.png
│   ├── test1.txt   
│   ├── test2.txt  
│   └── test3.txt       
└── README.md     
```

## Developer

**Danendra Shafi Athallah**  
Teknik Informatika - Institut Teknologi Bandung  
NIM: 13523136


---
💡 **Tips**: 
- Mulailah dengan berkas test yang sudah disediakan di folder test/
- Ikuti format input yang sesuai untuk membuat berkas test baru
- Gunakan fitur debug untuk melihat proses pencarian solusi
- Manfaatkan GUI untuk pengalaman penyelesaian yang lebih interaktif
