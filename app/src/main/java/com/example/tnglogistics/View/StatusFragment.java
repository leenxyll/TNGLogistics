package com.example.tnglogistics.View;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tnglogistics.Controller.AdapterAddrHelper;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.R;
import com.example.tnglogistics.ViewModel.RecycleAddrViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {
    private static final String TAG = "StatusFragment";
    private RecycleAddrViewModel recycleAddrViewModel;
    private AdapterAddrHelper adapter;

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferencesHelper.saveLastFragment(requireContext(), "StatusFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdapterAddrHelper(new ArrayList<>(), false);
        recyclerView.setAdapter(adapter);

        recycleAddrViewModel = RecycleAddrViewModel.getInstance(requireActivity().getApplication());
        recycleAddrViewModel.getItemList().observe(getViewLifecycleOwner(), items -> {
            adapter.updateList(items);
        });

        TextView txtview_allqueue = view.findViewById(R.id.txtview_allqueue);
        TextView txtview_inqueue = view.findViewById(R.id.txtview_queue);
        txtview_allqueue.setText(String.valueOf(recycleAddrViewModel.getSize()));
        txtview_inqueue.setText(String.valueOf(recycleAddrViewModel.getSize()));

        return view;
    }
}