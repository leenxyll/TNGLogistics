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
}
