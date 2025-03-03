package com.example.tnglogistics.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.Model.ShipmentListRepository;
import java.util.List;

public class ShipmentListViewModel extends AndroidViewModel {

    private static ShipmentListViewModel instance;
    private static ShipmentListRepository repository;
    private final MediatorLiveData<List<ShipmentList>> shipmentList = new MediatorLiveData<>();


    public ShipmentListViewModel(Application application) {
        super(application);
        repository = new ShipmentListRepository(application);
    }

    public static ShipmentListViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new ShipmentListViewModel(application);
        }
        return instance;
    }
}
