package com.google.androidbrowserhelper.trusted;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

public class QualityEnforcementCallbackHandler {
    private static final  String TAG = "TwaQualityEnforcement";

    @VisibleForTesting
    static final String CRASH = "quality_enforcement.crash";
    @VisibleForTesting
    static final String NOTIFY = "quality_enforcement.notify";
    @VisibleForTesting
    static final String KEY_CRASH_REASON = "crash_reason";
    static final String KEY_SUCCESS = "success";

    @Nullable
    public static Bundle extraCallbackWithResult(
            @NonNull String callbackName, @Nullable Bundle args) {
        Bundle result = new Bundle();
        result.putBoolean(KEY_SUCCESS, false);
        if (callbackName.equals(NOTIFY)) {
            String message = (args != null) ? args.getString(KEY_CRASH_REASON) : "";
            Log.e(TAG, message);
            result.putBoolean(KEY_SUCCESS, true);
        } else if (callbackName.equals(CRASH)) {
            String message = (args != null) ? args.getString(KEY_CRASH_REASON) : "";
            throw new RuntimeException(message);
        }
        return result;
    }
};
