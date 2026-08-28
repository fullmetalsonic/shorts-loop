package com.fullmetalsonic.shortsloop.updates;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

/** Preflight checks; Android's installer remains responsible for final APK signature verification. */
public final class ApkVerifier {
    private ApkVerifier() { }
    @SuppressWarnings("deprecation")
    public static void verify(Context context, File file, UpdateManifest item) throws IOException {
        if (item == null || !UpdatePolicy.PACKAGE.equals(item.packageName)
                || !UpdatePolicy.compatible(Build.VERSION.SDK_INT, item.minSdk)
                || !UpdatePolicy.validSha256(item.sha256) || !UpdatePolicy.trustedReleaseUrl(item.apkUrl)
                || item.apkSize <= 0 || item.apkSize > UpdatePolicy.MAX_APK_BYTES
                || !file.isFile() || file.length() != item.apkSize) throw new UpdateFailure(UpdateFailure.Code.FILE_INFO);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream input = new FileInputStream(file)) {
                byte[] buffer = new byte[32768]; int count;
                while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
            }
            if (!hex(digest.digest()).equalsIgnoreCase(item.sha256)) throw new UpdateFailure(UpdateFailure.Code.INTEGRITY);
            PackageManager manager = context.getPackageManager();
            int flags = Build.VERSION.SDK_INT >= 28 ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES;
            PackageInfo candidate = manager.getPackageArchiveInfo(file.getAbsolutePath(), flags);
            PackageInfo installed = manager.getPackageInfo(context.getPackageName(), flags);
            if (candidate == null || candidate.applicationInfo == null
                    || !context.getPackageName().equals(candidate.packageName)
                    || version(candidate) != item.versionCode || !item.versionName.equals(candidate.versionName)
                    || candidate.applicationInfo.minSdkVersion != item.minSdk
                    || !UpdatePolicy.isNewer(version(installed), version(candidate))) {
                throw new UpdateFailure(UpdateFailure.Code.INCOMPATIBLE);
            }
            String[] expected = signatures(installed), actual = signatures(candidate);
            if (expected.length == 0 || !Arrays.equals(expected, actual)) throw new UpdateFailure(UpdateFailure.Code.SIGNATURE);
        } catch (PackageManager.NameNotFoundException | java.security.NoSuchAlgorithmException error) {
            throw new UpdateFailure(UpdateFailure.Code.INSTALLED_INFO);
        } catch (RuntimeException error) { throw new UpdateFailure(UpdateFailure.Code.APK_INFO); }
    }
    @SuppressWarnings("deprecation") public static long version(PackageInfo info) {
        return Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
    }
    @SuppressWarnings("deprecation") private static String[] signatures(PackageInfo info) {
        Signature[] values = Build.VERSION.SDK_INT >= 28
                ? info.signingInfo == null ? null : info.signingInfo.getApkContentsSigners() : info.signatures;
        if (values == null) return new String[0];
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) result[i] = UpdatePolicy.sha256(values[i].toByteArray());
        Arrays.sort(result); return result;
    }
    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) { value.append(Character.forDigit((b >>> 4) & 15, 16)); value.append(Character.forDigit(b & 15, 16)); }
        return value.toString();
    }
}
