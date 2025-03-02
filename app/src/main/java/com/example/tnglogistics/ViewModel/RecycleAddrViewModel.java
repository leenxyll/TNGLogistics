package com.example.tnglogistics.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Model.AddrModel;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RecycleAddrViewModel extends AndroidViewModel {
    private static RecycleAddrViewModel instance;
    private MutableLiveData<ArrayList<AddrModel>> itemList = new MutableLiveData<>(new ArrayList<>());

    private RecycleAddrViewModel(Application application) {
        super(application);
    }

    public static RecycleAddrViewModel getInstance(Application application) {
        if (instance == null) {
           instance = new RecycleAddrViewModel(application);
        }
        return instance;
    }

    public LiveData<ArrayList<AddrModel>> getItemList() {
        return itemList;
    }

    public void addItem(String name, LatLng latLng) {
        ArrayList<AddrModel> updatedList = itemList.getValue();
        if (updatedList != null) {
            updatedList.add(new AddrModel(name, latLng));
            itemList.setValue(updatedList);
        }
    }

    public int getSize(){
        return itemList.getValue().size();
    }

    public void updateItemId(int index, String newId) {
        ArrayList<AddrModel> updatedList = itemList.getValue();
        if (updatedList != null && index >= 0 && index < updatedList.size()) {
            AddrModel item = updatedList.get(index);
            item.addGeofenceID(newId);  // เพิ่มเมธอดนี้ใน AddrModel
            itemList.setValue(updatedList);
        }
    }

    public AddrModel getItem(int index) {
        ArrayList<AddrModel> addrList = itemList.getValue();
        if (addrList != null && index >= 0 && index < addrList.size()) {
            return addrList.get(index);
        } else {
            return null; // คืนค่า null ถ้า index ไม่ถูกต้อง
        }
    }


}
