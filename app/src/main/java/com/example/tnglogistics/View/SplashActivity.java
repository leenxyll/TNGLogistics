package com.example.tnglogistics.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_TIMEOUT = 200; // 0.2 วินาที

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPreferencesHelper.isUserLoggedIn(SplashActivity.this)) {
                    // ถ้ามีข้อมูลผู้ใช้ที่ล็อกอินแล้ว ไปยัง HomeActivity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    // เริ่มติดตามตำแหน่ง
                    startLocationService();
                } else {
                    // ถ้ายังไม่ล็อกอิน ไปยัง LoginActivity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish(); // ปิด SplashActivity
            }
        }, SPLASH_TIMEOUT);
    }

    private void startLocationService() {
        // เริ่ม startForegroundService เพื่อเริ่ม LocationService
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent); // ใช้ startForegroundService สำหรับ Android 8.0 (API 26) ขึ้นไป
        } else {
            startService(serviceIntent); // ใช้ startService สำหรับเวอร์ชันเก่ากว่า
        }
    }
}