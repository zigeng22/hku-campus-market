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
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.model.result.RepositoryResultCode;
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

        Item item = repo.getItemById(itemId);
        if (item == null || item.getSellerId() != currentUserId) {
            Toast.makeText(this, R.string.offer_review_not_owner, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarOffers);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        textEmpty = findViewById(R.id.textEmptyOffers);

        recycler = findViewById(R.id.recyclerOffers);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfferAdapter();
        adapter.setOnAcceptListener(this::onAcceptOffer);
        adapter.setOnRejectListener(this::onRejectOffer);
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
                .setTitle(R.string.accept_offer_title)
                .setMessage(getString(R.string.accept_offer_message, offer.getBuyerNickname()))
                .setPositiveButton(R.string.action_accept_offer, (dialog, which) -> {
                    AcceptOfferResult result = repo.acceptOffer(offer.getOfferId(), currentUserId);
                    if (result.isSuccess()) {
                        Toast.makeText(this, R.string.offer_accepted, Toast.LENGTH_SHORT).show();
                        loadOffers();
                    } else {
                        int message;
                        switch (result.getCode()) {
                            case OFFER_NOT_FOUND:
                                message = R.string.accept_error_not_found;
                                break;
                            case OFFER_NOT_PENDING:
                                message = R.string.accept_error_not_pending;
                                break;
                            case ITEM_NOT_ACTIVE:
                                message = R.string.offer_error_inactive;
                                break;
                            case NOT_OWNER:
                                message = R.string.offer_review_not_owner;
                                break;
                            default:
                                message = R.string.accept_error_generic;
                                break;
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_close, null)
                .show();
    }

    private void onRejectOffer(OfferSummary offer) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reject_offer_title)
                .setMessage(getString(R.string.reject_offer_message, offer.getBuyerNickname()))
                .setPositiveButton(R.string.action_reject_offer, (dialog, which) -> {
                    RepositoryResultCode result = repo.rejectOffer(
                            offer.getOfferId(), currentUserId);
                    if (result == RepositoryResultCode.OK) {
                        Toast.makeText(this, R.string.offer_rejected, Toast.LENGTH_SHORT).show();
                        loadOffers();
                    } else {
                        int message = result == RepositoryResultCode.OFFER_NOT_PENDING
                                ? R.string.accept_error_not_pending
                                : R.string.reject_error_generic;
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_close, null)
                .show();
    }
}
