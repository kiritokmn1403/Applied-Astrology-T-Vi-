package com.appliedastrology.tuvi;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Applied Astrology v0.0.3 - ContentProvider tối giản (không cần thư viện AndroidX FileProvider)
 * để trình cài đặt hệ thống đọc APK cập nhật qua content:// trên Android 7.0+.
 * Chỉ phục vụ tệp .apk nằm trong thư mục cache/updates của ứng dụng; không xuất ra ngoài (exported=false).
 */
public class ApkProvider extends ContentProvider {
    static final String AUTHORITY = "com.appliedastrology.tuvi.updates";

    private File resolve(Uri uri) throws FileNotFoundException {
        String name = uri.getLastPathSegment();
        if (name == null || name.contains("/") || name.contains("..") || !name.endsWith(".apk")) throw new FileNotFoundException(String.valueOf(uri));
        File f = new File(MainActivity.updatesDir(getContext()), name);
        if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
        return f;
    }
    @Override public boolean onCreate() { return true; }
    @Override public String getType(Uri uri) { return "application/vnd.android.package-archive"; }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(resolve(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            File f = resolve(uri);
            MatrixCursor c = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
            c.addRow(new Object[]{f.getName(), f.length()});
            return c;
        } catch (FileNotFoundException e) { return null; }
    }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
