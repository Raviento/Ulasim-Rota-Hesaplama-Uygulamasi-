// Taksi sınıfı (verilen açılış ücreti ve km başına ücret kullanılarak hesaplanır)
class Taksi extends Arac {
    private double openingFee;
    private double costPerKm;
    
    public Taksi(String plaka, Konum konum, double openingFee, double costPerKm) {
        super(plaka, konum);
        this.openingFee = openingFee;
        this.costPerKm = costPerKm;
    }
    
    @Override
    public double hesaplaUcret(double mesafe) {
        return openingFee + (costPerKm * mesafe);
    }
}