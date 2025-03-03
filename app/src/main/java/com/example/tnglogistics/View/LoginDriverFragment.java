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
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.TruckViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginDriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginDriverFragment extends Fragment {
    private TruckViewModel truckViewModel;
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
        EditText edt_truckreg = view.findViewById(R.id.edt_truckreg);
        Button btn_start = view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String truckreg = edt_truckreg.getText().toString();
                Log.d(TAG, ""+truckreg);
                if (!truckreg.isEmpty()) {
                    SharedPreferencesHelper.saveTruck(requireContext(), truckreg);
                    SharedPreferencesHelper.setUserLoggedIn(requireContext(), true);
                    truckViewModel.addTruck(truckreg);
                    Toast.makeText(getContext(), "ลงทะเบียนสำเร็จ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent); // เรียก startActivity() เพื่อเปิด Activity ใหม่
                    requireActivity().finish(); // ปิด Fragment หรือ Activity ปัจจุบัน (ถ้าต้องการ)
                } else {
                    Toast.makeText(getContext(), "กรุณากรอกทะเบียนรถ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

}