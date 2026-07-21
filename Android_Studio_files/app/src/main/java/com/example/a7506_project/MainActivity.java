package com.example.a7506_project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.ui.auth.LoginActivity;
import com.example.a7506_project.ui.home.HomeActivity;
import com.example.a7506_project.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getCurrentUserId();
        MarketRepository repository = RepositoryProvider.get(this);
        boolean hasValidSession = userId != AppContract.INVALID_ID
                && repository.getUserById(userId) != null;
        if (!hasValidSession && userId != AppContract.INVALID_ID) {
            sessionManager.logout();
        }

        Class<?> destination = hasValidSession
                ? HomeActivity.class
                : LoginActivity.class;

        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
