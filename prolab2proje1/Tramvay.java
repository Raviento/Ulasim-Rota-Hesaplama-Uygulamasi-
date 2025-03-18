
class Tramvay extends Arac {
    public Tramvay(String plaka, Konum konum) {
        super(plaka, konum);
    }
    
    @Override
    public double hesaplaUcret(double mesafe) {
        return mesafe * 0.8; // placeholder
    }
}