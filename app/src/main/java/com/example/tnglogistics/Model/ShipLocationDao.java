package com.example.tnglogistics.Model;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ShipLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShipLocation shipLocation);

    @Query("SELECT * FROM ship_location WHERE geofenceID = :geofenceID")
    ShipLocation getLocationByGeofenceID(String geofenceID);

    @Query("SELECT * FROM ship_location")
    LiveData<List<ShipLocation>> getAllLocations();

    @Query("UPDATE ship_location SET geofenceID = :geofenceID WHERE shipLoCode = :shipLoCode")
    void updateGeofenceID(int shipLoCode, String geofenceID);

    @Query("UPDATE ship_location SET LatUpdateStatus = :LatUpdateStatus, LongUpdateStatus = :LongUpdateStatus WHERE shipLoCode = :shipLoCode")
    void updateLatLong(int shipLoCode, Double LatUpdateStatus, Double LongUpdateStatus);

}
