package com.example.tnglogistics.View;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnglogistics.Controller.AdapterAddrHelper;
import com.example.tnglogistics.Controller.AdapterShipLocationHelper;
import com.example.tnglogistics.Controller.GeocodeHelper;
import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.AddrModel;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.RecycleAddrViewModel;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlanFragment extends Fragment {
    private static final String TAG = "PlanFragment";
    private GeofenceHelper geofenceHelper;
    private RecycleAddrViewModel recycleAddrViewModel;
    private AdapterAddrHelper adapter;
    private String addr;
    private LatLng tmpLatLng;
    private Button btn_opencamera;
    private TruckViewModel truckViewModel;
    private ShipLocationViewModel shipLocationViewModel;
    private AdapterShipLocationHelper adapterShipLocationHelper;

    public static PlanFragment newInstance() {
        return new PlanFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PlanFragment");
        Log.d(TAG, TAG +" onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PlanFragment");
        Log.d(TAG, TAG +" onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PlanFragment");
        Log.d(TAG, TAG +" onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PlanFragment");
        Log.d(TAG, TAG +" onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PlanFragment");
        Log.d(TAG, TAG +" onDetach");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, TAG +" onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TAG +" onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, TAG +" onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, TAG +" onResume");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, TAG +" onCreateView");
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterShipLocationHelper = new AdapterShipLocationHelper(new ArrayList<>(), true);
        recyclerView.setAdapter(adapterShipLocationHelper);

        Toast.makeText(getContext(), "ลงทะเบียนด้วยทะเบียนรถ : " + SharedPreferencesHelper.getTruck(requireContext()), Toast.LENGTH_SHORT).show();
        shipLocationViewModel = ShipLocationViewModel.getInstance(requireActivity().getApplication());
//        shipLocationViewModel.fetchLocationsFromServer(); // ดึงข้อมูลจาก Server

        shipLocationViewModel.getShipLocationList().observe(getViewLifecycleOwner(), shipLocations -> {
            Log.d(TAG, ""+shipLocations);
            adapterShipLocationHelper.updateList(shipLocations);
            checkItemList(shipLocations);
        });

        adapterShipLocationHelper.setOnItemRemovedListener(new AdapterShipLocationHelper.OnItemRemovedListener() {
            @Override
            public void onItemRemoved() {
                List<ShipLocation> currentList = shipLocationViewModel.getShipLocationList().getValue();
                checkItemList(currentList);
            }
        });


//        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        adapter = new AdapterAddrHelper(new ArrayList<>(), true);
//        recyclerView.setAdapter(adapter);
//
//        recycleAddrViewModel = RecycleAddrViewModel.getInstance(requireActivity().getApplication());
//        recycleAddrViewModel.getItemList().observe(getViewLifecycleOwner(), items -> {
//            adapter.updateList(items);
//            checkItemList(items);
//        });
//
//
//        // สร้างและเชื่อมต่อ ItemTouchHelper
//        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
//            @Override
//            public boolean isLongPressDragEnabled() {
//                return true; // อนุญาตให้ลากได้เมื่อกดค้าง
//            }
//
//            @Override
//            public boolean isItemViewSwipeEnabled() {
//                return false; // ปิดการเลื่อนเพื่อสไลด์
//            }
//
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                int fromPosition = viewHolder.getAdapterPosition();
//                int toPosition = target.getAdapterPosition();
//                // ตรวจสอบว่า fromPosition และ toPosition อยู่ในขอบเขตของ itemList หรือไม่
//                if (fromPosition >= 0 && fromPosition < adapter.getItemList().size() && toPosition >= 0 && toPosition < adapter.getItemList().size()) {
//                    // สลับตำแหน่งรายการ
//                    Log.d("Adapter", "Condition True");
//                    AddrModel fromItem = adapter.getItemList().get(fromPosition);
//                    adapter.getItemList().set(fromPosition, adapter.getItemList().get(toPosition));
//                    adapter.getItemList().set(toPosition, fromItem);
//                    adapter.notifyItemMoved(fromPosition, toPosition);
//                    recycleAddrViewModel.getItemList().observe(getViewLifecycleOwner(), items -> {
//                        adapter.updateList(items);
//                    });
//                    return true;  // คืนค่า true เมื่อการย้ายรายการเสร็จสมบูรณ์
//                }
//                Log.d("Adapter", "Condition false");
//                return false;  // คืนค่า false หากไม่สามารถย้ายรายการได้
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                // ไม่มีการใช้งานการ swipe
//            }
//
//            @Override
//            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN; // อนุญาตให้ลากขึ้นและลง
//                int swipeFlags = 0;  // ปิดการลากซ้ายขวา
//                return makeMovementFlags(dragFlags, swipeFlags);
//            }
//        };
//
//        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
//        touchHelper.attachToRecyclerView(recyclerView);

        EditText edt_address = view.findViewById(R.id.edt_address);
        Button btn_add = view.findViewById(R.id.btn_add);
        Button btn_check = view.findViewById(R.id.btn_check);
        TextView txtview_address = view.findViewById(R.id.txtview_address);
        btn_opencamera = view.findViewById(R.id.btn_opencamera);

        // ตั้งค่า Listener เมื่อมีการลบไอเท็ม
//        adapter.setOnItemRemovedListener(new AdapterAddrHelper.OnItemRemovedListener() {
//            @Override
//            public void onItemRemoved() {
//                ArrayList<AddrModel> currentList = recycleAddrViewModel.getItemList().getValue();
//                checkItemList(currentList); // เรียกเช็คข้อมูลใหม่
//            }
//        });

        edt_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ถ้ามีการแก้ไขข้อความ ให้แสดง btn_check และซ่อน btn_add
                if (!s.toString().trim().isEmpty()) {
                    edt_address.setBackgroundTintList((ColorStateList.valueOf(
                            ContextCompat.getColor(getContext(), R.color.neutral4)
                    )));
                    btn_check.setVisibility(View.VISIBLE);
                    btn_add.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!addr.isEmpty()) {
                    shipLocationViewModel.addLocation(addr, tmpLatLng); // insert In Room DB
//                    recycleAddrViewModel.addItem(addr, tmpLatLng);
                    edt_address.setText("");
                }
                btn_add.setVisibility(View.GONE);
                btn_check.setVisibility(View.VISIBLE);
                txtview_address.setVisibility(View.GONE);
                edt_address.setVisibility(View.VISIBLE);
            }
        });

        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addr = edt_address.getText().toString();
                // เรียกใช้ GeocodeHelper
                GeocodeHelper.getLatLngAsync(getActivity(), addr, new GeocodeHelper.GeocodeCallback() {
                    @Override
                    public void onAddressFetched(LatLng latLng) {
                        Log.d(TAG, "latlng is : "+latLng);
                        if(latLng != null){
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri location = Uri.parse("geo:" + latLng.latitude + "," + latLng.longitude + "?q=" + latLng.latitude + "," + latLng.longitude);
                            intent.setData(location); // เปิดแผนที่
                            intent.setPackage("com.google.android.apps.maps");
                            Log.d(TAG, "Open gg maps " + location);
                            startActivity(intent);
                            tmpLatLng = new LatLng(latLng.latitude, latLng.longitude);
                            btn_add.setVisibility(View.VISIBLE);
                            btn_check.setVisibility(View.GONE);

                            // เปลี่ยน edt_address เป็น TextView
                            edt_address.setVisibility(View.GONE);
                            txtview_address.setVisibility(View.VISIBLE);
                            txtview_address.setText(addr);
                        } else {
                            edt_address.setBackgroundTintList((ColorStateList.valueOf(
                                    ContextCompat.getColor(getContext(), R.color.red)
                            )));
                        }
                    }
                });
            }
        });

        txtview_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edt_address.setVisibility(View.VISIBLE);
                txtview_address.setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, TAG +" onViewCreated");

