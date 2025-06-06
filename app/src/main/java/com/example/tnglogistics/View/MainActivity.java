package com.example.tnglogistics.View;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tnglogistics.Controller.NetworkReceiver;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityResultLauncher<Intent> cameraMileLauncher;

    private NetworkReceiver networkReceiver = new NetworkReceiver();

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
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, TAG +" onStart");
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
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
        cameraMileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String imagePath = result.getData().getStringExtra("image_path");
                        long imageTimestamp = result.getData().getLongExtra("image_timestamp", 0);
                        Invoice invoice = result.getData().getParcelableExtra("invoice");
                        openFragmentPreviewPicture(imagePath, imageTimestamp); // ส่งค่าไป FragmentB
                    }
                }
        );

//        // ลงทะเบียน Permission Launcher
        PermissionManager.registerPermissionLauncher(this);

        String lastFragment = SharedPreferencesHelper.getLastFragment(this);
        Log.d(TAG, lastFragment + " is lastFragment");
        Fragment fragment;

        if (savedInstanceState == null) {
            switch (lastFragment) {
                case "StatusFragment":
                    fragment = StatusFragment.newInstance();
                    break;
                    case "ShipDetailFragment":
                        fragment = ShipDetailFragment.newInstance();
                        break;
                default:
                    fragment = StatusFragment.newInstance();
                    Log.d(TAG, "Call DefaultFragment");
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

        }
    }

    // สร้าง getter method เพื่อให้ Fragment สามารถเข้าถึงได้
    public ActivityResultLauncher<Intent> getCameraMileLauncher() {
        return this.cameraMileLauncher;
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

}