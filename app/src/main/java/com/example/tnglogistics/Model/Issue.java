package com.example.tnglogistics.Model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Issue")
public class Issue {
    @PrimaryKey(autoGenerate = true)
    private int issueRow;
    private int issueCode;
    private String issueDescription;
    private int issueQty;
    private Double issueLat;
    private Double issueLong;
    private long issueCreated;
    private int issueSubCode;
    private boolean isSynced;

    public Issue(String issueDescription, int issueQty, Double issueLat, Double issueLong, long issueCreated, int issueSubCode, boolean isSynced) {
        this.issueDescription = issueDescription;
        this.issueQty = issueQty;
        this.issueLat = issueLat;
        this.issueLong = issueLong;
        this.issueCreated = issueCreated;
        this.issueSubCode = issueSubCode;
        this.isSynced = isSynced;
    }

    public int getIssueRow() {
        return issueRow;
    }

    public void setIssueRow(int issueRow) {
        this.issueRow = issueRow;
    }

    public int getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(int issueCode) {
        this.issueCode = issueCode;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public int getIssueQty() {
        return issueQty;
    }

    public void setIssueQty(int issueQty) {
        this.issueQty = issueQty;
    }

    public Double getIssueLat() {
        return issueLat;
    }

    public void setIssueLat(Double issueLat) {
        this.issueLat = issueLat;
    }

    public Double getIssueLong() {
        return issueLong;
    }

    public void setIssueLong(Double issueLong) {
        this.issueLong = issueLong;
    }

    public long getIssueCreated() {
        return issueCreated;
    }

    public void setIssueCreated(long issueCreated) {
        this.issueCreated = issueCreated;
    }

    public int getIssueSubCode() {
        return issueSubCode;
    }

    public void setIssueSubCode(int issueSubCode) {
        this.issueSubCode = issueSubCode;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
