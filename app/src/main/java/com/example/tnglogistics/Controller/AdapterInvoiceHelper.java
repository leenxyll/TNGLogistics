package com.example.tnglogistics.Controller;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//public class AdapterInvoiceHelper extends RecyclerView.Adapter<AdapterInvoiceHelper.ViewHolder> {
//    List<Invoice> invoiceList;
//
//    public AdapterInvoiceHelper(List<Invoice> invoiceList) {
//        this.invoiceList = invoiceList;
//    }
//
//    public void updateList(List<Invoice> newList) {
//        invoiceList = newList;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public AdapterInvoiceHelper.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_shiplocation, parent, false);
//        return new AdapterInvoiceHelper.ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull AdapterInvoiceHelper.ViewHolder holder, int position) {
//        holder.txtview_address.setText(invoiceList.get(position).getShipListSeq()+". "+invoiceList.get(position).getInvoiceCode());
//    }
//
//    @Override
//    public int getItemCount() {
//        return invoiceList.size();
//    }
//
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView txtview_address;
////        Button btn_remove;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            txtview_address = itemView.findViewById(R.id.txtview_address);
////            btn_remove = itemView.findViewById(R.id.btn_remove);
//        }
//    }
//}

public class AdapterInvoiceHelper extends RecyclerView.Adapter<AdapterInvoiceHelper.ViewHolder> {
    private Map<String, List<Invoice>> groupedInvoices;
    private List<String> locationKeys;

    public AdapterInvoiceHelper(Map<String, List<Invoice>> groupedInvoices) {
        this.groupedInvoices = groupedInvoices;
        this.locationKeys = new ArrayList<>(groupedInvoices.keySet());

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String location = locationKeys.get(position);
        List<Invoice> invoices = groupedInvoices.get(location);

        holder.txtLocation.setText(location);
        Log.d("AdapterInvoiceHelper", "Location: " + location + " | Invoice Count: " + invoices.size());


        // ตั้งค่า RecyclerView ย่อย
        AdapterInvoiceDetail detailAdapter = new AdapterInvoiceDetail(invoices);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(detailAdapter);

        // ป้องกัน RecyclerView ซ้อนกันทำให้แสดงไม่ครบ
        holder.recyclerView.setHasFixedSize(false);
        holder.recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
    }

    @Override
    public int getItemCount() {
        return locationKeys.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLocation;
        RecyclerView recyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            recyclerView = itemView.findViewById(R.id.recyclerViewInvoices);
        }
    }
}
