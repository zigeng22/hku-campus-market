package com.example.a7506_project.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.RepositoryResultCode;
import com.example.a7506_project.util.SessionManager;
import com.example.a7506_project.util.Validators;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {
    private MarketRepository repository;
    private long currentUserId;
    private TextInputLayout layoutNickname, layoutWhatsapp, layoutCurrentPassword,
            layoutNewPassword, layoutConfirmPassword;
    private TextInputEditText inputNickname, inputWhatsapp, inputCurrentPassword,
            inputNewPassword, inputConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = RepositoryProvider.get(this);
        currentUserId = new SessionManager(this).getCurrentUserId();

        MaterialToolbar toolbar = findViewById(R.id.toolbarProfile);
        toolbar.setNavigationOnClickListener(view -> finish());

        layoutNickname = findViewById(R.id.layoutProfileNickname);
        layoutWhatsapp = findViewById(R.id.layoutProfileWhatsapp);
        layoutCurrentPassword = findViewById(R.id.layoutCurrentPassword);
        layoutNewPassword = findViewById(R.id.layoutNewPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmNewPassword);
        inputNickname = findViewById(R.id.inputProfileNickname);
        inputWhatsapp = findViewById(R.id.inputProfileWhatsapp);
        inputCurrentPassword = findViewById(R.id.inputCurrentPassword);
        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmNewPassword);

        User user = repository.getUserById(currentUserId);
        if (user == null) {
            finish();
            return;
        }
        inputNickname.setText(user.getNickname());
        inputWhatsapp.setText(user.getWhatsapp());

        findViewById(R.id.buttonSaveProfile).setOnClickListener(view -> saveProfile());
        findViewById(R.id.buttonChangePassword).setOnClickListener(view -> changePassword());
    }

    private void saveProfile() {
        String nickname = text(inputNickname).trim();
        String whatsapp = text(inputWhatsapp).trim();
        layoutNickname.setError(null);
        layoutWhatsapp.setError(null);

        if (nickname.isEmpty()) {
            layoutNickname.setError(getString(R.string.error_nickname_required));
            return;
        }
        if (nickname.length() < 3 || nickname.length() > 20) {
            layoutNickname.setError(getString(R.string.error_nickname_length));
            return;
        }
        if (!nickname.matches("[a-zA-Z0-9_]+")) {
            layoutNickname.setError(getString(R.string.error_nickname_characters));
            return;
        }
        if (whatsapp.isEmpty()) {
            layoutWhatsapp.setError(getString(R.string.error_whatsapp_required));
            return;
        }
        if (Validators.validateWhatsapp(whatsapp) != null) {
            layoutWhatsapp.setError(getString(R.string.error_whatsapp_format));
            return;
        }

        RepositoryResultCode result = repository.updateUserProfile(
                currentUserId, nickname, whatsapp);
        if (result == RepositoryResultCode.OK) {
            Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
        } else if (result == RepositoryResultCode.DUPLICATE_NICKNAME) {
            layoutNickname.setError(getString(R.string.error_duplicate_nickname));
        } else {
            Toast.makeText(this, R.string.profile_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        String currentPassword = text(inputCurrentPassword);
        String newPassword = text(inputNewPassword);
        String confirmation = text(inputConfirmPassword);
        layoutCurrentPassword.setError(null);
        layoutNewPassword.setError(null);
        layoutConfirmPassword.setError(null);

        if (currentPassword.isEmpty()) {
            layoutCurrentPassword.setError(getString(R.string.error_password_required));
            return;
        }
        if (Validators.validatePassword(newPassword) != null) {
            layoutNewPassword.setError(getString(R.string.error_password_length));
            return;
        }
        if (!newPassword.equals(confirmation)) {
            layoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            return;
        }

        RepositoryResultCode result = repository.changePassword(
                currentUserId, currentPassword, newPassword);
        if (result == RepositoryResultCode.OK) {
            inputCurrentPassword.setText("");
            inputNewPassword.setText("");
            inputConfirmPassword.setText("");
            Toast.makeText(this, R.string.password_changed, Toast.LENGTH_SHORT).show();
        } else if (result == RepositoryResultCode.INVALID_CREDENTIALS) {
            layoutCurrentPassword.setError(getString(R.string.current_password_incorrect));
        } else {
            Toast.makeText(this, R.string.password_change_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
