package com.example.tnglogistics.View;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.TextRecognitionHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreviewPictureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

interface LocationCallback {
    void onLocationProcessed();
}

public class PreviewPictureFragment extends Fragment {
    private static String TAG ="PreviewPictureFragment";
    private TextRecognitionHelper txtRecog;
    private String imagePath;
    private long imageTimestamp;
    private ViewModel viewModel;
    private EditText edittxt_detectnum = null;
    private TextView txtview_detectnum;
//    private LocationService locationService;
//    private boolean isBound = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    

    @Override
    public void onStart() {
        super.onStart();
//        Intent intent = new Intent(requireContext(), LocationService.class);
//        requireContext().bindService(intent, serviceConnection, requireContext().BIND_AUTO_CREATE);
    }


    public static PreviewPictureFragment newInstance() {
        return new PreviewPictureFragment();
    }


    @Override
    public void onResume() {
        super.onResume();
        PermissionManager.startGPSMonitoring(requireContext(), requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
//        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
        PermissionManager.stopGPSMonitoring();
    }

    @Override
    public void onStop() {
        super.onStop();
//        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
//        if (isBound) {
//            requireContext().unbindService(serviceConnection);
//            isBound = false;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_picture, container, false);
        ImageView imgview_preview = view.findViewById(R.id.imgview_preview);
        ImageView imgview_edt = view.findViewById(R.id.imgview_edt);
        TextView txtview_time = view.findViewById(R.id.txtview_time);
        txtview_detectnum = view.findViewById(R.id.txtview_detectnum);
        LinearLayout container_milleage = view.findViewById(R.id.container_milleage);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        Button btn_opencameara_agian = view.findViewById(R.id.btn_opencamera_again);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
//        shipLocationViewModel = ShipLocationViewModel.getInstance(requireActivity().getApplication());
//        shipmentListViewModel = ShipmentListViewModel.getInstance(requireActivity().getApplication());
//        tripViewModel = TripViewModel.getInstance(requireActivity().getApplication());
//        truckViewModel = TruckViewModel.getInstance(requireActivity().getApplication());
        viewModel = ViewModel.getInstance(requireActivity().getApplication());

        Bundle args = getArguments();

        if (args != null) {
            imagePath = args.getString("image_path");
            imageTimestamp = args.getLong("image_timestamp", 0);

            txtRecog = new TextRecognitionHelper(imagePath, new TextRecognitionHelper.TextRecognitionListener() {
                @Override
                public void onTextRecognitionComplete(String result) {
                    // รับผลลัพธ์ที่ได้รับจากการประมวลผล
                    result = txtRecog.getResult();
                    Log.d(TAG, "Result : " + result);
                    if(result == null){
                        container_milleage.setBackgroundTintList((ColorStateList.valueOf(
                                ContextCompat.getColor(getContext(), R.color.red)
                        )));
                        txtview_detectnum.setText("0");
                        // for test
                        btn_confirm.setVisibility(View.VISIBLE);
                        imgview_edt.setVisibility(View.VISIBLE);
                    } else {
                        txtview_detectnum.setText(result);
                        btn_confirm.setVisibility(View.VISIBLE);
                        imgview_edt.setVisibility(View.VISIBLE);
//                        btn_opencameara_agian.setVisibility(View.GONE);
                    }
                }
            });

            if (imagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                imgview_preview.setImageBitmap(bitmap);
            }
            if (imageTimestamp != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                String formattedTime = sdf.format(new Date(imageTimestamp));
                txtview_time.setText(formattedTime);
            }
        }

        imgview_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // เปลี่ยน TextView เป็น EditText
                edittxt_detectnum = new EditText(v.getContext());
                edittxt_detectnum.setText(txtview_detectnum.getText());  // ใช้ข้อความจาก TextView
                edittxt_detectnum.setTextColor(txtview_detectnum.getCurrentTextColor());
                edittxt_detectnum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);  // ใช้ขนาดตัวอักษรเดิม
                edittxt_detectnum.setInputType(InputType.TYPE_CLASS_NUMBER);

                // ลบ TextView ออกและเพิ่ม EditText เข้าไปแทน
                ((LinearLayout) v.getParent()).removeView(txtview_detectnum);
                ((LinearLayout) v.getParent()).addView(edittxt_detectnum, 2); // ใส่ที่ตำแหน่งที่ต้องการใน LinearLayout
                imgview_edt.setVisibility(View.GONE);
            }
        });

        btn_opencameara_agian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraXActivity.class);
                ((MainActivity) getActivity()).getCameraMileLauncher().launch(intent);
            }
        });

        // แก้ไข btn_confirm onClick
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mileType = SharedPreferencesHelper.getMileType(getContext());
                if(mileType == 1 || mileType == 3){
                    Log.d(TAG, "MileType: "+mileType);
                    requestLocation(mileType, new LocationCallback() {
                        @Override
                        public void onLocationProcessed() {
                            // ทำงานหลังจาก requestLocation เสร็จแล้ว
                            ShipDetailFragment frag_shipdetail = ShipDetailFragment.newInstance();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, frag_shipdetail);
                            transaction.commit();
                        }
                    });
                }else {
                    Log.d(TAG, "MileType: "+mileType);
                    requestLocation(mileType, new LocationCallback() {
                        @Override
                        public void onLocationProcessed() {
                            // ทำงานหลังจาก requestLocation เสร็จแล้ว
                            Intent intent = new Intent(getActivity(), SplashActivity.class);
                            SharedPreferencesHelper.setUserLoggedIn(requireContext(), false);
                            SharedPreferencesHelper.setEmployee(requireContext(), 0);
                            SharedPreferencesHelper.saveLastFragment(requireContext(), "");
                            ViewModel.resetInstance();
                            requireActivity().finish();
                            startActivity(intent);
                        }
                    });
                }
            }
        });



        return view;
    }

    // ดึงพิกัดล่าสุดจาก LocationService
