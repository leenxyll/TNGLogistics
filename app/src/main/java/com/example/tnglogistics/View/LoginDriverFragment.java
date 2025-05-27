package com.example.tnglogistics.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Employee;
import com.example.tnglogistics.Model.Repository;
import com.example.tnglogistics.Network.RetrofitClient;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginDriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginDriverFragment extends Fragment {
    private static final String TAG = "LoginDriverFragment";
    private ViewModel viewModel;
    private LocationService locationService;
    private boolean isBound = false;
    private int EmpCode;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String branchCode; // ประกาศตัวแปร branchCode ในระดับ class
    private String branchName;
    private double branchLatitude;
    private double branchLongitude;
    private double radius;
    private double distance = Double.MAX_VALUE;
    private boolean isGPSDialogShowing = false; // ใช้เช็คว่า Dialog เปิดอยู่หรือไม่
    private FusedLocationProviderClient fusedLocationClient;


//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
//            locationService = binder.getService();
//            isBound = true;
//            Log.d(TAG, "LocationService Connected");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            isBound = false;
//        }
//    };

    public LoginDriverFragment() {
        // Required empty public constructor
    }

    public static LoginDriverFragment newInstance() {
        return new LoginDriverFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
//        Intent intent = new Intent(requireContext(), LocationService.class);
//        requireContext().bindService(intent, serviceConnection, requireContext().BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        PermissionManager.startGPSMonitoring(requireContext(), requireActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (isBound) {
//            requireContext().unbindService(serviceConnection);
//            isBound = false;
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PermissionManager.stopGPSMonitoring();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_driver, container, false);

        EditText edt_truckreg = view.findViewById(R.id.edt_truckreg);
        Button btn_start = view.findViewById(R.id.btn_start);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                String truckreg = edt_truckreg.getText().toString().trim();
                int truckRegNumber;
                Log.d(TAG, ""+truckreg);
                if (!truckreg.isEmpty()) {
                    truckRegNumber = Integer.parseInt(truckreg);
                    loginUser(truckRegNumber);
                } else {
                    Toast.makeText(getContext(), "รหัสพนักงาน", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void checkGPS() {
        if (!PermissionManager.checkGPS(requireContext())) {
            PermissionManager.showEnableGPSDialog(requireActivity());
        }
    }

    private void loginUser(int empCode) {
        JsonObject credentials = new JsonObject();
        credentials.addProperty("EmpCode", empCode);

        RetrofitClient.getInstance().getApiService().login(credentials).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseBody = response.body();
                    if (responseBody != null) {
                        Boolean status = responseBody.get("status").getAsBoolean();
                        String message = responseBody.get("message").getAsString();
                        if (status) {
//                            Toast.makeText(getContext(), "เข้าสู่ระบบสำเร็จ: " + message, Toast.LENGTH_SHORT).show();
                            // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
                            Log.d(TAG, "Login Success: " + responseBody.toString());
                            // ตัวอย่างการดึง branchCode (ถ้ามี)
                            if (responseBody.has("branchCode")) {
                                branchCode = responseBody.get("branchCode").getAsString();
                                Log.d(TAG, "Branch Code: " + branchCode);
                                // ทำอย่างอื่นต่อ เช่น เรียก API ดึงพิกัดสาขา
                                EmpCode = empCode;
                                getBranchLocation();
                            }
                        } else {
                            Toast.makeText(getContext(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Login Failed: " + responseBody.toString());
                        }
                    } else {
                        Log.e(TAG, "Login Response Body is null");
                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนอง", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Login Failed with status: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Login Error Body: " + errorBody);

                            // แปลง String เป็น JsonObject
                            JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                            // ดึงค่าจาก key "message"
                            if (errorJson.has("message")) {
                                String errorMessage = errorJson.get("message").getAsString();
                                Toast.makeText(getContext(), "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Login Failed: ไม่พบข้อความข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                            Toast.makeText(getContext(), "Login Failed: เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Login Error: " + t.getMessage());
                Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการเชื่อมต่อ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBranchLocation() {
        if (branchCode != null && !branchCode.isEmpty()) {
            RetrofitClient.getInstance().getApiService().getBranchLocation(branchCode).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseBody = response.body();
                        if (responseBody != null) {
                            Boolean status = responseBody.get("status").getAsBoolean();
                            String message = responseBody.get("message").getAsString();
                            if (status) {
                                branchLatitude = responseBody.get("latitude").getAsDouble();
                                branchLongitude = responseBody.get("longitude").getAsDouble();
                                branchName = responseBody.get("branchName").getAsString();
                                radius = responseBody.get("radius").getAsDouble();
//                                Toast.makeText(getContext(), "ได้รับข้อมูลพิกัดสาขา", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Branch Location: Lat=" + branchLatitude + ", Lon=" + branchLongitude + ", Radius=" + radius);
                                // ดำเนินการต่อหลังได้รับพิกัด เช่น ตรวจสอบตำแหน่ง
//                                checkLocationPermissionAndGetLocation();
//                                getLocationFromService();
                                requestLocation();

                            } else  {
                                Log.e(TAG, "Error parsing branch location response: " + responseBody.toString());
                                Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการอ่านข้อมูลพิกัดสาขา: " +message , Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Branch Location Response Body is null");
                            Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนองจากเซิร์ฟเวอร์", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to get branch location with status: " + response.code());
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Branch Location Error Body: " + errorBody);

                                // แปลง errorBody เป็น JsonObject
                                JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                                // ดึงค่าจาก key "message"
                                if (errorJson.has("message")) {
                                    String errorMessage = errorJson.get("message").getAsString();
                                    Toast.makeText(getContext(), "ดึงข้อมูลพิกัดสาขาผิดพลาด: " + errorMessage, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "ดึงข้อมูลพิกัดสาขาผิดพลาด: ไม่มีข้อความแสดงข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing branch location error body", e);
                                Toast.makeText(getContext(), "ดึงข้อมูลพิกัดสาขาผิดพลาด", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "ดึงข้อมูลพิกัดสาขาผิดพลาด", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e(TAG, "Error getting branch location: " + t.getMessage());
                    Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการเชื่อมต่อ", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "ไม่พบรหัสสาขา", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Branch code is null or empty, cannot get location.");
        }
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; //  รัศมีโลกในหน่วย "เมตร"
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // ระยะทางเป็น "เมตร"
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show explanation to the user before requesting permission again
                new AlertDialog.Builder(requireContext())
                        .setMessage("ต้องการสิทธิ์การเข้าถึงตำแหน่งเพื่อใช้งาน")
                        .setPositiveButton("ตกลง", (dialog, which) ->
                                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
                        .setNegativeButton("ยกเลิก", null)
                        .show();
            } else {
                // Request permission directly if rationale is not needed (i.e., the user denied previously)
                // กรณีเคยปฏิเสธสิทธิ์มาก่อน พาไปที่หน้าตั้งค่า
                new AlertDialog.Builder(requireActivity())
                        .setTitle("จำเป็นต้องเปิดสิทธิ์ตำแหน่งในการตั้งค่า")
                        .setMessage("เพื่อให้คุณสามารถเข้าใช้งาน จำเป็นต้องเปิดสิทธิ์การเข้าถึงตำแหน่ง\n\nกรุณาเปิดสิทธิ์ \"อนุญาตตลอดเวลา\" ในการตั้งค่าแอป เพื่อเข้าใช้งานระบบ")
                        .setPositiveButton("ไปที่การตั้งค่า", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", requireActivity().getPackageName(), null));
                            requireActivity().startActivity(intent);
                        })
                        .setNegativeButton("ภายหลัง", (dialog, which) -> {
                            dialog.dismiss();
                            // แสดงข้อความเตือนเพิ่มเติม
                            Toast.makeText(getContext(), "ต้องการสิทธิ์การเข้าถึงตำแหน่งเพื่อใช้งาน", Toast.LENGTH_SHORT).show();
                        })
                        .setCancelable(false) // ป้องกันการปิดไดอะล็อกโดยกดพื้นที่ว่าง
                        .show();
            }

        } else {
            // Permission is already granted
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    distance = haversine(latitude, longitude, branchLatitude, branchLongitude);
                    Log.d(TAG, "lat lng: " + latitude + ", " + longitude );
                    Log.d(TAG, "Distance: " + distance);
                    if (distance <= radius) {
                        // แล้วดึง Invoice ครั้งแรก
                        viewModel = ViewModel.getInstance(requireActivity().getApplication());

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        SharedPreferencesHelper.setEmployee(requireContext(), EmpCode);
                        viewModel.getShipmentList(getContext(), new Repository.StatusCallback() {
                            @Override
                            public void onStatusReceived(boolean status) {
                                if (status) {
                                    viewModel.getSubIssue();
                                    SharedPreferencesHelper.saveLastFragment(requireContext(), "");
                                    SharedPreferencesHelper.setUserLoggedIn(requireContext(), true);
                                    Employee employee = new Employee(EmpCode, branchCode, branchName, branchLatitude, branchLongitude, radius);
                                    viewModel.insertEmployee(employee);
                                    requireActivity().finish();
                                    startActivity(intent);
                                    Toast.makeText(getContext(), "เข้าสู่ระบบสำเร็จ", Toast.LENGTH_SHORT).show();
                                } else {
                                    // ดำเนินการเมื่อไม่สำเร็จ
                                    Toast.makeText(getContext(), "ไม่พบรอบการส่ง", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "คุณอยู่นอกเขตของ " + branchName, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Distance: " + distance + " > Radius : " + radius);
                    }
                }
            });
        }
    }

}