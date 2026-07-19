package com.example.a7506_project.data;

import android.content.Context;

public final class RepositoryProvider {
    private static volatile MarketRepository instance;

    private RepositoryProvider() {
    }

    public static MarketRepository get(Context context) {
        if (instance == null) {
            synchronized (RepositoryProvider.class) {
                if (instance == null) {
                    DatabaseHelper dbHelper = new DatabaseHelper(context.getApplicationContext());
                    instance = new MarketRepositoryImpl(dbHelper);
                }
            }
        }
        return instance;
    }
}
