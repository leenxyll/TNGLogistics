package com.example.tnglogistics.Controller;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.tnglogistics.View.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;
import java.util.List;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Receiver called "+intent.getAction());
        Log.d(TAG, "Intent Action: " + intent.getAction());
        Log.d(TAG, "Intent Extras: " + intent.getExtras());

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.d(TAG, "Receiver geofence event "+geofencingEvent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }
//        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "", MainActivity.class);
//                callGeofenceEnterAPI();
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "GEOFENCE_TRANSITION_DWELL");
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", ShowLog.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "", ShowLog.class);
                break;
        }

    }

//    private void callGeofenceEnterAPI() {
//        OkHttpClient client = new OkHttpClient();
//
//        // สร้าง Request
//        Request request = new Request.Builder()
//                .url("https://7ba5-2001-fb1-a1-7eee-150f-6a1d-72d0-8e43.ngrok-free.app/GeofenceEnter") // URL ของ API
//                .get() // การเรียกแบบ GET
//                .build();
//
//        // ส่งคำขอแบบ Asynchronous
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "API call failed: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//                    Log.d(TAG, "API Response: " + responseData);
//                } else {
//                    Log.e(TAG, "API Response Error: " + response.code());
//                }
//            }
//        });
//    }
}