//    private void getLocationFromService() {
//        if (isBound && locationService != null) {
//            Location currentLocation = locationService.getCurrentLocation();
//            if (currentLocation != null) {
//                latitude = currentLocation.getLatitude();
//                longitude = currentLocation.getLongitude();
//                Log.d(TAG, "Current Location: Lat = " + latitude + ", Lng = " + longitude);
////                Toast.makeText(requireContext(), "Lat: " + latitude + ", Lng: " + longitude, Toast.LENGTH_LONG).show();
//            } else {
//                Log.e(TAG, "Location not available yet.");
//            }
//        } else {
//            Log.e(TAG, "LocationService is not bound.");
//        }
//    }


    private void startLocationService() {
        // เริ่ม startForegroundService เพื่อเริ่ม LocationService
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d(TAG, "🚫 LocationService startForegroundService");
            requireContext().startForegroundService(serviceIntent); // ใช้ startForegroundService สำหรับ Android 8.0 (API 26) ขึ้นไป
        } else {
            Log.d(TAG, "🚫 LocationService startService");
            requireContext().startService(serviceIntent); // ใช้ startService สำหรับเวอร์ชันเก่ากว่า
        }
    }

    private void stopLocationService() {
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().stopService(serviceIntent);
        Log.d(TAG, "🚫 LocationService Stopped");
    }

