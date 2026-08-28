package com.fullmetalsonic.shortsloop.updates;

import java.io.IOException;

/** Android-independent, non-sensitive error codes. Never contains a URL, path or server response. */
public final class UpdateFailure extends IOException {
    private static final long serialVersionUID = 1L;
    public enum Code {
        UNTRUSTED_URL, TOO_MANY_REDIRECTS, INVALID_REDIRECT, UNTRUSTED_REDIRECT,
        SERVER_RESPONSE, TRANSFER_FORMAT, CONNECTION_CLOSE, CONNECTION, TIMEOUT, CANCELLED,
        STORAGE, DOWNLOAD_BUSY, STORAGE_PREPARE, SIZE_MISMATCH, INTEGRITY, DOWNLOAD,
        STORAGE_PATH, MANIFEST_SIZE, HASH_UNAVAILABLE, PROCESS_TIMEOUT, INVALID_MANIFEST,
        FILE_INFO, INCOMPATIBLE, SIGNATURE, INSTALLED_INFO, APK_INFO
    }
    public final Code code;
    public UpdateFailure(Code code) { super("UPDATE_" + code.name()); this.code = code; }
}
