package com.example.tnglogistics.View;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnglogistics.Controller.AdapterInvoiceHelper;
import com.example.tnglogistics.Controller.LocationService;
//import com.example.tnglogistics.Controller.MyService;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.SyncImageWorker;
import com.example.tnglogistics.Model.Employee;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.Model.MileLog;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {
    private static final String TAG = "StatusFragment";
    private ViewModel viewModel;
    private AdapterInvoiceHelper adapterInvoiceHelper;
    private int allqueue;
    private LocationService locationService;
    private boolean isBound = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private TextView txtview_shipped;
    private TextView txtview_inqueue;
    private Button btn_camera;
    private String branchCode; // ประกาศตัวแปร branchCode ในระดับ class
    private String branchName;
    private double branchLatitude;
    private double branchLongitude;
    private double radius;
    private MutableLiveData<Double> distanceLiveData = new MutableLiveData<>();
    private double distance = Double.MAX_VALUE;
    private static boolean isDialogShowing; // ป้องกัน Dialog ซ้ำ
    private AlertDialog progressDialog;

    // เชื่อมต่อกับ LocationService
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            isBound = true;
            Log.d(TAG, "LocationService Connected");
            if (isBound && locationService != null && isAdded() && getView() != null) {
                Log.d(TAG, "location not null");
                locationService.getLocationLiveData().observe(getViewLifecycleOwner(), location -> {
//                    Log.d(TAG, "live data location: "+ location);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        distance = haversine(latitude, longitude, branchLatitude, branchLongitude);
                        distanceLiveData.postValue(distance);
                        Log.d(TAG, "Distance: " + distance);
                    }
                });
            }
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

    @Override
    public void onResume() {
        super.onResume();
        PermissionManager.startGPSMonitoring(requireContext(), requireActivity());
    }

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
        PermissionManager.stopGPSMonitoring();
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

//        PermissionManager.registerPermissionLauncher(requireActivity());
        TextView txtview_allqueue = view.findViewById(R.id.txtview_allqueue);
        txtview_inqueue = view.findViewById(R.id.txtview_queue);
        txtview_shipped = view.findViewById(R.id.txtview_shipped);
        btn_camera = view.findViewById(R.id.btn_camera);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = ViewModel.getInstance(requireActivity().getApplication());

        // Observe ข้อมูลที่จัดกลุ่มตาม Location
//        viewModel.getInvoiceList().observe(getViewLifecycleOwner(), invoices -> {
//            if (invoices != null && !invoices.isEmpty()) {
//                allqueue = invoices.size();
//                txtview_allqueue.setText(String.valueOf(invoices.size()));
//                Map<String, List<Invoice>> groupedInvoices = groupInvoicesByLocation(invoices);
//                adapterInvoiceHelper = new AdapterInvoiceHelper(groupedInvoices);
//                recyclerView.setAdapter(adapterInvoiceHelper);
//                checkItemList(invoices);
//                updateUI();
//            }
//        });

        viewModel.getinvoiceByTrip(requireContext()).observe(getViewLifecycleOwner(), invoices -> {
            if (invoices != null && !invoices.isEmpty()) {
                allqueue = invoices.size();
                txtview_allqueue.setText(String.valueOf(invoices.size()));
                Map<String, List<Invoice>> groupedInvoices = groupInvoicesByLocation(invoices);
                adapterInvoiceHelper = new AdapterInvoiceHelper(groupedInvoices);
                recyclerView.setAdapter(adapterInvoiceHelper);
                checkItemList(invoices);
                updateUI();
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "on Click");
                checkAndRequestPermissions();
            }
        });

