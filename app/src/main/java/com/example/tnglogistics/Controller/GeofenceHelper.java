package com.example.tnglogistics.Controller;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GeofenceHelper {
    private static final String TAG = "GeofenceHelper";

    private float radius = 1000;
    private int transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL;
    private PendingIntent pendingIntent;
    private static GeofenceHelper instance;
    private Context context;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList = new ArrayList<>();

    private GeofenceHelper(Context context) {
        this.context = context.getApplicationContext();
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    public static GeofenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GeofenceHelper(context);
        }
        return instance;
    }

    // เพิ่ม geofence
    public void addGeofence(String ID, double latitude,double longitude) {
        Geofence geofence = new Geofence.Builder()
                .setCircularRegion(latitude, longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(10000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
        geofenceList.add(geofence);
        Log.d(TAG, "add geofence in List");
        requestGeofenceUpdates();
    }

    private void requestGeofenceUpdates() {
        if (geofenceList.isEmpty()) return;

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();
        Log.d(TAG,"Get Geofence Request");

        PendingIntent geofencePendingIntent = getGeofencePendingIntent();
        Log.d(TAG,"Get Pending Intent"+geofencePendingIntent);

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add Geofence", e));
    }

    // ลบ geofence
    public void removeGeofenceByID(String geofenceID) {
        List<String> geofenceIDs = new ArrayList<>();
        geofenceIDs.add(geofenceID);
        geofencingClient.removeGeofences(geofenceIDs)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence removed"))
                .addOnFailureListener(e -> Log.e(TAG, "Error removing geofence", e));
    }

    // ลบทั้งหมด
    public void removeAllGeofences(PendingIntent pendingIntent) {
        geofencingClient.removeGeofences(pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "All geofences removed"))
                .addOnFailureListener(e -> Log.e(TAG, "Error removing all geofences", e));
    }

    public PendingIntent getGeofencePendingIntent() {
        if (pendingIntent != null) {
            Log.d(TAG, "PendingIntent not null "+ pendingIntent);
            return pendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
//        intent.setAction("com.example.geofencing.GEOFENCE_ACTION");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        Log.d(TAG, "PendingIntent not null "+ pendingIntent);
        return pendingIntent;
    }
}
