package com.appliedastrology.tuvi;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.appliedastrology.tuvi.core.Assistant;
import com.appliedastrology.tuvi.core.Engine;
import com.appliedastrology.tuvi.core.I18n;
import com.appliedastrology.tuvi.core.Json;
import com.appliedastrology.tuvi.core.Library;
import com.appliedastrology.tuvi.core.Theme;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Applied Astrology v0.0.5 – ứng dụng Android NATIVE (Java + Android Views, không dùng WebView).
 * Giao diện, lá số, Âm Lịch, thư viện, luận giải, cài đặt, Liquid Glass đều vẽ bằng View native → hệ thống nhận diện là
 * ứng dụng bình thường, khởi động nhanh, không phụ thuộc phiên bản Android System WebView.
 * Vùng an toàn: đệm WindowInsets (thanh trạng thái, thanh điều hướng, tai thỏ, bàn phím) trên mọi phiên bản 6.0 → 17.
 */
public class MainActivity extends Activity {
    Host host; Library lib; I18n i18n; Assistant asst; Ui ui;
    final GlassViews.Cfg gcfg = new GlassViews.Cfg();
    GlassViews.Bg bg; FrameLayout root, content, overlay; LinearLayout shell; GlassViews.Frame header, nav, toastBox;
    Map<String, Object> S; Screens sc; SelfTest selftest;
    final Handler h = new Handler(Looper.getMainLooper());
    String current = "chart"; boolean wide; int insetL, insetT, insetR, insetB;
    static final String[] VIEWS = {"chart", "calendar", "library", "assistant", "saved", "settings"};
    static final String[] TAB_KEYS = {"tab_chart", "tab_calendar", "tab_library", "tab_assistant", "tab_saved", "tab_settings"};

