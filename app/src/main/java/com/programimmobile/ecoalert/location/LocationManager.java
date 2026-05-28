package com.programimmobile.ecoalert.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationManager {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface LocationCallback2 {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String errorMessage);
    }

    public interface AddressCallback {
        void onAddressReceived(String address);
        void onAddressError();
    }

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public LocationManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // ─── Kontrollo lejet ─────────────────────────────────────────────────────

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    // ─── Merr lokacionin një herë ─────────────────────────────────────────────

    public void getCurrentLocation(LocationCallback2 callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("Leja e lokacionit nuk është dhënë.");
            return;
        }

        // Provoj me last known location fillimisht — më e shpejtë
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationReceived(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                        } else {
                            // Nëse last location është null, kërko lokacion të ri
                            requestFreshLocation(callback);
                        }
                    })
                    .addOnFailureListener(e ->
                            callback.onLocationError("Gabim gjatë marrjes së lokacionit: "
                                    + e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationError("Leja e lokacionit u refuzua.");
        }
    }

    private void requestFreshLocation(LocationCallback2 callback) {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    android.location.Location location =
                            locationResult.getLocations().get(0);
                    callback.onLocationReceived(
                            location.getLatitude(),
                            location.getLongitude()
                    );
                    stopLocationUpdates();
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            callback.onLocationError("Leja e lokacionit u refuzua.");
        }
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    // ─── Konverto koordinata → adresë të lexueshme ───────────────────────────

    public void getAddressFromCoordinates(double latitude, double longitude,
                                          AddressCallback callback) {
        if (!Geocoder.isPresent()) {
            callback.onAddressError();
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses =
                        geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (address.getThoroughfare() != null) {
                        sb.append(address.getThoroughfare());
                    }
                    if (address.getLocality() != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(address.getLocality());
                    }

                    callback.onAddressReceived(sb.toString());
                } else {
                    callback.onAddressError();
                }
            } catch (IOException e) {
                callback.onAddressError();
            }
        }).start();
    }
}