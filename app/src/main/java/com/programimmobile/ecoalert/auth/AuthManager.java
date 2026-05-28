package com.programimmobile.ecoalert.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;


import com.programimmobile.ecoalert.R;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.EmailAuthProvider;

public class AuthManager {

    public static final int RC_GOOGLE_SIGN_IN = 9001;

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public interface LinkCallback {
        void onSuccess(FirebaseUser user, boolean wasAnonymous);
        void onFailure(String errorMessage);
    }

    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;

    public AuthManager(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();

        // Konfiguro Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    // ─── Kontrollo nëse përdoruesi është i loguar ───────────────────────────

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public boolean isAnonymous() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.isAnonymous();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ─── Hyrje Anonime ───────────────────────────────────────────────────────

    public void signInAnonymously(AuthCallback callback) {
        firebaseAuth.signInAnonymously()
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Regjistrim me Email ─────────────────────────────────────────────────

    public void registerWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Hyrje me Email ──────────────────────────────────────────────────────

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Google Sign-In ──────────────────────────────────────────────────────

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleGoogleSignInResult(Intent data, AuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            if (isAnonymous()) {
                // Upgrade llogari anonime → Google
                linkAnonymousWithCredential(credential, callback);
            } else {
                firebaseAuth.signInWithCredential(credential)
                        .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }
        } catch (ApiException e) {
            callback.onFailure("Google Sign-In dështoi: " + e.getMessage());
        }
    }

    // ─── Upgrade: Anonim → Llogari e plotë ──────────────────────────────────
    // Raportet ekzistuese ruhen — userId mbetet i njëjtë

    public void linkAnonymousWithEmail(String email, String password, LinkCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || !user.isAnonymous()) {
            callback.onFailure("Nuk ka përdorues anonim aktiv.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.linkWithCredential(credential)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser(), true))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void linkAnonymousWithGoogle(Intent data, LinkCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            linkAnonymousWithCredential(credential, new AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    callback.onSuccess(user, true);
                }
                @Override
                public void onFailure(String errorMessage) {
                    callback.onFailure(errorMessage);
                }
            });
        } catch (ApiException e) {
            callback.onFailure("Google Sign-In dështoi: " + e.getMessage());
        }
    }

    private void linkAnonymousWithCredential(AuthCredential credential, AuthCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("Nuk ka përdorues aktiv.");
            return;
        }
        user.linkWithCredential(credential)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Dalje ───────────────────────────────────────────────────────────────

    public void signOut(Activity activity) {
        firebaseAuth.signOut();
        googleSignInClient.signOut();
    }

    // ─── Reset fjalëkalimi ───────────────────────────────────────────────────

    public void resetPassword(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}