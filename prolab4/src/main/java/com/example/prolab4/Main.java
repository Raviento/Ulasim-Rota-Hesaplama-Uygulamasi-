package com.example.prolab4;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    private CityData cityData;

    @Override
    public void start(Stage primaryStage) {
        // JSON verisetini yükle
        loadCityData();

        primaryStage.setTitle("Ulaşım Rota Planlama Sistemi");

        // GridPane ile düzenli bir arayüz oluşturuyoruz
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        // Başlangıç konumu girişi
        Label lblBaslangic = new Label("Başlangıç Enlem:");
        TextField tfBaslangicEnlem = new TextField();
        tfBaslangicEnlem.setPromptText("Örn: 40.78259");

        Label lblBaslangicBoylam = new Label("Başlangıç Boylam:");
        TextField tfBaslangicBoylam = new TextField();
        tfBaslangicBoylam.setPromptText("Örn: 29.94628");

        // Hedef konumu girişi
        Label lblHedefEnlem = new Label("Hedef Enlem:");
        TextField tfHedefEnlem = new TextField();
        tfHedefEnlem.setPromptText("Örn: 40.76200");

        Label lblHedefBoylam = new Label("Hedef Boylam:");
        TextField tfHedefBoylam = new TextField();
        tfHedefBoylam.setPromptText("Örn: 29.96550");

        // Yolcu tipi seçimi
        Label lblYolcuTipi = new Label("Yolcu Tipi:");
        ComboBox<String> cbYolcuTipi = new ComboBox<>();
        cbYolcuTipi.getItems().addAll("Genel", "Öğrenci", "Yaşlı");
        cbYolcuTipi.setValue("Genel");

        // Hesapla butonu
        Button btnHesapla = new Button("Rota Hesapla");

        // Sonuçların gösterileceği alan
        TextArea taSonuc = new TextArea();
        taSonuc.setEditable(false);
        taSonuc.setWrapText(true);
        taSonuc.setPrefHeight(300);

        // GridPane'e elemanları ekleyelim
        grid.add(lblBaslangic, 0, 0);
        grid.add(tfBaslangicEnlem, 1, 0);
        grid.add(lblBaslangicBoylam, 2, 0);
        grid.add(tfBaslangicBoylam, 3, 0);

        grid.add(lblHedefEnlem, 0, 1);
        grid.add(tfHedefEnlem, 1, 1);
        grid.add(lblHedefBoylam, 2, 1);
        grid.add(tfHedefBoylam, 3, 1);

        grid.add(lblYolcuTipi, 0, 2);
        grid.add(cbYolcuTipi, 1, 2);

        grid.add(btnHesapla, 0, 3, 4, 1);
        grid.add(taSonuc, 0, 4, 4, 1);

        // Butona tıklanınca çalışacak fonksiyon
        btnHesapla.setOnAction(e -> {
            try {
                // Kullanıcıdan girişleri alıyoruz
                double basEn = Double.parseDouble(tfBaslangicEnlem.getText());
                double basBoy = Double.parseDouble(tfBaslangicBoylam.getText());
                double hedefEn = Double.parseDouble(tfHedefEnlem.getText());
                double hedefBoy = Double.parseDouble(tfHedefBoylam.getText());

                Konum baslangic = new Konum(basEn, basBoy);
                Konum hedef = new Konum(hedefEn, hedefBoy);

                // JSON'dan okunan durak listesini alıyoruz
                List<Durak> duraklar = cityData.getDuraklar();

                // Rota hesaplaması: Basitçe kullanıcı konumuna en yakın durağı buluyoruz.
                Durak enYakinDurak = null;
                double minMesafe = Double.MAX_VALUE;
                for (Durak d : duraklar) {
                    double mesafe = baslangic.mesafeHesapla(d.getKonum());
                    if (mesafe < minMesafe) {
                        minMesafe = mesafe;
                        enYakinDurak = d;
                    }
                }

                StringBuilder sb = new StringBuilder();
                if (enYakinDurak != null) {
                    sb.append("📍 Kullanıcı Konumuna En Yakın Durak: ")
                            .append(enYakinDurak.getName())
                            .append(" (").append(String.format("%.2f", minMesafe * 1000)).append(" m)")
                            .append(" → 🚶 Yürüme = 0 TL\n\n");
                } else {
                    sb.append("En yakın durak bulunamadı.\n");
                }

                // Örnek rota detayları (burada RotaHesaplayici'nin çıktısı yerine sabit örnek değerler kullanılmıştır)
                sb.append("🚏 Rota Detayları:\n");
                if(duraklar.size() >= 2) {
                    sb.append("1⃣ ").append(duraklar.get(0).getName())
                            .append(" → ").append(duraklar.get(1).getName())
                            .append(" (🚌 Otobüs)\n   ⏳ Süre: 10 dk\n   💰 Ücret: 3 TL\n\n");
                }
                sb.append("📊 Toplam:\n");
                sb.append("   Ücret: 6 TL\n   Süre: 22 dk\n   Mesafe: 5 km\n\n");
                sb.append("🛤 Alternatif Rotalar:\n");
                sb.append("🔹 🚖 Sadece Taksi\n");
                sb.append("🔹 🚍 Sadece Otobüs\n");
                sb.append("🔹 🚋 Tramvay Öncelikli\n");
                sb.append("🔹 🛑 En Az Aktarmalı Rota\n");

                taSonuc.setText(sb.toString());
            } catch (Exception ex) {
                taSonuc.setText("Hata: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(grid, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // JSON veriseti yüklemesi: veriseti.json dosyasının src/main/resources altında olduğundan emin olun.
    private void loadCityData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/veriseti.json");
            if (is == null) {
                System.err.println("veriseti.json dosyası bulunamadı!");
                return;
            }
            cityData = mapper.readValue(is, CityData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
