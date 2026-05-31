package com.programimmobile.ecoalert.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREF_NAME = "ecoalert_prefs";
    private static final String KEY_AUTH_COMPLETED = "auth_choice_made";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setAuthCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_AUTH_COMPLETED, completed).apply();
    }

    public boolean isAuthCompleted() {
        return prefs.getBoolean(KEY_AUTH_COMPLETED, false);
    }
}