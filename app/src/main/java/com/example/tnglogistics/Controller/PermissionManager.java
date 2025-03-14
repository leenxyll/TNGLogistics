package com.example.tnglogistics.Controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
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

    private static ActivityResultLauncher<String[]> permissionLauncher;
    private static ActivityResultLauncher<String> backgroundPermissionLauncher;

    private static boolean isFineLocationGranted = false;
    private static boolean isBackgroundLocationGranted = false;
    private static boolean isCameraGranted = false;
    private static boolean isNotificationGranted = false;

    public static void registerPermissionLauncher(ComponentActivity activity) {
        Log.d(TAG, "Registering permission launchers");

        // Launcher สำหรับขอ permissions ปกติ
        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> result) -> {
                    if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                        isFineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        Log.d(TAG, "FINE_LOCATION granted: " + isFineLocationGranted);
                    }
                    if (result.get(Manifest.permission.CAMERA) != null) {
                        isCameraGranted = result.get(Manifest.permission.CAMERA);
                        Log.d(TAG, "CAMERA granted: " + isCameraGranted);
                    }
                    if (result.get(Manifest.permission.POST_NOTIFICATIONS) != null) {
                        isNotificationGranted = result.get(Manifest.permission.POST_NOTIFICATIONS);
                        Log.d(TAG, "POST_NOTIFICATIONS granted: " + isNotificationGranted);
                    }

                    // หลังจากขอ Fine Location สำเร็จ ให้ขอ Background Location แยก
                    if (isFineLocationGranted && !isBackgroundLocationGranted) {
                        requestBackgroundLocation(activity);
                    }
                }
        );

        // Launcher สำหรับขอ Background Location
        backgroundPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                (Boolean isGranted) -> {
                    isBackgroundLocationGranted = isGranted;
                    Log.d(TAG, "BACKGROUND_LOCATION granted: " + isBackgroundLocationGranted);
                }
        );
    }

    public static void requestPermissions(Activity activity) {
        Log.d(TAG, "Requesting permissions");

        isFineLocationGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        isBackgroundLocationGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        isCameraGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        isNotificationGranted = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionsToRequest = new ArrayList<>();

        if (!isFineLocationGranted) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!isCameraGranted) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        if (!isNotificationGranted) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            // ถ้าได้รับ FINE_LOCATION แล้วแต่ยังไม่ได้ BACKGROUND_LOCATION ให้ขอแยก
            if (isFineLocationGranted && !isBackgroundLocationGranted) {
                requestBackgroundLocation(activity);
            }
        }
    }

    private static void requestBackgroundLocation(Activity activity) {
        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            // แสดง Dialog อธิบายก่อนขอ
            new AlertDialog.Builder(activity)
                    .setTitle("ต้องการสิทธิ์ตำแหน่งพื้นหลัง")
                    .setMessage("แอปต้องการสิทธิ์ตำแหน่งพื้นหลังเพื่อให้การติดตามการส่งสินค้าได้ หากคุณปฏิเสธจะไม่สามารถติดตามการส่งสินค้าได้")
                    .setPositiveButton("อนุญาต", (dialog, which) -> {
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    })
                    .setNegativeButton("ปฏิเสธ", (dialog, which) -> Log.d(TAG, "User denied background location"))
                    .show();
        } else {
            // ถ้าเคยโดนปฏิเสธมาก่อน ให้พาไปที่ Settings
            new AlertDialog.Builder(activity)
                    .setTitle("ต้องเปิดสิทธิ์ในการตั้งค่า")
                    .setMessage("คุณต้องไปที่การตั้งค่าเพื่อเปิดสิทธิ์ตำแหน่งพื้นหลัง")
                    .setPositiveButton("ไปที่การตั้งค่า", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                        activity.startActivity(intent);
                    })
                    .setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}



