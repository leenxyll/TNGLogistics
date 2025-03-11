package com.example.tnglogistics.View;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // ลงทะเบียน Permission Launcher
        PermissionManager.registerPermissionLauncher(this);
        // ขอสิทธิ์
        PermissionManager.requestPermission(this);


        SharedPreferencesHelper.saveLastFragment(this, "LoginDriverFragment");
        SharedPreferencesHelper.setUserLoggedIn(this, false);
        SharedPreferencesHelper.saveLastActivity(this,"");
        SharedPreferencesHelper.saveLastFragment(this,"");
        SharedPreferencesHelper.saveTruck(this, "");
        SharedPreferencesHelper.saveTrip(this, 0);
        String lastFragment = SharedPreferencesHelper.getLastFragment(this);
        Log.d(TAG, lastFragment + " is lastFragment");
        Fragment fragment = null;

        if (savedInstanceState == null) {
            switch (lastFragment) {
                case "PlanFragment":
                    fragment = PlanFragment.newInstance();
                    Log.d(TAG, TAG + " : new Instant : PlanFragment ");
                    break;
                case "PreviewPictureFragment":
                    fragment = PreviewPictureFragment.newInstance();
                    break;
                case "StatusFragment":
                    fragment = StatusFragment.newInstance();
                    break;
                case "LoginDriverFragment":
                    fragment = LoginDriverFragment.newInstance();
                    break;
                default:
                    fragment = LoginDriverFragment.newInstance();
                    Log.d(TAG, "Call DefaultFragment");
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}