package com.example.prolab4;

class Yasli extends Yolcu {
    private int ucretsizSeyahatSayisi;
    
    public Yasli(String ad) {
        super(ad);
        this.ucretsizSeyahatSayisi = 0;
    }
    
    @Override
    public double getIndirimOrani() {
        return 0.2;
    }
    
    public void seyahatYap() {
        ucretsizSeyahatSayisi++;
    }
}