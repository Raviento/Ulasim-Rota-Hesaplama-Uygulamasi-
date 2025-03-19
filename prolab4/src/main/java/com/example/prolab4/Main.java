package com.example.prolab4;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
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

        // SplitPane kullanarak sol tarafta kullanıcı arayüzü, sağ tarafta grafik görünümü yer alsın
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        Pane leftPane = createRotaHesaplamaPane(primaryStage);
        Pane rightPane = createGraphPane(primaryStage);

        splitPane.getItems().addAll(leftPane, rightPane);

        Scene scene = new Scene(splitPane, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Sol tarafta kullanıcı arayüzü ve rota hesaplama sonuçlarının gösterildiği pane
    private Pane createRotaHesaplamaPane(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        Label lblBaslangic = new Label("Başlangıç Enlem:");
        TextField tfBaslangicEnlem = new TextField();
        tfBaslangicEnlem.setPromptText("Örn: 40.78259");

        Label lblBaslangicBoylam = new Label("Başlangıç Boylam:");
        TextField tfBaslangicBoylam = new TextField();
        tfBaslangicBoylam.setPromptText("Örn: 29.94628");

        Label lblHedefEnlem = new Label("Hedef Enlem:");
        TextField tfHedefEnlem = new TextField();
        tfHedefEnlem.setPromptText("Örn: 40.76200");

        Label lblHedefBoylam = new Label("Hedef Boylam:");
        TextField tfHedefBoylam = new TextField();
        tfHedefBoylam.setPromptText("Örn: 29.96550");

        Label lblYolcuTipi = new Label("Yolcu Tipi:");
        ComboBox<String> cbYolcuTipi = new ComboBox<>();
        cbYolcuTipi.getItems().addAll("Genel", "Öğrenci", "Yaşlı");
        cbYolcuTipi.setValue("Genel");

        Label lblOdemeYontemi = new Label("Ödeme Yöntemi:");
        ComboBox<String> cbOdemeYontemi = new ComboBox<>();
        cbOdemeYontemi.getItems().addAll("Nakit", "Kredi Kartı", "KentKart");
        cbOdemeYontemi.setValue("Nakit");

        Button btnHesapla = new Button("Rota Hesapla");

        TextArea taSonuc = new TextArea();
        taSonuc.setEditable(false);
        taSonuc.setWrapText(true);
        taSonuc.setPrefHeight(300);

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

        btnHesapla.setOnAction(e -> {
            try {
                double basEn = Double.parseDouble(tfBaslangicEnlem.getText());
                double basBoy = Double.parseDouble(tfBaslangicBoylam.getText());
                double hedefEn = Double.parseDouble(tfHedefEnlem.getText());
                double hedefBoy = Double.parseDouble(tfHedefBoylam.getText());

                Konum baslangic = new Konum(basEn, basBoy);
                Konum hedef = new Konum(hedefEn, hedefBoy);

                System.out.println("Kullanıcı başlangıç konumu: Enlem = " + basEn + ", Boylam = " + basBoy);
                System.out.println("Kullanıcı hedef konumu: Enlem = " + hedefEn + ", Boylam = " + hedefBoy);

                List<Durak> duraklar = cityData.getDuraklar();
                Durak startDurak = findNearestDurak(baslangic, duraklar);
                Durak endDurak = findNearestDurak(hedef, duraklar);

                System.out.println("Başlangıç için en yakın durak: " + startDurak.getName());
                System.out.println("Hedef için en yakın durak: " + endDurak.getName());

                StringBuilder sb = new StringBuilder();
                double baseFare = 0;
                double startToDurakMesafe = baslangic.mesafeHesapla(startDurak.getKonum());
                double taxiFareStart = 0;
                if (startToDurakMesafe > 3) {
                    sb.append("Kullanıcı konumundan en yakın durağa olan mesafe ")
                            .append(String.format("%.2f", startToDurakMesafe))
                            .append(" km olduğundan, yolculuk başlangıcında taksi kullanılması gerekmektedir.\n");
                    Taksi taxi = cityData.getTaxi();
                    taxiFareStart = taxi.hesaplaUcret(startToDurakMesafe);
                    sb.append("Taksi ile ulaşım ücreti: ").append(String.format("%.2f", taxiFareStart)).append(" TL\n\n");
                    baseFare = taxiFareStart;
                } else {
                    sb.append("Başlangıç konumundan en yakın durak (").append(startDurak.getName())
                            .append(") ").append(String.format("%.2f", startToDurakMesafe * 1000)).append(" m uzaklıkta → Yürüme = 0 TL\n\n");
                    baseFare = 6;
                }

                List<Durak> route = calculateRoute(startDurak, endDurak, duraklar);
                if (route == null || route.isEmpty()) {
                    sb.append("Uygun rota bulunamadı.\n");
                } else {
                    sb.append("🚏 Rota Detayları:\n");
                    double totalSure = 0;
                    double totalUcret = 0;
                    for (int i = 0; i < route.size() - 1; i++) {
                        Durak current = route.get(i);
                        Durak next = route.get(i + 1);
                        Pair<Double, Double> edgeInfo = getEdgeInfo(current, next);
                        double sure = edgeInfo.getKey();
                        double ucret = edgeInfo.getValue();
                        totalSure += sure;
                        totalUcret += ucret;
                        sb.append((i + 1)).append("⃣ ").append(current.getName())
                                .append(" → ").append(next.getName());
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
                    sb.append("   Mesafe: ").append("5 km\n\n");
                }

                // Yolcu tipi ve indirim
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
                double finalFare = (taxiFareStart + (route != null ? getTotalFare(route, duraklar) : 6)) * (1 - indirimOrani);
                sb.append("Yolcu tipi: ").append(cbYolcuTipi.getValue()).append("\n");
                sb.append("Uygulanan indirim oranı: ").append(String.format("%.0f%%", indirimOrani * 100)).append("\n");
                sb.append("İndirim sonrası toplam ücret: ").append(String.format("%.2f", finalFare)).append(" TL\n\n");

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

        return grid;
    }

    // Sağ tarafta graf yapısını oluşturan pane: Duraklar ve bağlantılar çiziliyor.
    // Durakların üzerine tıklandığında, kopyalanabilir detay içeren bir pencere (Stage) açılır.
    // Ayrıca, fare scroll olayları ile zoom yapma imkanı sağlanır.
    private Pane createGraphPane(Stage primaryStage) {
        Pane graphPane = new Pane();
        graphPane.setPrefSize(600, 400);
        if (cityData == null || cityData.getDuraklar() == null || cityData.getDuraklar().isEmpty()) {
            Label lbl = new Label("Veri seti yüklenemedi veya durak bilgisi boş.");
            graphPane.getChildren().add(lbl);
            return graphPane;
        }

        List<Durak> duraklar = cityData.getDuraklar();
        // Enlem ve boylam sınırlarını hesaplayalım
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        for (Durak d : duraklar) {
            Konum k = d.getKonum();
            if (k.getEnlem() < minLat) minLat = k.getEnlem();
            if (k.getEnlem() > maxLat) maxLat = k.getEnlem();
            if (k.getBoylam() < minLon) minLon = k.getBoylam();
            if (k.getBoylam() > maxLon) maxLon = k.getBoylam();
        }
        double paneWidth = graphPane.getPrefWidth();
        double paneHeight = graphPane.getPrefHeight();
        double margin = 40;

        Map<String, Circle> circleMap = new HashMap<>();
        for (Durak d : duraklar) {
            Konum k = d.getKonum();
            double x = margin + ((k.getBoylam() - minLon) / (maxLon - minLon)) * (paneWidth - 2 * margin);
            double y = paneHeight - margin - ((k.getEnlem() - minLat) / (maxLat - minLat)) * (paneHeight - 2 * margin);

            Circle circle = new Circle(x, y, 8);
            if (d.getType().equalsIgnoreCase("bus"))
                circle.setFill(Color.BLUE);
            else if (d.getType().equalsIgnoreCase("tram"))
                circle.setFill(Color.GREEN);
            else
                circle.setFill(Color.GRAY);

            // Durak üzerine tıklandığında kopyalanabilir detay penceresi açılır
            circle.setOnMouseClicked((MouseEvent event) -> {
                Stage detailStage = new Stage();
                detailStage.initOwner(primaryStage);
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.setTitle("Durak Detayları - " + d.getName());
                TextArea detailText = new TextArea("Durak: " + d.getName() +
                        "\nEnlem: " + k.getEnlem() +
                        "\nBoylam: " + k.getBoylam());
                detailText.setWrapText(true);
                // Kullanıcı kopyalama yapabilsin, düzenleme kapalı
                detailText.setEditable(false);
                VBox vbox = new VBox(detailText);
                vbox.setPadding(new Insets(10));
                Scene detailScene = new Scene(vbox, 250, 150);
                detailStage.setScene(detailScene);
                detailStage.show();
            });

            graphPane.getChildren().add(circle);
            circleMap.put(d.getId(), circle);
        }
        // Duraklar arası bağlantıları çizelim
        for (Durak d : duraklar) {
            Circle fromCircle = circleMap.get(d.getId());
            if (d.getNextStops() != null) {
                for (NextStop ns : d.getNextStops()) {
                    Circle toCircle = circleMap.get(ns.getStopId());
                    if (toCircle != null) {
                        Line line = new Line(fromCircle.getCenterX(), fromCircle.getCenterY(),
                                toCircle.getCenterX(), toCircle.getCenterY());
                        line.setStroke(Color.BLACK);
                        graphPane.getChildren().add(line);
                    }
                }
            }
            if (d.getTransfer() != null) {
                Circle toCircle = circleMap.get(d.getTransfer().getTransferStopId());
                if (toCircle != null) {
                    Line line = new Line(fromCircle.getCenterX(), fromCircle.getCenterY(),
                            toCircle.getCenterX(), toCircle.getCenterY());
                    line.setStroke(Color.RED);
                    line.getStrokeDashArray().addAll(5.0, 5.0);
                    graphPane.getChildren().add(line);
                }
            }
        }
        // Zoom için scroll olaylarını ekleyelim
        graphPane.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            graphPane.setScaleX(graphPane.getScaleX() * zoomFactor);
            graphPane.setScaleY(graphPane.getScaleY() * zoomFactor);
            event.consume();
        });

        return graphPane;
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
        Map<String, Durak> durakMap = new HashMap<>();
        for (Durak d : duraklar) {
            durakMap.put(d.getId(), d);
        }
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
        List<Durak> route = new ArrayList<>();
        String cur = end.getId();
        if (!prev.containsKey(cur) && !cur.equals(start.getId())) {
            return null;
        }
        while (cur != null) {
            route.add(0, durakMap.get(cur));
            cur = prev.get(cur);
        }
        return route;
    }

    // İki durak arasındaki edge bilgisini döndürür: key = süre, value = ücret
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
        return new Pair<>(0.0, 0.0);
    }

    // Rota üzerindeki toplam ücretin hesaplanması
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
