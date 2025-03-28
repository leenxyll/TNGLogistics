package com.example.tnglogistics.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.R;

import java.util.List;

public class AdapterInvoiceDetail extends RecyclerView.Adapter<AdapterInvoiceDetail.ViewHolder> {
    private List<Invoice> invoices;

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
        holder.txtInvoiceCode.setText(invoice.getShipListSeq()+". "+invoice.getInvoiceCode());
        holder.txtCustomerName.setText(invoice.getCusName());
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtInvoiceCode, txtCustomerName;

        public ViewHolder(View itemView) {
            super(itemView);
            txtInvoiceCode = itemView.findViewById(R.id.txtInvoiceCode);
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
        }
    }
}

