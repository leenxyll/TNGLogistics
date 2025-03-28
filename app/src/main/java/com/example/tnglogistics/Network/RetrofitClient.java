package com.example.tnglogistics.Network;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private final ApiService apiService;



    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://508f-2001-fb1-a3-db8e-b449-7f5b-c76c-81c8.ngrok-free.app") // เปลี่ยนเป็น URL ของ API จริง
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create())) // รองรับค่า null
                .build();

        apiService = retrofit.create(ApiService.class);

//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .retryOnConnectionFailure(true) // ถ้าการเชื่อมต่อล้มเหลว ให้ลองใหม่อัตโนมัติ
//                .connectTimeout(30, TimeUnit.SECONDS) // ตั้งค่า Timeout 30 วินาที
//                .readTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://tangbot.tangnamglass.com:4443")
//                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()))
//                .client(okHttpClient) // ใช้ OkHttpClient ที่กำหนดเอง
//                .build();
//
//        apiService = retrofit.create(ApiService.class);
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
