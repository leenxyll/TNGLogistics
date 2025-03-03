package com.example.tnglogistics.Model;
import android.icu.text.SimpleDateFormat;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "ship_location")
public class ShipLocation {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("ShipLoCode")
    private int ShipLoCode;
    @SerializedName("ShipLoLat")
    private Double ShipLoLat;
    @SerializedName("ShipLoLong")
    private Double ShipLoLong;
    @SerializedName("ShipLoAddr")
    private String ShipLoAddr;
    @SerializedName("ShipLoAddr2")
    private String ShipLoAddr2;
    @SerializedName("ShipLoStatus")
    private String ShipLoStatus;
    @SerializedName("LatUpdateStatus")
    private Double LatUpdateStatus;
    @SerializedName("LongUpdateStatus")
    private Double LongUpdateStatus;
    @SerializedName("LastUpdateStatus")
    private String LastUpdateStatus;
    private String GeofenceID; // ใช้เป็น ID อ้างอิงสำหรับ Geofencing

    public ShipLocation() {
    }

    public ShipLocation(double ShipLoLat, double ShipLoLong, String ShipLoAddr) {
        this.ShipLoLat = ShipLoLat;
        this.ShipLoLong = ShipLoLong;
        this.ShipLoAddr = ShipLoAddr;
    }

    // Getter และ Setter
    public int getShipLoCode() { return ShipLoCode; }
    public void setShipLoCode(int ShipLoCode) { this.ShipLoCode = ShipLoCode; }

    public Double getShipLoLat() {
        if(ShipLoLat != null){
            return ShipLoLat.doubleValue();
        } else {
            return 0.0;
        }
    }
    public void setShipLoLat(Double ShipLoLat) { this.ShipLoLat = ShipLoLat; }

    public Double getShipLoLong() {
        if(ShipLoLong != null){
            return ShipLoLong.doubleValue();
        } else {
            return 0.0;
        }
    }
    public void setShipLoLong(Double ShipLoLong) { this.ShipLoLong = ShipLoLong; }

    public String getShipLoAddr() { return ShipLoAddr; }
    public void setShipLoAddr(String ShipLoAddr) { this.ShipLoAddr = ShipLoAddr; }

    public String getShipLoAddr2() { return ShipLoAddr2; }
    public void setShipLoAddr2(String ShipLoAddr2) { this.ShipLoAddr2 = ShipLoAddr2; }

    public String getShipLoStatus() {
        return ShipLoStatus;
    }

    public void setShipLoStatus(String ShipLoStatus) {
        this.ShipLoStatus = ShipLoStatus;
    }

    public Double getLatUpdateStatus() {
        return LatUpdateStatus;
    }

    public void setLatUpdateStatus(Double LatUpdateStatus) {
        this.LatUpdateStatus = LatUpdateStatus;
    }

    public Double getLongUpdateStatus() {
        return LongUpdateStatus;
    }

    public void setLongUpdateStatus(Double LongUpdateStatus) {
        this.LongUpdateStatus = LongUpdateStatus;
    }

    public String getLastUpdateStatus() {
        return LastUpdateStatus;
    }

    public void setLastUpdateStatus(String LastUpdateStatus) {
        this.LastUpdateStatus = LastUpdateStatus;
    }

    public String getGeofenceID() { return GeofenceID; }
    public void setGeofenceID(String GeofenceID) { this.GeofenceID = GeofenceID; }

}