//    private void requestLocation(int mileType) {
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
//                // Show explanation to the user before requesting permission again
//                new AlertDialog.Builder(requireContext())
//                        .setMessage("ต้องการสิทธิ์การเข้าถึงตำแหน่งเพื่อใช้งาน")
//                        .setPositiveButton("ตกลง", (dialog, which) ->
//                                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
//                        .setNegativeButton("ยกเลิก", null)
//                        .show();
//            } else {
//                // Request permission directly if rationale is not needed (i.e., the user denied previously)
//                // กรณีเคยปฏิเสธสิทธิ์มาก่อน พาไปที่หน้าตั้งค่า
//                new AlertDialog.Builder(requireActivity())
//                        .setTitle("จำเป็นต้องเปิดสิทธิ์ตำแหน่งในการตั้งค่า")
//                        .setMessage("เพื่อให้คุณสามารถเข้าใช้งาน จำเป็นต้องเปิดสิทธิ์การเข้าถึงตำแหน่ง\n\nกรุณาเปิดสิทธิ์ \"อนุญาตตลอดเวลา\" ในการตั้งค่าแอป เพื่อเข้าใช้งานระบบ")
//                        .setPositiveButton("ไปที่การตั้งค่า", (dialog, which) -> {
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            intent.setData(Uri.fromParts("package", requireActivity().getPackageName(), null));
//                            requireActivity().startActivity(intent);
//                        })
//                        .setNegativeButton("ภายหลัง", (dialog, which) -> {
//                            dialog.dismiss();
//                            // แสดงข้อความเตือนเพิ่มเติม
//                            Toast.makeText(getContext(), "ต้องการสิทธิ์การเข้าถึงตำแหน่งเพื่อใช้งาน", Toast.LENGTH_SHORT).show();
//                        })
//                        .setCancelable(false) // ป้องกันการปิดไดอะล็อกโดยกดพื้นที่ว่าง
//                        .show();
//            }
//
//        } else {
//            // Permission is already granted
//            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//                if (location != null) {
//                    Log.d(TAG, "Get Location");
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//                    if(mileType == 1){
//                        updateInvoice(2);
//                        updateMile(1, mileType);
//                    }else if(mileType == 3){
//                        // ลบ geofence มั้ย
//                        Executors.newSingleThreadExecutor().execute(() -> {
//                            int nextSeq = viewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
//                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
//                            updateMile(nextSeq, mileType);
//                            //remove geofence here and delete geofenceID
//                        });
//                    }else if(mileType == 2){
//                        Executors.newSingleThreadExecutor().execute(() -> {
//                            int nextSeq = viewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
//                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
//                            updateMile(nextSeq, mileType);
//                            //remove geofence here and delete geofenceID
//                        });
//                    }
//
//                }
//            });
//        }
//    }

    // แก้ไข requestLocation method
    private void requestLocation(int mileType, LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // ... permission handling code เหมือนเดิม

        } else {
            // Permission is already granted
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Log.d(TAG, "Get Location");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    if(mileType == 1){
                        updateInvoice(2);
                        updateMile(1, mileType);
                    }else if(mileType == 3){
                        Executors.newSingleThreadExecutor().execute(() -> {
                            int nextSeq = viewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
                            updateMile(nextSeq, mileType);

                            // เรียก callback เมื่อทำงานเสร็จ
                            requireActivity().runOnUiThread(() -> callback.onLocationProcessed());
                        });
                        return; // return เพื่อไม่ให้เรียก callback ซ้ำ
                    }else if(mileType == 2){
                        Executors.newSingleThreadExecutor().execute(() -> {
                            int nextSeq = viewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
                            updateMile(nextSeq, mileType);

                            // เรียก callback เมื่อทำงานเสร็จ
                            requireActivity().runOnUiThread(() -> callback.onLocationProcessed());
                        });
                        return; // return เพื่อไม่ให้เรียก callback ซ้ำ
                    }

                    // สำหรับ mileType == 1
                    callback.onLocationProcessed();
                }
            });
        }
    }

