package com.example.prolab4;

import com.fasterxml.jackson.databind.ObjectMapper; import javafx.application.Application; import javafx.geometry.Insets; import javafx.scene.Scene; import javafx.scene.control.*;
import javafx.scene.input.MouseEvent; import javafx.scene.input.ScrollEvent; import javafx.scene.layout.*;
import javafx.scene.paint.Color; import javafx.scene.shape.Circle; import javafx.scene.shape.Line; import javafx.stage.Modality; import javafx.stage.Stage; import javafx.util.Pair;
import javafx.geometry.Point2D;
import java.io.InputStream; import java.util.*;

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
        loadCityData();
        primaryStage.setTitle("Ulaşım Rota Planlama Sistemi");

        // SplitPane: sol panel = kullanıcı arayüzü, sağ panel = grafik görünüm
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

    // Kullanıcı arayüzü ve rota hesaplama sonuçlarının gösterildiği sol panel
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

                List<Durak> duraklar = cityData.getDuraklar();
                Durak startDurak = findNearestDurak(baslangic, duraklar);
                Durak endDurak = findNearestDurak(hedef, duraklar);

                StringBuilder sb = new StringBuilder();

                double totalFare = 0;
                // Başlangıç için: Eğer 3 km'den fazla ise taksi, aksi halde yürüyüş
                double startToDurakMesafe = baslangic.mesafeHesapla(startDurak.getKonum());
                if (startToDurakMesafe > 3) {
                    sb.append("Başlangıç noktasından en yakın durağa olan mesafe ")
                            .append(String.format("%.2f", startToDurakMesafe))
                            .append(" km olduğundan, başlangıçta taksi kullanılması gerekmektedir.\n");
                    Taksi taxi = cityData.getTaxi();
                    double taxiFareStart = taxi.hesaplaUcret(startToDurakMesafe);
                    sb.append("Başlangıç için taksi ücreti: ").append(String.format("%.2f", taxiFareStart)).append(" TL\n\n");
                    totalFare += taxiFareStart;
                } else {
                    sb.append("Başlangıç noktasından en yakın durak (")
                            .append(startDurak.getName()).append(") ")
                            .append(String.format("%.2f", startToDurakMesafe * 1000))
                            .append(" m uzaklıkta → Yürüme = 0 TL\n\n");
                }

                // Ana rota hesaplaması (undirected Dijkstra)
                List<Durak> route = calculateRouteUndirected(startDurak, endDurak, duraklar);
                if (route == null || route.isEmpty()) {
                    sb.append("Uygun ana rota bulunamadı.\n");
                } else {
                    sb.append("🚏 Ana Rota:\n");
                    double totalSure = 0;
                    double routeFare = 0;
                    for (int i = 0; i < route.size() - 1; i++) {
                        Durak current = route.get(i);
                        Durak next = route.get(i + 1);
                        Pair<Double, Double> edgeInfo = getEdgeInfo(current, next);
                        totalSure += edgeInfo.getKey();
                        routeFare += edgeInfo.getValue();
                        sb.append((i + 1)).append("⃣ ").append(current.getName())
                                .append(" → ").append(next.getName());
                        if (current.getType().equalsIgnoreCase("bus"))
                            sb.append(" (🚌 Otobüs)");
                        else if (current.getType().equalsIgnoreCase("tram"))
                            sb.append(" (🚋 Tramvay)");
                        sb.append("\n   ⏳ Süre: ").append(edgeInfo.getKey()).append(" dk")
                                .append("\n   💰 Ücret: ").append(edgeInfo.getValue()).append(" TL\n\n");
                    }
                    sb.append("Ana rota üzerinden hesaplanan ücret: ").append(String.format("%.2f", routeFare)).append(" TL\n");
                    sb.append("Ana rota üzerinden hesaplanan süre: ").append(String.format("%.0f", totalSure)).append(" dk\n\n");
                    totalFare += routeFare;
                }

                // Hedef için: Eğer 3 km'den fazla ise taksi, aksi halde yürüyüş
                double endToDurakMesafe = hedef.mesafeHesapla(endDurak.getKonum());
                if (endToDurakMesafe > 3) {
                    sb.append("Hedef noktasından en yakın durağa olan mesafe ")
                            .append(String.format("%.2f", endToDurakMesafe))
                            .append(" km olduğundan, hedefte taksi kullanılması gerekmektedir.\n");
                    Taksi taxi = cityData.getTaxi();
                    double taxiFareEnd = taxi.hesaplaUcret(endToDurakMesafe);
                    sb.append("Hedef için taksi ücreti: ").append(String.format("%.2f", taxiFareEnd)).append(" TL\n\n");
                    totalFare += taxiFareEnd;
                } else {
                    sb.append("Hedef noktasından en yakın durak (")
                            .append(endDurak.getName()).append(") ")
                            .append(String.format("%.2f", endToDurakMesafe * 1000))
                            .append(" m uzaklıkta → Yürüme = 0 TL\n\n");
                }

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
                double fareAfterDiscount = totalFare * (1 - indirimOrani);

                // Kredi Kartı seçildiyse %20 zam uygulanmış fiyat
                String odemeYontemi = cbOdemeYontemi.getValue();
                double displayFare = fareAfterDiscount;
                if (odemeYontemi.equals("Kredi Kartı")) {
                    displayFare = fareAfterDiscount * 1.2;
                }
                sb.append("Yolcu tipi: ").append(cbYolcuTipi.getValue()).append("\n");
                sb.append("Uygulanan indirim oranı: ").append(String.format("%.0f%%", indirimOrani * 100)).append("\n");
                sb.append("İndirim sonrası toplam ücret: ").append(String.format("%.2f", displayFare)).append(" TL\n\n");

                // Ödeme işlemi simülasyonu
                Odeme odeme;
                if (odemeYontemi.equals("Nakit")) {
                    odeme = new Nakit();
                } else if (odemeYontemi.equals("Kredi Kartı")) {
                    odeme = new KrediKart("1234-5678-9012-3456");
                } else if (odemeYontemi.equals("KentKart")) {
                    odeme = new KentKart(10);
                } else {
                    odeme = new Nakit();
                }
                odeme.odemeYap(fareAfterDiscount);
                sb.append("Ödeme yöntemi: ").append(odemeYontemi).append("\n\n");

                // Alternatif rotalar
                sb.append("🛤 Alternatif Rotalar:\n");
                // 1. Sadece Taksi
                sb.append(computeDirectTaxiRoute(baslangic, hedef));
                // 2. Sadece Otobüs (otobüs + otobüs)
                sb.append(computeBusOnlyRoute(startDurak, endDurak, duraklar));
                // 3. Otobüs + Tramvay (yeni alternatif)
                sb.append(computeBusAndTramRoute(startDurak, endDurak, duraklar));
                // 4. En Az Aktarmalı Rota
                sb.append(computeMinHopsRoute(startDurak, endDurak, duraklar));

                taSonuc.setText(sb.toString());
            } catch (Exception ex) {
                taSonuc.setText("Hata: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        return grid;
    }

    // Grafik görünüm paneli
    private Pane createGraphPane(Stage primaryStage) {
        Pane graphPane = new Pane();
        graphPane.setPrefSize(600, 400);
        graphPane.setStyle("-fx-background-color: #2b2b2b;");

        List<Durak> duraklar = cityData.getDuraklar();
        if (duraklar == null || duraklar.isEmpty()) {
            Label lbl = new Label("Veri seti yüklenemedi veya durak bilgisi boş.");
            lbl.setStyle("-fx-text-fill: white;");
            graphPane.getChildren().add(lbl);
            return graphPane;
        }

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
            circle.setOnMouseClicked((MouseEvent event) -> {
                Stage detailStage = new Stage();
                detailStage.initOwner(primaryStage);
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.setTitle("Durak Detayları - " + d.getName());
                TextArea detailText = new TextArea("Durak: " + d.getName() +
                        "\nEnlem: " + k.getEnlem() +
                        "\nBoylam: " + k.getBoylam());
                detailText.setWrapText(true);
                detailText.setEditable(false);
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
        // Duraklar arası bağlantılar
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
        graphPane.setOnScroll(event -> {
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;

            // Farenin sahnedeki konumunu alıyoruz
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
            // Zoomdan önce, farenin graphPane içindeki koordinatlarını hesaplıyoruz
            Point2D mouseLocalBefore = graphPane.sceneToLocal(mouseSceneCoords);

            double oldScale = graphPane.getScaleX();
            double newScale = oldScale * zoomFactor;
            graphPane.setScaleX(newScale);
            graphPane.setScaleY(newScale);

            // Zoom sonrası farenin graphPane içindeki yeni koordinatlarını hesaplıyoruz
            Point2D mouseLocalAfter = graphPane.sceneToLocal(mouseSceneCoords);
            // İki konum arasındaki farkı buluyoruz
            Point2D delta = mouseLocalAfter.subtract(mouseLocalBefore);

            // Bu fark kadar translate değerlerini güncelliyoruz, böylece farenin konumu sabit kalır
            graphPane.setTranslateX(graphPane.getTranslateX() - delta.getX());
            graphPane.setTranslateY(graphPane.getTranslateY() - delta.getY());

            event.consume();
        });
        graphPane.setOnMousePressed(event -> {
            graphPane.setUserData(
                    new double[]{event.getSceneX(),
                    event.getSceneY(),
                    graphPane.getTranslateX(),
                    graphPane.getTranslateY()
                    });
        });

        graphPane.setOnMouseDragged(event -> {
            double[] data = (double[]) graphPane.getUserData();
            double deltaX = event.getSceneX() - data[0];
            double deltaY = event.getSceneY() - data[1];
            graphPane.setTranslateX(data[2] + deltaX);
            graphPane.setTranslateY(data[3] + deltaY); });

        return graphPane;
    }

    // Yeni alternatif: Otobüs + Tramvay rotası hesaplama
    private String computeBusAndTramRoute(Durak start, Durak end, List<Durak> duraklar) {
        // Otobüsle başlayıp, aktarma ile tramvaya geçilebilecek bir durak arıyoruz.
        // Örneğin; eğer bir otobüs durağında transfer bilgisi varsa ve transfer edilen durak tramvay tipindeyse,
        // o rota uygun kabul edilebilir.
        Durak busToTramStop = null;
        for (Durak d : duraklar) {
            if (d.getType().equalsIgnoreCase("bus") && d.getTransfer() != null) {
                Durak transferDurak = getDurakById(duraklar, d.getTransfer().getTransferStopId());
                if (transferDurak != null && transferDurak.getType().equalsIgnoreCase("tram")) {
                    busToTramStop = d;
                    break;
                }
            }
        }
        if (busToTramStop == null) {
            return "🔹 Otobüs + Tramvay: Uygun alternatif rota bulunamadı.\n";
        }
        // Hesaplama: start -> busToTramStop (otobüs rotası) ve transfer sonrası tramvay rotası: transferDurak -> end
        List<Durak> busRoute = calculateRouteUndirected(start, busToTramStop, duraklar);
        Durak transferDurak = getDurakById(duraklar, busToTramStop.getTransfer().getTransferStopId());
        List<Durak> tramRoute = calculateRouteUndirected(transferDurak, end, duraklar);
        if (busRoute == null || tramRoute == null || busRoute.isEmpty() || tramRoute.isEmpty()) {
            return "🔹 Otobüs + Tramvay: Uygun alternatif rota bulunamadı.\n";
        }
        double totalSure = 0;
        double totalFare = 0;
        StringBuilder sb = new StringBuilder("🔹 Otobüs + Tramvay:\n");
        // Otobüs rotası bilgileri
        for (int i = 0; i < busRoute.size() - 1; i++) {
            Durak current = busRoute.get(i);
            Durak next = busRoute.get(i + 1);
            Pair<Double, Double> info = getEdgeInfo(current, next);
            totalSure += info.getKey();
            totalFare += info.getValue();
            sb.append("   ").append(current.getName()).append(" -> ").append(next.getName()).append(" (Otobüs)\n");
        }
        // Transfer bilgisi ekleniyor
        Pair<Double, Double> transferInfo = new Pair<>((double)busToTramStop.getTransfer().getTransferSure(), busToTramStop.getTransfer().getTransferUcret());
        totalSure += transferInfo.getKey();
        totalFare += transferInfo.getValue();
        sb.append("   ").append(busToTramStop.getName()).append(" → Transfer (")
                .append(transferInfo.getKey()).append(" dk, ").append(transferInfo.getValue()).append(" TL)\n");
        // Tramvay rotası bilgileri
        for (int i = 0; i < tramRoute.size() - 1; i++) {
            Durak current = tramRoute.get(i);
            Durak next = tramRoute.get(i + 1);
            Pair<Double, Double> info = getEdgeInfo(current, next);
            totalSure += info.getKey();
            totalFare += info.getValue();
            sb.append("   ").append(current.getName()).append(" -> ").append(next.getName()).append(" (Tramvay)\n");
        }
        sb.append("   Toplam Süre: ").append(String.format("%.0f", totalSure)).append(" dk, Ücret: ")
                .append(String.format("%.2f", totalFare)).append(" TL\n");
        return sb.toString();
    }

    // Undirected Dijkstra algoritması
    private List<Durak> calculateRouteUndirected(Durak start, Durak end, List<Durak> duraklar) {
        Map<String, List<Edge>> graph = new HashMap<>();
        for (Durak d : duraklar) {
            graph.put(d.getId(), new ArrayList<>());
        }
        for (Durak d : duraklar) {
            if (d.getNextStops() != null) {
                for (NextStop ns : d.getNextStops()) {
                    graph.get(d.getId()).add(new Edge(ns.getStopId(), ns.getSure(), ns.getUcret()));
                    graph.get(ns.getStopId()).add(new Edge(d.getId(), ns.getSure(), ns.getUcret()));
                }
            }
            if (d.getTransfer() != null) {
                graph.get(d.getId()).add(new Edge(d.getTransfer().getTransferStopId(), d.getTransfer().getTransferSure(), d.getTransfer().getTransferUcret()));
                graph.get(d.getTransfer().getTransferStopId()).add(new Edge(d.getId(), d.getTransfer().getTransferSure(), d.getTransfer().getTransferUcret()));
            }
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

    // En az aktarmalı rota (BFS ile)
    private List<Durak> calculateRouteMinHops(Durak start, Durak end, List<Durak> duraklar) {
        Map<String, List<String>> graph = new HashMap<>();
        for (Durak d : duraklar) {
            graph.put(d.getId(), new ArrayList<>());
        }
        for (Durak d : duraklar) {
            if (d.getNextStops() != null) {
                for (NextStop ns : d.getNextStops()) {
                    graph.get(d.getId()).add(ns.getStopId());
                    graph.get(ns.getStopId()).add(d.getId());
                }
            }
            if (d.getTransfer() != null) {
                String tid = d.getTransfer().getTransferStopId();
                graph.get(d.getId()).add(tid);
                graph.get(tid).add(d.getId());
            }
        }
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start.getId());
        visited.add(start.getId());
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (cur.equals(end.getId())) break;
            for (String neighbor : graph.get(cur)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    prev.put(neighbor, cur);
                    queue.add(neighbor);
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

    // Direkt kenardaki bilgiyi döndüren metot (reverse kontrolü dahil)
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

    // En az aktarmalı rota metodu
    private String computeMinHopsRoute(Durak start, Durak end, List<Durak> duraklar) {
        List<Durak> route = calculateRouteMinHops(start, end, duraklar);
        if (route == null || route.isEmpty()) return "🔹 En Az Aktarmalı Rota: Uygun rota bulunamadı.\n";
        StringBuilder sb = new StringBuilder("🔹 En Az Aktarmalı Rota:\n   Rota: ");
        for (Durak d : route) {
            sb.append(d.getName()).append(" -> ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append("\n");
        return sb.toString();
    }

    // Sadece Taksi rotası hesaplama
    private String computeDirectTaxiRoute(Konum start, Konum destination) {
        double distance = start.mesafeHesapla(destination);
        Taksi taxi = cityData.getTaxi();
        double fare = taxi.hesaplaUcret(distance);
        double time = distance * 2; // varsayım: 2 dk/km
        return "🔹 Sadece Taksi:\n   Mesafe: " + String.format("%.2f", distance) + " km\n   Süre: " +
                String.format("%.0f", time) + " dk\n   Ücret: " + String.format("%.2f", fare) + " TL\n";
    }

    // Sadece Otobüs rotası hesaplama (otobüs + otobüs)
    private String computeBusOnlyRoute(Durak start, Durak end, List<Durak> duraklar) {
        List<Durak> route = calculateRouteUndirected(start, end, duraklar);
        if (route == null || route.isEmpty()) return "🔹 Sadece Otobüs: Uygun rota bulunamadı.\n";
        double totalSure = 0;
        double totalFare = 0;
        StringBuilder sb = new StringBuilder("🔹 Sadece Otobüs:\n");
        for (int i = 0; i < route.size() - 1; i++) {
            Durak current = route.get(i);
            Durak next = route.get(i + 1);
            Pair<Double, Double> info = getEdgeInfo(current, next);
            totalSure += info.getKey();
            totalFare += info.getValue();
            sb.append("   ").append(current.getName()).append(" -> ").append(next.getName()).append(" (Otobüs)\n");
        }
        sb.append("   Toplam Süre: ").append(String.format("%.0f", totalSure))
                .append(" dk, Ücret: ").append(String.format("%.2f", totalFare)).append(" TL\n");
        return sb.toString();
    }

    // Yardımcı metot: Durak'ı id'ye göre bulur.
    private Durak getDurakById(List<Durak> duraklar, String id) {
        for (Durak d : duraklar) {
            if (d.getId().equals(id)) return d;
        }
        return null;
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

    // JSON veriseti yüklemesi
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