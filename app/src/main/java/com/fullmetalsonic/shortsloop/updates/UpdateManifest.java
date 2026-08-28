package com.fullmetalsonic.shortsloop.updates;

import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import java.io.IOException;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/** Immutable release data. APK identity/signature verification is a separate installation step. */
public final class UpdateManifest {
    public static final String MANIFEST_NAME = "shorts-loop-update.json";
    public final String packageName, versionName, apkName, apkUrl, sha256, releaseUrl;
    public final long versionCode, apkSize;
    public final int minSdk;

    public UpdateManifest(String packageName, long versionCode, String versionName, int minSdk,
            String apkName, long apkSize, String apkUrl, String sha256, String releaseUrl) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.minSdk = minSdk;
        this.apkName = apkName;
        this.apkSize = apkSize;
        this.apkUrl = apkUrl;
        this.sha256 = sha256;
        this.releaseUrl = releaseUrl;
    }

    /** Only URLs taken from the SAME release's assets may supply the manifest and APK. */
    public static UpdateManifest parse(String json, String tag, String releaseUrl,
            Map<String, Asset> assets) throws IOException {
        try {
            Object decoded = decode(json);
            if (!(decoded instanceof JSONObject)) throw invalid();
            JSONObject object = (JSONObject) decoded;
            if (integer(object, "schema") != 1) throw invalid();
            String name = string(object, "packageName");
            String version = string(object, "versionName");
            String apkName = string(object, "apkName");
            String hash = string(object, "sha256");
            long code = integer(object, "versionCode"), size = integer(object, "apkSize");
            long minimum = integer(object, "minSdk");
            if (!UpdatePolicy.PACKAGE.equals(name) || !UpdatePolicy.validVersionName(version)
                    || !("v" + version).equals(tag) || code <= 0 || code > Integer.MAX_VALUE
                    || minimum < 26 || minimum > Integer.MAX_VALUE
                    || !UpdatePolicy.validAssetName(apkName) || !apkName.endsWith(".apk")
                    || !UpdatePolicy.validSha256(hash) || size <= 0 || size > UpdatePolicy.MAX_APK_BYTES
                    || !releasePage(tag).equals(releaseUrl) || assets == null) throw invalid();
            Asset descriptor = assets.get(MANIFEST_NAME), apk = assets.get(apkName);
            if (!validAsset(descriptor, tag) || !MANIFEST_NAME.equals(descriptor.name) || descriptor.size <= 0
                    || descriptor.size > UpdatePolicy.MAX_MANIFEST_BYTES
                    || !validAsset(apk, tag) || !apkName.equals(apk.name) || apk.size != size) throw invalid();
            return new UpdateManifest(name, code, version, (int) minimum, apkName, size,
                    apk.url, hash.toLowerCase(java.util.Locale.ROOT), releaseUrl);
        } catch (JSONException exception) {
            // Neither release bodies nor server-provided URLs belong in user-facing errors/logs.
            throw invalid();
        }
    }

    public static final class Asset {
        public final String name, url;
        public final long size;
        public final boolean uploaded;
        public Asset(String name, String url, long size) { this(name, url, size, true); }
        public Asset(String name, String url, long size, boolean uploaded) {
            this.name = name; this.url = url; this.size = size; this.uploaded = uploaded;
        }
    }

    static String releasePage(String tag) {
        return "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/tag/" + tag;
    }

    static boolean validAsset(Asset asset, String tag) {
        return asset != null && asset.uploaded && UpdatePolicy.validAssetName(asset.name)
                && UpdatePolicy.trustedReleaseUrl(asset.url)
                && ("https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/download/"
                    + tag + "/" + asset.name).equals(asset.url);
    }

    static Object decode(String text) throws JSONException, IOException {
        if (text == null) throw invalid();
        JSONTokener parser = new JSONTokener(text);
        Object value = parser.nextValue();
        if (parser.nextClean() != 0) throw invalid();
        return value;
    }

    static String string(JSONObject object, String key) throws JSONException, IOException {
        Object value = object.get(key);
        if (!(value instanceof String)) throw invalid();
        return (String) value;
    }

    static long integer(JSONObject object, String key) throws JSONException, IOException {
        Object value = object.get(key);
        // getLong coerces strings and fractional numbers; update metadata must not do that.
        if (!(value instanceof Integer) && !(value instanceof Long)) throw invalid();
        return ((Number) value).longValue();
    }

    static IOException invalid() { return new UpdateFailure(UpdateFailure.Code.INVALID_MANIFEST); }
}