//    // แก้ไข requestLocation method
//    private void requestLocation(int mileType, LocationCallback callback) {
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // ... permission handling code เหมือนเดิม
//
//        } else {
//            // Permission is already granted
//            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//                if (location != null) {
//                    Log.d(TAG, "Get Location");
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//
//                    if(mileType == 1){
//                        //ไมล์ออก
//                        updateInvoice(2);
//                        updateMile(1, mileType);
//                    }else if(mileType == 3){
//                        //ไมล์ถึง
//                        Executors.newSingleThreadExecutor().execute(() -> {
//                            int nextSeq = viewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
//                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
//                            updateMile(nextSeq, mileType);
//
//                            // เรียก callback เมื่อทำงานเสร็จ
//                            requireActivity().runOnUiThread(() -> callback.onLocationProcessed());
//                        });
//                        return; // return เพื่อไม่ให้เรียก callback ซ้ำ
//                    }else if(mileType == 2){
//                        Executors.newSingleThreadExecutor().execute(() -> {
//                            int nextSeq = viewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
//                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
//                            updateMile(nextSeq, mileType);
//
//                            // เรียก callback เมื่อทำงานเสร็จ
//                            requireActivity().runOnUiThread(() -> callback.onLocationProcessed());
//                        });
//                        return; // return เพื่อไม่ให้เรียก callback ซ้ำ
//                    }
//
//                    // สำหรับ mileType == 1
//                    callback.onLocationProcessed();
//                }
//            });
//        }
//    }

    private void updateMile(int seq, int mileType){
        int mileRecord;
        if (edittxt_detectnum != null) {
            mileRecord = Integer.parseInt(edittxt_detectnum.getText().toString());
            Log.d(TAG, "Mile: "+mileRecord);
        } else {
            mileRecord = Integer.parseInt(txtview_detectnum.getText().toString());
            Log.d(TAG, "Mile: "+mileRecord);
        }

        // ดึง location จาก SharedPreferences
        int currentLocation = SharedPreferencesHelper.getCurrentInvoiceLocation(getContext());

        viewModel.updateMile(requireContext(), seq, mileRecord, imageTimestamp, latitude, longitude, imagePath, mileType, currentLocation);
    }

    private void updateInvoice(int seq){
        Map<String, String> geofenceMap = new HashMap<>();

        LiveData<List<Invoice>> invoiceLiveData = viewModel.getinvoiceByTrip(requireContext());
        invoiceLiveData.observe(getViewLifecycleOwner(), new Observer<List<Invoice>>() {
            @Override
            public void onChanged(List<Invoice> invoiceList) {
                if(invoiceList != null && !invoiceList.isEmpty()){
                    Log.d(TAG, "Invoice Data: " + invoiceList.size());

                    // จัดกลุ่มตาม Location
                    Map<Integer, List<Invoice>> locationGroups = new HashMap<>();
                    for (Invoice invoice : invoiceList) {
                        int location = invoice.getInvoiceShipLoCode();
                        Log.d(TAG, "ShipLo For Key: "+location);
                        if (!locationGroups.containsKey(location)) {
                            locationGroups.put(location, new ArrayList<>());
                        }
                        locationGroups.get(location).add(invoice);
                    }

                    // สำหรับแต่ละ location ให้ใช้ Geofence ID เดียวกัน
                    for (Map.Entry<Integer, List<Invoice>> entry : locationGroups.entrySet()) {
                        int location = entry.getKey();
                        List<Invoice> invoicesInLocation = entry.getValue();

                        String sharedGeofenceID = null;

                        // หา Geofence ID ที่มีอยู่แล้ว หรือสร้างใหม่
                        for (Invoice invoice : invoicesInLocation) {
                            if (invoice.getGeofenceID() != null && !invoice.getGeofenceID().isEmpty()) {
                                sharedGeofenceID = invoice.getGeofenceID();
                                break;
                            }
                        }

                        if (sharedGeofenceID == null) {
                            sharedGeofenceID = UUID.randomUUID().toString();
                        }

                        // กำหนด Geofence ID เดียวกันให้ Invoice ทั้งหมดใน location นี้
                        for (Invoice invoice : invoicesInLocation) {
                            String invoiceCode = invoice.getInvoiceCode();

                            // ดึง seq แยกสำหรับแต่ละ invoice
//                            Executors.newSingleThreadExecutor().execute(() -> {
//                                int invoiceSeq = viewModel.getNextInvoiceLogSeq(invoiceCode);
//                                requireActivity().runOnUiThread(() -> {
                                        viewModel.updateInvoiceStatus(invoice, seq, 2, latitude, longitude, imageTimestamp, getContext());
//                                });
//                            });

                            if (invoice.getGeofenceID() == null || invoice.getGeofenceID().isEmpty()) {
                                invoice.setGeofenceID(sharedGeofenceID);
                                geofenceMap.put(invoiceCode, sharedGeofenceID);
                                Log.d(TAG, "Shared GeofenceID assigned: " + sharedGeofenceID + " to invoice: " + invoiceCode);
                                viewModel.update(invoice);
                            } else {
                                geofenceMap.put(invoiceCode, invoice.getGeofenceID());
                                Log.d(TAG, "GeofenceID already exists: " + invoice.getGeofenceID() + " to invoice: " + invoiceCode);
                            }
                        }
                    }
                }
                invoiceLiveData.removeObserver(this);
            }
        });
    }



}