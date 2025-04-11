package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "MileLog",
        primaryKeys = {"MileLogTripCode", "MileLogRow"}
)
public class MileLog {
    @NonNull
    private String MileLogTripCode;
    @NonNull
    private int MileLogRow;
    private int MileLogSeq;
    private int MileLogRecord;
    private Double MileLogLat;
    private Double MileLogLong;
    private String MileLogUpdate;
    private String MileLogPicPath;
    private int MileLogTypeCode;
    private int MileLogEmpCode;
    public boolean isSynced;
    public boolean isImageSynced;

    @NonNull
    public String getMileLogTripCode() {
        return MileLogTripCode;
    }

    public void setMileLogTripCode(@NonNull String mileLogTripCode) {
        MileLogTripCode = mileLogTripCode;
    }

    public int getMileLogRow() {
        return MileLogRow;
    }

    public void setMileLogRow(int mileLogRow) {
        MileLogRow = mileLogRow;
    }

    public int getMileLogSeq() {
        return MileLogSeq;
    }

    public void setMileLogSeq(int mileLogSeq) {
        MileLogSeq = mileLogSeq;
    }

    public int getMileLogRecord() {
        return MileLogRecord;
    }

    public void setMileLogRecord(int mileLogRecord) {
        MileLogRecord = mileLogRecord;
    }

    public Double getMileLogLat() {
        return MileLogLat;
    }

    public void setMileLogLat(Double mileLogLat) {
        MileLogLat = mileLogLat;
    }

    public Double getMileLogLong() {
        return MileLogLong;
    }

    public void setMileLogLong(Double mileLogLong) {
        MileLogLong = mileLogLong;
    }

    public String getMileLogUpdate() {
        return MileLogUpdate;
    }

    public void setMileLogUpdate(String mileLogUpdate) {
        MileLogUpdate = mileLogUpdate;
    }

    public String getMileLogPicPath() {
        return MileLogPicPath;
    }

    public void setMileLogPicPath(String mileLogPicPath) {
        MileLogPicPath = mileLogPicPath;
    }

    public int getMileLogTypeCode() {
        return MileLogTypeCode;
    }

    public void setMileLogTypeCode(int mileLogTypeCode) {
        MileLogTypeCode = mileLogTypeCode;
    }

    public int getMileLogEmpCode() {
        return MileLogEmpCode;
    }

    public void setMileLogEmpCode(int mileLogEmpCode) {
        MileLogEmpCode = mileLogEmpCode;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public boolean isImageSynced() {
        return isImageSynced;
    }

    public void setImageSynced(boolean imageSynced) {
        isImageSynced = imageSynced;
    }
}
