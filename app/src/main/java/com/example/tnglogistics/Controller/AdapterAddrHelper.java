package com.example.tnglogistics.Controller;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.AddrModel;
import com.example.tnglogistics.R;

import java.util.ArrayList;

public class AdapterAddrHelper extends RecyclerView.Adapter<AdapterAddrHelper.ViewHolder> {
    private ArrayList<AddrModel> itemList;
    private OnItemRemovedListener itemRemovedListener; // เพิ่ม Listener
    private boolean isRemovable;

    public interface OnItemRemovedListener {
        void onItemRemoved();
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.itemRemovedListener = listener;
    }

    public AdapterAddrHelper(ArrayList<AddrModel> itemList, boolean isRemovable) {
        this.itemList = itemList;
        this.isRemovable = isRemovable;

//        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
//            @Override
//            public boolean isLongPressDragEnabled() {
//                Log.d("Adapter", "itemList.size() : "+itemList.size());
//                return true; // อนุญาตให้ลากได้เมื่อกดค้าง
//            }
//
//            @Override
//            public boolean isItemViewSwipeEnabled() {
//                return false; // ปิดการเลื่อนเพื่อสไลด์
//            }
//
//            @Override
//            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                // อนุญาตให้ลากในทิศทางขึ้นและลงเท่านั้น
//                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
//                int swipeFlags = 0;  // ปิดการลากซ้ายขวา
//                return makeMovementFlags(dragFlags, swipeFlags);
//            }
//
////            @Override
////            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
////                int fromPosition = viewHolder.getAdapterPosition();
////                int toPosition = target.getAdapterPosition();
////
////                // สลับตำแหน่งรายการ
////                AddrModel fromItem = itemList.get(fromPosition);
////                itemList.remove(fromPosition);
////                itemList.add(toPosition, fromItem);
////                notifyItemMoved(fromPosition, toPosition);
////                return true;  // คืนค่า true เมื่อการย้ายรายการเสร็จสมบูรณ์
////            }
//
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                int fromPosition = viewHolder.getAdapterPosition();
//                int toPosition = target.getAdapterPosition();
//                Log.d("Adapter", "fromPosition : "+fromPosition); //1
//                Log.d("Adapter", "itemList.size() : "+itemList.size()); //0
//                Log.d("Adapter", "toPosition : "+toPosition); //0
//
//                // ตรวจสอบว่า fromPosition และ toPosition อยู่ในขอบเขตของ itemList หรือไม่
//                if (fromPosition >= 0 && fromPosition < itemList.size() && toPosition >= 0 && toPosition < itemList.size()) {
//                    // สลับตำแหน่งรายการ
//                    Log.d("Adapter", "Condition True");
//                    AddrModel fromItem = itemList.get(fromPosition);
//                    itemList.remove(fromPosition);
//                    itemList.add(toPosition, fromItem);
//                    notifyItemMoved(fromPosition, toPosition);
//                    return true;  // คืนค่า true เมื่อการย้ายรายการเสร็จสมบูรณ์
//                }
//                Log.d("Adapter", "Condition false");
//                return false;  // คืนค่า false หากไม่สามารถย้ายรายการได้
//            }
//
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                // ปิดการใช้งานการเลื่อน
//            }
//        };
//
//        touchHelper = new ItemTouchHelper(callback);
    }

//    public void attachToRecyclerView(RecyclerView recyclerView) {
//        touchHelper.attachToRecyclerView(recyclerView);
//    }

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
        holder.txtview_address.setText((position+1) + ". " +itemList.get(position).getAddr());
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