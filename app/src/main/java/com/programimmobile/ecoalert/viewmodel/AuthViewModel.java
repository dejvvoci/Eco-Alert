package com.programimmobile.ecoalert.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.programimmobile.ecoalert.auth.AuthManager;

public class AuthViewModel extends AndroidViewModel {

    private final AuthManager authManager;

    private final MutableLiveData<FirebaseUser> currentUserLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authManager = new AuthManager(application);
        this.currentUserLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();
        this.loadingLiveData = new MutableLiveData<>(false);

        // Nëse ka përdorues aktiv, vendose menjëherë
        if (authManager.isLoggedIn()) {
            currentUserLiveData.setValue(authManager.getCurrentUser());
        }
    }

    // ─── Getters për LiveData ─────────────────────────────────────────────────

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUserLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    // ─── Kontrolle statusi ────────────────────────────────────────────────────

    public boolean isLoggedIn() {
        return authManager.isLoggedIn();
    }

    public boolean isAnonymous() {
        return authManager.isAnonymous();
    }

    public String getCurrentUserId() {
        return authManager.getCurrentUserId();
    }

    // ─── Hyrje Anonime ────────────────────────────────────────────────────────

    public void signInAnonymously() {
        loadingLiveData.setValue(true);
        authManager.signInAnonymously(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                loadingLiveData.postValue(false);
                currentUserLiveData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Regjistrim me Email ──────────────────────────────────────────────────

    public void registerWithEmail(String email, String password) {
        if (!validateEmailPassword(email, password)) return;

        loadingLiveData.setValue(true);
        authManager.registerWithEmail(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                loadingLiveData.postValue(false);
                currentUserLiveData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Hyrje me Email ───────────────────────────────────────────────────────

    public void signInWithEmail(String email, String password) {
        if (!validateEmailPassword(email, password)) return;

        loadingLiveData.setValue(true);
        authManager.signInWithEmail(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                loadingLiveData.postValue(false);
                currentUserLiveData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Google Sign-In ───────────────────────────────────────────────────────

    public Intent getGoogleSignInIntent() {
        return authManager.getGoogleSignInIntent();
    }

    public void handleGoogleSignInResult(Intent data) {
        loadingLiveData.setValue(true);
        authManager.handleGoogleSignInResult(data, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                loadingLiveData.postValue(false);
                currentUserLiveData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Upgrade: Anonim → Llogari e plotë ───────────────────────────────────

    public void linkWithEmail(String email, String password) {
        if (!validateEmailPassword(email, password)) return;

        loadingLiveData.setValue(true);
        authManager.linkAnonymousWithEmail(email, password,
                new AuthManager.LinkCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user, boolean wasAnonymous) {
                        loadingLiveData.postValue(false);
                        currentUserLiveData.postValue(user);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingLiveData.postValue(false);
                        errorLiveData.postValue(errorMessage);
                    }
                });
    }

    // ─── Dalje ───────────────────────────────────────────────────────────────

    public void signOut(android.app.Activity activity) {
        authManager.signOut(activity);
        currentUserLiveData.setValue(null);
    }

    // ─── Reset fjalëkalimi ────────────────────────────────────────────────────

    public void resetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            errorLiveData.setValue("Shkruaj email-in për reset.");
            return;
        }
        authManager.resetPassword(email, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                errorLiveData.postValue("Email-i i reset-it u dërgua!");
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    // ─── Validim ──────────────────────────────────────────────────────────────

    private boolean validateEmailPassword(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorLiveData.setValue("Email-i nuk mund të jetë bosh.");
            return false;
        }
        if (password == null || password.length() < 6) {
            errorLiveData.setValue("Fjalëkalimi duhet të ketë të paktën 6 karaktere.");
            return false;
        }
        return true;
    }
}