package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InvoiceShipLogDao {

    @Query("SELECT COALESCE(MAX(InvoiceShipLogSeq), 0) + 1 FROM Invoice_Ship_Log WHERE InvoiceShipLogCode = :InvoiceShipLogCode")
    int getNextInvoiceLogSeq(String InvoiceShipLogCode);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertShipLog(InvoiceShipLog shipLog);

    @Query("SELECT * FROM Invoice_Ship_Log WHERE isSynced = 0 ORDER BY InvoiceShipLogUpdate ASC")
    List<InvoiceShipLog> getUnsyncedInvoiceShipLogs();

//    @Query("UPDATE InvoiceShipLog SET isSynced = true WHERE InvoiceShipLogSeq = :InvoiceShipLogSeq AND InvoiceShipLogCode = :InvoiceCode")
//    void markAsSynced(int InvoiceShipLogSeq, String InvoiceCode);

    @Query("SELECT COUNT(*) FROM Invoice_Ship_Log WHERE InvoiceShipLogCode = :code AND InvoiceShipLogSeq = :seq")
    int checkIfLogExists(String code, int seq);


    @Query("SELECT * FROM Invoice_Ship_Log WHERE InvoiceShipLogCode = :code AND InvoiceShipLogSeq = :seq LIMIT 1")
    InvoiceShipLog getShipLogByCodeAndSeq(String code, int seq);

    @Update
    void update(InvoiceShipLog InvoiceShipLog);
}
