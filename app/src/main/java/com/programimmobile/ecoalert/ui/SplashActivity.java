package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (authViewModel.isLoggedIn()) {
                // Përdoruesi ekziston — hyr direkt
                goToMain();
            } else {
                // Hyrje anonime automatike
                authViewModel.signInAnonymously();
                observeAuth();
            }
        }, SPLASH_DELAY);
    }

    private void observeAuth() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                goToMain();
            }
        });

        authViewModel.getError().observe(this, error -> {
            if (error != null) {
                // Edhe nëse ka gabim, hyr si anonim pas 1 sekonde
                new Handler(Looper.getMainLooper()).postDelayed(this::goToMain, 1000);
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}