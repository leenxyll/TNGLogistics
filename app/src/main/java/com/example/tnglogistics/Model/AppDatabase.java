package com.example.tnglogistics.Model;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {ShipLocation.class, Truck.class, Trip.class, ShipmentList.class, Invoice.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ShipLocationDao shipLocationDao();
    public abstract TruckDao truckDao();
    public abstract TripDao tripDao();
    public abstract ShipmentListDao shipmentListDao();
    public abstract InvoiceDao invoiceDao();


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
