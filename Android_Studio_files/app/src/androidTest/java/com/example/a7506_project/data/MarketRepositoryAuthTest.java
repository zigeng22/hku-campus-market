package com.example.a7506_project.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.model.User;
import com.example.a7506_project.model.result.RegistrationResult;
import com.example.a7506_project.model.result.RepositoryResultCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MarketRepositoryAuthTest {
    private static final String TEST_DATABASE = "auth_repository_test.db";

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
    public void registeredUserCanAuthenticateAndBeLoaded() {
        RegistrationResult registration = repository.registerUser(
                "Alice_01", "secret12", "91234567");

        assertTrue(registration.isSuccess());
        User authenticated = repository.authenticate("  alice_01 ", "secret12");
        assertEquals(registration.getUserId(), authenticated.getId());
        assertEquals("Alice_01", authenticated.getNickname());
        assertEquals("91234567", authenticated.getWhatsapp());
        assertEquals(authenticated.getId(), repository.getUserById(authenticated.getId()).getId());
    }

    @Test
    public void duplicateNicknameIsRejectedIgnoringCase() {
        assertTrue(repository.registerUser("Alice", "secret12", "91234567").isSuccess());

        RegistrationResult duplicate = repository.registerUser(
                "alice", "another12", "92345678");

        assertFalse(duplicate.isSuccess());
        assertEquals(RepositoryResultCode.DUPLICATE_NICKNAME, duplicate.getCode());
    }

    @Test
    public void wrongCredentialsDoNotAuthenticate() {
        assertTrue(repository.registerUser("Alice", "secret12", "91234567").isSuccess());

        assertNull(repository.authenticate("Alice", "wrong12"));
        assertNull(repository.authenticate("Unknown", "secret12"));
        assertNull(repository.authenticate(null, "secret12"));
    }

    @Test
    public void commonInvalidRegistrationInputsAreRejected() {
        assertInvalid(repository.registerUser("a", "secret12", "91234567"));
        assertInvalid(repository.registerUser("Alice", "123", "91234567"));
        assertInvalid(repository.registerUser("Alice", "secret12", "abc12345678"));
        assertInvalid(repository.registerUser("Alice", "secret12", "1234"));
    }

    private void assertInvalid(RegistrationResult result) {
        assertFalse(result.isSuccess());
        assertEquals(RepositoryResultCode.INVALID_INPUT, result.getCode());
    }
}
