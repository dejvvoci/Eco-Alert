package com.programimmobile.ecoalert.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.repository.ReportRepository;

import java.util.List;
import com.programimmobile.ecoalert.model.Notification;
import com.programimmobile.ecoalert.repository.NotificationRepository;
import com.programimmobile.ecoalert.model.SentReport;

public class ReportViewModel extends AndroidViewModel {

    private final ReportRepository repository;

    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MutableLiveData<Boolean> reportSubmittedLiveData;
    private final MutableLiveData<List<Report>> userReportsLiveData;

    public ReportViewModel(@NonNull Application application) {
        super(application);
        this.repository = ReportRepository.getInstance();
        this.errorLiveData = new MutableLiveData<>();
        this.loadingLiveData = new MutableLiveData<>(false);
        this.reportSubmittedLiveData = new MutableLiveData<>(false);
        this.userReportsLiveData = new MutableLiveData<>();
    }

    // ─── Të gjitha raportet — për hartën (real-time) ──────────────────────────

    public LiveData<List<Report>> getAllReports() {
        return repository.getAllReports();
    }

    // ─── Raportet e përdoruesit — për historikun ──────────────────────────────

    public LiveData<List<Report>> getUserReports(String userId) {
        repository.getReportsByUser(userId, userReportsLiveData);
        return userReportsLiveData;
    }

    // ─── Filtrim sipas kategorisë ─────────────────────────────────────────────

    public LiveData<List<Report>> getReportsByCategory(String category) {
        MutableLiveData<List<Report>> filteredLiveData = new MutableLiveData<>();
        repository.getReportsByCategory(category, filteredLiveData);
        return filteredLiveData;
    }

    // ─── LiveData getters ─────────────────────────────────────────────────────

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getReportSubmitted() {
        return reportSubmittedLiveData;
    }

    // ─── Shto raport të ri ────────────────────────────────────────────────────

    public void addReport(Report report) {
        loadingLiveData.setValue(true);
        repository.addReport(report, new ReportRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.postValue(false);
                reportSubmittedLiveData.postValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Fshi raport ──────────────────────────────────────────────────────────

    public void deleteReport(String reportId) {
        loadingLiveData.setValue(true);
        repository.deleteReport(reportId, new ReportRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Konfirmo raport ──────────────────────────────────────────────────────

    public void confirmReport(String reportId) {
        repository.confirmReport(reportId, new ReportRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                // LiveData azhurohet automatikisht nga real-time listener
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    public LiveData<Report> getReportById(String reportId) {
        MutableLiveData<Report> reportLiveData = new MutableLiveData<>();
        repository.getReportById(reportId, reportLiveData);
        return reportLiveData;
    }

    // ─── Reset i statusit pas dërgimit ────────────────────────────────────────

    public void resetSubmittedStatus() {
        reportSubmittedLiveData.setValue(false);
    }

    // ─── Pastro listener kur ViewModel shkatërrohet ───────────────────────────

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }

    // Shto këto fusha në krye të klasës
    private final NotificationRepository notificationRepository =
            NotificationRepository.getInstance();

// Shto këto metoda

    public void approveReport(Report report) {
        // 1 — Ndrysho statusin
        repository.updateReportStatus(report.getId(), "I aprovuar",
                new ReportRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        // 2 — Dërgo notifikim
                        Notification notification = new Notification(
                                report.getUserId(),
                                report.getId(),
                                report.getCategory(),
                                Notification.TYPE_APPROVED,
                                "Raporti juaj për '" + report.getCategory()
                                        + "' u aprovua nga admini. Faleminderit!"
                        );
                        notificationRepository.sendNotification(
                                notification, new ReportRepository.ActionCallback() {
                                    @Override public void onSuccess() {}
                                    @Override public void onFailure(String e) {}
                                });
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        errorLiveData.postValue(errorMessage);
                    }
                });
    }

    public void rejectReport(Report report, String reason) {
        // 1 — Ndrysho statusin
        repository.updateReportStatus(report.getId(), "I refuzuar",
                new ReportRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        // 2 — Dërgo notifikim
                        String message = "Raporti juaj për '"
                                + report.getCategory() + "' u refuzua."
                                + (reason != null && !reason.isEmpty()
                                ? " Arsyeja: " + reason : "");
                        Notification notification = new Notification(
                                report.getUserId(),
                                report.getId(),
                                report.getCategory(),
                                Notification.TYPE_REJECTED,
                                message
                        );
                        notificationRepository.sendNotification(
                                notification, new ReportRepository.ActionCallback() {
                                    @Override public void onSuccess() {}
                                    @Override public void onFailure(String e) {}
                                });
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        errorLiveData.postValue(errorMessage);
                    }
                });
    }

    private final MutableLiveData<List<SentReport>> sentReportsLiveData
            = new MutableLiveData<>();

    public LiveData<List<SentReport>> getSentReports(String adminId) {
        repository.getSentReports(adminId, sentReportsLiveData);
        return sentReportsLiveData;
    }

    public void saveSentReport(SentReport sentReport) {
        repository.saveSentReport(sentReport,
                new ReportRepository.ActionCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(String e) {
                        errorLiveData.postValue(e);
                    }
                });
    }
}