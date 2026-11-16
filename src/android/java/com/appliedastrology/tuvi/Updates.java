package com.appliedastrology.tuvi;

import android.os.Build;

import com.appliedastrology.tuvi.core.Json;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Kiểm tra cập nhật (GitHub Releases – link trong sheet "Applied Astrolog Update Link"), tự tải, tự cài; thông tin gỡ lỗi;
 * báo cáo lỗi qua GitHub Issues. Bố cục & luồng tham khảo Aniyomi (AppUpdateChecker, CrashLogUtil) và Mangayomi (check_for_update).
 */
public final class Updates {
    public static final String OWNER = "kiritokmn1403", REPO = "Applied-Astrology-T-Vi-";
    public static final String REPO_URL = "https://github.com/" + OWNER + "/" + REPO, RELEASES_URL = REPO_URL + "/releases";
    public static final String API_LATEST = "https://api.github.com/repos/" + OWNER + "/" + REPO + "/releases/latest";
    final MainActivity a;
    public Map<String, Object> release, asset; public String state = "idle", lastMsg = ""; public boolean newer;
    public interface Cb { void done(Map<String, Object> r); }
    Runnable poller;
    Updates(MainActivity a) { this.a = a; }

    public static int cmpVer(String x, String y) {
        String[] p = String.valueOf(x).replaceFirst("^[vV]", "").split("[.\\-]"), q = String.valueOf(y).replaceFirst("^[vV]", "").split("[.\\-]");
        for (int i = 0; i < Math.max(p.length, q.length); i++) {
            int u = i < p.length ? parse(p[i]) : 0, v = i < q.length ? parse(q[i]) : 0;
            if (u != v) return u < v ? -1 : 1;
        }
        return 0;
    }
    static int parse(String s) { try { return Integer.parseInt(s.replaceAll("[^0-9].*$", "")); } catch (Exception e) { return 0; } }

    /** Chọn APK đúng ABI của thiết bị */
    public static Map<String, Object> pickAsset(List<Object> assets, String abi) {
        String[] order;
        abi = String.valueOf(abi).toLowerCase();
        if (abi.contains("arm64")) order = new String[]{"arm64-v8a", "armeabi-v7a"};
        else if (abi.contains("x86_64")) order = new String[]{"x86_64"};
        else if (abi.contains("armeabi")) order = new String[]{"armeabi-v7a"};
        else order = new String[]{"arm64-v8a", "armeabi-v7a", "x86_64"};
        for (String o : order) for (Object x : assets) { Map<String, Object> m = Json.obj(x); String n = Json.str(m.get("name"), ""); if (n.toLowerCase().endsWith("-" + o + ".apk")) return m; }
        return null;
    }

    public void check(final boolean silent, final Cb cb) {
        a.host.log("update check " + API_LATEST);
        new Thread(new Runnable() { public void run() {
            final Host.Resp r = a.host.httpGet(API_LATEST, 12000);
            a.runOnUiThread(new Runnable() { public void run() {
                Map<String, Object> rel = r.ok ? Json.obj(Json.parse(r.body)) : null;
                if (rel == null || rel.get("tag_name") == null) {
                    a.host.log("update check failed status=" + r.status);
                    lastMsg = a.i18n.t("ab_update_fail"); state = "fail";
                    if (cb != null) cb.done(Json.map("ok", false, "status", r.status)); else if (!silent) a.sc.refreshAbout();
                    return;
                }
                String latest = Json.str(rel.get("tag_name"), "").replaceFirst("^[vV]", "");
                List<Object> assets = Json.arr(rel.get("assets"));
                release = rel; asset = assets == null ? null : pickAsset(assets, a.host.abi());
                newer = cmpVer(latest, Host.VERSION) > 0;
                a.host.set("aa.lastUpdateCheck", Json.map("at", (double) System.currentTimeMillis(), "latest", latest));
                a.host.log("update latest=" + latest + " current=" + Host.VERSION + " newer=" + newer + " asset=" + (asset == null ? "-" : asset.get("name")));
                state = newer ? "new" : "latest";
                lastMsg = newer ? a.i18n.t("ab_new", "v", latest) : a.i18n.t("ab_latest", "v", Host.VERSION);
                if (newer && silent) a.toast(lastMsg);
                if (cb != null) cb.done(Json.map("ok", true, "latest", latest, "current", Host.VERSION, "newer", newer, "asset", asset == null ? null : asset.get("name"), "assets", (long) (assets == null ? 0 : assets.size())));
                else if ("about".equals(a.current)) a.sc.refreshAbout();
            } });
        } }, "aa-check").start();
    }

