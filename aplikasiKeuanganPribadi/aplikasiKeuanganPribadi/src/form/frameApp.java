/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package form;

import crud.crud;
import model.transaksi; // Import Model
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton; 
import javax.swing.JTextField;

/**
 * CLASS UTAMA (View/GUI)
 * Mengatur tampilan, interaksi pengguna, dan memanggil logika CRUD.
 */
public class frameApp extends javax.swing.JFrame {

    // --- Variabel Global ---
    crud mycode;              // Objek logika
    DefaultTableModel model;  // Model tabel GUI
    CardLayout cardLayout;    // Pengatur halaman (Card)
    int idBaris = 0;          // 0 = Mode Tambah, >0 = Mode Edit (menyimpan ID)
    
    // Warna Tema
    Color placeholderColor = new Color(153,153,153);
    Color activeColor = new Color(0, 0, 0);
    
    // Tombol manual (karena tidak ada di design XML awal)
    JButton btnExportPdf;
    JButton btnImportPdf;

    /**
     * Constructor Utama FrameApp
     */
    public frameApp() {
        initComponents(); // Inisialisasi komponen dari NetBeans GUI Builder
        initCustomLogic(); // Panggil logika tambahan kita
    }

    /**
     * Method untuk inisialisasi logika custom (Database, Event Listener, dll)
     */
    private void initCustomLogic() {
        // 1. Setup Logika & Database
        mycode = new crud();
        // Cek jika koneksi gagal, tutup aplikasi
        if (mycode == null) System.exit(0);
        
        // 2. Setup Layout & Tabel
        cardLayout = (CardLayout)(mainContent.getLayout());
        setupTabel();
        mycode.tampilData(model); // Load data awal
        
        // 3. Setup UI (Placeholder teks)
        addPlaceholder(txtCari, "Cari data...");
        addPlaceholder(txtKeterangan, "Keterangan...");
        addPlaceholder(txtJumlah, "Jumlah (Rp)...");
        cardLayout.show(mainContent, "cardTabel"); // Tampilkan halaman tabel

        // 4. Tambah Tombol PDF secara Manual (Coding) ke Panel Bawah
        btnExportPdf = new JButton("Export PDF");
        btnExportPdf.setBackground(new Color(255, 193, 7)); // Warna Kuning
        btnExportPdf.setForeground(Color.BLACK);
        
        btnImportPdf = new JButton("Import PDF");
        btnImportPdf.setBackground(new Color(23, 162, 184)); // Warna Biru
        btnImportPdf.setForeground(Color.WHITE);
        
        panelBottom.add(btnExportPdf);
        panelBottom.add(btnImportPdf);
        panelBottom.revalidate(); // Refresh panel agar tombol muncul

        // --- BAGIAN EVENT LISTENER (INTERAKSI TOMBOL) ---

        // A. LOGIKA EXPORT PDF
        btnExportPdf.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan sebagai PDF");
            chooser.setSelectedFile(new File("laporan_keuangan.pdf"));
            
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                // Pastikan ekstensi file adalah .pdf
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getParentFile(), file.getName() + ".pdf");
                }
                // Panggil fungsi export di crud.java
                mycode.exportKePDF(model, file);
            }
        });

        // B. LOGIKA IMPORT PDF
        btnImportPdf.addActionListener(e -> {
            // Peringatan karena import PDF itu tricky
            JOptionPane.showMessageDialog(this, 
                "Pastikan format PDF sesuai dengan hasil Export aplikasi ini.", 
                "Info Import", JOptionPane.WARNING_MESSAGE);
                
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih File PDF");
            chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
            
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                mycode.importDariPDF(file); // Panggil fungsi import
                mycode.tampilData(model); // Refresh tabel agar data baru muncul
            }
        });

        // C. NAVIGASI SIDEBAR
        btnMenuDashboard.addActionListener(e -> {
            cardLayout.show(mainContent, "cardTabel");
            resetBtnStyle();
            btnMenuDashboard.setBackground(new Color(68,170,119)); // Highlight tombol aktif
        });

        btnMenuTransaksi.addActionListener(e -> {
            resetForm(); // Bersihkan form sebelum pindah
            cardLayout.show(mainContent, "cardForm");
            resetBtnStyle();
            btnMenuTransaksi.setBackground(new Color(68,170,119)); // Highlight tombol aktif
        });

        // D. TOMBOL TAMBAH & BATAL
        btnTambah.addActionListener(e -> {
            resetForm();
            cardLayout.show(mainContent, "cardForm");
        });

        btnBatal.addActionListener(e -> {
            resetForm();
            cardLayout.show(mainContent, "cardTabel");
        });

        // E. TOMBOL SIMPAN (CREATE / UPDATE)
        btnSimpan.addActionListener(e -> {
            if (!validasiInput()) return; // Cek validasi dulu
            
            // Bungkus data form ke objek Transaksi (OOP)
            transaksi t = new transaksi();
            t.setKeterangan(txtKeterangan.getText());
            t.setJumlah(Double.parseDouble(txtJumlah.getText()));
            t.setKategori(cmbKategori.getSelectedItem().toString());
            
            boolean sukses;
            // Cek state: Tambah Baru atau Update?
            if (idBaris == 0) {
                // ID 0 = Data Baru
                sukses = mycode.simpanData(t);
                if(sukses) JOptionPane.showMessageDialog(this, "Berhasil Disimpan!");
            } else {
                // ID > 0 = Update Data Lama
                t.setId(idBaris); // Set ID yang mau diedit
                sukses = mycode.ubahData(t);
                if(sukses) JOptionPane.showMessageDialog(this, "Berhasil Diubah!");
            }
            
            // Jika sukses, kembali ke tabel
            if(sukses){
                mycode.tampilData(model);
                resetForm();
                cardLayout.show(mainContent, "cardTabel");
            }
        });

        // F. TOMBOL EDIT (AMBIL DATA)
        btnEdit.addActionListener(e -> {
            int baris = tblData.getSelectedRow();
            if (baris == -1) {
                JOptionPane.showMessageDialog(this, "Pilih baris dulu!");
                return;
            }
            
            // Ambil data dari tabel
            idBaris = (int) model.getValueAt(baris, 0); // Simpan ID
            String ket = model.getValueAt(baris, 2).toString();
            String kat = model.getValueAt(baris, 3).toString();
            String jml = model.getValueAt(baris, 4).toString();

            // Masukkan ke form
            lblFormTitle.setText("EDIT DATA TRANSAKSI");
            removePlaceholder(txtKeterangan); txtKeterangan.setText(ket);
            removePlaceholder(txtJumlah); txtJumlah.setText(jml);
            cmbKategori.setSelectedItem(kat);

            // Pindah halaman
            cardLayout.show(mainContent, "cardForm");
        });

        // G. TOMBOL HAPUS
        btnHapus.addActionListener(e -> {
            int baris = tblData.getSelectedRow();
            if (baris == -1) {
                JOptionPane.showMessageDialog(this, "Pilih baris dulu!");
                return;
            }
            
            if (JOptionPane.showConfirmDialog(this, "Yakin hapus?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                int id = (int) model.getValueAt(baris, 0);
                if (mycode.hapusData(id)) {
                    mycode.tampilData(model); // Refresh tabel
                }
            }
        });

        // H. INPUT LISTENERS (Realtime Search & Validasi Angka)
        txtCari.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                String key = txtCari.getText().equals("Cari data...") ? "" : txtCari.getText();
                mycode.cariData(model, key);
            }
        });

        txtJumlah.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                // Hanya boleh angka, titik, dan backspace
                if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) evt.consume();
            }
        });
    }

    /**
     * Helper: Mengatur Model Tabel
     */
    private void setupTabel() {
        String[] col = {"ID", "Tanggal", "Keterangan", "Kategori", "Jumlah"};
        model = new DefaultTableModel(col, 0) {
            public boolean isCellEditable(int row, int column) { return false; } // Tabel tidak bisa diedit langsung
        };
        tblData.setModel(model);
        // Sembunyikan kolom ID (Index 0)
        tblData.getColumnModel().getColumn(0).setMinWidth(0);
        tblData.getColumnModel().getColumn(0).setMaxWidth(0);
        tblData.getColumnModel().getColumn(0).setWidth(0);
    }

    /**
     * Helper: Reset Form Input ke kondisi awal
     */
    private void resetForm() {
        idBaris = 0;
        lblFormTitle.setText("INPUT DATA TRANSAKSI");
        cmbKategori.setSelectedIndex(0);
        txtKeterangan.setText(""); addPlaceholder(txtKeterangan, "Keterangan...");
        txtJumlah.setText(""); addPlaceholder(txtJumlah, "Jumlah (Rp)...");
    }

    /**
     * Helper: Reset warna tombol sidebar
     */
    private void resetBtnStyle() {
        btnMenuDashboard.setBackground(new Color(26,45,61));
        btnMenuTransaksi.setBackground(new Color(26,45,61));
    }

    /**
     * Helper: Validasi Input Form (Tidak boleh kosong & harus angka)
     */
    private boolean validasiInput() {
        if (txtKeterangan.getText().isEmpty() || txtKeterangan.getText().equals("Keterangan...")) {
            JOptionPane.showMessageDialog(this, "Isi Keterangan!"); return false;
        }
        try {
            Double.parseDouble(txtJumlah.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus angka!"); return false;
        }
        return true;
    }

    /**
     * Helper: Menambah Placeholder (Teks abu-abu)
     */
    private void addPlaceholder(JTextField txt, String ph) {
        if(txt.getText().isEmpty()) { txt.setText(ph); txt.setForeground(placeholderColor); }
        txt.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(ph)) { txt.setText(""); txt.setForeground(activeColor); }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setText(ph); txt.setForeground(placeholderColor); }
            }
        });
    }

    /**
     * Helper: Menghapus Placeholder saat mau diisi program (Edit)
     */
    private void removePlaceholder(JTextField txt) {
        if (txt.getText().equals("Keterangan...") || txt.getText().equals("Jumlah (Rp)...")) txt.setText("");
        txt.setForeground(activeColor);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sidebar = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        btnMenuDashboard = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10));
        btnMenuTransaksi = new javax.swing.JButton();
        mainContent = new javax.swing.JPanel();
        panelTabel = new javax.swing.JPanel();
        panelTop = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        btnTambah = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();
        panelBottom = new javax.swing.JPanel();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        panelForm = new javax.swing.JPanel();
        formContainer = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        txtKeterangan = new javax.swing.JTextField();
        txtJumlah = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox();
        btnSimpan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dashboard Manajemen Keuangan");
        setBackground(new java.awt.Color(18, 25, 31));

        sidebar.setBackground(new java.awt.Color(26, 45, 61));
        sidebar.setPreferredSize(new java.awt.Dimension(220, 600));
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("FINANCE APP");
        lblTitle.setMaximumSize(new java.awt.Dimension(32767, 80));
        lblTitle.setPreferredSize(new java.awt.Dimension(220, 80));
        sidebar.add(lblTitle);

        btnMenuDashboard.setBackground(new java.awt.Color(68, 170, 119));
        btnMenuDashboard.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnMenuDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuDashboard.setText("Dashboard");
        btnMenuDashboard.setMaximumSize(new java.awt.Dimension(32767, 50));
        btnMenuDashboard.setBorderPainted(false);
        btnMenuDashboard.setFocusPainted(false);
        sidebar.add(btnMenuDashboard);
        sidebar.add(filler1);

        btnMenuTransaksi.setBackground(new java.awt.Color(26, 45, 61));
        btnMenuTransaksi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnMenuTransaksi.setForeground(new java.awt.Color(204, 204, 204));
        btnMenuTransaksi.setText("Transaksi");
        btnMenuTransaksi.setMaximumSize(new java.awt.Dimension(32767, 50));
        btnMenuTransaksi.setBorderPainted(false);
        btnMenuTransaksi.setFocusPainted(false);
        sidebar.add(btnMenuTransaksi);

        getContentPane().add(sidebar, java.awt.BorderLayout.WEST);

        mainContent.setBackground(new java.awt.Color(18, 25, 31));
        mainContent.setLayout(new java.awt.CardLayout());

        panelTabel.setBackground(new java.awt.Color(18, 25, 31));
        panelTabel.setLayout(new java.awt.BorderLayout());

        panelTop.setBackground(new java.awt.Color(18, 25, 31));
        panelTop.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelTop.setLayout(new java.awt.BorderLayout());

        txtCari.setBackground(new java.awt.Color(26, 45, 61));
        txtCari.setForeground(new java.awt.Color(255, 255, 255));
        txtCari.setText("Cari data...");
        txtCari.setPreferredSize(new java.awt.Dimension(200, 35));
        txtCari.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(34, 53, 77)));
        panelTop.add(txtCari, java.awt.BorderLayout.WEST);

        btnTambah.setBackground(new java.awt.Color(68, 170, 119));
        btnTambah.setForeground(new java.awt.Color(255, 255, 255));
        btnTambah.setText("+ Tambah Data");
        btnTambah.setPreferredSize(new java.awt.Dimension(120, 35));
        btnTambah.setBorderPainted(false);
        btnTambah.setFocusPainted(false);
        panelTop.add(btnTambah, java.awt.BorderLayout.EAST);

        panelTabel.add(panelTop, java.awt.BorderLayout.NORTH);

        tblData.setBackground(new java.awt.Color(26, 45, 61));
        tblData.setForeground(new java.awt.Color(255, 255, 255));
        tblData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tanggal", "Keterangan", "Kategori", "Jumlah"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblData.setGridColor(new java.awt.Color(34, 53, 77));
        tblData.setRowHeight(30);
        tblData.setSelectionBackground(new java.awt.Color(68, 170, 119));
        jScrollPane1.setViewportView(tblData);

        panelTabel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panelBottom.setBackground(new java.awt.Color(18, 25, 31));
        panelBottom.setPreferredSize(new java.awt.Dimension(0, 60));

        btnEdit.setText("Edit Data");
        btnEdit.setBackground(new java.awt.Color(68, 136, 170));
        btnEdit.setForeground(new java.awt.Color(255, 255, 255));
        btnEdit.setPreferredSize(new java.awt.Dimension(100, 35));
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        panelBottom.add(btnEdit);

        btnHapus.setText("Hapus Data");
        btnHapus.setBackground(new java.awt.Color(204, 68, 68));
        btnHapus.setForeground(new java.awt.Color(255, 255, 255));
        btnHapus.setPreferredSize(new java.awt.Dimension(100, 35));
        btnHapus.setBorderPainted(false);
        btnHapus.setFocusPainted(false);
        panelBottom.add(btnHapus);

        panelTabel.add(panelBottom, java.awt.BorderLayout.SOUTH);

        mainContent.add(panelTabel, "cardTabel");

        panelForm.setBackground(new java.awt.Color(18, 25, 31));
        panelForm.setLayout(new java.awt.GridBagLayout());

        formContainer.setBackground(new java.awt.Color(26, 45, 61));
        formContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(34, 53, 77)));
        formContainer.setPreferredSize(new java.awt.Dimension(400, 400));
        formContainer.setLayout(new java.awt.GridBagLayout());

        lblFormTitle.setText("INPUT DATA TRANSAKSI");
        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(255, 255, 255));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 20, 0);
        formContainer.add(lblFormTitle, gridBagConstraints);

        txtKeterangan.setText("Keterangan...");
        txtKeterangan.setPreferredSize(new java.awt.Dimension(300, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        formContainer.add(txtKeterangan, gridBagConstraints);

        txtJumlah.setText("Jumlah (Rp)...");
        txtJumlah.setPreferredSize(new java.awt.Dimension(300, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        formContainer.add(txtJumlah, gridBagConstraints);

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pemasukan", "Pengeluaran" }));
        cmbKategori.setPreferredSize(new java.awt.Dimension(300, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        formContainer.add(cmbKategori, gridBagConstraints);

        btnSimpan.setText("Simpan");
        btnSimpan.setBackground(new java.awt.Color(68, 170, 119));
        btnSimpan.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpan.setPreferredSize(new java.awt.Dimension(140, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 10);
        formContainer.add(btnSimpan, gridBagConstraints);

        btnBatal.setText("Batal");
        btnBatal.setBackground(new java.awt.Color(68, 85, 102));
        btnBatal.setForeground(new java.awt.Color(255, 255, 255));
        btnBatal.setPreferredSize(new java.awt.Dimension(140, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        formContainer.add(btnBatal, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelForm.add(formContainer, gridBagConstraints);

        mainContent.add(panelForm, "cardForm");

        getContentPane().add(mainContent, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frameApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frameApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnMenuDashboard;
    private javax.swing.JButton btnMenuTransaksi;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox cmbKategori;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel formContainer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel mainContent;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelForm;
    private javax.swing.JPanel panelTabel;
    private javax.swing.JPanel panelTop;
    private javax.swing.JPanel sidebar;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtKeterangan;
    // End of variables declaration//GEN-END:variables
}