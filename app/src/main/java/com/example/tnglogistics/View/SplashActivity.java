package com.example.tnglogistics.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Switch;

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

        Log.d(TAG, "Login " +SharedPreferencesHelper.isUserLoggedIn(SplashActivity.this));
                if (SharedPreferencesHelper.isUserLoggedIn(SplashActivity.this)) {
                    Log.d(TAG, "Login call main " +SharedPreferencesHelper.isUserLoggedIn(SplashActivity.this));
                    // ถ้ามีข้อมูลผู้ใช้ที่ล็อกอินแล้ว ไปยัง MainActivity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    Log.d(TAG, "Login call Login " +SharedPreferencesHelper.isUserLoggedIn(SplashActivity.this));
                    // ถ้ายังไม่ล็อกอิน ไปยัง LoginActivity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
        finish();
    }

}