package com.example.a7506_project.model.result;

import com.example.a7506_project.contract.AppContract;

public final class RegistrationResult {
    private final boolean success;
    private final RepositoryResultCode code;
    private final long userId;

    private RegistrationResult(boolean success, RepositoryResultCode code, long userId) {
        this.success = success;
        this.code = code;
        this.userId = userId;
    }

    public static RegistrationResult success(long userId) {
        return new RegistrationResult(true, RepositoryResultCode.OK, userId);
    }

    public static RegistrationResult failure(RepositoryResultCode code) {
        return new RegistrationResult(false, code, AppContract.INVALID_ID);
    }

    public boolean isSuccess() { return success; }
    public RepositoryResultCode getCode() { return code; }
    public long getUserId() { return userId; }
}
