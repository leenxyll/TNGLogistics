package com.example.tnglogistics.View;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;
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
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.example.tnglogistics.Controller.GeofenceHelper;
import com.example.tnglogistics.Controller.LocationService;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.TextRecognitionHelper;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ShipLocationViewModel;

import java.util.Date;
import java.util.List;
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
    private OnBackPressedCallback callback;
    private ShipLocationViewModel shipLocationViewModel;
    private GeofenceHelper geofenceHelper;
    private LocationService locationService;

    public static PreviewPictureFragment newInstance() {
        return new PreviewPictureFragment();
    }

    public OnBackPressedCallback getCallback() {
        return callback;
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
                    } else {
                        txtview_detectnum.setText(result);
                        btn_confirm.setVisibility(View.VISIBLE);
                        imgview_edt.setVisibility(View.VISIBLE);
                        btn_opencameara_agian.setVisibility(View.GONE);
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

//        // สังเกตตำแหน่งจาก LocationService
//        locationService = new LocationService(); // หรือ bindService() ถ้าต้องการ bound service
//        locationService.getLocationLiveData().observe(this, new Observer<Location>() {
//            @Override
//            public void onChanged(Location location) {
//                // รับตำแหน่งที่อัปเดต
//                if (location != null) {
//                    Log.d("MainActivity", "Updated Location: " + location.getLatitude() + ", " + location.getLongitude());
//                }
//            }
//        });

        imgview_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // เปลี่ยน TextView เป็น EditText
                EditText editText = new EditText(v.getContext());
                editText.setText(txtview_detectnum.getText());  // ใช้ข้อความจาก TextView
                editText.setTextColor(txtview_detectnum.getCurrentTextColor());
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);  // ใช้ขนาดตัวอักษรเดิม
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                // ลบ TextView ออกและเพิ่ม EditText เข้าไปแทน
                ((LinearLayout) v.getParent()).removeView(txtview_detectnum);
                ((LinearLayout) v.getParent()).addView(editText, 1); // ใส่ที่ตำแหน่งที่ต้องการใน LinearLayout
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

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<ShipLocation> shipLocationList = shipLocationViewModel.getShipLocationList().getValue();
                if (shipLocationList != null) {
                    for (int i = 0; i < shipLocationList.size(); i++) {
                        String generatedId = UUID.randomUUID().toString();
                        shipLocationViewModel.updateGeofenceID(i, generatedId, 0.0, 0.0);
                        geofenceHelper = GeofenceHelper.getInstance(requireContext());
                        geofenceHelper.addGeofence(generatedId, shipLocationViewModel.getLocation(i).getShipLoLat(), shipLocationViewModel.getLocation(i).getShipLoLong());
//                        recycleAddrViewModel.updateItemId(i, generatedId); // อัปเดต ID ให้แต่ละตัว
//                        geofenceHelper.addGeofence(generatedId, recycleAddrViewModel.getItem(i).getLatLng());
                    }
                }

                // สร้าง Fragment ใหม่ที่ต้องการแสดง
                StatusFragment frag_status = new StatusFragment();

                // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, frag_status);  // R.id.fragment_container คือ ID ของ ViewGroup ที่ใช้สำหรับแสดง Fragment
//                transaction.addToBackStack(null);  // เพื่อให้สามารถกดปุ่ม back กลับไปยัง Fragment ก่อนหน้าได้
                transaction.commit();
            }
        });

//        Button btn_confirm = view.findViewById(R.id.btn_confirm);
//        btn_confirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (listener != null) {
////                    listener.onFragmentPreviewPictureButtonClicked();
////                }
//            }
//        });

        return view;
    }
}