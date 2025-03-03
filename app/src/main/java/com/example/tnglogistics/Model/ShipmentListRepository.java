package com.example.tnglogistics.Model;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShipmentListRepository {
    private static final String TAG = "Repository";
    private final ShipmentListDao shipmentListDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ShipmentListRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.shipmentListDao = db.shipmentListDao();
    }

    public void insertShipmentList(ShipmentList shipmentList){
        executorService.execute(() -> shipmentListDao.insertShipmentList(shipmentList));
    }

}
