package com.example.a7506_project.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.User;
import com.example.a7506_project.ui.home.HomeActivity;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputNickname;
    private TextInputEditText inputPassword;
    private MaterialButton buttonLogin;
    private View progressLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputNickname = findViewById(R.id.inputNickname);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressLogin = findViewById(R.id.progressLogin);

        findViewById(R.id.buttonOpenSignUp).setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class)));

        buttonLogin.setOnClickListener(view -> performLogin());
    }

    private void performLogin() {
        String nickname = inputNickname.getText().toString().trim();
        String password = inputPassword.getText().toString();

        if (nickname.isEmpty()) {
            inputNickname.setError("Nickname is required");
            return;
        }
        if (password.isEmpty()) {
            inputPassword.setError("Password is required");
            return;
        }

        setLoading(true);

        MarketRepository repo = RepositoryProvider.get(this);
        User user = repo.authenticate(nickname, password);

        setLoading(false);

        if (user != null) {
            SessionManager session = new SessionManager(this);
            session.login(user.getId());
            navigateToHome();
        } else {
            Toast.makeText(this, "Invalid nickname or password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        buttonLogin.setEnabled(!loading);
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
