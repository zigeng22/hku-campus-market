package com.example.a7506_project.ui.item;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.ui.management.OfferReviewActivity;

public class ItemDetailActivity extends AppCompatActivity {
    private long itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        itemId = getIntent().getLongExtra(AppContract.EXTRA_ITEM_ID, AppContract.INVALID_ID);

        findViewById(R.id.buttonMakeOffer).setOnClickListener(view -> showOfferDialog());
        findViewById(R.id.buttonBuyNow).setOnClickListener(view -> showPreviewMessage());
        findViewById(R.id.buttonDeleteItem).setOnClickListener(view -> showPreviewMessage());
        findViewById(R.id.buttonEditItem).setOnClickListener(view -> openEditItem());
        findViewById(R.id.buttonViewOffers).setOnClickListener(view -> openOffers());
    }

    private void showOfferDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_make_offer, null);
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_make_offer)
                .setView(content)
                .setPositiveButton(R.string.action_make_offer, (dialog, which) -> showPreviewMessage())
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

    private void showPreviewMessage() {
        Toast.makeText(this, R.string.phase_zero_preview, Toast.LENGTH_SHORT).show();
    }
}
