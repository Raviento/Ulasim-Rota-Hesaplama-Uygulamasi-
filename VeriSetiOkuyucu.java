import com.google.gson.Gson;
import java.io.FileReader;

public class VeriSetiOkuyucu {
    public static void main(String[] args) {
        try {
            // Gson nesnesi oluşturuluyor
            Gson gson = new Gson();
            // JSON dosyasını okuyacak FileReader oluşturun
            FileReader reader = new FileReader("veriseti.json");
            
            // JSON'u VeriSeti nesnesine parse ediyoruz
            VeriSeti veriSeti = gson.fromJson(reader, VeriSeti.class);
            reader.close();
            
            // Verileri ekrana yazdırarak kontrol edelim
            System.out.println("Şehir: " + veriSeti.getCity());
            System.out.println("Taksi Açılış Ücreti: " + veriSeti.getTaxi().getOpeningFee());
            System.out.println("Taksi Km Başına Ücret: " + veriSeti.getTaxi().getCostPerKm());
            System.out.println("Duraklar:");
            for(Durak durak : veriSeti.getDuraklar()){
                System.out.println("- " + durak.getName() + " (" + durak.getType() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
