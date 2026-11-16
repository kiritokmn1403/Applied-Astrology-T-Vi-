package com.appliedastrology.tuvi.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đa ngôn ngữ (68 ngôn ngữ = hợp Aniyomi/Nekoyomi moko-resources + Mangayomi l10n). Dữ liệu: data/lang/<mã>.json, data/langs.json.
 * Nội dung sách Bửu Đình giữ nguyên văn tiếng Việt; tên giao diện, cung, chương được dịch.
 */
public final class I18n {
    public static final class Lang { public String code, name; public boolean rtl; }
    private final Library.Source src;
    public final List<Lang> langs = new ArrayList<Lang>();
    private final Map<String, Map<String, Object>> dicts = new HashMap<String, Map<String, Object>>();
    private String current = "vi";
    static final Map<String, String> PALACE_KEY = new HashMap<String, String>();
    static {
        String[] k = {"Mệnh", "p_menh", "Phụ Mẫu", "p_phumau", "Phúc Đức", "p_phucduc", "Điền Trạch", "p_dientrach", "Quan Lộc", "p_quanloc", "Nô Bộc", "p_nobOc",
            "Thiên Di", "p_thiendi", "Tật Ách", "p_tatach", "Tài Bạch", "p_taibach", "Tử Tức", "p_tutuc", "Phu Thê", "p_phuthe", "Huynh Đệ", "p_huynhde"};
        for (int i = 0; i < k.length; i += 2) PALACE_KEY.put(k[i], k[i + 1]);
    }

    public I18n(Library.Source src) {
        this.src = src;
        List<Object> l = Json.arr(Json.parse(src.read("data/langs.json")));
        if (l != null) for (Object o : l) {
            Map<String, Object> m = Json.obj(o); Lang g = new Lang();
            g.code = Json.str(m.get("code"), "en"); g.name = Json.str(m.get("name"), g.code); g.rtl = Json.bool(m.get("rtl"), false); langs.add(g);
        }
        dict("vi"); dict("en");
    }
    private synchronized Map<String, Object> dict(String code) {
        Map<String, Object> d = dicts.get(code);
        if (d == null) {
            d = Json.obj(Json.parse(src.read("data/lang/" + code + ".json")));
            if (d == null) d = new HashMap<String, Object>();
            dicts.put(code, d);
        }
        return d;
    }
    public void setLang(String code) { boolean ok = false; for (Lang g : langs) if (g.code.equals(code)) ok = true; current = ok ? code : "en"; dict(current); }
    public String current() { return current; }
    public boolean isRtl() { for (Lang g : langs) if (g.code.equals(current)) return g.rtl; return false; }
    public boolean has(String key) { return dict(current).containsKey(key); }
    public String t(String key) {
        Object v = dict(current).get(key);
        if (v == null) v = dict("en").get(key);
        if (v == null) v = dict("vi").get(key);
        return v == null ? key : v.toString();
    }
    /** t("ab_new", "v", "0.0.4") – thay {v} */
    public String t(String key, Object... kv) {
        String s = t(key);
        for (int i = 0; i + 1 < kv.length; i += 2) s = s.replace("{" + kv[i] + "}", String.valueOf(kv[i + 1]));
        return s;
    }
    public String tIn(String code, String key) { Object v = dict(code).get(key); return v == null ? null : v.toString(); }
    public String palace(String vn) { String k = PALACE_KEY.get(vn); return k == null ? vn : t(k); }
    public String chapterName(Library lib, String id) { String k = Library.CH_KEY.get(id); if (k != null) return t(k); Library.Chapter c = lib.chapter(id); return c != null ? c.name : id; }
}
