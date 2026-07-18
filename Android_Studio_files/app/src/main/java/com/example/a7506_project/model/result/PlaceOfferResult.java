package com.example.a7506_project.model.result;

import com.example.a7506_project.contract.AppContract;

public final class PlaceOfferResult {
    private final boolean success;
    private final RepositoryResultCode code;
    private final long offerId;

    private PlaceOfferResult(boolean success, RepositoryResultCode code, long offerId) {
        this.success = success;
        this.code = code;
        this.offerId = offerId;
    }

    public static PlaceOfferResult success(long offerId) {
        return new PlaceOfferResult(true, RepositoryResultCode.OK, offerId);
    }

    public static PlaceOfferResult failure(RepositoryResultCode code) {
        return new PlaceOfferResult(false, code, AppContract.INVALID_ID);
    }

    public boolean isSuccess() { return success; }
    public RepositoryResultCode getCode() { return code; }
    public long getOfferId() { return offerId; }
}
