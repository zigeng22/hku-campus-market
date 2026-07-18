package com.example.a7506_project.model;

public final class TradeTransaction {
    private final long id;
    private final long itemId;
    private final long sellerId;
    private final long buyerId;
    private final long offerId;
    private final long finalPriceCents;
    private final long createdAt;

    public TradeTransaction(long id, long itemId, long sellerId, long buyerId, long offerId,
                            long finalPriceCents, long createdAt) {
        this.id = id;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.offerId = offerId;
        this.finalPriceCents = finalPriceCents;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getItemId() { return itemId; }
    public long getSellerId() { return sellerId; }
    public long getBuyerId() { return buyerId; }
    public long getOfferId() { return offerId; }
    public long getFinalPriceCents() { return finalPriceCents; }
    public long getCreatedAt() { return createdAt; }
}
