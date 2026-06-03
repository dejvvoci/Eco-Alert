package com.programimmobile.ecoalert.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Report;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportInfoWindow extends InfoWindow {

    private final Context context;
    private Report report;

    public ReportInfoWindow(MapView mapView, Context context) {
        super(R.layout.layout_map_popup, mapView);
        this.context = context;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public void onOpen(Object item) {
        if (report == null) return;

        View view = mView;

        TextView tvCategory      = view.findViewById(R.id.popup_category);
        TextView tvDescription   = view.findViewById(R.id.popup_description);
        TextView tvDate          = view.findViewById(R.id.popup_date);
        TextView tvConfirmations = view.findViewById(R.id.popup_confirmations);
        MaterialButton btnViewFull = view.findViewById(R.id.btn_view_full);
        View btnClose            = view.findViewById(R.id.btn_close_popup);

        // Kategoria
        tvCategory.setText(report.getCategory() != null
                ? report.getCategory() : "—");

        // Përshkrimi i shkurtër
        if (report.getDescription() != null
                && !report.getDescription().isEmpty()) {
            String desc = report.getDescription().length() > 80
                    ? report.getDescription().substring(0, 80) + "..."
                    : report.getDescription();
            tvDescription.setText(desc);
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

        // Konfirmimet
        tvConfirmations.setText(report.getConfirmations() + " konfirmime");

        // Butoni — hap raportin e plotë
        btnViewFull.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("report_id", report.getId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            close();
        });

        // Mbyll popup
        btnClose.setOnClickListener(v -> close());
    }

    @Override
    public void onClose() {}
}