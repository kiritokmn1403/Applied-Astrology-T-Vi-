package com.appliedastrology.tuvi;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.appliedastrology.tuvi.core.Json;
import com.appliedastrology.tuvi.core.Library;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Applied Astrology v0.0.4 – dịch vụ nền tảng Android (thay cho cầu nối JS ↔ native của bản WebView 0.0.3):
 * lưu trữ (SharedPreferences "aa_store", cùng khóa với 0.0.3 → nâng cấp giữ nguyên hồ sơ/cài đặt), nhật ký, báo cáo lỗi,
 * HTTP (TlsCompat cho Android 6), tải & cài bản cập nhật, lưu tệp, chia sẻ, sao chép, mở liên kết.
 */
public final class Host implements Library.Source {
    public static final String TAG = "AppliedAstrology";
    public static final String VERSION = "0.0.4";
    public static final String BUILD_DATE = "2026-11-16";
    final Activity act; final SharedPreferences prefs;
    public Host(Activity a) { act = a; prefs = a.getSharedPreferences("aa_store", Context.MODE_PRIVATE); }

    // ---------------- dữ liệu đóng gói ----------------
    @Override public String read(String path) {
        try {
            InputStream in = act.getAssets().open(path);
            ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] b = new byte[65536]; int n;
            while ((n = in.read(b)) > 0) bo.write(b, 0, n);
            in.close(); return bo.toString("UTF-8");
        } catch (Exception e) { Log.w(TAG, "asset " + path + " " + e); return null; }
    }

    // ---------------- lưu trữ ----------------
    public Object get(String key) { String s = prefs.getString(key, null); return s == null ? null : Json.parse(s); }
    public void set(String key, Object val) { prefs.edit().putString(key, Json.stringify(val)).apply(); }
    @SuppressWarnings("unchecked") public List<Object> list(String key) { Object o = get(key); return o instanceof List ? (List<Object>) o : new ArrayList<Object>(); }

    // ---------------- nhật ký & sự cố ----------------
    File logFile() { return new File(act.getFilesDir(), "app.log"); }
    File crashDir() { File d = new File(act.getFilesDir(), "crash"); d.mkdirs(); return d; }
    public synchronized void log(String m) {
        Log.i(TAG, m);
        try {
            String on = prefs.getString("aa.settings", null);
            if (on != null && on.contains("\"logs\":false")) return;
            File f = logFile();
            if (f.length() > 262144) { File old = new File(act.getFilesDir(), "app.log.1"); old.delete(); f.renameTo(old); f = logFile(); }
            FileOutputStream fo = new FileOutputStream(f, true);
            fo.write((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + " " + m + "\n").getBytes("UTF-8"));
            fo.close();
        } catch (Exception ignored) { }
    }
    public String readLog() { return readTail(new File(act.getFilesDir(), "app.log.1"), 32768) + readTail(logFile(), 131072); }
    public void clearLog() { logFile().delete(); new File(act.getFilesDir(), "app.log.1").delete(); }
    static String readTail(File f, int max) {
        if (f == null || !f.exists()) return "";
        try {
            FileInputStream in = new FileInputStream(f); long skip = Math.max(0, f.length() - max); if (skip > 0) in.skip(skip);
            ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close(); return bo.toString("UTF-8");
        } catch (Exception e) { return ""; }
    }
    public void installCrashHandler() {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread th, Throwable ex) {
                try { writeCrash(th.getName(), ex); } catch (Throwable ignored) { }
                if (prev != null) prev.uncaughtException(th, ex);
            }
        });
    }
    public void writeCrash(String where, Throwable ex) {
        try {
            StringWriter sw = new StringWriter(); ex.printStackTrace(new PrintWriter(sw));
            String head = "Applied Astrology " + VERSION + " (native)\nAndroid " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\nDevice: " + Build.MANUFACTURER + " " + Build.MODEL +
                "\nABI: " + NativeCore.abi() + "\nWhere: " + where + "\nTime: " + new Date() + "\n\n";
            FileOutputStream fo = new FileOutputStream(new File(crashDir(), "crash_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date()) + ".txt"));
            fo.write((head + sw).getBytes("UTF-8")); fo.close();
        } catch (Throwable ignored) { }
    }
    public List<String[]> crashList() {
        List<String[]> r = new ArrayList<String[]>(); File[] l = crashDir().listFiles();
        if (l != null) { Arrays.sort(l); for (int i = l.length - 1, n = 0; i >= 0 && n < 20; i--, n++) r.add(new String[]{l[i].getName(), readTail(l[i], 16384)}); }
        return r;
    }
    public void clearCrash() { File[] l = crashDir().listFiles(); if (l != null) for (File f : l) f.delete(); }

    // ---------------- thông tin thiết bị ----------------
    public boolean isDex() {
        try {
            Configuration c = act.getResources().getConfiguration(); Class<?> cls = c.getClass();
            return cls.getField("semDesktopModeEnabled").getInt(c) == cls.getField("SEM_DESKTOP_MODE_ENABLED").getInt(cls);
        } catch (Throwable ignored) { return false; }
    }
    public String os() { return "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"; }
    public String device() { return Build.MANUFACTURER + " " + Build.MODEL; }
    public String abi() { return NativeCore.abi(); }
    public String engine() { return "Native Android Views (no WebView)"; }
    public String screen() { DisplayMetrics m = act.getResources().getDisplayMetrics(); return m.widthPixels + "x" + m.heightPixels + " @" + m.density; }

    // ---------------- mạng ----------------
    public static final class Resp { public boolean ok; public int status; public String body = ""; public long ms; }
    public Resp httpGet(String url, int timeout) {
        Resp r = new Resp(); long t0 = System.currentTimeMillis(); HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            TlsCompat.apply(act, c);
            c.setConnectTimeout(timeout); c.setReadTimeout(timeout);
            c.setRequestProperty("User-Agent", "AppliedAstrology/" + VERSION + " (Android; native)");
            if (url.startsWith("https://api.github.com/")) c.setRequestProperty("Accept", "application/vnd.github+json");
            int code = c.getResponseCode();
            InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] buf = new byte[8192]; int n;
            while (in != null && (n = in.read(buf)) > 0 && bo.size() < 4194304) bo.write(buf, 0, n);
            r.status = code; r.ok = code >= 200 && code < 400; r.body = bo.toString("UTF-8");
        } catch (Exception e) { r.ok = false; r.status = 0; r.body = String.valueOf(e); }
        finally { if (c != null) c.disconnect(); r.ms = System.currentTimeMillis() - t0; }
        return r;
    }

    // ---------------- tải & cài cập nhật ----------------
    public final Object dlLock = new Object();
    public volatile String dlState = "idle", dlError = "", dlPath = "";
    public volatile long dlGot = 0, dlTotal = 0;
    File updatesDir() { return ApkProvider.updatesDir(act); }
    public boolean download(final String url, final String name) {
        if (url == null || !url.startsWith("https://")) return false;
        synchronized (dlLock) { if ("running".equals(dlState)) return true; dlState = "running"; dlGot = 0; dlTotal = 0; dlError = ""; dlPath = ""; }
        new Thread(new Runnable() { @Override public void run() {
            HttpURLConnection c = null;
            try {
                String safe = name.replaceAll("[^A-Za-z0-9._-]", "_"); File dir = updatesDir();
                File[] old = dir.listFiles(); if (old != null) for (File o : old) o.delete();
                File part = new File(dir, safe + ".part"), out = new File(dir, safe); String u = url;
                for (int hop = 0; hop < 6; hop++) {
                    c = (HttpURLConnection) new URL(u).openConnection(); TlsCompat.apply(act, c);
                    c.setInstanceFollowRedirects(false); c.setConnectTimeout(15000); c.setReadTimeout(30000);
                    c.setRequestProperty("User-Agent", "AppliedAstrology/" + VERSION + " (Android; native)");
                    int code = c.getResponseCode();
                    if (code >= 300 && code < 400) { u = c.getHeaderField("Location"); c.disconnect(); c = null; continue; }
                    if (code != 200) throw new Exception("HTTP " + code);
                    break;
                }
                if (c == null) throw new Exception("too many redirects");
                dlTotal = c.getContentLength();
                InputStream in = c.getInputStream(); FileOutputStream fo = new FileOutputStream(part);
                byte[] buf = new byte[65536]; int n;
                while ((n = in.read(buf)) > 0) { fo.write(buf, 0, n); dlGot += n; }
                fo.close(); in.close();
                if (dlTotal > 0 && dlGot != dlTotal) throw new Exception("incomplete " + dlGot + "/" + dlTotal);
                out.delete(); if (!part.renameTo(out)) throw new Exception("rename failed");
                out.setReadable(true, false); dlPath = out.getAbsolutePath();
                log("update downloaded " + dlPath + " " + dlGot); dlState = "done";
            } catch (Exception e) { dlError = String.valueOf(e); dlState = "error"; log("update download error " + e); }
            finally { if (c != null) c.disconnect(); }
        } }, "aa-update").start();
        return true;
    }
    /** "1" đã mở trình cài, "need_permission" cần cho phép nguồn không xác định, null lỗi */
    public String installApk(final File apk) {
        if (apk == null || !apk.exists()) return null;
        if (Build.VERSION.SDK_INT >= 26 && !act.getPackageManager().canRequestPackageInstalls()) {
            try { act.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + act.getPackageName()))); } catch (Exception e) { log("unknown sources settings " + e); }
            return "need_permission";
        }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            Uri uri = Build.VERSION.SDK_INT >= 24 ? Uri.parse("content://" + ApkProvider.AUTHORITY + "/" + apk.getName()) : Uri.fromFile(apk);
            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            act.startActivity(i); log("update install intent " + uri); return "1";
        } catch (Exception e) { log("update install error " + e); return null; }
    }

    // ---------------- tệp, chia sẻ, liên kết ----------------
    public String saveFile(String name, byte[] bytes, String mime) {
        try {
            String safe = name.replaceAll("[\\\\/:*?\"<>|]", "_"), where;
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, safe); cv.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AppliedAstrology");
                Uri uri = act.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                OutputStream os = act.getContentResolver().openOutputStream(uri); os.write(bytes); os.close();
                where = "Download/AppliedAstrology/" + safe;
            } else {
                File dir = new File(act.getExternalFilesDir(null), "exports"); dir.mkdirs();
                File f = new File(dir, safe); FileOutputStream fo = new FileOutputStream(f); fo.write(bytes); fo.close();
                where = f.getAbsolutePath();
            }
            log("saved " + where); return where;
        } catch (Exception e) { log("saveFile " + e); return null; }
    }
    public void share(String text) {
        Intent i = new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT, text);
        try { act.startActivity(Intent.createChooser(i, "Applied Astrology")); } catch (Exception ignored) { }
    }
    public void shareImage(File png) {
        try {
            Uri uri = Build.VERSION.SDK_INT >= 24 ? Uri.parse("content://" + ApkProvider.AUTHORITY + "/" + png.getName()) : Uri.fromFile(png);
            Intent i = new Intent(Intent.ACTION_SEND); i.setType("image/png"); i.putExtra(Intent.EXTRA_STREAM, uri); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            act.startActivity(Intent.createChooser(i, "Applied Astrology"));
        } catch (Exception e) { log("shareImage " + e); }
    }
    public File shareDir() { return updatesDir(); }
    public void openUrl(String url) { try { act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { log("openUrl " + e); } }
    public void copy(String text) { try { ((ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Applied Astrology", text)); } catch (Exception ignored) { } }
    public void toast(String m) { Toast.makeText(act, m, Toast.LENGTH_SHORT).show(); }
    public static Map<String, Object> m(Object... kv) { return Json.map(kv); }
}
