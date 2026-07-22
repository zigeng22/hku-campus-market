package com.example.a7506_project.ui.management;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.a7506_project.model.ParticipationSummary;
import com.example.a7506_project.ui.item.ItemDetailActivity;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class ManagementActivity extends AppCompatActivity {

    private MarketRepository repo;
    private long currentUserId;
    private RecyclerView recycler;
    private TextView textEmpty;
    private TabLayout tabLayout;

    private ListingAdapter listingAdapter;
    private ParticipationAdapter participationAdapter;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        repo = RepositoryProvider.get(this);
        currentUserId = new SessionManager(this).getCurrentUserId();

        MaterialToolbar toolbar = findViewById(R.id.toolbarManagement);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        textEmpty = findViewById(R.id.textEmptyManagement);

        recycler = findViewById(R.id.recyclerManagement);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        listingAdapter = new ListingAdapter();
        listingAdapter.setOnListingClickListener(this::openItemDetail);
        participationAdapter = new ParticipationAdapter();

        tabLayout = findViewById(R.id.tabManagement);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadTabContent();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loadTabContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTabContent();
    }

    private void loadTabContent() {
        List<ItemCard> listings = repo.getListingsBySeller(currentUserId);
        updatePendingOfferBadge(listings);
        if (currentTab == 0) {
            recycler.setAdapter(listingAdapter);
            listingAdapter.setItems(listings);
            textEmpty.setText(R.string.empty_my_listings);
            textEmpty.setVisibility(listings.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            List<ParticipationSummary> activities = repo.getBuyerActivity(currentUserId);
            recycler.setAdapter(participationAdapter);
            participationAdapter.setItems(activities);
            textEmpty.setText(R.string.empty_my_activity);
            textEmpty.setVisibility(activities.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updatePendingOfferBadge(List<ItemCard> listings) {
        int pendingOffers = 0;
        for (ItemCard listing : listings) {
            pendingOffers += listing.getOfferCount();
        }
        TabLayout.Tab listingsTab = tabLayout.getTabAt(0);
        if (listingsTab == null) return;
        if (pendingOffers > 0) {
            listingsTab.getOrCreateBadge().setNumber(pendingOffers);
        } else {
            listingsTab.removeBadge();
        }
    }

    private void openItemDetail(ItemCard item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(AppContract.EXTRA_ITEM_ID, item.getItemId());
        startActivity(intent);
    }
}
