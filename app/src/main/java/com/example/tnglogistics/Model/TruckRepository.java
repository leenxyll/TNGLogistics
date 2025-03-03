package com.example.tnglogistics.Model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.tnglogistics.Network.RetrofitClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TruckRepository {
    private static final String TAG = "Repository";
    private final TruckDao truckDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TruckRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.truckDao = db.truckDao();
    }

    public void insertTruck(Truck truck){
        executorService.execute(() -> truckDao.insertTruck(truck));
    }

    public LiveData<List<Truck>> getAllTrucks(){
        return  truckDao.getAllTrucks();
    }

    public void insertTruckToServer(Truck truck) {
        RetrofitClient.getInstance().getApiService().insertTruck(truck).enqueue(new Callback<Void>() {
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
