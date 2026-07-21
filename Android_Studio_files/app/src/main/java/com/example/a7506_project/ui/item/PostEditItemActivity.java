package com.example.a7506_project.ui.item;

import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a7506_project.R;
import com.example.a7506_project.contract.AppContract;
import com.example.a7506_project.data.MarketRepository;
import com.example.a7506_project.data.RepositoryProvider;
import com.example.a7506_project.model.Item;
import com.example.a7506_project.model.ItemDraft;
import com.example.a7506_project.util.ImageUriLoader;
import com.example.a7506_project.util.MoneyFormatter;
import com.example.a7506_project.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class PostEditItemActivity extends AppCompatActivity {

    private MarketRepository repo;
    private SessionManager session;
    private boolean editMode;
    private long itemId;

    private TextInputEditText inputName, inputDescription, inputPrice;
    private AutoCompleteTextView dropdownCategory;
    private ImageView imagePreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    boolean permissionPersisted = ImageUriLoader.persistReadPermission(this, uri);
                    boolean imageLoaded = permissionPersisted
                            && ImageUriLoader.loadOrShowPlaceholder(
                            this, imagePreview, uri.toString(), R.drawable.ic_item_placeholder);
                    if (imageLoaded) {
                        selectedImageUri = uri;
                    } else {
                        selectedImageUri = null;
                        Toast.makeText(this, R.string.image_unavailable, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit_item);

        repo = RepositoryProvider.get(this);
        session = new SessionManager(this);

        editMode = getIntent().getBooleanExtra(AppContract.EXTRA_EDIT_MODE, false);
        itemId = getIntent().getLongExtra(AppContract.EXTRA_ITEM_ID, AppContract.INVALID_ID);

        TextView title = findViewById(R.id.textPostEditTitle);
        title.setText(editMode ? R.string.edit_item_title : R.string.post_item_title);

        imagePreview = findViewById(R.id.imagePreview);
        inputName = findViewById(R.id.inputItemName);
        inputDescription = findViewById(R.id.inputDescription);
        inputPrice = findViewById(R.id.inputPrice);

        String[] categories = getResources().getStringArray(R.array.item_categories);
        dropdownCategory = findViewById(R.id.dropdownCategory);
        dropdownCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));

        findViewById(R.id.buttonChooseImage).setOnClickListener(view ->
                imagePickerLauncher.launch(new String[]{"image/*"}));

        findViewById(R.id.buttonSaveItem).setOnClickListener(view -> saveItem());

        if (editMode && itemId != AppContract.INVALID_ID) {
            loadExistingItem();
        }
    }

    private void loadExistingItem() {
        Item item = repo.getItemById(itemId);
        if (item == null) {
            Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (item.getSellerId() != session.getCurrentUserId()) {
            Toast.makeText(this, "You can only edit your own items.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        inputName.setText(item.getName());
        inputDescription.setText(item.getDescription());
        inputPrice.setText(String.valueOf(item.getPriceCents() / 100.0));
        dropdownCategory.setText(item.getCategory(), false);
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            boolean imageLoaded = ImageUriLoader.loadOrShowPlaceholder(
                    this, imagePreview, item.getImageUri(), R.drawable.ic_item_placeholder);
            selectedImageUri = imageLoaded ? Uri.parse(item.getImageUri()) : null;
        }
    }

    private void saveItem() {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String priceText = inputPrice.getText().toString().trim();
        String category = dropdownCategory.getText().toString();

        if (name.isEmpty()) {
            inputName.setError("Item name is required");
            return;
        }
        if (priceText.isEmpty()) {
            inputPrice.setError("Price is required");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            inputPrice.setError("Invalid price");
            return;
        }
        if (price <= 0) {
            inputPrice.setError("Price must be > 0");
            return;
        }
        long priceCents = MoneyFormatter.hkdToCents(price);

        if (category.isEmpty()) {
            dropdownCategory.setError("Select a category");
            return;
        }

        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : null;

        // Map category display name to category constant
        String categoryCode = mapCategory(category);

        ItemDraft draft = new ItemDraft(name, description, priceCents, imageUriStr, categoryCode);
        long userId = session.getCurrentUserId();

        if (editMode) {
            boolean ok = repo.updateItem(itemId, userId, draft);
            if (ok) {
                Toast.makeText(this, "Item updated.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed. Only active items you own can be edited.", Toast.LENGTH_SHORT).show();
            }
        } else {
            long newId = repo.createItem(userId, draft);
            if (newId != AppContract.INVALID_ID) {
                Toast.makeText(this, "Item posted!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to post item.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String mapCategory(String displayName) {
        if (displayName.equals(getString(R.string.category_books))) return AppContract.CATEGORY_BOOKS;
        if (displayName.equals(getString(R.string.category_electronics))) return AppContract.CATEGORY_ELECTRONICS;
        if (displayName.equals(getString(R.string.category_furniture))) return AppContract.CATEGORY_FURNITURE;
        if (displayName.equals(getString(R.string.category_daily_goods))) return AppContract.CATEGORY_DAILY_GOODS;
        return AppContract.CATEGORY_OTHERS;
    }
}
