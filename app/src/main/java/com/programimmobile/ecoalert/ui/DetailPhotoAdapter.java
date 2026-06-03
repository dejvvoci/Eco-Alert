package com.programimmobile.ecoalert.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.programimmobile.ecoalert.R;

import java.util.List;

public class DetailPhotoAdapter extends
        RecyclerView.Adapter<DetailPhotoAdapter.PhotoViewHolder> {

    private final List<String> base64Photos;

    public DetailPhotoAdapter(List<String> base64Photos) {
        this.base64Photos = base64Photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String base64 = base64Photos.get(position);

        // Dekodo në background thread
        new Thread(() -> {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length);
                holder.itemView.post(() -> {
                    if (bitmap != null) {
                        holder.ivPhoto.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception e) {
                // Foto e dëmtuar — shfaq placeholder
            }
        }).start();
    }

    @Override
    public int getItemCount() { return base64Photos.size(); }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_detail_photo);
        }
    }
}