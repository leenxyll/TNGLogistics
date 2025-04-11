package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TripShipLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTripShipLog(TripShipLog tripShipLog);

    @Query("SELECT * FROM TripShipLog WHERE isSynced = false ORDER BY TripShipLogUpdate ASC")
    List<TripShipLog> getUnsyncedStatusLogs();

    @Query("SELECT COUNT(*) FROM TripShipLog WHERE TripShipLogCode = :code AND TripShipLogSeq = :seq")
    int checkIfLogExists(String code, int seq);


    @Query("SELECT * FROM TripShipLog WHERE TripShipLogCode = :code AND TripShipLogSeq = :seq LIMIT 1")
    TripShipLog getTripShipLogByCodeAndSeq(String code, int seq);

    @Update
    void update(TripShipLog tripShipLog);
}
