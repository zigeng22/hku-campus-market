package com.example.a7506_project.ui.management;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.google.android.material.appbar.MaterialToolbar;

public class ManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbarManagement);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerManagement);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
