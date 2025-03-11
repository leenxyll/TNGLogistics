package com.example.tnglogistics.Controller;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.View.MainActivity;
import com.example.tnglogistics.ViewModel.ShipmentListViewModel;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";
    private ShipmentListViewModel shipmentListViewModel;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Receiver called " + intent.getAction());

        shipmentListViewModel = ShipmentListViewModel.getInstance((Application) context.getApplicationContext());
        NotificationHelper notificationHelper = new NotificationHelper(context);

        // ตรวจสอบ GeofencingEvent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        // ดึง Geofence ที่ถูกทริกเกอร์
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        if (geofenceList == null || geofenceList.isEmpty()) {
            Log.d(TAG, "onReceive: No geofences triggered");
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        for (Geofence geofence : geofenceList) {
            String geofenceId = geofence.getRequestId();
            Log.d(TAG, "onReceive: " + geofenceId);

            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
                    Toast.makeText(context, "ใกล้ถึงแล้ว", Toast.LENGTH_SHORT).show();
                    notificationHelper.sendHighPriorityNotification("ใกล้ถึงแล้ว", "Shipment ID: " + geofenceId, MainActivity.class);
                    shipmentListViewModel.updateShipmentStatus(geofenceId, "ใกล้ถึง");
                    break;

                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    Log.d(TAG, "GEOFENCE_TRANSITION_DWELL");
                    shipmentListViewModel.updateShipmentStatus(geofenceId, "ถึงแล้ว");

                    GeofenceHelper geofenceHelper = GeofenceHelper.getInstance(context);
                    if (geofenceHelper != null) {
                        geofenceHelper.removeGeofenceByID(geofenceId);
                    } else {
                        Log.e(TAG, "GeofenceHelper instance is null, cannot remove geofence.");
                    }
                    break;

                // กรณีถ้าต้องการใช้ EXIT ในอนาคต
//                case Geofence.GEOFENCE_TRANSITION_EXIT:
//                    Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
//                    Toast.makeText(context, "ออกจากพื้นที่ Geofence", Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    }
}