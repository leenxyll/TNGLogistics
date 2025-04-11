package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "InvoiceShipLog",
        primaryKeys = {"InvoiceShipLogCode", "InvoiceShipLogSeq"},
        foreignKeys = {
                @ForeignKey(entity = Invoice.class,
                        parentColumns = "InvoiceCode",
                        childColumns = "InvoiceShipLogCode",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class InvoiceShipLog {
    @NonNull
    private String InvoiceShipLogCode;
    @NonNull
    private int InvoiceShipLogSeq;
    private int InvoiceShipLogStatusCode;
    private String InvoiceShipLogUpdate;
    private Double InvoiceShipLogLat;
    private Double InvoiceShipLogLong;
    private int InvoiceShipLogEmpCode;
    public boolean isSynced;

    public String getInvoiceShipLogCode() {
        return InvoiceShipLogCode;
    }

    public void setInvoiceShipLogCode(String invoiceShipLogCode) {
        InvoiceShipLogCode = invoiceShipLogCode;
    }

    public int getInvoiceShipLogSeq() {
        return InvoiceShipLogSeq;
    }

    public void setInvoiceShipLogSeq(int invoiceShipLogSeq) {
        InvoiceShipLogSeq = invoiceShipLogSeq;
    }

    public int getInvoiceShipLogStatusCode() {
        return InvoiceShipLogStatusCode;
    }

    public void setInvoiceShipLogStatusCode(int invoiceShipLogStatusCode) {
        InvoiceShipLogStatusCode = invoiceShipLogStatusCode;
    }

    public String getInvoiceShipLogUpdate() {
        return InvoiceShipLogUpdate;
    }

    public void setInvoiceShipLogUpdate(String invoiceShipLogUpdate) {
        InvoiceShipLogUpdate = invoiceShipLogUpdate;
    }

    public Double getInvoiceShipLogLat() {
        return InvoiceShipLogLat;
    }

    public void setInvoiceShipLogLat(Double invoiceShipLogLat) {
        InvoiceShipLogLat = invoiceShipLogLat;
    }

    public Double getInvoiceShipLogLong() {
        return InvoiceShipLogLong;
    }

    public void setInvoiceShipLogLong(Double invoiceShipLogLong) {
        InvoiceShipLogLong = invoiceShipLogLong;
    }

    public int getInvoiceShipLogEmpCode() {
        return InvoiceShipLogEmpCode;
    }

    public void setInvoiceShipLogEmpCode(int invoiceShipLogEmpCode) {
        InvoiceShipLogEmpCode = invoiceShipLogEmpCode;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
