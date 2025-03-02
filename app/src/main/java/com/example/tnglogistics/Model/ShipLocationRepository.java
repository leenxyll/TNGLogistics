package com.example.tnglogistics.Model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.tnglogistics.Network.RetrofitClient;
import com.google.gson.Gson;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipLocationRepository {
    private static final String TAG = "Repository";
    private final ShipLocationDao shipLocationDao;

    public ShipLocationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.shipLocationDao = db.shipLocationDao();
    }

    public LiveData<List<ShipLocation>> getAllLocations() {
        return shipLocationDao.getAllLocations();
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
}
