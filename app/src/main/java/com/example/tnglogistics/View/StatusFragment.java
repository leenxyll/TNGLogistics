package com.example.tnglogistics.View;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private Location currentLocation;
    private String formattedTime;
    private long timestamp;
    private int allqueue;

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
        Button btn_camera = view.findViewById(R.id.btn_camera);

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

//        List<ShipmentList> shipmentListsByTrip = shipmentListViewModel.getShipmentListByTrip(SharedPreferencesHelper.getTrip(requireContext())).getValue();
//        if (shipmentListsByTrip != null && !shipmentListsByTrip.isEmpty()) {
//            Log.d(TAG, "Shipment Data: " + shipmentListsByTrip.size());
//            txtview_allqueue.setText(String.valueOf(shipmentListsByTrip.size()));
//
//            for (ShipmentList shipment : shipmentListsByTrip) {
//                shipLocationViewModel.addFilterCode(shipment.getShipListShipLoCode());
//                Log.d(TAG, "addFilterCode: " + shipment.ShipListShipLoCode);
//                if ("ใกล้ถึง".equals(shipment.getShipListStatus())) {
//                    timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
//                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
//                    formattedTime = sdf.format(new Date(timestamp));
//                    shipment.setLatUpdateStatus(currentLocation.getLatitude());
//                    shipment.setLongUpdateStatus(currentLocation.getLongitude());
//                    shipment.setLastUpdateStatus(formattedTime);
//                    Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is ใกล้ถึง."+shipment.getLastUpdateStatus());
//
//                    shipmentListViewModel.update(shipment);
//
//                }else if("ถึงแล้ว".equals(shipment.getShipListStatus())){
//                    timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
//                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
//                    formattedTime = sdf.format(new Date(timestamp));
//                    shipment.setLatUpdateStatus(currentLocation.getLatitude());
//                    shipment.setLongUpdateStatus(currentLocation.getLongitude());
//                    shipment.setLastUpdateStatus(formattedTime);
//                    shipment.setGeofenceID("");
//                    Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is ถึงแล้ว."+shipment.getLastUpdateStatus());
//
//                    shipmentListViewModel.update(shipment);
//                }
//            }
//        }

        shipmentListViewModel.getShipmentListByTrip(SharedPreferencesHelper.getTrip(requireContext()))
                .observe(getViewLifecycleOwner(), shipmentLists -> {
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
                });

        shipmentListViewModel.getShipped().observe(getViewLifecycleOwner(), shipmentLists -> {
            Log.d(TAG, "Shipments shipped: " + shipmentLists.size());
//            txtview_shipped.setText(String.valueOf(shipmentLists.size()));
            for (ShipmentList shipment : shipmentLists) {
                timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                formattedTime = sdf.format(new Date(timestamp));
                shipment.setShipListStatus("ถึงแล้ว");
                shipment.setLatUpdateStatus(currentLocation.getLatitude());
                shipment.setLongUpdateStatus(currentLocation.getLongitude());
                shipment.setLastUpdateStatus(formattedTime);
                shipment.setGeofenceID("");

                Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is SHIPPED. " + shipment.getLastUpdateStatus());

                shipmentListViewModel.update(shipment);
            }
        });

        shipmentListViewModel.getShippedCount().observe(getViewLifecycleOwner(), count -> {
            Log.d(TAG, "Update Shipments shipped: " + count);
            txtview_shipped.setText(String.valueOf(count));
            if(count == allqueue){
                SharedPreferencesHelper.saveMileIn(requireContext(), true);
                btn_camera.setVisibility(View.VISIBLE);
            }
        });

        shipmentListViewModel.getNearArrival().observe(getViewLifecycleOwner(), shipmentLists -> {
            for (ShipmentList shipment : shipmentLists) {
                timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                formattedTime = sdf.format(new Date(timestamp));
                shipment.setShipListStatus("ใกล้ถึงแล้ว");
                shipment.setLatUpdateStatus(currentLocation.getLatitude());
                shipment.setLongUpdateStatus(currentLocation.getLongitude());
                shipment.setLastUpdateStatus(formattedTime);
                Log.d(TAG, "Shipment with seq " + shipment.getShipListSeq() + " is NEAR ARRIVAL." + shipment.getLastUpdateStatus());

                shipmentListViewModel.update(shipment);
            }
        });


//        shipmentListViewModel.getNearArrivalCount().observe(getViewLifecycleOwner(), count -> {
//            Log.d(TAG, "Shipments shipped: " + count);
//            txt_near_arrival.setText(String.valueOf(count));
//        });



        shipLocationViewModel.getFilteredShipLocationList()
                .observe(getViewLifecycleOwner(), shipLocations -> {
                    adapterShipLocationHelper.updateList(shipLocations);
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



//        txtview_inqueue.setText(String.valueOf(recycleAddrViewModel.getSize()));

        return view;
    }
}