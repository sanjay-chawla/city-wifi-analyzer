package com.sanjaychawla.android.sensorapplication.DataObject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Record {
    // TODO: add some ID field
    private String timestamp;
    private double latitude;
    private double longitude;
    private String networkType;
    private double networkSpeed;

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

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
