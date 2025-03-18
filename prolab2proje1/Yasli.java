// Yaşlı yolcu (örneğin, belirli bir seyahat sayısından sonra ücretsiz seyahat hakkı eklenebilir)
class Yasli extends Yolcu {
    private int ucretsizSeyahatSayisi;
    
    public Yasli(String ad) {
        super(ad);
        this.ucretsizSeyahatSayisi = 0;
    }
    
    @Override
    public double getIndirimOrani() {
        // Örnek: %20 indirim uygulayabilir ya da 20 seyahatten sonra ücretsiz seyahat verebilirsiniz.
        return 0.2;
    }
    
    public void seyahatYap() {
        ucretsizSeyahatSayisi++;
        // 20 seyahat sonrası ücretsiz uygulama gibi kontrol eklenebilir.
    }
}