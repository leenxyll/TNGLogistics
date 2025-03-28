package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvoice(Invoice invoice);

    @Query("SELECT * FROM Invoice")
    LiveData<List<Invoice>> getAllInvoice();

    @Query("SELECT * FROM Invoice GROUP BY ShipLoAddr ORDER BY ShipLoAddr ASC")
    LiveData<List<Invoice>> getInvoicesGroupedByLocation();

    @Query("DELETE FROM Invoice")
    void deleteAll();
}
