
class Ogrenci extends Yolcu {
    public Ogrenci(String ad) {
        super(ad);
    }
    
    @Override
    public double getIndirimOrani() {
        return 0.5;
    }
}
