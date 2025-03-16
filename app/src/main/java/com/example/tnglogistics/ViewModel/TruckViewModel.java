package com.example.tnglogistics.ViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.Truck;
import com.example.tnglogistics.Model.TruckRepository;

import java.util.ArrayList;
import java.util.List;

public class TruckViewModel extends AndroidViewModel {
    private static TruckViewModel instance;
    private static TruckRepository repository;
    private final MediatorLiveData<List<Truck>> truckList = new MediatorLiveData<>();

    public TruckViewModel(Application application) {
        super(application);
        repository = new TruckRepository(application);
        truckList.addSource(repository.getAllTrucks(), truckList::setValue);

    }

    public static TruckViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new TruckViewModel(application);
        }
        return instance;
    }

    public static void resetInstance() {
        instance.truckList.setValue(new ArrayList<>());
        instance = null;
    }

    // ใน TruckViewModel.java
    public void resetData() {
        truckList.setValue(new ArrayList<>());
    }

    public LiveData<List<Truck>> getTruckList() {
        return truckList;
    }


//    public void addTruck(String truckReg){
//        Truck newTruck = new Truck(truckReg);
//        repository.insertTruck(newTruck);
//        repository.insertTruckToServer(newTruck);
//    }

    public void findOrCreateTruck(String truckReg){
        repository.findOrCreateTruck(truckReg);
    }

    // ฟังก์ชันดึง Truck ที่มี TruckReg ตรงกับที่เก็บใน SharedPreferences
    public LiveData<Truck> getTruckByRegFromSharedPreferences(Context context) {
        String truckReg = SharedPreferencesHelper.getTruck(context); // ดึงทะเบียนจาก SharedPreferences
        MediatorLiveData<Truck> result = new MediatorLiveData<>();

        getTruckList().observeForever(trucks -> {
            if (trucks != null && !trucks.isEmpty()) {
                for (Truck truck : trucks) {
                    if (truck.getTruckReg().equals(truckReg)) {
                        result.setValue(truck); // ถ้าพบ Truck ที่ตรงกับทะเบียน
                        break;
                    }
                }
            }
        });

        return result;
    }

}
