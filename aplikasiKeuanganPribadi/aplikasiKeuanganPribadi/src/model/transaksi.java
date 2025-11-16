/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * CLASS MODEL (OOP - Encapsulation)
 * Class ini bertugas sebagai "cetakan" atau wadah untuk data Transaksi.
 * Data dari database atau form akan dibungkus menjadi objek Transaksi.
 */
public class transaksi {
    
    // --- Fields (Atribut Data) ---
    // Dibuat private agar tidak bisa diakses sembarangan (Encapsulation)
    private int id;
    private String keterangan;
    private double jumlah;
    private String kategori;
    // Tanggal tidak kita simpan di sini karena dihandle langsung oleh SQL (CURDATE)

    // --- Constructors ---
    
    // Constructor Kosong (diperlukan untuk inisialisasi awal tanpa data)
    public transaksi() {
    }

    // Constructor Lengkap (untuk mengisi data sekaligus saat objek dibuat)
    public transaksi(int id, String keterangan, double jumlah, String kategori) {
        this.id = id;
        this.keterangan = keterangan;
        this.jumlah = jumlah;
        this.kategori = kategori;
    }

    // --- Getters and Setters ---
    // Method untuk mengambil (Get) dan mengubah (Set) nilai private fields di atas.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public double getJumlah() {
        return jumlah;
    }

    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }
}
