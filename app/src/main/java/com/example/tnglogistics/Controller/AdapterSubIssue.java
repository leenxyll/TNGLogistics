package com.example.tnglogistics.Controller;

import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tnglogistics.Model.SubIssue;
import com.example.tnglogistics.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AdapterSubIssue extends RecyclerView.Adapter<AdapterSubIssue.SubIssueViewHolder> {

    private List<SubIssue> subIssueList = new ArrayList<>();
    private int selectedPosition = -1;

    private OnSubIssueClickListener listener;
    private OnTakePhotoClickListener photoListener;
    private OnDeletePhotoClickListener deletePhotoListener;
    private OnDescriptionChangedListener descriptionChangedListener;

    private Map<Integer, List<Uri>> photoUriMap = new HashMap<>();
    private Map<Integer, String> descriptionMap = new HashMap<>();
    private static final int MAX_PHOTOS = 6;

    public interface OnSubIssueClickListener {
        void onSubIssueSelected(SubIssue subIssue, int position);
    }

    public interface OnTakePhotoClickListener {
        void onTakePhotoClicked(SubIssue subIssue, int position);
    }

    public interface OnDeletePhotoClickListener {
        void onDeletePhotoClicked(SubIssue subIssue, int position, Uri photoUri);
    }

    public interface OnDescriptionChangedListener {
        void onDescriptionChanged(String description); // ส่งกลับไปให้ Issue เก็บ
    }

    public AdapterSubIssue(OnSubIssueClickListener listener,
                           OnTakePhotoClickListener photoListener,
                           OnDeletePhotoClickListener deletePhotoListener,
                           OnDescriptionChangedListener descriptionChangedListener) {
        this.listener = listener;
        this.photoListener = photoListener;
        this.deletePhotoListener = deletePhotoListener;
        this.descriptionChangedListener = descriptionChangedListener;
    }

    @NonNull
    @Override
    public SubIssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sub_issue, parent, false);
        return new SubIssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubIssueViewHolder holder, int position) {
        SubIssue subIssue = subIssueList.get(position);

        holder.radioSubIssue.setText(subIssue.getSubIssueTypeName());
        holder.radioSubIssue.setChecked(position == selectedPosition);

        // จัดการรูปภาพ
        if ("Y".equals(subIssue.getRequirePic()) && position == selectedPosition) {
            holder.layoutPhotos.setVisibility(View.VISIBLE);

            int key = subIssue.getSubIssueTypeCode();
            List<Uri> photoUris = photoUriMap.getOrDefault(key, new ArrayList<>());

            AdapterImageHelper imageAdapter = new AdapterImageHelper(holder.recyclerViewImages.getContext(), deletePosition -> {
                Uri photoToDelete = photoUris.get(deletePosition);
                if (deletePhotoListener != null) {
                    deletePhotoListener.onDeletePhotoClicked(subIssue, deletePosition, photoToDelete);
                }
            });

            imageAdapter.getImageList().addAll(photoUris);
            holder.recyclerViewImages.setLayoutManager(new LinearLayoutManager(holder.recyclerViewImages.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerViewImages.setAdapter(imageAdapter);

            int currentCount = photoUris.size();
            holder.txtImageCount.setText("เพิ่มรูปภาพ\n" + currentCount + " / " + MAX_PHOTOS);
            holder.imgAddPhoto.setVisibility(currentCount >= MAX_PHOTOS ? View.GONE : View.VISIBLE);

            holder.imgAddPhoto.setOnClickListener(v -> {
                if (photoListener != null) {
                    photoListener.onTakePhotoClicked(subIssue, position);
                }
            });
        } else {
            holder.layoutPhotos.setVisibility(View.GONE);
        }

        // จัดการ description (เฉพาะ subIssueCode == 4)
        if (position == selectedPosition && subIssue.getSubIssueTypeCode() == 4) {
            holder.edtDescription.setVisibility(View.VISIBLE);
            // แสดงค่าของ description ที่เก็บไว้ (ถ้ามี)
            String savedDescription = descriptionMap.get(subIssue.getSubIssueTypeCode());
            if (savedDescription != null) {
                holder.edtDescription.setText(savedDescription);
            }
        } else {
            holder.edtDescription.setVisibility(View.GONE);
            holder.edtDescription.setText(""); // ล้างเมื่อไม่ได้เลือกหรือไม่ใช่ code 4
        }

        holder.edtDescription.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (position == selectedPosition && subIssue.getSubIssueTypeCode() == 4) {
                    // เก็บ description ลงใน map
                    descriptionMap.put(subIssue.getSubIssueTypeCode(), s.toString());
                    if (descriptionChangedListener != null) {
                        descriptionChangedListener.onDescriptionChanged(s.toString());
                    }
                }
            }
        });


        // จัดการคลิกเลือก
        holder.radioSubIssue.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null && selectedPosition >= 0 && selectedPosition < subIssueList.size()) {
                listener.onSubIssueSelected(subIssueList.get(selectedPosition), selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subIssueList.size();
    }

    public void setSubIssues(List<SubIssue> subIssues) {
        this.subIssueList = subIssues;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public SubIssue getSelectedSubIssue() {
        if (selectedPosition >= 0 && selectedPosition < subIssueList.size()) {
            return subIssueList.get(selectedPosition);
        }
        return null;
    }

    public void addPhoto(int subIssueId, Uri photoUri) {
        List<Uri> photoUris = photoUriMap.getOrDefault(subIssueId, new ArrayList<>());
        if (photoUris.size() < MAX_PHOTOS) {
            photoUris.add(photoUri);
            photoUriMap.put(subIssueId, photoUris);
            notifyItemChanged(selectedPosition);
        }
    }

    public void removePhoto(int subIssueId, Uri photoUri) {
        List<Uri> photoUris = photoUriMap.get(subIssueId);
        if (photoUris != null) {
            photoUris.remove(photoUri);
            notifyItemChanged(selectedPosition);
        }
    }

    public List<Uri> getPhotos(int subIssueId) {
        return photoUriMap.getOrDefault(subIssueId, new ArrayList<>());
    }

    public void clearPhotosExcept(int subIssueTypeCode) {
        for (Integer key : new HashSet<>(photoUriMap.keySet())) {
            if (!key.equals(subIssueTypeCode)) {
                photoUriMap.remove(key);
            }
        }
        notifyDataSetChanged();
    }

    public List<SubIssue> getSubIssues() {
        return this.subIssueList;
    }

    static class SubIssueViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioSubIssue;
        LinearLayout layoutPhotos;
        RecyclerView recyclerViewImages;
        ImageView imgAddPhoto;
        TextView txtImageCount;
        EditText edtDescription;

        SubIssueViewHolder(@NonNull View itemView) {
            super(itemView);
            radioSubIssue = itemView.findViewById(R.id.radioSubIssue);
            layoutPhotos = itemView.findViewById(R.id.layoutPhotos);
            recyclerViewImages = itemView.findViewById(R.id.recycler_view_images);
            imgAddPhoto = itemView.findViewById(R.id.img_add_photo);
            txtImageCount = itemView.findViewById(R.id.txt_image_count);
            edtDescription = itemView.findViewById(R.id.edt_description);
        }
    }
}
