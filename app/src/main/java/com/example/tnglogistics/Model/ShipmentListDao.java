package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ShipmentListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertShipmentList(ShipmentList shipmentList);

    @Query("SELECT * FROM shipment_list WHERE geofenceID = :geofenceID")
    LiveData<List<ShipmentList>> getLocationByGeofenceID(String geofenceID);

    @Query("SELECT * FROM shipment_list")
    LiveData<List<ShipmentList>> getAllShipList();

    @Query("UPDATE shipment_list SET geofenceID = :geofenceID WHERE shipListSeq = :shipListSeq AND shipListShipLoCode = :shipListShipLoCode")
    void updateGeofenceID(int shipListSeq, int shipListShipLoCode, String geofenceID);

//    @Query("UPDATE shipment_list SET geofenceID = :geofenceID WHERE ShipListShipLoCode = :shipLoCode")
//    void updateGeofenceID(int shipLoCode, String geofenceID);

    @Query("UPDATE shipment_list SET LatUpdateStatus = :LatUpdateStatus, LongUpdateStatus = :LongUpdateStatus WHERE ShipListShipLoCode = :shipLoCode")
    void updateLatLong(int shipLoCode, Double LatUpdateStatus, Double LongUpdateStatus);

    @Query("SELECT * FROM shipment_list WHERE shipListTripCode = :ShipListTripCode")
    LiveData<List<ShipmentList>> getShipmentListByTripCode(int ShipListTripCode);


}
