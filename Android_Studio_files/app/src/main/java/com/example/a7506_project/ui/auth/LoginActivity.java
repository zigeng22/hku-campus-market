package com.example.a7506_project.ui.auth;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.a7506_project.R;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.User;
import com.example.a7506_project.ui.home.HomeActivity;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputNickname;
    private TextInputEditText inputPassword;
    private TextInputLayout layoutNickname;
    private TextInputLayout layoutPassword;
    private ScrollView rootLogin;
    private int imeInsetBottom;
    private MaterialButton buttonLogin;
    private View progressLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rootLogin = findViewById(R.id.rootLogin);
        applyWindowInsets(rootLogin);

        inputNickname = findViewById(R.id.inputNickname);
        inputPassword = findViewById(R.id.inputPassword);
        layoutNickname = findViewById(R.id.layoutNickname);
        layoutPassword = findViewById(R.id.layoutPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressLogin = findViewById(R.id.progressLogin);

        findViewById(R.id.buttonOpenSignUp).setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class)));

        buttonLogin.setOnClickListener(view -> performLogin());
        inputNickname.setOnFocusChangeListener(this::onInputFocusChanged);
        inputPassword.setOnFocusChangeListener(this::onInputFocusChanged);
        inputPassword.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });
    }

    private void performLogin() {
        String nickname = inputNickname.getText().toString().trim();
        String password = inputPassword.getText().toString();

        layoutNickname.setError(null);
        layoutPassword.setError(null);

        if (nickname.isEmpty()) {
            layoutNickname.setError("Nickname is required");
            inputNickname.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            layoutPassword.setError("Password is required");
            inputPassword.requestFocus();
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

    private void applyWindowInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            imeInsetBottom = ime.bottom;
            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            View focused = view.findFocus();
            if (focused != null) {
                focused.postDelayed(() -> scrollFocusedFieldIntoView(focused), 100);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void onInputFocusChanged(View view, boolean hasFocus) {
        if (hasFocus) {
            view.postDelayed(() -> scrollFocusedFieldIntoView(view), 250);
        }
    }

    private void scrollFocusedFieldIntoView(View field) {
        if (imeInsetBottom == 0 || !field.hasFocus()) {
            return;
        }
        Rect bounds = new Rect();
        field.getDrawingRect(bounds);
        rootLogin.offsetDescendantRectToMyCoords(field, bounds);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        int visibleBottom = rootLogin.getHeight() - imeInsetBottom - margin;
        if (bounds.bottom > visibleBottom) {
            rootLogin.smoothScrollBy(0, bounds.bottom - visibleBottom);
        }
    }
}
