package com.example.tnglogistics.ViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Trip;
import com.example.tnglogistics.Model.TripRepository;
import com.example.tnglogistics.Model.Truck;

import java.util.List;

public class TripViewModel extends AndroidViewModel {

    private static TripViewModel instance;
    private static TripRepository repository;
    private final MediatorLiveData<List<Trip>> tripList = new MediatorLiveData<>();


    public TripViewModel(Application application) {
        super(application);
        repository = new TripRepository(application);
        tripList.addSource(repository.getAllTrip(), tripList::setValue);
    }

    public static TripViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new TripViewModel(application);
        }
        return instance;
    }

    public LiveData<List<Trip>> getTripList() {
        return tripList;
    }

    public LiveData<Integer> createTrip(int tripTruckCode){
        return repository.createTrip(tripTruckCode);
    }

    public void update(Trip trip){
        repository.update(trip);
    }

    // ฟังก์ชันดึง Trip ที่ตรงกับ Trip ที่เก็บใน SharedPreferences
    public LiveData<Trip> getTripByCodeFromSharedPreferences(Context context) {
        int tripCode = SharedPreferencesHelper.getTrip(context); // ดึง TripCode จาก SharedPreferences
        Log.d("Repository", "tripCode on sharepref: " + tripCode);

        return Transformations.map(getTripList(), trips -> {
            if (trips == null) return null;
            for (Trip trip : trips) {
                if (trip.getTripCode() == tripCode) {
                    Log.d("Repository", "Found Trip with tripCode: " + tripCode);
                    return trip;
                }
            }
            return null; // ถ้าไม่พบ Trip
        });
    }


}
