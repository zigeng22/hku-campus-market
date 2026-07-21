package com.example.a7506_project.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import java.io.InputStream;

public final class ImageUriLoader {
    private ImageUriLoader() {
    }

    public static boolean persistReadPermission(Context context, Uri uri) {
        if (uri == null) {
            return false;
        }
        try {
            context.getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return true;
        } catch (SecurityException | IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean loadOrShowPlaceholder(
            Context context,
            ImageView imageView,
            String uriString,
            @DrawableRes int placeholderResource) {
        imageView.setImageResource(placeholderResource);
        if (uriString == null || uriString.trim().isEmpty()) {
            return false;
        }

        try {
            Uri uri = Uri.parse(uriString);
            try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
                if (stream == null) {
                    return false;
                }
            }
            imageView.setImageURI(uri);
            if (imageView.getDrawable() == null) {
                imageView.setImageResource(placeholderResource);
                return false;
            }
            return true;
        } catch (Exception exception) {
            imageView.setImageResource(placeholderResource);
            return false;
        }
    }
}