    public boolean startDownload() {
        if (asset == null) return false;
        boolean ok = a.host.download(Json.str(asset.get("browser_download_url"), null), Json.str(asset.get("name"), "update.apk"));
        a.host.log("update download " + asset.get("name") + " -> " + ok);
        if (ok) { state = "downloading"; poll(); }
        return ok;
    }
    void poll() {
        if (poller != null) a.h.removeCallbacks(poller);
        poller = new Runnable() { public void run() {
            if ("running".equals(a.host.dlState)) { if ("about".equals(a.current)) a.sc.updateProgress(); a.h.postDelayed(this, 500); return; }
            if ("done".equals(a.host.dlState)) { state = "downloaded"; if (a.b("autoInstall") && a.selftest == null) install(); }
            else state = "error";
            if ("about".equals(a.current)) a.sc.refreshAbout();
        } };
        a.h.postDelayed(poller, 500);
    }
    public String install() {
        String r = a.host.installApk(new File(a.host.dlPath));
        a.host.log("update install -> " + r);
        if ("need_permission".equals(r)) lastMsg = a.i18n.t("ab_need_perm"); else if (r == null) lastMsg = a.i18n.t("ab_install_fail");
        return r;
    }
    void onResume() { if ("downloaded".equals(state) && "about".equals(a.current)) a.sc.refreshAbout(); }

    public String debugInfo() {
        StringBuilder s = new StringBuilder();
        s.append("App: Applied Astrology ").append(Host.VERSION).append(" (build ").append(Host.BUILD_DATE).append(", native)\n");
        s.append("Platform: android\nOS: ").append(a.host.os()).append("\nDevice: ").append(a.host.device()).append("\nABI: ").append(a.host.abi());
        s.append("\nEngine: ").append(a.host.engine()).append("\nLanguage: ").append(a.i18n.current()).append(" · Screen: ").append(a.host.screen());
        s.append("\nLiquid Glass: ").append(a.s("lgMode")).append(" · DeX: ").append(a.host.isDex()).append(" · Insets: ").append(a.insetL).append(',').append(a.insetT).append(',').append(a.insetR).append(',').append(a.insetB);
        return s.toString();
    }
    public String reportUrl() {
        List<String[]> cr = a.host.crashList();
        String body = "### Mô tả lỗi / Description\n\n\n### Các bước tái hiện / Steps\n1. \n\n### Thông tin thiết bị / Device info\n```\n" + debugInfo() + "\n```\n" +
            (cr.isEmpty() ? "" : "\n### Lỗi gần đây / Recent crash\n```\n" + cr.get(0)[1].substring(0, Math.min(1500, cr.get(0)[1].length())) + "\n```\n");
        try { return REPO_URL + "/issues/new?title=" + URLEncoder.encode("[Bug] ", "UTF-8") + "&body=" + URLEncoder.encode(body.substring(0, Math.min(6000, body.length())), "UTF-8"); }
        catch (Exception e) { return REPO_URL + "/issues/new"; }
    }
    public String logText() { return "=== " + debugInfo().split("\n")[0] + " ===\n" + debugInfo() + "\n\n=== Native log ===\n" + a.host.readLog(); }
    static final String[][] LICENSES = {
        {"Applied Astrology", "GPL-3.0", REPO_URL},
        {"Âm lịch Việt Nam – thuật toán Hồ Ngọc Đức", "Free for non-commercial use", "https://www.informatik.uni-leipzig.de/~duc/amlich/"},
        {"hochanh/amlich, copthuy/amlich, wolfhong/LunarCalendar (giờ hoàng đạo, tiết khí, ngày lễ)", "MIT", "https://github.com/copthuy/amlich"},
        {"QR Code generator – Project Nayuki", "MIT", "https://github.com/nayuki/QR-Code-generator"},
        {"archisvaze/liquid-glass (hồ sơ khúc xạ, viền phản xạ)", "MIT", "https://github.com/archisvaze/liquid-glass"},
        {"rdev/liquid-glass-react (tham số kính)", "MIT", "https://github.com/rdev/liquid-glass-react"},
        {"dashersw/liquid-glass-js (kiểu thanh/nút/khối kính)", "MIT", "https://github.com/dashersw/liquid-glass-js"},
        {"Aniyomi / Nekoyomi (bố cục Giới thiệu, danh sách ngôn ngữ)", "Apache-2.0", "https://github.com/aniyomiorg/aniyomi"},
        {"Mangayomi (kiểm tra cập nhật, nhật ký, báo cáo lỗi)", "Apache-2.0", "https://github.com/kodjodevf/mangayomi"},
        {"WiX Toolset 3.14 (chỉ dùng khi build MSI)", "MS-RL", "https://github.com/wixtoolset/wix3"},
        {"Nội dung sách \"Tử Vi Ứng Dụng\" – Bửu Đình (Tử Vũ)", "© tác giả", ""}};
    static String sdk() { return String.valueOf(Build.VERSION.SDK_INT); }
}
