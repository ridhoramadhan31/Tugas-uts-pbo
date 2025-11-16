/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;
// Import iText PDF Library (Wajib ada library itextpdf-5.x.jar)
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

// Import Java IO & SQL
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.transaksi; // Menggunakan model OOP

/**
 *
 * @author Ridho 
 */

/**
 * CLASS CRUD (Controller)
 * Berisi semua logika bisnis: Simpan, Ubah, Hapus, Tampil, Cari, Export, Import.
 */
public class crud {
    
    Connection conn; // Menyimpan koneksi aktif

    /**
     * Constructor: Menyiapkan koneksi saat objek crud dibuat.
     */
    public crud() {
        koneksi myKoneksi = new koneksi(); 
        conn = myKoneksi.getKoneksi();
    }

    /**
     * READ: Mengambil data dari DB dan menampilkannya ke JTable.
     * @param model Model tabel dari FrameApp
     */
    public void tampilData(DefaultTableModel model) {
        model.setRowCount(0); // Bersihkan data lama di tabel GUI
        String sql = "SELECT id, tanggal, keterangan, kategori, jumlah FROM transaksi ORDER BY id DESC";
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql); // Siapkan query
            ResultSet rs = ps.executeQuery(); // Jalankan query
            
            // Loop selama ada data (baris per baris)
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getDate("tanggal"),
                    rs.getString("keterangan"),
                    rs.getString("kategori"),
                    rs.getDouble("jumlah")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Tampil: " + e.getMessage());
        }
    }

    /**
     * CREATE: Menyimpan data baru.
     * @param t Objek Transaksi yang berisi data dari form
     */
    public boolean simpanData(transaksi t) {
        // Tanggal diisi otomatis oleh MySQL dengan fungsi CURDATE()
        String sql = "INSERT INTO transaksi (keterangan, jumlah, kategori, tanggal) VALUES (?, ?, ?, CURDATE())";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            // Mengambil data dari objek Transaksi (Getter)
            ps.setString(1, t.getKeterangan());
            ps.setDouble(2, t.getJumlah());
            ps.setString(3, t.getKategori());
            ps.executeUpdate(); // Eksekusi simpan
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Simpan: " + e.getMessage());
            return false;
        }
    }

    /**
     * UPDATE: Mengubah data yang sudah ada.
     * @param t Objek Transaksi (harus memiliki ID)
     */
    public boolean ubahData(transaksi t) {
        String sql = "UPDATE transaksi SET keterangan=?, jumlah=?, kategori=? WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, t.getKeterangan());
            ps.setDouble(2, t.getJumlah());
            ps.setString(3, t.getKategori());
            ps.setInt(4, t.getId()); // ID sebagai kunci update
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Ubah: " + e.getMessage());
            return false;
        }
    }

    /**
     * DELETE: Menghapus data berdasarkan ID.
     */
    public boolean hapusData(int id) {
        String sql = "DELETE FROM transaksi WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Hapus: " + e.getMessage());
            return false;
        }
    }

    /**
     * SEARCH: Mencari data berdasarkan keyword (LIKE query).
     */
    public void cariData(DefaultTableModel model, String keyword) {
        model.setRowCount(0);
        String sql = "SELECT id, tanggal, keterangan, kategori, jumlah FROM transaksi " +
                     "WHERE keterangan LIKE ? OR kategori LIKE ? ORDER BY id DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            // Tambahkan wildcard % agar mencari teks yang mengandung keyword
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getDate("tanggal"),
                    rs.getString("keterangan"),
                    rs.getString("kategori"),
                    rs.getDouble("jumlah")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Cari: " + e.getMessage());
        }
    }

    // ========================================================================
    // FITUR PDF (EXPORT & IMPORT)
    // ========================================================================

    /**
     * EXPORT PDF: Membuat file PDF dari data JTable.
     */
    public void exportKePDF(DefaultTableModel model, File file) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Menambahkan Judul
            document.add(new Paragraph("Laporan Data Keuangan"));
            document.add(new Paragraph(" ")); // Baris kosong

            // Membuat Tabel PDF dengan 5 kolom
            PdfPTable table = new PdfPTable(5);
            // Header Tabel
            table.addCell("ID");
            table.addCell("Tanggal");
            table.addCell("Keterangan");
            table.addCell("Kategori");
            table.addCell("Jumlah");

            // Loop data dari JTable Model dan masukkan ke PDF
            for (int i = 0; i < model.getRowCount(); i++) {
                table.addCell(model.getValueAt(i, 0).toString());
                table.addCell(model.getValueAt(i, 1).toString());
                table.addCell(model.getValueAt(i, 2).toString());
                table.addCell(model.getValueAt(i, 3).toString());
                table.addCell(model.getValueAt(i, 4).toString());
            }

            document.add(table);
            document.close();
            JOptionPane.showMessageDialog(null, "Berhasil Export ke PDF!");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Export: " + e.getMessage());
        }
    }

    /**
     * IMPORT PDF: Membaca teks dari PDF dan mencoba memasukkannya ke Database.
     * Catatan: Ini menggunakan logika parsing sederhana (memisahkan spasi).
     */
    public void importDariPDF(File file) {
        try {
            PdfReader reader = new PdfReader(new FileInputStream(file));
            // Ekstrak teks dari halaman 1
            String text = PdfTextExtractor.getTextFromPage(reader, 1); 
            String[] lines = text.split("\n"); // Pisahkan per baris
            
            int successCount = 0;

            // Loop setiap baris teks di PDF
            for (String line : lines) {
                // Pecah baris berdasarkan spasi
                String[] parts = line.split(" "); 
                
                // Validasi minimal: Harus ada cukup kata untuk membentuk data
                if (parts.length >= 5) {
                    try {
                        // Ambil angka terakhir sebagai jumlah
                        double jumlah = Double.parseDouble(parts[parts.length-1]);
                        // Ambil kata sebelum jumlah sebagai kategori
                        String kategori = parts[parts.length-2];
                        
                        // Gabungkan kata-kata di tengah sebagai keterangan
                        StringBuilder ketBuilder = new StringBuilder();
                        for(int i=2; i < parts.length-2; i++) {
                            ketBuilder.append(parts[i]).append(" ");
                        }
                        String keterangan = ketBuilder.toString().trim();
                        
                        // Simpan ke database menggunakan method simpanData
                        transaksi t = new transaksi(0, keterangan, jumlah, kategori);
                        if(simpanData(t)) successCount++;
                        
                    } catch (NumberFormatException e) {
                        // Abaikan baris yang tidak sesuai format (misal Header)
                    }
                }
            }
            
            JOptionPane.showMessageDialog(null, "Selesai Import. Data masuk: " + successCount);
            reader.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Import: " + e.getMessage());
        }
    }
}