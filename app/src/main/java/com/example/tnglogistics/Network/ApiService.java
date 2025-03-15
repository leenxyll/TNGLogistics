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
    @POST("/mobile/CreateTruck")
    Call<JsonObject> insertTruck(@Body JsonObject params);

    @POST("/mobile/CreateTrip")
    Call<JsonObject> insertTrip(@Body JsonObject params);

    @GET("/mobile/GetTruck")
    Call<Truck> getTruck(@Query("TruckReg") String truckReg);

    @GET("/mobile/GetShipLocationByAddr")
    Call<ShipLocation> getShipLocationByAddr(@Query("ShipLoAddr") String shipLoAddr);
    @GET("/mobile/GetShipLocationByCode")
    Call<ShipLocation> getShipLocationByCode(@Query("ShipLoCode") int ShipLoCode);

    @POST("/mobile/CreateShipLocation") // URL ตาม API ที่ Server กำหนด
    Call<JsonObject> insertShipLocation(@Body ShipLocation shipLocation);

    @POST("/mobile/CreateShipmentList")
    Call<Void> insertShipmentList(@Body ShipmentList shipmentList);

    @PATCH("/mobile/UpdateShipmentList")
    Call<Void> updateShipmentList(@Body ShipmentList shipmentList);

    @PATCH("/mobile/UpdateTrip")
    Call<Void> updateTrip(@Body Trip trip);
}
