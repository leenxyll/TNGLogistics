package com.example.tnglogistics.ViewModel;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tnglogistics.Model.AddrModel;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipLocationRepository;

import java.util.ArrayList;
import java.util.List;

public class ShipLocationViewModel extends AndroidViewModel {
    private static ShipLocationViewModel instance;
    private final ShipLocationRepository repository;
    private final LiveData<List<ShipLocation>> shipLocationList;


    public ShipLocationViewModel(Application application) {
        super(application);
        repository = new ShipLocationRepository(application);
        shipLocationList = repository.getAllLocations();
    }

    public static ShipLocationViewModel getInstance(Application application){
        if (instance == null) {
            instance = new ShipLocationViewModel(application);
        }
        return instance;
    }

    public LiveData<List<ShipLocation>> getShipLocationList() {
        return shipLocationList;
    }

    public void fetchLocationsFromServer() {
        repository.fetchAndStoreLocations();
    }

    public ShipLocation getItem(int index) {
        ArrayList<ShipLocation> shipLocations = (ArrayList<ShipLocation>) shipLocationList.getValue();
        if (shipLocations != null && index >= 0 && index < shipLocations.size()) {
            return shipLocations.get(index);
        } else {
            return null; // คืนค่า null ถ้า index ไม่ถูกต้อง
        }
    }

}
