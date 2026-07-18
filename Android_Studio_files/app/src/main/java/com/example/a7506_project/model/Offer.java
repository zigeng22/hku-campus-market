package com.example.a7506_project.model;

public final class Offer {
    private final long id;
    private final long itemId;
    private final long buyerId;
    private final long amountCents;
    private final String type;
    private final String status;
    private final long createdAt;
    private final long updatedAt;

    public Offer(long id, long itemId, long buyerId, long amountCents, String type,
                 String status, long createdAt, long updatedAt) {
        this.id = id;
        this.itemId = itemId;
        this.buyerId = buyerId;
        this.amountCents = amountCents;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public long getItemId() { return itemId; }
    public long getBuyerId() { return buyerId; }
    public long getAmountCents() { return amountCents; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
}
