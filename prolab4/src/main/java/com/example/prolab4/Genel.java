package com.example.prolab4;

class Genel extends Yolcu {
    public Genel(String ad) {
        super(ad);
    }
    
    @Override
    public double getIndirimOrani() {
        return 0.0;
    }
}