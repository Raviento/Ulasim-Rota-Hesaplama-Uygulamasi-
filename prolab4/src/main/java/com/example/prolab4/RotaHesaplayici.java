package com.example.prolab4;

import java.util.List;

class RotaHesaplayici {

    public void rotaHesapla(Konum baslangic, Konum hedef, List<Durak> duraklar) {
        System.out.println("Rota hesaplama işlemi gerçekleştiriliyor...");

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

    }
}
