package com.programimmobile.ecoalert.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.programimmobile.ecoalert.model.Report;

import java.util.ArrayList;
import java.util.List;
import com.programimmobile.ecoalert.model.SentReport;

public class ReportRepository {

    private static final String COLLECTION_REPORTS = "reports";

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Report>> allReportsLiveData;
    private final MutableLiveData<String> errorLiveData;
    private ListenerRegistration listenerRegistration;

    // Singleton
    private static ReportRepository instance;

    public static ReportRepository getInstance() {
        if (instance == null) {
            instance = new ReportRepository();
        }
        return instance;
    }

    private ReportRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.allReportsLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();
    }

    // ─── Real-time listener — të gjitha raportet ────────────────────────────
    // Çdo përdorues shikon raportet e të gjithëve në hartë

    public LiveData<List<Report>> getAllReports() {
        startListening();
        return allReportsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    private void startListening() {
        if (listenerRegistration != null) return;

        listenerRegistration = db.collection(COLLECTION_REPORTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        errorLiveData.postValue(error.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<Report> reports = new ArrayList<>();
                        reports.addAll(snapshots.toObjects(Report.class));
                        allReportsLiveData.postValue(reports);
                    }
                });
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    // ─── Shto raport të ri ───────────────────────────────────────────────────

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void addReport(Report report, ActionCallback callback) {
        db.collection(COLLECTION_REPORTS)
                .add(report)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Fshi raport (vetëm pronari) ─────────────────────────────────────────

    public void deleteReport(String reportId, ActionCallback callback) {
        db.collection(COLLECTION_REPORTS)
                .document(reportId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Konfirmo raport nga komunitetit ─────────────────────────────────────

    public void confirmReport(String reportId, ActionCallback callback) {
        db.collection(COLLECTION_REPORTS)
                .document(reportId)
                .update("confirmations",
                        com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Raportet e një përdoruesi specifik (për historikun) ─────────────────

    public void getReportsByUser(String userId,
                                 MutableLiveData<List<Report>> liveData) {
        db.collection(COLLECTION_REPORTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        errorLiveData.postValue(error.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        List<Report> reports = new ArrayList<>();
                        reports.addAll(snapshots.toObjects(Report.class));
                        liveData.postValue(reports);
                    }
                });
    }

    // ─── Filtrim sipas kategorisë ─────────────────────────────────────────────

    public void getReportsByCategory(String category,
                                     MutableLiveData<List<Report>> liveData) {
        db.collection(COLLECTION_REPORTS)
                .whereEqualTo("category", category)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots != null) {
                        List<Report> reports = new ArrayList<>();
                        reports.addAll(snapshots.toObjects(Report.class));
                        liveData.postValue(reports);
                    }
                });
    }

    public void getReportById(String reportId,
                              MutableLiveData<Report> liveData) {
        db.collection(COLLECTION_REPORTS)
                .document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Report report = documentSnapshot.toObject(Report.class);
                        liveData.postValue(report);
                    }
                })
                .addOnFailureListener(e ->
                        errorLiveData.postValue(e.getMessage()));
    }

    public void updateReportStatus(String reportId, String status,
                                   ActionCallback callback) {
        db.collection(COLLECTION_REPORTS)
                .document(reportId)
                .update("status", status)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    // Shto këto konstante
    private static final String COLLECTION_SENT = "sent_reports";

// Shto këto metoda

    public void saveSentReport(SentReport sentReport,
                               ActionCallback callback) {
        db.collection(COLLECTION_SENT)
                .add(sentReport)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getSentReports(String adminId,
                               MutableLiveData<List<SentReport>> liveData) {
        db.collection(COLLECTION_SENT)
                .whereEqualTo("adminId", adminId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots != null) {
                        List<SentReport> list = new ArrayList<>();
                        list.addAll(snapshots.toObjects(SentReport.class));
                        liveData.postValue(list);
                    }
                });
    }
}