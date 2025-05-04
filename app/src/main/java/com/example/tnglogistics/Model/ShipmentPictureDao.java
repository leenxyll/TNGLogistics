package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ShipmentPictureDao {
    @Query("SELECT COALESCE(MAX(ShipPicRow), 0) + 1 FROM shipment_picture WHERE ShipPicInvoiceCode = :invoiceCode")
    int getNextShipPicRow(String invoiceCode);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertShipPic(ShipmentPicture shipmentPicture);

    @Query("SELECT * FROM shipment_picture WHERE isSynced = false ORDER BY ShipPicUpdate ASC")
    List<MileLog> getUnsyncedShipPic();

    @Query("SELECT * FROM shipment_picture WHERE isImageSynced = false ORDER BY ShipPicUpdate ASC")
    List<MileLog> getUnsyncedShipPicImage();

    @Query("SELECT COUNT(*) FROM shipment_picture WHERE ShipPicInvoiceCode = :code AND ShipPicRow = :seq")
    int checkIfLogExists(String code, int seq);

    @Update
    void update(ShipmentPicture shipmentPicture);
}
