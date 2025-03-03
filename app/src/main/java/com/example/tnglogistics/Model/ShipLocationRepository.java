package com.example.tnglogistics.Model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.tnglogistics.Network.RetrofitClient;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipLocationRepository {
    private static final String TAG = "Repository";
    private final ShipLocationDao shipLocationDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ShipLocationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.shipLocationDao = db.shipLocationDao();
    }

    public LiveData<List<ShipLocation>> getAllLocations() {
        return shipLocationDao.getAllLocations();
    }

    // INSERT ข้อมูลเข้า Room Database
    public void insert(ShipLocation shipLocation) {
        executorService.execute(() -> shipLocationDao.insert(shipLocation));
    }

    // UPDATE ค่า GeofenceID
    public void updateGeofenceID(int shipLoCode, String geofenceID) {
        executorService.execute(() -> shipLocationDao.updateGeofenceID(shipLoCode, geofenceID));
    }

    public void updateLatLong(int shipLoCode, Double LatUpdateStatus, Double LongUpdateStatus){
        executorService.execute(() -> shipLocationDao.updateLatLong(shipLoCode, LatUpdateStatus, LongUpdateStatus));
    }

    // ดึงข้อมูลจากเซิร์ฟเวอร์และบันทึกลง Room Database
    public void fetchAndStoreLocations() {
        RetrofitClient.getInstance().getApiService().getShipLocations().enqueue(new Callback<List<ShipLocation>>() {
            @Override
            public void onResponse(Call<List<ShipLocation>> call, Response<List<ShipLocation>> response) {
                Log.d(TAG, "Response body: " + new Gson().toJson(response.body()));
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (ShipLocation location : response.body()) {
                            Log.d(TAG, "Raw JSON: " + response.raw().toString());
                            Log.d(TAG, "Response body: " + new Gson().toJson(response.body()));
                            Log.d(TAG, "location : " + location.getShipLoCode());
                            shipLocationDao.insert(location);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<ShipLocation>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch locations", t);
            }
        });
    }

    public void insertShipLocationToServer(ShipLocation shipLocation) {
        RetrofitClient.getInstance().getApiService().insertShipLocation(shipLocation).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG,"📌 Insert Success!");
                    // สามารถลบหรืออัปเดตข้อมูลใน Room ได้ (ถ้าจำเป็น)
                } else {
                    Log.d(TAG,"⚠️ Insert Failed: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "❌ Error: " + t.getMessage());
            }
        });
    }
}
