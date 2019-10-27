package com.sanjaychawla.android.sensorapplication.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Record {
    private String timestamp;
    private double latitude;
    private double longitude;
    private String networkType;
    private double networkSpeed;
    private boolean wifiRecognised;
    private String ssid;
    private float distanceFromRouter;
    private int wiFiStrength;

    public Record(){}

    public Record(String currentTime, double latitude, double longitude, String networkType, double speed) {
        this.timestamp = currentTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.networkSpeed = speed;
        this.networkType = networkType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public double getNetworkSpeed() {
        return networkSpeed;
    }

    public void setNetworkSpeed(double networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    public boolean isWifiRecognised() {
        return wifiRecognised;
    }

    public void setWifiRecognised(boolean wifiRecognised) {
        this.wifiRecognised = wifiRecognised;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public float getDistanceFromRouter() {
        return distanceFromRouter;
    }

    public void setDistanceFromRouter(float distanceFromRouter) {
        this.distanceFromRouter = distanceFromRouter;
    }

    public int getWiFiStrength() {
        return wiFiStrength;
    }

    public void setWiFiStrength(int wiFiStrength) {
        this.wiFiStrength = wiFiStrength;
    }

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
