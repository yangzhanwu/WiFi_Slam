package com.example.wifi_slam;

import com.google.ar.core.Anchor;

public class AnchorInfoBean {
    private String dataText;
    private Anchor anchor;
    private double length;
    public AnchorInfoBean(String dataText,Anchor anchor,double length){
        this.dataText=dataText;
        this.anchor=anchor;
        this.length=length;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public double getLength() {
        return length;
    }

    public String getDataText() {
        return dataText;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public void setDataText(String dataText) {
        this.dataText = dataText;
    }

    public void setLength(double length) {
        this.length = length;
    }
}
