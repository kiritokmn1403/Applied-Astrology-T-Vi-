package com.appliedastrology.tuvi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Applied Astrology v0.0.2 - vỏ Android (Android 6.0 / API 23 trở lên).
 * Nạp giao diện web từ assets/web, cung cấp cầu nối AANative cho lưu trữ, xuất tệp, chia sẻ,
 * kiểm tra mạng và tìm kiếm native (libaacore.so theo từng ABI).
 */
public class MainActivity extends Activity {
    private static final String TAG = "AppliedAstrology";
    private static final int FILE_CHOOSER = 7001;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private SharedPreferences prefs;
    private boolean selftest;
    private File testDir;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("aa_store", Context.MODE_PRIVATE);
        Intent it = getIntent();
        selftest = it != null && it.getBooleanExtra("selftest", false);
        testDir = new File(getExternalFilesDir(null), "selftest");
        if (selftest) { deleteDir(testDir); testDir.mkdirs(); }

        if (Build.VERSION.SDK_INT >= 19 && (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        web = new WebView(this);
        setContentView(web);
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
        Log.i(TAG, "start v0.0.2 abi=" + NativeCore.abi() + " sdk=" + Build.VERSION.SDK_INT + " selftest=" + selftest);
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
                c.setConnectTimeout(6000); c.setReadTimeout(6000);
                c.setRequestProperty("User-Agent", "AppliedAstrology/0.0.2 Android");
                int code = c.getResponseCode();
                InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int n;
                while (in != null && (n = in.read(buf)) > 0 && bo.size() < 65536) bo.write(buf, 0, n);
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

        @JavascriptInterface public String log(String m) { Log.i(TAG, m); return "1"; }

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
