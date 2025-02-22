package com.example.tnglogistics.Controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeocodeHelper {
    private static final String TAG = "GeocodeHelper";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface GeocodeCallback {
        void onAddressFetched(LatLng latlng);
    }

    public static void getLatLngAsync(Context context, String address, GeocodeCallback callback) {
        executor.execute(() -> {
            LatLng result = getLatLngFromAddress(context, address);
            new Handler(Looper.getMainLooper()).post(() -> callback.onAddressFetched(result));
        });
    }

    private static LatLng getLatLngFromAddress(Context context, String address) {
        Log.d(TAG, "call Geocoder");
        Log.d(TAG, "address "+ address);
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
//            Log.d(TAG, "In Try");
            if (addresses != null && !addresses.isEmpty()) {
//                Log.d(TAG, "In If");
                Address location = addresses.get(0);
//                Log.d(TAG, address);
//                Log.d(TAG, location.getAddressLine(0));
                LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                latitude = location.getLatitude();
//                longitude = location.getLongitude();
//                Log.d(TAG, Double.toString(latitude));
//                Log.d(TAG, Double.toString(longitude));
//                textResultLat.setText(Double.toString(latitude));
//                textResultLng.setText(Double.toString(longitude));
                return locationLatLng;
//                textResult.setText(String.format(Locale.getDefault(), "Latitude: %.6f\nLongitude: %.6f", latitude, longitude));
            } else {
//                textResult.setText("ไม่พบพิกัดสำหรับที่อยู่นี้");
            }
        } catch (IOException e) {
//            Log.d(TAG, "In Catch");
//            Log.e(TAG, String.valueOf(e));
            e.printStackTrace();
//            textResult.setText("เกิดข้อผิดพลาดในการแปลงที่อยู่");
        }
        return new LatLng(0.0, 0.0);
    }
}
