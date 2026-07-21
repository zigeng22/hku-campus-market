package com.example.a7506_project.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.a7506_project.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ImageUriLoaderTest {
    private Context context;
    private ImageView imageView;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        imageView = new ImageView(context);
    }

    @Test
    public void emptyUriUsesPlaceholder() {
        boolean loaded = ImageUriLoader.loadOrShowPlaceholder(
                context, imageView, null, R.drawable.ic_item_placeholder);

        assertFalse(loaded);
        assertNotNull(imageView.getDrawable());
    }

    @Test
    public void unreadableUriUsesPlaceholderWithoutThrowing() {
        boolean loaded = ImageUriLoader.loadOrShowPlaceholder(
                context,
                imageView,
                "content://com.example.missing.provider/not-found",
                R.drawable.ic_item_placeholder);

        assertFalse(loaded);
        assertNotNull(imageView.getDrawable());
    }

    @Test
    public void readableAndroidResourceUriLoadsImage() {
        Uri uri = new Uri.Builder()
                .scheme("android.resource")
                .authority(context.getPackageName())
                .appendPath("drawable")
                .appendPath("ic_item_placeholder")
                .build();

        boolean loaded = ImageUriLoader.loadOrShowPlaceholder(
                context, imageView, uri.toString(), R.drawable.ic_item_placeholder);

        assertTrue(loaded);
        assertNotNull(imageView.getDrawable());
    }
}
