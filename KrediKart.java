// Kredi Kartı ödeme
class KrediKart extends Odeme {
    private String kartNumarasi;
    
    public KrediKart(String kartNumarasi) {
        this.kartNumarasi = kartNumarasi;
    }
    
    @Override
    public void odemeYap(double tutar) {
        System.out.println("Kredi Kart ile " + tutar + " TL ödeme yapıldı.");
    }
}