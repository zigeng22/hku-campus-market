package com.example.a7506_project.contract;

import android.provider.BaseColumns;

public final class DatabaseContract {
    public static final String DATABASE_NAME = "hku_campus_market.db";
    public static final int DATABASE_VERSION = 1;

    private DatabaseContract() {
    }

    public static final class Users implements BaseColumns {
        public static final String TABLE = "users";
        public static final String NICKNAME = "nickname";
        public static final String PASSWORD_HASH = "password_hash";
        public static final String PASSWORD_SALT = "password_salt";
        public static final String WHATSAPP = "whatsapp";
        public static final String CREATED_AT = "created_at";

        private Users() {
        }
    }

    public static final class Items implements BaseColumns {
        public static final String TABLE = "items";
        public static final String SELLER_ID = "seller_id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String PRICE_CENTS = "price_cents";
        public static final String IMAGE_URI = "image_uri";
        public static final String CATEGORY = "category";
        public static final String STATUS = "status";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";

        private Items() {
        }
    }

    public static final class Offers implements BaseColumns {
        public static final String TABLE = "offers";
        public static final String ITEM_ID = "item_id";
        public static final String BUYER_ID = "buyer_id";
        public static final String AMOUNT_CENTS = "amount_cents";
        public static final String TYPE = "type";
        public static final String STATUS = "status";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";

        private Offers() {
        }
    }

    public static final class TradeTransactions implements BaseColumns {
        public static final String TABLE = "trade_transactions";
        public static final String ITEM_ID = "item_id";
        public static final String SELLER_ID = "seller_id";
        public static final String BUYER_ID = "buyer_id";
        public static final String OFFER_ID = "offer_id";
        public static final String FINAL_PRICE_CENTS = "final_price_cents";
        public static final String CREATED_AT = "created_at";

        private TradeTransactions() {
        }
    }
}
