package com.example.prolab4;

class Tramvay extends Arac {
    public Tramvay(Konum konum) {
        super(konum);
    }
    
    @Override
    public double hesaplaUcret(double mesafe) {
        return mesafe * 0.8; // placeholder
    }
}