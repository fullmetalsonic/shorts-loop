package com.fullmetalsonic.shortsloop.updates;

import android.content.Context;
import com.fullmetalsonic.shortsloop.R;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

/** Render known codes with the caller's localized context. Never expose exception messages or causes. */
public final class UpdateMessages {
    private UpdateMessages() {}
    public static String failure(Context context, int fallbackResource, Throwable error) {
        int detail = detailResource(error);
        String fallback = context.getString(fallbackResource);
        return detail == 0 ? fallback : fallback + "\n" + context.getString(detail);
    }
    private static int detailResource(Throwable error) {
        if (error instanceof SocketTimeoutException) return R.string.updates_error_timeout;
        if (error instanceof InterruptedIOException) return R.string.updates_error_cancelled;
        if (!(error instanceof UpdateFailure)) return 0;
        switch (((UpdateFailure) error).code) {
            case UNTRUSTED_URL: case TOO_MANY_REDIRECTS: case INVALID_REDIRECT: case UNTRUSTED_REDIRECT:
                return R.string.updates_error_address;
            case SERVER_RESPONSE: case TRANSFER_FORMAT: case CONNECTION_CLOSE: case CONNECTION:
                return R.string.updates_error_connection;
            case TIMEOUT: case PROCESS_TIMEOUT: return R.string.updates_error_timeout;
            case CANCELLED: return R.string.updates_error_cancelled;
            case STORAGE: case STORAGE_PREPARE: case STORAGE_PATH: return R.string.updates_error_storage;
            case DOWNLOAD_BUSY: return R.string.updates_error_busy;
            case SIZE_MISMATCH: case FILE_INFO: return R.string.updates_error_file_info;
            case INTEGRITY: return R.string.updates_error_integrity;
            case DOWNLOAD: return R.string.updates_error_download;
            case MANIFEST_SIZE: case INVALID_MANIFEST: return R.string.updates_error_manifest;
            case HASH_UNAVAILABLE: case INSTALLED_INFO: case APK_INFO: return R.string.updates_error_verification;
            case INCOMPATIBLE: return R.string.updates_error_incompatible;
            case SIGNATURE: return R.string.updates_error_signature;
            default: return 0;
        }
    }
}
