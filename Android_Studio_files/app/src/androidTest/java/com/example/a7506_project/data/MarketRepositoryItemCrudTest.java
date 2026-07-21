package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.contract.DatabaseContract.Items;
import com.example.a7506_project.contract.DatabaseContract.Offers;
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.SortOrder;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryItemCrudTest {
    private static final String TEST_DATABASE = "item_02_crud_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;
    private long sellerId;
    private long otherUserId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);
        sellerId = register("ItemSeller", "91234567");
        otherUserId = register("OtherUser", "92345678");
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void createAndGetItemMapsNormalizedValuesAndDefaults() {
        long itemId = repository.createItem(sellerId,
                new ItemDraft("  Java book  ", null, 12345, null, null));

        assertTrue(itemId != AppContract.INVALID_ID);
        Item item = repository.getItemById(itemId);
        assertEquals(sellerId, item.getSellerId());
        assertEquals("Java book", item.getName());
        assertEquals("", item.getDescription());
        assertEquals(12345, item.getPriceCents());
        assertNull(item.getImageUri());
        assertEquals(AppContract.CATEGORY_OTHERS, item.getCategory());
        assertEquals(AppContract.ITEM_ACTIVE, item.getStatus());
    }

    @Test
    public void invalidDraftPriceNameOrSellerReturnsInvalidId() {
        assertEquals(AppContract.INVALID_ID, repository.createItem(sellerId, null));
        assertEquals(AppContract.INVALID_ID, repository.createItem(sellerId,
                new ItemDraft("   ", "Description", 1000, null, AppContract.CATEGORY_BOOKS)));
        assertEquals(AppContract.INVALID_ID, repository.createItem(sellerId,
                new ItemDraft("Book", "Description", 0, null, AppContract.CATEGORY_BOOKS)));
        assertEquals(AppContract.INVALID_ID, repository.createItem(999999,
                new ItemDraft("Book", "Description", 1000, null, AppContract.CATEGORY_BOOKS)));
        assertEquals(0, countRows(Items.TABLE));
    }

    @Test
    public void ownerCanUpdateActiveItemAndNullCategoryUsesDefault() {
        long itemId = createItem();
        ItemDraft changes = new ItemDraft("Updated book", null, 9900, "content://image", null);

        assertTrue(repository.updateItem(itemId, sellerId, changes));

        Item updated = repository.getItemById(itemId);
        assertEquals("Updated book", updated.getName());
        assertEquals("", updated.getDescription());
        assertEquals(9900, updated.getPriceCents());
        assertEquals("content://image", updated.getImageUri());
        assertEquals(AppContract.CATEGORY_OTHERS, updated.getCategory());
    }

    @Test
    public void nonOwnerInvalidDraftAndMissingItemCannotUpdate() {
        long itemId = createItem();
        Item before = repository.getItemById(itemId);
        ItemDraft changes = new ItemDraft("Changed", "Changed", 8000, null,
                AppContract.CATEGORY_ELECTRONICS);

        assertFalse(repository.updateItem(itemId, otherUserId, changes));
        assertFalse(repository.updateItem(itemId, sellerId, null));
        assertFalse(repository.updateItem(itemId, sellerId,
                new ItemDraft("Changed", "Changed", -1, null, AppContract.CATEGORY_BOOKS)));
        assertFalse(repository.updateItem(999999, sellerId, changes));

        Item unchanged = repository.getItemById(itemId);
        assertEquals(before.getName(), unchanged.getName());
        assertEquals(before.getPriceCents(), unchanged.getPriceCents());
    }

    @Test
    public void onlyOwnerCanDeleteActiveItemAndDeletedItemCannotChangeAgain() {
        long itemId = createItem();

        assertFalse(repository.softDeleteItem(itemId, otherUserId));
        assertEquals(AppContract.ITEM_ACTIVE, repository.getItemById(itemId).getStatus());
        assertTrue(repository.softDeleteItem(itemId, sellerId));
        assertEquals(AppContract.ITEM_DELETED, repository.getItemById(itemId).getStatus());
        assertFalse(repository.softDeleteItem(itemId, sellerId));
        assertFalse(repository.updateItem(itemId, sellerId,
                new ItemDraft("Changed", "Changed", 8000, null, AppContract.CATEGORY_BOOKS)));
        assertTrue(repository.searchActiveItems(
                null, AppContract.CATEGORY_ALL, SortOrder.NEWEST).isEmpty());
    }

    @Test
    public void deletingItemRejectsEveryPendingOffer() {
        long itemId = createItem();
        long thirdUserId = register("ThirdUser", "93456789");
        assertTrue(repository.placeOffer(itemId, otherUserId, 10000,
                AppContract.OFFER_TYPE_NEGOTIATED).isSuccess());
        assertTrue(repository.placeOffer(itemId, thirdUserId, 9000,
                AppContract.OFFER_TYPE_NEGOTIATED).isSuccess());

        assertTrue(repository.softDeleteItem(itemId, sellerId));

        List<OfferSummary> offers = repository.getOffersForSellerItem(itemId, sellerId);
        assertEquals(2, offers.size());
        for (OfferSummary offer : offers) {
            assertEquals(AppContract.OFFER_REJECTED, offer.getStatus());
        }
    }

    @Test
    public void offerUpdateFailureRollsBackSoftDelete() {
        long itemId = createItem();
        PlaceOfferResult offer = repository.placeOffer(itemId, otherUserId, 10000,
                AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(offer.isSuccess());
        databaseHelper.getWritableDatabase().execSQL(
                "CREATE TRIGGER force_offer_update_failure BEFORE UPDATE ON " + Offers.TABLE
                        + " WHEN OLD." + Offers.STATUS + " = '" + AppContract.OFFER_PENDING + "'"
                        + " BEGIN SELECT RAISE(ABORT, 'forced offer update failure'); END");

        assertFalse(repository.softDeleteItem(itemId, sellerId));

        assertEquals(AppContract.ITEM_ACTIVE, repository.getItemById(itemId).getStatus());
        assertEquals(AppContract.OFFER_PENDING,
                repository.getOffersForSellerItem(itemId, sellerId).get(0).getStatus());
    }

    @Test
    public void missingItemReturnsNull() {
        assertNull(repository.getItemById(999999));
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private long createItem() {
        long itemId = repository.createItem(sellerId,
                new ItemDraft("Course book", "Clean condition", 12000, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);
        return itemId;
    }

    private int countRows(String table) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return (int) db.compileStatement("SELECT COUNT(*) FROM " + table).simpleQueryForLong();
    }
}
