package com.example.prolab4;

import java.util.List;

// JSON'dan üst seviye verileri tutacak
public class CityData {
    private String city;            // "city": "Izmit"
    private Taksi taxi;              // "taxi": { ... }
    private List<Durak> duraklar;   // "duraklar": [...]

    // Jackson için boş constructor
    public CityData() {
    }

    // Getter & Setter
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
