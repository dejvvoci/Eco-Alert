package com.programimmobile.ecoalert.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.location.LocationManager;
import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;
import com.programimmobile.ecoalert.viewmodel.ReportViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private static final int MAX_PHOTOS = 3;

    private ReportViewModel reportViewModel;
    private AuthViewModel authViewModel;
    private LocationManager locationManager;

    private Spinner spinnerCategory;
    private TextInputEditText etDescription;
    private TextView tvLocation;
    private TextView tvPhotoCount;
    private HorizontalScrollView scrollPhotos;
    private LinearLayout llPhotos;
    private MaterialButton btnRefreshLocation;
    private MaterialButton btnPickLocation;
    private MaterialButton btnCamera;
    private MaterialButton btnGallery;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;

    private double currentLatitude  = 0.0;
    private double currentLongitude = 0.0;
    private boolean locationObtained = false;
    private Uri cameraPhotoUri = null;

    private final String[] categories = {
            "Mbetje Urbane", "Zhurmë", "Ndotje Ajri", "Ndotje Uji", "Tjetër"
    };

    // ─── Launchers ────────────────────────────────────────────────────────────

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        Boolean granted = permissions.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        if (Boolean.TRUE.equals(granted)) {
                            getCurrentLocation();
                        } else {
                            tvLocation.setText("Leja e lokacionit u refuzua.");
                        }
                    });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (Boolean.TRUE.equals(granted)) {
                            openCamera();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Leja e kamerës u refuzua.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && cameraPhotoUri != null) {
                            Bitmap bitmap = loadBitmapFromUri(cameraPhotoUri);
                            if (bitmap != null) {
                                addPhotoToLayout(cameraPhotoUri, bitmap);
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                Bitmap bitmap = loadBitmapFromUri(uri);
                                if (bitmap != null) {
                                    addPhotoToLayout(uri, bitmap);
                                }
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> locationPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            currentLatitude  = result.getData()
                                    .getDoubleExtra(
                                            LocationPickerActivity.EXTRA_LATITUDE, 0.0);
                            currentLongitude = result.getData()
                                    .getDoubleExtra(
                                            LocationPickerActivity.EXTRA_LONGITUDE, 0.0);
                            String address = result.getData()
                                    .getStringExtra(
                                            LocationPickerActivity.EXTRA_ADDRESS);
                            locationObtained = true;
                            tvLocation.setText(address != null ? address :
                                    String.format("%.4f, %.4f",
                                            currentLatitude, currentLongitude));
                            tvLocation.setTextColor(ContextCompat.getColor(
                                    requireContext(), R.color.green_primary_dark));
                        }
                    });

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewModels();
        setupCategorySpinner();
        setupClickListeners();
        checkAndGetLocation();
    }

    private void initViews(View view) {
        spinnerCategory    = view.findViewById(R.id.spinner_category);
        etDescription      = view.findViewById(R.id.et_description);
        tvLocation         = view.findViewById(R.id.tv_location);
        tvPhotoCount       = view.findViewById(R.id.tv_photo_count);
        scrollPhotos       = view.findViewById(R.id.scroll_photos);
        llPhotos           = view.findViewById(R.id.ll_photos);
        btnRefreshLocation = view.findViewById(R.id.btn_refresh_location);
        btnPickLocation    = view.findViewById(R.id.btn_pick_location);
        btnCamera          = view.findViewById(R.id.btn_camera);
        btnGallery         = view.findViewById(R.id.btn_gallery);
        btnSubmit          = view.findViewById(R.id.btn_submit);
        progressBar        = view.findViewById(R.id.progress_bar);
    }

    private void setupViewModels() {
        reportViewModel = new ViewModelProvider(requireActivity())
                .get(ReportViewModel.class);
        authViewModel   = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);
        locationManager = new LocationManager(requireContext());

        reportViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!Boolean.TRUE.equals(loading));
        });

        reportViewModel.getReportSubmitted().observe(getViewLifecycleOwner(),
                submitted -> {
                    if (Boolean.TRUE.equals(submitted)) {
                        Toast.makeText(requireContext(),
                                "Raporti u dërgua me sukses!",
                                Toast.LENGTH_SHORT).show();
                        clearForm();
                        reportViewModel.resetSubmittedStatus();
                    }
                });

        reportViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnRefreshLocation.setOnClickListener(v -> checkAndGetLocation());

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),
                    LocationPickerActivity.class);
            intent.putExtra(LocationPickerActivity.EXTRA_INIT_LAT,
                    currentLatitude);
            intent.putExtra(LocationPickerActivity.EXTRA_INIT_LNG,
                    currentLongitude);
            locationPickerLauncher.launch(intent);
        });

        btnCamera.setOnClickListener(v -> {
            if (llPhotos.getChildCount() >= MAX_PHOTOS) {
                Toast.makeText(requireContext(),
                        "Maksimumi 3 foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> {
            if (llPhotos.getChildCount() >= MAX_PHOTOS) {
                Toast.makeText(requireContext(),
                        "Maksimumi 3 foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    // ─── Kamera ───────────────────────────────────────────────────────────────

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile == null) {
                Toast.makeText(requireContext(),
                        "Gabim gjatë krijimit të skedarit.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Hiq resolveActivity — launch direkt
            cameraLauncher.launch(cameraIntent);

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Gabim: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String fileName = "PHOTO_" + timeStamp + "_";

            // Provo cache internal — më i sigurt
            File storageDir = requireContext().getCacheDir();
            File imageFile = File.createTempFile(
                    fileName, ".jpg", storageDir);

            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }



    // ─── Foto Layout ──────────────────────────────────────────────────────────

    private void addPhotoToLayout(Uri uri, Bitmap bitmap) {
        FrameLayout frame = new FrameLayout(requireContext());
        LinearLayout.LayoutParams frameParams =
                new LinearLayout.LayoutParams(dpToPx(80), dpToPx(80));
        frameParams.setMarginEnd(dpToPx(8));
        frame.setLayoutParams(frameParams);

        // ImageView e fotos
        ImageView ivPhoto = new ImageView(requireContext());
        ivPhoto.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivPhoto.setImageBitmap(bitmap);
        ivPhoto.setTag(uri);

        // Butoni X për fshirje
        ImageView btnRemove = new ImageView(requireContext());
        FrameLayout.LayoutParams removeParams =
                new FrameLayout.LayoutParams(dpToPx(22), dpToPx(22));
        removeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        btnRemove.setLayoutParams(removeParams);
        btnRemove.setImageResource(
                android.R.drawable.ic_menu_close_clear_cancel);
        btnRemove.setBackgroundColor(0xCC000000);
        btnRemove.setPadding(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
        btnRemove.setClickable(true);
        btnRemove.setFocusable(true);

        frame.addView(ivPhoto);
        frame.addView(btnRemove);

        btnRemove.setOnClickListener(v -> {
            llPhotos.removeView(frame);
            updatePhotoCount();
        });

        llPhotos.addView(frame);
        updatePhotoCount();
    }

    private void updatePhotoCount() {
        int count = llPhotos.getChildCount();
        tvPhotoCount.setText(count + "/" + MAX_PHOTOS);
        scrollPhotos.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources()
                .getDisplayMetrics().density);
    }

    // ─── Konverto foto ────────────────────────────────────────────────────────

    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media
                    .getBitmap(requireContext().getContentResolver(), uri);

            int maxSize = 600;
            int w = bitmap.getWidth(), h = bitmap.getHeight();
            if (w > maxSize || h > maxSize) {
                float scale = Math.min(
                        (float) maxSize / w, (float) maxSize / h);
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        Math.round(w * scale), Math.round(h * scale), true);
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] bytes = baos.toByteArray();

        if (bytes.length > 250000) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 15, baos);
            bytes = baos.toByteArray();
        }
        return android.util.Base64.encodeToString(bytes,
                android.util.Base64.DEFAULT);
    }

    // ─── GPS ──────────────────────────────────────────────────────────────────

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
        locationManager.getCurrentLocation(
                new LocationManager.LocationCallback2() {
                    @Override
                    public void onLocationReceived(double lat, double lng) {
                        currentLatitude  = lat;
                        currentLongitude = lng;
                        locationObtained = true;

                        locationManager.getAddressFromCoordinates(lat, lng,
                                new LocationManager.AddressCallback() {
                                    @Override
                                    public void onAddressReceived(String address) {
                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(() -> {
                                                tvLocation.setText(address);
                                                tvLocation.setTextColor(
                                                        ContextCompat.getColor(requireContext(), R.color.green_primary_dark));
                                            });
                                        }
                                    }

                                    @Override
                                    public void onAddressError() {
                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(() -> {
                                                tvLocation.setText(String.format("%.4f, %.4f", lat, lng));
                                                tvLocation.setTextColor(
                                                        ContextCompat.getColor(requireContext(), R.color.green_primary_dark));
                                            });
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onLocationError(String errorMessage) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(
                                    () -> tvLocation.setText(
                                            "Lokacioni nuk u mor."));
                        }
                    }
                });
    }

    // ─── Submit ───────────────────────────────────────────────────────────────

    private void submitReport() {
        if (!locationObtained
                || (currentLatitude == 0.0 && currentLongitude == 0.0)) {
            Toast.makeText(requireContext(),
                    "Lokacioni është i detyrueshëm.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String category    = categories[spinnerCategory.getSelectedItemPosition()];
        String description = etDescription.getText() != null
                ? etDescription.getText().toString().trim() : "";
        String userId      = authViewModel.getCurrentUserId();

        if (userId == null) {
            Toast.makeText(requireContext(),
                    "Gabim autentifikimi.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        new Thread(() -> {
            List<String> base64Photos = new ArrayList<>();

            for (int i = 0; i < llPhotos.getChildCount(); i++) {
                View child = llPhotos.getChildAt(i);
                if (child instanceof FrameLayout) {
                    ImageView iv = (ImageView)
                            ((FrameLayout) child).getChildAt(0);
                    Uri uri = (Uri) iv.getTag();
                    if (uri != null) {
                        Bitmap bmp = loadBitmapFromUri(uri);
                        if (bmp != null) {
                            base64Photos.add(bitmapToBase64(bmp));
                        }
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                Report report = new Report(userId, category, description,
                        currentLatitude, currentLongitude);
                if (!base64Photos.isEmpty()) {
                    report.setPhotos(base64Photos);
                }
                reportViewModel.addReport(report);
            });
        }).start();
    }

    // ─── Clear ────────────────────────────────────────────────────────────────

    private void clearForm() {
        spinnerCategory.setSelection(0);
        etDescription.setText("");
        llPhotos.removeAllViews();
        scrollPhotos.setVisibility(View.GONE);
        tvPhotoCount.setText("0/" + MAX_PHOTOS);
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