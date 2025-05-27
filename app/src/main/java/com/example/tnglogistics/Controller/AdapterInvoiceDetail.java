package com.example.tnglogistics.Controller;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterInvoiceDetail extends RecyclerView.Adapter<AdapterInvoiceDetail.ViewHolder> {
    private List<Invoice> invoices;
    private Set<Integer> expandedPositions = new HashSet<>();

    public AdapterInvoiceDetail(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);
        holder.txtInvoiceCode.setText(invoice.getShipListSeq() + ". " + invoice.getInvoiceCode());
        holder.txtCustomerName.setText(invoice.getCusName());

        if(invoice.getInvoiceReceiverName() == null){
            holder.txtReceiverName.setVisibility(View.GONE);
        }else{
            holder.txtReceiverName.setVisibility(View.VISIBLE);
            holder.txtReceiverName.setText("ช่องทางติดต่อ : " + invoice.getInvoiceReceiverName());
        }

        if(invoice.getInvoiceReceiverPhone() == null){
            holder.txtReceiverPhone.setVisibility(View.GONE);
        }else {
            holder.txtReceiverPhone.setVisibility(View.VISIBLE);
            holder.txtReceiverPhone.setText("เบอร์ : " + invoice.getInvoiceReceiverPhone());
        }

        if(invoice.getInvoiceNote() != null){
            holder.txtInvoiceNote.setVisibility(View.VISIBLE);
            holder.txtInvoiceNote.setText("Note : " + invoice.getInvoiceNote());
        }else{
            holder.txtInvoiceNote.setVisibility(View.GONE);
        }

        boolean isExpanded = expandedPositions.contains(position);
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.btnExpand.setRotation(isExpanded ? 180 : 0); // ให้ปุ่มหมุนตามสถานะ

        holder.btnExpand.setOnClickListener(v -> {
            if (isExpanded) {
                expandedPositions.remove(position);
            } else {
                expandedPositions.add(position);
            }
            notifyItemChanged(position);
        });

        // กดเพื่อโทร
        holder.txtReceiverPhone.setOnClickListener(v -> {
            String phone = invoice.getInvoiceReceiverPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtInvoiceCode, txtCustomerName, txtInvoiceNote, txtReceiverName, txtReceiverPhone;
        LinearLayout expandableLayout;
        ImageButton btnExpand;

        public ViewHolder(View itemView) {
            super(itemView);
            txtInvoiceCode = itemView.findViewById(R.id.txtInvoiceCode);
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
            txtInvoiceNote = itemView.findViewById(R.id.txtInvoiceNote);
            txtReceiverName = itemView.findViewById(R.id.txtReceiverName);
            txtReceiverPhone = itemView.findViewById(R.id.txtReceiverPhone);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            btnExpand = itemView.findViewById(R.id.btnExpand);
        }
    }
}


