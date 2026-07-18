package com.example.a7506_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.ui.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.buttonOpenSignUp).setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class)));

        findViewById(R.id.buttonLogin).setOnClickListener(view -> openNavigationPreview());
    }

    private void openNavigationPreview() {
        Toast.makeText(this, R.string.phase_zero_preview, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
    }
}
