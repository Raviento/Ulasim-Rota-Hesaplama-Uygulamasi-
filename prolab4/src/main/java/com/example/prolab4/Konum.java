package com.example.prolab4;

class Konum {
    private double enlem;
    private double boylam;
    
    public Konum(double enlem, double boylam) {
        this.enlem = enlem;
        this.boylam = boylam;
    }
    
    public double getEnlem() {
        return enlem;
    }
    
    public double getBoylam() {
        return boylam;
    }

    public double mesafeHesapla(Konum diger) {
        double R = 6371;
        double dLat = Math.toRadians(diger.enlem - this.enlem);
        double dLon = Math.toRadians(diger.boylam - this.boylam);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(this.enlem)) * Math.cos(Math.toRadians(diger.enlem)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
