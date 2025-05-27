package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "Invoice")
public class Invoice {
    private int ShipListSeq;
    private String TripCode;
    @PrimaryKey
    @NonNull
    private String InvoiceCode;
    private String InvoiceCusCode;
    private String CusName;
    private int InvoiceShipLoCode;
    private String ShipLoAddr;
    private double ShipLoLat;
    private double ShipLoLong;
    private String InvoiceReceiverName;
    private String InvoiceReceiverPhone;
    private String InvoiceNote;
//    private int InvoiceIssueCode;
    private int InvoiceShipStatusCode;
    private String InvoiceShipStatusLastUpdate;
    private Double LatUpdateStatus;
    private Double LongUpdateStatus;
    private String GeofenceID;
    private boolean isAddGeofence;

    public int getShipListSeq() {
        return ShipListSeq;
    }

    public void setShipListSeq(int shipListSeq) {
        ShipListSeq = shipListSeq;
    }

    public String getTripCode() {
        return TripCode;
    }

    public void setTripCode(String tripCode) {
        TripCode = tripCode;
    }

    public String getInvoiceCode() {
        return InvoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        InvoiceCode = invoiceCode;
    }

    public String getInvoiceCusCode() {
        return InvoiceCusCode;
    }

    public void setInvoiceCusCode(String invoiceCusCode) {
        InvoiceCusCode = invoiceCusCode;
    }

    public String getCusName() {
        return CusName;
    }

    public void setCusName(String cusName) {
        CusName = cusName;
    }

    public int getInvoiceShipLoCode() {
        return InvoiceShipLoCode;
    }

    public void setInvoiceShipLoCode(int invoiceShipLoCode) {
        InvoiceShipLoCode = invoiceShipLoCode;
    }

    public String getShipLoAddr() {
        return ShipLoAddr;
    }

    public void setShipLoAddr(String shipLoAddr) {
        ShipLoAddr = shipLoAddr;
    }

    public double getShipLoLat() {
        return ShipLoLat;
    }

    public void setShipLoLat(double shipLoLat) {
        ShipLoLat = shipLoLat;
    }

    public double getShipLoLong() {
        return ShipLoLong;
    }

    public void setShipLoLong(double shipLoLong) {
        ShipLoLong = shipLoLong;
    }

    public String getInvoiceReceiverName() {
        return InvoiceReceiverName;
    }

    public void setInvoiceReceiverName(String invoiceReceiverName) {
        InvoiceReceiverName = invoiceReceiverName;
    }

    public String getInvoiceReceiverPhone() {
        return InvoiceReceiverPhone;
    }

    public void setInvoiceReceiverPhone(String invoiceReceiverPhone) {
        InvoiceReceiverPhone = invoiceReceiverPhone;
    }

    public String getInvoiceNote() {
        return InvoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        InvoiceNote = invoiceNote;
    }

//    public int getInvoiceIssueCode() {
//        return InvoiceIssueCode;
//    }
//
//    public void setInvoiceIssueCode(int invoiceIssueCode) {
//        InvoiceIssueCode = invoiceIssueCode;
//    }

    public int getInvoiceShipStatusCode() {
        return InvoiceShipStatusCode;
    }

    public void setInvoiceShipStatusCode(int invoiceShipStatusCode) {
        InvoiceShipStatusCode = invoiceShipStatusCode;
    }

    public String getInvoiceShipStatusLastUpdate() {
        return InvoiceShipStatusLastUpdate;
    }

    public void setInvoiceShipStatusLastUpdate(String invoiceShipStatusLastUpdate) {
        InvoiceShipStatusLastUpdate = invoiceShipStatusLastUpdate;
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

    public String getGeofenceID() {
        return GeofenceID;
    }

    public void setGeofenceID(String geofenceID) {
        GeofenceID = geofenceID;
    }

    public boolean isAddGeofence() {
        return isAddGeofence;
    }

    public void setAddGeofence(boolean addGeofence) {
        isAddGeofence = addGeofence;
    }
}
