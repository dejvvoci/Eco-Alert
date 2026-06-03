package com.programimmobile.ecoalert.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.SentReport;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SentReportsFragment extends Fragment {

    private ReportViewModel reportViewModel;
    private AuthViewModel authViewModel;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sent_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_sent);
        progressBar  = view.findViewById(R.id.progress_bar);
        layoutEmpty  = view.findViewById(R.id.layout_empty);

        reportViewModel = new ViewModelProvider(requireActivity())
                .get(ReportViewModel.class);
        authViewModel   = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        String adminId = authViewModel.getCurrentUserId();
        if (adminId != null) {
            progressBar.setVisibility(View.VISIBLE);
            reportViewModel.getSentReports(adminId)
                    .observe(getViewLifecycleOwner(), this::updateUI);
        }
    }

    private void updateUI(List<SentReport> sentReports) {
        progressBar.setVisibility(View.GONE);

        if (sentReports == null || sentReports.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Adapter inline i thjeshtë
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(
                    @NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_sent_report, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(
                    @NonNull RecyclerView.ViewHolder holder, int position) {
                SentReport sr = sentReports.get(position);

                ((TextView) holder.itemView.findViewById(
                        R.id.tv_institution_name))
                        .setText(sr.getInstitutionName() != null
                                ? sr.getInstitutionName() : "Institucion");

                ((TextView) holder.itemView.findViewById(
                        R.id.tv_institution_email))
                        .setText(sr.getInstitutionEmail() != null
                                ? sr.getInstitutionEmail() : "—");

                ((TextView) holder.itemView.findViewById(R.id.tv_category))
                        .setText(sr.getCategory() != null
                                ? sr.getCategory() : "—");

                ((TextView) holder.itemView.findViewById(R.id.tv_description))
                        .setText(sr.getDescription() != null
                                && !sr.getDescription().isEmpty()
                                ? sr.getDescription() : "Pa përshkrim.");

                if (sr.getSentAt() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm", Locale.getDefault());
                    ((TextView) holder.itemView.findViewById(R.id.tv_sent_date))
                            .setText(sdf.format(sr.getSentAt()));
                }
            }

            @Override
            public int getItemCount() { return sentReports.size(); }
        });
    }
}