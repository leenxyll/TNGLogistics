package com.example.tnglogistics.Model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.tnglogistics.Network.RetrofitClient;
import com.google.gson.JsonObject;

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

//    public void insertTruck(Truck truck){
//        executorService.execute(() -> truckDao.insertTruck(truck));
//    }

    public LiveData<List<Truck>> getAllTrucks(){
        return  truckDao.getAllTrucks();
    }

    public void  findOrCreateTruck(String truckReg){
        RetrofitClient.getInstance().getApiService().getTruck(truckReg).enqueue(new Callback<Truck>() {
            @Override
            public void onResponse(Call<Truck> call, Response<Truck> response) {
                if(response.isSuccessful() && response.body() != null){
                    Log.d(TAG, "Found Truck : " + response.body().getTruckCode());
                    Log.d(TAG, "Found Truck : " + response.body().getTruckReg());
                    Truck truck = response.body();
                    executorService.execute(() -> truckDao.insertTruck(truck));
                    Log.d(TAG,"Insert Success! : "+truckReg + ", " + truck.getTruckCode() + " : " + truck.getTruckReg() );
                } else {
                    Log.d(TAG, "Not Found Truck : " + response.body());
                    CreateTruck(truckReg);
                }
            }

            @Override
            public void onFailure(Call<Truck> call, Throwable t) {
                Log.e(TAG, "Error Truck: " + t.getMessage());
            }
        });
    }

    public void CreateTruck(String truckReg){
        JsonObject params = new JsonObject();
        params.addProperty("TruckReg", truckReg);
//        Truck newTruck = new Truck(truckReg);
        RetrofitClient.getInstance().getApiService().insertTruck(params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    JsonObject responseBody = response.body();
                    int truckCode = responseBody.get("TruckCode").getAsInt(); // รับ TripID ที่ส่งกลับจาก Server
                    Truck newTruck = new Truck(truckCode, truckReg);
                    executorService.execute(() -> truckDao.insertTruck(newTruck));
                    Log.d(TAG, "Insert Success! TruckCode: " + newTruck.getTruckCode() +", "+newTruck.getTruckReg());
                } else {
                    Log.d(TAG,"Insert Truck Failed: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error Truck: " + t.getMessage());
            }
        });
    }

//    public void insertTruckToServer(Truck truck) {
//        RetrofitClient.getInstance().getApiService().insertTruck(truck).enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG,"📌 Insert Success!");
//                    // สามารถลบหรืออัปเดตข้อมูลใน Room ได้ (ถ้าจำเป็น)
//                } else {
//                    Log.d(TAG,"⚠️ Insert Failed: " + response.errorBody());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Log.e(TAG, "❌ Error: " + t.getMessage());
//            }
//        });
//    }
}
