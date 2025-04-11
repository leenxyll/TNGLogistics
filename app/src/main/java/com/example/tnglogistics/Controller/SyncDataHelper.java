package com.example.tnglogistics.Controller;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SyncDataHelper {
    public static void startSyncDataWhenConnected(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // ต้องมีเน็ตเท่านั้น
                .build();

        OneTimeWorkRequest syncRequest =
                new OneTimeWorkRequest.Builder(SyncDataWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context)
                .enqueue(syncRequest);
    }
}
