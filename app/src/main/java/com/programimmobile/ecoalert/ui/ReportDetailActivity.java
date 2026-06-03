package com.programimmobile.ecoalert.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.location.LocationManager;
import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Locale;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReportDetailActivity extends AppCompatActivity {

    private ReportViewModel reportViewModel;
    private AuthViewModel authViewModel;
    private LocationManager locationManager;

    private MaterialToolbar toolbar;
    private ImageView ivCategoryIcon;
    private ImageView ivPhoto;
    private TextView tvCategory;
    private TextView tvDate;
    private TextView tvStatus;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvConfirmations;
    private MaterialButton btnConfirm;
    private MaterialButton btnDelete;
    private MapView miniMap;
    private MaterialCardView cardPhoto;

    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_report_detail);

        initViews();
        setupToolbar();
        setupViewModels();

        reportId = getIntent().getStringExtra("report_id");
        if (reportId != null) {
            loadReport(reportId);
        }
    }

    private void initViews() {
        toolbar          = findViewById(R.id.toolbar);
        ivCategoryIcon   = findViewById(R.id.iv_category_icon);
        ivPhoto          = findViewById(R.id.iv_photo);
        tvCategory       = findViewById(R.id.tv_category);
        tvDate           = findViewById(R.id.tv_date);
        tvStatus         = findViewById(R.id.tv_status);
        tvDescription    = findViewById(R.id.tv_description);
        tvLocation       = findViewById(R.id.tv_location);
        tvConfirmations  = findViewById(R.id.tv_confirmations);
        btnConfirm       = findViewById(R.id.btn_confirm);
        btnDelete        = findViewById(R.id.btn_delete);
        miniMap          = findViewById(R.id.mini_map);
        cardPhoto        = findViewById(R.id.card_photo);
        locationManager  = new LocationManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModels() {
        reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        authViewModel   = new ViewModelProvider(this).get(AuthViewModel.class);

        reportViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadReport(String id) {
        reportViewModel.getReportById(id).observe(this, report -> {
            if (report != null) {
                populateUI(report);
            }
        });
    }

    private void populateUI(Report report) {
        // Kategoria
        tvCategory.setText(report.getCategory() != null
                ? report.getCategory() : "—");

        // Përshkrimi
        tvDescription.setText(report.getDescription() != null
                && !report.getDescription().isEmpty()
                ? report.getDescription() : "Pa përshkrim.");

        // Statusi
        tvStatus.setText(report.getStatus() != null
                ? report.getStatus() : "E re");

        // Konfirmimet
        tvConfirmations.setText(report.getConfirmations()
                + " persona e kanë konfirmuar");

        // Data
        if (report.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDate.setText(sdf.format(report.getTimestamp()));
        }

        // Ngjyra e ikonës
        int colorRes = getCategoryColor(report.getCategory());
        ivCategoryIcon.setColorFilter(
                getResources().getColor(colorRes, getTheme()));

        // Foto — Base64
        List<String> photos = report.getPhotos();
        String legacyPhoto  = report.getPhotoUrl();

// Ndërto listën e plotë
        List<String> allPhotos = new ArrayList<>();
        if (photos != null && !photos.isEmpty()) {
            allPhotos.addAll(photos);
        } else if (legacyPhoto != null && !legacyPhoto.isEmpty()
                && !legacyPhoto.startsWith("http")) {
            // Legacy Base64
            allPhotos.add(legacyPhoto);
        }

        RecyclerView rvDetailPhotos = findViewById(R.id.rv_detail_photos);

        if (!allPhotos.isEmpty()) {
            cardPhoto.setVisibility(View.VISIBLE);
            rvDetailPhotos.setLayoutManager(
                    new androidx.recyclerview.widget.LinearLayoutManager(
                            this,
                            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                            false));
            rvDetailPhotos.setAdapter(new DetailPhotoAdapter(allPhotos));
        } else {
            cardPhoto.setVisibility(View.GONE);
        }

        // Adresa
        double lat = report.getLatitude();
        double lng = report.getLongitude();
        if (lat != 0.0 && lng != 0.0) {
            locationManager.getAddressFromCoordinates(lat, lng,
                    new LocationManager.AddressCallback() {
                        @Override
                        public void onAddressReceived(String address) {
                            runOnUiThread(() -> tvLocation.setText(address));
                        }

                        @Override
                        public void onAddressError() {
                            runOnUiThread(() -> tvLocation.setText(
                                    String.format("%.4f, %.4f", lat, lng)));
                        }
                    });
            setupMiniMap(lat, lng);
        }

        // Butoni Delete — vetëm pronari
        String currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(report.getUserId())) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Click listeners
        btnConfirm.setOnClickListener(v -> {
            reportViewModel.confirmReport(report.getId());
            Toast.makeText(this,
                    "Faleminderit për konfirmimin!", Toast.LENGTH_SHORT).show();
            btnConfirm.setEnabled(false);
            btnConfirm.setText("U konfirmua ✓");
        });

        btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Fshi Raportin")
                        .setMessage("A je i sigurt? Ky veprim nuk mund të zhbëhet.")
                        .setPositiveButton("Fshi", (dialog, which) -> {
                            reportViewModel.deleteReport(report.getId());
                            Toast.makeText(this,
                                    "Raporti u fshi.", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("Anulo", null)
                        .show());
    }

    private void setupMiniMap(double latitude, double longitude) {
        miniMap.setTileSource(TileSourceFactory.MAPNIK);
        miniMap.setMultiTouchControls(false);
        miniMap.setClickable(false);
        miniMap.getController().setZoom(15.0);

        GeoPoint point = new GeoPoint(latitude, longitude);
        miniMap.getController().setCenter(point);

        Marker marker = new Marker(miniMap);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        miniMap.getOverlays().add(marker);
        miniMap.invalidate();
    }

    private int getCategoryColor(String category) {
        if (category == null) return R.color.category_other;
        switch (category) {
            case "Mbetje Urbane": return R.color.category_waste;
            case "Zhurmë":       return R.color.category_noise;
            case "Ndotje Ajri":  return R.color.category_air;
            case "Ndotje Uji":   return R.color.category_water;
            default:             return R.color.category_other;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onResume()  { super.onResume();  miniMap.onResume();  }
    @Override protected void onPause()   { super.onPause();   miniMap.onPause();   }
    @Override protected void onDestroy() { super.onDestroy(); miniMap.onDetach();  }
}