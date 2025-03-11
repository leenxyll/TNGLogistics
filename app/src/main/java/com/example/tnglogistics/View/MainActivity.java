package com.example.tnglogistics.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.example.tnglogistics.ViewModel.TripViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TruckViewModel truckViewModel;

    private ActivityResultLauncher<Intent> cameraLauncher;

    // initial fragment
//    private PlanFragment plan_frag;

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferencesHelper.saveLastActivity(this, "MainActivity");
        Log.d(TAG, TAG +" onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveLastActivity(this, "MainActivity");
        Log.d(TAG, TAG +" onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastActivity(this, "MainActivity");
        Log.d(TAG, TAG +" onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, TAG +" onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TAG +" onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, TAG +" onCreate");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TAG +" onCreate");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView imgview_logo_navbar = findViewById(R.id.imgview_logo_navbar);

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

        truckViewModel = TruckViewModel.getInstance(getApplication());

        truckViewModel.getTruckByRegFromSharedPreferences(this).observe(this, truck -> {
            if (truck != null) {
                Toast.makeText(this, "ลงทะเบียนด้วยทะเบียนรถ : " + truck.getTruckReg() + " : " + truck.getTruckCode(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Trip : "+ SharedPreferencesHelper.getTrip(this), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ไม่พบทะเบียนรถที่ตรงกับข้อมูลที่เก็บไว้", Toast.LENGTH_SHORT).show();
            }
        });

//        // ลงทะเบียน Permission Launcher
//        PermissionManager.registerPermissionLauncher(this);
//        // ขอสิทธิ์
//        PermissionManager.requestPermission(this);

        // เริ่มติดตามตำแหน่ง
        startLocationService();

        String lastFragment = SharedPreferencesHelper.getLastFragment(this);
        Log.d(TAG, lastFragment + " is lastFragment");
        Fragment fragment;

        if (savedInstanceState == null) {
            switch (lastFragment) {
                case "PlanFragment":
                    fragment = PlanFragment.newInstance();
                    Log.d(TAG, TAG + " : new Instant : PlanFragment ");
                    break;
//                case "PreviewPictureFragment":
//                    fragment = PreviewPictureFragment.newInstance();
//                    break;
                case "StatusFragment":
                    fragment = StatusFragment.newInstance();
                    break;
                default:
                    fragment = PlanFragment.newInstance();
                    Log.d(TAG, "Call DefaultFragment");
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

        }
        imgview_logo_navbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHelper.setUserLoggedIn(MainActivity.this, false);
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(intent); // เรียก startActivity() เพื่อเปิด Activity ใหม่
                finish(); // ปิด Fragment หรือ Activity ปัจจุบัน (ถ้าต้องการ)
            }
        });
//            finish();
    }

    // สร้าง getter method เพื่อให้ Fragment สามารถเข้าถึงได้
    public ActivityResultLauncher<Intent> getCameraLauncher() {
        return this.cameraLauncher;
    }

    private void openFragmentPreviewPicture(String imagePath, long imageTimestamp) {
        PreviewPictureFragment frag_preview_pic = PreviewPictureFragment.newInstance();

        // ส่งค่าไปให้ FragmentB ผ่าน arguments
        Bundle args = new Bundle();
        args.putString("image_path", imagePath);
        args.putLong("image_timestamp", imageTimestamp);
        frag_preview_pic.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag_preview_pic)
//                .addToBackStack(null)
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