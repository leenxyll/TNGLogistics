package com.example.tnglogistics.Controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.tnglogistics.Model.MileLog;
import com.example.tnglogistics.Model.Repository;

import java.util.List;

public class SyncImageWorker extends Worker {
    private static final String TAG = "SyncImageWorker";
    private Repository repository;
    private Context context;

    public SyncImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        repository = new Repository(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncImageWorker started.");

        if (!repository.isNetworkAvailable()) {
            Log.e(TAG, "No internet connection. Image Sync postponed.");
            return Result.retry();
        }

        List<MileLog> unsyncedMileLogsImage = repository.getUnsyncedMileLogsImage();
        for (MileLog log : unsyncedMileLogsImage) {
            repository.syncMileLogPicture(log);
        }

        return Result.success();
    }
}
