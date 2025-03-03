package com.example.tnglogistics.Network;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.Truck;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

public interface ApiService {
    @GET("/readshiplocation/all") // URL API ที่ใช้ดึงข้อมูล
    Call<List<ShipLocation>> getShipLocations();

    @POST("/CreateTruck")
    Call<Void> insertTruck(@Body Truck truck);

    @POST("/CreateShipLocation") // URL ตาม API ที่ Server กำหนด
    Call<Void> insertShipLocation(@Body ShipLocation shipLocation);
}
