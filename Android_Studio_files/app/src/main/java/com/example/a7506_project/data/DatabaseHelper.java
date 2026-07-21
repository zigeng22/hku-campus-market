package com.example.a7506_project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.contract.DatabaseContract;
import com.example.a7506_project.contract.DatabaseContract.Items;
import com.example.a7506_project.contract.DatabaseContract.Offers;
import com.example.a7506_project.contract.DatabaseContract.TradeTransactions;
import com.example.a7506_project.contract.DatabaseContract.Users;
import com.example.a7506_project.util.PasswordHasher;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String ITEMS_V1 = Items.TABLE + "_v1";
    private static final String OFFERS_V1 = Offers.TABLE + "_v1";
    private static final String TRANSACTIONS_V1 = TradeTransactions.TABLE + "_v1";

    public DatabaseHelper(Context context) {
        this(context, DatabaseContract.DATABASE_NAME);
    }

    DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createItemsTable(db);
        createOffersTable(db);
        createTransactionsTable(db);
        createIndexes(db);
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Users.TABLE + " ("
                + Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Users.NICKNAME + " TEXT NOT NULL COLLATE NOCASE UNIQUE, "
                + Users.PASSWORD_HASH + " TEXT NOT NULL, "
                + Users.PASSWORD_SALT + " TEXT NOT NULL, "
                + Users.WHATSAPP + " TEXT NOT NULL, "
                + Users.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");
    }

    private void createItemsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Items.TABLE + " ("
                + Items._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Items.SELLER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + Items.NAME + " TEXT NOT NULL, "
                + Items.DESCRIPTION + " TEXT NOT NULL DEFAULT '', "
                + Items.PRICE_CENTS + " INTEGER NOT NULL CHECK (" + Items.PRICE_CENTS + " > 0), "
                + Items.IMAGE_URI + " TEXT, "
                + Items.CATEGORY + " TEXT NOT NULL, "
                + Items.STATUS + " TEXT NOT NULL DEFAULT '" + AppContract.ITEM_ACTIVE + "' CHECK ("
                + Items.STATUS + " IN ('" + AppContract.ITEM_ACTIVE + "','" + AppContract.ITEM_SOLD
                + "','" + AppContract.ITEM_DELETED + "')), "
                + Items.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
                + Items.UPDATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");
    }

    private void createOffersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Offers.TABLE + " ("
                + Offers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Offers.ITEM_ID + " INTEGER NOT NULL REFERENCES " + Items.TABLE + "(" + Items._ID + "), "
                + Offers.BUYER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + Offers.AMOUNT_CENTS + " INTEGER NOT NULL CHECK (" + Offers.AMOUNT_CENTS + " > 0), "
                + Offers.TYPE + " TEXT NOT NULL CHECK (" + Offers.TYPE + " IN ('"
                + AppContract.OFFER_TYPE_NEGOTIATED + "','" + AppContract.OFFER_TYPE_BUY_NOW + "')), "
                + Offers.STATUS + " TEXT NOT NULL DEFAULT '" + AppContract.OFFER_PENDING + "' CHECK ("
                + Offers.STATUS + " IN ('" + AppContract.OFFER_PENDING + "','"
                + AppContract.OFFER_ACCEPTED + "','" + AppContract.OFFER_REJECTED + "')), "
                + Offers.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
                + Offers.UPDATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");
    }

    private void createTransactionsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TradeTransactions.TABLE + " ("
                + TradeTransactions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TradeTransactions.ITEM_ID + " INTEGER NOT NULL UNIQUE REFERENCES " + Items.TABLE + "(" + Items._ID + "), "
                + TradeTransactions.SELLER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + TradeTransactions.BUYER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + TradeTransactions.OFFER_ID + " INTEGER NOT NULL UNIQUE REFERENCES " + Offers.TABLE + "(" + Offers._ID + "), "
                + TradeTransactions.FINAL_PRICE_CENTS + " INTEGER NOT NULL CHECK ("
                + TradeTransactions.FINAL_PRICE_CENTS + " > 0), "
                + TradeTransactions.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX idx_items_status_created_at ON " + Items.TABLE
                + "(" + Items.STATUS + "," + Items.CREATED_AT + ")");
        db.execSQL("CREATE INDEX idx_items_seller_status ON " + Items.TABLE
                + "(" + Items.SELLER_ID + "," + Items.STATUS + ")");
        db.execSQL("CREATE INDEX idx_items_category ON " + Items.TABLE + "(" + Items.CATEGORY + ")");
        db.execSQL("CREATE INDEX idx_offers_item_status ON " + Offers.TABLE
                + "(" + Offers.ITEM_ID + "," + Offers.STATUS + ")");
        db.execSQL("CREATE INDEX idx_offers_buyer_status ON " + Offers.TABLE
                + "(" + Offers.BUYER_ID + "," + Offers.STATUS + ")");
        db.execSQL("CREATE UNIQUE INDEX idx_one_pending_per_buyer ON "
                + Offers.TABLE + "(" + Offers.ITEM_ID + "," + Offers.BUYER_ID + ") "
                + "WHERE " + Offers.STATUS + " = '" + AppContract.OFFER_PENDING + "'");
        db.execSQL("CREATE INDEX idx_transactions_buyer ON " + TradeTransactions.TABLE
                + "(" + TradeTransactions.BUYER_ID + ")");
        db.execSQL("CREATE INDEX idx_transactions_seller ON " + TradeTransactions.TABLE
                + "(" + TradeTransactions.SELLER_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateVersion1To2(db);
        }
    }

    private void migrateVersion1To2(SQLiteDatabase db) {
        dropVersion1Indexes(db);
        db.execSQL("ALTER TABLE " + TradeTransactions.TABLE + " RENAME TO " + TRANSACTIONS_V1);
        db.execSQL("ALTER TABLE " + Offers.TABLE + " RENAME TO " + OFFERS_V1);
        db.execSQL("ALTER TABLE " + Items.TABLE + " RENAME TO " + ITEMS_V1);

        createItemsTable(db);
        createOffersTable(db);
        createTransactionsTable(db);

        copyItems(db);
        copyOffers(db);
        copyTransactions(db);

        db.execSQL("DROP TABLE " + TRANSACTIONS_V1);
        db.execSQL("DROP TABLE " + OFFERS_V1);
        db.execSQL("DROP TABLE " + ITEMS_V1);
        createIndexes(db);
    }

    private void dropVersion1Indexes(SQLiteDatabase db) {
        db.execSQL("DROP INDEX IF EXISTS idx_items_status");
        db.execSQL("DROP INDEX IF EXISTS idx_items_seller");
        db.execSQL("DROP INDEX IF EXISTS idx_items_category");
        db.execSQL("DROP INDEX IF EXISTS idx_offers_item");
        db.execSQL("DROP INDEX IF EXISTS idx_one_pending_per_buyer");
    }

    private void copyItems(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Items.TABLE + " ("
                + Items._ID + "," + Items.SELLER_ID + "," + Items.NAME + "," + Items.DESCRIPTION + ","
                + Items.PRICE_CENTS + "," + Items.IMAGE_URI + "," + Items.CATEGORY + "," + Items.STATUS + ","
                + Items.CREATED_AT + "," + Items.UPDATED_AT + ") SELECT "
                + Items._ID + "," + Items.SELLER_ID + "," + Items.NAME + "," + Items.DESCRIPTION + ","
                + Items.PRICE_CENTS + "," + Items.IMAGE_URI + "," + Items.CATEGORY + "," + Items.STATUS + ","
                + Items.CREATED_AT + "," + Items.UPDATED_AT + " FROM " + ITEMS_V1);
    }

    private void copyOffers(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + Offers.TABLE + " ("
                + Offers._ID + "," + Offers.ITEM_ID + "," + Offers.BUYER_ID + "," + Offers.AMOUNT_CENTS + ","
                + Offers.TYPE + "," + Offers.STATUS + "," + Offers.CREATED_AT + "," + Offers.UPDATED_AT
                + ") SELECT " + Offers._ID + "," + Offers.ITEM_ID + "," + Offers.BUYER_ID + ","
                + Offers.AMOUNT_CENTS + "," + Offers.TYPE + "," + Offers.STATUS + "," + Offers.CREATED_AT + ","
                + Offers.UPDATED_AT + " FROM " + OFFERS_V1);
    }

    private void copyTransactions(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TradeTransactions.TABLE + " ("
                + TradeTransactions._ID + "," + TradeTransactions.ITEM_ID + "," + TradeTransactions.SELLER_ID + ","
                + TradeTransactions.BUYER_ID + "," + TradeTransactions.OFFER_ID + ","
                + TradeTransactions.FINAL_PRICE_CENTS + "," + TradeTransactions.CREATED_AT + ") SELECT "
                + TradeTransactions._ID + "," + TradeTransactions.ITEM_ID + "," + TradeTransactions.SELLER_ID + ","
                + TradeTransactions.BUYER_ID + "," + TradeTransactions.OFFER_ID + ","
                + TradeTransactions.FINAL_PRICE_CENTS + "," + TradeTransactions.CREATED_AT
                + " FROM " + TRANSACTIONS_V1);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
