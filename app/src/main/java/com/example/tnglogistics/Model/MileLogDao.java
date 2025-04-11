package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MileLogDao {
    @Query("SELECT COALESCE(MAX(MileLogRow), 0) + 1 FROM MileLog WHERE MileLogTripCode = :tripCode")
    int getNextMileLogSeq(String tripCode);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMileLog(MileLog mileLog);

    @Query("SELECT * FROM MileLog WHERE isSynced = false ORDER BY MileLogUpdate ASC")
    List<MileLog> getUnsyncedMileLogs();

    @Query("SELECT * FROM MileLog WHERE isImageSynced = false ORDER BY MileLogUpdate ASC")
    List<MileLog> getUnsyncedMileLogsImage();

    @Query("SELECT COUNT(*) FROM MileLog WHERE MilelogTripCode = :code AND MileLogSeq = :seq")
    int checkIfLogExists(String code, int seq);


    @Query("SELECT * FROM MileLog WHERE MilelogTripCode = :code AND MileLogSeq = :seq LIMIT 1")
    MileLog getMileLogByCodeAndSeq(String code, int seq);

    @Update
    void update(MileLog mileLog);
}
