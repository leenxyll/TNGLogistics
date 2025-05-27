package com.example.tnglogistics.Controller;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Receiver called " + intent.getAction());

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
        Log.d(TAG, "Geofence transition: " + transitionType);
        // ดึงพิกัดของจุดที่เกิดเหตุการณ์
        double latitude = geofencingEvent.getTriggeringLocation().getLatitude();
        double longitude = geofencingEvent.getTriggeringLocation().getLongitude();
        Log.d(TAG, "Triggered Location: Lat = " + latitude + ", Lng = " + longitude);



        for (Geofence geofence : geofenceList) {
            String geofenceId = geofence.getRequestId();
            Log.d(TAG, "onReceive: " + geofenceId);

            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
//                    Toast.makeText(context, "ใกล้ถึงแล้ว", Toast.LENGTH_SHORT).show();
//                    notificationHelper.sendHighPriorityNotification("ใกล้ถึงจุดจัดส่งแล้ว", "", MainActivity.class);
//                    shipmentListViewModel.updateShipmentStatus(geofenceId, "ENTER");
                    // เรียกใช้ WorkManager เพื่ออัปเดต InvoiceShipStatusCode
                    updateInvoiceStatus(context, geofenceId, latitude, longitude);
                    GeofenceHelper geofenceHelper = GeofenceHelper.getInstance(context);
                    if (geofenceHelper != null) {
                        Log.e(TAG, "GeofenceHelper remove geofence.");
                        geofenceHelper.removeGeofenceByID(geofenceId);
                    } else {
                        Log.e(TAG, "GeofenceHelper instance is null, cannot remove geofence.");
                    }
                    break;

                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    Log.d(TAG, "GEOFENCE_TRANSITION_DWELL");
//                    Toast.makeText(context, "ถึงแล้ว", Toast.LENGTH_SHORT).show();
//                    notificationHelper.sendHighPriorityNotification("ถึงจุดจัดส่งแล้ว", "", MainActivity.class);
//                    shipmentListViewModel.updateShipmentStatus(geofenceId, "DWELL");

//                    GeofenceHelper geofenceHelper = GeofenceHelper.getInstance(context);
//                    if (geofenceHelper != null) {
//                        Log.e(TAG, "GeofenceHelper remove geofence.");
//                        geofenceHelper.removeGeofenceByID(geofenceId);
//                    } else {
//                        Log.e(TAG, "GeofenceHelper instance is null, cannot remove geofence.");
//                    }
                    break;

                // กรณีถ้าต้องการใช้ EXIT ในอนาคต
//                case Geofence.GEOFENCE_TRANSITION_EXIT:
//                    Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
//                    Toast.makeText(context, "ออกจากพื้นที่ Geofence", Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    }

    private void updateInvoiceStatus(Context context, String geofenceId, double latitude, double longitude) {
        // สร้างงานสำหรับ WorkManager
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateWorker.class)
                .setInputData(new Data.Builder()
                        .putString("geofenceId", geofenceId)
                        .putDouble("latitude", latitude)
                        .putDouble("longitude", longitude)
                        .putLong("timestamp", System.currentTimeMillis())
                        .build())
                .build();

        // เรียกใช้ WorkManager เพื่อเริ่มงาน
        WorkManager.getInstance(context).enqueue(workRequest);
    }

}