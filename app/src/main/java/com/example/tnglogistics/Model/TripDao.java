package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrip(Trip trip);

    @Query("SELECT * FROM trip")
    LiveData<List<Trip>> getAllTrip();

    @Update
    void update(Trip trip);

//    @Query("UPDATE TRIP SET TripTruckCode = :TripTruckCode, TripMileageIn = :TripMileageIn, TripMileageOut = :TripMileageOut, TripTimeIn = :TripTimeIn, TripTimeOut = :TripTimeOut WHERE TripCode = :TripCode")
//    void update(int TripTruckCode, double TripMileageIn, double TripMileageOut, String TripTimeIn, String TripTimeOut, int TripCode);


}
