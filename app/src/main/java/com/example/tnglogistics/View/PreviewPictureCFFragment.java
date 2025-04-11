package com.example.tnglogistics.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
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
import com.example.tnglogistics.Controller.PermissionManager;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Controller.TextRecognitionHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.InvoiceViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreviewPictureCFFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewPictureCFFragment extends Fragment {
    private static String TAG ="PreviewPictureCFFragment";
    private Button btn_confirmPic;
    private String imagePath;
    private long imageTimestamp;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private InvoiceViewModel invoiceViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private Invoice aInvoice;


    public PreviewPictureCFFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static PreviewPictureCFFragment newInstance() {
        return new PreviewPictureCFFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        PermissionManager.startGPSMonitoring(requireContext(), requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        PermissionManager.stopGPSMonitoring();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview_picture_c_f, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        invoiceViewModel = InvoiceViewModel.getInstance(requireActivity().getApplication());
        ImageView imgview_preview = view.findViewById(R.id.imgview_preview);

        Bundle args = getArguments();
        // Observe ข้อมูลที่จัดกลุ่มตาม Location
        invoiceViewModel.getInvoiceBySeq().observe(getViewLifecycleOwner(), invoices -> {
            if (invoices != null) {
                for(Invoice invoice : invoices){
                    aInvoice = invoice;
                    if (args != null) {
                        imagePath = args.getString("image_path");
                        imageTimestamp = args.getLong("image_timestamp", 0);
                        btn_confirmPic = view.findViewById(R.id.btn_confirmPic);
                        btn_confirmPic.setVisibility(View.VISIBLE);
                        btn_confirmPic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // ส่ง callback ไปที่ requestLocation
                                requestLocation(() -> {

                                    ShipDetailFragment frag_shipdetail = ShipDetailFragment.newInstance();
                                    // ใช้ FragmentTransaction เพื่อแทนที่ Fragment ใน MainActivity
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.fragment_container, frag_shipdetail);
                                    // ใช้ Handler หรือ postDelayed เพื่อรอให้ข้อมูลเสร็จก่อนการแทนที่ Fragment
                                    new Handler().postDelayed(() -> {
                                        transaction.commit();
                                    }, 500);  // รอให้ข้อมูลอัปเดตก่อน 500ms (คุณสามารถปรับเวลาให้เหมาะสม)
                                });
                            }
                        });


                        if (imagePath != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            imgview_preview.setImageBitmap(bitmap);
                        }
                        if (imageTimestamp != 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                            String formattedTime = sdf.format(new Date(imageTimestamp));
//                txtview_time.setText(formattedTime);
                        }
                    }
                }
            }
        });

        return view;
    }

    private void requestLocation(Runnable onCompleteCallback) {
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
                    // อัปเดทสถานะ Invoice
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int nextSeq = invoiceViewModel.getNextInvoiceLogSeq(aInvoice.getInvoiceCode());
                        Log.d(TAG, "Next Seq InvoiceShipLog: " + nextSeq);
                        String invoiceCode = aInvoice.getInvoiceCode();
                        invoiceViewModel.updateInvoice(aInvoice, nextSeq, 4, latitude, longitude, imageTimestamp, requireContext());
                        Log.d(TAG, "update invoice: " + invoiceCode);
//                        aInvoice.setInvoiceShipStatusCode(4);
//                        invoiceViewModel.update(aInvoice);
                        //remove geofence here and delete geofenceID

                        // เรียกใช้ callback บน UI thread หลังจากที่ update เสร็จแล้ว
                        if (onCompleteCallback != null) {
                            requireActivity().runOnUiThread(onCompleteCallback);
                        }
                    });
                } else {
                    // กรณีที่ไม่สามารถรับ location ได้ ก็เรียก callback เพื่อไปต่อ
                    if (onCompleteCallback != null) {
                        requireActivity().runOnUiThread(onCompleteCallback);
                    }
                }
            }).addOnFailureListener(e -> {
                // กรณีที่เกิด error ก็เรียก callback เพื่อไปต่อ
                Log.e(TAG, "Failed to get location", e);
                if (onCompleteCallback != null) {
                    requireActivity().runOnUiThread(onCompleteCallback);
                }
            });
        }
    }
}