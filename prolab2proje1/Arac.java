
abstract class Arac {
    protected String plaka;
    protected Konum konum;
    
    public Arac(String plaka, Konum konum) {
        this.plaka = plaka;
        this.konum = konum;
    }
    
    public abstract double hesaplaUcret(double mesafe);
}