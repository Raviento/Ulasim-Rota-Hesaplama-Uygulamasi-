package com.example.prolab4;

class Otobus extends Arac {
    public Otobus(Konum konum) {
        super(konum);
    }
    
    @Override
    public double hesaplaUcret(double mesafe) {
        // Örnek ücret hesaplaması; veri setine göre sabit ücretler veya mesafeye bağlı ücretler uygulanabilir.
        return mesafe * 1.0; // placeholder
    }
}