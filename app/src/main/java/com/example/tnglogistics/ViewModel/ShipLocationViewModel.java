package com.example.tnglogistics.ViewModel;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.tnglogistics.Model.AddrModel;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipLocationRepository;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ShipLocationViewModel extends AndroidViewModel {
    private static ShipLocationViewModel instance;
    private final ShipLocationRepository repository;
    private final MediatorLiveData<List<ShipLocation>> shipLocationList = new MediatorLiveData<>();


    public ShipLocationViewModel(Application application) {
        super(application);
        repository = new ShipLocationRepository(application);
        shipLocationList.addSource(repository.getAllLocations(), shipLocationList::setValue);
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

    // Method สำหรับ Insert ข้อมูล
    public void insertShipLocationToServer(ShipLocation shipLocation) {
        repository.insertShipLocationToServer(shipLocation);
    }

    public void addLocation(String addr, LatLng latLng) {
        List<ShipLocation> updatedList = shipLocationList.getValue();
        if (updatedList != null) {
            ShipLocation newLocation = new ShipLocation(latLng.latitude, latLng.longitude, addr);
            updatedList.add(newLocation);
            shipLocationList.setValue(updatedList);
            repository.insert(newLocation);
//            updatedList.add(new AddrModel(name, latLng));
//            itemList.setValue(updatedList);
        }
    }

    public void updateGeofenceID(int index, String newId, double latitude, double longitude) {
        List<ShipLocation> updatedList = shipLocationList.getValue();
        if (updatedList != null && index >= 0 && index < updatedList.size()) {
            ShipLocation location = updatedList.get(index);
            location.setGeofenceID(newId);
            location.setShipLoStatus("พร้อมจัดส่ง");
            location.setLatUpdateStatus(latitude);
            location.setLongUpdateStatus(longitude);
            shipLocationList.setValue(updatedList);
            repository.updateLatLong(index, latitude, longitude);
            repository.updateGeofenceID(index, newId);
            repository.insertShipLocationToServer(location);
        }
    }

    public ShipLocation getLocation(int index) {
        ArrayList<ShipLocation> shipLocations = (ArrayList<ShipLocation>) shipLocationList.getValue();
        if (shipLocations != null && index >= 0 && index < shipLocations.size()) {
            return shipLocations.get(index);
        } else {
            return null; // คืนค่า null ถ้า index ไม่ถูกต้อง
        }
    }

}
