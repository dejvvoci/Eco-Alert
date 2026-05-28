package com.programimmobile.ecoalert.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.location.LocationManager;
import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class MapFragment extends Fragment {

    private ReportViewModel reportViewModel;
    private LocationManager locationManager;

    private MapView mapView;
    private ProgressBar progressBar;
    private FloatingActionButton fabMyLocation;
    private ChipGroup chipGroupFilter;

    private String currentFilter = "Të gjitha";

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        Boolean granted = permissions.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        if (Boolean.TRUE.equals(granted)) {
                            centerMapOnMyLocation();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(
                requireContext().getPackageName());
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupMap();
        setupViewModel();
        setupChipFilters();
        setupFab();
    }

    private void initViews(View view) {
        mapView = view.findViewById(R.id.map_view);
        progressBar = view.findViewById(R.id.progress_bar);
        fabMyLocation = view.findViewById(R.id.fab_my_location);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        locationManager = new LocationManager(requireContext());
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);

        // Koordinatat e Tiranës si default
        GeoPoint startPoint = new GeoPoint(41.3275, 19.8187);
        mapView.getController().setCenter(startPoint);
    }

    private void setupViewModel() {
        reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

        progressBar.setVisibility(View.VISIBLE);

        reportViewModel.getAllReports().observe(getViewLifecycleOwner(), reports -> {
            progressBar.setVisibility(View.GONE);
            if (reports != null) {
                updateMarkers(reports);
            }
        });
    }

    private void setupChipFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                currentFilter = "Të gjitha";
            } else if (checkedId == R.id.chip_waste) {
                currentFilter = "Mbetje Urbane";
            } else if (checkedId == R.id.chip_noise) {
                currentFilter = "Zhurmë";
            } else if (checkedId == R.id.chip_air) {
                currentFilter = "Ndotje Ajri";
            } else if (checkedId == R.id.chip_water) {
                currentFilter = "Ndotje Uji";
            } else if (checkedId == R.id.chip_other) {
                currentFilter = "Tjetër";
            }

            // Rifresko markers me filtrin e ri
            List<Report> reports = reportViewModel.getAllReports().getValue();
            if (reports != null) {
                updateMarkers(reports);
            }
        });
    }

    private void setupFab() {
        fabMyLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                centerMapOnMyLocation();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });
    }

    private void updateMarkers(List<Report> reports) {
        // Pastro të gjitha markers ekzistuese
        mapView.getOverlays().clear();

        for (Report report : reports) {
            // Apliko filtrin
            if (!currentFilter.equals("Të gjitha")
                    && !currentFilter.equals(report.getCategory())) {
                continue;
            }

            addMarkerForReport(report);
        }

        mapView.invalidate();
    }

    private void addMarkerForReport(Report report) {
        GeoPoint point = new GeoPoint(report.getLatitude(), report.getLongitude());
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Titulli dhe përshkrimi i popup-it
        marker.setTitle(report.getCategory());
        String snippet = "";
        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            snippet = report.getDescription().length() > 60
                    ? report.getDescription().substring(0, 60) + "..."
                    : report.getDescription();
        }
        if (report.getConfirmations() > 0) {
            snippet += "\n✓ " + report.getConfirmations() + " konfirmime";
        }
        marker.setSnippet(snippet);

        // Ngjyra e markerit sipas kategorisë
        marker.setIcon(getMarkerIcon(report.getCategory()));

        // Klikim mbi marker — hap ReportDetailActivity
        marker.setOnMarkerClickListener((m, map) -> {
            m.showInfoWindow();
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    private Drawable getMarkerIcon(String category) {
        int colorRes;
        switch (category) {
            case "Mbetje Urbane":
                colorRes = R.color.category_waste;
                break;
            case "Zhurmë":
                colorRes = R.color.category_noise;
                break;
            case "Ndotje Ajri":
                colorRes = R.color.category_air;
                break;
            case "Ndotje Uji":
                colorRes = R.color.category_water;
                break;
            default:
                colorRes = R.color.category_other;
                break;
        }

        Drawable icon = ContextCompat.getDrawable(requireContext(),
                android.R.drawable.ic_menu_myplaces);
        if (icon != null) {
            icon.setTint(ContextCompat.getColor(requireContext(), colorRes));
        }
        return icon;
    }

    private void centerMapOnMyLocation() {
        locationManager.getCurrentLocation(new LocationManager.LocationCallback2() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        GeoPoint myLocation = new GeoPoint(latitude, longitude);
                        mapView.getController().animateTo(myLocation);
                        mapView.getController().setZoom(15.0);
                    });
                }
            }

            @Override
            public void onLocationError(String errorMessage) {
                // Mbetet te lokacioni aktual i hartës
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationManager.stopLocationUpdates();
        mapView.onDetach();
    }
}