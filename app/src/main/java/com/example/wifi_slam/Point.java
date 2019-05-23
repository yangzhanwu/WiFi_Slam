package com.example.wifi_slam;

public class Point {
    private float x;
    private float y;
    private float z;
    public static float cameraX=0;
    public static float cameraY=0;
    public static float cameraZ=0;

    public Point(float x, float y, float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public  float getX(){
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
