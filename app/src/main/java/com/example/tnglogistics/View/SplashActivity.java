package com.example.tnglogistics.View;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.R;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIMEOUT = 2000; // 2 วินาที

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
//                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else {
                    // ถ้ายังไม่ล็อกอิน ไปยัง LoginActivity
//                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish(); // ปิด SplashActivity
            }
        }, SPLASH_TIMEOUT);
    }
}