package com.example.a7506_project.ui.management;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class OfferReviewActivity extends AppCompatActivity {

    private MarketRepository repo;
    private long itemId;
    private long currentUserId;
    private OfferAdapter adapter;
    private TextView textEmpty;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_review);

        repo = RepositoryProvider.get(this);
        currentUserId = new SessionManager(this).getCurrentUserId();
        itemId = getIntent().getLongExtra(AppContract.EXTRA_ITEM_ID, AppContract.INVALID_ID);

        MaterialToolbar toolbar = findViewById(R.id.toolbarOffers);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        textEmpty = findViewById(R.id.textEmptyOffers);

        recycler = findViewById(R.id.recyclerOffers);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfferAdapter();
        adapter.setOnAcceptListener(this::onAcceptOffer);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOffers();
    }

    private void loadOffers() {
        List<OfferSummary> offers = repo.getOffersForSellerItem(itemId, currentUserId);
        adapter.setOffers(offers);
        textEmpty.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onAcceptOffer(OfferSummary offer) {
        new AlertDialog.Builder(this)
                .setTitle("Accept offer")
                .setMessage("Accept this offer from " + offer.getBuyerNickname() + "? This will mark the item as sold and reject all other offers.")
                .setPositiveButton("Accept", (dialog, which) -> {
                    AcceptOfferResult result = repo.acceptOffer(offer.getOfferId(), currentUserId);
                    if (result.isSuccess()) {
                        Toast.makeText(this, "Offer accepted! Deal confirmed.", Toast.LENGTH_SHORT).show();
                        loadOffers();
                    } else {
                        String msg;
                        switch (result.getCode()) {
                            case OFFER_NOT_FOUND:
                                msg = "Offer not found.";
                                break;
                            case OFFER_NOT_PENDING:
                                msg = "This offer is no longer pending.";
                                break;
                            case ITEM_NOT_ACTIVE:
                                msg = "This item is no longer active.";
                                break;
                            case NOT_OWNER:
                                msg = "Only the item owner can accept offers.";
                                break;
                            default:
                                msg = "Failed to accept offer.";
                                break;
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_close, null)
                .show();
    }
}
