package com.example.tnglogistics.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tnglogistics.Model.RecyclePlanModel;

import java.util.ArrayList;

public class RecyclePlanViewModel extends ViewModel {
    private MutableLiveData<ArrayList<RecyclePlanModel>> itemList;

    public RecyclePlanViewModel() {
        itemList = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<ArrayList<RecyclePlanModel>> getItemList() {
        return itemList;
    }

    public void addItem(String name) {
        ArrayList<RecyclePlanModel> updatedList = itemList.getValue();
        updatedList.add(new RecyclePlanModel(name));
        itemList.setValue(updatedList);
    }
}
