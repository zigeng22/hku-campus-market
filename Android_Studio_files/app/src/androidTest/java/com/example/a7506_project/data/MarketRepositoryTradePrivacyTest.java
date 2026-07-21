package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.ParticipationSummary;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryTradePrivacyTest {
    private static final String TEST_DATABASE = "trade_07_privacy_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void acceptedBuyerIsOnlyBuyerWhoReceivesSellerWhatsapp() {
        long sellerId = register("Alice", "91234567");
        long acceptedBuyerId = register("Bob", "92345678");
        long rejectedBuyerId = register("Charlie", "93456789");

        long itemId = repository.createItem(sellerId,
                new ItemDraft("Java textbook", "Clean condition", 12000, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);

        PlaceOfferResult acceptedOffer = repository.placeOffer(
                itemId, acceptedBuyerId, 11000, AppContract.OFFER_TYPE_NEGOTIATED);
        PlaceOfferResult rejectedOffer = repository.placeOffer(
                itemId, rejectedBuyerId, 10000, AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(acceptedOffer.isSuccess());
        assertTrue(rejectedOffer.isSuccess());

        AcceptOfferResult accepted = repository.acceptOffer(acceptedOffer.getOfferId(), sellerId);
        assertTrue(accepted.isSuccess());

        ParticipationSummary winner = onlyActivityFor(acceptedBuyerId);
        assertEquals(AppContract.OFFER_ACCEPTED, winner.getOfferStatus());
        assertEquals("CONFIRMED", winner.getDealStatus());
        assertEquals("91234567", winner.getCounterpartyWhatsapp());

        ParticipationSummary otherBuyer = onlyActivityFor(rejectedBuyerId);
        assertEquals(AppContract.OFFER_REJECTED, otherBuyer.getOfferStatus());
        assertFalse("CONFIRMED".equals(otherBuyer.getDealStatus()));
        assertNull(otherBuyer.getCounterpartyWhatsapp());
    }

    @Test
    public void pendingOfferDoesNotExposeSellerWhatsapp() {
        long sellerId = register("Seller", "91234567");
        long buyerId = register("Buyer", "92345678");
        long itemId = repository.createItem(sellerId,
                new ItemDraft("Desk lamp", "Works normally", 8000, null,
                        AppContract.CATEGORY_DAILY_GOODS));

        PlaceOfferResult offer = repository.placeOffer(
                itemId, buyerId, 7000, AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(offer.isSuccess());

        ParticipationSummary pending = onlyActivityFor(buyerId);
        assertEquals(AppContract.OFFER_PENDING, pending.getOfferStatus());
        assertFalse("CONFIRMED".equals(pending.getDealStatus()));
        assertNull(pending.getCounterpartyWhatsapp());
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private ParticipationSummary onlyActivityFor(long buyerId) {
        List<ParticipationSummary> activities = repository.getBuyerActivity(buyerId);
        assertEquals(1, activities.size());
        return activities.get(0);
    }
}
