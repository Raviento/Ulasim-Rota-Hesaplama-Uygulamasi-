package com.example.prolab4;

import java.util.List;

public class CityData {
    private String city;
    private Taksi taxi;
    private List<Durak> duraklar;


    public CityData() {
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public Taksi getTaxi() {
        return taxi;
    }
    public void setTaxi(Taksi taxi) {
        this.taxi = taxi;
    }

    public List<Durak> getDuraklar() {
        return duraklar;
    }
    public void setDuraklar(List<Durak> duraklar) {
        this.duraklar = duraklar;
    }
}
