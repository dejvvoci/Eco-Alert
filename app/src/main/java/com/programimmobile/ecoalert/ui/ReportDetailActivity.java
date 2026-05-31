package com.programimmobile.ecoalert.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.location.LocationManager;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

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
    private String reportUserId;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_report_detail);

        initViews();
        setupToolbar();
        loadDataFromIntent();
        setupMiniMap();
        setupViewModel();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivCategoryIcon = findViewById(R.id.iv_category_icon);
        ivPhoto = findViewById(R.id.iv_photo);
        tvCategory = findViewById(R.id.tv_category);
        tvDate = findViewById(R.id.tv_date);
        tvStatus = findViewById(R.id.tv_status);
        tvDescription = findViewById(R.id.tv_description);
        tvLocation = findViewById(R.id.tv_location);
        tvConfirmations = findViewById(R.id.tv_confirmations);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnDelete = findViewById(R.id.btn_delete);
        miniMap = findViewById(R.id.mini_map);
        cardPhoto = findViewById(R.id.card_photo);
        locationManager = new LocationManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadDataFromIntent() {
        reportId = getIntent().getStringExtra("report_id");
        reportUserId = getIntent().getStringExtra("report_user_id");
        String category = getIntent().getStringExtra("category");
        String description = getIntent().getStringExtra("description");
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        String status = getIntent().getStringExtra("status");
        int confirmations = getIntent().getIntExtra("confirmations", 0);
        String photoUrl = getIntent().getStringExtra("photo_url");

        // Të dhënat bazë
        tvCategory.setText(category != null ? category : "—");
        tvDescription.setText(description != null && !description.isEmpty()
                ? description : "Pa përshkrim.");
        tvStatus.setText(status != null ? status : "E re");
        tvConfirmations.setText(confirmations + " persona e kanë konfirmuar");

        // Ngjyra e ikonës
        int colorRes = getCategoryColor(category);
        ivCategoryIcon.setColorFilter(
                getResources().getColor(colorRes, getTheme()));

        // Foto — shfaq nëse ekziston
        if (photoUrl != null && !photoUrl.isEmpty()) {
            cardPhoto.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(ivPhoto);
        } else {
            cardPhoto.setVisibility(View.GONE);
        }

        // Adresa nga koordinatat
        if (latitude != 0.0 && longitude != 0.0) {
            locationManager.getAddressFromCoordinates(latitude, longitude,
                    new LocationManager.AddressCallback() {
                        @Override
                        public void onAddressReceived(String address) {
                            runOnUiThread(() -> tvLocation.setText(address));
                        }

                        @Override
                        public void onAddressError() {
                            runOnUiThread(() -> tvLocation.setText(
                                    String.format("%.4f, %.4f", latitude, longitude)));
                        }
                    });
        }
    }

    private void setupMiniMap() {
        if (latitude == 0.0 && longitude == 0.0) return;

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

    private void setupViewModel() {
        reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Shfaq butonin delete vetëm nëse është pronari
        String currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(reportUserId)) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        reportViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        // Konfirmo raportin
        btnConfirm.setOnClickListener(v -> {
            if (reportId != null) {
                reportViewModel.confirmReport(reportId);
                Toast.makeText(this,
                        "Faleminderit për konfirmimin!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(false);
                btnConfirm.setText("U konfirmua ✓");
            }
        });

        // Fshi raportin — vetëm pronari
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Fshi Raportin")
                    .setMessage("A je i sigurt që dëshiron ta fshish këtë raport? " +
                            "Ky veprim nuk mund të zhbëhet.")
                    .setPositiveButton("Fshi", (dialog, which) -> {
                        if (reportId != null) {
                            reportViewModel.deleteReport(reportId);
                            Toast.makeText(this,
                                    "Raporti u fshi.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("Anulo", null)
                    .show();
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        miniMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        miniMap.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        miniMap.onDetach();
    }
}