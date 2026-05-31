package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.utils.AppPreferences;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppPreferences appPreferences = new AppPreferences(this);
        AuthViewModel authViewModel =
                new ViewModelProvider(this).get(AuthViewModel.class);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (authViewModel.isLoggedIn() && appPreferences.isAuthCompleted()) {
                // Përdoruesi ka bërë zgjedhje të qëllimshme — hyr direkt
                goToMain();
            } else {
                // Shfaq ekranin e autentifikimit
                goToAuth();
            }
        }, SPLASH_DELAY);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void goToAuth() {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}