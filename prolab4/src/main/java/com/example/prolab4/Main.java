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

    // Inner helper class: Edge bilgisi (undirected graph için)
    private static class Edge {
        String neighborId;
        double sure;    // süre (dk)
        double ucret;   // ücret (TL)

        public Edge(String neighborId, double sure, double ucret) {
            this.neighborId = neighborId;
            this.sure = sure;
            this.ucret = ucret;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // JSON verisetini yükle
        loadCityData();

        primaryStage.setTitle("Ulaşım Rota Planlama Sistemi");

        // SplitPane: Sol tarafta kullanıcı arayüzü, sağ tarafta grafik görünümü
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        Pane leftPane = createRotaHesaplamaPane(primaryStage);
        Pane rightPane = createGraphPane(primaryStage);

        splitPane.getItems().addAll(leftPane, rightPane);

        Scene scene = new Scene(splitPane, 1000, 600);
        scene.getRoot().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Sol tarafta rota hesaplama arayüzü
    private Pane createRotaHesaplamaPane(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");

        Label lblBaslangic = new Label("Başlangıç Enlem:");
        lblBaslangic.setStyle("-fx-text-fill: white;");
        TextField tfBaslangicEnlem = new TextField();
        tfBaslangicEnlem.setPromptText("Örn: 40.78259");

        Label lblBaslangicBoylam = new Label("Başlangıç Boylam:");
        lblBaslangicBoylam.setStyle("-fx-text-fill: white;");
        TextField tfBaslangicBoylam = new TextField();
        tfBaslangicBoylam.setPromptText("Örn: 29.94628");

        Label lblHedefEnlem = new Label("Hedef Enlem:");
        lblHedefEnlem.setStyle("-fx-text-fill: white;");
        TextField tfHedefEnlem = new TextField();
        tfHedefEnlem.setPromptText("Örn: 40.76200");

        Label lblHedefBoylam = new Label("Hedef Boylam:");
        lblHedefBoylam.setStyle("-fx-text-fill: white;");
        TextField tfHedefBoylam = new TextField();
        tfHedefBoylam.setPromptText("Örn: 29.96550");

        Label lblYolcuTipi = new Label("Yolcu Tipi:");
        lblYolcuTipi.setStyle("-fx-text-fill: white;");
        ComboBox<String> cbYolcuTipi = new ComboBox<>();
        cbYolcuTipi.getItems().addAll("Genel", "Öğrenci", "Yaşlı");
        cbYolcuTipi.setValue("Genel");

        Label lblOdemeYontemi = new Label("Ödeme Yöntemi:");
        lblOdemeYontemi.setStyle("-fx-text-fill: white;");
        ComboBox<String> cbOdemeYontemi = new ComboBox<>();
        cbOdemeYontemi.getItems().addAll("Nakit", "Kredi Kartı", "KentKart");
        cbOdemeYontemi.setValue("Nakit");

        Button btnHesapla = new Button("Rota Hesapla");
        btnHesapla.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        TextArea taSonuc = new TextArea();
        taSonuc.setEditable(false);
        taSonuc.setWrapText(true);
        taSonuc.setPrefHeight(300);
        taSonuc.setStyle("-fx-control-inner-background: #3c3f41; -fx-text-fill: white;");

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
                double totalFare = 0;
                double taxiFareStart = 0;
                double startToDurakMesafe = baslangic.mesafeHesapla(startDurak.getKonum());
                if (startToDurakMesafe > 3) {
                    sb.append("Kullanıcı konumundan başlangıç durağına olan mesafe ")
                            .append(String.format("%.2f", startToDurakMesafe))
                            .append(" km olduğundan, başlangıçta taksi kullanılması gerekmektedir.\n");
                    Taksi taxi = cityData.getTaxi();
                    taxiFareStart = taxi.hesaplaUcret(startToDurakMesafe);
                    sb.append("Başlangıç için taksi ücreti: ").append(String.format("%.2f", taxiFareStart)).append(" TL\n\n");
                } else {
                    sb.append("Başlangıç konumundan en yakın durak (").append(startDurak.getName())
                            .append(") ").append(String.format("%.2f", startToDurakMesafe * 1000))
                            .append(" m uzaklıkta → Yürüme = 0 TL\n\n");
                }
                totalFare += taxiFareStart;

                // Rota hesaplaması: Undirected graf üzerinden Dijkstra algoritması kullanılarak
                List<Durak> route = calculateRouteUndirected(startDurak, endDurak, duraklar);
                if (route == null || route.isEmpty()) {
                    sb.append("Uygun rota bulunamadı.\n");
                } else {
                    sb.append("🚏 Rota Detayları:\n");
                    double totalSure = 0;
                    double routeFare = 0;
                    for (int i = 0; i < route.size() - 1; i++) {
                        Durak current = route.get(i);
                        Durak next = route.get(i + 1);
                        Pair<Double, Double> edgeInfo = getEdgeInfo(current, next);
                        double sure = edgeInfo.getKey();
                        double ucret = edgeInfo.getValue();
                        totalSure += sure;
                        routeFare += ucret;
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
                    sb.append("Rota üzerinden hesaplanan ücret: ").append(String.format("%.2f", routeFare)).append(" TL\n");
                    sb.append("Rota üzerinden hesaplanan süre: ").append(String.format("%.0f", totalSure)).append(" dk\n");
                    sb.append("Rota üzerinden hesaplanan mesafe: ").append("5 km\n\n");
                    totalFare += routeFare;
                }

                // Hedef nokta için taksi kontrolü
                double taxiFareEnd = 0;
                double endToDurakMesafe = hedef.mesafeHesapla(endDurak.getKonum());
                if (endToDurakMesafe > 3) {
                    sb.append("Hedef noktasından en yakın durağa olan mesafe ")
                            .append(String.format("%.2f", endToDurakMesafe))
                            .append(" km olduğundan, hedefte taksi kullanılması gerekmektedir.\n");
                    Taksi taxi = cityData.getTaxi();
                    taxiFareEnd = taxi.hesaplaUcret(endToDurakMesafe);
                    sb.append("Hedef için taksi ücreti: ").append(String.format("%.2f", taxiFareEnd)).append(" TL\n\n");
                } else {
                    sb.append("Hedef noktasından en yakın durak (").append(endDurak.getName())
                            .append(") ").append(String.format("%.2f", endToDurakMesafe * 1000))
                            .append(" m uzaklıkta → Yürüme = 0 TL\n\n");
                }
                totalFare += taxiFareEnd;

                // Yolcu tipi ve indirim uygulanması
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
                double finalFare = totalFare * (1 - indirimOrani);

                sb.append("Yolcu tipi: ").append(cbYolcuTipi.getValue()).append("\n");
                sb.append("Uygulanan indirim oranı: ").append(String.format("%.0f%%", indirimOrani * 100)).append("\n");

                // Eğer ödeme yöntemi Kredi Kartı ise, zamlı fiyatı hesaplayıp görüntüleyelim
                String odemeYontemi = cbOdemeYontemi.getValue();
                double displayFare = finalFare;
                if (odemeYontemi.equals("Kredi Kartı")) {
                    displayFare = finalFare * 1.2; // %20 zam uygulanmış hali
                }
                sb.append("İndirim sonrası toplam ücret: ").append(String.format("%.2f", displayFare)).append(" TL\n\n");

                // Ödeme işlemi simülasyonu
                Odeme odeme;
                if (odemeYontemi.equals("Nakit")) {
                    odeme = new Nakit();
                } else if (odemeYontemi.equals("Kredi Kartı")) {
                    odeme = new KrediKart("1234-5678-9012-3456");
                } else if (odemeYontemi.equals("KentKart")) {
                    // KentKart için başlangıç bakiyesi 10 TL olarak ayarlandı.
                    odeme = new KentKart(10);
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

    // Sağ tarafta grafik görünüm: Duraklar, bağlantılar, zoom ve durak üzerine tıklandığında kopyalanabilir detay penceresi
    private Pane createGraphPane(Stage primaryStage) {
        Pane graphPane = new Pane();
        graphPane.setPrefSize(600, 400);
        graphPane.setStyle("-fx-background-color: #2b2b2b;");

        if (cityData == null || cityData.getDuraklar() == null || cityData.getDuraklar().isEmpty()) {
            Label lbl = new Label("Veri seti yüklenemedi veya durak bilgisi boş.");
            lbl.setStyle("-fx-text-fill: white;");
            graphPane.getChildren().add(lbl);
            return graphPane;
        }

        List<Durak> duraklar = cityData.getDuraklar();
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

            // Üzerine tıklandığında kopyalanabilir detay penceresi açılır.
            circle.setOnMouseClicked((MouseEvent event) -> {
                Stage detailStage = new Stage();
                detailStage.initOwner(primaryStage);
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.setTitle("Durak Detayları - " + d.getName());
                TextArea detailText = new TextArea("Durak: " + d.getName() +
                        "\nEnlem: " + k.getEnlem() +
                        "\nBoylam: " + k.getBoylam());
                detailText.setWrapText(true);
                detailText.setEditable(true);
                VBox vbox = new VBox(detailText);
                vbox.setPadding(new Insets(10));
                Scene detailScene = new Scene(vbox, 250, 150);
                detailStage.setScene(detailScene);
                detailStage.setX(event.getScreenX());
                detailStage.setY(event.getScreenY() - 150);
                detailStage.show();
            });

            graphPane.getChildren().add(circle);
            circleMap.put(d.getId(), circle);
        }
        // Duraklar arası bağlantılar: NextStop için düz çizgi, Transfer için kırmızı kesikli çizgi
        for (Durak d : duraklar) {
            Circle fromCircle = circleMap.get(d.getId());
            if (d.getNextStops() != null) {
                for (NextStop ns : d.getNextStops()) {
                    Circle toCircle = circleMap.get(ns.getStopId());
                    if (toCircle != null) {
                        Line line = new Line(fromCircle.getCenterX(), fromCircle.getCenterY(),
                                toCircle.getCenterX(), toCircle.getCenterY());
                        line.setStroke(Color.WHITE);
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
        // Zoom: fare scroll olaylarıyla
        graphPane.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            graphPane.setScaleX(graphPane.getScaleX() * zoomFactor);
            graphPane.setScaleY(graphPane.getScaleY() * zoomFactor);
            event.consume();
        });

        return graphPane;
    }

    // Undirected graph üzerinden Dijkstra algoritması
    private List<Durak> calculateRouteUndirected(Durak start, Durak end, List<Durak> duraklar) {
        // Undirected graph oluşturuluyor
        Map<String, List<Edge>> graph = new HashMap<>();
        for (Durak d : duraklar) {
            graph.put(d.getId(), new ArrayList<>());
        }
        // NextStop ve Transfer edge’leri iki yönlü ekleniyor
        for (Durak d : duraklar) {
            if (d.getNextStops() != null) {
                for (NextStop ns : d.getNextStops()) {
                    graph.get(d.getId()).add(new Edge(ns.getStopId(), ns.getSure(), ns.getUcret()));
                    // Reverse edge
                    graph.get(ns.getStopId()).add(new Edge(d.getId(), ns.getSure(), ns.getUcret()));
                }
            }
            if (d.getTransfer() != null) {
                Transfer t = d.getTransfer();
                graph.get(d.getId()).add(new Edge(t.getTransferStopId(), t.getTransferSure(), t.getTransferUcret()));
                graph.get(t.getTransferStopId()).add(new Edge(d.getId(), t.getTransferSure(), t.getTransferUcret()));
            }
        }
        // Dijkstra algoritması
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
            for (Edge edge : graph.get(currentId)) {
                double alt = dist.get(currentId) + edge.sure;
                if (alt < dist.get(edge.neighborId)) {
                    dist.put(edge.neighborId, alt);
                    prev.put(edge.neighborId, currentId);
                    queue.add(new Pair<>(edge.neighborId, alt));
                }
            }
        }
        List<Durak> route = new ArrayList<>();
        String cur = end.getId();
        if (!prev.containsKey(cur) && !cur.equals(start.getId())) return null;
        while (cur != null) {
            route.add(0, getDurakById(duraklar, cur));
            cur = prev.get(cur);
        }
        return route;
    }

    // Helper: Get Durak by id
    private Durak getDurakById(List<Durak> duraklar, String id) {
        for (Durak d : duraklar) {
            if (d.getId().equals(id)) return d;
        }
        return null;
    }

    // Directed edge bilgisini döndüren metot; reverse kontrolü de yapılıyor.
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
        // Reverse kontrol
        if (to.getNextStops() != null) {
            for (NextStop ns : to.getNextStops()) {
                if (ns.getStopId().equals(from.getId())) {
                    return new Pair<>((double) ns.getSure(), ns.getUcret());
                }
            }
        }
        if (to.getTransfer() != null && to.getTransfer().getTransferStopId().equals(from.getId())) {
            return new Pair<>((double) to.getTransfer().getTransferSure(), to.getTransfer().getTransferUcret());
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

    public static void main(String[] args) {
        launch(args);
    }
}
