package com.example.a7506_project.model;

public final class Item {
    private final long id;
    private final long sellerId;
    private final String name;
    private final String description;
    private final long priceCents;
    private final String imageUri;
    private final String category;
    private final String status;
    private final long createdAt;
    private final long updatedAt;

    public Item(long id, long sellerId, String name, String description, long priceCents,
                String imageUri, String category, String status, long createdAt, long updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUri = imageUri;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public long getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPriceCents() { return priceCents; }
    public String getImageUri() { return imageUri; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
}
