package com.example.a7506_project.ui.item;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.model.OfferSummary;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.util.MoneyFormatter;
import com.example.a7506_project.util.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ItemDetailOfferFlowTest {
    private Context context;
    private MarketRepository repository;
    private long sellerId;
    private long buyerId;
    private long itemId;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = RepositoryProvider.get(context);

        String suffix = Long.toHexString(System.nanoTime());
        suffix = suffix.substring(Math.max(0, suffix.length() - 8));
        sellerId = register("Seller" + suffix, "91234567");
        buyerId = register("Buyer" + suffix, "92345678");
        itemId = repository.createItem(sellerId,
                new ItemDraft("Course book " + suffix, "Clean condition", 12345, null,
                        AppContract.CATEGORY_BOOKS));
        assertTrue(itemId != AppContract.INVALID_ID);
        new SessionManager(context).login(buyerId);
    }

    @Test
    public void buyNowShowsFixedPriceWithoutAmountInputAndStoresListPrice() {
        try (ActivityScenario<ItemDetailActivity> ignored = launchItemDetail()) {
            onView(withId(R.id.buttonBuyNow)).perform(click());

            String expectedMessage = context.getString(
                    R.string.buy_now_confirmation_message,
                    MoneyFormatter.centsToHkd(12345));
            onView(withText(expectedMessage)).check(matches(isDisplayed()));
            onView(withId(R.id.inputOfferAmount)).check(doesNotExist());

            onView(withId(android.R.id.button1)).perform(click());

            OfferSummary saved = onlyOffer();
            assertEquals(AppContract.OFFER_TYPE_BUY_NOW, saved.getType());
            assertEquals(12345, saved.getAmountCents());
        }
    }

    @Test
    public void makeOfferKeepsAmountInputAndStoresBuyerAmount() {
        try (ActivityScenario<ItemDetailActivity> ignored = launchItemDetail()) {
            onView(withId(R.id.buttonMakeOffer)).perform(click());
            onView(withId(R.id.inputOfferAmount)).check(matches(isDisplayed()));
            onView(withId(R.id.inputOfferAmount)).perform(replaceText("98.00"));
            onView(withId(android.R.id.button1)).perform(click());

            OfferSummary saved = onlyOffer();
            assertEquals(AppContract.OFFER_TYPE_NEGOTIATED, saved.getType());
            assertEquals(9800, saved.getAmountCents());
        }
    }

    private ActivityScenario<ItemDetailActivity> launchItemDetail() {
        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra(AppContract.EXTRA_ITEM_ID, itemId);
        return ActivityScenario.launch(intent);
    }

    private long register(String nickname, String whatsapp) {
        RegistrationResult result = repository.registerUser(nickname, "secret12", whatsapp);
        assertTrue(result.isSuccess());
        return result.getUserId();
    }

    private OfferSummary onlyOffer() {
        List<OfferSummary> offers = repository.getOffersForSellerItem(itemId, sellerId);
        assertEquals(1, offers.size());
        return offers.get(0);
    }
}
