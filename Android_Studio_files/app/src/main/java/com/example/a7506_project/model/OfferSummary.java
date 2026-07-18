package com.example.a7506_project.model;

public final class OfferSummary {
    private final long offerId;
    private final long itemId;
    private final String buyerNickname;
    private final long buyerId;
    private final long amountCents;
    private final String type;
    private final String status;
    private final long createdAt;

    public OfferSummary(long offerId, long itemId, String buyerNickname, long buyerId,
                        long amountCents, String type, String status, long createdAt) {
        this.offerId = offerId;
        this.itemId = itemId;
        this.buyerNickname = buyerNickname;
        this.buyerId = buyerId;
        this.amountCents = amountCents;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getOfferId() { return offerId; }
    public long getItemId() { return itemId; }
    public String getBuyerNickname() { return buyerNickname; }
    public long getBuyerId() { return buyerId; }
    public long getAmountCents() { return amountCents; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
}
