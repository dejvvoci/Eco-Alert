package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.utils.AppPreferences;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private BottomNavigationView bottomNavigation;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (!authViewModel.isLoggedIn()) {
            goToAuth();
            return;
        }

        initViews();
        setupToolbar();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new ReportFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                showProfileMenu();
                return true;
            }
            return false;
        });

        // Shfaq emrin e përdoruesit
        updateToolbarSubtitle();
    }

    private void updateToolbarSubtitle() {
        if (authViewModel.isAnonymous()) {
            toolbar.setSubtitle("Anonim");
        } else {
            var user = authViewModel.getCurrentUser().getValue();
            if (user != null && user.getEmail() != null) {
                toolbar.setSubtitle(user.getEmail());
            }
        }
        toolbar.setSubtitleTextColor(
                getResources().getColor(R.color.green_light, getTheme()));
    }

    private void showProfileMenu() {
        String userInfo = authViewModel.isAnonymous()
                ? "Përdorues Anonim"
                : (authViewModel.getCurrentUser().getValue() != null
                ? authViewModel.getCurrentUser().getValue().getEmail()
                : "");

        String[] options = authViewModel.isAnonymous()
                ? new String[]{"Krijo llogari", "Dil / Ndrysho llogari"}
                : new String[]{"Shiko Profilin", "Dil nga llogaria"};

        new AlertDialog.Builder(this)
                .setTitle(userInfo)
                .setItems(options, (dialog, which) -> {
                    if (authViewModel.isAnonymous()) {
                        if (which == 0) {
                            // Kalo te ProfileFragment për upgrade
                            loadFragment(new ProfileFragment());
                            bottomNavigation.setSelectedItemId(R.id.nav_profile);
                        } else {
                            showLogoutDialog();
                        }
                    } else {
                        if (which == 0) {
                            loadFragment(new ProfileFragment());
                            bottomNavigation.setSelectedItemId(R.id.nav_profile);
                        } else {
                            showLogoutDialog();
                        }
                    }
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Dil nga llogaria")
                .setMessage("A je i sigurt që dëshiron të dalësh?")
                .setPositiveButton("Dil", (dialog, which) -> {
                    new AppPreferences(this).setAuthCompleted(false);
                    authViewModel.signOut(this);
                    goToAuth();
                })
                .setNegativeButton("Anulo", null)
                .show();
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

    public void goToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}