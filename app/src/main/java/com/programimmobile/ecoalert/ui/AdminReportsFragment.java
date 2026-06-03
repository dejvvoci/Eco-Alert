package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.model.SentReport;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import java.util.ArrayList;
import java.util.List;

public class AdminReportsFragment extends Fragment
        implements AdminReportAdapter.AdminActionListener {

    private ReportViewModel reportViewModel;
    private AuthViewModel authViewModel;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private ChipGroup chipGroupFilter;
    private AdminReportAdapter adapter;

    private List<Report> allReports = new ArrayList<>();
    private String currentFilter = "Të gjitha";

    // Institucionet — mund të shtohen më shumë
    private final String[] institutionNames = {
            "Ministria e Mjedisit",
            "Bashkia Tiranë",
            "Agjencia Kombëtare e Mjedisit",
            "Inspektoriati i Mjedisit",
            "Tjetër"
    };

    private final String[] institutionEmails = {
            "info@mjedisi.gov.al",
            "info@bashkiatirane.gov.al",
            "info@akm.gov.al",
            "inspektoriati@mjedisi.gov.al",
            ""
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        setupRecyclerView();
        setupChipFilter();
    }

    private void initViews(View view) {
        recyclerView    = view.findViewById(R.id.recycler_view);
        progressBar     = view.findViewById(R.id.progress_bar);
        layoutEmpty     = view.findViewById(R.id.layout_empty);
        chipGroupFilter = view.findViewById(R.id.chip_group_admin_filter);
    }

    private void setupViewModels() {
        reportViewModel = new ViewModelProvider(requireActivity())
                .get(ReportViewModel.class);
        authViewModel   = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);

        progressBar.setVisibility(View.VISIBLE);

        reportViewModel.getAllReports().observe(getViewLifecycleOwner(),
                reports -> {
                    progressBar.setVisibility(View.GONE);
                    if (reports != null) {
                        allReports = reports;
                        applyFilter();
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new AdminReportAdapter(this);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupChipFilter() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if (id == R.id.chip_admin_all)      currentFilter = "Të gjitha";
            else if (id == R.id.chip_admin_new) currentFilter = "E re";
            else if (id == R.id.chip_admin_approved) currentFilter = "I aprovuar";
            else if (id == R.id.chip_admin_rejected) currentFilter = "I refuzuar";
            applyFilter();
        });
    }

    private void applyFilter() {
        List<Report> filtered = new ArrayList<>();
        for (Report r : allReports) {
            String status = r.getStatus() != null ? r.getStatus() : "E re";
            if (currentFilter.equals("Të gjitha")
                    || currentFilter.equals(status)) {
                filtered.add(r);
            }
        }

        if (filtered.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.setReports(filtered);
        }
    }

    // ─── Admin Actions ────────────────────────────────────────────────────────

    @Override
    public void onApprove(Report report) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Aprovo Raportin")
                .setMessage("A dëshiron të aprovosh raportin për '"
                        + report.getCategory() + "'?\n\n"
                        + "Përdoruesi do njoftohet automatikisht.")
                .setPositiveButton("Aprovo", (d, w) -> {
                    reportViewModel.approveReport(report);
                    Toast.makeText(requireContext(),
                            "Raporti u aprovua!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    @Override
    public void onReject(Report report) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_1, null);
        EditText etReason = new EditText(requireContext());
        etReason.setHint("Arsyeja e refuzimit (opsionale)");
        etReason.setPadding(32, 24, 32, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle("Refuzo Raportin")
                .setMessage("Raporti për '" + report.getCategory()
                        + "' do refuzohet.")
                .setView(etReason)
                .setPositiveButton("Refuzo", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    reportViewModel.rejectReport(report, reason);
                    Toast.makeText(requireContext(),
                            "Raporti u refuzua.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    @Override
    public void onSendToInstitution(Report report) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Dërgo te Institucioni")
                .setItems(institutionNames, (dialog, which) -> {
                    String email = institutionEmails[which];
                    String name  = institutionNames[which];

                    if (which == institutionNames.length - 1) {
                        // "Tjetër" — kërko email manual
                        showCustomEmailDialog(report, name);
                    } else {
                        sendEmailToInstitution(report, email, name);
                    }
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    private void showCustomEmailDialog(Report report, String name) {
        EditText etEmail = new EditText(requireContext());
        etEmail.setHint("email@institucion.gov.al");
        etEmail.setPadding(32, 24, 32, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle("Email i Institucionit")
                .setView(etEmail)
                .setPositiveButton("Dërgo", (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    if (!email.isEmpty()) {
                        sendEmailToInstitution(report, email, name);
                    }
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    private void sendEmailToInstitution(Report report,
                                        String email, String institutionName) {
        // Ndërto emailin
        String subject = "[EcoAlert] Raport Ndotjeje — "
                + report.getCategory();
        String body = "Të nderuar,\n\n"
                + "Ju dërgojmë këtë raport ndotjeje nga aplikacioni EcoAlert:\n\n"
                + "Kategoria: " + report.getCategory() + "\n"
                + "Përshkrimi: " + (report.getDescription() != null
                ? report.getDescription() : "Pa përshkrim") + "\n"
                + "Koordinatat: " + report.getLatitude()
                + ", " + report.getLongitude() + "\n"
                + "Konfirmime nga komuniteti: "
                + report.getConfirmations() + "\n\n"
                + "Ju lutemi ndërmerrni masat e nevojshme.\n\n"
                + "Me respekt,\nEchoAlert Admin";

        // Hap email client
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        if (emailIntent.resolveActivity(
                requireContext().getPackageManager()) != null) {
            startActivity(emailIntent);

            // Ruaj në Firestore
            String adminId = authViewModel.getCurrentUserId();
            SentReport sentReport = new SentReport(
                    report.getId(),
                    report.getCategory(),
                    report.getDescription(),
                    email,
                    institutionName,
                    report.getLatitude(),
                    report.getLongitude(),
                    adminId != null ? adminId : ""
            );
            reportViewModel.saveSentReport(sentReport);

            Toast.makeText(requireContext(),
                    "Email u dërgua te " + institutionName,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(),
                    "Nuk u gjet asnjë aplikacion emaili.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onViewDetail(Report report) {
        Intent intent = new Intent(requireContext(),
                ReportDetailActivity.class);
        intent.putExtra("report_id", report.getId());
        startActivity(intent);
    }
}