//package com.example.tnglogistics.Controller;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.util.Log;
//
//import androidx.activity.ComponentActivity;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AlertDialog;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class PermissionManager {
//    private static final String TAG = "PermissionManager";
//
//    //    ขอ Permission แบบใหม่ แทน requestPermissions() เพื่อขอหลายๆสิทธิ์พร้อมกัน
//    private static ActivityResultLauncher<String[]> mPermissionResultLancher;
//    private static boolean isFineLocationPermissionGranted = false;
//    private static boolean isBackgroundLocationPermissionGranted = false;
//    private static boolean isCameraPermissionGranted = false;
//    private static boolean isPostNotificationPermissionGranted = false;
//
//    public static void registerPermissionLauncher(ComponentActivity activity){
//        Log.d(TAG, "Call registerPermissionLauncher");
//        mPermissionResultLancher = activity.registerForActivityResult(
//                new ActivityResultContracts.RequestMultiplePermissions(),
//                new ActivityResultCallback<Map<String, Boolean>>() {
//                    @Override
//                    public void onActivityResult(Map<String, Boolean> result) {
//                        if(result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null){
//                            isFineLocationPermissionGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
//                            Log.d(TAG, "FINE " + result.get(Manifest.permission.ACCESS_FINE_LOCATION));
//                        }
//                        if(result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != null){
//                            isBackgroundLocationPermissionGranted = result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
//                            Log.d(TAG, "BACKGROUND " + result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
//                        }
//                        if(result.get(Manifest.permission.CAMERA) != null){
//                            isCameraPermissionGranted = result.get(Manifest.permission.CAMERA);
//                            Log.d(TAG, "CAMERA " + result.get(Manifest.permission.CAMERA));
//                        }
//                        if(result.get(Manifest.permission.POST_NOTIFICATIONS) != null){
//                            isPostNotificationPermissionGranted = result.get(Manifest.permission.POST_NOTIFICATIONS);
//                            Log.d(TAG, "POST NOTI " + result.get(Manifest.permission.POST_NOTIFICATIONS));
//                        }
//
//                        // หลังจากขอ Foreground Location แล้ว ถ้ายังไม่ได้ Background Location ให้ขอแยก
//                        if (isFineLocationPermissionGranted && !isBackgroundLocationPermissionGranted
//                                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                            showBackgroundLocationDialog(activity);
//                        }
//
//                    }
//                });
//    }
//
//    public static void requestPermission(Activity activity){
//        Log.d(TAG, "Call requestPermissions");
//        isFineLocationPermissionGranted = ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED;
//
//        isBackgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED;
//
//        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED;
//
//        isPostNotificationPermissionGranted = ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.POST_NOTIFICATIONS
//        ) == PackageManager.PERMISSION_GRANTED;
//
//        //เพิ่มลิสต์เพื่อขอ
//        List<String> permissionRequest = new ArrayList<String>();
//
//        if(!isFineLocationPermissionGranted){
//            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//
//        if(!isCameraPermissionGranted){
//            permissionRequest.add(Manifest.permission.CAMERA);
//        }
//
//        if(!isPostNotificationPermissionGranted){
//            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS);
//        }
//
////        if (!isFineLocationPermissionGranted && retryCount < MAX_RETRY_COUNT) {
////            retryCount++;
////            new Handler(Looper.getMainLooper()).postDelayed(this::requestPermission, 2000); // ลองใหม่ทุก 2 วินาที
////        } else if (retryCount >= MAX_RETRY_COUNT) {
////            Log.e(TAG, "ขอสิทธิ์ FINE_LOCATION ไม่สำเร็จหลังจากลอง " + MAX_RETRY_COUNT + " ครั้ง");
////        }
//
//        if(!permissionRequest.isEmpty()){
//            //เรียกใช้ launch เพื่อขอสิทธิ์
//            mPermissionResultLancher.launch(permissionRequest.toArray(new String[0]));
//        } else {
//            // ถ้า Foreground Location ได้รับอนุญาตแล้ว ค่อยขอ Background Location แยก
//            // ขอ Background Location เฉพาะ Android 10+ เท่านั้น
//            if (!isBackgroundLocationPermissionGranted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                showBackgroundLocationDialog(activity);
//            }
//        }
//    }
//
////            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
////        // TODO: Consider calling
////        //    ActivityCompat#requestPermissions
////        // here to request the missing permissions, and then overriding
////        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
////        //                                          int[] grantResults)
////        // to handle the case where the user grants the permission. See the documentation
////        // for ActivityCompat#requestPermissions for more details.
////        return;
////    }
//
////    public static vo
//
//    public static void showBackgroundLocationDialog(Activity activity) {
//        new AlertDialog.Builder(activity)
////                .setTitle("ต้องการสิทธิ์การเข้าถึงตำแหน่งพื้นหลัง")
////                .setMessage("แอปต้องการสิทธิ์เข้าถึงตำแหน่งของคุณในพื้นหลังเพื่อให้การติดตามตำแหน่งทำงานได้อย่างถูกต้อง")
//                .setTitle("ต้องการสิทธิ์การเข้าถึงตำแหน่งพื้นหลัง")
//                .setMessage("แอปนี้ต้องการสิทธิ์เข้าถึงตำแหน่งพื้นหลังเพื่อให้การติดตามตำแหน่งการส่งสินค้าได้อย่างถูกต้องเท่านั้น")
//                .setPositiveButton("อนุญาต", (dialog, which) -> {
//                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                        // ขอสิทธิ์ Background Location ถ้าผู้ใช้ยังไม่ได้เลือก "Don't ask again"
//                        mPermissionResultLancher.launch(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
//                    } else {
//                        // ถ้าผู้ใช้เคยปฏิเสธก่อนหน้านี้ก็ขอ
//                        showBackgroundLocationDialog(activity);
//                    }
//                })
//                .show();
//    }
//}
