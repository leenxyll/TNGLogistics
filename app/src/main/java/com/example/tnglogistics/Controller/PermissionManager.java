package com.example.tnglogistics.Controller;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionManager {
    private static final String TAG = "PermissionManager";

    //    ขอ Permission แบบใหม่ แทน requestPermissions() เพื่อขอหลายๆสิทธิ์พร้อมกัน
    private static ActivityResultLauncher<String[]> mPermissionResultLancher;
    private static boolean isFineLocationPermissionGranted = false;
    private static boolean isBackgroundLocationPermissionGranted = false;
    private static boolean isCameraPermissionGranted = false;
    private static boolean isPostNotificationPermissionGranted = false;

    public static void registerPermissionLauncher(ComponentActivity activity){
        Log.d(TAG, "Call registerPermissionLauncher");
        mPermissionResultLancher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        if(result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null){
                            isFineLocationPermissionGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                            Log.d(TAG, "FINE " + result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                        }
                        if(result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != null){
                            isBackgroundLocationPermissionGranted = result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                            Log.d(TAG, "BACKGROUND " + result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
                        }
                        if(result.get(Manifest.permission.CAMERA) != null){
                            isCameraPermissionGranted = result.get(Manifest.permission.CAMERA);
                            Log.d(TAG, "CAMERA " + result.get(Manifest.permission.CAMERA));
                        }
                        if(result.get(Manifest.permission.POST_NOTIFICATIONS) != null){
                            isPostNotificationPermissionGranted = result.get(Manifest.permission.POST_NOTIFICATIONS);
                            Log.d(TAG, "POST NOTI " + result.get(Manifest.permission.POST_NOTIFICATIONS));
                        }

                        // หลังจากขอ Foreground Location แล้ว ถ้ายังไม่ได้ Background Location ให้ขอแยก
                        if (isFineLocationPermissionGranted && !isBackgroundLocationPermissionGranted
                                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            showBackgroundLocationDialog(activity);
                        }

                    }
                });
    }

    public static void requestPermission(Activity activity){
        Log.d(TAG, "Call requestPermissions");
        isFineLocationPermissionGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        isBackgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;

        isPostNotificationPermissionGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;

        //เพิ่มลิสต์เพื่อขอ
        List<String> permissionRequest = new ArrayList<String>();

        if(!isFineLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(!isCameraPermissionGranted){
            permissionRequest.add(Manifest.permission.CAMERA);
        }

        if(!isPostNotificationPermissionGranted){
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

//        if (!isFineLocationPermissionGranted && retryCount < MAX_RETRY_COUNT) {
//            retryCount++;
//            new Handler(Looper.getMainLooper()).postDelayed(this::requestPermission, 2000); // ลองใหม่ทุก 2 วินาที
//        } else if (retryCount >= MAX_RETRY_COUNT) {
//            Log.e(TAG, "ขอสิทธิ์ FINE_LOCATION ไม่สำเร็จหลังจากลอง " + MAX_RETRY_COUNT + " ครั้ง");
//        }

        if(!permissionRequest.isEmpty()){
            //เรียกใช้ launch เพื่อขอสิทธิ์
            mPermissionResultLancher.launch(permissionRequest.toArray(new String[0]));
        } else {
            // ถ้า Foreground Location ได้รับอนุญาตแล้ว ค่อยขอ Background Location แยก
            // ขอ Background Location เฉพาะ Android 10+ เท่านั้น
            if (!isBackgroundLocationPermissionGranted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                showBackgroundLocationDialog(activity);
            }
        }
    }

    public static void showBackgroundLocationDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("ต้องการสิทธิ์การเข้าถึงตำแหน่งพื้นหลัง")
                .setMessage("แอปต้องการสิทธิ์เข้าถึงตำแหน่งของคุณในพื้นหลังเพื่อให้การติดตามตำแหน่งทำงานได้อย่างถูกต้อง")
                .setPositiveButton("อนุญาต", (dialog, which) -> {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        // ขอสิทธิ์ Background Location ถ้าผู้ใช้ยังไม่ได้เลือก "Don't ask again"
                        mPermissionResultLancher.launch(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
                    } else {
                        // ถ้าผู้ใช้เคยปฏิเสธก่อนหน้านี้ก็ขอ
                        showBackgroundLocationDialog(activity);
                    }
                })
                .show();
    }
}
