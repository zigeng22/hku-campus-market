package com.example.a7506_project.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.contract.DatabaseContract.Items;
import com.example.a7506_project.contract.DatabaseContract.Offers;
import com.example.a7506_project.contract.DatabaseContract.TradeTransactions;
import com.example.a7506_project.contract.DatabaseContract.Users;
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.ParticipationSummary;
import com.example.a7506_project.model.SortOrder;
import com.example.a7506_project.model.TradeTransaction;
import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;
import com.example.a7506_project.util.PasswordHasher;
import com.example.a7506_project.util.Validators;

import java.util.ArrayList;
import java.util.List;

public class MarketRepositoryImpl implements MarketRepository {

    private final DatabaseHelper dbHelper;

    public MarketRepositoryImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ── Auth ──────────────────────────────────────────────────────────────

    @Override
    public RegistrationResult registerUser(String nickname, String password, String whatsapp) {
        String validationError = Validators.validateNickname(nickname);
        if (validationError != null) return RegistrationResult.failure(RepositoryResultCode.INVALID_INPUT);
        validationError = Validators.validatePassword(password);
        if (validationError != null) return RegistrationResult.failure(RepositoryResultCode.INVALID_INPUT);
        validationError = Validators.validateWhatsapp(whatsapp);
        if (validationError != null) return RegistrationResult.failure(RepositoryResultCode.INVALID_INPUT);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Users.TABLE + " WHERE " + Users.NICKNAME + " = ? COLLATE NOCASE",
                new String[]{nickname.trim()});
        cursor.moveToFirst();
        if (cursor.getInt(0) > 0) {
            cursor.close();
            return RegistrationResult.failure(RepositoryResultCode.DUPLICATE_NICKNAME);
        }
        cursor.close();

        byte[] salt = PasswordHasher.generateSalt();
        byte[] hash = PasswordHasher.hash(password, salt);

        ContentValues values = new ContentValues();
        values.put(Users.NICKNAME, nickname.trim());
        values.put(Users.PASSWORD_HASH, PasswordHasher.bytesToHex(hash));
        values.put(Users.PASSWORD_SALT, PasswordHasher.bytesToHex(salt));
        values.put(Users.WHATSAPP, whatsapp.trim());

