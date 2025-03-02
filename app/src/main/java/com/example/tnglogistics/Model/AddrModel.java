package com.example.tnglogistics.Model;

import com.google.android.gms.maps.model.LatLng;

public class AddrModel {
    private String addr;
    private LatLng latLng;
    private String geofenceID;

    public AddrModel(String addr, LatLng latLng) {
        this.addr = addr;
        this.latLng = latLng;
    }

    public void addGeofenceID(String ID){
        this.geofenceID = ID;
    }

    public String getAddr() {
        return addr;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
