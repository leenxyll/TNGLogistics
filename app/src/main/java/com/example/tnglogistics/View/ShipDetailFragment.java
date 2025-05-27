package com.example.tnglogistics.View;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import android.widget.Toast;

import com.example.tnglogistics.Controller.AdapterInvoiceHelper;
import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private ViewModel viewModel;
    private AdapterInvoiceHelper adapterInvoiceHelper;
    private GeofenceHelper geofenceHelper;
    private LocationService locationService;
    private boolean isBound = false;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btn_camera;
    private Button btn_confirm;
    private Button btn_reportIssue;
    private LinearLayout container_btn;
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
        View view = inflater.inflate(R.layout.fragment_ship_detail, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        container_btn = view.findViewById(R.id.container_arrived);
        btn_camera = view.findViewById(R.id.btn_camera);
        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = ViewModel.getInstance(requireActivity().getApplication());

        // Observe ข้อมูลที่จัดกลุ่มตาม Location
        viewModel.getInvoiceBySeq().observe(getViewLifecycleOwner(), invoices -> {
            if (invoices != null && !invoices.isEmpty()) {
                // จัดกลุ่มตาม Location
                Map<String, List<Invoice>> locationGroups = groupInvoicesByLocation(invoices);

                // จัดการ Geofence สำหรับแต่ละ Location
                handleGeofenceForLocations(locationGroups);

                // ตรวจสอบสถานะการแสดงปุ่มตามที่เคยทำ
                checkMileLogAndUpdateUI(invoices);

                // แสดงรายการ
                adapterInvoiceHelper = new AdapterInvoiceHelper(locationGroups);
                recyclerView.setAdapter(adapterInvoiceHelper);
            } else {
                Log.d(TAG, "Invoice is empty with MileType "+SharedPreferencesHelper.getMileType(requireContext()));
                SharedPreferencesHelper.saveMileType(requireContext(), 2);
                Log.d(TAG, "Save MileType To " + SharedPreferencesHelper.getMileType(requireContext()));
                StatusFragment frag_status = StatusFragment.newInstance();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, frag_status);
                new Handler().postDelayed(() -> {
                transaction.commit();
                }, 500);
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "on Click");
                SharedPreferencesHelper.saveMileType(requireContext(), 3);
                SharedPreferencesHelper.setCameraMile(requireContext(), true);

                // เก็บ location ปัจจุบันไว้ใน SharedPreferences
                if (aInvoice != null) {
                    SharedPreferencesHelper.saveCurrentInvoiceLocation(requireContext(), aInvoice.getInvoiceShipLoCode());
                }

                checkAndRequestPermissions();
            }
        });

        return view;
    }

    private void handleGeofenceForLocations(Map<String, List<Invoice>> locationGroups) {
        for (Map.Entry<String, List<Invoice>> entry : locationGroups.entrySet()) {
            String location = entry.getKey();
            List<Invoice> invoicesInLocation = entry.getValue();

            // ตรวจสอบว่ามี Invoice ที่ยังไม่ได้เพิ่ม Geofence ใน location นี้หรือไม่
            boolean needsGeofence = false;
            Invoice representativeInvoice = null;
            String sharedGeofenceID = null;

            // หา Geofence ID ที่มีอยู่แล้ว หรือ Invoice ที่ต้องการเพิ่ม Geofence
            for (Invoice invoice : invoicesInLocation) {
                if (invoice.getGeofenceID() != null && !invoice.getGeofenceID().isEmpty()) {
                    sharedGeofenceID = invoice.getGeofenceID();
                }
                if (invoice.getGeofenceID() != null && !invoice.isAddGeofence()) {
                    needsGeofence = true;
                    representativeInvoice = invoice;
                }
            }

            // ถ้าไม่มี Geofence ID ให้สร้างใหม่
            if (sharedGeofenceID == null && needsGeofence) {
                sharedGeofenceID = UUID.randomUUID().toString();
            }

            if (needsGeofence && representativeInvoice != null && sharedGeofenceID != null) {
                if (representativeInvoice.getShipLoLat() != 0.0 && representativeInvoice.getShipLoLong() != 0.0) {
                    Log.d(TAG, "เพิ่ม Geofence สำหรับ Location: " + location);

                    // เพิ่ม Geofence
                    geofenceHelper = GeofenceHelper.getInstance(requireContext());
                    geofenceHelper.addGeofence(sharedGeofenceID, representativeInvoice.getShipLoLat(), representativeInvoice.getShipLoLong());

                    // อัปเดต Invoice ทั้งหมดใน location นี้ให้ใช้ GeofenceID เดียวกัน
                    for (Invoice invoice : invoicesInLocation) {
                        if (invoice.getGeofenceID() == null || invoice.getGeofenceID().isEmpty()) {
                            invoice.setGeofenceID(sharedGeofenceID);
                        }
                        invoice.setAddGeofence(true);
                        viewModel.update(invoice);
                    }
                } else {
                    Log.d(TAG, "ไม่มีพิกัดสำหรับ Location: " + location);
                    showMileButton();
                }
            }
        }
    }

    private void checkMileLogAndUpdateUI(List<Invoice> invoices) {
        for(Invoice invoice : invoices) {
            aInvoice = invoice;

            String currentLocation = invoice.getShipLoAddr();
            int currentLocationCode = invoice.getInvoiceShipLoCode();
            String tripCode = SharedPreferencesHelper.getTrip(getContext());

            Executors.newSingleThreadExecutor().execute(() -> {
                boolean hasMile = viewModel.hasMileLogForLocation(tripCode, currentLocationCode, 3);

                requireActivity().runOnUiThread(() -> {
                    updateUIBasedOnMileStatus(invoice, hasMile);
                });
            });
            break; // เอาแค่ invoice แรกมาตรวจสอบ UI
        }
    }

    private void updateUIBasedOnMileStatus(Invoice invoice, boolean hasMileForThisLocation) {
        if(invoice.getInvoiceShipStatusCode() == 3 && SharedPreferencesHelper.getMileType(getContext()) == 1){
            Log.d(TAG, "status 3 และ mile 1");

            if (hasMileForThisLocation) {
                Log.d(TAG, "เคยถ่ายเลขไมล์สำหรับสถานที่นี้แล้ว - ข้ามการถ่ายเลขไมล์"+invoice.getInvoiceCode()+", "+invoice.getShipLoAddr());
                btn_camera.setVisibility(View.GONE);
                container_btn.setVisibility(View.VISIBLE);
                setupConfirmButtons(invoice);
            } else {
                Log.d(TAG, "ยังไม่เคยถ่ายเลขไมล์สำหรับสถานที่นี้ - ต้องถ่ายเลขไมล์"+invoice.getInvoiceCode()+", "+invoice.getShipLoAddr());
                btn_camera.setVisibility(View.VISIBLE);
                container_btn.setVisibility(View.GONE);
            }
        }
        else if(invoice.getInvoiceShipStatusCode() == 3 && SharedPreferencesHelper.getMileType(getContext()) == 3){
            Log.d(TAG, "status 3 และ mile 3");
            btn_camera.setVisibility(View.GONE);
            container_btn.setVisibility(View.VISIBLE);
            setupConfirmButtons(invoice);
        }
        else if(invoice.getInvoiceShipStatusCode() == 2 && SharedPreferencesHelper.getMileType(requireContext()) == 3){
            Log.d(TAG, "status 2 และ mile 3");
            btn_camera.setVisibility(View.GONE);
            container_btn.setVisibility(View.VISIBLE);
            setupConfirmButtons(invoice);
        }
        else if(invoice.getInvoiceShipStatusCode() == 2 && SharedPreferencesHelper.getMileType(getContext()) == 1){
            Log.d(TAG, "status 2 และ mile 1");

            if (hasMileForThisLocation) {
                Log.d(TAG, "เคยถ่ายเลขไมล์สำหรับสถานที่นี้แล้ว - ข้ามการถ่ายเลขไมล์");
                btn_camera.setVisibility(View.GONE);
                container_btn.setVisibility(View.VISIBLE);
                setupConfirmButtons(invoice);
            } else {
                Log.d(TAG, "ยังไม่เคยถ่ายเลขไมล์สำหรับสถานที่นี้ - ต้องถ่ายเลขไมล์");
                btn_camera.setVisibility(View.VISIBLE);
                container_btn.setVisibility(View.GONE);
            }
        }
    }

    private void setupConfirmButtons(Invoice invoice) {
        btn_confirm = getView().findViewById(R.id.btn_confirm);
        btn_reportIssue = getView().findViewById(R.id.btn_report_issue);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHelper.setCameraMile(requireContext(), false);
                checkAndRequestPermissions();
            }
        });

        btn_reportIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportIssueFragment frag_report_issue = ReportIssueFragment.newInstance(invoice);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, frag_report_issue);
                transaction.addToBackStack(null);
                new Handler().postDelayed(() -> {
                    transaction.commit();
                }, 500);
            }
        });
    }

    private void showMileButton() {
        btn_camera.setVisibility(View.VISIBLE);
        container_btn.setVisibility(View.GONE);
    }

    // ฟังก์ชันจัดกลุ่ม Invoice ตาม Location
    private Map<String, List<Invoice>> groupInvoicesByLocation(List<Invoice> invoices) {
        Map<String, List<Invoice>> groupedMap = new HashMap<>();
        for (Invoice invoice : invoices) {
            String location = invoice.getShipLoAddr();
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
            if(SharedPreferencesHelper.isCameraMile(requireContext())){
                // ถ้าได้รับแล้ว ทำงานต่อได้เลย
                Toast.makeText(getContext(), "กรุณาถ่ายเลขไมล์ให้อยู่ในกรอบ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), CameraXActivity.class);
                ((MainActivity) getActivity()).getCameraMileLauncher().launch(intent);
            }else {
                ShipmentPictureFragment frag_cf = ShipmentPictureFragment.newInstance();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, frag_cf);
                transaction.addToBackStack(null);
                new Handler().postDelayed(() -> {
                    transaction.commit();
                }, 500);
            }
        }
    }
}
