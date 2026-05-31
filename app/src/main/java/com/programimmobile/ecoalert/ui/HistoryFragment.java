package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import java.util.List;

public class HistoryFragment extends Fragment {

    private ReportViewModel reportViewModel;
    private AuthViewModel authViewModel;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private ReportAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        setupRecyclerView();
        setupSwipeToDelete();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    private void setupViewModels() {
        reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ReportAdapter(report -> {
            Intent intent = new Intent(requireContext(), ReportDetailActivity.class);
            intent.putExtra("report_id", report.getId());
            intent.putExtra("category", report.getCategory());
            intent.putExtra("description", report.getDescription());
            intent.putExtra("latitude", report.getLatitude());
            intent.putExtra("longitude", report.getLongitude());
            intent.putExtra("status", report.getStatus());
            intent.putExtra("confirmations", report.getConfirmations());
            intent.putExtra("photo_url", report.getPhotoUrl());
            intent.putExtra("report_user_id", report.getUserId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observe raportet e përdoruesit
        String userId = authViewModel.getCurrentUserId();
        if (userId != null) {
            progressBar.setVisibility(View.VISIBLE);
            reportViewModel.getUserReports(userId).observe(getViewLifecycleOwner(),
                    this::updateUI);
        }
    }

    private void updateUI(List<Report> reports) {
        progressBar.setVisibility(View.GONE);

        if (reports == null || reports.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.setReports(reports);
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Report report = adapter.getReportAt(position);

                        // Dialog konfirmimi para fshirjes
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Fshi Raportin")
                                .setMessage("A je i sigurt që dëshiron ta fshish këtë raport?")
                                .setPositiveButton("Fshi", (dialog, which) -> {
                                    reportViewModel.deleteReport(report.getId());
                                    Toast.makeText(requireContext(),
                                            "Raporti u fshi.", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Anulo", (dialog, which) -> {
                                    // Rikthe kartën në pozicion
                                    adapter.notifyItemChanged(position);
                                })
                                .setOnCancelListener(dialog ->
                                        adapter.notifyItemChanged(position))
                                .show();
                    }
                };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }
}