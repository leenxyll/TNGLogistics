package com.example.tnglogistics.Controller;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tnglogistics.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterImageHelper extends RecyclerView.Adapter<AdapterImageHelper.ViewHolder> {
    private static final int MAX_IMAGES = 6;
    private List<Uri> imageList = new ArrayList<>();
    private Context context;
    private OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public AdapterImageHelper(Context context, OnImageDeleteListener listener) {
        this.context = context;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageList.get(position);

        // Load image using Glide or your preferred image loading library
        Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .into(holder.imageView);

        // Set up delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                deleteListener.onImageDelete(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void addImage(Uri imageUri) {
        if (imageList.size() < MAX_IMAGES) {
            imageList.add(imageUri);
            notifyItemInserted(imageList.size() - 1);
        }
    }

    public void removeImage(int position) {
        if (position >= 0 && position < imageList.size()) {
            imageList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageList.size());
        }
    }

    public List<Uri> getImageList() {
        return imageList;
    }

    public boolean canAddMoreImages() {
        return imageList.size() < MAX_IMAGES;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_delivery);
            btnDelete = itemView.findViewById(R.id.btn_delete_image);
        }
    }
}