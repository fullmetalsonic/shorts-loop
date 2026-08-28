package com.fullmetalsonic.shortsloop.updates;

import android.content.Context;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

/** Resource-only locale/privacy checks; no network, preferences, files or installer actions. */
public final class UpdateMessageChecks {
    private static final String PRIVATE_DETAIL = "private-account@example.invalid /private/update/path https://example.invalid/?secret=hidden";
    private UpdateMessageChecks() {}
    public static int run(Context context) {
        int checks = 0;
        for (String language : new String[]{"en", "ko"}) {
            Context localized = AppLocale.forLanguage(context, language);
            String fallback = localized.getString(R.string.updates_download_failed);
            for (UpdateFailure.Code code : UpdateFailure.Code.values()) {
                UpdateFailure failure = new UpdateFailure(code);
                failure.initCause(new IOException(PRIVATE_DETAIL));
                failure.addSuppressed(new IOException(PRIVATE_DETAIL));
                String actual = UpdateMessages.failure(localized, R.string.updates_download_failed, failure);
                require(actual.equals(fallback + "\n" + localized.getString(resource(code))), "Known update code maps to exact localized detail: " + code); checks++;
                require(isRequestedLanguage(actual, language), "Known update code uses selected language: " + code); checks++;
                require(noPrivateDetail(actual) && !actual.contains("UPDATE_"), "Known error exposes neither code nor private cause: " + code); checks++;
            }
            SocketTimeoutException timeout = new SocketTimeoutException(PRIVATE_DETAIL);
            String timeoutText = UpdateMessages.failure(localized, R.string.updates_download_failed, timeout);
            require(timeoutText.equals(fallback + "\n" + localized.getString(R.string.updates_error_timeout)),
                    "Socket timeout wins over its InterruptedIOException base type"); checks++;
            require(noPrivateDetail(timeoutText), "Timeout message details never reach UI"); checks++;
            String cancelled = UpdateMessages.failure(localized, R.string.updates_download_failed, new InterruptedIOException(PRIVATE_DETAIL));
            require(cancelled.equals(fallback + "\n" + localized.getString(R.string.updates_error_cancelled)), "Cancellation has localized detail"); checks++;
            require(noPrivateDetail(cancelled), "Cancellation private message never reaches UI"); checks++;
            for (Throwable unknown : new Throwable[]{null, new IOException(PRIVATE_DETAIL),
                    new RuntimeException(PRIVATE_DETAIL, new UpdateFailure(UpdateFailure.Code.SIGNATURE)),
                    new IOException() {
                        @Override public String getMessage() { throw new AssertionError("Raw exception message must not be read"); }
                        @Override public String getLocalizedMessage() { throw new AssertionError("Raw localized message must not be read"); }
                    }}) {
                String actual = UpdateMessages.failure(localized, R.string.updates_download_failed, unknown);
                require(actual.equals(fallback), "Unknown/null errors show only localized generic fallback"); checks++;
                require(noPrivateDetail(actual) && isRequestedLanguage(actual, language), "Generic fallback preserves locale and privacy"); checks++;
            }
            require(localized.getString(R.string.updates_version_available, "9.8.7").contains("9.8.7"), "Version placeholder formats in selected locale"); checks++;
            require(isRequestedLanguage(localized.getString(R.string.updates_install_permission_body), language), "Installer guidance uses selected locale"); checks++;
            require(isRequestedLanguage(localized.getString(R.string.updates_install_action), language)
                    && isRequestedLanguage(localized.getString(R.string.updates_download_action), language), "Update action labels use selected locale"); checks++;
        }
        return checks;
    }
    private static boolean noPrivateDetail(String value) {
        return !value.contains("private-account") && !value.contains("/private/")
                && !value.contains("example.invalid") && !value.contains("secret=hidden");
    }
    private static boolean isRequestedLanguage(String value, String language) {
        boolean korean = value.matches("(?s).*[가-힣].*");
        return "ko".equals(language) == korean;
    }
    private static int resource(UpdateFailure.Code code) {
        switch (code) {
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
            default: throw new AssertionError("Add a locale expectation for new code " + code);
        }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
