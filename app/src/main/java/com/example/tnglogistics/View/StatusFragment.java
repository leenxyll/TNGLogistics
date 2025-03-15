package com.example.tnglogistics.View;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnglogistics.Controller.AdapterAddrHelper;
import com.example.tnglogistics.Controller.AdapterShipLocationHelper;
import com.example.tnglogistics.Controller.LocationHelper;
import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.NotificationHelper;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.RecycleAddrViewModel;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;
import com.example.tnglogistics.ViewModel.ShipmentListViewModel;
import com.example.tnglogistics.ViewModel.TripViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {
    private static final String TAG = "StatusFragment";
    private ShipmentListViewModel shipmentListViewModel;
    private ShipLocationViewModel shipLocationViewModel;
    private AdapterShipLocationHelper adapterShipLocationHelper;
    private String formattedTime;
    private long timestamp;
    private int allqueue;
    private NotificationHelper notificationHelper;
    private LocationService locationService;
    private boolean isBound = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private MediatorLiveData<Integer> totalShippedCount = new MediatorLiveData<>();
    private MediatorLiveData<Integer> totalNearArrivalCount = new MediatorLiveData<>();
    private TextView txtview_shipped;
    private TextView txtview_inqueue;
    private Button btn_camera;


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

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), LocationService.class);
        requireContext().bindService(intent, serviceConnection, requireContext().BIND_AUTO_CREATE);
    }

    // ดึงพิกัดล่าสุดจาก LocationService
    private void getLocationFromService() {
        if (isBound && locationService != null) {
            Location currentLocation = locationService.getCurrentLocation();
            if (currentLocation != null) {
                latitude = currentLocation.getLatitude();
                longitude = currentLocation.getLongitude();
                Log.d(TAG, "Current Location: Lat = " + latitude + ", Lng = " + longitude);
//                Toast.makeText(requireContext(), "Lat: " + latitude + ", Lng: " + longitude, Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Location not available yet.");
            }
        } else {
            Log.e(TAG, "LocationService is not bound.");
        }
    }

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        TextView txtview_allqueue = view.findViewById(R.id.txtview_allqueue);
        txtview_inqueue = view.findViewById(R.id.txtview_queue);
        txtview_shipped = view.findViewById(R.id.txtview_shipped);
        btn_camera = view.findViewById(R.id.btn_camera);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationHelper = new NotificationHelper(requireContext());
        adapterShipLocationHelper = new AdapterShipLocationHelper(new ArrayList<>(), false);
        recyclerView.setAdapter(adapterShipLocationHelper);

        shipLocationViewModel = ShipLocationViewModel.getInstance(requireActivity().getApplication());
        shipmentListViewModel = ShipmentListViewModel.getInstance(requireActivity().getApplication());


        LiveData<List<ShipmentList>> shipmentLiveData =
                shipmentListViewModel.getShipmentListByTrip(SharedPreferencesHelper.getTrip(requireContext()));
        shipmentLiveData.observe(getViewLifecycleOwner(), new Observer<List<ShipmentList>>() {
            @Override
            public void onChanged(List<ShipmentList> shipmentLists) {
                // เรียงลำดับตาม ShipListSeq
                Collections.sort(shipmentLists, Comparator.comparingInt(ShipmentList::getShipListSeq));

                Log.d(TAG, "Shipment Data: " + shipmentLists.size());
                txtview_allqueue.setText(String.valueOf(shipmentLists.size()));
                allqueue = shipmentLists.size();
                if (shipmentLists != null && !shipmentLists.isEmpty()) {
                    Log.d(TAG, "Shipment Data: " + shipmentLists.size());

                    for (ShipmentList shipment : shipmentLists) {
                        Log.d(TAG, "Status: [" + shipment.getShipListStatus() + "]");
                        shipLocationViewModel.addFilterCode(shipment.getShipListShipLoCode());
                        Log.d(TAG, "addFilterCode: " + shipment.getShipListShipLoCode());
                    }
                }
                shipmentLiveData.removeObserver(this);
            }
        });

        shipLocationViewModel.getFilteredShipLocationList()
                .observe(getViewLifecycleOwner(), shipLocations -> {
                    adapterShipLocationHelper.updateList(shipLocations);
                    for(ShipLocation shipLocation: shipLocations){
                        Log.d(TAG, ""+shipLocation.getShipLoAddr());
                    }
                });

