package com.programimmobile.ecoalert.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.location.LocationManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class LocationPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LATITUDE  = "selected_latitude";
    public static final String EXTRA_LONGITUDE = "selected_longitude";
    public static final String EXTRA_ADDRESS   = "selected_address";

    // Koordinatat fillestare — kalon nga ReportFragment
    public static final String EXTRA_INIT_LAT  = "init_latitude";
    public static final String EXTRA_INIT_LNG  = "init_longitude";

    private MapView mapView;
    private TextView tvSelectedAddress;
    private ProgressBar progressAddress;
    private MaterialButton btnConfirm;
    private FloatingActionButton fabMyLocation;

    private Marker selectedMarker;
    private LocationManager locationManager;

    private double selectedLatitude  = 0.0;
    private double selectedLongitude = 0.0;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        Boolean granted = permissions.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        if (Boolean.TRUE.equals(granted)) {
                            centerOnMyLocation();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_location_picker);

        locationManager = new LocationManager(this);

        initViews();
        setupMap();
        setupInitialLocation();
        setupClickListeners();
    }

    private void initViews() {
        mapView         = findViewById(R.id.map_picker);
        tvSelectedAddress = findViewById(R.id.tv_selected_address);
        progressAddress = findViewById(R.id.progress_address);
        btnConfirm      = findViewById(R.id.btn_confirm_location);
        fabMyLocation   = findViewById(R.id.fab_my_location);

        // Çaktivizo konfirmo derisa të zgjidhet lokacioni
        btnConfirm.setEnabled(false);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Dëgjo klikime mbi hartë
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectLocation(p.getLatitude(), p.getLongitude());
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
    }

    private void setupInitialLocation() {
        double initLat = getIntent().getDoubleExtra(EXTRA_INIT_LAT, 0.0);
        double initLng = getIntent().getDoubleExtra(EXTRA_INIT_LNG, 0.0);

        if (initLat != 0.0 && initLng != 0.0) {
            // Ka lokacion GPS — vendose si të zgjedhur paraprakisht
            GeoPoint startPoint = new GeoPoint(initLat, initLng);
            mapView.getController().setCenter(startPoint);
            selectLocation(initLat, initLng);
        } else {
            // Pa lokacion — cento te Tirana si default
            GeoPoint tirana = new GeoPoint(41.3275, 19.8187);
            mapView.getController().setCenter(tirana);
            centerOnMyLocation();
        }
    }

    private void selectLocation(double latitude, double longitude) {
        selectedLatitude  = latitude;
        selectedLongitude = longitude;

        // Shto / zhvendos marker
        if (selectedMarker == null) {
            selectedMarker = new Marker(mapView);
            selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            selectedMarker.setTitle("Lokacioni i zgjedhur");
            mapView.getOverlays().add(selectedMarker);
        }

        GeoPoint point = new GeoPoint(latitude, longitude);
        selectedMarker.setPosition(point);
        mapView.getController().animateTo(point);
        mapView.invalidate();

        // Aktivizo butonin
        btnConfirm.setEnabled(true);

        // Merr adresën
        progressAddress.setVisibility(View.VISIBLE);
        tvSelectedAddress.setText("Duke kërkuar adresën...");

        locationManager.getAddressFromCoordinates(latitude, longitude,
                new LocationManager.AddressCallback() {
                    @Override
                    public void onAddressReceived(String address) {
                        runOnUiThread(() -> {
                            progressAddress.setVisibility(View.GONE);
                            tvSelectedAddress.setText(address);
                        });
                    }

                    @Override
                    public void onAddressError() {
                        runOnUiThread(() -> {
                            progressAddress.setVisibility(View.GONE);
                            tvSelectedAddress.setText(
                                    String.format("%.4f, %.4f", latitude, longitude));
                        });
                    }
                });
    }

    private void centerOnMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.getCurrentLocation(
                    new LocationManager.LocationCallback2() {
                        @Override
                        public void onLocationReceived(double lat, double lng) {
                            runOnUiThread(() -> {
                                GeoPoint myPoint = new GeoPoint(lat, lng);
                                mapView.getController().animateTo(myPoint);
                                mapView.getController().setZoom(16.0);
                                // Vendos si lokacion i zgjedhur nëse nuk ka
                                if (selectedLatitude == 0.0) {
                                    selectLocation(lat, lng);
                                }
                            });
                        }

                        @Override
                        public void onLocationError(String errorMessage) {}
                    });
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void setupClickListeners() {
        fabMyLocation.setOnClickListener(v -> centerOnMyLocation());

        btnConfirm.setOnClickListener(v -> {
            if (selectedLatitude == 0.0 && selectedLongitude == 0.0) return;

            Intent result = new Intent();
            result.putExtra(EXTRA_LATITUDE,  selectedLatitude);
            result.putExtra(EXTRA_LONGITUDE, selectedLongitude);
            result.putExtra(EXTRA_ADDRESS,
                    tvSelectedAddress.getText().toString());
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.stopLocationUpdates();
        mapView.onDetach();
    }
}