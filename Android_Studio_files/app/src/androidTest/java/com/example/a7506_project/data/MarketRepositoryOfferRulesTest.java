package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryOfferRulesTest {
    private static final String TEST_DATABASE = "trade_02_offer_rules_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;
    private long sellerId;
    private long buyerId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);
        sellerId = register("Seller", "91234567");
        buyerId = register("Buyer", "92345678");
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void buyNowAlwaysStoresCurrentItemPrice() {
        long itemId = createActiveItem(12345);

        PlaceOfferResult result = repository.placeOffer(
                itemId, buyerId, 1, AppContract.OFFER_TYPE_BUY_NOW);

        assertTrue(result.isSuccess());
        OfferSummary saved = onlyOfferFor(itemId);
        assertEquals(12345, saved.getAmountCents());
        assertEquals(AppContract.OFFER_TYPE_BUY_NOW, saved.getType());
    }

    @Test
    public void negotiatedOfferStoresBuyerAmount() {
        long itemId = createActiveItem(12345);

        PlaceOfferResult result = repository.placeOffer(
                itemId, buyerId, 9800, AppContract.OFFER_TYPE_NEGOTIATED);

        assertTrue(result.isSuccess());
        assertEquals(9800, onlyOfferFor(itemId).getAmountCents());
    }

    @Test
    public void unsupportedOfferTypesAreRejected() {
        long itemId = createActiveItem(12345);

        assertFailure(RepositoryResultCode.INVALID_INPUT,
                repository.placeOffer(itemId, buyerId, 10000, null));
        assertFailure(RepositoryResultCode.INVALID_INPUT,
                repository.placeOffer(itemId, buyerId, 10000, "FLASH_SALE"));
        assertTrue(repository.getOffersForSellerItem(itemId, sellerId).isEmpty());
    }

    @Test
    public void negotiatedOfferRequiresPositiveAmount() {
        long itemId = createActiveItem(12345);

        assertFailure(RepositoryResultCode.INVALID_PRICE,
                repository.placeOffer(itemId, buyerId, 0, AppContract.OFFER_TYPE_NEGOTIATED));
        assertFailure(RepositoryResultCode.INVALID_PRICE,
                repository.placeOffer(itemId, buyerId, -1, AppContract.OFFER_TYPE_NEGOTIATED));
        assertTrue(repository.getOffersForSellerItem(itemId, sellerId).isEmpty());
    }

    @Test
    public void sellerCannotOfferOnOwnItem() {
        long itemId = createActiveItem(12345);

        assertFailure(RepositoryResultCode.CANNOT_OFFER_OWN_ITEM,
                repository.placeOffer(itemId, sellerId, 10000, AppContract.OFFER_TYPE_NEGOTIATED));
    }

    @Test
    public void buyerCannotCreateSecondPendingOffer() {
        long itemId = createActiveItem(12345);
        assertTrue(repository.placeOffer(
                itemId, buyerId, 10000, AppContract.OFFER_TYPE_NEGOTIATED).isSuccess());

        assertFailure(RepositoryResultCode.DUPLICATE_PENDING_OFFER,
                repository.placeOffer(itemId, buyerId, 11000, AppContract.OFFER_TYPE_NEGOTIATED));
        assertEquals(1, repository.getOffersForSellerItem(itemId, sellerId).size());
    }

    @Test
    public void inactiveItemRejectsNewOffer() {
        long itemId = createActiveItem(12345);
        assertTrue(repository.softDeleteItem(itemId, sellerId));

        assertFailure(RepositoryResultCode.ITEM_NOT_ACTIVE,
                repository.placeOffer(itemId, buyerId, 10000, AppContract.OFFER_TYPE_NEGOTIATED));
    }

    @Test
    public void sellerCanRejectPendingOfferAndBuyerCanOfferAgain() {
        long itemId = createActiveItem(12345);
        PlaceOfferResult first = repository.placeOffer(
                itemId, buyerId, 10000, AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(first.isSuccess());

        assertEquals(RepositoryResultCode.NOT_OWNER,
                repository.rejectOffer(first.getOfferId(), buyerId));
        assertEquals(RepositoryResultCode.OK,
                repository.rejectOffer(first.getOfferId(), sellerId));
        assertEquals(AppContract.OFFER_REJECTED, onlyOfferFor(itemId).getStatus());
        assertEquals(RepositoryResultCode.OFFER_NOT_PENDING,
                repository.rejectOffer(first.getOfferId(), sellerId));
        assertTrue(repository.placeOffer(
                itemId, buyerId, 10500, AppContract.OFFER_TYPE_NEGOTIATED).isSuccess());
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private long createActiveItem(long priceCents) {
        long itemId = repository.createItem(sellerId,
                new ItemDraft("Course book", "Clean condition", priceCents, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);
        return itemId;
    }

    private OfferSummary onlyOfferFor(long itemId) {
        List<OfferSummary> offers = repository.getOffersForSellerItem(itemId, sellerId);
        assertEquals(1, offers.size());
        return offers.get(0);
    }

    private void assertFailure(RepositoryResultCode expectedCode, PlaceOfferResult result) {
        assertTrue(!result.isSuccess());
        assertEquals(expectedCode, result.getCode());
    }
}
