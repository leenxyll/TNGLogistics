package com.example.tnglogistics.Controller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
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
    private static boolean isDialogShowing = false; // ป้องกัน Dialog ซ้ำ

    private static Handler gpsHandler;
    private static Runnable gpsRunnable;

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


//    private static void requestBackgroundLocation(Activity activity) {
//        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//            // แสดง Dialog อธิบายก่อนขอ
//            new AlertDialog.Builder(activity)
//                    .setTitle("ต้องการสิทธิ์ตำแหน่งพื้นหลัง")
//                    .setMessage("แอปต้องการสิทธิ์ตำแหน่งพื้นหลังเพื่อให้การติดตามการส่งสินค้าได้ หากคุณปฏิเสธจะไม่สามารถติดตามการส่งสินค้าได้")
//                    .setPositiveButton("อนุญาต", (dialog, which) -> {
//                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
//                    })
//                    .setNegativeButton("ปฏิเสธ", (dialog, which) -> Log.d(TAG, "User denied background location"))
//                    .show();
//        } else {
//            // ถ้าเคยโดนปฏิเสธมาก่อน ให้พาไปที่ Settings
//            new AlertDialog.Builder(activity)
//                    .setTitle("ต้องเปิดสิทธิ์ในการตั้งค่า")
//                    .setMessage("คุณต้องไปที่การตั้งค่าเพื่อเปิดสิทธิ์ตำแหน่งพื้นหลัง")
//                    .setPositiveButton("ไปที่การตั้งค่า", (dialog, which) -> {
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
//                        activity.startActivity(intent);
//                    })
//                    .setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss())
//                    .show();
//        }
//    }

    private static void requestBackgroundLocation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                // แสดงไดอะล็อกอธิบายสำหรับคนขับรถส่งสินค้า
                new AlertDialog.Builder(activity)
                        .setTitle("ต้องการสิทธิ์การเข้าถึงตำแหน่งในพื้นหลัง")
