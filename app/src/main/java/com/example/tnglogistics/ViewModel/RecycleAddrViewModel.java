package com.example.tnglogistics.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tnglogistics.Model.AddrModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RecycleAddrViewModel extends ViewModel {
    private MutableLiveData<ArrayList<AddrModel>> itemList;

    public RecycleAddrViewModel() {
        itemList = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<ArrayList<AddrModel>> getItemList() {
        return itemList;
    }

    public void addItem(String name, LatLng latLng) {
        ArrayList<AddrModel> updatedList = itemList.getValue();
        updatedList.add(new AddrModel(name, latLng));
        itemList.setValue(updatedList);
    }

    public int getSize(){
        return itemList.getValue().size();
    }
}
