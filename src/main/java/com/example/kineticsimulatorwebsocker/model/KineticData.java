package com.example.kineticsimulatorwebsocker.model;

/**
 * 动力学数据模型
 */
public class KineticData {
    private String id;
    private String type;
    private String x;
    private String y;
    private String z;
    private String a;
    private String d;
    private String vx;
    private String vy;

    public KineticData() {}

    public KineticData(String id, String type, String x, String y, String z, String a, String d, String vx, String vy) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = a;
        this.d = d;
        this.vx = vx;
        this.vy = vy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getVx() {
        return vx;
    }

    public void setVx(String vx) {
        this.vx = vx;
    }

    public String getVy() {
        return vy;
    }

    public void setVy(String vy) {
        this.vy = vy;
    }
} 