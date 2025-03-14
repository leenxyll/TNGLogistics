package com.example.tnglogistics.View;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Controller.LocationHelper;
import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.TextRecognitionHelper;
import com.example.tnglogistics.Model.ShipmentList;
import com.example.tnglogistics.Model.Trip;
import com.example.tnglogistics.Model.Truck;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;
import com.example.tnglogistics.ViewModel.ShipmentListViewModel;
import com.example.tnglogistics.ViewModel.TripViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreviewPictureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewPictureFragment extends Fragment {
    private static String TAG ="PreviewPictureFragment";
    private TextRecognitionHelper txtRecog;
    private ShipLocationViewModel shipLocationViewModel;
    private ShipmentListViewModel shipmentListViewModel;
    private TripViewModel tripViewModel;
    private GeofenceHelper geofenceHelper;
    private Location currentLocation;
//    private Trip aTrip;
    private EditText edittxt_detectnum = null;
    private TruckViewModel truckViewModel;
    private Truck aTruck;

    public static PreviewPictureFragment newInstance() {
        return new PreviewPictureFragment();
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "PreviewPictureFragment");
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
        TextView txtview_detectnum = view.findViewById(R.id.txtview_detectnum);
        LinearLayout container_milleage = view.findViewById(R.id.container_milleage);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        Button btn_opencameara_agian = view.findViewById(R.id.btn_opencamera_again);
        shipLocationViewModel = ShipLocationViewModel.getInstance(requireActivity().getApplication());
        shipmentListViewModel = ShipmentListViewModel.getInstance(requireActivity().getApplication());
        tripViewModel = TripViewModel.getInstance(requireActivity().getApplication());
        truckViewModel = TruckViewModel.getInstance(requireActivity().getApplication());

        String imagePath;
        long imageTimestamp;

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
                ((MainActivity) getActivity()).getCameraLauncher().launch(intent);
            }
        });


        LocationHelper.getInstance(requireContext()).getCurrentLocation(requireContext(), new LocationHelper.LocationListener() {
            @Override
            public void onLocationResult(Location location) {
                if(location != null){
                    Log.d(TAG, "Current Location:" + location.getLatitude()+ " " + location.getLongitude());
                    currentLocation = location;
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "On Click");

                if(SharedPreferencesHelper.getMileIn(requireContext())){
//                    //ขาเข้า
                    stopLocationService();
                    LiveData<Trip> tripLiveData = tripViewModel.getTripByCodeFromSharedPreferences(requireContext());
                    tripLiveData.observe(getViewLifecycleOwner(), new Observer<Trip>() {
                        @Override
                        public void onChanged(Trip trip) {
                            if (trip != null) {
                                Log.d(TAG, "trip : " + trip.getTripCode());
                                Trip aTrip = trip;
                                if (edittxt_detectnum != null) {
                                    Log.d(TAG, "trip : " + trip.getTripCode());
                                    aTrip.setTripMileageIn(Double.parseDouble(edittxt_detectnum.getText().toString()));
                                } else {
                                    aTrip.setTripMileageIn(Double.parseDouble(txtview_detectnum.getText().toString()));
                                }
                                aTrip.setTripTimeIn(txtview_time.getText().toString());
                                tripViewModel.update(aTrip);
                            } else {
                                Log.d(TAG, "Trip not found");
                            }
                            // 🛑 ลบ Observer หลังทำงาน ✅
                            tripLiveData.removeObserver(this);
                        }
                    });

                    // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
                    new Handler().postDelayed(() -> {
//                        transaction.commit();
                        SharedPreferencesHelper.saveLastFragment(requireContext(),"");
                        SharedPreferencesHelper.setUserLoggedIn(requireContext(),false);
                        Intent intent = new Intent(getActivity(), SplashActivity.class);
                        startActivity(intent); // เรียก startActivity() เพื่อเปิด Activity ใหม่
                        requireActivity().finish(); // ปิด Fragment หรือ Activity ปัจจุบัน (ถ้าต้องการ)
                    }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
                    SharedPreferencesHelper.saveMileIn(requireContext(), false);

                } else {

                    // ขาออก
                    // สร้าง Fragment ใหม่ที่ต้องการแสดง
                    StatusFragment frag_status = new StatusFragment();

                    // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, frag_status);  // R.id.fragment_container คือ ID ของ ViewGroup ที่ใช้สำหรับแสดง Fragment

                    startLocationService();

                    shipmentListViewModel.getShipmentListByTrip(SharedPreferencesHelper.getTrip(requireContext()))
                            .observe(getViewLifecycleOwner(), shipmentLists -> {
                    if (shipmentLists != null && !shipmentLists.isEmpty()) {
                        Log.d(TAG, "Shipment Data: " + shipmentLists.size());

                        for (ShipmentList shipment : shipmentLists) {
                            Log.d(TAG, "Loop in ShipmentList : " + shipment.getShipListSeq() +
                                    " LoCode : " + shipment.getShipListShipLoCode() +
                                    " geofenceID : " + shipment.getGeofenceID());

                            shipment.setShipListStatus("กำลังจัดส่ง");
                            shipment.setLatUpdateStatus(currentLocation.getLatitude());
                            shipment.setLongUpdateStatus(currentLocation.getLongitude());
                            shipment.setLastUpdateStatus(txtview_time.getText().toString());

                            // ตรวจสอบว่า GeofenceID มีค่าอยู่แล้วหรือไม่
                            if (shipment.getGeofenceID() == null || shipment.getGeofenceID().isEmpty()) {
                                String generatedId = UUID.randomUUID().toString();
                                shipment.setGeofenceID(generatedId);
                                Log.d(TAG, "New GeofenceID assigned: " + generatedId);
                                shipmentListViewModel.update(shipment); // อัปเดตข้อมูลใหม่
                            } else {
                                Log.d(TAG, "GeofenceID already exists: " + shipment.getGeofenceID());
                            }

                            // ตรวจสอบว่าต้องเพิ่ม Geofence หรือไม่ (เช็คค่า isGeofenceAdded)
                            if (shipment.getGeofenceID() != null && !shipment.isGeofenceAdded()) {
                                geofenceHelper = GeofenceHelper.getInstance(requireContext());
                                shipLocationViewModel.getShipLocationByCode(shipment.getShipListShipLoCode())
                                        .observe(getViewLifecycleOwner(), shipLocation -> {
                                if (shipLocation != null) {
                                    Log.d(TAG, "Add Geofence : " + shipment.getGeofenceID() +
                                            " " + shipLocation.getShipLoLat() +
                                            " " + shipLocation.getShipLoLong());

                                    // เพิ่ม Geofence
                                    geofenceHelper.addGeofence(shipment.getGeofenceID(),
                                            shipLocation.getShipLoLat(),
                                            shipLocation.getShipLoLong());

                                    // อัปเดตค่า isGeofenceAdded เป็น true
                                    shipment.setGeofenceAdded(true);
                                    shipmentListViewModel.update(shipment);
                                }
                                        });
                            } else {
                                Log.d(TAG, "Geofence already added, skipping...");
                            }
                        }
                    }

//                                tripViewModel.getTripByCodeFromSharedPreferences(requireContext()).observe(getViewLifecycleOwner(), trip -> {
//                                    //                        Log.d(TAG, "trip : "+ trip.getTripCode());
//                                if (trip != null) {
//                                    Log.d(TAG, "trip : " + trip.getTripCode());
//                                    Trip aTrip = trip;
//                                    if(edittxt_detectnum != null){
//                                        aTrip.setTripMileageOut(Double.parseDouble(edittxt_detectnum.getText().toString()));
//                                    }else{
//                                        aTrip.setTripMileageOut(Double.parseDouble(txtview_detectnum.getText().toString()));
//                                    }
//                                    aTrip.setTripTimeOut(txtview_time.getText().toString());
//                                    tripViewModel.update(aTrip);
//                                } else {
//                                    Log.d(TAG, "Trip not found");
//                                }
//                                });

                                LiveData<Trip> tripLiveData = tripViewModel.getTripByCodeFromSharedPreferences(requireContext());
                                tripLiveData.observe(getViewLifecycleOwner(), new Observer<Trip>() {
                                    @Override
                                    public void onChanged(Trip trip) {
                                        if (trip != null) {
                                            Log.d(TAG, "trip : " + trip.getTripCode());
                                            Trip aTrip = trip;
                                            if (edittxt_detectnum != null) {
                                                Log.d(TAG, "trip : " + trip.getTripCode());
                                                aTrip.setTripMileageOut(Double.parseDouble(edittxt_detectnum.getText().toString()));
                                            } else {
                                                aTrip.setTripMileageOut(Double.parseDouble(txtview_detectnum.getText().toString()));
                                            }
                                            aTrip.setTripTimeOut(txtview_time.getText().toString());
                                            tripViewModel.update(aTrip);
                                        } else {
                                            Log.d(TAG, "Trip not found");
                                        }
                                        // 🛑 ลบ Observer หลังทำงาน ✅
                                        tripLiveData.removeObserver(this);
                                    }
                                });

                            });


                    // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
                    new Handler().postDelayed(() -> {
                        transaction.commit();
                }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
                }
            }
        });



        return view;
    }

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

}