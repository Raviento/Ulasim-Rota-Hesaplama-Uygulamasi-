package com.example.prolab4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Taksi extends Arac {
    private double openingFee;
    private double costPerKm;

    public Taksi() {
        super(new Konum(0, 0));
    }

    @JsonCreator
    public Taksi(@JsonProperty("openingFee") double openingFee,
                 @JsonProperty("costPerKm") double costPerKm) {

        super(new Konum(0, 0));
        this.openingFee = openingFee;
        this.costPerKm = costPerKm;
    }

    public double getOpeningFee() {
        return openingFee;
    }

    public void setOpeningFee(double openingFee) {
        this.openingFee = openingFee;
    }

    public double getCostPerKm() {
        return costPerKm;
    }

    public void setCostPerKm(double costPerKm) {
        this.costPerKm = costPerKm;
    }

    @Override
    public double hesaplaUcret(double mesafe) {
        return openingFee + (costPerKm * mesafe);
    }
}
