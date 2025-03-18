import java.util.ArrayList;
import java.util.List;

class Durak {
    private String id;
    private String name;
    private String type; // "bus" veya "tram"
    private Konum konum;
    private boolean sonDurak;
    private List<NextStop> nextStops;
    private Transfer transfer;
    
    public Durak(String id, String name, String type, Konum konum, boolean sonDurak) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.konum = konum;
        this.sonDurak = sonDurak;
        this.nextStops = new ArrayList<>();
    }
    
    public void addNextStop(NextStop ns) {
        nextStops.add(ns);
    }
    
    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public Konum getKonum() {
        return konum;
    }
    
    public boolean isSonDurak() {
        return sonDurak;
    }
    
    public List<NextStop> getNextStops() {
        return nextStops;
    }
    
    public Transfer getTransfer() {
        return transfer;
    }
}
