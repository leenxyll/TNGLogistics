package com.example.tnglogistics.ViewModel;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.tnglogistics.Model.AddrModel;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipLocationRepository;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShipLocationViewModel extends AndroidViewModel {
    private static ShipLocationViewModel instance;
    private final ShipLocationRepository repository;
    private final MediatorLiveData<List<ShipLocation>> shipLocationList = new MediatorLiveData<>();
//    private final MutableLiveData<List<ShipLocation>> filteredShipLocationList = new MutableLiveData<>();
    private final MutableLiveData<Set<Integer>> filterCodes = new MutableLiveData<>(new HashSet<>()); // ใช้ Set เพื่อเก็บค่าหลายตัว


    public ShipLocationViewModel(Application application) {
        super(application);
        repository = new ShipLocationRepository(application);
//        shipLocationList.addSource(repository.getNewLocations(System.currentTimeMillis()), shipLocationList::setValue);
        shipLocationList.addSource(repository.getAllLocations(), shipLocationList::setValue);
    }

    public static ShipLocationViewModel getInstance(Application application){
        if (instance == null) {
            instance = new ShipLocationViewModel(application);
        }
        return instance;
    }

    // ใน ShipLocationViewModel
    public void resetData() {
        shipLocationList.setValue(new ArrayList<>());
        filterCodes.setValue(new HashSet<>());
    }

    // เพิ่มเมธอดนี้เพื่อรีเซ็ต singleton
    public static void resetInstance() {
        instance.shipLocationList.setValue(new ArrayList<>());
        instance.filterCodes.setValue(new HashSet<>());
        // ล้างข้อมูลในฐานข้อมูลผ่าน repository
        instance.repository.clearAllData();
        instance = null;
    }

// เมธอดแบบเดียวกันในอีก ViewModel

    public LiveData<List<ShipLocation>> getShipLocationList() {
        return shipLocationList;
    }

    public void setNewLocations(long currentTime) {
        // อัพเดต source ใหม่เมื่อมีการเปลี่ยนแปลงเวลา
        shipLocationList.addSource(repository.getNewLocations(currentTime), shipLocationList::setValue);
    }

//    public LiveData<List<ShipLocation>> getNewLocations(long currentTime){
//        return repository.getNewLocations(currentTime);
//    }

    public LiveData<ShipLocation> getShipLocationByCode(int shipLoCode) {
        return Transformations.map(getShipLocationList(), shipLocations -> {
            if (shipLocations == null) return null;
            for (ShipLocation shipLocation : shipLocations) {
                if (shipLocation.getShipLoCode() == shipLoCode) {
                    Log.d("Repository", "Found ShipLocation with code: " + shipLoCode);
                    return shipLocation;
                }
            }
            return null; // ถ้าไม่พบ ShipLocation
        });
    }

    // 📌 เพิ่มค่า shipLocationCode ที่ต้องการกรอง
    public void addFilterCode(int shipLocationCode) {
        Set<Integer> currentFilters = filterCodes.getValue();
        if (currentFilters != null) {
            currentFilters.add(shipLocationCode);
            filterCodes.setValue(new HashSet<>(currentFilters)); // อัปเดตค่าใหม่
        }
    }

    // 📌 ลบค่า shipLocationCode ออกจาก filter
    public void removeFilterCode(int shipLocationCode) {
        Set<Integer> currentFilters = filterCodes.getValue();
        if (currentFilters != null && currentFilters.contains(shipLocationCode)) {
            currentFilters.remove(shipLocationCode);
            filterCodes.setValue(new HashSet<>(currentFilters)); // อัปเดตค่าใหม่
        }
    }

    // ✅ ใช้ฟังก์ชันนี้แทนการ `addFilterCode()` ทีละรายการ
    public void setFilterCodes(Set<Integer> shipLocationCodes) {
        filterCodes.setValue(shipLocationCodes);
    }


    // 📌 กรองหลาย `shipLocationCode`
    public LiveData<List<ShipLocation>> getFilteredShipLocationList() {
        return Transformations.switchMap(filterCodes, codes ->
                Transformations.map(shipLocationList, locations -> {
                    if (locations == null || codes.isEmpty()) return new ArrayList<>();
                    List<ShipLocation> filteredList = new ArrayList<>();
                    for (ShipLocation location : locations) {
                        if (codes.contains(location.getShipLoCode())) {
                            filteredList.add(location);
                        }
                    }
                    return filteredList;
                })
        );
    }



//    public void fetchLocationsFromServer() {
//        repository.fetchAndStoreLocations();
//    }

//    // Method สำหรับ Insert ข้อมูล
//    public void insertShipLocationToServer(ShipLocation shipLocation) {
//        repository.insertShipLocationToServer(shipLocation);
//    }

    public void addLocation(String addr, LatLng latLng, long createdAt) {
        List<ShipLocation> updatedList = shipLocationList.getValue();
        if (updatedList != null) {
            ShipLocation newLocation = new ShipLocation(latLng.latitude, latLng.longitude, addr, createdAt);
            updatedList.add(newLocation);
            shipLocationList.setValue(updatedList);
//            repository.insert(newLocation);
//            updatedList.add(new AddrModel(name, latLng));
//            itemList.setValue(updatedList);
        }
    }

    public void removeLocation(ShipLocation shipLocation) {
        List<ShipLocation> currentList = shipLocationList.getValue();
        if (currentList != null) {
            List<ShipLocation> updatedList = new ArrayList<>(currentList); // สร้าง List ใหม่
            updatedList.remove(shipLocation);
            shipLocationList.setValue(updatedList); // อัปเดตค่าใหม่เข้าไป
            // 📌 ถ้าต้องการให้ลบออกจาก Database ด้วย ให้เรียก repository.delete(shipLocation);
        }
    }

    public LiveData<Integer> findOrCreateShipLocation(ShipLocation shipLocation){
        return repository.findOrCreateShipLocation(shipLocation);
    }


//    public void removeLocation(ShipLocation shipLocation){
//        repository.delete(shipLocation);
//    }

    public LiveData<ShipLocation> getLocationByShipLoCode(int shipLocode){
        return repository.getLocationByShipLoCode(shipLocode);
    }
}


