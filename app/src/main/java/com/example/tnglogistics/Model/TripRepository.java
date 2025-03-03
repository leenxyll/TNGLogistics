package com.example.tnglogistics.Model;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripRepository {
    private static final String TAG = "Repository";
    private final TripDao tripDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TripRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.tripDao = db.tripDao();
    }

    public void insertTrip(Trip trip){
        executorService.execute(() -> tripDao.insertTrip(trip));
    }
}