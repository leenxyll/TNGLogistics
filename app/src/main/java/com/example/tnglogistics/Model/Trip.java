package com.example.tnglogistics.Model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "trip", foreignKeys = @ForeignKey(entity = Truck.class,
        parentColumns = "TruckCode", childColumns = "TripTruckCode"))
public class Trip {
    @PrimaryKey(autoGenerate = true)
    public int TripCode;

    @SerializedName("TripTruckCode")
    public int TripTruckCode;

    @SerializedName("TripMileageIn")
    public Double TripMileageIn;

    @SerializedName("TripMileageOut")
    public Double TripMileageOut;

    @SerializedName("TripTimeIn")
    public String TripTimeIn;

    @SerializedName("TripTimeOut")
    public String TripTimeOut;

    public Trip() {
    }

    public Trip(int tripTruckCode) {
        TripTruckCode = tripTruckCode;
    }
    public Trip(int tripCode, int tripTruckCode) {
        TripCode = tripCode;
        TripTruckCode = tripTruckCode;
    }

    public int getTripCode() {
        return TripCode;
    }

    public void setTripCode(int tripCode) {
        TripCode = tripCode;
    }

    public int getTripTruckCode() {
        return TripTruckCode;
    }

    public void setTripTruckCode(int tripTruckCode) {
        TripTruckCode = tripTruckCode;
    }

    public Double getTripMileageIn() {
        return TripMileageIn;
    }

    public void setTripMileageIn(Double tripMileageIn) {
        TripMileageIn = tripMileageIn;
    }

    public Double getTripMileageOut() {
        return TripMileageOut;
    }

    public void setTripMileageOut(Double tripMileageOut) {
        TripMileageOut = tripMileageOut;
    }

    public String getTripTimeIn() {
        return TripTimeIn;
    }

    public void setTripTimeIn(String tripTimeIn) {
        TripTimeIn = tripTimeIn;
    }

    public String getTripTimeOut() {
        return TripTimeOut;
    }

    public void setTripTimeOut(String tripTimeOut) {
        TripTimeOut = tripTimeOut;
    }
}