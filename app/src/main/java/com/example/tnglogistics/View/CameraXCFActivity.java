package com.example.tnglogistics.View;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.tnglogistics.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraXCFActivity extends AppCompatActivity {
    private static final String TAG = "CameraXCFActivity";
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private String formattedTime;
    private long timestamp;
    private CameraControl cameraControl;
    private ScaleGestureDetector scaleGestureDetector;
    private float currentZoomRatio = 1f; // เริ่มต้นที่ 1x


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_xcfactivity);

        Button btn_capture = findViewById(R.id.btn_capture);
        previewView = findViewById(R.id.preview_view);
        startCamera();

//        เพิ่ม ScaleGestureDetector เพื่อจับการซูม
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                currentZoomRatio *= scaleFactor;
                currentZoomRatio = Math.max(1f, Math.min(currentZoomRatio, 5f)); // จำกัดซูมที่ 1x - 5x

                if (cameraControl != null) {
                    cameraControl.setZoomRatio(currentZoomRatio);
                }
                return true;
            }
        });

//        เพิ่ม Gesture Listener ให้ PreviewView
        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
                cameraControl = camera.getCameraControl(); // เก็บ CameraControl ไว้ใช้ซูม
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void takePicture() {
        timestamp = System.currentTimeMillis(); // เวลาถ่ายรูป (หน่วยเป็น milliseconds)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formattedTime = sdf.format(new Date(timestamp));


        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + formattedTime + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // TakePicture
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String savedImagePath = photoFile.getAbsolutePath(); // ใช้ path ของไฟล์ที่เรากำหนดเอง

                String croppedImagePath = cropImage(savedImagePath); // crop รูป

                Intent intent = new Intent();
                intent.putExtra("image_path", croppedImagePath); // ส่ง path ของรูปกลับไป
                intent.putExtra("image_timestamp", timestamp);
                setResult(RESULT_OK, intent);
                finish(); // ปิด Activity

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraXCFActivity.this, "Error taking picture", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String cropImage(String imagePath) {
        Bitmap original = BitmapFactory.decodeFile(imagePath);

        // ตรวจสอบการหมุนของภาพจาก Exif
        int rotation = getImageRotation(imagePath);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            original = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        }

        // อ่านขนาดจริงของภาพที่ถ่ายได้
        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        Log.d(TAG, "Image Size: " + imageWidth + "x" + imageHeight);

        // อ่านขนาดของ PreviewView ที่ใช้แสดงภาพจากกล้อง
        int previewWidth = previewView.getWidth();
        int previewHeight = previewView.getHeight();
        Log.d(TAG, "PreviewView Size: " + previewWidth + "x" + previewHeight);

        // คำนวณอัตราส่วนของ PreviewView เทียบกับภาพจริง
        float scaleX = (float) imageWidth / previewWidth;
        float scaleY = (float) imageHeight / previewHeight;
        Log.d(TAG, "Scale Factors: scaleX=" + scaleX + ", scaleY=" + scaleY);

        // พิกัดของกรอบการครอปใน PreviewView (Rect(90, 930, 990, 1230))
        int previewCropLeft = 0;
        int previewCropTop = 0;
        int previewCropRight = 1080;
        int previewCropBottom = 2161;

        // คำนวณตำแหน่งการครอปในภาพจริงจากพิกัดใน PreviewView
        int cropX = (int) (previewCropLeft * scaleX);
        int cropY = (int) (previewCropTop * scaleY);
        int cropWidth = (int) ((previewCropRight - previewCropLeft) * scaleX);
        int cropHeight = (int) ((previewCropBottom - previewCropTop) * scaleY);

        // ตรวจสอบว่า cropX และ cropY ไม่เกินขนาดของรูปภาพจริง
        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        cropWidth = Math.min(imageWidth - cropX, cropWidth);
        cropHeight = Math.min(imageHeight - cropY, cropHeight);

        Log.d(TAG, "Crop Area on Image: cropX=" + cropX + ", cropY=" + cropY + ", cropWidth=" + cropWidth + ", cropHeight=" + cropHeight);

        // ครอปภาพ
        Bitmap cropped = Bitmap.createBitmap(original, cropX, cropY, cropWidth, cropHeight);

        // ใช้ฟังก์ชัน resizeBitmap() ก่อนบันทึก
        Bitmap resizedBitmap = resizeBitmap(cropped, 600);

        File croppedFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                formattedTime + ".jpg");

        try (FileOutputStream out = new FileOutputStream(croppedFile)) {
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Cropped Image Saved: " + croppedFile.getAbsolutePath());

        return croppedFile.getAbsolutePath();
    }

    // ปรับขนาดรูปให้กว้างหรือสูงสุดที่ 300px
    private Bitmap resizeBitmap(Bitmap original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        // คำนวณอัตราส่วน
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    // อ่านค่า Exif เพื่อเช็คการหมุนของภาพ
    private int getImageRotation(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

}