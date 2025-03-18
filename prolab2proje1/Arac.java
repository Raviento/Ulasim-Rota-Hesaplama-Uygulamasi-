// 2. Ulaşım Araçları (Arac) Sınıfı ve Alt Sınıfları

// Soyut Araç sınıfı
abstract class Arac {
    protected String plaka;
    protected Konum konum;
    
    public Arac(String plaka, Konum konum) {
        this.plaka = plaka;
        this.konum = konum;
    }
    
    // Belirli mesafe için ücret hesaplama metodu
    public abstract double hesaplaUcret(double mesafe);
}