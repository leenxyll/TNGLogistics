package com.example.tnglogistics.Controller;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.Model.InvoiceShipLog;
import com.example.tnglogistics.Model.MileLog;
import com.example.tnglogistics.Model.Repository;
import com.example.tnglogistics.View.MainActivity;

import java.util.List;
import java.util.concurrent.Executors;

public class UpdateWorker extends Worker {
    private static final String TAG = "UpdateWorker";
    private Repository repository;
    private Context context;

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        repository = new Repository(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "UpdateWorker started.");

        // ดึงข้อมูลจาก Geofence
        String geofenceId = getInputData().getString("geofenceId");
        double latitude = getInputData().getDouble("latitude", 0);
        double longitude = getInputData().getDouble("longitude", 0);
        long timestamp = getInputData().getLong("timestamp", 0);

        if (geofenceId != null) {
            updateInvoiceStatus(geofenceId, latitude, longitude, timestamp);
        } else {
            Log.e(TAG, "Geofence ID is null.");
            return Result.failure();
        }

        return Result.success();
    }

    private void updateInvoiceStatus(String geofenceId, double latitude, double longitude, long timestamp) {
        Log.d(TAG, "Updating invoice status for Geofence: " + geofenceId);

        // ดึง Invoice ทั้งหมดที่ใช้ Geofence ID เดียวกัน
        List<Invoice> invoices = repository.getInvoicesByGeofenceID(geofenceId);

        if (invoices != null && !invoices.isEmpty()) {
            NotificationHelper notificationHelper = new NotificationHelper(context);

            // ใช้ข้อมูลจาก Invoice แรกเพื่อแสดง notification
            Invoice firstInvoice = invoices.get(0);
            notificationHelper.sendHighPriorityNotification(
                    "ใกล้ถึง " + firstInvoice.getShipLoAddr() + " แล้ว",
                    "มี " + invoices.size() + " รายการที่ต้องจัดส่ง กรุณาเข้ามาที่แอปเพื่อยืนยันการถึงสถานที่จัดส่ง",
                    MainActivity.class
            );

            // Update สถานะของ Invoice ทั้งหมดในสถานที่เดียวกัน
            for (Invoice invoice : invoices) {
                // ตรวจสอบว่า Invoice ยังไม่เสร็จสิ้น (ไม่ใช่สถานะ 4 หรือ 5)
                if (invoice.getInvoiceShipStatusCode() != 4 && invoice.getInvoiceShipStatusCode() != 5) {
                    Log.d(TAG, "Updating invoice: " + invoice.getInvoiceCode() + " to status 3");

                    Executors.newSingleThreadExecutor().execute(() -> {
                        int nextSeq = repository.getNextInvoiceLogSeq(invoice.getInvoiceCode());
                        repository.updateInvoiceStatus(invoice, nextSeq, 3, latitude, longitude, timestamp, context);
                    });
                }
            }
        } else {
            Log.e(TAG, "No invoices found for Geofence: " + geofenceId);
        }
    }

//    private void updateInvoiceStatus(String geofenceId, double latitude, double longitude, long timestamp) {
//        Log.d(TAG, "Updating invoice status for Geofence: " + geofenceId);
//
//        Invoice invoice = repository.getInvoiceByGeofenceID(geofenceId);
//        NotificationHelper notificationHelper = new NotificationHelper(context);
//        notificationHelper.sendHighPriorityNotification("ใกล้ถึง"  + invoice.getShipLoAddr()+ "แล้ว", "เมื่อถึงสถานที่จัดส่งกรุณาเข้ามาที่แอปเพื่อยืนยันการถึงสถานที่จัดส่ง", MainActivity.class);
//        if (invoice != null) {
//            repository.updateInvoiceStatus(invoice, 3, 3, latitude, longitude, timestamp, context);
//        } else {
//            Log.e(TAG, "Invoice not found for Geofence: " + geofenceId);
//        }
//    }
}





