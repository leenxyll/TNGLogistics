package com.example.tnglogistics.Model;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ShipLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertShipLocation(ShipLocation shipLocation);

//    @Delete
//    void deleteShipLocation(ShipLocation shipLocation);
//
//    @Query("SELECT * FROM ship_location WHERE geofenceID = :geofenceID")
//    ShipLocation getLocationByGeofenceID(String geofenceID);

    @Query("SELECT * FROM ship_location")
    LiveData<List<ShipLocation>> getAllLocations();

    @Query("SELECT * FROM ship_location WHERE createdAt > :lastUpdateTime")
    LiveData<List<ShipLocation>> getNewLocations(long lastUpdateTime);

    @Query("SELECT * FROM ship_location WHERE ShipLoCode > :ShipLoCode")
    LiveData<ShipLocation> getLocationByShipLoCode(int ShipLoCode);

//    @Query("UPDATE ship_location SET geofenceID = :geofenceID WHERE shipLoCode = :shipLoCode")
//    void updateGeofenceID(int shipLoCode, String geofenceID);
//
//    @Query("UPDATE ship_location SET LatUpdateStatus = :LatUpdateStatus, LongUpdateStatus = :LongUpdateStatus WHERE shipLoCode = :shipLoCode")
//    void updateLatLong(int shipLoCode, Double LatUpdateStatus, Double LongUpdateStatus);

}
