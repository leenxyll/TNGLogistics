package com.example.tnglogistics.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.tnglogistics.Model.TripRepository;

public class TripViewModel extends AndroidViewModel {

    private static TripViewModel instance;
    private static TripRepository repository;


    public TripViewModel(Application application) {
        super(application);
        repository = new TripRepository(application);
    }

    public static TripViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new TripViewModel(application);
        }
        return instance;
    }
}
