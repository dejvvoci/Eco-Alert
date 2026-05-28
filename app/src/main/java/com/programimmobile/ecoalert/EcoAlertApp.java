package com.programimmobile.ecoalert;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import org.osmdroid.config.Configuration;

public class EcoAlertApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializo Firebase
        FirebaseApp.initializeApp(this);

        // Aktivizo cache offline për Firestore
        // Kështu app funksionon edhe pa internet
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Inicializo OSMDroid — kërkon kontekstin e aplikacionit
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }
}