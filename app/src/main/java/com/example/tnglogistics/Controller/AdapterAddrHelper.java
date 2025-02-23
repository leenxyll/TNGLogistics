package com.example.tnglogistics.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.AddrModel;
import com.example.tnglogistics.R;

import java.util.ArrayList;

public class AdapterAddrHelper extends RecyclerView.Adapter<AdapterAddrHelper.ViewHolder> {
    private ArrayList<AddrModel> itemList;
    private OnItemRemovedListener itemRemovedListener; // เพิ่ม Listener

    public interface OnItemRemovedListener {
        void onItemRemoved();
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.itemRemovedListener = listener;
    }

    public AdapterAddrHelper(ArrayList<AddrModel> itemList) {
        this.itemList = itemList;
    }

    public void updateList(ArrayList<AddrModel> newList) {
        itemList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position){
        if (position >= 0 && position < itemList.size()) {
            itemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemList.size()); // อัปเดตตำแหน่งที่เหลือ

            // แจ้งเตือนไปยัง Fragment
            if (itemRemovedListener != null) {
                itemRemovedListener.onItemRemoved();
            }
        }
    }

    public ArrayList<AddrModel> getItemList() {
        return itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtview_address.setText(itemList.get(position).getAddr());
        holder.btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtview_address;
        Button btn_remove;

        public ViewHolder(View itemView) {
            super(itemView);
            txtview_address = itemView.findViewById(R.id.txtview_address);
            btn_remove = itemView.findViewById(R.id.btn_remove);
        }
    }
}