package com.example.tnglogistics.Model;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Invoice.class, InvoiceShipLog.class, MileLog.class, Employee.class, ShipmentPicture.class, SubIssue.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InvoiceDao invoiceDao();
    public abstract InvoiceShipLogDao invoiceShipLogDao();
    public abstract MileLogDao mileLogDao();
    public abstract EmployeeDao employeeDao();
    public abstract ShipmentPictureDao shipmentPictureDao();
    public abstract SubIssueDao subIssueDao();


    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "Mobile")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
