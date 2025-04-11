//package com.example.tnglogistics.Controller;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.Network;
//import android.os.IBinder;
//
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;
//import androidx.work.WorkRequest;
//
//public class MyService extends Service {
//    private ConnectivityManager.NetworkCallback networkCallback;
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        networkCallback = new ConnectivityManager.NetworkCallback() {
//            @Override
//            public void onAvailable(Network network) {
//                super.onAvailable(network);
//                triggerSyncWorker();  // เรียก SyncWorker ที่นี่
//            }
//        };
//        cm.registerDefaultNetworkCallback(networkCallback);
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (networkCallback != null) {
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            cm.unregisterNetworkCallback(networkCallback);  // ยกเลิกการลงทะเบียนเมื่อ Service ถูกทำลาย
//        }
//    }
//
//    private void triggerSyncWorker() {
//        // เรียก SyncWorker
//        WorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
//        WorkManager.getInstance(this).enqueue(syncRequest);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
