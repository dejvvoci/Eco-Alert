package com.programimmobile.ecoalert.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.location.LocationManager;
import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

public class ReportFragment extends Fragment {

    private ReportViewModel reportViewModel;
    private AuthViewModel authViewModel;
    private LocationManager locationManager;

    private Spinner spinnerCategory;
    private TextInputEditText etDescription;
    private TextView tvLocation;
    private MaterialButton btnRefreshLocation;
    private MaterialButton btnSubmit;
    private MaterialButton btnAddPhoto;
    private ImageView ivPhotoPreview;
    private ProgressBar progressBar;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private boolean locationObtained = false;
    private Uri selectedPhotoUri = null;

    private final String[] categories = {
            "Mbetje Urbane",
            "Zhurmë",
            "Ndotje Ajri",
            "Ndotje Uji",
            "Tjetër"
    };

    // Launcher për zgjedhjen e fotos nga galeria
    private final ActivityResultLauncher<Intent> photoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            selectedPhotoUri = result.getData().getData();
                            ivPhotoPreview.setImageURI(selectedPhotoUri);
                            ivPhotoPreview.setVisibility(View.VISIBLE);
                        }
                    });

    // Launcher për kërkesën e lejes GPS
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        Boolean fineGranted = permissions.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        if (Boolean.TRUE.equals(fineGranted)) {
                            getCurrentLocation();
                        } else {
                            tvLocation.setText("Leja e lokacionit u refuzua.");
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        setupCategorySpinner();
        setupClickListeners();
        checkAndGetLocation();
    }

    private void initViews(View view) {
        spinnerCategory = view.findViewById(R.id.spinner_category);
        etDescription = view.findViewById(R.id.et_description);
        tvLocation = view.findViewById(R.id.tv_location);
        btnRefreshLocation = view.findViewById(R.id.btn_refresh_location);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnAddPhoto = view.findViewById(R.id.btn_add_photo);
        ivPhotoPreview = view.findViewById(R.id.iv_photo_preview);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupViewModels() {
        reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        locationManager = new LocationManager(requireContext());

        // Observe loading
        reportViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!isLoading);
        });

        // Observe dërgimin e suksesshëm
        reportViewModel.getReportSubmitted().observe(getViewLifecycleOwner(), submitted -> {
            if (Boolean.TRUE.equals(submitted)) {
                Toast.makeText(requireContext(),
                        "Raporti u dërgua me sukses!", Toast.LENGTH_SHORT).show();
                clearForm();
                reportViewModel.resetSubmittedStatus();
            }
        });

        // Observe gabimet
        reportViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnRefreshLocation.setOnClickListener(v -> checkAndGetLocation());

        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPickerLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void checkAndGetLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        tvLocation.setText("Duke marrë lokacionin...");
        locationManager.getCurrentLocation(new LocationManager.LocationCallback2() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                locationObtained = true;

                // Konverto koordinatat në adresë
                locationManager.getAddressFromCoordinates(latitude, longitude,
                        new LocationManager.AddressCallback() {
                            @Override
                            public void onAddressReceived(String address) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() ->
                                            tvLocation.setText(address));
                                }
                            }

                            @Override
                            public void onAddressError() {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() ->
                                            tvLocation.setText(
                                                    String.format("%.4f, %.4f",
                                                            latitude, longitude)));
                                }
                            }
                        });
            }

            @Override
            public void onLocationError(String errorMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            tvLocation.setText("Lokacioni nuk u mor: " + errorMessage));
                }
            }
        });
    }

    private void submitReport() {
        if (!locationObtained) {
            Toast.makeText(requireContext(),
                    "Prit derisa të merret lokacioni.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = categories[spinnerCategory.getSelectedItemPosition()];
        String description = etDescription.getText() != null ?
                etDescription.getText().toString().trim() : "";
        String userId = authViewModel.getCurrentUserId();

        if (userId == null) {
            Toast.makeText(requireContext(),
                    "Gabim autentifikimi. Provo përsëri.", Toast.LENGTH_SHORT).show();
            return;
        }

        Report report = new Report(userId, category, description,
                currentLatitude, currentLongitude);
        reportViewModel.addReport(report);
    }

    private void clearForm() {
        spinnerCategory.setSelection(0);
        etDescription.setText("");
        selectedPhotoUri = null;
        ivPhotoPreview.setVisibility(View.GONE);
        tvLocation.setText("Duke marrë lokacionin...");
        locationObtained = false;
        checkAndGetLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationManager.stopLocationUpdates();
    }
}