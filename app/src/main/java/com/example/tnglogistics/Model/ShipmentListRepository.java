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

public class ShipmentListRepository {
    private static final String TAG = "Repository";
    private final ShipmentListDao shipmentListDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ShipmentListRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.shipmentListDao = db.shipmentListDao();
    }

    public void createShipmentList(int shipListSeq, int shipListTripCode, int shipListShipLoCode){
        ShipmentList newShipmentList = new ShipmentList(shipListSeq, shipListTripCode, shipListShipLoCode);
        RetrofitClient.getInstance().getApiService().insertShipmentList(newShipmentList).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                executorService.execute(() -> shipmentListDao.insertShipmentList(newShipmentList));
                Log.d(TAG,"Insert ShipmentList Success! : Seq: "+newShipmentList.getShipListSeq() + " Trip: " + newShipmentList.getShipListTripCode() + "ShipLoCode: " + newShipmentList.getShipListShipLoCode() );
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

    }

    public LiveData<List<ShipmentList>> getAllShipList(){
        return  shipmentListDao.getAllShipList();
    }

    public void update(ShipmentList shipmentList){
        RetrofitClient.getInstance().getApiService().updateShipmentList(shipmentList).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Update ShipmentList Success : "+shipmentList.getShipListSeq()+" Trip "+shipmentList.getShipListTripCode()+" : "+shipmentList.getLastUpdateStatus()+" : "+shipmentList.getLatUpdateStatus());
                executorService.execute(() -> shipmentListDao.update(shipmentList));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "ShipmentList Fail "+t);
            }
        });
    }

    public void updateGeofenceID(int shipListSeq, int shipListShipLoCode, String geofenceID){
        executorService.execute(() -> shipmentListDao.updateGeofenceID(shipListSeq, shipListShipLoCode, geofenceID));
    }

    public LiveData<Integer> getLocationByGeofenceID(String geofenceID){
        return shipmentListDao.getLocationByGeofenceID(geofenceID);
    }

}
