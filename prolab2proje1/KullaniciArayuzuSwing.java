import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KullaniciArayuzuSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KullaniciArayuzuSwing().createAndShowGUI());
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Seyahat Planlayıcı");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);

        // Ana panel oluşturuluyor
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Seyahat başlangıç tarihi (JSpinner kullanarak tarih seçimi)
        panel.add(new JLabel("Seyahat Başlangıç Tarihi:"));
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        panel.add(dateSpinner);

        // Seyahat başlangıç zamanı
        panel.add(new JLabel("Seyahat Başlangıç Zamanı (HH:mm):"));
        JTextField tfZaman = new JTextField("08:00");
        panel.add(tfZaman);

        // Mevcut konum girişi
        panel.add(new JLabel("Mevcut Konum (Enlem, Boylam):"));
        JPanel mevcutKonumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField tfMevcutEnlem = new JTextField(8);
        tfMevcutEnlem.setToolTipText("Enlem");
        JTextField tfMevcutBoylam = new JTextField(8);
        tfMevcutBoylam.setToolTipText("Boylam");
        mevcutKonumPanel.add(tfMevcutEnlem);
        mevcutKonumPanel.add(tfMevcutBoylam);
        panel.add(mevcutKonumPanel);

        // Hedef konum girişi
        panel.add(new JLabel("Hedef Konum (Enlem, Boylam):"));
        JPanel hedefKonumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField tfHedefEnlem = new JTextField(8);
        tfHedefEnlem.setToolTipText("Enlem");
        JTextField tfHedefBoylam = new JTextField(8);
        tfHedefBoylam.setToolTipText("Boylam");
        hedefKonumPanel.add(tfHedefEnlem);
        hedefKonumPanel.add(tfHedefBoylam);
        panel.add(hedefKonumPanel);

        // Kullanıcı profili seçimi
        panel.add(new JLabel("Kullanıcı Profili:"));
        String[] profiller = { "Genel", "Öğrenci", "Yaşlı" };
        JComboBox<String> cbProfil = new JComboBox<>(profiller);
        panel.add(cbProfil);

        // Ulaşım modu seçimi
        panel.add(new JLabel("Ulaşım Modu:"));
        String[] ulasimModlari = { "Otobüs", "Tramvay", "Taksi", "Kombinasyon" };
        JComboBox<String> cbUlasim = new JComboBox<>(ulasimModlari);
        panel.add(cbUlasim);

        // Rota hesaplama butonu
        JButton btnHesapla = new JButton("Rota Hesapla");
        panel.add(btnHesapla);

        // Çıktı alanı
        JTextArea taCikti = new JTextArea(10, 30);
        taCikti.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(taCikti);
        panel.add(scrollPane);

        // Buton tıklama işlemi
        btnHesapla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Tarih bilgisi (JSpinner'dan alınan Date nesnesi)
                    Date tarih = (Date) dateSpinner.getValue();
                    SimpleDateFormat tarihFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String tarihStr = tarihFormat.format(tarih);

                    // Zaman bilgisi
                    String zamanStr = tfZaman.getText();

                    // Konum bilgileri
                    double mevcutEnlem = Double.parseDouble(tfMevcutEnlem.getText());
                    double mevcutBoylam = Double.parseDouble(tfMevcutBoylam.getText());
                    double hedefEnlem = Double.parseDouble(tfHedefEnlem.getText());
                    double hedefBoylam = Double.parseDouble(tfHedefBoylam.getText());

                    // Kullanıcı profili ve ulaşım modu
                    String profil = (String) cbProfil.getSelectedItem();
                    String ulasim = (String) cbUlasim.getSelectedItem();

                    // Örnek çıktı üretimi
                    StringBuilder sb = new StringBuilder();
                    sb.append("Seyahat Tarihi: ").append(tarihStr).append(" - ").append(zamanStr).append("\n");
                    sb.append("Mevcut Konum: ").append(mevcutEnlem).append(", ").append(mevcutBoylam).append("\n");
                    sb.append("Hedef Konum: ").append(hedefEnlem).append(", ").append(hedefBoylam).append("\n");
                    sb.append("Kullanıcı Profili: ").append(profil).append("\n");
                    sb.append("Ulaşım Modu: ").append(ulasim).append("\n\n");
                    sb.append("--- Rota Detayları ---\n");
                    sb.append("Örnek rota hesaplaması burada gösterilecek...\n");

                    taCikti.setText(sb.toString());
                } catch (NumberFormatException ex) {
                    taCikti.setText("Lütfen geçerli enlem ve boylam değerleri giriniz.");
                } catch (Exception ex) {
                    taCikti.setText("Bir hata oluştu: " + ex.getMessage());
                }
            }
        });

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
