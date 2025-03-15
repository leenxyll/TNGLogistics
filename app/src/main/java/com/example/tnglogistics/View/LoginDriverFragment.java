package com.example.tnglogistics.View;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Truck;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.TripViewModel;
import com.example.tnglogistics.ViewModel.TruckViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginDriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginDriverFragment extends Fragment {
    private TruckViewModel truckViewModel;
    private TripViewModel tripViewModel;
    private Truck aTruck;
    private static final String TAG = "LoginDriverFragment";
    public LoginDriverFragment() {
        // Required empty public constructor
    }

    public static LoginDriverFragment newInstance() {
        return new LoginDriverFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_driver, container, false);

        truckViewModel = TruckViewModel.getInstance(requireActivity().getApplication());
        tripViewModel = TripViewModel.getInstance(requireActivity().getApplication());

        EditText edt_truckreg = view.findViewById(R.id.edt_truckreg);
        Button btn_start = view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String truckreg = edt_truckreg.getText().toString();
                Log.d(TAG, ""+truckreg);
                if (!truckreg.isEmpty()) {
                    SharedPreferencesHelper.setUserLoggedIn(requireContext(), true);
                    truckViewModel.findOrCreateTruck(truckreg);
                    SharedPreferencesHelper.saveTruck(requireContext(), truckreg);
                    Toast.makeText(getContext(), "ลงทะเบียนสำเร็จ", Toast.LENGTH_SHORT).show();

                    truckViewModel.getTruckByRegFromSharedPreferences(requireContext()).observe(getViewLifecycleOwner(), truck -> {
                        if (truck != null) {
                            aTruck = truck;
//                            Toast.makeText(getContext(), "ลงทะเบียนด้วยทะเบียนรถ : " + truck.getTruckReg() + " : " + truck.getTruckCode(), Toast.LENGTH_SHORT).show();
//                            tripViewModel.createTrip(aTruck.getTruckCode());

                            if (SharedPreferencesHelper.getTrip(requireContext()) == 0) {
                                tripViewModel.createTrip(aTruck.getTruckCode()).observe(getViewLifecycleOwner(), tripCode -> {
                                    if (tripCode != null) {
                                        // บันทึก tripCode ลง SharedPreferences หรือทำการอัพเดท UI
                                        SharedPreferencesHelper.saveTrip(requireContext(), tripCode);
                                        Log.d(TAG, "Trip created: " + tripCode);
                                        SharedPreferencesHelper.saveLastFragment(requireContext(), "");
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        requireActivity().finish(); // ปิด Fragment หรือ Activity ปัจจุบัน (ถ้าต้องการ)
                                        startActivity(intent); // เรียก startActivity() เพื่อเปิด Activity ใหม่
                                    } else {
                                        Log.e(TAG, "Failed to create trip");
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "ไม่พบทะเบียนรถที่ตรงกับข้อมูลที่เก็บไว้", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "Trip already exists");
                            // ถ้า tripCode มีแล้ว สามารถข้ามการสร้าง trip ได้
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            requireActivity().finish();
                            startActivity(intent);
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "กรุณากรอกทะเบียนรถ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

}