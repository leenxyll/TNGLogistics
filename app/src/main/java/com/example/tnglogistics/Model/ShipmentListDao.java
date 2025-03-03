package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface ShipmentListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertShipmentList(ShipmentList shipmentList);

}
