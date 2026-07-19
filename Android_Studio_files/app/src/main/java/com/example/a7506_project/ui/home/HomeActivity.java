package com.example.a7506_project.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.SortOrder;
import com.example.a7506_project.model.User;
import com.example.a7506_project.ui.auth.LoginActivity;
import com.example.a7506_project.ui.item.ItemDetailActivity;
import com.example.a7506_project.ui.item.PostEditItemActivity;
import com.example.a7506_project.ui.management.ManagementActivity;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MarketRepository repo;
    private SessionManager session;
    private ItemAdapter adapter;
    private TextInputEditText searchInput;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        repo = RepositoryProvider.get(this);
        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarHome);
        User user = repo.getUserById(session.getCurrentUserId());
        if (user != null) {
            toolbar.setTitle(user.getNickname());
        }
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                session.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter();
        adapter.setOnItemClickListener(this::openItemDetail);
        recyclerView.setAdapter(adapter);

        textEmpty = findViewById(R.id.textEmptyItems);

        searchInput = findViewById(R.id.inputSearch);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadItems();
            }
        });

        findViewById(R.id.buttonClearSearch).setOnClickListener(view -> searchInput.setText(""));
        findViewById(R.id.fabPostItem).setOnClickListener(view ->
                startActivity(new Intent(this, PostEditItemActivity.class)));
        findViewById(R.id.fabManagement).setOnClickListener(view ->
                startActivity(new Intent(this, ManagementActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        String keyword = searchInput.getText().toString().trim();
        List<ItemCard> items = repo.searchActiveItems(keyword, AppContract.CATEGORY_ALL, SortOrder.NEWEST);
        adapter.setItems(items);
        textEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openItemDetail(ItemCard item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(AppContract.EXTRA_ITEM_ID, item.getItemId());
        startActivity(intent);
    }
}
