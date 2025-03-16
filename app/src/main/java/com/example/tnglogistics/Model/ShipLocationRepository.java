package com.example.tnglogistics.Model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tnglogistics.Network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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

    // ดึงเฉพาะข้อมูลที่ถูกสร้างใหม่
    public LiveData<List<ShipLocation>> getNewLocations(long lastUpdateTime) {
        return shipLocationDao.getNewLocations(lastUpdateTime);
    }

    // ใน ShipLocationRepository.java
    public void clearAllData() {
        // ล้างข้อมูลในฐานข้อมูล
        executorService.execute(() -> shipLocationDao.deleteAll());// สมมติว่าคุณมี DAO ที่มีเมธอด deleteAll()

        // หรือถ้าคุณมีการเชื่อมต่อกับ API
        // อาจจะต้องล้างข้อมูล cache หรือข้อมูลที่เก็บไว้ชั่วคราว
    }

//    // INSERT ข้อมูลเข้า Room Database
//    public void insert(ShipLocation shipLocation) {
//        executorService.execute(() -> shipLocationDao.insert(shipLocation));
//    }
//
//    public void delete(ShipLocation shipLocation){
//        executorService.execute(() -> shipLocationDao.delete(shipLocation));
//    }
//
//    // UPDATE ค่า GeofenceID
//    public void updateGeofenceID(int shipLoCode, String geofenceID) {
//        executorService.execute(() -> shipLocationDao.updateGeofenceID(shipLoCode, geofenceID));
//    }
//
//    public void updateLatLong(int shipLoCode, Double LatUpdateStatus, Double LongUpdateStatus){
//        executorService.execute(() -> shipLocationDao.updateLatLong(shipLoCode, LatUpdateStatus, LongUpdateStatus));
//    }

//    // ดึงข้อมูลจากเซิร์ฟเวอร์และบันทึกลง Room Database
//    public void fetchAndStoreLocations() {
//        RetrofitClient.getInstance().getApiService().getShipLocations().enqueue(new Callback<List<ShipLocation>>() {
//            @Override
//            public void onResponse(Call<List<ShipLocation>> call, Response<List<ShipLocation>> response) {
//                Log.d(TAG, "Response body: " + new Gson().toJson(response.body()));
//                if (response.isSuccessful() && response.body() != null) {
//                    new Thread(() -> {
//                        for (ShipLocation location : response.body()) {
//                            Log.d(TAG, "Raw JSON: " + response.raw().toString());
//                            Log.d(TAG, "Response body: " + new Gson().toJson(response.body()));
//                            Log.d(TAG, "location : " + location.getShipLoCode());
//                            shipLocationDao.insert(location);
//                        }
//                    }).start();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<ShipLocation>> call, Throwable t) {
//                Log.e(TAG, "Failed to fetch locations", t);
//            }
//        });
//    }

    public LiveData<Integer>  findOrCreateShipLocation(ShipLocation shipLocation){
        MutableLiveData<Integer> shipLoCodeLiveData = new MutableLiveData<>();

        RetrofitClient.getInstance().getApiService().getShipLocationByAddr(shipLocation.getShipLoAddr()).enqueue(new Callback<ShipLocation>() {
            @Override
            public void onResponse(Call<ShipLocation> call, Response<ShipLocation> response) {
                if(response.isSuccessful() && response.body() != null){
//                    Log.d(TAG, "Found Ship : " + response.body().getShipLoCode());
//                    Log.d(TAG, "Found Ship : " + response.body().getShipLoAddr());
                    int shipLoCode = response.body().getShipLoCode();
                    Log.d(TAG,"Old ShipLocation : "+shipLocation.getShipLoAddr() + ", " + shipLocation.getShipLoCode() + " : " + shipLocation.getShipLoAddr() );
                    shipLocation.setShipLoCode(shipLoCode);
                    Log.d(TAG,"Edit ShipLocation : "+shipLocation.getShipLoAddr() + ", " + shipLocation.getShipLoCode() + " : " + shipLocation.getShipLoAddr() );
                    executorService.execute(() -> shipLocationDao.insertShipLocation(shipLocation)); // ลง Room ด้วย ID เดียวกับ Server
                    Log.d(TAG,"Insert Success! : "+shipLocation.getShipLoAddr() + ", " + shipLocation.getShipLoCode() + " : " + shipLocation.getShipLoAddr() );
                    shipLoCodeLiveData.setValue(shipLoCode); // Set shipLoCode in LiveData
                } else {
                    Log.d(TAG, "Not Found Ship : " + response.body());
                    CreateShipLocation(shipLocation, shipLoCodeLiveData); // Pass LiveData to Create function
                }
            }

            @Override
            public void onFailure(Call<ShipLocation> call, Throwable t) {
                Log.e(TAG, "Error Ship: " + t.getMessage());
            }
        });
        return shipLoCodeLiveData;
    }

    public void CreateShipLocation(ShipLocation shipLocation, MutableLiveData<Integer> shipLoCodeLiveData){
        RetrofitClient.getInstance().getApiService().insertShipLocation(shipLocation).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    JsonObject responseBody = response.body();
                    int shipLoCode = responseBody.get("ShipLoCode").getAsInt(); // รับ TripID ที่ส่งกลับจาก Server
                    shipLocation.setShipLoCode(shipLoCode);
                    Log.d(TAG,"Insert To room New ShipLocation Code : "+shipLocation.getShipLoAddr() + ", " + shipLocation.getShipLoCode() + " : " + shipLocation.getShipLoAddr() );
                    executorService.execute(() -> shipLocationDao.insertShipLocation(shipLocation));
                    shipLoCodeLiveData.setValue(shipLoCode); // Set shipLoCode in LiveData
                } else {
                    Log.d(TAG,"Insert Failed: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error Ship: " + t.getMessage());
            }
        });
    }

    public LiveData<ShipLocation> getLocationByShipLoCode(int shipLocode){
        MutableLiveData<ShipLocation> shipLocationLiveData = new MutableLiveData<>();
        RetrofitClient.getInstance().getApiService().getShipLocationByCode(shipLocode).enqueue(new Callback<ShipLocation>() {
            @Override
            public void onResponse(Call<ShipLocation> call, Response<ShipLocation> response) {
                ShipLocation shipLocation = response.body();
                Log.d(TAG, "Found Ship: " + shipLocation.getShipLoCode() + " - " + shipLocation.getShipLoAddr());
                // อัปเดต LiveData
                shipLocationLiveData.postValue(shipLocation);
            }

            @Override
            public void onFailure(Call<ShipLocation> call, Throwable t) {

            }
        });

        return shipLocationLiveData;

    }
}
