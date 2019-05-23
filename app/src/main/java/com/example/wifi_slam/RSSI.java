package com.example.wifi_slam;

import org.litepal.crud.LitePalSupport;

public class RSSI extends LitePalSupport {
    private long id;
    public RSSI(){
        this.id=getBaseObjId();
    }
    public long getId() {
        return id;
    }


    private int level;
    private String SSID;
    private WiFi_info wiFi_info;

    public WiFi_info getWiFi_info() {
        return wiFi_info;
    }

    public void setWiFi_info(WiFi_info wiFi_info) {
        this.wiFi_info = wiFi_info;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }
}
