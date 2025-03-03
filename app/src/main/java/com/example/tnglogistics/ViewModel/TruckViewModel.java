package com.example.tnglogistics.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;

import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.Truck;
import com.example.tnglogistics.Model.TruckRepository;

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

    public void addTruck(String truckReg){
        Truck newTruck = new Truck(truckReg);
        repository.insertTruck(newTruck);
        repository.insertTruckToServer(newTruck);
    }

}
