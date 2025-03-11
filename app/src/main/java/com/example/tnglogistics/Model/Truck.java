package com.example.tnglogistics.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "TRUCK")
public class Truck {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("TruckCode")
    private int TruckCode;

    @SerializedName("TruckReg")
    private String TruckReg;

    public Truck() {
    }

    public Truck(int truckCode, String truckReg){
        TruckCode = truckCode;
        TruckReg = truckReg;
    }

    public Truck(String truckReg) {
        TruckReg = truckReg;
    }

    public int getTruckCode() {
        return TruckCode;
    }

    public void setTruckCode(int truckCode) {
        TruckCode = truckCode;
    }

    public String getTruckReg() {
        return TruckReg;
    }

    public void setTruckReg(String truckReg) {
        TruckReg = truckReg;
    }
}
