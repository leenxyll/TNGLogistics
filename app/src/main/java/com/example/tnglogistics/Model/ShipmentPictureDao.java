package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ShipmentPictureDao {
    @Query("SELECT COALESCE(MAX(ShipPicRow), 0) + 1 FROM Shipment_Picture WHERE ShipPicInvoiceCode = :invoiceCode")
    int getNextShipPicRow(String invoiceCode);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertShipPic(ShipmentPicture shipmentPicture);

    @Query("SELECT * FROM Shipment_Picture WHERE isSynced = false ORDER BY ShipPicUpdate ASC")
    List<ShipmentPicture> getUnsyncedShipPic();

    @Query("SELECT * FROM Shipment_Picture WHERE isImageSynced = false ORDER BY ShipPicUpdate ASC")
    List<ShipmentPicture> getUnsyncedShipPicImage();

    @Query("SELECT COUNT(*) FROM Shipment_Picture WHERE ShipPicInvoiceCode = :code AND ShipPicRow = :row")
    int checkIfLogExists(String code, int row);

    @Update
    void update(ShipmentPicture shipmentPicture);
}
