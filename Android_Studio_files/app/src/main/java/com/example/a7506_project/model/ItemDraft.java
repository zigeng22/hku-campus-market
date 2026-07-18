package com.example.a7506_project.model;

public final class ItemDraft {
    private final String name;
    private final String description;
    private final long priceCents;
    private final String imageUri;
    private final String category;

    public ItemDraft(String name, String description, long priceCents, String imageUri, String category) {
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUri = imageUri;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPriceCents() { return priceCents; }
    public String getImageUri() { return imageUri; }
    public String getCategory() { return category; }
}
