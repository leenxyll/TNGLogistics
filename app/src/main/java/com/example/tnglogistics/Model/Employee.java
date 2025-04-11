package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Employee")
public class Employee {
    @PrimaryKey
    @NonNull
    private int EmpCode;
    private String EmpBrchCode;
    private String BrchName;
    private double BrchLat;
    private double BrchLong;
    private double Radius;

    public Employee() {
    }

    public Employee(int empCode, String empBrchCode, String brchName, double brchLat, double brchLong, double radius) {
        EmpCode = empCode;
        EmpBrchCode = empBrchCode;
        BrchName = brchName;
        BrchLat = brchLat;
        BrchLong = brchLong;
        Radius = radius;
    }

    public int getEmpCode() {
        return EmpCode;
    }

    public void setEmpCode(int empCode) {
        EmpCode = empCode;
    }

    public String getEmpBrchCode() {
        return EmpBrchCode;
    }

    public void setEmpBrchCode(String empBrchCode) {
        EmpBrchCode = empBrchCode;
    }

    public String getBrchName() {
        return BrchName;
    }

    public void setBrchName(String brchName) {
        BrchName = brchName;
    }

    public double getBrchLat() {
        return BrchLat;
    }

    public void setBrchLat(double brchLat) {
        BrchLat = brchLat;
    }

    public double getBrchLong() {
        return BrchLong;
    }

    public void setBrchLong(double brchLong) {
        BrchLong = brchLong;
    }

    public double getRadius() {
        return Radius;
    }

    public void setRadius(double radius) {
        Radius = radius;
    }
}
