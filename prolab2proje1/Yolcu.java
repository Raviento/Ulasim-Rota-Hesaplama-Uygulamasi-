abstract class Yolcu {
    protected String ad;
    
    public Yolcu(String ad) {
        this.ad = ad;
    }
    
    public abstract double getIndirimOrani();
    
    public String getAd() {
        return ad;
    }
}