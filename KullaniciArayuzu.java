import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class KullaniciArayuzu extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Seyahat Planlayıcı");

        // Ana layout: dikey kutu (VBox)
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Seyahat başlangıç tarihi ve zamanı
        Label lblTarih = new Label("Seyahat Başlangıç Tarihi:");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Label lblZaman = new Label("Seyahat Başlangıç Zamanı (HH:mm):");
        TextField tfZaman = new TextField("08:00");

        // Mevcut konum girişi (enlem, boylam)
        Label lblMevcutKonum = new Label("Mevcut Konum (Enlem, Boylam):");
        HBox mevcutKonumBox = new HBox(5);
        TextField tfMevcutEnlem = new TextField();
        tfMevcutEnlem.setPromptText("Enlem");
        TextField tfMevcutBoylam = new TextField();
        tfMevcutBoylam.setPromptText("Boylam");
        mevcutKonumBox.getChildren().addAll(tfMevcutEnlem, tfMevcutBoylam);

        // Hedef konum girişi (enlem, boylam)
        Label lblHedefKonum = new Label("Hedef Konum (Enlem, Boylam):");
        HBox hedefKonumBox = new HBox(5);
        TextField tfHedefEnlem = new TextField();
        tfHedefEnlem.setPromptText("Enlem");
        TextField tfHedefBoylam = new TextField();
        tfHedefBoylam.setPromptText("Boylam");
        hedefKonumBox.getChildren().addAll(tfHedefEnlem, tfHedefBoylam);

        // Kullanıcı profili seçimi (Genel, Öğrenci, Yaşlı)
        Label lblProfil = new Label("Kullanıcı Profili:");
        ComboBox<String> cbProfil = new ComboBox<>();
        cbProfil.getItems().addAll("Genel", "Öğrenci", "Yaşlı");
        cbProfil.getSelectionModel().selectFirst();

        // Ulaşım modları arasında seçim (Örnek: Otobüs, Tramvay, Taksi, Kombinasyon)
        Label lblUlasim = new Label("Ulaşım Modu:");
        ComboBox<String> cbUlasim = new ComboBox<>();
        cbUlasim.getItems().addAll("Otobüs", "Tramvay", "Taksi", "Kombinasyon");
        cbUlasim.getSelectionModel().selectFirst();

        // Rota hesaplama için buton
        Button btnHesapla = new Button("Rota Hesapla");

        // Rota detaylarını göstermek için çıktı alanı (TextArea)
        TextArea taCikti = new TextArea();
        taCikti.setEditable(false);
        taCikti.setPrefHeight(200);

        // Buton tıklandığında verileri al, nesneleri oluştur ve örnek çıktı üret
        btnHesapla.setOnAction(e -> {
            try {
                // Tarih ve zaman bilgisi
                LocalDate tarih = datePicker.getValue();
                String zamanStr = tfZaman.getText();
                LocalTime zaman = LocalTime.parse(zamanStr, DateTimeFormatter.ofPattern("HH:mm"));

                // Konum bilgileri
                double mevcutEnlem = Double.parseDouble(tfMevcutEnlem.getText());
                double mevcutBoylam = Double.parseDouble(tfMevcutBoylam.getText());
                double hedefEnlem = Double.parseDouble(tfHedefEnlem.getText());
                double hedefBoylam = Double.parseDouble(tfHedefBoylam.getText());

                // Kullanıcı profili seçimi
                String profil = cbProfil.getSelectionModel().getSelectedItem();
                // Ulaşım modu seçimi
                String ulasimModu = cbUlasim.getSelectionModel().getSelectedItem();

                // Nesneleri oluştur (bu sınıflar daha önce tanımlanan yapılar)
                Konum mevcutKonum = new Konum(mevcutEnlem, mevcutBoylam);
                Konum hedefKonum = new Konum(hedefEnlem, hedefBoylam);

                Yolcu yolcu;
                if(profil.equals("Öğrenci")) {
                    yolcu = new Ogrenci("Kullanıcı");
                } else if(profil.equals("Yaşlı")) {
                    yolcu = new Yasli("Kullanıcı");
                } else {
                    yolcu = new Genel("Kullanıcı");
                }

                // Örnek rota hesaplama: RotaHesaplayici kullanılarak hesaplama yapılabilir.
                // Burada basit bir çıktı üretiyoruz. Gerçek hesaplamalarda RotaHesaplayici'nin
                // rotaHesapla metodu kullanılabilir.
                StringBuilder sb = new StringBuilder();
                sb.append("Seyahat Tarihi: ").append(tarih).append(" - ").append(zaman).append("\n");
                sb.append("Mevcut Konum: ").append(mevcutEnlem).append(", ").append(mevcutBoylam).append("\n");
                sb.append("Hedef Konum: ").append(hedefEnlem).append(", ").append(hedefBoylam).append("\n");
                sb.append("Kullanıcı Profili: ").append(profil).append("\n");
                sb.append("Seçilen Ulaşım Modu: ").append(ulasimModu).append("\n\n");
                sb.append("--- Rota Detayları ---\n");
                sb.append("Örnek rota hesaplaması burada gösterilecek...\n");
                
                // Burada, RotaHesaplayici.rotaHesapla(mevcutKonum, hedefKonum, durakListesi) metodu entegre edilerek
                // hesaplanan rota detayları bu string'e eklenebilir.
                
                taCikti.setText(sb.toString());
            } catch (NumberFormatException ex) {
                taCikti.setText("Lütfen geçerli enlem ve boylam değerleri giriniz.");
            } catch (Exception ex) {
                taCikti.setText("Bir hata oluştu: " + ex.getMessage());
            }
        });

        // Tüm bileşenleri ana layout'a ekleyelim
        root.getChildren().addAll(
            lblTarih, datePicker,
            lblZaman, tfZaman,
            lblMevcutKonum, mevcutKonumBox,
            lblHedefKonum, hedefKonumBox,
            lblProfil, cbProfil,
            lblUlasim, cbUlasim,
            btnHesapla,
            taCikti
        );

        Scene scene = new Scene(root, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Main metodu ile uygulama başlatılıyor
    public static void main(String[] args) {
        launch(args);
    }
}
