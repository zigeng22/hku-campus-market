package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.SortOrder;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryListingSummaryTest {
    private static final String TEST_DATABASE = "trade_06_listing_summary_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;
    private long sellerId;
    private long firstBuyerId;
    private long secondBuyerId;
    private long itemId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);

        sellerId = register("ListingSeller", "91234567");
        firstBuyerId = register("ListingBuyerOne", "92345678");
        secondBuyerId = register("ListingBuyerTwo", "93456789");
        itemId = repository.createItem(sellerId,
                new ItemDraft("Listing book", "Summary test", 12000, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void homeAndSellerListingsReturnConsistentStatusAndPendingCount() {
        placeOffer(firstBuyerId, 11000);
        placeOffer(secondBuyerId, 10000);

        ItemCard homeCard = onlyCard(repository.searchActiveItems(
                null, AppContract.CATEGORY_ALL, SortOrder.NEWEST));
        ItemCard sellerCard = onlyCard(repository.getListingsBySeller(sellerId));

        assertEquals(AppContract.ITEM_ACTIVE, homeCard.getStatus());
        assertEquals(2, homeCard.getOfferCount());
        assertEquals(AppContract.ITEM_ACTIVE, sellerCard.getStatus());
        assertEquals(2, sellerCard.getOfferCount());
    }

    @Test
    public void confirmedDealUpdatesSellerListingAndRemovesHomeCard() {
        PlaceOfferResult acceptedOffer = placeOffer(firstBuyerId, 11000);
        placeOffer(secondBuyerId, 10000);

        AcceptOfferResult accepted = repository.acceptOffer(acceptedOffer.getOfferId(), sellerId);
        assertTrue(accepted.isSuccess());

        ItemCard sellerCard = onlyCard(repository.getListingsBySeller(sellerId));
        assertEquals(AppContract.ITEM_SOLD, sellerCard.getStatus());
        assertEquals(0, sellerCard.getOfferCount());
        assertTrue(repository.searchActiveItems(
                null, AppContract.CATEGORY_ALL, SortOrder.NEWEST).isEmpty());
    }

    @Test
    public void anotherSellerCannotSeeListingsTheyDoNotOwn() {
        long otherSellerId = register("OtherListingSeller", "94567890");
        assertTrue(repository.getListingsBySeller(otherSellerId).isEmpty());
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private PlaceOfferResult placeOffer(long buyerId, long amountCents) {
        PlaceOfferResult result = repository.placeOffer(
                itemId, buyerId, amountCents, AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(result.isSuccess());
        return result;
    }

    private ItemCard onlyCard(List<ItemCard> cards) {
        assertEquals(1, cards.size());
        return cards.get(0);
    }
}
