package com.appliedastrology.tuvi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Applied Astrology v0.0.3 - vỏ Android (Android 6.0 / API 23 trở lên).
 * Nạp giao diện web từ assets/web, cung cấp cầu nối AANative cho lưu trữ, xuất tệp, chia sẻ,
 * kiểm tra mạng, tìm kiếm native (libaacore.so theo từng ABI), nhật ký/báo cáo lỗi và tự cập nhật từ GitHub Releases.
 * v0.0.3: đệm WindowInsets (thanh trạng thái, thanh điều hướng, tai thỏ, bàn phím) để nội dung không bị che
 * khi Android 15+ ép chế độ tràn viền (edge-to-edge) với targetSdk 35.
 */
public class MainActivity extends Activity {
    private static final String TAG = "AppliedAstrology";
    private static final int FILE_CHOOSER = 7001;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private SharedPreferences prefs;
    private boolean selftest;
    private File testDir;
    private FrameLayout rootView;
    static final String VERSION = "0.0.3";
    private final Object dlLock = new Object();
    private volatile String dlState = "idle", dlError = "", dlPath = "";
    private volatile long dlGot = 0, dlTotal = 0;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installCrashHandler();
        prefs = getSharedPreferences("aa_store", Context.MODE_PRIVATE);
        Intent it = getIntent();
        selftest = it != null && it.getBooleanExtra("selftest", false);
        testDir = new File(getExternalFilesDir(null), "selftest");
        if (selftest) { deleteDir(testDir); testDir.mkdirs(); }

