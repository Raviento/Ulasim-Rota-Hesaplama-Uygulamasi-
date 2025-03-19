package com.example.prolab4;

class KentKart extends Odeme {
    private double bakiye;

    public KentKart(double bakiye) {
        this.bakiye = bakiye;
    }

    @Override
    public void odemeYap(double tutar) {
        if(bakiye >= tutar) {
            bakiye -= tutar;
            System.out.println("Kentkart ile ödeme yapıldı. Kalan bakiye: " + bakiye + " TL");
        } else {
            System.out.println("Yetersiz bakiye.");
        }
    }
}