        long userId = db.insertOrThrow(Users.TABLE, null, values);
        if (userId == -1) {
            return RegistrationResult.failure(RepositoryResultCode.DATABASE_ERROR);
        }
        return RegistrationResult.success(userId);
    }

    @Override
    public User authenticate(String nickname, String password) {
        if (nickname == null || password == null) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Users.TABLE, null,
                Users.NICKNAME + " = ? COLLATE NOCASE",
                new String[]{nickname.trim()}, null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String hashHex = cursor.getString(cursor.getColumnIndexOrThrow(Users.PASSWORD_HASH));
        String saltHex = cursor.getString(cursor.getColumnIndexOrThrow(Users.PASSWORD_SALT));

        byte[] storedHash = PasswordHasher.hexToBytes(hashHex);
        byte[] salt = PasswordHasher.hexToBytes(saltHex);

        if (!PasswordHasher.verify(password, salt, storedHash)) {
            cursor.close();
            return null;
        }

        User user = userFromCursor(cursor);
        cursor.close();
        return user;
    }

    @Override
    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Users.TABLE, null,
                Users._ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = userFromCursor(cursor);
        }
        cursor.close();
        return user;
    }

    // ── Items ──────────────────────────────────────────────────────────────

    @Override
    public long createItem(long sellerId, ItemDraft draft) {
        String validationError = Validators.validateItemName(draft.getName());
        if (validationError != null) return AppContract.INVALID_ID;
        validationError = Validators.validatePriceCents(draft.getPriceCents());
        if (validationError != null) return AppContract.INVALID_ID;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Items.SELLER_ID, sellerId);
        values.put(Items.NAME, draft.getName().trim());
        values.put(Items.DESCRIPTION, draft.getDescription() != null ? draft.getDescription() : "");
        values.put(Items.PRICE_CENTS, draft.getPriceCents());
        values.put(Items.IMAGE_URI, draft.getImageUri());
        values.put(Items.CATEGORY, draft.getCategory() != null ? draft.getCategory() : AppContract.CATEGORY_OTHERS);
        values.put(Items.STATUS, AppContract.ITEM_ACTIVE);

        return db.insertOrThrow(Items.TABLE, null, values);
    }

    @Override
    public boolean updateItem(long itemId, long sellerId, ItemDraft draft) {
        String validationError = Validators.validateItemName(draft.getName());
        if (validationError != null) return false;
        validationError = Validators.validatePriceCents(draft.getPriceCents());
        if (validationError != null) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Items.NAME, draft.getName().trim());
        values.put(Items.DESCRIPTION, draft.getDescription() != null ? draft.getDescription() : "");
        values.put(Items.PRICE_CENTS, draft.getPriceCents());
        values.put(Items.IMAGE_URI, draft.getImageUri());
        values.put(Items.CATEGORY, draft.getCategory());

        int rows = db.update(Items.TABLE, values,
                Items._ID + " = ? AND " + Items.SELLER_ID + " = ? AND " + Items.STATUS + " = ?",
                new String[]{String.valueOf(itemId), String.valueOf(sellerId), AppContract.ITEM_ACTIVE});
        return rows > 0;
    }

    @Override
    public boolean softDeleteItem(long itemId, long sellerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Items.STATUS, AppContract.ITEM_DELETED);

        int rows = db.update(Items.TABLE, values,
                Items._ID + " = ? AND " + Items.SELLER_ID + " = ? AND " + Items.STATUS + " = ?",
                new String[]{String.valueOf(itemId), String.valueOf(sellerId), AppContract.ITEM_ACTIVE});

        if (rows > 0) {
            // reject all pending offers on this item
            ContentValues offerValues = new ContentValues();
            offerValues.put(Offers.STATUS, AppContract.OFFER_REJECTED);
            db.update(Offers.TABLE, offerValues,
                    Offers.ITEM_ID + " = ? AND " + Offers.STATUS + " = ?",
                    new String[]{String.valueOf(itemId), AppContract.OFFER_PENDING});
        }
        return rows > 0;
    }

    @Override
    public Item getItemById(long itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Items.TABLE, null,
                Items._ID + " = ?", new String[]{String.valueOf(itemId)},
                null, null, null);

        Item item = null;
        if (cursor.moveToFirst()) {
            item = itemFromCursor(cursor);
        }
        cursor.close();
        return item;
    }

    @Override
    public List<ItemCard> searchActiveItems(String keyword, String category, SortOrder sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT i." + Items._ID + ", i." + Items.NAME + ", i." + Items.PRICE_CENTS + ", "
                        + "i." + Items.IMAGE_URI + ", i." + Items.CATEGORY + ", u." + Users.NICKNAME
                        + " FROM " + Items.TABLE + " i"
                        + " JOIN " + Users.TABLE + " u ON i." + Items.SELLER_ID + " = u." + Users._ID
                        + " WHERE i." + Items.STATUS + " = ?");
        args.add(AppContract.ITEM_ACTIVE);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND i." + Items.NAME + " LIKE ?");
            args.add("%" + keyword.trim() + "%");
        }

        if (category != null && !category.equals(AppContract.CATEGORY_ALL)) {
            sql.append(" AND i." + Items.CATEGORY + " = ?");
            args.add(category);
        }

        if (sortOrder == SortOrder.PRICE_LOW_TO_HIGH) {
            sql.append(" ORDER BY i." + Items.PRICE_CENTS + " ASC");
        } else if (sortOrder == SortOrder.PRICE_HIGH_TO_LOW) {
            sql.append(" ORDER BY i." + Items.PRICE_CENTS + " DESC");
        } else {
            sql.append(" ORDER BY i." + Items.CREATED_AT + " DESC");
        }

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        List<ItemCard> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(new ItemCard(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    "", 0  // searchActiveItems doesn't need status/offerCount
            ));
        }
        cursor.close();
        return result;
    }

    @Override
    public List<ItemCard> getListingsBySeller(long sellerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT i." + Items._ID + ", i." + Items.NAME + ", i." + Items.PRICE_CENTS + ", "
                + "i." + Items.IMAGE_URI + ", i." + Items.CATEGORY + ", u." + Users.NICKNAME + ", "
                + "i." + Items.STATUS + ", "
                + "(SELECT COUNT(*) FROM " + Offers.TABLE + " o"
                + " WHERE o." + Offers.ITEM_ID + " = i." + Items._ID
                + " AND o." + Offers.STATUS + " = 'PENDING') AS offer_count"
                + " FROM " + Items.TABLE + " i"
                + " JOIN " + Users.TABLE + " u ON i." + Items.SELLER_ID + " = u." + Users._ID
                + " WHERE i." + Items.SELLER_ID + " = ?"
                + " ORDER BY i." + Items.CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(sellerId)});
        List<ItemCard> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(new ItemCard(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7)
            ));
        }
        cursor.close();
        return result;
    }

    // ── Offers ─────────────────────────────────────────────────────────────

    @Override
    public PlaceOfferResult placeOffer(long itemId, long buyerId, long amountCents, String offerType) {
        if (amountCents <= 0) return PlaceOfferResult.failure(RepositoryResultCode.INVALID_PRICE);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // verify item exists and is ACTIVE
        Item item = getItemById(itemId);
        if (item == null) return PlaceOfferResult.failure(RepositoryResultCode.ITEM_NOT_FOUND);
        if (!AppContract.ITEM_ACTIVE.equals(item.getStatus()))
            return PlaceOfferResult.failure(RepositoryResultCode.ITEM_NOT_ACTIVE);
        if (item.getSellerId() == buyerId)
            return PlaceOfferResult.failure(RepositoryResultCode.CANNOT_OFFER_OWN_ITEM);

        // check no duplicate pending offer
        Cursor dup = db.rawQuery(
                "SELECT COUNT(*) FROM " + Offers.TABLE
                        + " WHERE " + Offers.ITEM_ID + " = ? AND " + Offers.BUYER_ID + " = ? AND " + Offers.STATUS + " = ?",
                new String[]{String.valueOf(itemId), String.valueOf(buyerId), AppContract.OFFER_PENDING});
        dup.moveToFirst();
        if (dup.getInt(0) > 0) {
            dup.close();
            return PlaceOfferResult.failure(RepositoryResultCode.DUPLICATE_PENDING_OFFER);
        }
        dup.close();

        ContentValues values = new ContentValues();
        values.put(Offers.ITEM_ID, itemId);
        values.put(Offers.BUYER_ID, buyerId);
        values.put(Offers.AMOUNT_CENTS, amountCents);
        values.put(Offers.TYPE, offerType);
        values.put(Offers.STATUS, AppContract.OFFER_PENDING);

        long offerId = db.insertOrThrow(Offers.TABLE, null, values);
        return PlaceOfferResult.success(offerId);
    }

    @Override
    public List<OfferSummary> getOffersForSellerItem(long itemId, long sellerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // verify the caller is the seller
        Item item = getItemById(itemId);
        if (item == null || item.getSellerId() != sellerId) return new ArrayList<>();

        String sql = "SELECT o." + Offers._ID + ", o." + Offers.ITEM_ID + ", u." + Users.NICKNAME + ", "
                + "u." + Users._ID + ", o." + Offers.AMOUNT_CENTS + ", o." + Offers.TYPE + ", "
                + "o." + Offers.STATUS + ", o." + Offers.CREATED_AT
                + " FROM " + Offers.TABLE + " o"
                + " JOIN " + Users.TABLE + " u ON o." + Offers.BUYER_ID + " = u." + Users._ID
                + " WHERE o." + Offers.ITEM_ID + " = ?"
                + " ORDER BY o." + Offers.CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(itemId)});
        List<OfferSummary> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(new OfferSummary(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getLong(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getLong(7)
            ));
        }
        cursor.close();
        return result;
    }

    @Override
    public AcceptOfferResult acceptOffer(long offerId, long sellerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            // step 1: verify offer is PENDING
            Cursor offerCursor = db.query(Offers.TABLE, null,
                    Offers._ID + " = ?", new String[]{String.valueOf(offerId)},
                    null, null, null);
            if (!offerCursor.moveToFirst()) {
                offerCursor.close();
                db.endTransaction();
                return AcceptOfferResult.failure(RepositoryResultCode.OFFER_NOT_FOUND);
            }
            String offerStatus = offerCursor.getString(offerCursor.getColumnIndexOrThrow(Offers.STATUS));
            if (!AppContract.OFFER_PENDING.equals(offerStatus)) {
                offerCursor.close();
                db.endTransaction();
                return AcceptOfferResult.failure(RepositoryResultCode.OFFER_NOT_PENDING);
            }
            long itemId = offerCursor.getLong(offerCursor.getColumnIndexOrThrow(Offers.ITEM_ID));
            long buyerId = offerCursor.getLong(offerCursor.getColumnIndexOrThrow(Offers.BUYER_ID));
            long amountCents = offerCursor.getLong(offerCursor.getColumnIndexOrThrow(Offers.AMOUNT_CENTS));
            offerCursor.close();

            // step 2: verify item is ACTIVE and caller is seller
            Cursor itemCursor = db.query(Items.TABLE, null,
                    Items._ID + " = ?", new String[]{String.valueOf(itemId)},
                    null, null, null);
            if (!itemCursor.moveToFirst()) {
                itemCursor.close();
                db.endTransaction();
                return AcceptOfferResult.failure(RepositoryResultCode.ITEM_NOT_FOUND);
            }
            if (!AppContract.ITEM_ACTIVE.equals(itemCursor.getString(itemCursor.getColumnIndexOrThrow(Items.STATUS)))) {
                itemCursor.close();
                db.endTransaction();
                return AcceptOfferResult.failure(RepositoryResultCode.ITEM_NOT_ACTIVE);
            }
            long itemSellerId = itemCursor.getLong(itemCursor.getColumnIndexOrThrow(Items.SELLER_ID));
            if (itemSellerId != sellerId) {
                itemCursor.close();
                db.endTransaction();
                return AcceptOfferResult.failure(RepositoryResultCode.NOT_OWNER);
            }
            itemCursor.close();

            // step 3: mark target offer ACCEPTED
            ContentValues acceptValues = new ContentValues();
            acceptValues.put(Offers.STATUS, AppContract.OFFER_ACCEPTED);
            db.update(Offers.TABLE, acceptValues,
                    Offers._ID + " = ?", new String[]{String.valueOf(offerId)});

            // step 4: mark all other PENDING offers REJECTED
            ContentValues rejectValues = new ContentValues();
            rejectValues.put(Offers.STATUS, AppContract.OFFER_REJECTED);
            db.update(Offers.TABLE, rejectValues,
                    Offers.ITEM_ID + " = ? AND " + Offers._ID + " != ? AND " + Offers.STATUS + " = ?",
                    new String[]{String.valueOf(itemId), String.valueOf(offerId), AppContract.OFFER_PENDING});

            // step 5: mark item SOLD
            ContentValues soldValues = new ContentValues();
            soldValues.put(Items.STATUS, AppContract.ITEM_SOLD);
            db.update(Items.TABLE, soldValues,
                    Items._ID + " = ?", new String[]{String.valueOf(itemId)});

            // step 6: insert trade_transaction
            ContentValues txnValues = new ContentValues();
            txnValues.put(TradeTransactions.ITEM_ID, itemId);
            txnValues.put(TradeTransactions.SELLER_ID, sellerId);
            txnValues.put(TradeTransactions.BUYER_ID, buyerId);
            txnValues.put(TradeTransactions.OFFER_ID, offerId);
            txnValues.put(TradeTransactions.FINAL_PRICE_CENTS, amountCents);
            long txnId = db.insertOrThrow(TradeTransactions.TABLE, null, txnValues);

            db.setTransactionSuccessful();
            db.endTransaction();
            return AcceptOfferResult.success(txnId);

        } catch (Exception e) {
            db.endTransaction();
            return AcceptOfferResult.failure(RepositoryResultCode.DATABASE_ERROR);
        }
    }

    @Override
    public List<ParticipationSummary> getBuyerActivity(long buyerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT o." + Offers.ITEM_ID + ", i." + Items.NAME + ", o." + Offers.AMOUNT_CENTS + ", "
                + "o." + Offers.STATUS + ", "
                + "CASE WHEN t." + TradeTransactions._ID + " IS NOT NULL THEN 'CONFIRMED' ELSE 'PENDING' END, "
                + "u." + Users.WHATSAPP
                + " FROM " + Offers.TABLE + " o"
                + " JOIN " + Items.TABLE + " i ON o." + Offers.ITEM_ID + " = i." + Items._ID
                + " LEFT JOIN " + TradeTransactions.TABLE + " t ON o." + Offers.ITEM_ID + " = t." + TradeTransactions.ITEM_ID
                + " LEFT JOIN " + Users.TABLE + " u ON i." + Items.SELLER_ID + " = u." + Users._ID
                + " WHERE o." + Offers.BUYER_ID + " = ?"
                + " ORDER BY o." + Offers.CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(buyerId)});
        List<ParticipationSummary> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            String dealStatus = cursor.getString(4);
            // only reveal WhatsApp when deal is CONFIRMED
            String whatsapp = "CONFIRMED".equals(dealStatus) ? cursor.getString(5) : null;
            result.add(new ParticipationSummary(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3),
                    dealStatus,
                    whatsapp
            ));
        }
        cursor.close();
        return result;
    }

    @Override
    public TradeTransaction getTransactionForItem(long itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TradeTransactions.TABLE, null,
                TradeTransactions.ITEM_ID + " = ?", new String[]{String.valueOf(itemId)},
                null, null, null);

        TradeTransaction txn = null;
        if (cursor.moveToFirst()) {
            txn = new TradeTransaction(
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions._ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions.ITEM_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions.SELLER_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions.BUYER_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions.OFFER_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions.FINAL_PRICE_CENTS)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(TradeTransactions.CREATED_AT))
            );
        }
        cursor.close();
        return txn;
    }

    // ── Cursor helpers ─────────────────────────────────────────────────────

    private User userFromCursor(Cursor cursor) {
        return new User(
                cursor.getLong(cursor.getColumnIndexOrThrow(Users._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(Users.NICKNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(Users.WHATSAPP)),
                cursor.getLong(cursor.getColumnIndexOrThrow(Users.CREATED_AT))
        );
    }

    private Item itemFromCursor(Cursor cursor) {
        return new Item(
                cursor.getLong(cursor.getColumnIndexOrThrow(Items._ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(Items.SELLER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(Items.NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(Items.DESCRIPTION)),
                cursor.getLong(cursor.getColumnIndexOrThrow(Items.PRICE_CENTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(Items.IMAGE_URI)),
                cursor.getString(cursor.getColumnIndexOrThrow(Items.CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(Items.STATUS)),
                cursor.getLong(cursor.getColumnIndexOrThrow(Items.CREATED_AT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(Items.UPDATED_AT))
        );
    }
}
