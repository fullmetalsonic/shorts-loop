package com.fullmetalsonic.shortsloop.updates;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;

/** Read-only digest-named install snapshots only, never a general-purpose filesystem provider. */
public final class UpdateFileProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }
    public static File directory(android.content.Context context) { return new File(context.getCacheDir(), "updates"); }
    public static File apk(android.content.Context context) { return new File(directory(context), "update.apk"); }
    static File installFile(android.content.Context context, String digest) {
        if (!UpdatePolicy.validSha256(digest)) throw new IllegalArgumentException("Invalid digest");
        return new File(directory(context), "install-" + digest.toLowerCase(java.util.Locale.ROOT) + ".apk");
    }
    public static Uri uri(android.content.Context context, String digest) {
        return Uri.parse("content://" + context.getPackageName() + ".updates/" + installFile(context, digest).getName());
    }
    private File requireUri(Uri value) {
        if (!"content".equals(value.getScheme()) || !(getContext().getPackageName() + ".updates").equals(value.getAuthority())
                || value.getQuery() != null || value.getFragment() != null || value.getPath() == null
                || !value.getPath().matches("/install-[a-f0-9]{64}\\.apk")) throw new IllegalArgumentException("Unsupported update URI");
        return new File(directory(getContext()), value.getPath().substring(1));
    }
    @Override public String getType(Uri uri) { requireUri(uri); return "application/vnd.android.package-archive"; }
    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File file = requireUri(uri);
        if (!"r".equals(mode)) throw new FileNotFoundException("Read-only update");
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) {
        File file = requireUri(uri);
        String[] columns = projection == null ? new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE} : projection;
        MatrixCursor cursor = new MatrixCursor(columns); Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) values[i] = "ShortsLoop-update.apk";
            else if (OpenableColumns.SIZE.equals(columns[i])) values[i] = file.length();
        }
        cursor.addRow(values); return cursor;
    }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException(); }
}
