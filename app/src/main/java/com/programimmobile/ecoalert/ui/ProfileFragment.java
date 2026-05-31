package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.utils.AppPreferences;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

public class ProfileFragment extends Fragment {

    private AuthViewModel authViewModel;
    private ReportViewModel reportViewModel;

    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvAccountType;
    private TextView tvReportCount;
    private TextView tvConfirmationCount;
    private MaterialButton btnUpgrade;
    private com.google.android.material.card.MaterialCardView cardUpgrade;
    private LinearLayout layoutLogout;
    private LinearLayout layoutAbout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        updateProfileUI();
        setupClickListeners();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvAccountType = view.findViewById(R.id.tv_account_type);
        tvReportCount = view.findViewById(R.id.tv_report_count);
        tvConfirmationCount = view.findViewById(R.id.tv_confirmation_count);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        cardUpgrade = view.findViewById(R.id.card_upgrade);
        layoutLogout = view.findViewById(R.id.layout_logout);
        layoutAbout = view.findViewById(R.id.layout_about);
    }

    private void setupViewModels() {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

        // Observe raportet për statistika
        String userId = authViewModel.getCurrentUserId();
        if (userId != null) {
            reportViewModel.getUserReports(userId).observe(getViewLifecycleOwner(),
                    reports -> {
                        if (reports != null) {
                            tvReportCount.setText(String.valueOf(reports.size()));

                            // Llogarit konfirmimet totale
                            int totalConfirmations = 0;
                            for (var report : reports) {
                                totalConfirmations += report.getConfirmations();
                            }
                            tvConfirmationCount.setText(
                                    String.valueOf(totalConfirmations));
                        }
                    });
        }

        // Observe gabimet
        authViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe përdoruesin — kur bën logout
        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                // Logout — kalo te AuthActivity
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).goToAuth();
                }
            }
        });
    }

    private void updateProfileUI() {
        if (authViewModel.isAnonymous()) {
            tvUserName.setText("Përdorues Anonim");
            tvUserEmail.setText("Pa llogari");
            tvAccountType.setText("Anonim");
            tvAccountType.setBackgroundResource(R.drawable.status_badge);
            cardUpgrade.setVisibility(View.VISIBLE);
        } else {
            var user = authViewModel.getCurrentUser().getValue();
            if (user != null) {
                String displayName = user.getDisplayName();
                String email = user.getEmail();

                tvUserName.setText(displayName != null && !displayName.isEmpty()
                        ? displayName : "Përdorues");
                tvUserEmail.setText(email != null ? email : "");
                tvAccountType.setText("Llogari");
            }
            cardUpgrade.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Upgrade llogaria anonime
        btnUpgrade.setOnClickListener(v -> showUpgradeDialog());

        // Logout
        layoutLogout.setOnClickListener(v -> showLogoutDialog());

        // Rreth aplikacionit
        layoutAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void showUpgradeDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.activity_auth, null);

        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password);

        new AlertDialog.Builder(requireContext())
                .setTitle("Krijo Llogari")
                .setView(dialogView)
                .setPositiveButton("Regjistrohu", (dialog, which) -> {
                    String email = etEmail.getText() != null ?
                            etEmail.getText().toString().trim() : "";
                    String password = etPassword.getText() != null ?
                            etPassword.getText().toString() : "";

                    authViewModel.linkWithEmail(email, password);

                    authViewModel.getCurrentUser().observe(getViewLifecycleOwner(),
                            user -> {
                                if (user != null && !user.isAnonymous()) {
                                    Toast.makeText(requireContext(),
                                            "Llogaria u krijua me sukses!",
                                            Toast.LENGTH_SHORT).show();
                                    updateProfileUI();
                                }
                            });
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Dil nga llogaria")
                .setMessage("A je i sigurt që dëshiron të dalësh?")
                .setPositiveButton("Dil", (dialog, which) -> {
                    // Pastro flag-un — AuthActivity do shfaqet përsëri
                    new AppPreferences(requireContext()).setAuthCompleted(false);
                    authViewModel.signOut(requireActivity());
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Rreth EcoAlert")
                .setMessage("EcoAlert v1.0\n\nAplikacion për raportimin dhe " +
                        "monitorimin e ndotjes mjedisore.\n\n" +
                        "Zhvilluar nga:\nDejvi Voci & Ersa Mustafa\n\n" +
                        "Master i Shkencave në Informatikë\nUniversiteti i Tiranës")
                .setPositiveButton("OK", null)
                .show();
    }
}