//                    .setMessage("เพื่อให้ระบบติดตามการส่งสินค้าทำงานได้อย่างถูกต้อง และช่วยให้คุณส่งสินค้าได้อย่างมีประสิทธิภาพ แอปจำเป็นต้องติดตามตำแหน่งของคุณแม้ในขณะที่แอปทำงานในพื้นหลัง\n\nการอนุญาตนี้ช่วยให้:\n- บริษัทติดตามสถานะการจัดส่งได้แบบเรียลไทม์\n- ลูกค้าทราบเวลาการจัดส่งที่แม่นยำ\n- คุณได้รับแจ้งเตือนเส้นทางและข้อมูลการจัดส่งโดยอัตโนมัติ")
                        .setMessage("เพื่อให้ระบบติดตามการส่งสินค้าทำงานได้อย่างถูกต้อง และช่วยให้คุณส่งสินค้าได้อย่างมีประสิทธิภาพ แอปจำเป็นต้องติดตามตำแหน่งของคุณแม้ในขณะที่แอปทำงานในพื้นหลัง\n\nกรุณาเปิดสิทธิ์ \"อนุญาตตลอดเวลา\" ในการตั้งค่าแอป เพื่อให้ระบบติดตามการจัดส่งสินค้าทำงานได้อย่างต่อเนื่อง")
                        .setPositiveButton("อนุญาตการใช้งาน", (dialog, which) -> {
                            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        })
                        .setNegativeButton("ไม่อนุญาต", (dialog, which) -> {
                            Log.d(TAG, "Driver denied background location");
                            // แสดงข้อความเตือนเพิ่มเติม
                            Toast.makeText(activity, "หากไม่อนุญาต คุณจะไม่สามารถใช้ฟีเจอร์การติดตามการส่งสินค้าได้", Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false) // ป้องกันการปิดไดอะล็อกโดยกดพื้นที่ว่าง
                        .show();
            } else {
                // กรณีเคยปฏิเสธสิทธิ์มาก่อน พาไปที่หน้าตั้งค่า
                new AlertDialog.Builder(activity)
                        .setTitle("จำเป็นต้องเปิดสิทธิ์ตำแหน่งในการตั้งค่า")
                        .setMessage("เพื่อให้คุณสามารถทำงานจัดส่งสินค้าได้อย่างมีประสิทธิภาพ จำเป็นต้องเปิดสิทธิ์การเข้าถึงตำแหน่งในพื้นหลัง\n\nกรุณาเปิดสิทธิ์ \"อนุญาตตลอดเวลา\" ในการตั้งค่าแอป เพื่อให้ระบบติดตามการจัดส่งสินค้าทำงานได้อย่างต่อเนื่อง")
                        .setPositiveButton("ไปที่การตั้งค่า", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                            activity.startActivity(intent);
                        })
                        .setNegativeButton("ภายหลัง", (dialog, which) -> {
                            dialog.dismiss();
                            // แสดงข้อความเตือนเพิ่มเติม
                            Toast.makeText(activity, "คุณจำเป็นต้องเปิดสิทธิ์เพื่อใช้งานระบบติดตามการส่งสินค้า", Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false) // ป้องกันการปิดไดอะล็อกโดยกดพื้นที่ว่าง
                        .show();
            }
        } else {
            // Android 9 และต่ำกว่า ไม่ต้องขอ
            isBackgroundLocationGranted = true;
            Toast.makeText(activity, "แอปยังคงใช้งานได้ แต่อาจจำกัดฟีเจอร์บางอย่าง", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Background Location not needed for Android < Q");
        }
    }

    // เพิ่มฟังก์ชันสำหรับแสดงหน้าแนะนำประโยชน์ก่อนขอสิทธิ์ (แนะนำให้เรียกก่อน requestBackgroundLocation)
//    private static void showLocationBenefitsDialog(Activity activity) {
//        new AlertDialog.Builder(activity)
//                .setTitle("ประโยชน์ของการแชร์ตำแหน่ง")
//                .setMessage("สวัสดีคุณคนขับ!\n\nการแชร์ตำแหน่งของคุณจะช่วยให้:\n\n1. คุณได้รับคำแนะนำเส้นทางที่ดีที่สุดตลอดการเดินทาง\n2. ลูกค้าทราบเวลาจัดส่งที่แม่นยำ\n3. ระบบส่งงานใกล้เคียงให้คุณโดยอัตโนมัติ\n4. มีหลักฐานยืนยันการส่งสินค้าถึงจุดหมาย\n\nเราใช้ข้อมูลตำแหน่งเฉพาะเพื่อการจัดส่งสินค้าเท่านั้น")
//                .setPositiveButton("ตกลง ดำเนินการต่อ", (dialog, which) -> {
//                    requestBackgroundLocation(activity);
//                })
//                .setCancelable(false)
//                .show();
//    }


    public static boolean checkGPS(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

//    public static void startGPSMonitoring(Context context, Activity activity) {
//        gpsHandler = new Handler();
//        gpsRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (!PermissionManager.checkGPS(context)) {
//                    PermissionManager.showEnableGPSDialog(activity);
//                }
//                gpsHandler.postDelayed(this, 10*1000); // เช็คทุก 3 วินาที
//            }
//        };
//        gpsHandler.post(gpsRunnable);
//    }

    // เช็ค Location Permission
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //  เช็ค Location Services (รองรับทุก Android Version)
    @TargetApi(Build.VERSION_CODES.P)
    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            // สำหรับ Android เวอร์ชันเก่า - เช็คทั้ง GPS และ Network
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return gpsEnabled || networkEnabled;
        }
    }

    // ✅ เช็คทั้ง Permission และ Location Services
    public static boolean checkLocationServices(Context context) {
        if (!hasLocationPermission(context)) {
            return false;
        }
        return isLocationEnabled(context);
    }

    public static void startGPSMonitoring(Context context, Activity activity) {
        gpsHandler = new Handler();
        gpsRunnable = new Runnable() {
            @Override
            public void run() {
                if (!PermissionManager.checkLocationServices(context)) {
                    PermissionManager.showEnableGPSDialog(activity);
                }
                gpsHandler.postDelayed(this, 10*1000); // เช็คทุก 10 วินาที
            }
        };
        gpsHandler.post(gpsRunnable);
    }
    public static void stopGPSMonitoring() {
        if (gpsHandler != null && gpsRunnable != null) {
            gpsHandler.removeCallbacks(gpsRunnable);
        }
    }

    public static void showEnableGPSDialog(Activity activity) {
        if (isDialogShowing) return; // ✅ ถ้า Dialog เปิดอยู่ ไม่ต้องเปิดใหม่

        isDialogShowing = true; // ✅ ตั้งค่าให้รู้ว่ากำลังเปิด Dialog

        new AlertDialog.Builder(activity)
                .setTitle("ต้องการเข้าถึงตำแหน่ง")
                .setMessage("จำเป็นต้องเปิด GPS เพื่อใช้งานแอปพลิเคชัน หากไม่เปิด GPS จะไม่สามารถใช้งานแอปได้")
                .setCancelable(false) // ✅ ไม่ให้ปิด Dialog ด้วยการกดข้างนอก
                .setPositiveButton("เปิด GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        isDialogShowing = false; // ✅ ปิด Dialog แล้วรีเซ็ตค่า
                    }
                })
                .setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDialogShowing = false; // ✅ ปิด Dialog แล้วรีเซ็ตค่า
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        isDialogShowing = false; // ✅ ป้องกัน Dialog ค้าง
                    }
                })
                .show();
    }
}
