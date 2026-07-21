package com.example.a7506_project.util;

import android.content.Context;

import androidx.annotation.ColorRes;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;

public final class TradeDisplayFormatter {

    private TradeDisplayFormatter() {
    }

    public static String statusLabel(Context context, String status) {
        if (AppContract.ITEM_ACTIVE.equals(status)) {
            return context.getString(R.string.status_active);
        }
        if (AppContract.ITEM_SOLD.equals(status)) {
            return context.getString(R.string.status_sold);
        }
        if (AppContract.ITEM_DELETED.equals(status)) {
            return context.getString(R.string.status_deleted);
        }
        if (AppContract.OFFER_PENDING.equals(status)) {
            return context.getString(R.string.status_pending);
        }
        if (AppContract.OFFER_ACCEPTED.equals(status)) {
            return context.getString(R.string.status_accepted);
        }
        if (AppContract.OFFER_REJECTED.equals(status)) {
            return context.getString(R.string.status_rejected);
        }
        return status;
    }

    public static String offerTypeLabel(Context context, String type) {
        if (AppContract.OFFER_TYPE_BUY_NOW.equals(type)) {
            return context.getString(R.string.offer_type_buy_now);
        }
        return context.getString(R.string.offer_type_negotiated);
    }

    @ColorRes
    public static int statusColor(String status) {
        if (AppContract.ITEM_ACTIVE.equals(status)
                || AppContract.OFFER_ACCEPTED.equals(status)) {
            return R.color.status_success;
        }
        if (AppContract.ITEM_DELETED.equals(status)
                || AppContract.OFFER_REJECTED.equals(status)) {
            return R.color.status_error;
        }
        if (AppContract.ITEM_SOLD.equals(status)) {
            return R.color.brand_primary;
        }
        return R.color.brand_gold;
    }
}
