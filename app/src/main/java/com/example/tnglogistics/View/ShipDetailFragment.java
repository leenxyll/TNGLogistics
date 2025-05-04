package com.example.tnglogistics.View;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnglogistics.Controller.AdapterInvoiceHelper;
import com.example.tnglogistics.Controller.AdapterShipLocationHelper;
import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.NotificationHelper;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.TextRecognitionHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.InvoiceViewModel;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;
import com.example.tnglogistics.ViewModel.ShipmentListViewModel;
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
 * Use the {@link ShipDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShipDetailFragment extends Fragment {
    private static final String TAG = "ShipDetailFragment";
    private InvoiceViewModel invoiceViewModel;
    private AdapterInvoiceHelper adapterInvoiceHelper;
    private GeofenceHelper geofenceHelper;
    private LocationService locationService;
    private boolean isBound = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btn_camera;
    private Button btn_confirm;
    private Button btn_confirmPic;
    private LinearLayout container_btn;
    private String imagePath;
    private long imageTimestamp;
    private boolean cameraMile;
    private Invoice aInvoice;

    // เชื่อมต่อกับ LocationService
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            isBound = true;
            Log.d(TAG, "LocationService Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    public static ShipDetailFragment newInstance() {
        return new ShipDetailFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), LocationService.class);
        requireContext().bindService(intent, serviceConnection, requireContext().BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        PermissionManager.startGPSMonitoring(requireContext(), requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "ShipDetailFragment");
        PermissionManager.stopGPSMonitoring();
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "ShipDetailFragment");
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "ShipDetailFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "ShipDetailFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "ShipDetailFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ship_detail, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        ImageView imgview_preview = view.findViewById(R.id.imgview_preview);
        container_btn = view.findViewById(R.id.container_arrived);
        btn_camera = view.findViewById(R.id.btn_camera);
        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        invoiceViewModel = InvoiceViewModel.getInstance(requireActivity().getApplication());

        Bundle args = getArguments();

        // Observe ข้อมูลที่จัดกลุ่มตาม Location
        invoiceViewModel.getInvoiceBySeq().observe(getViewLifecycleOwner(), invoices -> {
            if (invoices != null && !invoices.isEmpty()) {
                for(Invoice invoice : invoices){
                    aInvoice = invoice;
                    if (invoice.getGeofenceID() != null && !invoice.isAddGeofence()){
                        if(invoice.getShipLoLat() != 0.0 && invoice.getShipLoLong() != 0.0) {
                            //มีพิกัด
                            Log.d(TAG, "Geofence ID : " + invoice.getGeofenceID());
                            Log.d(TAG, "location : " + invoice.getShipLoLat() + ", " + invoice.getShipLoLong());
                            invoice.setAddGeofence(true);
                            invoiceViewModel.update(invoice);
                            geofenceHelper = GeofenceHelper.getInstance(requireContext());
                            String geofenceID = invoice.getGeofenceID();
                            geofenceHelper.addGeofence(geofenceID, invoice.getShipLoLat(), invoice.getShipLoLong());
                        } else {
                            //ไม่มีพิกัด
                            invoice.setInvoiceShipStatusCode(3);
                            invoiceViewModel.update(invoice);
                            btn_camera.setVisibility(View.VISIBLE);
                            container_btn.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Log.d(TAG, "Geofence already added, skipping...");
                    }

                    if(invoice.getInvoiceShipStatusCode() == 3 & SharedPreferencesHelper.getMileType(getContext()) == 1 & args == null){
                        // ใกล้ถึง
                        btn_camera.setVisibility(View.VISIBLE);
                        container_btn.setVisibility(View.INVISIBLE);
                    }else if(invoice.getInvoiceShipStatusCode() == 3 & SharedPreferencesHelper.getMileType(getContext()) == 3 & args == null){
                        btn_camera.setVisibility(View.INVISIBLE);
                        container_btn.setVisibility(View.VISIBLE);
                        btn_confirm = view.findViewById(R.id.btn_confirm);
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferencesHelper.saveMileType(requireContext(), 1);
                                cameraMile = false;
                                checkAndRequestPermissions();
                            }
                        });
                    }else if(!invoice.isAddGeofence() && invoice.getShipLoLat() != 0.0 && invoice.getShipLoLong() != 0.0){
                        // ไม่มีพิกัด
                        btn_camera.setVisibility(View.VISIBLE);
                        container_btn.setVisibility(View.INVISIBLE);
                    }else{
                        invoice.setInvoiceShipStatusCode(3);
                        invoiceViewModel.update(invoice);
                        btn_camera.setVisibility(View.VISIBLE);
                        container_btn.setVisibility(View.INVISIBLE);
                    }
                }
                Map<String, List<Invoice>> groupedInvoices = groupInvoicesByLocation(invoices);
                adapterInvoiceHelper = new AdapterInvoiceHelper(groupedInvoices);
                recyclerView.setAdapter(adapterInvoiceHelper);
            } else {
                SharedPreferencesHelper.saveMileType(requireContext(), 2);
                Log.d(TAG, "Save MileType To "+ SharedPreferencesHelper.getMileType(requireContext()));
                StatusFragment frag_status = StatusFragment.newInstance();
                // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, frag_status);
                // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
                new Handler().postDelayed(() -> {
                    transaction.commit();
                }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
            }
        });


        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "on Click");
                SharedPreferencesHelper.saveMileType(requireContext(),3);
                cameraMile = true;
                checkAndRequestPermissions();
            }
        });


        return view;
    }

    // ฟังก์ชันจัดกลุ่ม Invoice ตาม Location
    private Map<String, List<Invoice>> groupInvoicesByLocation(List<Invoice> invoices) {
        Map<String, List<Invoice>> groupedMap = new HashMap<>();
        for (Invoice invoice : invoices) {
            String location = invoice.getShipLoAddr(); // ใช้ค่าของ ShipLocation เป็นตัวจัดกลุ่ม
            if (!groupedMap.containsKey(location)) {
                groupedMap.put(location, new ArrayList<>());
            }
            groupedMap.get(location).add(invoice);
        }
        return groupedMap;
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // ถ้าสิทธิ์ยังไม่ได้รับ ให้ขอใหม่
            PermissionManager.requestPermissions(requireActivity());
        } else {
            if(cameraMile){
                // ถ้าได้รับแล้ว ทำงานต่อได้เลย
                Toast.makeText(getContext(), "กรุณาถ่ายเลขไมล์ให้อยู่ในกรอบ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), CameraXActivity.class);
                ((MainActivity) getActivity()).getCameraMileLauncher().launch(intent);
            }else {
                Intent intent = new Intent(getActivity(), CameraXCFActivity.class);
                ((MainActivity) getActivity()).getCameraCFLauncher().launch(intent);
            }
        }
    }

    private void requestLocation(Runnable onCompleteCallback) {
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
                    Log.d(TAG, "Get Location");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    // อัปเดทสถานะ Invoice
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int nextSeq = invoiceViewModel.getNextInvoiceLogSeq(aInvoice.getInvoiceCode());
                        Log.d(TAG, "Next Seq InvoiceShipLog: " + nextSeq);
                        String invoiceCode = aInvoice.getInvoiceCode();
                        invoiceViewModel.updateInvoice(aInvoice, nextSeq, 4, latitude, longitude, imageTimestamp, requireContext());
                        Log.d(TAG, "update invoice: " + invoiceCode);
//                        aInvoice.setInvoiceShipStatusCode(4);
//                        invoiceViewModel.update(aInvoice);
                        //remove geofence here and delete geofenceID

                        // เรียกใช้ callback บน UI thread หลังจากที่ update เสร็จแล้ว
                        if (onCompleteCallback != null) {
                            requireActivity().runOnUiThread(onCompleteCallback);
                        }
                    });
                } else {
                    // กรณีที่ไม่สามารถรับ location ได้ ก็เรียก callback เพื่อไปต่อ
                    if (onCompleteCallback != null) {
                        requireActivity().runOnUiThread(onCompleteCallback);
                    }
                }
            }).addOnFailureListener(e -> {
                // กรณีที่เกิด error ก็เรียก callback เพื่อไปต่อ
                Log.e(TAG, "Failed to get location", e);
                if (onCompleteCallback != null) {
                    requireActivity().runOnUiThread(onCompleteCallback);
                }
            });
        }
    }
    }