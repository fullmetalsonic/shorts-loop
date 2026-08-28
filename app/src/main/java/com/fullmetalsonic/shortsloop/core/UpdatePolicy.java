package com.fullmetalsonic.shortsloop.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;

/** Validation only: no network, Android dependency, or automatic installation. */
public final class UpdatePolicy {
    public static final String REPOSITORY = "fullmetalsonic/shorts-loop";
    public static final String PACKAGE = "com.fullmetalsonic.shortsloop";
    public static final int MAX_APK_BYTES = 40 * 1024 * 1024;
    public static final int MAX_MANIFEST_BYTES = 16_384;

    private static final long CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000;
    private static final String RELEASE_PATH = "/" + REPOSITORY + "/releases/download/";
    private static final Pattern VERSION = Pattern.compile(
            "(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)");
    private static final Pattern SHA256 = Pattern.compile("[A-Fa-f0-9]{64}");
    private static final Pattern ASSET = Pattern.compile("[A-Za-z0-9._-]+");

    private UpdatePolicy() { }

    public static boolean validVersionName(String version) {
        return version != null && VERSION.matcher(version).matches();
    }

    public static boolean isNewer(long installedCode, long candidateCode) {
        return installedCode > 0 && candidateCode > installedCode
                && candidateCode <= Integer.MAX_VALUE;
    }

    public static boolean compatible(int sdk, int minSdk) {
        return minSdk >= 26 && sdk >= minSdk;
    }

    public static boolean validSha256(String hash) {
        return hash != null && SHA256.matcher(hash).matches();
    }

    public static boolean validAssetName(String name) {
        return name != null && !name.equals(".") && !name.equals("..")
                && ASSET.matcher(name).matches();
    }

    public static boolean trustedReleaseUrl(String url) {
        URI uri = secureUri(url);
        if (uri == null || !"github.com".equalsIgnoreCase(uri.getHost())
                || uri.getRawQuery() != null) return false;
        String path = uri.getRawPath();
        // Do not decode first: encoded separators/dot segments must never become trusted.
        if (path == null || path.indexOf('%') >= 0 || path.indexOf('\\') >= 0
                || !path.startsWith(RELEASE_PATH)) return false;
        String tail = path.substring(RELEASE_PATH.length());
        int separator = tail.indexOf('/');
        if (separator < 2) return false;
        String tag = tail.substring(0, separator);
        String asset = tail.substring(separator + 1);
        return tag.charAt(0) == 'v' && validVersionName(tag.substring(1))
                && validAssetName(asset);
    }

    public static boolean trustedRedirectUrl(String url) {
        URI uri = secureUri(url);
        if (uri == null) return false;
        String host = uri.getHost();
        return "release-assets.githubusercontent.com".equalsIgnoreCase(host)
                || "objects.githubusercontent.com".equalsIgnoreCase(host)
                || trustedReleaseUrl(url);
    }

    private static URI secureUri(String url) {
        if (url == null) return null;
        try {
            URI uri = new URI(url);
            return !uri.isOpaque() && "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null && uri.getRawUserInfo() == null
                    && (uri.getPort() == -1 || uri.getPort() == 443)
                    && uri.getRawFragment() == null ? uri : null;
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    public static boolean shouldCheck(long nowMs, long lastAttemptMs) {
        if (lastAttemptMs == 0 || nowMs < lastAttemptMs) return true;
        // Comparing a bounded sum avoids overflowing nowMs - lastAttemptMs.
        return lastAttemptMs <= Long.MAX_VALUE - CHECK_INTERVAL_MS
                && nowMs >= lastAttemptMs + CHECK_INTERVAL_MS;
    }

    public static String sha256(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            char[] hex = new char[digest.length * 2];
            char[] alphabet = "0123456789abcdef".toCharArray();
            for (int i = 0; i < digest.length; i++) {
                hex[i * 2] = alphabet[(digest[i] & 0xff) >>> 4];
                hex[i * 2 + 1] = alphabet[digest[i] & 0x0f];
            }
            return new String(hex);
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError("SHA-256 is required by Java", impossible);
        }
    }
}
