package com.example.tnglogistics.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.Model.ShipmentListRepository;
import com.example.tnglogistics.Model.Truck;

import java.util.ArrayList;
import java.util.List;

public class ShipmentListViewModel extends AndroidViewModel {

    private static ShipmentListViewModel instance;
    private static ShipmentListRepository repository;
    private final MediatorLiveData<List<ShipmentList>> shipmentList = new MediatorLiveData<>();


    public ShipmentListViewModel(Application application) {
        super(application);
        repository = new ShipmentListRepository(application);
        shipmentList.addSource(repository.getAllShipList(), shipmentList::setValue);
    }

    public static ShipmentListViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new ShipmentListViewModel(application);
        }
        return instance;
    }

    public void createShipmentList(int shipListSeq, int shipListTripCode, int shipListShipLoCode){
        repository.createShipmentList(shipListSeq, shipListTripCode, shipListShipLoCode);
    }

    public LiveData<List<ShipmentList>> getShipList(){
        return shipmentList;
    }

    // เพิ่มเมธอดสำหรับดึง shipmentList ตาม tripCode
    public LiveData<List<ShipmentList>> getShipmentListByTrip(int tripCode) {
        return Transformations.map(getShipList(), shipmentLists -> {
            if (shipmentLists == null) return new ArrayList<>();
            List<ShipmentList> filteredList = new ArrayList<>();
            for (ShipmentList shipmentList : shipmentLists) {
                if (shipmentList.getShipListTripCode() == tripCode) {
                    filteredList.add(shipmentList);
                }
            }
            return filteredList;
        });
    }
    public LiveData<Integer> getNearArrivalCount() {
        return Transformations.map(shipmentList, shipments -> {
            return (int) shipments.stream()
                    .filter(shipment -> "ใกล้ถึง".equals(shipment.getShipListStatus()))
                    .count();
        });
    }

    public LiveData<Integer> getShippedCount() {
        return Transformations.map(shipmentList, shipments -> {
            return (int) shipments.stream()
                    .filter(shipment -> "ถึงแล้ว".equals(shipment.getShipListStatus()))
                    .count();
        });
    }

    public void update(ShipmentList shipmentList){
        repository.update(shipmentList);
    }

    // ฟังก์ชันนี้จะทำการอัปเดตสถานะของ shipment โดยใช้ geofenceId
    public void updateShipmentStatus(String geofenceId, String status) {
        List<ShipmentList> currentList = shipmentList.getValue();
        if (currentList != null) {
            for (ShipmentList shipment : currentList) {
                if (shipment.getGeofenceID().equals(geofenceId)) {
                    shipment.setShipListStatus(status); // อัปเดตสถานะของ shipment
                    Log.d("Repository", "Status Update : "+shipment.getShipListStatus());
                    break;
                }
            }
            shipmentList.setValue(currentList); // อัปเดต LiveData
        }
    }


//    public void updateGeofenceID(int position, String geofenceID) {
//        List<ShipmentList> currentList = shipmentList.getValue();
//        if (currentList != null && position >= 0 && position < currentList.size()) {
//            ShipmentList shipment = currentList.get(position);
//            shipment.setGeofenceID(geofenceID);
//            repository.updateGeofenceID(shipment.getShipListSeq(), shipment.getShipListShipLoCode(), geofenceID);
////            shipment.setGeofenceID(geofenceID); // สมมติว่า ShipmentList มีเมธอด setGeofenceID
////            repository.updateShipmentList(shipment); // บันทึกการเปลี่ยนแปลงในฐานข้อมูล
//        }
//    }


    //    public void updateGeofenceID(int index, String newId, double latitude, double longitude) {
//        List<ShipLocation> updatedList = shipLocationList.getValue();
//        if (updatedList != null && index >= 0 && index < updatedList.size()) {
//            ShipLocation location = updatedList.get(index);
//            location.setGeofenceID(newId);
//            location.setShipLoStatus("พร้อมจัดส่ง");
//            location.setLatUpdateStatus(latitude);
//            location.setLongUpdateStatus(longitude);
//            shipLocationList.setValue(updatedList);
//            repository.updateLatLong(index, latitude, longitude);
//            repository.updateGeofenceID(index, newId);
//            repository.insertShipLocationToServer(location);
//        }
//    }
}
