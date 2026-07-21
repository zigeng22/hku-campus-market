package com.example.a7506_project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.util.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivitySessionRoutingTest {
    private Context context;
    private MarketRepository repository;
    private SessionManager sessionManager;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = RepositoryProvider.get(context);
        sessionManager = new SessionManager(context);
        sessionManager.logout();
    }

    @After
    public void tearDown() {
        sessionManager.logout();
    }

    @Test
    public void validSessionRoutesToHomeAndKeepsUserId() {
        String suffix = Long.toHexString(System.nanoTime());
        suffix = suffix.substring(Math.max(0, suffix.length() - 8));
        RegistrationResult registration = repository.registerUser(
                "Route" + suffix, "secret12", "91234567");
        assertTrue(registration.isSuccess());
        sessionManager.login(registration.getUserId());

        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.toolbarHome)).check(matches(isDisplayed()));
            assertEquals(registration.getUserId(), sessionManager.getCurrentUserId());
        }
    }

    @Test
    public void missingSessionUserIsClearedAndRoutesToLogin() {
        sessionManager.login(Long.MAX_VALUE);

        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.rootLogin)).check(matches(isDisplayed()));
            assertFalse(sessionManager.isLoggedIn());
            assertEquals(AppContract.INVALID_ID, sessionManager.getCurrentUserId());
        }
    }
}
