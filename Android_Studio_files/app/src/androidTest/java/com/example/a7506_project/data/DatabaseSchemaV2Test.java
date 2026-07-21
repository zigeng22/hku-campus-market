package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.contract.DatabaseContract;
import com.example.a7506_project.contract.DatabaseContract.Items;
import com.example.a7506_project.contract.DatabaseContract.Offers;
import com.example.a7506_project.contract.DatabaseContract.TradeTransactions;
import com.example.a7506_project.contract.DatabaseContract.Users;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class DatabaseSchemaV2Test {
    private static final String TEST_DATABASE = "schema_v2_test.db";
    private static final String MIGRATION_DATABASE = "schema_v1_migration_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;
    private long sellerId;
    private long buyerId;
    private long itemId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        context.deleteDatabase(MIGRATION_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);

        sellerId = register("SchemaSeller", "91234567");
        buyerId = register("SchemaBuyer", "92345678");
        itemId = repository.createItem(sellerId,
                new ItemDraft("Schema book", "Migration test", 12000, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
        context.deleteDatabase(MIGRATION_DATABASE);
    }

    @Test
    public void itemTableRejectsInvalidPriceAndStatus() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues invalidPrice = itemValues(0, AppContract.ITEM_ACTIVE);
        ContentValues invalidStatus = itemValues(1000, "ARCHIVED");

        assertConstraintViolation(() -> database.insertOrThrow(Items.TABLE, null, invalidPrice));
        assertConstraintViolation(() -> database.insertOrThrow(Items.TABLE, null, invalidStatus));
    }

    @Test
    public void offerAndTransactionTablesRejectInvalidValues() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        assertConstraintViolation(() -> database.insertOrThrow(
                Offers.TABLE, null, offerValues(0, AppContract.OFFER_TYPE_NEGOTIATED,
                        AppContract.OFFER_PENDING)));
        assertConstraintViolation(() -> database.insertOrThrow(
                Offers.TABLE, null, offerValues(1000, "AUCTION", AppContract.OFFER_PENDING)));
        assertConstraintViolation(() -> database.insertOrThrow(
                Offers.TABLE, null, offerValues(1000, AppContract.OFFER_TYPE_NEGOTIATED, "CANCELLED")));

        PlaceOfferResult offer = repository.placeOffer(
                itemId, buyerId, 10000, AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(offer.isSuccess());
        ContentValues invalidTransaction = new ContentValues();
        invalidTransaction.put(TradeTransactions.ITEM_ID, itemId);
        invalidTransaction.put(TradeTransactions.SELLER_ID, sellerId);
        invalidTransaction.put(TradeTransactions.BUYER_ID, buyerId);
        invalidTransaction.put(TradeTransactions.OFFER_ID, offer.getOfferId());
        invalidTransaction.put(TradeTransactions.FINAL_PRICE_CENTS, 0);
        assertConstraintViolation(() -> database.insertOrThrow(
                TradeTransactions.TABLE, null, invalidTransaction));
    }

    @Test
    public void requiredCompositeIndexesExist() {
        assertContainsIndexes(Items.TABLE,
                "idx_items_status_created_at", "idx_items_seller_status");
        assertContainsIndexes(Offers.TABLE,
                "idx_offers_item_status", "idx_offers_buyer_status", "idx_one_pending_per_buyer");
        assertContainsIndexes(TradeTransactions.TABLE,
                "idx_transactions_buyer", "idx_transactions_seller");
    }

    @Test
    public void versionOneDatabaseMigratesWithoutLosingRows() {
        createVersionOneDatabase();

        DatabaseHelper migratedHelper = new DatabaseHelper(context, MIGRATION_DATABASE);
        SQLiteDatabase migrated = migratedHelper.getWritableDatabase();
        assertEquals(DatabaseContract.DATABASE_VERSION, migrated.getVersion());
        assertEquals(2, countRows(migrated, Users.TABLE));
        assertEquals(1, countRows(migrated, Items.TABLE));
        assertEquals(1, countRows(migrated, Offers.TABLE));
        assertEquals(1, countRows(migrated, TradeTransactions.TABLE));

        Cursor foreignKeyCheck = migrated.rawQuery("PRAGMA foreign_key_check", null);
        assertFalse(foreignKeyCheck.moveToFirst());
        foreignKeyCheck.close();

        assertContainsIndexes(migrated, Items.TABLE, "idx_items_status_created_at");
        assertContainsIndexes(migrated, Offers.TABLE, "idx_offers_buyer_status");
        migratedHelper.close();
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private ContentValues itemValues(long priceCents, String status) {
        ContentValues values = new ContentValues();
        values.put(Items.SELLER_ID, sellerId);
        values.put(Items.NAME, "Invalid item");
        values.put(Items.DESCRIPTION, "Constraint test");
        values.put(Items.PRICE_CENTS, priceCents);
        values.put(Items.CATEGORY, AppContract.CATEGORY_BOOKS);
        values.put(Items.STATUS, status);
        return values;
    }

    private ContentValues offerValues(long amountCents, String type, String status) {
        ContentValues values = new ContentValues();
        values.put(Offers.ITEM_ID, itemId);
        values.put(Offers.BUYER_ID, buyerId);
        values.put(Offers.AMOUNT_CENTS, amountCents);
        values.put(Offers.TYPE, type);
        values.put(Offers.STATUS, status);
        return values;
    }

    private void assertConstraintViolation(Runnable insert) {
        try {
            insert.run();
            fail("Expected SQLiteConstraintException");
        } catch (SQLiteConstraintException expected) {
            // Expected: schema constraints are the behavior under test.
        }
    }

    private void assertContainsIndexes(String table, String... expectedNames) {
        assertContainsIndexes(databaseHelper.getReadableDatabase(), table, expectedNames);
    }

    private void assertContainsIndexes(SQLiteDatabase database, String table, String... expectedNames) {
        Set<String> names = new HashSet<>();
        Cursor cursor = database.rawQuery("PRAGMA index_list(" + table + ")", null);
        while (cursor.moveToNext()) {
            names.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        for (String expectedName : expectedNames) {
            assertTrue("Missing index " + expectedName, names.contains(expectedName));
        }
    }

    private int countRows(SQLiteDatabase database, String table) {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + table, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private void createVersionOneDatabase() {
        SQLiteDatabase database = context.openOrCreateDatabase(
                MIGRATION_DATABASE, Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, nickname TEXT NOT NULL COLLATE NOCASE UNIQUE, password_hash TEXT NOT NULL, password_salt TEXT NOT NULL, whatsapp TEXT NOT NULL, created_at INTEGER NOT NULL)");
        database.execSQL("CREATE TABLE items (_id INTEGER PRIMARY KEY AUTOINCREMENT, seller_id INTEGER NOT NULL REFERENCES users(_id), name TEXT NOT NULL, description TEXT NOT NULL, price_cents INTEGER NOT NULL, image_uri TEXT, category TEXT NOT NULL, status TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)");
        database.execSQL("CREATE TABLE offers (_id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER NOT NULL REFERENCES items(_id), buyer_id INTEGER NOT NULL REFERENCES users(_id), amount_cents INTEGER NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)");
        database.execSQL("CREATE TABLE trade_transactions (_id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER NOT NULL UNIQUE REFERENCES items(_id), seller_id INTEGER NOT NULL REFERENCES users(_id), buyer_id INTEGER NOT NULL REFERENCES users(_id), offer_id INTEGER NOT NULL UNIQUE REFERENCES offers(_id), final_price_cents INTEGER NOT NULL, created_at INTEGER NOT NULL)");
        database.execSQL("CREATE INDEX idx_items_status ON items(status)");
        database.execSQL("CREATE INDEX idx_items_seller ON items(seller_id)");
        database.execSQL("CREATE INDEX idx_items_category ON items(category)");
        database.execSQL("CREATE INDEX idx_offers_item ON offers(item_id)");
        database.execSQL("CREATE UNIQUE INDEX idx_one_pending_per_buyer ON offers(item_id,buyer_id) WHERE status = 'PENDING'");

        database.execSQL("INSERT INTO users VALUES (1,'OldSeller','hash','salt','91234567',1000)");
        database.execSQL("INSERT INTO users VALUES (2,'OldBuyer','hash','salt','92345678',1000)");
        database.execSQL("INSERT INTO items VALUES (10,1,'Old book','Preserved',12000,NULL,'Books','SOLD',1000,1000)");
        database.execSQL("INSERT INTO offers VALUES (20,10,2,11000,'NEGOTIATED','ACCEPTED',1000,1000)");
        database.execSQL("INSERT INTO trade_transactions VALUES (30,10,1,2,20,11000,1000)");
        database.setVersion(1);
        database.close();
    }
}
