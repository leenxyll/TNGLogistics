package com.example.tnglogistics.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.RecyclePlanModel;
import com.example.tnglogistics.R;

import java.util.ArrayList;

public class AdapterPlanHelper extends RecyclerView.Adapter<AdapterPlanHelper.ViewHolder> {
    private ArrayList<RecyclePlanModel> itemList;

    public AdapterPlanHelper(ArrayList<RecyclePlanModel> itemList) {
        this.itemList = itemList;
    }

    public void updateList(ArrayList<RecyclePlanModel> newList) {
        itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(itemList.get(position).getAddr());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtview_address);
        }
    }
}