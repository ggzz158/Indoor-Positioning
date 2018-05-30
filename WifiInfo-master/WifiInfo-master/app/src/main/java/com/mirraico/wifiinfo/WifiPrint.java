package com.mirraico.wifiinfo;


public class WifiPrint implements Comparable {
    private String ssid;
    private int rssi;
    private String mac;

    public WifiPrint(String ssid, int rssi, String mac) {
        this.ssid = ssid;
        this.rssi = rssi;
        this.mac = mac;
    }

    @Override
    public int compareTo(Object another) {
        WifiPrint o = (WifiPrint) another;
        if(this.rssi > o.rssi || (this.rssi == o.rssi && this.ssid.compareTo(o.ssid) <= 0)) return -1;
        else return 1;
    }

    @Override
    public String toString() {
        return ssid + "(" + mac + ")" + "  RSSI: " + rssi + "dB" + "\r\n";
    }

    public String getSSID() {
        return ssid;
    }

    public int getRssi() {
        return rssi;
    }

    public String getMac() {
        return mac;
    }
}
