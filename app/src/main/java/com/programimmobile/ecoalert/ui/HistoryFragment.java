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
    private String currentUserId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        setupRecyclerView();
        setupSwipeToDelete();

        currentUserId = authViewModel.getCurrentUserId();
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
            Intent intent = new Intent(requireContext(),
                    ReportDetailActivity.class);
            intent.putExtra("report_id", report.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
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

    private void loadUserReports() {
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("reports")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);

                    if (snapshots == null || snapshots.isEmpty()) {
                        updateUI(new java.util.ArrayList<>());
                        return;
                    }

                    java.util.List<com.programimmobile.ecoalert.model.Report> reports =
                            new java.util.ArrayList<>();
                    reports.addAll(snapshots.toObjects(
                            com.programimmobile.ecoalert.model.Report.class));

                    // Rendo në Java — pa nevojë për index
                    reports.sort((a, b) -> {
                        if (a.getTimestamp() == null) return 1;
                        if (b.getTimestamp() == null) return -1;
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    });

                    updateUI(reports);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    updateUI(new java.util.ArrayList<>());
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rifresho çdo herë që faqja bëhet aktive
        loadUserReports();
    }
}