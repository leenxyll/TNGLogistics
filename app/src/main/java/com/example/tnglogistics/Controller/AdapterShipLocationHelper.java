package com.example.tnglogistics.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.R;
import java.util.ArrayList;
import java.util.List;

public class AdapterShipLocationHelper extends RecyclerView.Adapter<AdapterShipLocationHelper.ViewHolder> {
    private List<ShipLocation> shipLocationList;
    private OnItemRemovedListener itemRemovedListener; // เพิ่ม Listener
    private boolean isRemovable;

    public AdapterShipLocationHelper(ArrayList<ShipLocation> shipLocationLis, boolean isRemovable) {
        this.shipLocationList = shipLocationLis;
        this.isRemovable = isRemovable;
    }

    public interface OnItemRemovedListener {
        void onItemRemoved();
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.itemRemovedListener = listener;
    }

    public void updateList(List<ShipLocation> newList) {
        shipLocationList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position){
        if (position >= 0 && position < shipLocationList.size()) {
            shipLocationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, shipLocationList.size()); // อัปเดตตำแหน่งที่เหลือ

            // แจ้งเตือนไปยัง Fragment
            if (itemRemovedListener != null) {
                itemRemovedListener.onItemRemoved();
            }
        }
    }

    public List<ShipLocation> getItemList() {
        return shipLocationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_shiplocation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShipLocationHelper.ViewHolder holder, int position) {
        holder.txtview_address.setText((position+1) + ". " +shipLocationList.get(position).getShipLoAddr());
        if(isRemovable) {
            holder.btn_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(position);
                }
            });
        } else {
            holder.btn_remove.setVisibility(View.GONE);
            holder.btn_remove.setOnClickListener(null); // ป้องกันปัญหาคลิกปุ่มที่ซ่อนไว้
        }
    }

    @Override
    public int getItemCount() {
        return shipLocationList.size();
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
