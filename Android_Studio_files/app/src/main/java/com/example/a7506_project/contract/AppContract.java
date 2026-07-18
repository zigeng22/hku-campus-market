package com.example.a7506_project.contract;

public final class AppContract {
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";
    public static final long INVALID_ID = -1L;

    public static final String ITEM_ACTIVE = "ACTIVE";
    public static final String ITEM_SOLD = "SOLD";
    public static final String ITEM_DELETED = "DELETED";

    public static final String OFFER_PENDING = "PENDING";
    public static final String OFFER_ACCEPTED = "ACCEPTED";
    public static final String OFFER_REJECTED = "REJECTED";

    public static final String OFFER_TYPE_NEGOTIATED = "NEGOTIATED";
    public static final String OFFER_TYPE_BUY_NOW = "BUY_NOW";

    public static final String CATEGORY_ALL = "ALL";
    public static final String CATEGORY_BOOKS = "BOOKS";
    public static final String CATEGORY_ELECTRONICS = "ELECTRONICS";
    public static final String CATEGORY_FURNITURE = "FURNITURE";
    public static final String CATEGORY_DAILY_GOODS = "DAILY_GOODS";
    public static final String CATEGORY_OTHERS = "OTHERS";

    private AppContract() {
    }
}
