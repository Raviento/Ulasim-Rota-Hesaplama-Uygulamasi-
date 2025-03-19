package com.example.prolab4;

abstract class Arac {
    protected Konum konum;
    
    public Arac(Konum konum) {
        this.konum = konum;
    }
    
    public abstract double hesaplaUcret(double mesafe);
}