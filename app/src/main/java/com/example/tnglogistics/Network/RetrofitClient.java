package com.example.tnglogistics.Network;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private final ApiService apiService;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://9005-2001-fb1-a1-2cad-f007-4c20-b279-2de8.ngrok-free.app ") // เปลี่ยนเป็น URL ของ API จริง
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create())) // รองรับค่า null
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
