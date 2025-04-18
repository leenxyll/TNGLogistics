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
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Controller.LocationHelper;
import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.TextRecognitionHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.Model.Trip;
import com.example.tnglogistics.Model.Truck;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.InvoiceViewModel;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;
import com.example.tnglogistics.ViewModel.ShipmentListViewModel;
import com.example.tnglogistics.ViewModel.TripViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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
public class PreviewPictureFragment extends Fragment {
    private static String TAG ="PreviewPictureFragment";
    private TextRecognitionHelper txtRecog;
    private String imagePath;
    private long imageTimestamp;
//    private ShipLocationViewModel shipLocationViewModel;
//    private ShipmentListViewModel shipmentListViewModel;
//    private TripViewModel tripViewModel;
    private InvoiceViewModel invoiceViewModel;
    private GeofenceHelper geofenceHelper;
//    private Location currentLocation;
//    private Trip aTrip;
    private EditText edittxt_detectnum = null;
    private TextView txtview_detectnum;
    private TruckViewModel truckViewModel;
//    private Truck aTruck;
    private LocationService locationService;
    private boolean isBound = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
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
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
        PermissionManager.stopGPSMonitoring();
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
//        if (isBound) {
//            requireContext().unbindService(serviceConnection);
//            isBound = false;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.preview_picture_fragment, container, false);
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
        invoiceViewModel = InvoiceViewModel.getInstance(requireActivity().getApplication());

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


//        LocationHelper.getInstance(requireContext()).getCurrentLocation(requireContext(), new LocationHelper.LocationListener() {
//            @Override
//            public void onLocationResult(Location location) {
//                if(location != null){
//                    Log.d(TAG, "Current Location:" + location.getLatitude()+ " " + location.getLongitude());
//                    currentLocation = location;
//                }
//            }
//        });

//        btn_confirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "On Click");
//
//                if(SharedPreferencesHelper.getMileIn(requireContext())){
////                    //ขาเข้า
//                    stopLocationService();
////                    LiveData<Trip> tripLiveData = tripViewModel.getTripByCodeFromSharedPreferences(requireContext());
////                    tripLiveData.observe(getViewLifecycleOwner(), new Observer<Trip>() {
////                        @Override
////                        public void onChanged(Trip trip) {
////                            if (trip != null) {
////                                Log.d(TAG, "trip : " + trip.getTripCode());
////                                Trip aTrip = trip;
////                                if (edittxt_detectnum != null) {
////                                    Log.d(TAG, "trip : " + trip.getTripCode());
////                                    aTrip.setTripMileageIn(Double.parseDouble(edittxt_detectnum.getText().toString()));
////                                } else {
////                                    aTrip.setTripMileageIn(Double.parseDouble(txtview_detectnum.getText().toString()));
////                                }
////                                aTrip.setTripTimeIn(txtview_time.getText().toString());
////                                tripViewModel.update(aTrip);
////                            } else {
////                                Log.d(TAG, "Trip not found");
////                            }
////                            // 🛑 ลบ Observer หลังทำงาน ✅
////                            tripLiveData.removeObserver(this);
////                        }
////                    });
//
//                    Toast.makeText(getContext(), "สินสุดการจัดส่ง", Toast.LENGTH_SHORT).show();
//
//                    // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
//                    new Handler().postDelayed(() -> {
////                        transaction.commit();
//                        ShipLocationViewModel.resetInstance();
//                        ShipmentListViewModel.resetInstance();
//                        TripViewModel.resetInstance();
//                        TruckViewModel.resetInstance();
//                        SharedPreferencesHelper.saveLastFragment(requireContext(),"");
//                        SharedPreferencesHelper.setUserLoggedIn(requireContext(),false);
//                        SharedPreferencesHelper.saveTrip(requireContext(), "");
//                        Intent intent = new Intent(getActivity(), SplashActivity.class);
//                        startActivity(intent); // เรียก startActivity() เพื่อเปิด Activity ใหม่
//                        requireActivity().finish(); // ปิด Fragment หรือ Activity ปัจจุบัน (ถ้าต้องการ)
//                    }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
//                    SharedPreferencesHelper.saveMileIn(requireContext(), false);
//
//                } else {
//
//                    startLocationService();
//                    // ขาออก
//                    // สร้าง Fragment ใหม่ที่ต้องการแสดง
//                    StatusFragment frag_status = new StatusFragment();
//
//                    // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
//                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                    transaction.replace(R.id.fragment_container, frag_status);  // R.id.fragment_container คือ ID ของ ViewGroup ที่ใช้สำหรับแสดง Fragment
//
////                    Map<Integer, String> geofenceMap = new HashMap<>();
////
////                    LiveData<List<ShipmentList>> shipmentLiveData =
////                            shipmentListViewModel.getShipmentListByTrip(SharedPreferencesHelper.getTrip(requireContext()));
////                    shipmentLiveData                    .observe(getViewLifecycleOwner(), new Observer<List<ShipmentList>>() {
////                        @Override
////                        public void onChanged(List<ShipmentList> shipmentLists) {
////                            getLocationFromService(); // อัปเดตค่าพิกัดก่อนใช้งาน
////
////                            if (shipmentLists != null && !shipmentLists.isEmpty()) {
////                                Log.d(TAG, "Shipment Data: " + shipmentLists.size());
////
////                                for (ShipmentList shipment : shipmentLists) {
////                                    int shipLoCode = shipment.getShipListShipLoCode();
////                                    Log.d(TAG, "Loop in ShipmentList : " + shipment.getShipListSeq() +
////                                            " LoCode : " + shipment.getShipListShipLoCode() + "("+shipLoCode+")"+
////                                            " geofenceID : " + shipment.getGeofenceID());
////
////                                    shipment.setShipListStatus("กำลังจัดส่ง");
////                                    shipment.setLatUpdateStatus(latitude);
////                                    shipment.setLongUpdateStatus(longitude);
////                                    shipment.setLastUpdateStatus(txtview_time.getText().toString());
////
////                                    // ตรวจสอบว่า GeofenceID มีค่าอยู่แล้วหรือไม่
////                                    if (shipment.getGeofenceID() == null || shipment.getGeofenceID().isEmpty()) {
////                                        String generatedId = UUID.randomUUID().toString();
////                                        shipment.setGeofenceID(generatedId);
////                                        geofenceMap.put(shipLoCode, generatedId);
////                                        Log.d(TAG, "New GeofenceID assigned: " + generatedId + " to shipcode: "+shipLoCode);
////                                        shipmentListViewModel.update(shipment); // อัปเดตข้อมูลใหม่
////                                    } else {
////                                        geofenceMap.put(shipLoCode, shipment.getGeofenceID());
////                                        Log.d(TAG, "GeofenceID already exists: " + shipment.getGeofenceID()+ " to shipcode: "+shipLoCode);
////                                    }
////
////                                    // ตรวจสอบว่าต้องเพิ่ม Geofence หรือไม่ (เช็คค่า isGeofenceAdded)
////                                    if (shipment.getGeofenceID() != null && !shipment.isGeofenceAdded()) {
////                                        geofenceHelper = GeofenceHelper.getInstance(requireContext());
////                                        shipLocationViewModel.getShipLocationByCode(shipment.getShipListShipLoCode())
////                                                .observe(getViewLifecycleOwner(), shipLocation -> {
////                                                    if (shipLocation != null & shipLoCode == shipLocation.getShipLoCode()) {
////                                                        String geofenceID = geofenceMap.get(shipLoCode);
////                                                        Log.d(TAG, "Add Geofence : " + shipment.getGeofenceID() + "("+geofenceID+")"+
////                                                                " " + shipLocation.getShipLoLat() +
////                                                                " " + shipLocation.getShipLoLong());
////
////                                                        // เพิ่ม Geofence
////                                                        geofenceHelper.addGeofence(geofenceID,
////                                                                shipLocation.getShipLoLat(),
////                                                                shipLocation.getShipLoLong());
////
////                                                        // อัปเดตค่า isGeofenceAdded เป็น true
////                                                        shipment.setGeofenceAdded(true);
////                                                        shipmentListViewModel.update(shipment);
////                                                    }
////                                                });
////                                    } else {
////                                        Log.d(TAG, "Geofence already added, skipping...");
////                                    }
////                                }
////                            }
////                            shipmentLiveData.removeObserver(this);
////                        }
////                    });
////
////                    LiveData<Trip> tripLiveData = tripViewModel.getTripByCodeFromSharedPreferences(requireContext());
////                    tripLiveData.observe(getViewLifecycleOwner(), new Observer<Trip>() {
////                        @Override
////                        public void onChanged(Trip trip) {
////                            if (trip != null) {
////                                Log.d(TAG, "trip : " + trip.getTripCode());
////                                Trip aTrip = trip;
////                                if (edittxt_detectnum != null) {
////                                    Log.d(TAG, "trip : " + trip.getTripCode());
////                                    aTrip.setTripMileageOut(Double.parseDouble(edittxt_detectnum.getText().toString()));
////                                } else {
////                                    aTrip.setTripMileageOut(Double.parseDouble(txtview_detectnum.getText().toString()));
////                                }
////                                aTrip.setTripTimeOut(txtview_time.getText().toString());
////                                tripViewModel.update(aTrip);
////                            } else {
////                                Log.d(TAG, "Trip not found");
////                            }
////                            //ลบ Observer หลังทำงาน
////                            tripLiveData.removeObserver(this);
////                        }
////                    });
//
//
//                    // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
//                    new Handler().postDelayed(() -> {
//                        transaction.commit();
//                    }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
//                }
//            }
//        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mileType = SharedPreferencesHelper.getMileType(getContext());
                if(mileType == 1 || mileType == 3){
                    // ขาออก และ ขาถึง
                    Log.d(TAG, "MileType: "+mileType);
                    requestLocation(mileType);
                    ShipDetailFragment frag_shipdetail = ShipDetailFragment.newInstance();
                    // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, frag_shipdetail);
                    // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
                    new Handler().postDelayed(() -> {
                        transaction.commit();
                    }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
                }else {
                    // ขาเข้า
                    Log.d(TAG, "MileType: "+mileType);
                    requestLocation(mileType);
                    Intent intent = new Intent(getActivity(), SplashActivity.class);
                    // ถ้ามัน sync แล้วให้ล้างข้อมูลเลย
                    new Handler().postDelayed(() -> {
                        InvoiceViewModel.resetInstance();
                        requireActivity().finish();
                        startActivity(intent);
                        SharedPreferencesHelper.setUserLoggedIn(requireContext(), false);
                        SharedPreferencesHelper.setEmployee(requireContext(), 0);
//                        transaction.commit();
                    }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
                }


//                switch (SharedPreferencesHelper.getMileIn(getContext())){
//                    case 1:
//                        //ขาออก
////                        startLocationService();
//                        requestLocation(2, 1);
//                        ShipDetailFragment frag_shipdetail = ShipDetailFragment.newInstance();
//                        // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
//                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                        transaction.replace(R.id.fragment_container, frag_shipdetail);
//                        // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
//                        new Handler().postDelayed(() -> {
//                            transaction.commit();
//                        }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
//                        break;
//
//                    case 2:
//                        //ขาเข้า
////                        stopLocationService();
//                        break;
//
//                    case 3:
//                        //ถึงสถานที่
//                        break;
//                }
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

    private void requestLocation(int mileType) {
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
                    if(mileType == 1){
                        updateInvoice(2);
                        updateMile(1, mileType);
                    }else if(mileType == 3){
                        Executors.newSingleThreadExecutor().execute(() -> {
                            int nextSeq = invoiceViewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
                            updateMile(nextSeq, mileType);
                            //remove geofence here and delete geofenceID
                        });
                    }else if(mileType == 2){
                        Executors.newSingleThreadExecutor().execute(() -> {
                            int nextSeq = invoiceViewModel.getNextMileLogSeq(SharedPreferencesHelper.getTrip(getContext()));
                            Log.d(TAG, "Next Seq MileLog: "+nextSeq);
                            updateMile(nextSeq, mileType);
                            //remove geofence here and delete geofenceID
                        });
                    }

                }
            });
        }
    }

    private void updateMile(int seq, int mileType){
        int mileRecord;
        if (edittxt_detectnum != null) {
            mileRecord = Integer.parseInt(edittxt_detectnum.getText().toString());
            Log.d(TAG, "Mile: "+mileRecord);
        } else {
            mileRecord = Integer.parseInt(txtview_detectnum.getText().toString());
            Log.d(TAG, "Mile: "+mileRecord);
        }
        invoiceViewModel.updateMile(requireContext(), seq, mileRecord, imageTimestamp,latitude, longitude, imagePath, mileType);
    }

    private void updateInvoice(int seq){
        //updatestatus and generate geofenceID => กำลังจัดส่ง (2)
        Map<String, String> geofenceMap = new HashMap<>();

        LiveData<List<Invoice>> invoiceLiveData = invoiceViewModel.getinvoiceByTrip(requireContext());
        invoiceLiveData.observe(getViewLifecycleOwner(), new Observer<List<Invoice>>() {
            @Override
            public void onChanged(List<Invoice> invoiceList) {
                if(invoiceList != null && !invoiceList.isEmpty()){
                    Log.d(TAG, "Invoice Data: " + invoiceList.size());

                    for (Invoice invoice : invoiceList){
                        String invoiceCode = invoice.getInvoiceCode();
                        invoiceViewModel.updateInvoice(invoice, seq,2, latitude, longitude, imageTimestamp, requireContext());

                        // add Geofence ID
                        if(invoice.getGeofenceID() == null || invoice.getGeofenceID().isEmpty()){
                            String generateID = UUID.randomUUID().toString();
                            invoice.setGeofenceID(generateID);
                            geofenceMap.put(invoiceCode, generateID);
                            Log.d(TAG, "New GeofenceID assigned: " + generateID + " to invoice: "+invoiceCode);
                            // update ?
                            invoiceViewModel.update(invoice);
                        } else {
                            geofenceMap.put(invoiceCode, invoice.getGeofenceID());
                            Log.d(TAG, "GeofenceID already exists: " + invoice.getGeofenceID()+ " to invoice: "+invoiceCode);
                        }

//                        if (invoice.getGeofenceID() != null){
////                            geofenceHelper = GeofenceHelper.getInstance(requireContext());
////                            String geofenceID = geofenceMap.get(invoiceCode);
////                            geofenceHelper.addGeofence(geofenceID, invoice.getShipLoLat(), invoice.getShipLoLong());
//
//                        } else {
//                            Log.d(TAG, "Geofence already added, skipping...");
//                        }
                    }
                }
                Log.d(TAG, "observe null");
                invoiceLiveData.removeObserver(this);
            }
        });
    }


}