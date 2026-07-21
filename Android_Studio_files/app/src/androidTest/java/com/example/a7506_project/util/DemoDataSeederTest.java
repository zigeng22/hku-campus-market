package com.example.a7506_project.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

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
    public void prepareCreatesReusableAliceBobScenarioWithoutDuplicates() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MarketRepository repository = RepositoryProvider.get(context);

        assertTrue(DemoDataSeeder.prepare(context, repository));
        assertTrue(DemoDataSeeder.prepare(context, repository));

        User alice = repository.authenticate(
                DemoDataSeeder.ALICE_NICKNAME, DemoDataSeeder.DEMO_PASSWORD);
        User bob = repository.authenticate(
                DemoDataSeeder.BOB_NICKNAME, DemoDataSeeder.DEMO_PASSWORD);
        assertNotNull(alice);
        assertNotNull(bob);

        List<ItemCard> listings = repository.getListingsBySeller(alice.getId());
        assertEquals(2, listings.size());

        List<ParticipationSummary> activity = repository.getBuyerActivity(bob.getId());
        assertEquals(2, activity.size());
        assertTrue(activity.stream().anyMatch(item -> "CONFIRMED".equals(item.getDealStatus())));
        assertTrue(activity.stream().anyMatch(item -> "PENDING".equals(item.getOfferStatus())));
    }
}
