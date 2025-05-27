package com.example.tnglogistics.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "Shipment_Picture",
        primaryKeys = {"ShipPicInvoiceCode", "ShipPicRow"},
        foreignKeys = {
                @ForeignKey(entity = Invoice.class,
                        parentColumns = "InvoiceCode",
                        childColumns = "ShipPicInvoiceCode",
                        onDelete = ForeignKey.CASCADE),
        }
)
public class ShipmentPicture {
        @NonNull
        private String ShipPicInvoiceCode;
        @NonNull
        private int ShipPicRow;
        private int ShipPicSeq;
        private String ShipPicUpdate;
        private String ShipPicPath;
        private int ShipPicTypeCode;
        private boolean isSynced;
        private boolean isImageSynced;

        @NonNull
        public String getShipPicInvoiceCode() {
                return ShipPicInvoiceCode;
        }

        public void setShipPicInvoiceCode(@NonNull String shipPicInvoiceCode) {
                ShipPicInvoiceCode = shipPicInvoiceCode;
        }

        public int getShipPicRow() {
                return ShipPicRow;
        }

        public void setShipPicRow(int shipPicRow) {
                ShipPicRow = shipPicRow;
        }

        public int getShipPicSeq() {
                return ShipPicSeq;
        }

        public void setShipPicSeq(int shipPicSeq) {
                ShipPicSeq = shipPicSeq;
        }

        public String getShipPicUpdate() {
                return ShipPicUpdate;
        }

        public void setShipPicUpdate(String shipPicUpdate) {
                ShipPicUpdate = shipPicUpdate;
        }

        public String getShipPicPath() {
                return ShipPicPath;
        }

        public void setShipPicPath(String shipPicPath) {
                ShipPicPath = shipPicPath;
        }

        public int getShipPicTypeCode() {
                return ShipPicTypeCode;
        }

        public void setShipPicTypeCode(int shipPicTypeCode) {
                ShipPicTypeCode = shipPicTypeCode;
        }

        public boolean isSynced() {
                return isSynced;
        }

        public void setSynced(boolean synced) {
                isSynced = synced;
        }

        public boolean isImageSynced() {
                return isImageSynced;
        }

        public void setImageSynced(boolean imageSynced) {
                isImageSynced = imageSynced;
        }
}
