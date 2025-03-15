package com.example.tnglogistics.Controller;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.tnglogistics.R;
import com.example.tnglogistics.View.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private final IBinder binder = new LocalBinder();
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // สร้าง Notification channel สำหรับ Foreground Service (สำหรับ Android 8.0 ขึ้นไป)
        createNotificationChannel();

        // สร้าง FusedLocationProviderClient สำหรับติดตามตำแหน่ง
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "Create FLP");

        // สร้าง LocationCallback เพื่อติดตามตำแหน่ง
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        currentLocation = location;
                        // อัปเดตตำแหน่งที่ได้รับ
                        Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getTime());
                    }
                }
            }
        };

        // เริ่มการติดตามตำแหน่ง
        startLocationUpdates();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void startLocationUpdates() {
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setInterval(30000); // อัปเดตทุกๆ 30 วินาที
//        locationRequest.setFastestInterval(30000); // ความถี่สูงสุดที่สามารถอัปเดตได้
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // ตำแหน่งที่มีความแม่นยำสูง

        LocationRequest locationRequest = new LocationRequest.Builder(30000)  // อัปเดตทุกๆ 30 วินาที
                .setMinUpdateIntervalMillis(30000)  // ความถี่สูงสุดที่สามารถอัปเดตได้
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)  // ตำแหน่งที่มีความแม่นยำสูง
                .build();

        // ตรวจสอบว่าได้รับสิทธิ์การเข้าถึงตำแหน่งหรือไม่ (สำหรับ Android 6.0 ขึ้นไป)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // เริ่มติดตามตำแหน่ง
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "LocationServiceChannel",
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // สร้าง Notification สำหรับ Foreground Service
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "LocationServiceChannel")
//                .setContentTitle("Location Service")
//                .setContentText("Tracking location...")
                .setContentTitle("กำลังติดตามตำแหน่งการส่งสินค้า")
//                .setContentText("ระบบกำลังติดตามตำแหน่งของคุณเพื่อช่วยในการส่งสินค้า")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build();

        // ทำงานในโหมด Foreground Service
        startForeground(1, notification);

        // ให้บริการทำงานต่อไปจนกว่าจะถูกหยุด
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // หยุดการติดตามตำแหน่งเมื่อ Service ถูกทำลาย
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}