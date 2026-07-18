package com.example.a7506_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.ui.home.HomeActivity;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findViewById(R.id.buttonBackToLogin).setOnClickListener(view -> finish());
        findViewById(R.id.buttonSignUp).setOnClickListener(view -> openNavigationPreview());
    }

    private void openNavigationPreview() {
        Toast.makeText(this, R.string.phase_zero_preview, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
    }
}
