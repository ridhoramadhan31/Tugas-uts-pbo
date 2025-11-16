/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 * CLASS KONEKSI
 * --------------
 * Class ini bertugas menangani hubungan antara aplikasi Java dengan MySQL.
 * Menggunakan pola Singleton sederhana (menyimpan objek Connection).
 */

/**
 *
 * @author Ridho
 */

public class koneksi {
    
    // Konfigurasi Database
    String urlKoneksi = "jdbc:mysql://localhost:3306/db_keuangan"; // Lokasi DB
    String username = "root"; // Default user XAMPP
    String password = ""; // Default password XAMPP (kosong)
    
    Connection Koneksidb; // Variabel untuk menyimpan objek koneksi

    /**
     * Constructor: Dijalankan otomatis saat class dipanggil (new koneksi()).
     */
    public koneksi() {
        try {
            // 1. Load Driver MySQL (Menggunakan style lama/legacy sesuai request)
            Driver dbdriver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(dbdriver);
            
            // 2. Melakukan koneksi menggunakan DriverManager
            Koneksidb = DriverManager.getConnection(urlKoneksi, username, password);
            
            // Cetak di console jika berhasil (untuk debugging)
            System.out.println("Database Terkoneksi");
            
        } catch (Exception e) {
            // Tampilkan Popup Error jika koneksi gagal
            JOptionPane.showMessageDialog(null, "Koneksi Gagal: " + e.toString());
        }
    }

    /**
     * Method Getter untuk memberikan objek koneksi ke class lain yang membutuhkan.
     * @return Connection object
     */
    public Connection getKoneksi() {
        return Koneksidb;
    }
}