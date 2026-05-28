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

    public LocationManager(Context conte