package com.android.wallet.viewmodel;


import androidx.annotation.Nullable;

public class ErrorEnvelope {
    public final int code;
    @Nullable
    public final String message;
    @Nullable
    private final Throwable throwable;

    public ErrorEnvelope(@Nullable String message) {
        this(Constants.ErrorCode.UNKNOWN, message);
    }

    public ErrorEnvelope(int code, @Nullable String message) {
        this(code, message, null);
    }

    public ErrorEnvelope(int code, @Nullable String message, @Nullable Throwable throwable) {
        this.code = code;
        this.message = message;
        this.throwable = throwable;
    }
}
