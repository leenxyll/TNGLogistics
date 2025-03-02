package com.example.tnglogistics.Network;
import com.example.tnglogistics.Model.ShipLocation;
import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface ApiService {
    @GET("/readshiplocation/all") // URL API ที่ใช้ดึงข้อมูล
    Call<List<ShipLocation>> getShipLocations();
}
