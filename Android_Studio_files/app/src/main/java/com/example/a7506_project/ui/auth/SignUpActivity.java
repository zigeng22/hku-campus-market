package com.example.a7506_project.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;
import com.example.a7506_project.util.SessionManager;
import com.example.a7506_project.ui.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.content.Intent;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText inputNickname;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;
    private TextInputEditText inputWhatsapp;
    private MaterialButton buttonSignUp;
    private View progressSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputNickname = findViewById(R.id.inputNickname);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        inputWhatsapp = findViewById(R.id.inputWhatsapp);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        progressSignUp = findViewById(R.id.progressSignUp);

        findViewById(R.id.buttonBackToLogin).setOnClickListener(view -> finish());
        buttonSignUp.setOnClickListener(view -> performSignUp());
    }

    private void performSignUp() {
        String nickname = inputNickname.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();
        String whatsapp = inputWhatsapp.getText().toString().trim();

        // client-side validation
        if (nickname.isEmpty()) {
            inputNickname.setError("Nickname is required");
            return;
        }
        if (nickname.length() < 3) {
            inputNickname.setError("At least 3 characters");
            return;
        }
        if (!nickname.matches("[a-zA-Z0-9_]+")) {
            inputNickname.setError("Only letters, numbers, underscores");
            return;
        }
        if (password.isEmpty()) {
            inputPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("At least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            inputConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (whatsapp.isEmpty()) {
            inputWhatsapp.setError("WhatsApp number is required");
            return;
        }
        String digits = whatsapp.replaceAll("[\\s\\-()+]", "");
        if (digits.length() < 8 || digits.length() > 15) {
            inputWhatsapp.setError("8–15 digits");
            return;
        }

        setLoading(true);

        MarketRepository repo = RepositoryProvider.get(this);
        RegistrationResult result = repo.registerUser(nickname, password, whatsapp);

        setLoading(false);

        if (result.isSuccess()) {
            SessionManager session = new SessionManager(this);
            session.login(result.getUserId());
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            String msg;
            if (result.getCode() == RepositoryResultCode.DUPLICATE_NICKNAME) {
                msg = "This nickname is already taken.";
            } else if (result.getCode() == RepositoryResultCode.INVALID_INPUT) {
                msg = "Please check your input and try again.";
            } else {
                msg = "Registration failed. Please try again.";
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        buttonSignUp.setEnabled(!loading);
        if (progressSignUp != null) {
            progressSignUp.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}
