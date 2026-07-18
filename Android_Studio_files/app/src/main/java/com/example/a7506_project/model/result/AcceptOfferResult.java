package com.example.a7506_project.model.result;

import com.example.a7506_project.contract.AppContract;

public final class AcceptOfferResult {
    private final boolean success;
    private final RepositoryResultCode code;
    private final long transactionId;

    private AcceptOfferResult(boolean success, RepositoryResultCode code, long transactionId) {
        this.success = success;
        this.code = code;
        this.transactionId = transactionId;
    }

    public static AcceptOfferResult success(long transactionId) {
        return new AcceptOfferResult(true, RepositoryResultCode.OK, transactionId);
    }

    public static AcceptOfferResult failure(RepositoryResultCode code) {
        return new AcceptOfferResult(false, code, AppContract.INVALID_ID);
    }

    public boolean isSuccess() { return success; }
    public RepositoryResultCode getCode() { return code; }
    public long getTransactionId() { return transactionId; }
}
