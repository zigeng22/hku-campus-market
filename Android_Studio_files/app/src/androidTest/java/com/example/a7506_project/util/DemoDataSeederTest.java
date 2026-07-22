package com.example.a7506_project.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.ItemCard;
import com.example.a7506_project.model.ParticipationSummary;
import com.example.a7506_project.model.User;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DemoDataSeederTest {

    @Test
    public void prepareCreatesReusableFourAccountMarketplace() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MarketRepository repository = RepositoryProvider.get(context);

        assertTrue(DemoDataSeeder.prepare(context, repository));
        assertTrue(DemoDataSeeder.prepare(context, repository));

        User alice = repository.authenticate(
                DemoDataSeeder.ALICE_NICKNAME, DemoDataSeeder.DEMO_PASSWORD);
        User bob = repository.authenticate(
                DemoDataSeeder.BOB_NICKNAME, DemoDataSeeder.DEMO_PASSWORD);
        User carol = repository.authenticate(
                DemoDataSeeder.CAROL_NICKNAME, DemoDataSeeder.DEMO_PASSWORD);
        User david = repository.authenticate(
                DemoDataSeeder.DAVID_NICKNAME, DemoDataSeeder.DEMO_PASSWORD);
        assertNotNull(alice);
        assertNotNull(bob);
        assertNotNull(carol);
        assertNotNull(david);

        assertDemoAccount(repository, alice);
        assertDemoAccount(repository, bob);
        assertDemoAccount(repository, carol);
        assertDemoAccount(repository, david);

        List<ParticipationSummary> bobActivity = repository.getBuyerActivity(bob.getId());
        assertTrue(bobActivity.stream().anyMatch(
                item -> "CONFIRMED".equals(item.getDealStatus())));
    }

    private void assertDemoAccount(MarketRepository repository, User user) {
        List<ItemCard> listings = repository.getListingsBySeller(user.getId());
        assertTrue(listings.size() >= 2);
        assertTrue(repository.getBuyerActivity(user.getId()).size() >= 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            assertTrue(listings.stream().allMatch(
                    item -> item.getImageUri() != null && !item.getImageUri().isEmpty()));
        }
    }
}
