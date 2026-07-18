package com.example.a7506_project.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.ui.item.PostEditItemActivity;
import com.example.a7506_project.ui.management.ManagementActivity;
import com.google.android.material.textfield.TextInputEditText;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        RecyclerView recyclerView = findViewById(R.id.recyclerItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextInputEditText searchInput = findViewById(R.id.inputSearch);
        findViewById(R.id.buttonClearSearch).setOnClickListener(view -> searchInput.setText(""));
        findViewById(R.id.fabPostItem).setOnClickListener(view ->
                startActivity(new Intent(this, PostEditItemActivity.class)));
        findViewById(R.id.fabManagement).setOnClickListener(view ->
                startActivity(new Intent(this, ManagementActivity.class)));
    }
}
