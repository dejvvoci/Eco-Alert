package com.programimmobile.ecoalert.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.repository.UserRepository;
import com.programimmobile.ecoalert.utils.AppPreferences;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private BottomNavigationView bottomNavigation;
    private MaterialToolbar toolbar;
    private boolean isAdmin = false;

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
        checkRoleAndSetup(savedInstanceState);
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
    }

    private void checkRoleAndSetup(Bundle savedInstanceState) {
        String userId = authViewModel.getCurrentUserId();
        if (userId == null) {
            goToAuth();
            return;
        }

        MutableLiveData<Boolean> isAdminLiveData = new MutableLiveData<>();
        UserRepository.getInstance().checkIsAdmin(userId, isAdminLiveData);

        isAdminLiveData.observe(this, admin -> {
            isAdmin = Boolean.TRUE.equals(admin);
            setupBottomNavigation();
            updateToolbarSubtitle();
            if (savedInstanceState == null) {
                if (isAdmin) {
                    loadFragment(new AdminReportsFragment());
                    bottomNavigation.setSelectedItemId(R.id.nav_admin_reports);
                } else {
                    loadFragment(new ReportFragment());
                    bottomNavigation.setSelectedItemId(R.id.nav_report);
                }
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.getMenu().clear();
        if (isAdmin) {
            bottomNavigation.inflateMenu(R.menu.bottom_nav_admin);
        } else {
            bottomNavigation.inflateMenu(R.menu.bottom_nav_menu);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_report) {
                fragment = new ReportFragment();
            } else if (id == R.id.nav_admin_reports) {
                fragment = new AdminReportsFragment();
            } else if (id == R.id.nav_map) {
                fragment = new MapFragment();
            } else if (id == R.id.nav_history) {
                fragment = new HistoryFragment();
            } else if (id == R.id.nav_sent) {
                fragment = new SentReportsFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (id == R.id.nav_notifications) {
                fragment = new NotificationsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void updateToolbarSubtitle() {
        String subtitle = isAdmin ? "Admin" :
                (authViewModel.isAnonymous() ? "Anonim" : "");
        toolbar.setSubtitle(subtitle);
        toolbar.setSubtitleTextColor(
                getResources().getColor(R.color.green_light, getTheme()));
    }

    private void showProfileMenu() {
        if (authViewModel.isAnonymous()) {
            // User anonim — vetëm opsioni për të krijuar llogari
            new AlertDialog.Builder(this)
                    .setTitle("Përdorues Anonim")
                    .setMessage("Nëse dilni pa krijuar llogari, " +
                            "raportet tuaja do humbasin.\n\n" +
                            "Krijoni llogari për t'i ruajtur.")
                    .setPositiveButton("Krijo Llogari", (d, w) -> {
                        loadFragment(new ProfileFragment());
                        bottomNavigation.setSelectedItemId(R.id.nav_profile);
                    })
                    .setNegativeButton("Mbyll", null)
                    .show();
        } else {
            // User me llogari — opsion normal
            String[] options = {"Shiko Profilin", "Dil nga llogaria"};
            new AlertDialog.Builder(this)
                    .setTitle(authViewModel.getCurrentUser().getValue() != null
                            ? authViewModel.getCurrentUser()
                            .getValue().getEmail()
                            : "Profili")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            loadFragment(new ProfileFragment());
                            bottomNavigation.setSelectedItemId(
                                    R.id.nav_profile);
                        } else {
                            showLogoutDialog();
                        }
                    })
                    .show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Dil nga llogaria")
                .setMessage("A je i sigurt?")
                .setPositiveButton("Dil", (d, w) -> {
                    new AppPreferences(this).setAuthCompleted(false);
                    authViewModel.signOut(this);
                    goToAuth();
                })
                .setNegativeButton("Anulo", null)
                .show();
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public boolean getIsAdmin() { return isAdmin; }

    public void goToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}