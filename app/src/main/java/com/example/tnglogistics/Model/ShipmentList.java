package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.google.gson.annotations.SerializedName;

@Entity(
        tableName = "shipment_list",
        primaryKeys = {"ShipListTripCode", "ShipListSeq"},
        foreignKeys = {
                @ForeignKey(entity = Trip.class,
                        parentColumns = "TripCode",
                        childColumns = "ShipListTripCode",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = ShipLocation.class,
                        parentColumns = "ShipLoCode",
                        childColumns = "ShipListShipLoCode",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class ShipmentList {
    @NonNull
    @SerializedName("ShipListSeq")
    public int ShipListSeq;

    @NonNull
    @SerializedName("ShipListTripCode")
    public int ShipListTripCode;

    @SerializedName("ShipListShipLoCode")
    public int ShipListShipLoCode;

    @SerializedName("ShipListStatus")
    private String ShipListStatus;

    @SerializedName("LatUpdateStatus")
    private Double LatUpdateStatus;

    @SerializedName("LongUpdateStatus")
    private Double LongUpdateStatus;

    @SerializedName("LastUpdateStatus")
    private String LastUpdateStatus;

    private String GeofenceID; // ใช้เป็น ID อ้างอิงสำหรับ Geofencing


    public ShipmentList() {
    }

    public ShipmentList(int shipListSeq, int shipListTripCode, int shipListShipLoCode) {
        ShipListSeq = shipListSeq;
        ShipListTripCode = shipListTripCode;
        ShipListShipLoCode = shipListShipLoCode;
    }

    public int getShipListSeq() {
        return ShipListSeq;
    }

    public void setShipListSeq(int shipListSeq) {
        ShipListSeq = shipListSeq;
    }

    public int getShipListTripCode() {
        return ShipListTripCode;
    }

    public void setShipListTripCode(int shipListTripCode) {
        ShipListTripCode = shipListTripCode;
    }

    public int getShipListShipLoCode() {
        return ShipListShipLoCode;
    }

    public void setShipListShipLoCode(int shipListShipLoCode) {
        ShipListShipLoCode = shipListShipLoCode;
    }

    public String getShipListStatus() {
        return ShipListStatus;
    }

    public void setShipListStatus(String shipListStatus) {
        ShipListStatus = shipListStatus;
    }

    public Double getLatUpdateStatus() {
        return LatUpdateStatus;
    }

    public void setLatUpdateStatus(Double latUpdateStatus) {
        LatUpdateStatus = latUpdateStatus;
    }

    public Double getLongUpdateStatus() {
        return LongUpdateStatus;
    }

    public void setLongUpdateStatus(Double longUpdateStatus) {
        LongUpdateStatus = longUpdateStatus;
    }

    public String getLastUpdateStatus() {
        return LastUpdateStatus;
    }

    public void setLastUpdateStatus(String lastUpdateStatus) {
        LastUpdateStatus = lastUpdateStatus;
    }

    public String getGeofenceID() {
        return GeofenceID;
    }

    public void setGeofenceID(String geofenceID) {
        GeofenceID = geofenceID;
    }
}
