import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class VeriSetiOkuyucu {
    public static void main(String[] args) {
        Gson gson = new Gson();
        
        try (FileReader reader = new FileReader("veriseti.json")) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> veriMap = gson.fromJson(reader, type);
            
            // Örnek: "city" alanını çekme
            System.out.println("Şehir: " + veriMap.get("city"));
            
            // "taxi" ve "duraklar" alanlarına erişim için veri yapısını incelemen gerekecek.
            // Bu yöntemle tip dönüşümleri yapman gerekecektir.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
