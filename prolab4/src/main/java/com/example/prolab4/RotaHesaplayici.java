package com.example.prolab4;

import java.util.List;

// Bu sınıf, kullanıcının başlangıç ve hedef konumlarına göre en uygun rotayı hesaplamak üzere algoritmaları içerir.
class RotaHesaplayici {
    
    // Örnek rota hesaplama metodu
    public void rotaHesapla(Konum baslangic, Konum hedef, List<Durak> duraklar) {
        System.out.println("Rota hesaplama işlemi gerçekleştiriliyor...");
        
        // 1. Kullanıcının bulunduğu konuma en yakın durağı bulma
        Durak enYakinDurak = null;
        double minMesafe = Double.MAX_VALUE;
        for(Durak d : duraklar) {
            double mesafe = baslangic.mesafeHesapla(d.getKonum());
            if(mesafe < minMesafe) {
                minMesafe = mesafe;
                enYakinDurak = d;
            }
        }
        
        if(enYakinDurak != null) {
            System.out.println("Kullanıcı konumuna en yakın durak: " + enYakinDurak.getName() + " (" + minMesafe + " km)");
        }
        
        // 2. Hedef konuma en yakın durağı benzer şekilde bulunabilir.
        // 3. Otobüs, tramvay, aktarma ve taksi seçenekleri değerlendirilerek toplam ücret, süre ve mesafe hesaplanabilir.
        // Bu kısım, projenizin algoritmasına göre detaylandırılmalıdır.
    }
}
