package com.example.tnglogistics.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityResultLauncher<Intent> cameraLauncher;

    // initial fragment
    private PlanFragment plan_frag;

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferencesHelper.saveLastActivity(this, "MainActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // เหมาะกับ Android 10++ ปรับขนาด padding ให้ UI ไม่ถูกซ้อนทับโดย status bar และ navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // กำหนด cameraLauncher ใน Activity
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String imagePath = result.getData().getStringExtra("image_path");
                        long imageTimestamp = result.getData().getLongExtra("image_timestamp", 0);
                        openFragmentPreviewPicture(imagePath, imageTimestamp); // ส่งค่าไป FragmentB
                    }
                }
        );

        // ลงทะเบียน Permission Launcher
        PermissionManager.registerPermissionLauncher(this);
        // ขอสิทธิ์
        PermissionManager.requestPermission(this);

        // เริ่มติดตามตำแหน่ง
        startLocationService();

        String lastFragment = SharedPreferencesHelper.getLastFragment(this);
        Fragment fragment;

        switch(lastFragment){
            case "PlanFragment":
                fragment = new PlanFragment();
                break;
            case "PreviewPictureFragment":
                fragment = new PreviewPictureFragment();
                break;
            case "StatusFragment":
                fragment = new StatusFragment();
                break;
            default:
                fragment = new PlanFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }

    // สร้าง getter method เพื่อให้ Fragment สามารถเข้าถึงได้
    public ActivityResultLauncher<Intent> getCameraLauncher() {
        return this.cameraLauncher;
    }

    private void openFragmentPreviewPicture(String imagePath, long imageTimestamp) {
        PreviewPictureFragment frag_preview_pic = new PreviewPictureFragment();

        // ส่งค่าไปให้ FragmentB ผ่าน arguments
        Bundle args = new Bundle();
        args.putString("image_path", imagePath);
        args.putLong("image_timestamp", imageTimestamp);
        frag_preview_pic.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag_preview_pic)
                .addToBackStack(null)
                .commit();
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