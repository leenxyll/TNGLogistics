package com.example.tnglogistics.Model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tnglogistics.Network.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripRepository {
    private static final String TAG = "Repository";
    private final TripDao tripDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TripRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.tripDao = db.tripDao();
    }

    public LiveData<List<Trip>> getAllTrip(){
        return  tripDao.getAllTrip();
    }

    public Trip getTripByTripCode(int tripCode){
        return tripDao.getTripByTripcode(tripCode);
    }

    public LiveData<Integer> createTrip(int tripTruckCode){
        MutableLiveData<Integer> tripCodeLiveData = new MutableLiveData<>();

        JsonObject params = new JsonObject();
        params.addProperty("TripTruckCode", tripTruckCode);
        RetrofitClient.getInstance().getApiService().insertTrip(params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject responseBody = response.body();
                int tripCode = responseBody.get("TripCode").getAsInt(); // รับ TripID ที่ส่งกลับจาก Server
                Log.d(TAG, "Insert Success! TripCode: " + tripCode);

//                Log.d(TAG,"Insert Success! : "+response );
//                Log.d(TAG,"Insert Success! : "+tripTruckCode + ", " + trip.getTripCode() + " : " + trip.getTripTruckCode() );

                Trip newTrip = new Trip(tripCode, tripTruckCode);
                executorService.execute(() -> tripDao.insertTrip(newTrip));
                // ส่ง tripCode กลับไปยัง ViewModel
                tripCodeLiveData.postValue(tripCode);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

        return tripCodeLiveData;  // ส่ง LiveData กลับ
    }

    public void update(Trip trip){
        RetrofitClient.getInstance().getApiService().updateTrip(trip).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                void update(int TripTruckCode, double TripMileageIn, double TripMileageOut, String TripTimeIn, String TripTimeOut, int TripCode);
//                executorService.execute(() -> tripDao.update(trip.getTripTruckCode(), trip.getTripMileageIn(), trip.getTripMileageOut(), trip.getTripTimeIn(), trip.getTripTimeOut(), trip.getTripCode()));
                executorService.execute(() -> tripDao.update(trip));
                Log.d(TAG, "Update Trip Success : "+trip.getTripCode()+" Out "+trip.getTripTimeOut()+", "+trip.getTripMileageOut()+" In "+trip.getTripTimeIn()+", "+trip.getTripMileageIn());
//                Log.d(TAG, "Update Trip Success : "+trip.getTripCode()+" In "+trip.getTripTimeIn()+", "+trip.getTripMileageIn());
                Log.e(TAG, response.message() + ", " + response.body() + ", " +response.raw()+ " ::: " +  response.toString());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Trip Fail "+t);
            }
        });
    }
}