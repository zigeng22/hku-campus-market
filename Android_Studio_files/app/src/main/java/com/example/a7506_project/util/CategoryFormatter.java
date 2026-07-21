package com.example.a7506_project.util;

import android.content.Context;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;

public final class CategoryFormatter {
    private CategoryFormatter() {
    }

    public static String displayName(Context context, String category) {
        if (AppContract.CATEGORY_BOOKS.equals(category)) {
            return context.getString(R.string.category_books);
        }
        if (AppContract.CATEGORY_ELECTRONICS.equals(category)) {
            return context.getString(R.string.category_electronics);
        }
        if (AppContract.CATEGORY_FURNITURE.equals(category)) {
            return context.getString(R.string.category_furniture);
        }
        if (AppContract.CATEGORY_DAILY_GOODS.equals(category)) {
            return context.getString(R.string.category_daily_goods);
        }
        return context.getString(R.string.category_others);
    }
}
