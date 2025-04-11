package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvoice(Invoice invoice);

    @Query("SELECT * FROM Invoice ORDER BY ShipListSeq ASC ")
    LiveData<List<Invoice>> getAllInvoice();

    @Query("SELECT * From Invoice WHERE InvoiceCode = :InvoiceCode")
    Invoice getInvoiceByCode(String InvoiceCode);

    @Query("SELECT * From Invoice WHERE GeofenceID = :GeofenceID")
    Invoice getInvoiceByGeofence(String GeofenceID);

    @Update
    void update(Invoice invoice);

    // และเงื่อนไขที่
    @Query("SELECT * FROM Invoice WHERE InvoiceShipStatusCode IN (2, 3) ORDER BY ShipListSeq ASC LIMIT 1")
    LiveData<List<Invoice>> getInvoiceWithMinSeq();

    @Query("SELECT COUNT(*) FROM Invoice WHERE InvoiceShipStatusCode = 4")
    LiveData<Integer> countInvoicesWithStatusFour();

    @Query("SELECT COUNT(*) FROM Invoice WHERE InvoiceShipStatusCode = 5")
    LiveData<Integer> countInvoicesWithStatusFive();

//    @Query("SELECT * FROM Invoice GROUP BY ShipLoAddr ORDER BY ShipLoAddr ASC")
//    LiveData<List<Invoice>> getInvoicesGroupedByLocation();

    @Query("DELETE FROM Invoice")
    void deleteAll();
}
