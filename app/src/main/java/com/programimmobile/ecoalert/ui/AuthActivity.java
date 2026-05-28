package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

public class AuthActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TabLayout tabLayout;
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnAuth, btnAnonymous;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initViews();
        setupViewModel();
        setupTabLayout();
        setupClickListeners();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        btnAuth = findViewById(R.id.btn_auth);
        btnAnonymous = findViewById(R.id.btn_anonymous);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe përdoruesin — kur logohet kalo te MainActivity
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                goToMain();
            }
        });

        // Observe gabimet
        authViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe loading
        authViewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnAuth.setEnabled(!isLoading);
            btnAnonymous.setEnabled(!isLoading);
        });
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLoginMode = tab.getPosition() == 0;
                btnAuth.setText(isLoginMode ? "Hyr" : "Regjistrohu");
                tvForgotPassword.setVisibility(isLoginMode ? View.VISIBLE : View.GONE);
                clearErrors();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        // Butoni kryesor — Hyr ose Regjistrohu
        btnAuth.setOnClickListener(v -> {
            String email = etEmail.getText() != null ?
                    etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ?
                    etPassword.getText().toString() : "";

            clearErrors();

            if (isLoginMode) {
                authViewModel.signInWithEmail(email, password);
            } else {
                authViewModel.registerWithEmail(email, password);
            }
        });

        // Vazhdo pa llogari
        btnAnonymous.setOnClickListener(v -> {
            authViewModel.signInAnonymously();
        });

        // Harrove fjalëkalimin
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText() != null ?
                    etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                tilEmail.setError("Shkruaj email-in për reset.");
                return;
            }
            authViewModel.resetPassword(email);
            Toast.makeText(this,
                    "Email-i i reset-it u dërgua!", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}