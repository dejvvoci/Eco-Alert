package com.programimmobile.ecoalert.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.programimmobile.ecoalert.model.Report;
import com.programimmobile.ecoalert.repository.ReportRepository;

import java.util.List;

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
}