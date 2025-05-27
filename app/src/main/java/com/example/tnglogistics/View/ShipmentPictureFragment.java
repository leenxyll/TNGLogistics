package com.example.tnglogistics.View;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Controller.AdapterImageHelper;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Invoice;
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

public class ShipmentPictureFragment extends Fragment implements AdapterImageHelper.OnImageDeleteListener {
    private static String TAG = "ShipmentPictureFragment";
    private RecyclerView recyclerViewImages;
    private AdapterImageHelper imageAdapter;
    private TextView txtImageCount;
    private LinearLayout layoutAddImage;
    private Button btn_confirmPic;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private String imagePath;
    private long imageTimestamp;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private ViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private Invoice aInvoice;
    private Uri photoUri;
    List<String> imagePaths = new ArrayList<>();


    public static ShipmentPictureFragment newInstance() {
        return new ShipmentPictureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipment_picture, container, false);

        // Initialize UI components
        recyclerViewImages = view.findViewById(R.id.recycler_view_images);
        txtImageCount = view.findViewById(R.id.txt_image_count);
        layoutAddImage = view.findViewById(R.id.layout_add_image);
        btn_confirmPic = view.findViewById(R.id.btn_confirmPic);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        viewModel = ViewModel.getInstance(requireActivity().getApplication());

//        cameraLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        if (photoUri != null) {
//                            imageAdapter.addImage(photoUri); // ยังใช้ Uri ได้อยู่
//                            imagePaths.add(imagePath);
//                            Log.d(TAG, "File path: " + imagePath); // ได้ path แบบเต็ม
//                            updateImageCounter();
//                        }
//                    }
//                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoUri != null) {
                            File originalFile = new File(imagePath);
                            try {
                                File compressedFile = compressImage(originalFile);


                                imagePath = compressedFile.getAbsolutePath(); // ใช้ path ใหม่นี้แทน
                                photoUri = Uri.fromFile(compressedFile); // แสดงภาพที่ถูกบีบอัด

                                imageAdapter.addImage(photoUri);
                                imagePaths.add(imagePath);
                                updateImageCounter();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "บีบอัดภาพล้มเหลว", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });


        // Set up recycler view for images
        recyclerViewImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
        imageAdapter = new AdapterImageHelper(getContext(), this);
        recyclerViewImages.setAdapter(imageAdapter);
        viewModel.getInvoiceBySeq().observe(getViewLifecycleOwner(), invoices -> {
            if (invoices != null) {
                for (Invoice invoice : invoices) {
                    aInvoice = invoice;
                    updateImageCounter();

                    // Set up click listeners
                    layoutAddImage.setOnClickListener(v -> {
                        if (!imageAdapter.canAddMoreImages()) {
                            Toast.makeText(getContext(), "คุณสามารถเพิ่มรูปได้สูงสุด 6 รูป", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dispatchTakePictureIntent();
                    });

                    btn_confirmPic.setOnClickListener(v -> {
                        confirmDelivery(aInvoice.getInvoiceCode());
                    });


                }
            }
        });

        return view;
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
                photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
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

//    private File compressImage(File originalFile) throws IOException {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);
//
//        File compressedFile = new File(
//                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//                "COMPRESSED_" + originalFile.getName()
//        );
//
//        FileOutputStream out = new FileOutputStream(compressedFile);
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out); // 50 = quality, ลองปรับระหว่าง 30–70
//        out.flush();
//        out.close();
//
//        return compressedFile;
//    }

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



    private void updateImageCounter() {
        int currentCount = imageAdapter.getItemCount();
        txtImageCount.setText("เพิ่มรูปภาพ\n" + currentCount + " / 6");

        // Update UI based on whether we can add more images
        if (!imageAdapter.canAddMoreImages()) {
            layoutAddImage.setAlpha(0.5f);
            layoutAddImage.setVisibility(View.GONE);
        } else {
            layoutAddImage.setAlpha(1.0f);
            layoutAddImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onImageDelete(int position) {
        // Show confirmation dialog before deleting
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ลบรูปภาพ");
        builder.setMessage("คุณต้องการลบรูปภาพนี้ใช่หรือไม่?");
        builder.setPositiveButton("ใช่", (dialog, which) -> {
            imageAdapter.removeImage(position);
            imagePaths.remove(position);
            updateImageCounter();
            Toast.makeText(requireContext(), "ลบรูปภาพแล้ว", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("ไม่", null);
        builder.show();

    }

    private void confirmDelivery(String invoiceCode) {
        // Check if at least one image is uploaded
        if (imageAdapter.getItemCount() == 0) {
            Toast.makeText(getContext(), "กรุณาเพิ่มรูปภาพอย่างน้อย 1 รูป", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, ""+imagePaths.size());

        Executors.newSingleThreadExecutor().execute(() -> {
            viewModel.updateShipmentPicture(invoiceCode, imageTimestamp, imagePaths, 2); // เรียกใหม่
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
                        viewModel.updateInvoiceStatus(aInvoice, nextSeq, 4, latitude, longitude, imageTimestamp, requireContext());
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