package com.example.prolab4;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.InputStream;
import java.util.*;

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

        // Ödeme yöntemi seçimi
        Label lblOdemeYontemi = new Label("Ödeme Yöntemi:");
        ComboBox<String> cbOdemeYontemi = new ComboBox<>();
        cbOdemeYontemi.getItems().addAll("Nakit", "Kredi Kartı", "KentKart");
        cbOdemeYontemi.setValue("Nakit");

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
        grid.add(lblOdemeYontemi, 2, 2);
        grid.add(cbOdemeYontemi, 3, 2);

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

                // Terminale debug çıktıları
                System.out.println("Kullanıcı başlangıç konumu: Enlem = " + basEn + ", Boylam = " + basBoy);
                System.out.println("Kullanıcı hedef konumu: Enlem = " + hedefEn + ", Boylam = " + hedefBoy);

                List<Durak> duraklar = cityData.getDuraklar();

                // Başlangıç ve hedef için en yakın durakları buluyoruz
                Durak startDurak = findNearestDurak(baslangic, duraklar);
                Durak endDurak = findNearestDurak(hedef, duraklar);

                System.out.println("Başlangıç için en yakın durak: " + startDurak.getName());
                System.out.println("Hedef için en yakın durak: " + endDurak.getName());

                StringBuilder sb = new StringBuilder();

                // Eğer başlangıç konumundan en yakın durağa olan mesafe 3 km'den fazla ise taksi kullanılması gerekecek
                double startToDurakMesafe = baslangic.mesafeHesapla(startDurak.getKonum());
                double taxiFareStart = 0;
                if (startToDurakMesafe > 3) {
                    sb.append("Kullanıcı konumundan en yakın durağa olan mesafe ")
                            .append(String.format("%.2f", startToDurakMesafe))
                            .append(" km olduğundan, yolculuk başlangıcında taksi kullanılması gerekmektedir.\n");
                    Taksi taxi = cityData.getTaxi();
                    taxiFareStart = taxi.hesaplaUcret(startToDurakMesafe);
                    sb.append("Taksi ile ulaşım ücreti: ").append(String.format("%.2f", taxiFareStart)).append(" TL\n\n");
                } else {
                    sb.append("Başlangıç konumundan en yakın durak (").append(startDurak.getName())
                            .append(") ").append(String.format("%.2f", startToDurakMesafe * 1000)).append(" m uzaklıkta → Yürüme = 0 TL\n\n");
                }

                // Graf yapısını kullanarak Dijkstra algoritması ile rota hesaplaması yapalım
                List<Durak> route = calculateRoute(startDurak, endDurak, duraklar);
                if (route == null || route.isEmpty()) {
                    sb.append("Uygun rota bulunamadı.\n");
                } else {
                    sb.append("🚏 Rota Detayları:\n");
                    double totalSure = 0;
                    double totalUcret = 0;
                    // Rota segmentlerini yazdıralım:
                    for (int i = 0; i < route.size() - 1; i++) {
                        Durak current = route.get(i);
                        Durak next = route.get(i + 1);
                        // İlgili edge bilgisini bulalım: nextStop veya transfer
                        Pair<Double, Double> edgeInfo = getEdgeInfo(current, next);
                        double sure = edgeInfo.getKey();  // süre
                        double ucret = edgeInfo.getValue(); // ücret
                        totalSure += sure;
                        totalUcret += ucret;
                        sb.append((i + 1)).append("⃣ ").append(current.getName())
                                .append(" → ").append(next.getName());
                        // Hangi ulaşım aracı (örneğin, current durak'taki type)
                        if (current.getType().equalsIgnoreCase("bus")) {
                            sb.append(" (🚌 Otobüs)");
                        } else if (current.getType().equalsIgnoreCase("tram")) {
                            sb.append(" (🚋 Tramvay)");
                        }
                        sb.append("\n   ⏳ Süre: ").append(sure).append(" dk")
                                .append("\n   💰 Ücret: ").append(ucret).append(" TL\n\n");
                    }
                    sb.append("📊 Toplam:\n");
                    sb.append("   Ücret: ").append(String.format("%.2f", totalUcret + taxiFareStart)).append(" TL\n");
                    sb.append("   Süre: ").append(String.format("%.0f", totalSure)).append(" dk\n");
                    // Toplam mesafe için, örneğin rota segmentleri üzerinden hesaplama yapılabilir (burada örnek olarak sabit değer)
                    sb.append("   Mesafe: ").append("5 km\n\n");
                }

                // Yolcu tipine göre indirim uygulanması
                Yolcu yolcu;
                switch (cbYolcuTipi.getValue()) {
                    case "Öğrenci":
                        yolcu = new Ogrenci("Test");
                        break;
                    case "Yaşlı":
                        yolcu = new Yasli("Test");
                        break;
                    default:
                        yolcu = new Genel("Test");
                        break;
                }
                double indirimOrani = yolcu.getIndirimOrani();
                // Final ücret: rota ücreti + taksi başlangıç ücreti, indirim uygulanır
                double finalFare = (taxiFareStart + (route != null ? getTotalFare(route, duraklar) : 6)) * (1 - indirimOrani);
                sb.append("Yolcu tipi: ").append(cbYolcuTipi.getValue()).append("\n");
                sb.append("Uygulanan indirim oranı: ").append(String.format("%.0f%%", indirimOrani * 100)).append("\n");
                sb.append("İndirim sonrası toplam ücret: ").append(String.format("%.2f", finalFare)).append(" TL\n\n");

                // Ödeme yöntemi simülasyonu
                String odemeYontemi = cbOdemeYontemi.getValue();
                Odeme odeme;
                if (odemeYontemi.equals("Nakit")) {
                    odeme = new Nakit();
                } else if (odemeYontemi.equals("Kredi Kartı")) {
                    odeme = new KrediKart("1234-5678-9012-3456");
                } else if (odemeYontemi.equals("KentKart")) {
                    odeme = new KentKart(100);
                } else {
                    odeme = new Nakit();
                }
                odeme.odemeYap(finalFare);
                sb.append("Ödeme yöntemi: ").append(odemeYontemi).append("\n");

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

    // Kullanıcının konumuna en yakın durağı bulur
    private Durak findNearestDurak(Konum pos, List<Durak> duraklar) {
        Durak nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Durak d : duraklar) {
            double distance = pos.mesafeHesapla(d.getKonum());
            if (distance < minDist) {
                minDist = distance;
                nearest = d;
            }
        }
        return nearest;
    }

    // Dijkstra algoritması ile rota hesaplaması: ağırlık olarak süre (sure) kullanılır.
    private List<Durak> calculateRoute(Durak start, Durak end, List<Durak> duraklar) {
        // Map: durak id'si -> Durak
        Map<String, Durak> durakMap = new HashMap<>();
        for (Durak d : duraklar) {
            durakMap.put(d.getId(), d);
        }

        // Dijkstra için başlangıç
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (Durak d : duraklar) {
            dist.put(d.getId(), Double.MAX_VALUE);
        }
        dist.put(start.getId(), 0.0);

        PriorityQueue<Pair<String, Double>> queue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getValue));
        queue.add(new Pair<>(start.getId(), 0.0));

        while (!queue.isEmpty()) {
            Pair<String, Double> current = queue.poll();
            String currentId = current.getKey();
            if (currentId.equals(end.getId())) break;
            Durak currentDurak = durakMap.get(currentId);
            // NextStops üzerinden kenarları gezelim
            if (currentDurak.getNextStops() != null) {
                for (NextStop ns : currentDurak.getNextStops()) {
                    String neighborId = ns.getStopId();
                    double alt = dist.get(currentId) + ns.getSure();
                    if (alt < dist.getOrDefault(neighborId, Double.MAX_VALUE)) {
                        dist.put(neighborId, alt);
                        prev.put(neighborId, currentId);
                        queue.add(new Pair<>(neighborId, alt));
                    }
                }
            }
            // Transfer kenarı
            if (currentDurak.getTransfer() != null) {
                Transfer t = currentDurak.getTransfer();
                String neighborId = t.getTransferStopId();
                double alt = dist.get(currentId) + t.getTransferSure();
                if (alt < dist.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    dist.put(neighborId, alt);
                    prev.put(neighborId, currentId);
                    queue.add(new Pair<>(neighborId, alt));
                }
            }
        }

        // Rota yeniden oluşturulması
        List<Durak> route = new ArrayList<>();
        String cur = end.getId();
        if (!prev.containsKey(cur) && !cur.equals(start.getId())) {
            return null; // rota bulunamadı
        }
        while (cur != null) {
            route.add(0, durakMap.get(cur));
            cur = prev.get(cur);
        }
        return route;
    }

    // İki durak arasındaki edge bilgisini döndürür: key = süre, value = ücret
    // İlk olarak NextStop kontrol edilir, yoksa transfer kontrolü yapılır.
    private Pair<Double, Double> getEdgeInfo(Durak from, Durak to) {
        if (from.getNextStops() != null) {
            for (NextStop ns : from.getNextStops()) {
                if (ns.getStopId().equals(to.getId())) {
                    return new Pair<>((double) ns.getSure(), ns.getUcret());
                }
            }
        }
        if (from.getTransfer() != null && from.getTransfer().getTransferStopId().equals(to.getId())) {
            return new Pair<>((double) from.getTransfer().getTransferSure(), from.getTransfer().getTransferUcret());
        }
        return new Pair<>(0.0, 0.0); // varsayılan
    }

    // Rota üzerindeki toplam ücretin (nextStop ücretleri) hesaplanması
    private double getTotalFare(List<Durak> route, List<Durak> duraklar) {
        double total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            Pair<Double, Double> info = getEdgeInfo(route.get(i), route.get(i + 1));
            total += info.getValue();
        }
        return total;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
