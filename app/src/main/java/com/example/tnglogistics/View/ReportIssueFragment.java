package com.example.tnglogistics.View;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.tnglogistics.Controller.AdapterSubIssue;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.Model.Issue;
import com.example.tnglogistics.Model.SubIssue;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.ViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReportIssueFragment extends Fragment implements
        AdapterSubIssue.OnSubIssueClickListener,
        AdapterSubIssue.OnTakePhotoClickListener,
        AdapterSubIssue.OnDeletePhotoClickListener,
        AdapterSubIssue.OnDescriptionChangedListener {

    private static final String TAG = "ReportIssueFragment";
    private ViewModel viewModel;
    private AdapterSubIssue adapter;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private String imagePath;
    private long imageTimestamp;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private static Invoice aInvoice;
    List<String> imagePaths = new ArrayList<>();
    private Uri currentPhotoUri;
    private SubIssue currentSubIssue;
    private int currentPosition;
    private String currentDescription;

//    public static ReportIssueFragment newInstance() {
//        return new ReportIssueFragment();
//    }

    public static ReportIssueFragment newInstance(Invoice invoice) {
        ReportIssueFragment fragment = new ReportIssueFragment();
//        Bundle args = new Bundle();
//        args.putSerializable("invoice", invoice);  // หรือใช้ Parcelable ถ้า Invoice implements Parcelable
//        fragment.setArguments(args);
        aInvoice = invoice;
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_issue, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // ตั้งค่า ViewModel
        viewModel = ViewModel.getInstance(requireActivity().getApplication());

        // ตั้งค่า RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewSubIssues);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdapterSubIssue(this, this, this, this);
        recyclerView.setAdapter(adapter);

        // ปุ่มยืนยัน
        Button confirmButton = view.findViewById(R.id.btn_confirm);
        confirmButton.setOnClickListener(v -> {
            if (currentSubIssue != null) {
                // ตรวจสอบว่าถ้าต้องการรูปภาพ แต่ยังไม่มีรูป
                if ("Y".equals(currentSubIssue.getRequirePic()) && adapter.getPhotos(currentSubIssue.getSubIssueTypeCode()).isEmpty()) {
                    Toast.makeText(requireContext(), "กรุณาถ่ายรูปเพื่อยืนยัน", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ดำเนินการเมื่อกดปุ่มยืนยัน
//                Toast.makeText(requireContext(), "เลือก: " + currentSubIssue.getSubIssueTypeName(), Toast.LENGTH_SHORT).show();
                reportDelivery(aInvoice.getInvoiceCode());
            } else {
                Toast.makeText(requireContext(), "กรุณาเลือกรายการ", Toast.LENGTH_SHORT).show();
            }
        });

        // ตั้งค่า Camera Launcher
//        cameraLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK && currentPhotoUri != null) {
//                        // เพิ่มรูปภาพลงใน adapter
//                        if (currentSubIssue != null) {
//                            adapter.addPhoto(currentSubIssue.getSubIssueTypeCode(), currentPhotoUri);
//                            Toast.makeText(requireContext(), "เพิ่มรูปภาพแล้ว", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (currentPhotoUri != null) {
                            File originalFile = new File(imagePath);
                            try {
                                File compressedFile = compressImage(originalFile);


                                imagePath = compressedFile.getAbsolutePath(); // ใช้ path ใหม่นี้แทน
                                currentPhotoUri = Uri.fromFile(compressedFile); // แสดงภาพที่ถูกบีบอัด

                                adapter.addPhoto(currentSubIssue.getSubIssueTypeCode(), currentPhotoUri);
//                                imageAdapter.addImage(photoUri);
                                imagePaths.add(imagePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "บีบอัดภาพล้มเหลว", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        // ดึงข้อมูล SubIssue จาก ViewModel
        loadSubIssues();

        return view;
    }

    private void loadSubIssues() {
        // เปลี่ยนตัวเลข 1 เป็นโค้ดประเภทปัญหาที่ต้องการ
        viewModel.getSubIssueByIssueTypeCode(1).observe(getViewLifecycleOwner(), subIssues -> {
            if (subIssues != null && !subIssues.isEmpty()) {
//                setupRadioButtons(subIssues);
                adapter.setSubIssues(subIssues);
            } else {
                Toast.makeText(requireContext(), "ไม่พบรายการ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSubIssueSelected(SubIssue subIssue, int position) {
        if (currentSubIssue != null && currentSubIssue.getSubIssueTypeCode() != (subIssue.getSubIssueTypeCode())) {
            adapter.clearPhotosExcept(subIssue.getSubIssueTypeCode()); // เคลียร์ภาพเก่า
        }
        currentSubIssue = subIssue;
        currentPosition = position;
//        currentDescription = description;
    }

    @Override
    public void onTakePhotoClicked(SubIssue subIssue, int position) {
        dispatchTakePictureIntent();
        currentSubIssue = subIssue;
        currentPosition = position;
    }

    @Override
    public void onDeletePhotoClicked(SubIssue subIssue, int position, Uri photoUri) {
        // ลบรูปภาพ
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ลบรูปภาพ");
        builder.setMessage("คุณต้องการลบรูปภาพนี้ใช่หรือไม่?");
        builder.setPositiveButton("ใช่", (dialog, which) -> {
            adapter.removePhoto(subIssue.getSubIssueTypeCode(), photoUri);
            imagePaths.remove(position);
            Toast.makeText(requireContext(), "ลบรูปภาพแล้ว", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("ไม่", null);
        builder.show();

        // ลบไฟล์จริงจากอุปกรณ์
        new File(photoUri.getPath()).delete();

    }

    @Override
    public void onDescriptionChanged(String description) {
        // บันทึก description ที่ถูกเปลี่ยนแปลง
        currentDescription = description;

        // คุณสามารถใช้ description นี้เพื่อทำการอัปเดตข้อมูลใน ViewModel หรือที่อื่นๆ
        Log.d(TAG, "Description changed: " + currentDescription);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }

            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        long timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(timestamp));
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("ShipPic_" + formattedTime, ".jpg", storageDir);
        imagePath = image.getAbsolutePath(); // ได้ไฟล์ path ที่คุณต้องการ
        imageTimestamp = System.currentTimeMillis();
        return image;
    }

    private File compressImage(File originalFile) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

        // ตรวจสอบขนาดไฟล์ หลังจากบีบอัดครั้งแรก
        int quality = 80; // เริ่มต้นที่ 80% คุณภาพ

        // ทำการบีบอัดภาพครั้งแรก
        File compressedFile = new File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "COMPRESSED_" + originalFile.getName()
        );

        FileOutputStream out = new FileOutputStream(compressedFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out); // บีบอัดที่ 80% ของคุณภาพ
        out.flush();
        out.close();

        // ตรวจสอบขนาดไฟล์
        long fileSize = compressedFile.length();
        long maxSize = 300 * 1024; // 300KB (ปรับขนาดตามต้องการ)

        // ถ้าขนาดไฟล์ยังเกิน 300KB, ลดคุณภาพการบีบอัดเพิ่ม
        while (fileSize > maxSize && quality > 20) {  // ลดคุณภาพลงทีละ 10%
            quality -= 10;
            out = new FileOutputStream(compressedFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
            fileSize = compressedFile.length();
        }

        // ถ้าขนาดไฟล์ยังเกิน 300KB ให้ลดขนาดภาพ
        if (fileSize > maxSize) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false);
            out = new FileOutputStream(compressedFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out); // ลดคุณภาพอีกครั้ง
            out.flush();
            out.close();
        }

        // ลบไฟล์ต้นฉบับหลังจากบีบอัดแล้ว
        boolean deleted = originalFile.delete();
        if (!deleted) {
            Log.w(TAG, "ไม่สามารถลบไฟล์ต้นฉบับได้: " + originalFile.getAbsolutePath());
        }

        return compressedFile;
    }

    private void reportDelivery(String invoiceCode) {
        // Check if at least one image is uploaded
        // ดึงรายการ SubIssue ทั้งหมดจาก adapter
//        List<SubIssue> subIssueList = adapter.getSubIssues();  // ต้องเพิ่ม getSubIssues() ใน AdapterSubIssue
//
//        for (SubIssue subIssue : subIssueList) {
//            if ("Y".equals(subIssue.getRequirePic())) {
//                List<Uri> photos = adapter.getPhotos(subIssue.getSubIssueTypeCode());
//                if (photos == null || photos.isEmpty()) {
//                    Toast.makeText(getContext(), "กรุณาถ่ายรูปสำหรับ: " + subIssue.getSubIssueTypeName(), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }
//        }

        if (currentSubIssue != null && "Y".equals(currentSubIssue.getRequirePic())) {
            List<Uri> photos = adapter.getPhotos(currentSubIssue.getSubIssueTypeCode());
            if (photos == null || photos.isEmpty()) {
                Toast.makeText(getContext(), "กรุณาถ่ายรูปสำหรับ: " + currentSubIssue.getSubIssueTypeName(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.d(TAG, "imagePaths size: " + imagePaths.size());

        Executors.newSingleThreadExecutor().execute(() -> {
            viewModel.updateShipmentPicture(invoiceCode, imageTimestamp, imagePaths, 3); // เรียกใหม่
        });

        requestLocation(() -> {
            SharedPreferencesHelper.saveMileType(requireContext(), 1);
            SharedPreferencesHelper.setCameraMile(requireContext(), true);
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
                        int nextSeq = viewModel.getNextInvoiceLogSeq(aInvoice.getInvoiceCode());
                        Log.d(TAG, "Next Seq InvoiceShipLog: " + nextSeq);
                        String invoiceCode = aInvoice.getInvoiceCode();
//                        viewModel.updateInvoiceStatus(aInvoice, nextSeq, 5, latitude, longitude, imageTimestamp, requireContext());
//                        Log.d(TAG, "update invoice: " + invoiceCode);

                        //เพิ่ม Issue / อัปเดท Invoice พร้อมใส่ IssueCode ไปด้วย

//                        Issue aIssue = new Issue(currentDescription, 0, latitude, longitude, imageTimestamp, currentSubIssue.getSubIssueTypeCode(), false);
//                        viewModel.insertIssue(aIssue);
//                        Log.d(TAG, "des : " + aIssue.getIssueDescription());

//                        viewModel.updateInvoiceWithIssue(aInvoice, nextSeq, 5, latitude, longitude, imageTimestamp, requireContext(), aIssue);
                        viewModel.updateInvoiceStatus(aInvoice, nextSeq, 5, latitude, longitude, imageTimestamp, currentDescription, currentSubIssue.getSubIssueTypeCode(), requireContext());
                        Log.d(TAG, "update invoice: " + invoiceCode);

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