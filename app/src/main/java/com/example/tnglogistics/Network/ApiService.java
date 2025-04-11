package com.example.tnglogistics.Network;

import com.example.tnglogistics.Model.InvoiceShipLog;
import com.example.tnglogistics.Model.MileLog;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.Model.Trip;
import com.example.tnglogistics.Model.Truck;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @POST("/auth/login")
    Call<JsonObject> login(@Body JsonObject credentials); // เพิ่ม method login

    @GET("/auth/getBranchLocation") // เพิ่ม endpoint นี้
    Call<JsonObject> getBranchLocation(@Query("BrchCode") String branchCode);

    @GET("/driver/trip/getShipmentList")
    Call<JsonObject> getShipmentList(@Query("EmpCode") int EmpCode);

    @POST("/driver/invoice/updateStatus")
    Call<JsonObject> updateInvoiceStatus(@Body InvoiceShipLog log);

//    @POST("/driver/trip/updateStatus")
//    Call<JsonObject>

    @POST("/driver/mileLog/insertData")
    Call<JsonObject> insertMileLogData(@Body JsonObject mileLog);

    @Multipart
    @POST("/driver/mileLog/uploadPic")
    Call<JsonObject> updateMileLogPic(
            @Part MultipartBody.Part image,
            @Part("MileLogTripCode") RequestBody MileLogTripCode,
            @Part("MileLogRecord") RequestBody MileLogRecord,
            @Part("MileLogUpdate") RequestBody MileLogUpdate,
            @Part("MileLogSeq") RequestBody MileLogSeq
    );
}
