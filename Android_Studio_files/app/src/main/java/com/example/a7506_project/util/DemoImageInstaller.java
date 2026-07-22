package com.example.a7506_project.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DemoImageInstaller {
    private static final String ASSET_DIRECTORY = "demo_products/";
    private static final String GALLERY_DIRECTORY = "Pictures/HKU Campus Market/";
    private static final String PREFERENCES_NAME = "demo_image_uris";

    private DemoImageInstaller() {
    }

    public static Map<String, String> install(Context context, String... assetNames) {
        Map<String, String> uris = new LinkedHashMap<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return uris;
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        for (String assetName : assetNames) {
            Uri uri = readableStoredUri(
                    context.getContentResolver(), preferences.getString(assetName, null));
            if (uri == null) {
                uri = findExisting(context.getContentResolver(), assetName);
            }
            if (uri == null) {
                uri = copyToGallery(context, assetName);
            }
            if (uri != null) {
                uris.put(assetName, uri.toString());
                preferences.edit().putString(assetName, uri.toString()).apply();
            }
        }
        return uris;
    }

    private static Uri readableStoredUri(ContentResolver resolver, String storedUri) {
        if (storedUri == null || storedUri.isEmpty()) {
            return null;
        }
        Uri uri = Uri.parse(storedUri);
        try (InputStream ignored = resolver.openInputStream(uri)) {
            return ignored == null ? null : uri;
        } catch (Exception exception) {
            return null;
        }
    }

    private static Uri findExisting(ContentResolver resolver, String assetName) {
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DISPLAY_NAME + " = ? AND "
                        + MediaStore.Images.Media.RELATIVE_PATH + " = ?",
                new String[]{assetName, GALLERY_DIRECTORY},
                null);
        if (cursor == null) {
            return null;
        }
        Uri result = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            result = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    String.valueOf(id));
        }
        cursor.close();
        return result;
    }

    private static Uri copyToGallery(Context context, String assetName) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, assetName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, GALLERY_DIRECTORY);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            return null;
        }
        try (InputStream input = context.getAssets().open(ASSET_DIRECTORY + assetName);
             OutputStream output = resolver.openOutputStream(uri)) {
            if (output == null) {
                resolver.delete(uri, null, null);
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            ContentValues published = new ContentValues();
            published.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(uri, published, null, null);
            return uri;
        } catch (Exception exception) {
            resolver.delete(uri, null, null);
            return null;
        }
    }
}
