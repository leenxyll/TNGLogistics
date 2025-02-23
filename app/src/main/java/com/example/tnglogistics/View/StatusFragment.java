package com.example.tnglogistics.View;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.RecycleAddrViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {
    private static final String TAG = "StatusFragment";
    private RecycleAddrViewModel recycleAddrViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        recycleAddrViewModel = new ViewModelProvider(requireActivity()).get(RecycleAddrViewModel.class);
        Log.d(TAG, ""+recycleAddrViewModel.getSize());
//
        TextView txtview_allqueue = view.findViewById(R.id.txtview_allqueue);
        txtview_allqueue.setText(String.valueOf(recycleAddrViewModel.getSize()));

        return view;
    }
}