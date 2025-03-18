// Otobüs sınıfı
class Otobus extends Arac {
    public Otobus(String plaka, Konum konum) {
        super(plaka, konum);
    }
    
    @Override
    public double hesaplaUcret(double mesafe) {
        // Örnek ücret hesaplaması; veri setine göre sabit ücretler veya mesafeye bağlı ücretler uygulanabilir.
        return mesafe * 1.0; // placeholder
    }
}