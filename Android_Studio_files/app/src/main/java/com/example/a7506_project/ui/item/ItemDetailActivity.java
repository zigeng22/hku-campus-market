package com.example.a7506_project.ui.item;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.ui.management.OfferReviewActivity;
import com.example.a7506_project.util.CategoryFormatter;
import com.example.a7506_project.util.ImageUriLoader;
import com.example.a7506_project.util.MoneyFormatter;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class ItemDetailActivity extends AppCompatActivity {

    private MarketRepository repo;
    private SessionManager session;
    private long itemId;
    private Item item;
    private long currentUserId;

    private ImageView imageItem;
    private TextView textItemName, textItemPrice, textItemCategory, textItemDescription, textSellerName,
            textItemStatus;
    private View groupBuyerActions, groupSellerActions, footerItemActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        repo = RepositoryProvider.get(this);
        session = new SessionManager(this);
        currentUserId = session.getCurrentUserId();
        itemId = getIntent().getLongExtra(AppContract.EXTRA_ITEM_ID, AppContract.INVALID_ID);

        MaterialToolbar toolbar = findViewById(R.id.toolbarItemDetail);
        toolbar.setTitle(R.string.item_detail_title);
        toolbar.setNavigationOnClickListener(view -> finish());

        imageItem = findViewById(R.id.imageItem);
        textItemName = findViewById(R.id.textItemName);
        textItemPrice = findViewById(R.id.textItemPrice);
        textItemCategory = findViewById(R.id.textItemCategory);
        textItemDescription = findViewById(R.id.textItemDescription);
        textSellerName = findViewById(R.id.textSellerName);
        textItemStatus = findViewById(R.id.textItemStatus);
        groupBuyerActions = findViewById(R.id.groupBuyerActions);
        groupSellerActions = findViewById(R.id.groupSellerActions);
        footerItemActions = findViewById(R.id.footerItemActions);

        findViewById(R.id.buttonMakeOffer).setOnClickListener(v -> showOfferDialog(AppContract.OFFER_TYPE_NEGOTIATED));
        findViewById(R.id.buttonBuyNow).setOnClickListener(v -> showOfferDialog(AppContract.OFFER_TYPE_BUY_NOW));
        findViewById(R.id.buttonEditItem).setOnClickListener(v -> openEditItem());
        findViewById(R.id.buttonDeleteItem).setOnClickListener(v -> deleteItem());
        findViewById(R.id.buttonViewOffers).setOnClickListener(v -> openOffers());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItem();
    }

    private void loadItem() {
        item = repo.getItemById(itemId);
        if (item == null) {
            Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textItemName.setText(item.getName());
        textItemPrice.setText(MoneyFormatter.centsToHkd(item.getPriceCents()));
        textItemCategory.setText(CategoryFormatter.displayName(this, item.getCategory()));
        textItemDescription.setText(item.getDescription().trim().isEmpty()
                ? getString(R.string.description_not_provided)
                : item.getDescription());

        User seller = repo.getUserById(item.getSellerId());
        if (seller != null) {
            textSellerName.setText(getString(R.string.seller_prefix, seller.getNickname()));
        }

        ImageUriLoader.loadOrShowPlaceholder(
                this, imageItem, item.getImageUri(), R.drawable.ic_item_placeholder);

        boolean active = AppContract.ITEM_ACTIVE.equals(item.getStatus());
        textItemStatus.setVisibility(active ? View.GONE : View.VISIBLE);
        footerItemActions.setVisibility(active ? View.VISIBLE : View.GONE);

        if (!active) {
            groupBuyerActions.setVisibility(View.GONE);
            groupSellerActions.setVisibility(View.GONE);
        } else if (item.getSellerId() == currentUserId) {
            groupBuyerActions.setVisibility(View.GONE);
            groupSellerActions.setVisibility(View.VISIBLE);
        } else {
            groupSellerActions.setVisibility(View.GONE);
            groupBuyerActions.setVisibility(View.VISIBLE);
        }
    }

    private void showOfferDialog(String offerType) {
        if (AppContract.OFFER_TYPE_BUY_NOW.equals(offerType)) {
            showBuyNowConfirmation();
            return;
        }

        View content = LayoutInflater.from(this).inflate(R.layout.dialog_make_offer, null);
        TextInputEditText inputAmount = content.findViewById(R.id.inputOfferAmount);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.action_make_offer)
                .setView(content)
                .setPositiveButton(R.string.action_submit_offer, null)
                .setNegativeButton(R.string.action_close, null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String amountText = inputAmount.getText().toString().trim();
                    long amountCents = MoneyFormatter.hkdToCents(amountText);
                    if (amountCents <= 0) {
                        inputAmount.setError(getString(R.string.offer_amount_error));
                        return;
                    }
                    inputAmount.setError(null);
                    if (submitOffer(amountCents, AppContract.OFFER_TYPE_NEGOTIATED)) {
                        dialog.dismiss();
                    }
                }));
        dialog.show();
    }

    private void showBuyNowConfirmation() {
        String formattedPrice = MoneyFormatter.centsToHkd(item.getPriceCents());
        new AlertDialog.Builder(this)
                .setTitle(R.string.buy_now_confirmation_title)
                .setMessage(getString(R.string.buy_now_confirmation_message, formattedPrice))
                .setPositiveButton(R.string.action_buy_now, (dialog, which) ->
                        submitOffer(item.getPriceCents(), AppContract.OFFER_TYPE_BUY_NOW))
                .setNegativeButton(R.string.action_close, null)
                .show();
    }

    private boolean submitOffer(long amountCents, String offerType) {
        PlaceOfferResult result = repo.placeOffer(itemId, currentUserId, amountCents, offerType);
        if (result.isSuccess()) {
            int message = AppContract.OFFER_TYPE_BUY_NOW.equals(offerType)
                    ? R.string.buy_now_submitted : R.string.offer_submitted;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadItem();
            return true;
        }

        int errorMessage;
        switch (result.getCode()) {
            case CANNOT_OFFER_OWN_ITEM:
                errorMessage = R.string.offer_error_own_item;
                break;
            case DUPLICATE_PENDING_OFFER:
                errorMessage = R.string.offer_error_duplicate;
                break;
            case ITEM_NOT_ACTIVE:
                errorMessage = R.string.offer_error_inactive;
                break;
            case INVALID_PRICE:
                errorMessage = R.string.offer_amount_error;
                break;
            default:
                errorMessage = R.string.offer_error_generic;
                break;
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void deleteItem() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item_title)
                .setMessage(R.string.delete_item_message)
                .setPositiveButton(R.string.delete_item_confirm, (dialog, which) -> {
                    boolean ok = repo.softDeleteItem(itemId, currentUserId);
                    if (ok) {
                        Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, R.string.item_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_close, null)
                .show();
    }

    private void openEditItem() {
        Intent intent = new Intent(this, PostEditItemActivity.class);
        intent.putExtra(AppContract.EXTRA_ITEM_ID, itemId);
        intent.putExtra(AppContract.EXTRA_EDIT_MODE, true);
        startActivity(intent);
    }

    private void openOffers() {
        Intent intent = new Intent(this, OfferReviewActivity.class);
        intent.putExtra(AppContract.EXTRA_ITEM_ID, itemId);
        startActivity(intent);
    }
}
