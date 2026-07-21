package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.contract.DatabaseContract.Items;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.SortOrder;
import com.example.a7506_project.model.result.RegistrationResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryItemSearchTest {
    private static final String TEST_DATABASE = "item_03_search_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;
    private long sellerId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);

        RegistrationResult seller = repository.registerUser(
                "SearchSeller", "secret12", "91234567");
        assertTrue(seller.isSuccess());
        sellerId = seller.getUserId();
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void keywordMatchesNameOrDescriptionIgnoringAsciiCase() {
        long nameMatch = createItem("Java Textbook", "First edition", 10000,
                AppContract.CATEGORY_BOOKS);
        long descriptionMatch = createItem("Desk reference", "Includes JAVA examples", 9000,
                AppContract.CATEGORY_BOOKS);
        createItem("Reading lamp", "Warm light", 8000, AppContract.CATEGORY_DAILY_GOODS);

        List<ItemCard> results = repository.searchActiveItems(
                "java", AppContract.CATEGORY_ALL, SortOrder.NEWEST);

        assertEquals(2, results.size());
        assertTrue(containsItem(results, nameMatch));
        assertTrue(containsItem(results, descriptionMatch));
    }

    @Test
    public void blankKeywordReturnsOnlyActiveItems() {
        long active = createItem("Active book", "Available", 10000,
                AppContract.CATEGORY_BOOKS);
        long deleted = createItem("Deleted book", "Unavailable", 9000,
                AppContract.CATEGORY_BOOKS);
        assertTrue(repository.softDeleteItem(deleted, sellerId));

        List<ItemCard> results = repository.searchActiveItems(
                "   ", AppContract.CATEGORY_ALL, SortOrder.NEWEST);

        assertEquals(1, results.size());
        assertEquals(active, results.get(0).getItemId());
    }

    @Test
    public void categoryFilterCombinesWithDescriptionSearch() {
        long book = createItem("Study guide", "Portable charger tips", 10000,
                AppContract.CATEGORY_BOOKS);
        createItem("Power bank", "Portable charger", 12000,
                AppContract.CATEGORY_ELECTRONICS);

        List<ItemCard> results = repository.searchActiveItems(
                "charger", AppContract.CATEGORY_BOOKS, SortOrder.NEWEST);

        assertEquals(1, results.size());
        assertEquals(book, results.get(0).getItemId());
    }

    @Test
    public void allSortOrdersUseStableNewestIdTieBreaker() {
        long first = createItem("First", "Same timestamp", 10000,
                AppContract.CATEGORY_BOOKS);
        long second = createItem("Second", "Same timestamp", 10000,
                AppContract.CATEGORY_BOOKS);
        long third = createItem("Third", "Same timestamp", 10000,
                AppContract.CATEGORY_BOOKS);
        forceCreatedAt(first, 1000);
        forceCreatedAt(second, 1000);
        forceCreatedAt(third, 1000);

        assertOrder(repository.searchActiveItems(
                null, AppContract.CATEGORY_ALL, SortOrder.NEWEST), third, second, first);
        assertOrder(repository.searchActiveItems(
                null, AppContract.CATEGORY_ALL, SortOrder.PRICE_LOW_TO_HIGH), third, second, first);
        assertOrder(repository.searchActiveItems(
                null, AppContract.CATEGORY_ALL, SortOrder.PRICE_HIGH_TO_LOW), third, second, first);
    }

    private long createItem(String name, String description, long priceCents, String category) {
        long itemId = repository.createItem(sellerId,
                new ItemDraft(name, description, priceCents, null, category));
        assertTrue(itemId != AppContract.INVALID_ID);
        return itemId;
    }

    private void forceCreatedAt(long itemId, long createdAt) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Items.CREATED_AT, createdAt);
        assertEquals(1, database.update(Items.TABLE, values,
                Items._ID + " = ?", new String[]{String.valueOf(itemId)}));
    }

    private boolean containsItem(List<ItemCard> items, long itemId) {
        for (ItemCard item : items) {
            if (item.getItemId() == itemId) {
                return true;
            }
        }
        return false;
    }

    private void assertOrder(List<ItemCard> items, long... expectedIds) {
        assertEquals(expectedIds.length, items.size());
        for (int index = 0; index < expectedIds.length; index++) {
            assertEquals(expectedIds[index], items.get(index).getItemId());
        }
    }
}
