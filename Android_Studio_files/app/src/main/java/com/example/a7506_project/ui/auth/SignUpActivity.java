package com.example.a7506_project.ui.auth;

import android.os.Bundle;
import android.graphics.Rect;
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
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;
import com.example.a7506_project.util.SessionManager;
import com.example.a7506_project.ui.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.content.Intent;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText inputNickname;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;
    private TextInputEditText inputWhatsapp;
    private TextInputLayout layoutNickname;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;
    private TextInputLayout layoutWhatsapp;
    private ScrollView rootSignUp;
    private int imeInsetBottom;
    private MaterialButton buttonSignUp;
    private View progressSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        rootSignUp = findViewById(R.id.rootSignUp);
        applyWindowInsets(rootSignUp);

        inputNickname = findViewById(R.id.inputNickname);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        inputWhatsapp = findViewById(R.id.inputWhatsapp);
        layoutNickname = findViewById(R.id.layoutNickname);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        layoutWhatsapp = findViewById(R.id.layoutWhatsapp);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        progressSignUp = findViewById(R.id.progressSignUp);

        findViewById(R.id.buttonBackToLogin).setOnClickListener(view -> finish());
        buttonSignUp.setOnClickListener(view -> performSignUp());
        inputNickname.setOnFocusChangeListener(this::onInputFocusChanged);
        inputPassword.setOnFocusChangeListener(this::onInputFocusChanged);
        inputConfirmPassword.setOnFocusChangeListener(this::onInputFocusChanged);
        inputWhatsapp.setOnFocusChangeListener(this::onInputFocusChanged);
        inputWhatsapp.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSignUp();
                return true;
            }
            return false;
        });
    }

    private void performSignUp() {
        String nickname = inputNickname.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();
        String whatsapp = inputWhatsapp.getText().toString().trim();

        layoutNickname.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        layoutWhatsapp.setError(null);

        // client-side validation
        if (nickname.isEmpty()) {
            layoutNickname.setError("Nickname is required");
            inputNickname.requestFocus();
            return;
        }
        if (nickname.length() < 3) {
            layoutNickname.setError("At least 3 characters");
            inputNickname.requestFocus();
            return;
        }
        if (!nickname.matches("[a-zA-Z0-9_]+")) {
            layoutNickname.setError("Only letters, numbers, underscores");
            inputNickname.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            layoutPassword.setError("Password is required");
            inputPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            layoutPassword.setError("At least 6 characters");
            inputPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError("Passwords do not match");
            inputConfirmPassword.requestFocus();
            return;
        }
        if (whatsapp.isEmpty()) {
            layoutWhatsapp.setError("WhatsApp number is required");
            inputWhatsapp.requestFocus();
            return;
        }
        String digits = whatsapp.replaceAll("[\\s\\-()+]", "");
        if (digits.length() < 8 || digits.length() > 15) {
            layoutWhatsapp.setError("8-15 digits");
            inputWhatsapp.requestFocus();
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
        rootSignUp.offsetDescendantRectToMyCoords(field, bounds);
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        int visibleBottom = rootSignUp.getHeight() - imeInsetBottom - margin;
        if (bounds.bottom > visibleBottom) {
            rootSignUp.smoothScrollBy(0, bounds.bottom - visibleBottom);
        }
    }
}
