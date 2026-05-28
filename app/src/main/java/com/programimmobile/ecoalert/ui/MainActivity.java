package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Nëse nuk është i loguar, dërgo te AuthActivity
        if (!authViewModel.isLoggedIn()) {
            goToAuth();
            return;
        }

        initViews();
        setupBottomNavigation();

        // Ngarko fragmentin e parë si default
        if (savedInstanceState == null) {
            loadFragment(new ReportFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_report) {
                fragment = new ReportFragment();
            } else if (itemId == R.id.nav_map) {
                fragment = new MapFragment();
            } else if (itemId == R.id.nav_history) {
                fragment = new HistoryFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Metodë publike — thirret nga ProfileFragment pas logout
    public void goToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}