package com.example.a7506_project.model;

public final class ItemCard {
    private final long itemId;
    private final String name;
    private final long priceCents;
    private final String imageUri;
    private final String category;
    private final String sellerNickname;
    private final String status;
    private final int offerCount;

    public ItemCard(long itemId, String name, long priceCents, String imageUri,
                    String category, String sellerNickname, String status, int offerCount) {
        this.itemId = itemId;
        this.name = name;
        this.priceCents = priceCents;
        this.imageUri = imageUri;
        this.category = category;
        this.sellerNickname = sellerNickname;
        this.status = status;
        this.offerCount = offerCount;
    }

    public long getItemId() { return itemId; }
    public String getName() { return name; }
    public long getPriceCents() { return priceCents; }
    public String getImageUri() { return imageUri; }
    public String getCategory() { return category; }
    public String getSellerNickname() { return sellerNickname; }
    public String getStatus() { return status; }
    public int getOfferCount() { return offerCount; }
}
