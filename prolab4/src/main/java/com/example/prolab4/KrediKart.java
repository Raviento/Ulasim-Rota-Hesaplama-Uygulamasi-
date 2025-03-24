package com.example.prolab4;

class KrediKart extends Odeme {
    private String kartNumarasi;

    public KrediKart(String kartNumarasi) {
        this.kartNumarasi = kartNumarasi;
    }

    @Override
    public void odemeYap(double tutar) {
        double zamliTutar = tutar * 1.2; // %20 zam ekleniyor
        System.out.println("Kredi Kart ile " + zamliTutar + " TL ödeme yapıldı (zamlı).");
    }
}
