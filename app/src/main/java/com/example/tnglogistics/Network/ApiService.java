package com.example.tnglogistics.Network;

import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.Model.Trip;
import com.example.tnglogistics.Model.Truck;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @POST("/CreateTruck")
    Call<JsonObject> insertTruck(@Body JsonObject params);

    @POST("/CreateTrip")
    Call<JsonObject> insertTrip(@Body JsonObject params);

    @GET("/GetTruck")
    Call<Truck> getTruck(@Query("TruckReg") String truckReg);

    @GET("/GetShipLocation")
    Call<ShipLocation> getShipLocation(@Query("ShipLoAddr") String shipLoAddr);

    @POST("/CreateShipLocation") // URL ตาม API ที่ Server กำหนด
    Call<JsonObject> insertShipLocation(@Body ShipLocation shipLocation);

    @POST("/CreateShipmentList")
    Call<Void> insertShipmentList(@Body ShipmentList shipmentList);

    @PATCH("/UpdateShipmentList")
    Call<Void> updateShipmentList(@Body ShipmentList shipmentList);

    @PATCH("/UpdateTrip")
    Call<Void> updateTrip(@Body Trip trip);
}
