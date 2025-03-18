abstract class Yolcu {
    protected String ad;
    
    public Yolcu(String ad) {
        this.ad = ad;
    }
    
    // Her yolcu için indirim oranı hesaplanır.
    public abstract double getIndirimOrani();
    
    public String getAd() {
        return ad;
    }
}