//        geofenceHelper = GeofenceHelper.getInstance(requireContext());
        btn_opencamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ArrayList<AddrModel> addrList = recycleAddrViewModel.getItemList().getValue();
//                Log.d(TAG, "Size Addr :"+addrList.size());
//                List<ShipLocation> shipLocationList = shipLocationViewModel.getShipLocationList().getValue();
//                if (shipLocationList != null) {
//                    for (int i = 0; i < shipLocationList.size(); i++) {
//                        String generatedId = UUID.randomUUID().toString();
//                        shipLocationViewModel.updateGeofenceID(i, generatedId);
//                        geofenceHelper.addGeofence(generatedId, shipLocationViewModel.getLocation(i).getShipLoLat(), shipLocationViewModel.getLocation(i).getShipLoLong());
////                        recycleAddrViewModel.updateItemId(i, generatedId); // อัปเดต ID ให้แต่ละตัว
////                        geofenceHelper.addGeofence(generatedId, recycleAddrViewModel.getItem(i).getLatLng());
//                    }
//                }
                Toast.makeText(getContext(), "กรุณาถ่ายเลขไมล์ให้อยู่ในกรอบ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), CameraXActivity.class);
                ((MainActivity) getActivity()).getCameraLauncher().launch(intent);

            }
        });
    }

    // ฟังก์ชันเช็คว่ามีไอเท็มเหลือหรือไม่
    private void checkItemList(List<ShipLocation> items) {
        if (items != null && !items.isEmpty()) {
            btn_opencamera.setVisibility(View.VISIBLE);
        } else {
            btn_opencamera.setVisibility(View.GONE);
        }
    }
}