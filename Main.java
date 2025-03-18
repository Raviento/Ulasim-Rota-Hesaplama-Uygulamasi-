// 6. Main Sınıfı (Örnek Kullanım)
import java.util.List;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Örnek konumlar
        Konum konumOtogar = new Konum(40.78259, 29.94628);
        Konum konumSekapark = new Konum(40.7652, 29.9619);
        
        // Durak nesneleri oluşturuluyor
        Durak busOtogar = new Durak("bus_otogar", "Otogar (Bus)", "bus", konumOtogar, false);
        Durak busSekapark = new Durak("bus_sekapark", "Sekapark (Bus)", "bus", konumSekapark, false);
        
        // Otogar'dan Sekapark'a giden next stop ekleniyor
        busOtogar.addNextStop(new NextStop("bus_sekapark", 3.5, 10, 3));
        // Transfer bilgisi ekleniyor (örneğin tramvaya aktarma)
        busOtogar.setTransfer(new Transfer("tram_otogar", 2, 0.5));
        
        // Taksi nesnesi oluşturuluyor (veriseti.json'daki bilgilere göre: açılış ücreti 10 TL, km başına 4 TL)
        Taksi taxi = new Taksi("taxi_01", konumOtogar, 10, 4);
        double taxiUcret = taxi.hesaplaUcret(5);  // Örnek: 5 km için hesaplama
        System.out.println("Taksi ücreti: " + taxiUcret + " TL");
        
        // Ödeme örneği
        Odeme nakit = new Nakit();
        nakit.odemeYap(taxiUcret);
        
        // Rota hesaplayıcı örneği
        RotaHesaplayici rotaHesaplayici = new RotaHesaplayici();
        List<Durak> duraklar = Arrays.asList(busOtogar, busSekapark);
        rotaHesaplayici.rotaHesapla(konumOtogar, konumSekapark, duraklar);
        
        // Yolcu nesneleri örneği
        Yolcu yolcu1 = new Genel("Ahmet");
        Yolcu yolcu2 = new Ogrenci("Mehmet");
        System.out.println(yolcu1.getAd() + " indirim oranı: " + yolcu1.getIndirimOrani());
        System.out.println(yolcu2.getAd() + " indirim oranı: " + yolcu2.getIndirimOrani());
    }
}