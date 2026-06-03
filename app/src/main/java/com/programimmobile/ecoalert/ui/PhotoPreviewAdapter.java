package com.programimmobile.ecoalert.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.programimmobile.ecoalert.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoPreviewAdapter extends
        RecyclerView.Adapter<PhotoPreviewAdapter.PhotoViewHolder> {

    public interface OnPhotoRemovedListener {
        void onRemove(int position);
    }

    private final List<Uri> photoUris = new ArrayList<>();
    private final List<Bitmap> photoBitmaps = new ArrayList<>();
    private final OnPhotoRemovedListener listener;

    public PhotoPreviewAdapter(OnPhotoRemovedListener listener) {
        this.listener = listener;
    }

    public void addPhoto(Uri uri, Bitmap bitmap) {
        photoUris.add(uri);
        photoBitmaps.add(bitmap);
        notifyItemInserted(photoUris.size() - 1);
    }

    public void removePhoto(int position) {
        photoUris.remove(position);
        photoBitmaps.remove(position);
        notifyItemRemoved(position);
    }

    public List<Uri> getPhotoUris() { return photoUris; }
    public int getPhotoCount()      { return photoUris.size(); }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_preview, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.ivPhoto.setImageBitmap(photoBitmaps.get(position));
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                listener.onRemove(pos);
            }
        });
    }

    @Override
    public int getItemCount() { return photoUris.size(); }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto, btnRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto   = itemView.findViewById(R.id.iv_photo);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}