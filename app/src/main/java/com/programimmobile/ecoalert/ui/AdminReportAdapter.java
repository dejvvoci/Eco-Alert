package com.programimmobile.ecoalert.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminReportAdapter extends
        RecyclerView.Adapter<AdminReportAdapter.ViewHolder> {

    public interface AdminActionListener {
        void onApprove(Report report);
        void onReject(Report report);
        void onSendToInstitution(Report report);
        void onViewDetail(Report report);
    }

    private List<Report> reports = new ArrayList<>();
    private final AdminActionListener listener;

    public AdminReportAdapter(AdminActionListener listener) {
        this.listener = listener;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reports.get(position), listener);
    }

    @Override
    public int getItemCount() { return reports.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvCategory, tvDate, tvStatus;
        private final TextView tvDescription, tvConfirmations;
        private final MaterialButton btnApprove, btnReject, btnSend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon           = itemView.findViewById(R.id.iv_category_icon);
            tvCategory       = itemView.findViewById(R.id.tv_category);
            tvDate           = itemView.findViewById(R.id.tv_date);
            tvStatus         = itemView.findViewById(R.id.tv_status);
            tvDescription    = itemView.findViewById(R.id.tv_description);
            tvConfirmations  = itemView.findViewById(R.id.tv_confirmations);
            btnApprove       = itemView.findViewById(R.id.btn_approve);
            btnReject        = itemView.findViewById(R.id.btn_reject);
            btnSend          = itemView.findViewById(R.id.btn_send_institution);
        }

        public void bind(Report report, AdminActionListener listener) {
            tvCategory.setText(report.getCategory() != null
                    ? report.getCategory() : "—");

            tvDescription.setText(report.getDescription() != null
                    && !report.getDescription().isEmpty()
                    ? report.getDescription() : "Pa përshkrim.");

            tvConfirmations.setText(report.getConfirmations() + " konfirmime");

            String status = report.getStatus() != null
                    ? report.getStatus() : "E re";
            tvStatus.setText(status);

            // Ngjyra e status badge
            int statusColor;
            switch (status) {
                case "I aprovuar": statusColor = R.color.category_other; break;
                case "I refuzuar": statusColor = R.color.category_waste;  break;
                default:           statusColor = R.color.status_new;      break;
            }
            tvStatus.getBackground().setTint(
                    ContextCompat.getColor(itemView.getContext(), statusColor));

            // Data
            if (report.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(report.getTimestamp()));
            }

            // Ngjyra e ikonës
            int colorRes = getCategoryColor(report.getCategory());
            android.graphics.drawable.Drawable icon =
                    ContextCompat.getDrawable(itemView.getContext(),
                            android.R.drawable.ic_menu_myplaces);
            if (icon != null) {
                icon.setTint(ContextCompat.getColor(
                        itemView.getContext(), colorRes));
                ivIcon.setImageDrawable(icon);
            }

            // Butonat
            btnApprove.setOnClickListener(v -> listener.onApprove(report));
            btnReject.setOnClickListener(v -> listener.onReject(report));
            btnSend.setOnClickListener(v -> listener.onSendToInstitution(report));
            itemView.setOnClickListener(v -> listener.onViewDetail(report));

            // Çaktivizo butonat nëse tashmë është procesuar
            boolean isProcessed = "I aprovuar".equals(status)
                    || "I refuzuar".equals(status);
            btnApprove.setEnabled(!isProcessed);
            btnReject.setEnabled(!isProcessed);
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