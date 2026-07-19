package com.example.a7506_project.ui.item;

import android.content.Intent;
import android.net.Uri;
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
import com.example.a7506_project.util.MoneyFormatter;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class ItemDetailActivity extends AppCompatActivity {

    private MarketRepository repo;
    private SessionManager session;
    private long itemId;
    private Item item;
    private long currentUserId;

    private ImageView imageItem;
    private TextView textItemName, textItemPrice, textItemCategory, textItemDescription, textSellerName;
    private View groupBuyerActions, groupSellerActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        repo = RepositoryProvider.get(this);
        session = new SessionManager(this);
        currentUserId = session.getCurrentUserId();
        itemId = getIntent().getLongExtra(AppContract.EXTRA_ITEM_ID, AppContract.INVALID_ID);

        imageItem = findViewById(R.id.imageItem);
        textItemName = findViewById(R.id.textItemName);
        textItemPrice = findViewById(R.id.textItemPrice);
        textItemCategory = findViewById(R.id.textItemCategory);
        textItemDescription = findViewById(R.id.textItemDescription);
        textSellerName = findViewById(R.id.textSellerName);
        groupBuyerActions = findViewById(R.id.groupBuyerActions);
        groupSellerActions = findViewById(R.id.groupSellerActions);

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
            Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textItemName.setText(item.getName());
        textItemPrice.setText(MoneyFormatter.centsToHkd(item.getPriceCents()));
        textItemCategory.setText(item.getCategory());
        textItemDescription.setText(item.getDescription());

        User seller = repo.getUserById(item.getSellerId());
        if (seller != null) {
            textSellerName.setText(getString(R.string.seller_prefix, seller.getNickname()));
        }

        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            imageItem.setImageURI(Uri.parse(item.getImageUri()));
        } else {
            imageItem.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Show different actions based on whether user is seller or buyer
        if (item.getSellerId() == currentUserId) {
            groupBuyerActions.setVisibility(View.GONE);
            groupSellerActions.setVisibility(AppContract.ITEM_ACTIVE.equals(item.getStatus()) ? View.VISIBLE : View.GONE);
        } else {
            groupSellerActions.setVisibility(View.GONE);
            groupBuyerActions.setVisibility(AppContract.ITEM_ACTIVE.equals(item.getStatus()) ? View.VISIBLE : View.GONE);
        }
    }

    private void showOfferDialog(String offerType) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_make_offer, null);
        TextInputEditText inputAmount = content.findViewById(R.id.inputOfferAmount);

        String title = AppContract.OFFER_TYPE_BUY_NOW.equals(offerType)
                ? getString(R.string.action_buy_now) : getString(R.string.action_make_offer);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(content)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String amountText = inputAmount.getText().toString().trim();
                    if (amountText.isEmpty()) {
                        Toast.makeText(this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount;
                    try {
                        amount = Double.parseDouble(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long amountCents = MoneyFormatter.hkdToCents(amount);

                    PlaceOfferResult result = repo.placeOffer(itemId, currentUserId, amountCents, offerType);
                    if (result.isSuccess()) {
                        Toast.makeText(this, "Offer submitted!", Toast.LENGTH_SHORT).show();
                        loadItem();
                    } else {
                        String errMsg;
                        switch (result.getCode()) {
                            case CANNOT_OFFER_OWN_ITEM:
                                errMsg = "You cannot offer on your own item.";
                                break;
                            case DUPLICATE_PENDING_OFFER:
                                errMsg = "You already have a pending offer on this item.";
                                break;
                            case ITEM_NOT_ACTIVE:
                                errMsg = "This item is no longer active.";
                                break;
                            case INVALID_PRICE:
                                errMsg = "Price must be greater than zero.";
                                break;
                            default:
                                errMsg = "Failed to submit offer.";
                                break;
                        }
                        Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_close, null)
                .show();
    }

    private void deleteItem() {
        new AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Delete this item? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean ok = repo.softDeleteItem(itemId, currentUserId);
                    if (ok) {
                        Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show();
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
