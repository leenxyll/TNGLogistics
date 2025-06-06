package com.example.tnglogistics.Controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import android.net.NetworkCapabilities;
import androidx.work.ExistingWorkPolicy;


public class NetworkReceiver extends BroadcastReceiver {
    private static final String SYNC_WORK_NAME = "SyncWorker";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ ใช้ getActiveNetwork()
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = cm.getActiveNetwork();
            if (network != null) {
                triggerSyncWorker(context);
            }
        } else {
            // Android ต่ำกว่า 10 ใช้ NetworkInfo (Deprecated)
            if (isNetworkAvailable(context)) {
                triggerSyncWorker(context);
            }
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        }
        return false;
    }

    private void triggerSyncWorker(Context context) {
        WorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncDataWorker.class).build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, (OneTimeWorkRequest) syncRequest);
    }
}