    static Map<String, Object> defaults() {
        return Json.map("lang", "vi", "theme", "celestial-dark", "font", "normal", "layout", "auto", "desktop", null, "ts", true, "bs", false, "tt", true, "luu", true,
            "leap", "same", "paper", "none", "autoUpdate", true, "autoInstall", true, "logs", true,
            // v0.0.4 – Liquid Glass
            "lgOn", true, "lgMode", "liquid", "lgBg", "nebula", "lgBlur", 18L, "lgRefr", 60L, "lgBezel", 18L, "lgTint", 35L, "lgSpec", 55L, "lgSat", 135L, "lgCards", true, "lgChart", false);
    }
    /** Máy yếu: ActivityManager báo low-RAM, hạn mức heap ≤ 128 MB, hoặc Android 6/7 */
    boolean lowEnd() {
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            return am.isLowRamDevice() || am.getMemoryClass() <= 128 || Build.VERSION.SDK_INT < 26;
        } catch (Throwable e) { return false; }
    }
    String s(String k) { Object v = S.get(k); return v == null ? null : v.toString(); }
    boolean b(String k) { Object v = S.get(k); return v instanceof Boolean && (Boolean) v; }
    int n(String k) { return Json.num(S.get(k), 0); }
    void save() { host.set("aa.settings", S); }

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        host = new Host(this); host.installCrashHandler();
        S = new LinkedHashMap<String, Object>(defaults());
        Map<String, Object> st = Json.obj(host.get("aa.settings")); if (st != null) S.putAll(st);
        // v0.0.5: Liquid Glass có công tắc riêng (lgOn). Bản cũ lưu lgMode = "off" → chuyển thành lgOn = false (giữ kiểu kính mặc định)
        if (st != null && "off".equals(String.valueOf(st.get("lgMode")))) { S.put("lgOn", false); S.put("lgMode", "liquid"); }
        // máy cấu hình thấp (RAM ít / Android 6–7): lần đầu chạy dùng Kính mờ thay vì khúc xạ thật
        if (st == null || !st.containsKey("lgMode")) { if (lowEnd()) S.put("lgMode", "frost"); }
        for (Map.Entry<String, Object> e : defaults().entrySet()) if (!S.containsKey(e.getKey())) S.put(e.getKey(), e.getValue());
        lib = new Library(host);
        lib.setNativeSearch(new Library.NativeSearch() { public String search(String f, int max) { return NativeCore.search(getAssets(), f, max); } });
        i18n = new I18n(host); i18n.setLang(s("lang"));
        asst = new Assistant(lib, i18n);
        // v0.0.6: nạp sẵn thư viện ở luồng nền ưu tiên thấp (mở chương / bài liên quan lần đầu không phải chờ đọc JSON)
        Thread warm = new Thread(new Runnable() { public void run() { try { android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND); long t0 = System.currentTimeMillis(); lib.warm(); host.log("library warm " + (System.currentTimeMillis() - t0) + "ms"); } catch (Throwable e) { host.log("library warm failed " + e); } } }, "aa-warm");
        warm.setDaemon(true); warm.start();
        ui = new Ui(this);
        root = new FrameLayout(this);
        bg = new GlassViews.Bg(this, gcfg);
        root.addView(bg, new FrameLayout.LayoutParams(-1, -1));
        shell = ui.col(); root.addView(shell, new FrameLayout.LayoutParams(-1, -1));
        overlay = new FrameLayout(this); overlay.setVisibility(View.GONE); root.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        applyInsets();
        sc = new Screens(this);
        Intent it = getIntent(); boolean st0 = it != null && it.getBooleanExtra("selftest", false);
        applySettings();
        host.log("start v" + Host.VERSION + " native abi=" + NativeCore.abi() + " sdk=" + Build.VERSION.SDK_INT + " device=" + host.device() + " selftest=" + st0);
        if (it != null && it.getStringExtra("tab") != null) show(it.getStringExtra("tab"));
        if (st0) { keepAwakeForSelftest(); selftest = new SelfTest(this); h.postDelayed(new Runnable() { public void run() { selftest.start(); } }, 1500); }
        else if (b("autoUpdate")) {
            Map<String, Object> lc = Json.obj(host.get("aa.lastUpdateCheck"));
            long at = lc == null ? 0 : (long) Json.dbl(lc.get("at"), 0);
            if (System.currentTimeMillis() - at > 12 * 3600 * 1000L) h.postDelayed(new Runnable() { public void run() { sc.updates.check(true, null); } }, 4000);
        }
    }

    /**
     * v0.0.6: khi tự kiểm thử, giữ màn hình sáng và hiện trên màn hình khóa. Emulator Android 6 tắt màn hình giữa chừng (0.0.5):
     * cửa sổ không còn được bố cục/vẽ → hộp thoại 0x0, kính không bao giờ dựng xong (bước 37/47/50 FAIL).
     */
    @SuppressWarnings("deprecation")
    void keepAwakeForSelftest() {
        try {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        } catch (Throwable ignored) { }
    }

    /** v0.0.5: app đã chạy sẵn (vd. Android 17 tự mở lại sau khi cài đè) mà nhận intent mới có cờ selftest/tab */
    @Override protected void onNewIntent(Intent it) {
        super.onNewIntent(it); setIntent(it);
        if (it == null) return;
        if (it.getBooleanExtra("selftest", false) && selftest == null) { keepAwakeForSelftest(); selftest = new SelfTest(this); h.postDelayed(new Runnable() { public void run() { selftest.start(); } }, 1500); }
        else if (it.getStringExtra("tab") != null) show(it.getStringExtra("tab"));
    }

    // ---------------- vùng an toàn ----------------
    @SuppressWarnings("deprecation")
    void applyInsets() {
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets in) {
                int l, t, r, b;
                if (Build.VERSION.SDK_INT >= 30) {
                    android.graphics.Insets s = in.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                    l = s.left; t = s.top; r = s.right; b = s.bottom;
                } else { l = in.getSystemWindowInsetLeft(); t = in.getSystemWindowInsetTop(); r = in.getSystemWindowInsetRight(); b = in.getSystemWindowInsetBottom(); }
                // Android 14 trở xuống không tràn viền: hệ thống đã chừa thanh trạng thái/điều hướng; chỉ cần chừa bàn phím (adjustResize lo)
                if (Build.VERSION.SDK_INT < 35) { l = t = r = b = 0; }
                insetL = l; insetT = t; insetR = r; insetB = b;
                shell.setPadding(l, t, r, b); overlay.setPadding(l, t, r, b);
                if (selftest != null) host.log("insets l=" + l + " t=" + t + " r=" + r + " b=" + b);
                return in;
            }
        });
        root.requestApplyInsets();
    }
    @SuppressWarnings("deprecation")
    void setBars(int color, boolean light) {
        try {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT < 35) { w.setStatusBarColor(color); w.setNavigationBarColor(color); }
            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsetsController ic = w.getInsetsController();
                if (ic != null) { int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS; ic.setSystemBarsAppearance(light ? mask : 0, mask); }
            } else {
                View d = w.getDecorView(); int f = d.getSystemUiVisibility();
                f = light ? (f | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (f & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                if (Build.VERSION.SDK_INT >= 26) f = light ? (f | 0x10) : (f & ~0x10);
                d.setSystemUiVisibility(f);
            }
        } catch (Exception e) { host.log("setBars " + e); }
    }

    // ---------------- cài đặt → giao diện ----------------
    void applyGlassCfg() {
        gcfg.th = Theme.byId(s("theme")); gcfg.mode = b("lgOn") ? s("lgMode") : "off"; if (!"frost".equals(gcfg.mode) && !"liquid".equals(gcfg.mode)) gcfg.mode = "off"; gcfg.bg = s("lgBg"); gcfg.cards = b("lgCards"); gcfg.chart = b("lgChart");
        gcfg.p.blur = n("lgBlur"); gcfg.p.refraction = n("lgRefr") / 100.0; gcfg.p.bezel = n("lgBezel") * getResources().getDisplayMetrics().density / 2;
        gcfg.p.tint = n("lgTint") / 100.0; gcfg.p.specular = n("lgSpec") / 100.0; gcfg.p.saturation = n("lgSat") / 100.0;
        gcfg.p.thickness = 40 * getResources().getDisplayMetrics().density / 2;
        gcfg.version++;
    }
    boolean isDesktop() { Object d = S.get("desktop"); return d == null ? (getResources().getConfiguration().screenWidthDp >= 1000 || host.isDex()) : (Boolean) d; }
    void applySettings() {
        applyGlassCfg();
        ui.setTheme(gcfg.th, s("font"));
        root.setLayoutDirection(i18n.isRtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        setBars(gcfg.th.bg2, !gcfg.th.dark);
        getWindow().getDecorView().setBackgroundColor(gcfg.th.bg);
        bg.ensure(); bg.invalidate();
        buildShell();
        show(current);
    }

    void buildShell() {
        shell.removeAllViews();
        wide = getResources().getConfiguration().screenWidthDp >= 720 || isDesktop();
        header = new GlassViews.Frame(this, bg, gcfg, 1, 0);
        LinearLayout hr = ui.row(); hr.setPadding(ui.dp(14), ui.dp(8), ui.dp(14), ui.dp(8));
        hr.addView(new Logo(this), ui.margins(Ui.lp(ui.dp(40), ui.dp(40)), 0, 0, 10, 0));
        LinearLayout tt = ui.col(); tt.addView(ui.text(i18n.t("app_name"), 18, gcfg.th.gold2, true)); tt.addView(ui.text(i18n.t("app_sub"), 12, gcfg.th.muted, false));
        hr.addView(tt, Ui.weight(1));
        header.addView(hr, Ui.fill());
        LinearLayout.LayoutParams hp = Ui.fill(); if (!"off".equals(gcfg.mode)) ui.margins(hp, 8, 6, 8, 4);
        header.setRadius("off".equals(gcfg.mode) ? 0 : 18);
        shell.addView(header, hp);
        nav = new GlassViews.Frame(this, bg, gcfg, 1, "off".equals(gcfg.mode) ? 0 : 22);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < VIEWS.length; i++) {
            final String v = VIEWS[i];
            LinearLayout tab = ui.col(); tab.setGravity(Gravity.CENTER); tab.setPadding(ui.dp(2), ui.dp(wide ? 8 : 6), ui.dp(2), ui.dp(wide ? 8 : 6));
            boolean act = v.equals(current) || ("about".equals(current) && "settings".equals(v));
            int col = act ? gcfg.th.gold : gcfg.th.muted;
            TabIcon ic = new TabIcon(this, i, col);
            TextView lb = ui.text(i18n.t(TAB_KEYS[i]), wide ? 13 : 11, col, act); lb.setGravity(Gravity.CENTER); lb.setSingleLine(true);
            fitLabel(lb, VIEWS.length);
            tab.addView(ic, Ui.lp(ui.dp(22), ui.dp(22))); tab.addView(lb);
            tab.setTag("tab:" + v); tab.setContentDescription(i18n.t(TAB_KEYS[i]));
            tab.setOnClickListener(new View.OnClickListener() { public void onClick(View x) { show(v); } });
            if (act) { View bar = new View(this); bar.setBackgroundColor(gcfg.th.gold); LinearLayout.LayoutParams bp = Ui.lp(ui.dp(26), ui.dp(3)); bp.topMargin = ui.dp(3); tab.addView(bar, bp); }
            nav.addView(tab, Ui.weight(1));
        }
        content = new FrameLayout(this);
        LinearLayout.LayoutParams np = Ui.fill(); if (!"off".equals(gcfg.mode)) ui.margins(np, 8, 4, 8, wide ? 4 : 8);
        LinearLayout.LayoutParams cp = Ui.lp(-1, 0); cp.weight = 1;
        if (wide) { shell.addView(nav, np); shell.addView(content, cp); }
        else { shell.addView(content, cp); shell.addView(nav, np); }
    }

    /** Hiển thị một màn hình */
    void show(String view) {
        current = view;
        if (nav != null) for (int i = 0; i < nav.getChildCount(); i++) {
            View t = nav.getChildAt(i); String v = String.valueOf(t.getTag()).replace("tab:", "");
            boolean act = v.equals(view) || ("about".equals(view) && "settings".equals(v));
            LinearLayout tl = (LinearLayout) t;
            ((TabIcon) tl.getChildAt(0)).setColor(act ? gcfg.th.gold : gcfg.th.muted);
            ((TextView) tl.getChildAt(1)).setTextColor(act ? gcfg.th.gold : gcfg.th.muted);
            if (tl.getChildCount() > 2) tl.removeViewAt(2);
            if (act) { View bar = new View(this); bar.setBackgroundColor(gcfg.th.gold); LinearLayout.LayoutParams bp = Ui.lp(ui.dp(26), ui.dp(3)); bp.topMargin = ui.dp(3); tl.addView(bar, bp); }
        }
        content.removeAllViews();
        View v;
        try { v = sc.build(view); }
        catch (Throwable e) { host.writeCrash("screen " + view, e); host.log("screen error " + view + " " + e); v = ui.body(String.valueOf(e)); }
        final ScrollView sv = new ScrollView(this); sv.setFillViewport(true); sv.setTag("scroll");
        LinearLayout pad = ui.col(); pad.setPadding(ui.dp(8), ui.dp(6), ui.dp(8), ui.dp(16)); pad.addView(v, Ui.fill());
        sv.addView(pad);
        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() { public void onScrollChanged() { bg.onScroll(); } });   // v0.0.5: gộp theo khung hình, chỉ vẽ lại thẻ đổi vị trí
        content.addView(sv, new FrameLayout.LayoutParams(-1, -1));
    }
    ScrollView scroller() { return content.getChildCount() > 0 ? (ScrollView) content.getChildAt(0) : null; }
    void scrollTo(final View v) { final ScrollView s = scroller(); if (s != null && v != null) s.post(new Runnable() { public void run() { int[] a = new int[2], b = new int[2]; v.getLocationInWindow(a); s.getChildAt(0).getLocationInWindow(b); s.smoothScrollTo(0, a[1] - b[1] - ui.dp(8)); } }); }
    void scrollTop() { ScrollView s = scroller(); if (s != null) s.scrollTo(0, 0); }

    // ---------------- thẻ kính, hộp thoại, thông báo ----------------
    GlassViews.Frame card() {
        GlassViews.Frame f = new GlassViews.Frame(this, bg, gcfg, 0, 12);
        f.setPadding(ui.dp(14), ui.dp(12), ui.dp(14), ui.dp(12));
        return f;
    }
    LinearLayout.LayoutParams cardLp() { return ui.margins(Ui.fill(), 0, 0, 0, 10); }

    void modal(String title, View body) {
        overlay.removeAllViews(); overlay.setVisibility(View.VISIBLE);
        View dim = new View(this); dim.setBackgroundColor(0x99000000);
        dim.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { closeModal(); } });
        overlay.addView(dim, new FrameLayout.LayoutParams(-1, -1));
        GlassViews.Frame box = new GlassViews.Frame(this, bg, gcfg, 1, 16);
        box.setPadding(ui.dp(16), ui.dp(14), ui.dp(16), ui.dp(12)); box.setTag("modal");
        if (title != null) box.addView(ui.h2(title));
        ScrollView sv = new ScrollView(this); sv.addView(body);
        int maxH = (int) (getResources().getDisplayMetrics().heightPixels * 0.72);
        box.addView(sv, new LinearLayout.LayoutParams(-1, -2));
        TextView close = ui.button(i18n.t("btn_close"), false, new View.OnClickListener() { public void onClick(View v) { closeModal(); } });
        close.setTag("mClose");
        LinearLayout.LayoutParams clp = Ui.wrap(); clp.gravity = Gravity.CENTER_HORIZONTAL; clp.topMargin = ui.dp(10); box.addView(close, clp);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels - ui.dp(28), ui.dp(560)), -2, Gravity.CENTER);
        overlay.addView(box, bp);
        sv.getLayoutParams().height = -2; sv.setLayoutParams(sv.getLayoutParams());
        final ScrollView fsv = sv; final int fmax = maxH;
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { public void onGlobalLayout() {
            if (fsv.getHeight() > fmax) { fsv.getLayoutParams().height = fmax; fsv.requestLayout(); } fsv.getViewTreeObserver().removeOnGlobalLayoutListener(this); } });
    }
    boolean modalOpen() { return overlay.getVisibility() == View.VISIBLE; }
    void closeModal() { overlay.removeAllViews(); overlay.setVisibility(View.GONE); }

    void toast(String msg) {
        if (toastBox != null) root.removeView(toastBox);
        toastBox = new GlassViews.Frame(this, bg, gcfg, 2, 14); toastBox.solidColor = Theme.alpha(gcfg.th.gold, 255);
        TextView t = ui.text(msg, 14, 0xFF1A1205, true); t.setGravity(Gravity.CENTER); t.setPadding(ui.dp(14), ui.dp(9), ui.dp(14), ui.dp(9));
        toastBox.addView(t);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL); p.bottomMargin = insetB + ui.dp(wide ? 24 : 92);
        root.addView(toastBox, p);
        final View tb = toastBox; h.postDelayed(new Runnable() { public void run() { root.removeView(tb); if (toastBox == tb) toastBox = null; } }, 2200);
        host.log("toast " + msg);
    }

    @Override public void onBackPressed() {
        if (modalOpen()) { closeModal(); return; }
        if ("about".equals(current)) { show("settings"); return; }
        if ("library".equals(current) && sc.libBack()) return;
        if (!"chart".equals(current)) { show("chart"); return; }
        super.onBackPressed();
    }
    @Override public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        boolean w = c.screenWidthDp >= 720 || isDesktop();
        if (w != wide) { buildShell(); show(current); } else { show(current); }
    }
    // ---------------- nhập tệp sao lưu ----------------
    static final int PICK_FILE = 7001;
    void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        try { startActivityForResult(Intent.createChooser(i, "JSON"), PICK_FILE); } catch (Exception e) { host.log("pickFile " + e); }
    }
    @Override protected void onActivityResult(int req, int res, Intent data) {
        if (req == PICK_FILE && res == RESULT_OK && data != null && data.getData() != null) {
            try {
                java.io.InputStream in = getContentResolver().openInputStream(data.getData()); java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                byte[] b = new byte[16384]; int n; while ((n = in.read(b)) > 0) bo.write(b, 0, n); in.close();
                sc.importBackup(bo.toString("UTF-8"));
            } catch (Exception e) { toast(i18n.t("import_fail")); }
            return;
        }
        super.onActivityResult(req, res, data);
    }
    @Override protected void onResume() { super.onResume(); if (sc != null && sc.updates != null) sc.updates.onResume(); }

    /** Biểu tượng ứng dụng (sao tám cánh + Âm Dương), vẽ native */
    static final class Logo extends View {
        final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        Logo(android.content.Context c) { super(c); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            float w = getWidth(), s = w / 100f; c.save(); c.scale(s, s);
            p.setShader(new android.graphics.RadialGradient(50, 50, 50, 0xFF3B2A6B, 0xFF0A0518, android.graphics.Shader.TileMode.CLAMP)); c.drawCircle(50, 50, 48, p); p.setShader(null);
            p.setStyle(android.graphics.Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(0xFFF2B84B); c.drawCircle(50, 50, 48, p); p.setStyle(android.graphics.Paint.Style.FILL);
            android.graphics.Path st = new android.graphics.Path(); float[] pts = {50, 8, 56, 40, 88, 50, 56, 60, 50, 92, 44, 60, 12, 50, 44, 40};
            st.moveTo(pts[0], pts[1]); for (int i = 2; i < pts.length; i += 2) st.lineTo(pts[i], pts[i + 1]); st.close();
            p.setColor(0xE6F2B84B); c.drawPath(st, p);
            p.setColor(0xFF0A0518); c.drawCircle(50, 50, 13, p); p.setStyle(android.graphics.Paint.Style.STROKE); p.setColor(0xFFFFE7A3); c.drawCircle(50, 50, 13, p); p.setStyle(android.graphics.Paint.Style.FILL);
            android.graphics.Path yy = new android.graphics.Path(); yy.addArc(new android.graphics.RectF(37, 37, 63, 63), -90, 180); yy.arcTo(new android.graphics.RectF(43.5f, 50, 56.5f, 63), 90, 180); yy.arcTo(new android.graphics.RectF(43.5f, 37, 56.5f, 50), 90, -180); yy.close();
            c.drawPath(yy, p); c.restore();
        }
    }
    /** Biểu tượng thanh tab vẽ bằng Path (bản 0.0.3 dùng SVG vì Samsung vẽ ☾ ☯ thành emoji) */
    /** v0.0.4: nhãn tab dài (vd. tiếng Pháp "Bibliothèque") – thu nhỏ cỡ chữ tới 8sp cho vừa ô, còn dài thì "…" thay vì tràn sang tab bên cạnh */
    void fitLabel(TextView lb, int tabs) {
        int avail = getResources().getDisplayMetrics().widthPixels / tabs - ui.dp(8);
        if (getResources().getDisplayMetrics().widthPixels >= ui.dp(720)) avail = Math.min(avail, ui.dp(140));
        android.text.TextPaint tp = new android.text.TextPaint(lb.getPaint());
        float sz = lb.getTextSize(), min = 8 * getResources().getDisplayMetrics().scaledDensity;
        String t = String.valueOf(lb.getText());
        while (sz > min) { tp.setTextSize(sz); if (tp.measureText(t) <= avail) break; sz -= 0.5f * getResources().getDisplayMetrics().density; }
        lb.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, Math.max(sz, min));
        lb.setEllipsize(android.text.TextUtils.TruncateAt.END);
    }

    static final class TabIcon extends View {
        final int kind; int color; final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        TabIcon(android.content.Context c, int kind, int color) { super(c); this.kind = kind; this.color = color; }
        void setColor(int c) { color = c; invalidate(); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            float s = getWidth() / 24f; c.save(); c.scale(s, s); p.setColor(color); p.setStyle(android.graphics.Paint.Style.FILL); p.setStrokeWidth(1.8f);
            android.graphics.Path a = new android.graphics.Path();
            switch (kind) {
                case 0: { float[] q = {12, 1, 14.6f, 9.4f, 23, 12, 14.6f, 14.6f, 12, 23, 9.4f, 14.6f, 1, 12, 9.4f, 9.4f}; a.moveTo(q[0], q[1]); for (int i = 2; i < q.length; i += 2) a.lineTo(q[i], q[i + 1]); a.close(); c.drawPath(a, p); break; }
                case 1: { a.addCircle(11, 12, 9.5f, android.graphics.Path.Direction.CW); android.graphics.Path b = new android.graphics.Path(); b.addCircle(16.5f, 9.5f, 7.6f, android.graphics.Path.Direction.CW); a.op(b, android.graphics.Path.Op.DIFFERENCE); c.drawPath(a, p); break; }
                case 2: { float[][] d = {{12, 6}, {6, 12}, {18, 12}, {12, 18}}; for (float[] k : d) { a.reset(); a.moveTo(k[0], k[1] - 4); a.lineTo(k[0] + 4, k[1]); a.lineTo(k[0], k[1] + 4); a.lineTo(k[0] - 4, k[1]); a.close(); c.drawPath(a, p); } break; }
                case 3: { p.setStyle(android.graphics.Paint.Style.STROKE); c.drawCircle(12, 12, 9.6f, p); p.setStyle(android.graphics.Paint.Style.FILL);
                    a.addArc(new android.graphics.RectF(2.4f, 2.4f, 21.6f, 21.6f), -90, 180); a.arcTo(new android.graphics.RectF(7.2f, 12, 16.8f, 21.6f), 90, 180); a.arcTo(new android.graphics.RectF(7.2f, 2.4f, 16.8f, 12), 90, -180); a.close(); c.drawPath(a, p);
                    p.setColor(color); c.drawCircle(12, 17, 1.5f, p); break; }
                case 4: { float[] q = {12, 2.5f, 14.9f, 8.8f, 21.8f, 9.5f, 16.6f, 14.1f, 18.1f, 20.9f, 12, 17.4f, 5.9f, 20.9f, 7.4f, 14.1f, 2.2f, 9.5f, 9.1f, 8.8f}; a.moveTo(q[0], q[1]); for (int i = 2; i < q.length; i += 2) a.lineTo(q[i], q[i + 1]); a.close();
                    p.setStyle(android.graphics.Paint.Style.STROKE); p.setStrokeJoin(android.graphics.Paint.Join.ROUND); c.drawPath(a, p); break; }
                default: { for (int i = 0; i < 8; i++) { c.save(); c.rotate(i * 45, 12, 12); c.drawRoundRect(new android.graphics.RectF(10.3f, 1.5f, 13.7f, 6), 0.8f, 0.8f, p); c.restore(); }
                    p.setStyle(android.graphics.Paint.Style.STROKE); p.setStrokeWidth(3.2f); c.drawCircle(12, 12, 6.2f, p); }
            }
            c.restore();
        }
    }
    static int year() { return Calendar.getInstance().get(Calendar.YEAR); }
    static int colorOr(int c) { return c == 0 ? Color.BLACK : c; }
    static ViewGroup.LayoutParams mm() { return new ViewGroup.LayoutParams(-1, -1); }
    Engine.Chart chart() { return sc.chart; }
}
