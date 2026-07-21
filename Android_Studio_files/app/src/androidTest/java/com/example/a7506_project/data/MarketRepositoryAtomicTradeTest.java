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
import com.example.a7506_project.contract.DatabaseContract.TradeTransactions;
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.TradeTransaction;
import com.example.a7506_project.model.result.AcceptOfferResult;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryAtomicTradeTest {
    private static final String TEST_DATABASE = "trade_05_atomic_test.db";

    private Context context;
    private DatabaseHelper databaseHelper;
    private MarketRepository repository;
    private long sellerId;
    private long firstBuyerId;
    private long secondBuyerId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        databaseHelper = new DatabaseHelper(context, TEST_DATABASE);
        repository = new MarketRepositoryImpl(databaseHelper);
        sellerId = register("Seller", "91234567");
        firstBuyerId = register("FirstBuyer", "92345678");
        secondBuyerId = register("SecondBuyer", "93456789");
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void acceptingOfferCompletesAllDealStateChanges() {
        long itemId = createItem();
        PlaceOfferResult acceptedOffer = placeOffer(itemId, firstBuyerId, 11000);
        PlaceOfferResult rejectedOffer = placeOffer(itemId, secondBuyerId, 10500);

        AcceptOfferResult result = repository.acceptOffer(acceptedOffer.getOfferId(), sellerId);

        assertTrue(result.isSuccess());
        assertTrue(result.getTransactionId() != AppContract.INVALID_ID);
        assertEquals(AppContract.ITEM_SOLD, repository.getItemById(itemId).getStatus());
        assertEquals(AppContract.OFFER_ACCEPTED,
                findOffer(itemId, acceptedOffer.getOfferId()).getStatus());
        assertEquals(AppContract.OFFER_REJECTED,
                findOffer(itemId, rejectedOffer.getOfferId()).getStatus());

        TradeTransaction transaction = repository.getTransactionForItem(itemId);
        assertEquals(result.getTransactionId(), transaction.getId());
        assertEquals(itemId, transaction.getItemId());
        assertEquals(sellerId, transaction.getSellerId());
        assertEquals(firstBuyerId, transaction.getBuyerId());
        assertEquals(acceptedOffer.getOfferId(), transaction.getOfferId());
        assertEquals(11000, transaction.getFinalPriceCents());
        assertEquals(1, countRows(TradeTransactions.TABLE));
    }

    @Test
    public void nonOwnerCannotAcceptAndLeavesDealUnchanged() {
        long itemId = createItem();
        PlaceOfferResult offer = placeOffer(itemId, firstBuyerId, 11000);

        AcceptOfferResult result = repository.acceptOffer(offer.getOfferId(), secondBuyerId);

        assertFalse(result.isSuccess());
        assertEquals(RepositoryResultCode.NOT_OWNER, result.getCode());
        assertUnchangedPendingDeal(itemId, offer.getOfferId());
    }

    @Test
    public void repeatedAcceptDoesNotCreateSecondTransaction() {
        long itemId = createItem();
        PlaceOfferResult offer = placeOffer(itemId, firstBuyerId, 11000);
        AcceptOfferResult firstResult = repository.acceptOffer(offer.getOfferId(), sellerId);

        AcceptOfferResult repeatedResult = repository.acceptOffer(offer.getOfferId(), sellerId);

        assertTrue(firstResult.isSuccess());
        assertFalse(repeatedResult.isSuccess());
        assertEquals(RepositoryResultCode.OFFER_NOT_PENDING, repeatedResult.getCode());
        assertEquals(1, countRows(TradeTransactions.TABLE));
        assertEquals(AppContract.ITEM_SOLD, repository.getItemById(itemId).getStatus());
        assertEquals(AppContract.OFFER_ACCEPTED, findOffer(itemId, offer.getOfferId()).getStatus());
    }

    @Test
    public void transactionInsertFailureRollsBackOffersAndItem() {
        long itemId = createItem();
        PlaceOfferResult firstOffer = placeOffer(itemId, firstBuyerId, 11000);
        PlaceOfferResult secondOffer = placeOffer(itemId, secondBuyerId, 10500);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("CREATE TRIGGER force_transaction_failure BEFORE INSERT ON "
                + TradeTransactions.TABLE
                + " BEGIN SELECT RAISE(ABORT, 'forced transaction failure'); END");

        AcceptOfferResult result = repository.acceptOffer(firstOffer.getOfferId(), sellerId);

        assertFalse(result.isSuccess());
        assertEquals(RepositoryResultCode.DATABASE_ERROR, result.getCode());
        assertEquals(AppContract.ITEM_ACTIVE, repository.getItemById(itemId).getStatus());
        assertEquals(AppContract.OFFER_PENDING,
                findOffer(itemId, firstOffer.getOfferId()).getStatus());
        assertEquals(AppContract.OFFER_PENDING,
                findOffer(itemId, secondOffer.getOfferId()).getStatus());
        assertNull(repository.getTransactionForItem(itemId));
        assertEquals(0, countRows(TradeTransactions.TABLE));
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private long createItem() {
        long itemId = repository.createItem(sellerId,
                new ItemDraft("Java textbook", "Clean condition", 12000, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);
        return itemId;
    }

    private PlaceOfferResult placeOffer(long itemId, long buyerId, long amountCents) {
        PlaceOfferResult result = repository.placeOffer(
                itemId, buyerId, amountCents, AppContract.OFFER_TYPE_NEGOTIATED);
        assertTrue(result.isSuccess());
        return result;
    }

    private OfferSummary findOffer(long itemId, long offerId) {
        List<OfferSummary> offers = repository.getOffersForSellerItem(itemId, sellerId);
        for (OfferSummary offer : offers) {
            if (offer.getOfferId() == offerId) {
                return offer;
            }
        }
        throw new AssertionError("Offer not found: " + offerId);
    }

    private void assertUnchangedPendingDeal(long itemId, long offerId) {
        Item item = repository.getItemById(itemId);
        assertEquals(AppContract.ITEM_ACTIVE, item.getStatus());
        assertEquals(AppContract.OFFER_PENDING, findOffer(itemId, offerId).getStatus());
        assertNull(repository.getTransactionForItem(itemId));
        assertEquals(0, countRows(TradeTransactions.TABLE));
    }

    private int countRows(String table) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        return (int) db.compileStatement("SELECT COUNT(*) FROM " + table).simpleQueryForLong();
    }
}
