package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TruckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTruck(Truck truck);

    @Query("SELECT * FROM truck")
    LiveData<List<Truck>> getAllTrucks();
}
