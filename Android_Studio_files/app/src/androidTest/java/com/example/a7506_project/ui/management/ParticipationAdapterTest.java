package com.example.a7506_project.ui.management;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.View;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.R;
import com.example.a7506_project.model.ParticipationSummary;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class ParticipationAdapterTest {

    @Test
    public void missingWhatsappIsClearedAndHiddenWhenViewHolderIsReused() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AtomicReference<AssertionError> failure = new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            try {
                ParticipationAdapter adapter = new ParticipationAdapter();
                Context themedContext = new ContextThemeWrapper(
                        context, R.style.Theme__7506_project);
                FrameLayout parent = new FrameLayout(themedContext);
                ParticipationAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

                adapter.setItems(Collections.singletonList(new ParticipationSummary(
                        1, "Java textbook", 12000, "ACCEPTED", "CONFIRMED", "91234567")));
                adapter.onBindViewHolder(holder, 0);
                assertEquals(View.VISIBLE, holder.whatsapp.getVisibility());

                adapter.setItems(Collections.singletonList(new ParticipationSummary(
                        2, "Desk lamp", 7000, "PENDING", "PENDING", null)));
                adapter.onBindViewHolder(holder, 0);
                assertEquals(View.GONE, holder.whatsapp.getVisibility());
                assertEquals("", holder.whatsapp.getText().toString());
            } catch (AssertionError error) {
                failure.set(error);
            }
        });

        if (failure.get() != null) {
            throw failure.get();
        }
    }
}
