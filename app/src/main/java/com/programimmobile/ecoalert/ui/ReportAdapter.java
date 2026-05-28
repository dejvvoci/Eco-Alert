package com.programimmobile.ecoalert.ui;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    private List<Report> reports = new ArrayList<>();
    private final OnReportClickListener listener;

    public ReportAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    public Report getReportAt(int position) {
        return reports.get(position);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCategoryIcon;
        private final TextView tvCategory;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvStatus;
        private final TextView tvConfirmations;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvConfirmations = itemView.findViewById(R.id.tv_confirmations);
        }

        public void bind(Report report, OnReportClickListener listener) {
            tvCategory.setText(report.getCategory());

            // Përshkrimi
            if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                tvDescription.setText(report.getDescription());
            } else {
                tvDescription.setText("Pa përshkrim.");
            }

            // Data
            if (report.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(report.getTimestamp()));
            } else {
                tvDate.setText("—");
            }

            // Statusi
            tvStatus.setText(report.getStatus() != null ? report.getStatus() : "E re");

            // Konfirmimet
            tvConfirmations.setText(report.getConfirmations() + " konfirmime");

            // Ngjyra e ikonës sipas kategorisë
            int colorRes = getCategoryColor(report.getCategory());
            Drawable icon = ContextCompat.getDrawable(itemView.getContext(),
                    android.R.drawable.ic_menu_myplaces);
            if (icon != null) {
                icon.setTint(ContextCompat.getColor(itemView.getContext(), colorRes));
                ivCategoryIcon.setImageDrawable(icon);
            }

            // Klikim
            itemView.setOnClickListener(v -> listener.onReportClick(report));
        }

        private int getCategoryColor(String category) {
            if (category == null) return R.color.category_other;
            switch (category) {
                case "Mbetje Urbane": return R.color.category_waste;
                case "Zhurmë":       return R.color.category_noise;
                case "Ndotje Ajri":  return R.color.category_air;
                case "Ndotje Uji":   return R.color.category_water;
                default:             return R.color.category_other;
            }
        }
    }
}