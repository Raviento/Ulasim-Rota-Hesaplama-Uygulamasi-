package com.example.prolab4;

class NextStop {
    private String stopId;
    private double mesafe;
    private int sure;
    private double ucret;

    public NextStop() {
    }

    public NextStop(String stopId, double mesafe, int sure, double ucret) {
        this.stopId = stopId;
        this.mesafe = mesafe;
        this.sure = sure;
        this.ucret = ucret;
    }
    
    public String getStopId() {
        return stopId;
    }
    
    public double getMesafe() {
        return mesafe;
    }
    
    public int getSure() {
        return sure;
    }
    
    public double getUcret() {
        return ucret;
    }
}