        if (Build.VERSION.SDK_INT >= 19 && (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        web = new WebView(this);
        rootView = new FrameLayout(this);
        rootView.setBackgroundColor(Color.parseColor("#0E1428"));
        rootView.addView(web, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(rootView);
        applyInsets();
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(), "AANative");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("file:///android_asset/")) return false;
                openExternal(url);
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage m) {
                Log.i(TAG, "console[" + m.messageLevel() + "] " + m.message() + " @" + m.sourceId() + ":" + m.lineNumber());
                return true;
            }
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try { startActivityForResult(Intent.createChooser(i, "JSON"), FILE_CHOOSER); }
                catch (ActivityNotFoundException e) { fileCallback = null; return false; }
                return true;
            }
        });
        // Android WebView không hỗ trợ query string với file:///android_asset -> dùng #hash
        String url = "file:///android_asset/web/index.html" + (selftest ? "#selftest=1" : "");
        if (savedInstanceState != null) web.restoreState(savedInstanceState); else web.loadUrl(url);
        fileLog("start v" + VERSION + " abi=" + NativeCore.abi() + " sdk=" + Build.VERSION.SDK_INT + " device=" + Build.MANUFACTURER + " " + Build.MODEL);
        Log.i(TAG, "start v" + VERSION + " abi=" + NativeCore.abi() + " sdk=" + Build.VERSION.SDK_INT + " selftest=" + selftest);
    }

    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); if (web != null) web.saveState(out); }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        if (web != null) web.evaluateJavascript("window.dispatchEvent(new Event('resize'))", null);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == FILE_CHOOSER && fileCallback != null) {
            Uri[] r = (res == RESULT_OK && data != null && data.getData() != null) ? new Uri[]{data.getData()} : null;
            fileCallback.onReceiveValue(r);
            fileCallback = null;
            return;
        }
        super.onActivityResult(req, res, data);
    }

    private void openExternal(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Log.w(TAG, "openUrl failed " + e); }
    }

    private static void deleteDir(File f) {
        if (f == null || !f.exists()) return;
        File[] l = f.listFiles();
        if (l != null) for (File c : l) deleteDir(c);
        f.delete();
    }

    private static String jsonStr(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') b.append('\\').append(c);
            else if (c == '\n') b.append("\\n");
            else if (c == '\r') b.append("\\r");
            else if (c == '\t') b.append("\\t");
            else if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
        }
        return b.append('"').toString();
    }

    /** Ảnh chụp WebView (dùng cho kiểm thử tự động) */
    private void captureTo(final File out) {
        final CountDownLatch latch = new CountDownLatch(1);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                try {
                    View v = web;
                    Bitmap bmp = Bitmap.createBitmap(Math.max(1, v.getWidth()), Math.max(1, v.getHeight()), Bitmap.Config.ARGB_8888);
                    v.draw(new Canvas(bmp));
                    FileOutputStream fo = new FileOutputStream(out);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fo);
                    fo.close();
                    bmp.recycle();
                } catch (Exception e) { Log.w(TAG, "capture " + e); }
                latch.countDown();
            }
        });
        try { latch.await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) { }
    }

    // ---------------- v0.0.3: WindowInsets (Android 15+ ép edge-to-edge khi targetSdk 35) ----------------
    private void applyInsets() {
        if (Build.VERSION.SDK_INT < 20) return;
        rootView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                int l, t, r, b;
                if (Build.VERSION.SDK_INT >= 30) {
                    android.graphics.Insets s = in.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                    l = s.left; t = s.top; r = s.right; b = s.bottom;
                } else {
                    l = in.getSystemWindowInsetLeft(); t = in.getSystemWindowInsetTop(); r = in.getSystemWindowInsetRight(); b = in.getSystemWindowInsetBottom();
                }
                // Android 14 trở xuống (không tràn viền): hệ thống đã chừa chỗ cho thanh trạng thái/điều hướng
                if (Build.VERSION.SDK_INT < 35) { l = t = r = 0; b = 0; }
                v.setPadding(l, t, r, b);
                if (selftest) fileLog("insets l=" + l + " t=" + t + " r=" + r + " b=" + b);
                return in;
            }
        });
        rootView.requestApplyInsets();
    }
    @SuppressWarnings("deprecation")
    private void setBars(String color, boolean light) {
        try {
            int c = Color.parseColor(color);
            rootView.setBackgroundColor(c);
            Window w = getWindow();
            if (Build.VERSION.SDK_INT < 35) { w.setStatusBarColor(c); w.setNavigationBarColor(c); }
            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsetsController ic = w.getInsetsController();
                if (ic != null) {
                    int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                    ic.setSystemBarsAppearance(light ? mask : 0, mask);
                }
            } else if (Build.VERSION.SDK_INT >= 23) {
                View d = w.getDecorView();
                int f = d.getSystemUiVisibility();
                f = light ? (f | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (f & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                if (Build.VERSION.SDK_INT >= 26) f = light ? (f | 0x10) : (f & ~0x10); // SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                d.setSystemUiVisibility(f);
            }
        } catch (Exception e) { Log.w(TAG, "setBars " + e); }
    }

    // ---------------- v0.0.3: nhật ký ứng dụng & báo cáo lỗi (crash) ----------------
    private File logFile() { return new File(getFilesDir(), "app.log"); }
    private File crashDir() { File d = new File(getFilesDir(), "crash"); d.mkdirs(); return d; }
    private synchronized void fileLog(String m) {
        try {
            String on = prefs != null ? prefs.getString("aa.settings", null) : null;
            if (on != null && on.contains("\"logs\":false")) return;
            File f = logFile();
            if (f.length() > 262144) { File old = new File(getFilesDir(), "app.log.1"); old.delete(); f.renameTo(old); f = logFile(); }
            FileOutputStream fo = new FileOutputStream(f, true);
            fo.write((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + " " + m + "\n").getBytes("UTF-8"));
            fo.close();
        } catch (Exception ignored) { }
    }
    private static String readTail(File f, int max) {
        if (f == null || !f.exists()) return "";
        try {
            FileInputStream in = new FileInputStream(f);
            long skip = Math.max(0, f.length() - max);
            if (skip > 0) in.skip(skip);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            return bo.toString("UTF-8");
        } catch (Exception e) { return ""; }
    }
    private void installCrashHandler() {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread th, Throwable ex) {
                try {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    String head = "Applied Astrology " + VERSION + "\nAndroid " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\nDevice: " + Build.MANUFACTURER + " " + Build.MODEL +
                        "\nABI: " + NativeCore.abi() + "\nThread: " + th.getName() + "\nTime: " + new Date() + "\n\n";
                    FileOutputStream fo = new FileOutputStream(new File(crashDir(), "crash_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt"));
                    fo.write((head + sw).getBytes("UTF-8")); fo.close();
                } catch (Throwable ignored) { }
                if (prev != null) prev.uncaughtException(th, ex);
            }
        });
    }

    // ---------------- v0.0.3: tải và cài bản cập nhật từ GitHub Releases ----------------
    static File updatesDir(Context c) {
        File base = c.getExternalCacheDir();
        if (base == null) base = c.getCacheDir();
        File d = new File(base, "updates"); d.mkdirs();
        return d;
    }
    private boolean startDownload(final String url, final String name) {
        if (url == null || !url.startsWith("https://")) return false;
        synchronized (dlLock) {
            if ("running".equals(dlState)) return true;
            dlState = "running"; dlGot = 0; dlTotal = 0; dlError = ""; dlPath = "";
        }
        new Thread(new Runnable() { @Override public void run() {
            HttpURLConnection c = null;
            try {
                String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
                File dir = updatesDir(MainActivity.this);
                File[] old = dir.listFiles(); if (old != null) for (File o : old) o.delete();
                File part = new File(dir, safe + ".part"), out = new File(dir, safe);
                String u = url;
                for (int hop = 0; hop < 6; hop++) { // tự theo chuyển hướng (github.com -> objects.githubusercontent.com)
                    c = (HttpURLConnection) new URL(u).openConnection();
                    TlsCompat.apply(MainActivity.this, c);
                    c.setInstanceFollowRedirects(false);
                    c.setConnectTimeout(15000); c.setReadTimeout(30000);
                    c.setRequestProperty("User-Agent", "AppliedAstrology/" + VERSION + " Android");
                    int code = c.getResponseCode();
                    if (code >= 300 && code < 400) { u = c.getHeaderField("Location"); c.disconnect(); c = null; continue; }
                    if (code != 200) throw new Exception("HTTP " + code);
                    break;
                }
                if (c == null) throw new Exception("too many redirects");
                dlTotal = c.getContentLength();
                InputStream in = c.getInputStream();
                FileOutputStream fo = new FileOutputStream(part);
                byte[] buf = new byte[65536]; int n;
                while ((n = in.read(buf)) > 0) { fo.write(buf, 0, n); dlGot += n; }
                fo.close(); in.close();
                if (dlTotal > 0 && dlGot != dlTotal) throw new Exception("incomplete " + dlGot + "/" + dlTotal);
                out.delete();
                if (!part.renameTo(out)) throw new Exception("rename failed");
                out.setReadable(true, false);
                dlPath = out.getAbsolutePath();
                fileLog("update downloaded " + dlPath + " " + dlGot);
                dlState = "done";
            } catch (Exception e) {
                dlError = String.valueOf(e); dlState = "error"; fileLog("update download error " + e);
            } finally { if (c != null) c.disconnect(); }
        } }, "aa-update").start();
        return true;
    }
    private String installApk(final File apk) {
        if (apk == null || !apk.exists()) return null;
        if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            runOnUiThread(new Runnable() { @Override public void run() {
                try { startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()))); }
                catch (Exception e) { Log.w(TAG, "unknown sources settings " + e); }
            } });
            return "need_permission";
        }
        runOnUiThread(new Runnable() { @Override public void run() {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri uri = Build.VERSION.SDK_INT >= 24 ? Uri.parse("content://" + ApkProvider.AUTHORITY + "/" + apk.getName()) : Uri.fromFile(apk);
                i.setDataAndType(uri, "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(i);
                fileLog("update install intent " + uri);
            } catch (Exception e) { fileLog("update install error " + e); Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_LONG).show(); }
        } });
        return "1";
    }

    /** Cầu nối JS -> native (chạy trên luồng JavaBridge) */
    public class Bridge {
        @JavascriptInterface public String storeGet(String k) { return prefs.getString(k, null); }
        @JavascriptInterface public String storeSet(String k, String v) { prefs.edit().putString(k, v).apply(); return "1"; }

        @JavascriptInterface
        public String info() {
            boolean dex = false;
            try {
                Configuration c = getResources().getConfiguration();
                Class<?> cls = c.getClass();
                int v = cls.getField("semDesktopModeEnabled").getInt(c);
                int on = cls.getField("SEM_DESKTOP_MODE_ENABLED").getInt(cls);
                dex = v == on;
            } catch (Throwable ignored) { }
            return "{\"os\":" + jsonStr("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")") +
                ",\"device\":" + jsonStr(Build.MANUFACTURER + " " + Build.MODEL) +
                ",\"abi\":" + jsonStr(NativeCore.abi()) + ",\"engine\":\"Android WebView\",\"dex\":" + dex + "}";
        }

        @JavascriptInterface
        public String saveFile(String name, String data) {
            try {
                byte[] bytes;
                String mime = "application/json";
                if (data.startsWith("data:")) {
                    int comma = data.indexOf(',');
                    mime = data.substring(5, data.indexOf(';'));
                    bytes = Base64.decode(data.substring(comma + 1), Base64.DEFAULT);
                } else bytes = data.getBytes("UTF-8");
                String safe = name.replaceAll("[\\\\/:*?\"<>|]", "_");
                String where;
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.MediaColumns.DISPLAY_NAME, safe);
                    cv.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                    cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AppliedAstrology");
                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write(bytes); os.close();
                    where = "Download/AppliedAstrology/" + safe;
                } else {
                    File dir = new File(getExternalFilesDir(null), "exports");
                    dir.mkdirs();
                    File f = new File(dir, safe);
                    FileOutputStream fo = new FileOutputStream(f); fo.write(bytes); fo.close();
                    where = f.getAbsolutePath();
                }
                final String msg = where;
                runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show(); } });
                return where;
            } catch (Exception e) {
                Log.w(TAG, "saveFile " + e);
                return null;
            }
        }

        @JavascriptInterface
        public String share(final String text) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    try { startActivity(Intent.createChooser(i, "Applied Astrology")); } catch (Exception ignored) { }
                }
            });
            return "1";
        }

        @JavascriptInterface public String openUrl(final String url) { runOnUiThread(new Runnable() { @Override public void run() { openExternal(url); } }); return "1"; }

        @JavascriptInterface
        public String httpGet(String url) {
            long t0 = System.currentTimeMillis();
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(url).openConnection();
                TlsCompat.apply(MainActivity.this, c);
                c.setConnectTimeout(6000); c.setReadTimeout(6000);
                c.setRequestProperty("User-Agent", "AppliedAstrology/" + VERSION + " Android");
                if (url.startsWith("https://api.github.com/")) c.setRequestProperty("Accept", "application/vnd.github+json");
                int code = c.getResponseCode();
                InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int n;
                while (in != null && (n = in.read(buf)) > 0 && bo.size() < 2097152) bo.write(buf, 0, n);
                long ms = System.currentTimeMillis() - t0;
                return "{\"ok\":" + (code >= 200 && code < 400) + ",\"status\":" + code + ",\"ms\":" + ms + ",\"body\":" + jsonStr(bo.toString("UTF-8")) + "}";
            } catch (Exception e) {
                return "{\"ok\":false,\"status\":0,\"ms\":" + (System.currentTimeMillis() - t0) + ",\"body\":" + jsonStr(String.valueOf(e)) + "}";
            } finally { if (c != null) c.disconnect(); }
        }

        @JavascriptInterface
        public String search(String foldedQuery, String max) {
            try { return NativeCore.search(getAssets(), foldedQuery, Integer.parseInt(max)); }
            catch (Throwable e) { Log.w(TAG, "native search unavailable: " + e); return null; }
        }

        @JavascriptInterface public String log(String m) { Log.i(TAG, m); fileLog(m); return "1"; }
        // ---------- v0.0.3: nhật ký, báo cáo lỗi, cập nhật, thanh hệ thống (mọi hàm nhận 2 tham số chuỗi) ----------
        @JavascriptInterface public String x_readLog(String a, String b) { return readTail(logFile(), 131072); }
        @JavascriptInterface public String x_clearLog(String a, String b) { logFile().delete(); new File(getFilesDir(), "app.log.1").delete(); return "1"; }
        @JavascriptInterface
        public String x_crashList(String a, String b) {
            File[] l = crashDir().listFiles();
            StringBuilder sb = new StringBuilder("[");
            if (l != null) {
                Arrays.sort(l);
                for (int i = l.length - 1, n = 0; i >= 0 && n < 20; i--, n++) {
                    if (sb.length() > 1) sb.append(',');
                    sb.append("{\"name\":").append(jsonStr(l[i].getName())).append(",\"text\":").append(jsonStr(readTail(l[i], 16384))).append('}');
                }
            }
            return sb.append(']').toString();
        }
        @JavascriptInterface public String x_clearCrash(String a, String b) { deleteDir(crashDir()); return "1"; }
        @JavascriptInterface
        public String x_copy(final String text, String b) {
            runOnUiThread(new Runnable() { @Override public void run() {
                try { ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Applied Astrology", text)); } catch (Exception ignored) { }
            } });
            return "1";
        }
        @JavascriptInterface public String x_download(String url, String name) { return startDownload(url, name) ? "1" : null; }
        @JavascriptInterface
        public String x_downloadStatus(String a, String b) {
            return "{\"state\":" + jsonStr(dlState) + ",\"got\":" + dlGot + ",\"total\":" + dlTotal + ",\"path\":" + jsonStr(dlPath) + ",\"error\":" + jsonStr(dlError) + "}";
        }
        @JavascriptInterface public String x_installUpdate(String path, String b) { return installApk(new File(path)); }
        @JavascriptInterface
        public String x_setBars(final String color, final String light) {
            runOnUiThread(new Runnable() { @Override public void run() { setBars(color, "1".equals(light)); } });
            return "1";
        }


        @JavascriptInterface
        public String testStep(String name, String payload) {
            if (!selftest) return null;
            try {
                captureTo(new File(testDir, name + ".png"));
                FileOutputStream fo = new FileOutputStream(new File(testDir, "steps.jsonl"), true);
                fo.write(("{\"step\":" + jsonStr(name) + ",\"data\":" + (payload.length() > 0 ? payload : "null") + "}\n").getBytes("UTF-8"));
                fo.close();
                if ("99_done".equals(name)) {
                    FileOutputStream fr = new FileOutputStream(new File(testDir, "results.json"));
                    fr.write(payload.getBytes("UTF-8")); fr.close();
                    Log.i(TAG, "SELFTEST_DONE " + payload);
                }
                return "1";
            } catch (Exception e) { return null; }
        }
    }
}