//        viewModel.getEmployee(SharedPreferencesHelper.getEmployee(requireContext())).observe(getViewLifecycleOwner(), new Observer<Employee>() {
//            @Override
//            public void onChanged(Employee employee) {
//                branchName = employee.getBrchName();
//                branchLatitude = employee.getBrchLat();
//                branchLongitude = employee.getBrchLong();
//                radius = employee.getRadius();
//            }
//        });

        if (SharedPreferencesHelper.getMileType(requireContext()) == 2) {
            viewModel.getEmployee(SharedPreferencesHelper.getEmployee(requireContext())).observe(getViewLifecycleOwner(), new Observer<Employee>() {
                @Override
                public void onChanged(Employee employee) {
                    branchName = employee.getBrchName();
                    branchLatitude = employee.getBrchLat();
                    branchLongitude = employee.getBrchLong();
                    radius = employee.getRadius();
                }
            });
            Log.d(TAG, "MileType: "+SharedPreferencesHelper.getMileType(requireContext()));
            btn_camera.setVisibility(View.GONE);
            distanceLiveData.observe(getViewLifecycleOwner(), new Observer<Double>() {
                @Override
                public void onChanged(Double aDouble) {
                    Log.d(TAG, "Distance: " + aDouble);
                    if (aDouble <= radius) {
                        new Thread(() -> {
                            List<MileLog> unsyncedMileLogsImage = viewModel.getUnsyncedMileLogsImage();
                            Log.d(TAG, "unsyncedMileLogsImage size: " + unsyncedMileLogsImage.size());

                            if (!unsyncedMileLogsImage.isEmpty()) {
                                requireActivity().runOnUiThread(() -> {
                                    showSyncDialog(); // ✅ แสดง dialog เฉพาะตอนมีข้อมูลให้ sync
                                    btn_camera.setVisibility(View.VISIBLE);
                                    btn_camera.setText("ถึงสาขาเรียบร้อยแล้ว");
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    btn_camera.setVisibility(View.VISIBLE);
                                    btn_camera.setText("ถึงสาขาเรียบร้อยแล้ว");
//                                    Toast.makeText(getContext(), "ซิงค์ข้อมูลรูปภาพครบแล้ว", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }).start();
                    } else {
                        btn_camera.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "คุณอยู่นอกเขตของ " + branchName, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Distance: " + aDouble + " > Radius : " + radius);
                    }
                }
            });
        } else {
            btn_camera.setText("ถ่ายเลขไมล์เพื่อเริ่มต้นจัดส่ง");
        }

        return view;
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

        // เรียงลำดับ invoice ใน List ของแต่ละ location ตาม ShipListSeq
        for (List<Invoice> invoiceList : groupedMap.values()) {
            invoiceList.sort(Comparator.comparingInt(Invoice::getShipListSeq));
        }

        return groupedMap;
    }

        // ฟังก์ชันเช็คว่ามีไอเท็มเหลือหรือไม่
    private void checkItemList(List<Invoice> items) {
        if (items != null && !items.isEmpty()) {
            btn_camera.setVisibility(View.VISIBLE);
        } else {
            btn_camera.setVisibility(View.GONE);
        }
    }

        private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // ถ้าสิทธิ์ยังไม่ได้รับ ให้ขอใหม่
            PermissionManager.requestPermissions(requireActivity());
        } else {
            // ถ้าได้รับแล้ว ทำงานต่อได้เลย
            Toast.makeText(getContext(), "กรุณาถ่ายเลขไมล์ให้อยู่ในกรอบ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), CameraXActivity.class);
            ((MainActivity) getActivity()).getCameraMileLauncher().launch(intent);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        updateUI();
    }

    private void updateUI() {
        viewModel.countInvoicesWithStatusFour().observe(getViewLifecycleOwner(), shippedCount -> {
            // why 0 ?
            int shipped = shippedCount != null ? shippedCount : 0;
            int queue = allqueue - shipped;

            txtview_shipped.setText(String.valueOf(shipped));
            txtview_inqueue.setText(String.valueOf(queue));
            Log.d(TAG, "allQ: "+allqueue+", Shipped: " + shipped + ", In Queue: " + queue);
        });

    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; //  รัศมีโลกในหน่วย "เมตร"
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // ระยะทางเป็น "เมตร"
    }
    private void showSyncDialog() {
        if (isDialogShowing) return; // ✅ ถ้า Dialog เปิดอยู่ ไม่ต้องเปิดใหม่

        isDialogShowing = true; // ✅ ตั้งค่าให้รู้ว่ากำลังเปิด Dialog

        new AlertDialog.Builder(requireContext())
                .setTitle("ยืนยันการซิงค์ข้อมูลรูปภาพ")
                .setMessage("แอปพลิเคชันจำเป็นต้องซิงค์ข้อมูลรูปภาพเพื่อทำงานต่อ กรุณากดตกลงเพื่อซิงค์รูปภาพ")
                .setCancelable(false) // ✅ ไม่ให้ปิด Dialog ด้วยการกดข้างนอก
                .setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSyncImageWorker();
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

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false); // ป้องกันการกดปิด

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, null);
        builder.setView(view);

        progressDialog = builder.create();
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void startSyncImageWorker() {
        showLoadingDialog();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncImageWorkRequest = new OneTimeWorkRequest.Builder(SyncImageWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(requireContext()).enqueue(syncImageWorkRequest);
        new Handler().postDelayed(() -> {
            dismissLoadingDialog();
        }, 500);
    }
}