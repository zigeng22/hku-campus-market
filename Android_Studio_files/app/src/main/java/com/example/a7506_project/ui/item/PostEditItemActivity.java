package com.example.a7506_project.ui.item;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;

public class PostEditItemActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit_item);

        boolean editMode = getIntent().getBooleanExtra(AppContract.EXTRA_EDIT_MODE, false);
        TextView title = findViewById(R.id.textPostEditTitle);
        title.setText(editMode ? R.string.edit_item_title : R.string.post_item_title);

        String[] categories = getResources().getStringArray(R.array.item_categories);
        AutoCompleteTextView categoryDropdown = findViewById(R.id.dropdownCategory);
        categoryDropdown.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories));

        findViewById(R.id.buttonChooseImage).setOnClickListener(view ->
                Toast.makeText(this, R.string.phase_zero_preview, Toast.LENGTH_SHORT).show());
        findViewById(R.id.buttonSaveItem).setOnClickListener(view -> {
            Toast.makeText(this, R.string.phase_zero_preview, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
