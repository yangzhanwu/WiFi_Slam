package com.example.wifi_slam;

import android.net.wifi.ScanResult;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class WiFi_info extends LitePalSupport {
    private long id;
    public WiFi_info(){
        this.id=getBaseObjId();
    }
    public long getId() {
        return id;
    }


    private float x;
    private float y;
    private float z;
    private List<RSSI> scanResults=new ArrayList<RSSI>();
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public List<RSSI> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<RSSI> scanResults) {
        this.scanResults = scanResults;
    }
}
