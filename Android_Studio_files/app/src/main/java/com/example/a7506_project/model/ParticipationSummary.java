package com.example.a7506_project.model;

public final class ParticipationSummary {
    private final long itemId;
    private final String itemName;
    private final long offerAmountCents;
    private final String offerStatus;
    private final String dealStatus;
    private final String counterpartyWhatsapp;

    public ParticipationSummary(long itemId, String itemName, long offerAmountCents,
                                String offerStatus, String dealStatus, String counterpartyWhatsapp) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.offerAmountCents = offerAmountCents;
        this.offerStatus = offerStatus;
        this.dealStatus = dealStatus;
        this.counterpartyWhatsapp = counterpartyWhatsapp;
    }

    public long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public long getOfferAmountCents() { return offerAmountCents; }
    public String getOfferStatus() { return offerStatus; }
    public String getDealStatus() { return dealStatus; }
    public String getCounterpartyWhatsapp() { return counterpartyWhatsapp; }
}
