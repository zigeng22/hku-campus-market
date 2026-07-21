package com.example.a7506_project.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;

import java.util.List;

public final class DemoDataSeeder {
    public static final String ALICE_NICKNAME = "AliceDemo";
    public static final String BOB_NICKNAME = "BobDemo";
    public static final String DEMO_PASSWORD = "demo123";

    private static final String PREFERENCES_NAME = "demo_data_setup";
    private static final String KEY_PREPARED = "prepared_v1";
    private static final String PENDING_ITEM_NAME = "Java Programming Textbook";
    private static final String SOLD_ITEM_NAME = "Scientific Calculator";

    private DemoDataSeeder() {
    }

    public static boolean prepare(Context context, MarketRepository repository) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        User alice = ensureUser(repository, ALICE_NICKNAME, "91234567");
        User bob = ensureUser(repository, BOB_NICKNAME, "92345678");
        if (alice == null || bob == null) {
            return false;
        }
        if (preferences.getBoolean(KEY_PREPARED, false)) {
            return true;
        }

        long pendingItemId = ensureItem(repository, alice.getId(), PENDING_ITEM_NAME,
                "Clean COMP7506 Java course textbook.", 12000, AppContract.CATEGORY_BOOKS);
        if (pendingItemId == AppContract.INVALID_ID
                || !ensurePendingOffer(repository, pendingItemId, bob.getId(), 10000)) {
            return false;
        }

        long soldItemId = ensureItem(repository, alice.getId(), SOLD_ITEM_NAME,
                "Used scientific calculator in good condition.", 8000, AppContract.CATEGORY_ELECTRONICS);
        if (soldItemId == AppContract.INVALID_ID
                || !ensureConfirmedDeal(repository, soldItemId, alice.getId(), bob.getId(), 8000)) {
            return false;
        }

        preferences.edit().putBoolean(KEY_PREPARED, true).apply();
        return true;
    }

    private static User ensureUser(MarketRepository repository, String nickname, String whatsapp) {
        User existing = repository.authenticate(nickname, DEMO_PASSWORD);
        if (existing != null) {
            return existing;
        }
        RegistrationResult result = repository.registerUser(
                nickname, DEMO_PASSWORD, whatsapp);
        return result.isSuccess() ? repository.getUserById(result.getUserId()) : null;
    }

    private static long ensureItem(MarketRepository repository, long sellerId, String name,
                                   String description, long priceCents, String category) {
        for (ItemCard item : repository.getListingsBySeller(sellerId)) {
            if (name.equals(item.getName())) {
                return item.getItemId();
            }
        }
        return repository.createItem(sellerId,
                new ItemDraft(name, description, priceCents, null, category));
    }

    private static boolean ensurePendingOffer(MarketRepository repository, long itemId,
                                              long buyerId, long amountCents) {
        if (hasBuyerActivity(repository, buyerId, itemId)) {
            return true;
        }
        return repository.placeOffer(itemId, buyerId, amountCents,
                AppContract.OFFER_TYPE_NEGOTIATED).isSuccess();
    }

    private static boolean ensureConfirmedDeal(MarketRepository repository, long itemId,
                                               long sellerId, long buyerId, long amountCents) {
        if (repository.getTransactionForItem(itemId) != null) {
            return true;
        }

        PlaceOfferResult offerResult = repository.placeOffer(itemId, buyerId, amountCents,
                AppContract.OFFER_TYPE_BUY_NOW);
        long offerId = offerResult.getOfferId();
        if (!offerResult.isSuccess()) {
            offerId = findPendingOffer(repository.getOffersForSellerItem(itemId, sellerId), buyerId);
        }
        return offerId != AppContract.INVALID_ID
                && repository.acceptOffer(offerId, sellerId).isSuccess();
    }

    private static boolean hasBuyerActivity(MarketRepository repository, long buyerId, long itemId) {
        return repository.getBuyerActivity(buyerId).stream()
                .anyMatch(activity -> activity.getItemId() == itemId);
    }

    private static long findPendingOffer(List<OfferSummary> offers, long buyerId) {
        for (OfferSummary offer : offers) {
            if (offer.getBuyerId() == buyerId
                    && AppContract.OFFER_PENDING.equals(offer.getStatus())) {
                return offer.getOfferId();
            }
        }
        return AppContract.INVALID_ID;
    }
}