//        MediatorLiveData<ShipmentList> shipmentLiveDataUpdate = new MediatorLiveData<>();
//
//        shipmentLiveDataUpdate.addSource(shipmentListViewModel.getShipped(), shipmentLists -> {
//            for (ShipmentList shipment : shipmentLists) {
//                updateShipmentStatus(shipment, "ถึงแล้ว");
//            }
//        });
//
//        shipmentLiveDataUpdate.addSource(shipmentListViewModel.getNearArrival(), shipmentLists -> {
//            for (ShipmentList shipment : shipmentLists) {
//                updateShipmentStatus(shipment, "ใกล้ถึงแล้ว");
//            }
//        });

        shipmentListViewModel.getShipped().observe(getViewLifecycleOwner(), shipmentLists -> {
            Log.d(TAG, "Shipments shipped: " + shipmentLists.size());
            getLocationFromService(); // ดึงพิกัดก่อนยืนยัน
            for (ShipmentList shipment : shipmentLists) {
                timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                formattedTime = sdf.format(new Date(timestamp));
                shipment.setShipListStatus("ถึงแล้ว");
                shipment.setLatUpdateStatus(latitude);
                shipment.setLongUpdateStatus(longitude);
                shipment.setLastUpdateStatus(formattedTime);
                shipment.setGeofenceID("");
//                Toast.makeText(requireContext(), "ถึงแล้ว", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("ถึงแล้ว", "ถึงแล้ว : " + shipment.getShipListShipLoCode(), MainActivity.class);
                shipLocationViewModel.getLocationByShipLoCode(shipment.getShipListShipLoCode()).observe(getViewLifecycleOwner(), new Observer<ShipLocation>() {
                    @Override
                    public void onChanged(ShipLocation shipLocation) {
                        // เมื่อข้อมูลพร้อมใช้งานแล้ว ทำการส่ง Notification
                        if (shipLocation != null) {
                            notificationHelper.sendHighPriorityNotification(
                                    "ถึงแล้ว",
                                    shipLocation.getShipLoAddr(),
                                    MainActivity.class
                            );
                            shipmentListViewModel.update(shipment);
//                            shipmentListViewModel.updateShipmentStatus(geofenceId, "DWELL");
                        }
                    }
                });
                Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is SHIPPED. " + shipment.getLastUpdateStatus());
            }
        });

        shipmentListViewModel.getShippedCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Update Shipments shipped: " + count);
            txtview_shipped.setText(String.valueOf(count));
            int queue = allqueue - count;
            Log.d(TAG, "Update Shipments queue: " + queue);
            txtview_inqueue.setText(String.valueOf(queue));
            if(count == allqueue){
                SharedPreferencesHelper.saveMileIn(requireContext(), true);
                btn_camera.setVisibility(View.VISIBLE);
            }
        });

        shipmentListViewModel.getNearArrival().observe(getViewLifecycleOwner(), shipmentLists -> {
            getLocationFromService(); // ดึงพิกัดก่อนยืนยัน
            for (ShipmentList shipment : shipmentLists) {
                timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                formattedTime = sdf.format(new Date(timestamp));
                shipment.setShipListStatus("ใกล้ถึงแล้ว");
                shipment.setLatUpdateStatus(latitude);
                shipment.setLongUpdateStatus(longitude);
                shipment.setLastUpdateStatus(formattedTime);

//                Toast.makeText(requireContext(), "ใกล้ถึงแล้ว", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("ใกล้ถึงแล้ว", "ใกล้ถึงแล้ว : " + shipment.getShipListShipLoCode(), MainActivity.class);

                shipLocationViewModel.getLocationByShipLoCode(shipment.getShipListShipLoCode()).observe(getViewLifecycleOwner(), new Observer<ShipLocation>() {
                    @Override
                    public void onChanged(ShipLocation shipLocation) {
                        // เมื่อข้อมูลพร้อมใช้งานแล้ว ทำการส่ง Notification
                        if (shipLocation != null) {
                            notificationHelper.sendHighPriorityNotification(
                                    "ใกล้ถึงแล้ว",
                                    shipLocation.getShipLoAddr(),
                                    MainActivity.class
                            );
                            shipmentListViewModel.update(shipment);
//                            shipmentListViewModel.updateShipmentStatus(geofenceId, "DWELL");
                        }
                    }
                });

                Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is NEAR ARRIVAL." + shipment.getLastUpdateStatus());

                shipmentListViewModel.update(shipment);
            }
        });

        // ไว้เทส
//        btn_camera.setVisibility(View.VISIBLE);
//        SharedPreferencesHelper.saveMileIn(requireContext(), true);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "กรุณาถ่ายเลขไมล์ให้อยู่ในกรอบ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), CameraXActivity.class);
                ((MainActivity) getActivity()).getCameraLauncher().launch(intent);
            }
        });
        return view;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        setupObservers();
//    }
//
//    // ฟังก์ชันอัปเดตสถานะ
//    private void updateShipmentStatus(ShipmentList shipment, String status) {
//        timestamp = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
//        formattedTime = sdf.format(new Date(timestamp));
//
//        shipment.setShipListStatus(status);
//        shipment.setLatUpdateStatus(latitude);
//        shipment.setLongUpdateStatus(longitude);
//        shipment.setLastUpdateStatus(formattedTime);
//
//        shipLocationViewModel.getLocationByShipLoCode(shipment.getShipListShipLoCode()).observe(getViewLifecycleOwner(), shipLocation -> {
//            if (shipLocation != null) {
//                notificationHelper.sendHighPriorityNotification(
//                        status,
//                        shipLocation.getShipLoAddr(),
//                        MainActivity.class
//                );
//            }
//            shipmentListViewModel.update(shipment);
//        });
//    }
//
//    public void setupObservers() {
//        totalShippedCount.addSource(shipmentListViewModel.getShippedCount(), count -> {
//            totalShippedCount.setValue(count);
//            updateUI();
//        });
//
//        totalNearArrivalCount.addSource(shipmentListViewModel.getNearArrivalCount(), count -> {
//            totalNearArrivalCount.setValue(count);
//            updateUI();
//        });
//    }
//
//    private void updateUI() {
//        int shipped = totalShippedCount.getValue() != null ? totalShippedCount.getValue() : 0;
//        int nearArrival = totalNearArrivalCount.getValue() != null ? totalNearArrivalCount.getValue() : 0;
//        int queue = allqueue - shipped;
//
//        txtview_shipped.setText(String.valueOf(shipped));
//        txtview_inqueue.setText(String.valueOf(queue));
//
//        if (shipped == allqueue) {
//            SharedPreferencesHelper.saveMileIn(requireContext(), true);
//            btn_camera.setVisibility(View.VISIBLE);
//        }
//
//        Log.d(TAG, "Shipped: " + shipped + ", Near Arrival: " + nearArrival + ", In Queue: " + queue);
//    }

}