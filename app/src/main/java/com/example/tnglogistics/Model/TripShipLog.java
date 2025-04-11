package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "TripShipLog",
        primaryKeys = {"TripShipLogCode", "TripShipLogSeq"}
)
public class TripShipLog {
    @NonNull
    private String TripShipLogCode;
    @NonNull
    private int TripShipLogSeq;
    private int TripShipLogStatusCode;
    private String TripShipLogUpdate;
    private Double TripShipLogLat;
    private Double TripShipLogLong;
    private int TripShipLogEmpCode;
    public boolean isSynced;

    @NonNull
    public String getTripShipLogCode() {
        return TripShipLogCode;
    }

    public void setTripShipLogCode(@NonNull String tripShipLogCode) {
        TripShipLogCode = tripShipLogCode;
    }

    public int getTripShipLogSeq() {
        return TripShipLogSeq;
    }

    public void setTripShipLogSeq(int tripShipLogSeq) {
        TripShipLogSeq = tripShipLogSeq;
    }

    public int getTripShipLogStatusCode() {
        return TripShipLogStatusCode;
    }

    public void setTripShipLogStatusCode(int tripShipLogStatusCode) {
        TripShipLogStatusCode = tripShipLogStatusCode;
    }

    public String getTripShipLogUpdate() {
        return TripShipLogUpdate;
    }

    public void setTripShipLogUpdate(String tripShipLogUpdate) {
        TripShipLogUpdate = tripShipLogUpdate;
    }

    public Double getTripShipLogLat() {
        return TripShipLogLat;
    }

    public void setTripShipLogLat(Double tripShipLogLat) {
        TripShipLogLat = tripShipLogLat;
    }

    public Double getTripShipLogLong() {
        return TripShipLogLong;
    }

    public void setTripShipLogLong(Double tripShipLogLong) {
        TripShipLogLong = tripShipLogLong;
    }

    public int getTripShipLogEmpCode() {
        return TripShipLogEmpCode;
    }

    public void setTripShipLogEmpCode(int tripShipLogEmpCode) {
        TripShipLogEmpCode = tripShipLogEmpCode;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
