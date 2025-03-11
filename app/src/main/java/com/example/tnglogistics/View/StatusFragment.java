package com.example.tnglogistics.View;

import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tnglogistics.Controller.AdapterAddrHelper;
import com.example.tnglogistics.Controller.AdapterShipLocationHelper;
import com.example.tnglogistics.Controller.LocationHelper;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.RecycleAddrViewModel;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;
import com.example.tnglogistics.ViewModel.ShipmentListViewModel;
import com.example.tnglogistics.ViewModel.TripViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;

import java.util.ArrayList;
import java.util.Date;
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
    private Location currentLocation;
    private String formattedTime;
    private long timestamp;

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
        TextView txtview_inqueue = view.findViewById(R.id.txtview_queue);
        TextView txtview_shipped = view.findViewById(R.id.txtview_shipped);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        adapterShipLocationHelper = new AdapterShipLocationHelper(new ArrayList<>(), false);
        recyclerView.setAdapter(adapterShipLocationHelper);

        LocationHelper.getInstance(requireContext()).getCurrentLocation(requireContext(), new LocationHelper.LocationListener() {
            @Override
            public void onLocationResult(Location location) {
                if(location != null){
                    Log.d(TAG, "Current Location:" + location.getLatitude()+ " " + location.getLongitude());
                    currentLocation = location;
                }
            }
        });

        shipLocationViewModel = ShipLocationViewModel.getInstance(requireActivity().getApplication());
        shipmentListViewModel = ShipmentListViewModel.getInstance(requireActivity().getApplication());

        shipmentListViewModel.getShipmentListByTrip(SharedPreferencesHelper.getTrip(requireContext()))
                .observe(getViewLifecycleOwner(), shipmentLists -> {
                    Log.d(TAG, "Shipment Data: " + shipmentLists.size());
                    txtview_allqueue.setText(String.valueOf(shipmentLists.size()));
                    if (shipmentLists != null && !shipmentLists.isEmpty()) {
                        for (ShipmentList shipment : shipmentLists) {
                            shipLocationViewModel.addFilterCode(shipment.getShipListShipLoCode());
                            Log.d(TAG, "addFilterCode: " + shipment.ShipListShipLoCode);
                            if ("ใกล้ถึง".equals(shipment.getShipListStatus())) {
                                // ถ้ามีการเปลี่ยนแปลงสถานะเป็น "Delivered" ให้แสดงการแจ้งเตือนหรือทำการอื่นๆ
                                timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                formattedTime = sdf.format(new Date(timestamp));
                                shipment.setLatUpdateStatus(currentLocation.getLatitude());
                                shipment.setLatUpdateStatus(currentLocation.getLongitude());
                                shipment.setLastUpdateStatus(formattedTime);
                                Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is ใกล้ถึง."+shipment.getLastUpdateStatus());

                                shipmentListViewModel.update(shipment);

                            }else if("ถึงแล้ว".equals(shipment.getShipListStatus())){
                                timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                formattedTime = sdf.format(new Date(timestamp));
                                shipment.setLatUpdateStatus(currentLocation.getLatitude());
                                shipment.setLatUpdateStatus(currentLocation.getLongitude());
                                shipment.setLastUpdateStatus(formattedTime);
                                shipment.setGeofenceID("");
                                Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is ถึงแล้ว."+shipment.getLastUpdateStatus());

                                shipmentListViewModel.update(shipment);
                            }
                        }
                    }
                });

//        shipmentListViewModel.getNearArrivalCount().observe(getViewLifecycleOwner(), count -> {
//            Log.d(TAG, "จำนวน shipment ที่ใกล้ถึง: " + count);
//            txt_near_arrival.setText(String.valueOf(count));
//        });

        shipmentListViewModel.getShippedCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "จำนวน shipment ที่ถึงแล้ว: " + count);
            txtview_shipped.setText(String.valueOf(count));
        });


        shipLocationViewModel.getFilteredShipLocationList()
                .observe(getViewLifecycleOwner(), shipLocations -> {
                    adapterShipLocationHelper.updateList(shipLocations);
                });

//        txtview_inqueue.setText(String.valueOf(recycleAddrViewModel.getSize()));

        return view;
    }
}