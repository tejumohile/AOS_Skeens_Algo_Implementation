package helper;

/**
 *
 * @author Tejashree
 */
public class Neighbor {

    private String IPAddress;
    private int PortNo;
    private String neighborID;

    public Neighbor( String neighborID, String IPAddress, int PortNo) {
        this.IPAddress = IPAddress;
        this.PortNo = PortNo;
        this.neighborID = neighborID;
    }

    public String getNeighborID() {
        return neighborID;
    }

    public void setNeighborID(String neighborID) {
        this.neighborID = neighborID;
    }
    
    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public int getPortNo() {
        return PortNo;
    }

    public void setPortNo(int PortNo) {
        this.PortNo = PortNo;
    }

    @Override
    public String toString() {
        return "Neighbor{" + "IPAddress=" + IPAddress + ", PortNo=" + PortNo + ", neighborID=" + neighborID + '}';
    }
    
    
    
    
}
