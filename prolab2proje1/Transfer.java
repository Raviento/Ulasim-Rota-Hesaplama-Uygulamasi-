
class Transfer {
    private String transferStopId;
    private int transferSure;
    private double transferUcret;
    
    public Transfer(String transferStopId, int transferSure, double transferUcret) {
        this.transferStopId = transferStopId;
        this.transferSure = transferSure;
        this.transferUcret = transferUcret;
    }
    
    public String getTransferStopId() {
        return transferStopId;
    }
    
    public int getTransferSure() {
        return transferSure;
    }
    
    public double getTransferUcret() {
        return transferUcret;
    }
}
