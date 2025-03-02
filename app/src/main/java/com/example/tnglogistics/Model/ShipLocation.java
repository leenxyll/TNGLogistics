package com.example.tnglogistics.Model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "ship_location")
public class ShipLocation {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("ShipLoCode")
    private int shipLoCode;
    @SerializedName("ShipLoLat")
    private Double shipLoLat;
    @SerializedName("ShipLoLong")
    private Double shipLoLong;
    @SerializedName("ShipLoAddr")
    private String shipLoAddr;
    @SerializedName("ShipLoAddr2")
    private String shipLoAddr2;
    private String geofenceID; // ใช้เป็น ID อ้างอิงสำหรับ Geofencing
    private String geofenceEvent; // เช่น ENTER, EXIT

    // Constructor
    public ShipLocation(int shipLoCode, double shipLoLat, double shipLoLong, String shipLoAddr,
                        String shipLoAddr2, String geofenceID, String geofenceEvent) {
        this.shipLoCode = shipLoCode;
        this.shipLoLat = shipLoLat;
        this.shipLoLong = shipLoLong;
        this.shipLoAddr = shipLoAddr;
        this.shipLoAddr2 = shipLoAddr2;
        this.geofenceID = geofenceID;
        this.geofenceEvent = geofenceEvent;
    }

    // Getter และ Setter
    public int getShipLoCode() { return shipLoCode; }
    public void setShipLoCode(int shipLoCode) { this.shipLoCode = shipLoCode; }

    public Double getShipLoLat() {
        if(shipLoLat != null){
            return shipLoLat.doubleValue();
        } else {
            return 0.0;
        }
    }
    public void setShipLoLat(Double shipLoLat) { this.shipLoLat = shipLoLat; }

    public Double getShipLoLong() {
        if(shipLoLat != null){
            return shipLoLat.doubleValue();
        } else {
            return 0.0;
        }
    }
    public void setShipLoLong(Double shipLoLong) { this.shipLoLong = shipLoLong; }

    public String getShipLoAddr() { return shipLoAddr; }
    public void setShipLoAddr(String shipLoAddr) { this.shipLoAddr = shipLoAddr; }

    public String getShipLoAddr2() { return shipLoAddr2; }
    public void setShipLoAddr2(String shipLoAddr2) { this.shipLoAddr2 = shipLoAddr2; }

    public String getGeofenceID() { return geofenceID; }
    public void setGeofenceID(String geofenceID) { this.geofenceID = geofenceID; }

    public String getGeofenceEvent() { return geofenceEvent; }
    public void setGeofenceEvent(String geofenceEvent) { this.geofenceEvent = geofenceEvent; }
}

