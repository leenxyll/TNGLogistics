package com.example.tnglogistics.View;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

import com.example.tnglogistics.Controller.AdapterPlanHelper;
import com.example.tnglogistics.Controller.GeocodeHelper;
import com.example.tnglogistics.Model.RecyclePlanModel;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.RecyclePlanViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlanFragment extends Fragment {
    private static final String TAG = "PlanFragment";
    private RecyclePlanViewModel recyclePlanViewModel;
    private AdapterPlanHelper adapter;
    private String addr;
    private Button btn_opencamera;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdapterPlanHelper(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        EditText edt_address = view.findViewById(R.id.edt_address);
        Button btn_add = view.findViewById(R.id.btn_add);
        Button btn_check = view.findViewById(R.id.btn_check);
        btn_opencamera = view.findViewById(R.id.btn_opencamera);

        recyclePlanViewModel = new ViewModelProvider(this).get(RecyclePlanViewModel.class);
        recyclePlanViewModel.getItemList().observe(getViewLifecycleOwner(), items -> {
            adapter.updateList(items);
            checkItemList(items);
        });

        // ตั้งค่า Listener เมื่อมีการลบไอเท็ม
        adapter.setOnItemRemovedListener(new AdapterPlanHelper.OnItemRemovedListener() {
            @Override
            public void onItemRemoved() {
                ArrayList<RecyclePlanModel> currentList = recyclePlanViewModel.getItemList().getValue();
                checkItemList(currentList); // เรียกเช็คข้อมูลใหม่
            }
        });

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
                    recyclePlanViewModel.addItem(addr);
                    edt_address.setText("");
                }
                btn_add.setVisibility(View.GONE);
                btn_check.setVisibility(View.VISIBLE);
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
                            btn_add.setVisibility(View.VISIBLE);
                            btn_check.setVisibility(View.GONE);
                        } else {
                            edt_address.setBackgroundTintList((ColorStateList.valueOf(
                                    ContextCompat.getColor(getContext(), R.color.red)
                            )));
                        }
                    }
                });
            }
        });

        btn_opencamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraXActivity.class);
                ((MainActivity) getActivity()).getCameraLauncher().launch(intent);
            }
        });

        return view;
    }

    // ฟังก์ชันเช็คว่ามีไอเท็มเหลือหรือไม่
    private void checkItemList(ArrayList<RecyclePlanModel> items) {
        if (items != null && !items.isEmpty()) {
            btn_opencamera.setVisibility(View.VISIBLE);
        } else {
            btn_opencamera.setVisibility(View.GONE);
        }
    }
}