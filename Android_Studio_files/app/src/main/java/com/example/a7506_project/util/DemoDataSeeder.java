package com.example.a7506_project.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.PlaceOfferResult;
import com.example.a7506_project.model.result.RegistrationResult;

import java.util.List;
import java.util.Map;

public final class DemoDataSeeder {
    public static final String ALICE_NICKNAME = "AliceDemo";
    public static final String BOB_NICKNAME = "BobDemo";
    public static final String CAROL_NICKNAME = "CarolDemo";
    public static final String DAVID_NICKNAME = "DavidDemo";
    public static final String DEMO_PASSWORD = "demo123";

    private static final String PREFERENCES_NAME = "demo_data_setup";
    private static final String KEY_PREPARED = "prepared_v2";

    private static final String[] IMAGE_ASSETS = {
            "java_textbook.png", "scientific_calculator.png", "wireless_keyboard.png",
            "desk_lamp.png", "office_chair.png", "rice_cooker.png",
            "monitor_stand.png", "tennis_racket.png", "statistics_notes.png",
            "usb_c_hub.png", "headphones.png", "desk_fan.png",
            "storage_trolley.png", "bicycle_helmet.png"
    };

    private DemoDataSeeder() {
    }

    public static boolean prepare(Context context, MarketRepository repository) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        User alice = ensureUser(repository, ALICE_NICKNAME, "91234567");
        User bob = ensureUser(repository, BOB_NICKNAME, "92345678");
        User carol = ensureUser(repository, CAROL_NICKNAME, "93456789");
        User david = ensureUser(repository, DAVID_NICKNAME, "94567890");
        if (alice == null || bob == null || carol == null || david == null) {
            return false;
        }
        Map<String, String> images = DemoImageInstaller.install(context, IMAGE_ASSETS);

        long textbook = ensureItem(repository, alice.getId(), "Java Programming Textbook",
                "Clean COMP7506 Java course textbook with light notes.", 12000,
                AppContract.CATEGORY_BOOKS, images.get("java_textbook.png"));
        long calculator = ensureItem(repository, alice.getId(), "Scientific Calculator",
                "Used scientific calculator in good condition.", 8000,
                AppContract.CATEGORY_ELECTRONICS, images.get("scientific_calculator.png"));
        long keyboard = ensureItem(repository, bob.getId(), "Wireless Keyboard",
                "Compact keyboard, fully working and ideal for a study desk.", 15000,
                AppContract.CATEGORY_ELECTRONICS, images.get("wireless_keyboard.png"));
        long lamp = ensureItem(repository, bob.getId(), "Blue Study Desk Lamp",
                "Adjustable metal lamp with a bright reading light.", 9000,
                AppContract.CATEGORY_DAILY_GOODS, images.get("desk_lamp.png"));
        long chair = ensureItem(repository, carol.getId(), "Ergonomic Mesh Chair",
                "Comfortable adjustable chair for long study sessions.", 32000,
                AppContract.CATEGORY_FURNITURE, images.get("office_chair.png"));
        long cooker = ensureItem(repository, carol.getId(), "Compact Rice Cooker",
                "Clean one-person rice cooker with power cable.", 18000,
                AppContract.CATEGORY_DAILY_GOODS, images.get("rice_cooker.png"));
        long stand = ensureItem(repository, david.getId(), "Bamboo Monitor Stand",
                "Sturdy desktop stand with storage space underneath.", 11000,
                AppContract.CATEGORY_FURNITURE, images.get("monitor_stand.png"));
        long racket = ensureItem(repository, david.getId(), "Tennis Racket with Cover",
                "Beginner-friendly racket with a protective cover.", 14000,
                AppContract.CATEGORY_OTHERS, images.get("tennis_racket.png"));
        long statisticsNotes = ensureItem(repository, alice.getId(), "Statistics Revision Notes",
                "Organised lecture notes with worked examples and exam summaries.", 6500,
                AppContract.CATEGORY_BOOKS, images.get("statistics_notes.png"));
        long usbHub = ensureItem(repository, alice.getId(), "USB-C Multiport Hub",
                "Compact hub with HDMI, USB and card-reader ports.", 13000,
                AppContract.CATEGORY_ELECTRONICS, images.get("usb_c_hub.png"));
        long headphones = ensureItem(repository, bob.getId(), "Over-Ear Headphones",
                "Comfortable wireless headphones with a protective case.", 22000,
                AppContract.CATEGORY_ELECTRONICS, images.get("headphones.png"));
        long fan = ensureItem(repository, bob.getId(), "Compact Desk Fan",
                "Quiet fan with adjustable speed for a dorm study desk.", 7000,
                AppContract.CATEGORY_DAILY_GOODS, images.get("desk_fan.png"));
        long trolley = ensureItem(repository, carol.getId(), "Three-Tier Storage Trolley",
                "Slim rolling trolley for books, snacks or bathroom supplies.", 16000,
                AppContract.CATEGORY_FURNITURE, images.get("storage_trolley.png"));
        long helmet = ensureItem(repository, david.getId(), "Bicycle Helmet",
                "Lightweight adjustable helmet with minor signs of use.", 10000,
                AppContract.CATEGORY_OTHERS, images.get("bicycle_helmet.png"));

        boolean ready = allItemsCreated(textbook, calculator, keyboard, lamp,
                chair, cooker, stand, racket, statisticsNotes, usbHub,
                headphones, fan, trolley, helmet)
                && ensurePendingOffer(repository, textbook, bob.getId(), 10000)
                && ensurePendingOffer(repository, keyboard, alice.getId(), 13000)
                && ensurePendingOffer(repository, chair, david.getId(), 28000)
                && ensurePendingOffer(repository, cooker, bob.getId(), 16000)
                && ensurePendingOffer(repository, lamp, carol.getId(), 7500)
                && ensurePendingOffer(repository, stand, carol.getId(), 9500)
                && ensurePendingOffer(repository, racket, alice.getId(), 12000)
                && ensureConfirmedDeal(repository, calculator, alice.getId(), bob.getId(), 8000);
        if (!ready) return false;

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
                                   String description, long priceCents, String category,
                                   String imageUri) {
        for (ItemCard item : repository.getListingsBySeller(sellerId)) {
            if (name.equals(item.getName())) {
                Item existing = repository.getItemById(item.getItemId());
                if (existing != null && imageUri != null
                        && !imageUri.equals(existing.getImageUri())) {
                    repository.updateItemImage(existing.getId(), sellerId, imageUri);
                }
                return item.getItemId();
            }
        }
        return repository.createItem(sellerId,
                new ItemDraft(name, description, priceCents, imageUri, category));
    }

    private static boolean allItemsCreated(long... itemIds) {
        for (long itemId : itemIds) {
            if (itemId == AppContract.INVALID_ID) return false;
        }
        return true;
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
