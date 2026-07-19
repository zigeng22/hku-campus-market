package com.example.a7506_project.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.a7506_project.contract.DatabaseContract;
import com.example.a7506_project.contract.DatabaseContract.Items;
import com.example.a7506_project.contract.DatabaseContract.Offers;
import com.example.a7506_project.contract.DatabaseContract.TradeTransactions;
import com.example.a7506_project.contract.DatabaseContract.Users;
import com.example.a7506_project.util.PasswordHasher;

import java.util.Arrays;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Users.TABLE + " ("
                + Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Users.NICKNAME + " TEXT NOT NULL COLLATE NOCASE UNIQUE, "
                + Users.PASSWORD_HASH + " TEXT NOT NULL, "
                + Users.PASSWORD_SALT + " TEXT NOT NULL, "
                + Users.WHATSAPP + " TEXT NOT NULL, "
                + Users.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");

        db.execSQL("CREATE TABLE " + Items.TABLE + " ("
                + Items._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Items.SELLER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + Items.NAME + " TEXT NOT NULL, "
                + Items.DESCRIPTION + " TEXT NOT NULL DEFAULT '', "
                + Items.PRICE_CENTS + " INTEGER NOT NULL, "
                + Items.IMAGE_URI + " TEXT, "
                + Items.CATEGORY + " TEXT NOT NULL, "
                + Items.STATUS + " TEXT NOT NULL DEFAULT 'ACTIVE', "
                + Items.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
                + Items.UPDATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");

        db.execSQL("CREATE INDEX idx_items_status ON " + Items.TABLE + "(" + Items.STATUS + ")");
        db.execSQL("CREATE INDEX idx_items_seller ON " + Items.TABLE + "(" + Items.SELLER_ID + ")");
        db.execSQL("CREATE INDEX idx_items_category ON " + Items.TABLE + "(" + Items.CATEGORY + ")");

        db.execSQL("CREATE TABLE " + Offers.TABLE + " ("
                + Offers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Offers.ITEM_ID + " INTEGER NOT NULL REFERENCES " + Items.TABLE + "(" + Items._ID + "), "
                + Offers.BUYER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + Offers.AMOUNT_CENTS + " INTEGER NOT NULL, "
                + Offers.TYPE + " TEXT NOT NULL, "
                + Offers.STATUS + " TEXT NOT NULL DEFAULT 'PENDING', "
                + Offers.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), "
                + Offers.UPDATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");

        db.execSQL("CREATE INDEX idx_offers_item ON " + Offers.TABLE + "(" + Offers.ITEM_ID + ")");
        db.execSQL("CREATE UNIQUE INDEX idx_one_pending_per_buyer ON "
                + Offers.TABLE + "(" + Offers.ITEM_ID + "," + Offers.BUYER_ID + ") "
                + "WHERE " + Offers.STATUS + " = 'PENDING'");

        db.execSQL("CREATE TABLE " + TradeTransactions.TABLE + " ("
                + TradeTransactions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TradeTransactions.ITEM_ID + " INTEGER NOT NULL UNIQUE REFERENCES " + Items.TABLE + "(" + Items._ID + "), "
                + TradeTransactions.SELLER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + TradeTransactions.BUYER_ID + " INTEGER NOT NULL REFERENCES " + Users.TABLE + "(" + Users._ID + "), "
                + TradeTransactions.OFFER_ID + " INTEGER NOT NULL UNIQUE REFERENCES " + Offers.TABLE + "(" + Offers._ID + "), "
                + TradeTransactions.FINAL_PRICE_CENTS + " INTEGER NOT NULL, "
                + TradeTransactions.CREATED_AT + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No migration needed for v1
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
