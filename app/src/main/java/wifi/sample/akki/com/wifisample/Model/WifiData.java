package wifi.sample.akki.com.wifisample.Model;

public class WifiData {
    private String Ssid, Bssid, connectionState, Rssi;

    public WifiData() {

    }

    public WifiData(String ssid, String bssid, String connectionState, String rssi) {
        this.Ssid = ssid;
        this.Bssid = bssid;
        this.connectionState = connectionState;
        this.Rssi = rssi;
    }

    public String getSsid() {
        return Ssid;
    }

    public void setSsid(String ssid) {
        Ssid = ssid;
    }

    public String getBssid() {
        return Bssid;
    }

    public void setBssid(String bssid) {
        this.Bssid = bssid;
    }

    public String getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(String connectionState) {
        this.connectionState = connectionState;
    }

    public String getRssi() {
        return Rssi;
    }

    public void setRssi(String rssi) {
        Rssi = rssi;
    }
}
