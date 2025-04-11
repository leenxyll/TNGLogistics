package com.example.tnglogistics.Controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.tnglogistics.Model.InvoiceShipLog;
import com.example.tnglogistics.Model.MileLog;
import com.example.tnglogistics.Model.Repository;
import java.util.List;

public class SyncDataWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private Repository repository;
    private Context context;

    public SyncDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        repository = new Repository(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker started.");

        int TripShipUpdateEmp = SharedPreferencesHelper.getEmployee(context);

        if (!repository.isNetworkAvailable()) {
            Log.e(TAG, "No internet connection. Sync postponed.");
            return Result.retry(); // ถ้าไม่มีการเชื่อมต่อเน็ต ให้ลองใหม่
        }

        // ดึงข้อมูลที่ยังไม่ได้ซิงค์จากฐานข้อมูล
        List<InvoiceShipLog> unsyncedShipLogs = repository.getUnsyncedInvoiceShipLogs();
        for (InvoiceShipLog log : unsyncedShipLogs) {
            repository.syncInvoiceShipLog(log);
        }

        List<MileLog> unsyncedMileLogs = repository.getUnsyncedMileLogs();
        for (MileLog log : unsyncedMileLogs) {
            repository.synMilelogData(TripShipUpdateEmp, log);
        }

        return Result.success();
    }
}