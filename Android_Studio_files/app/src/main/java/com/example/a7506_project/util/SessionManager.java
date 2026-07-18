package com.example.a7506_project.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.a7506_project.contract.AppContract;

public final class SessionManager {
    private static final String PREFERENCES_NAME = "market_session";
    private static final String KEY_USER_ID = "current_user_id";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void login(long userId) {
        preferences.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public void logout() {
        preferences.edit().remove(KEY_USER_ID).apply();
    }

    public boolean isLoggedIn() {
        return getCurrentUserId() != AppContract.INVALID_ID;
    }

    public long getCurrentUserId() {
        return preferences.getLong(KEY_USER_ID, AppContract.INVALID_ID);
    }
}
