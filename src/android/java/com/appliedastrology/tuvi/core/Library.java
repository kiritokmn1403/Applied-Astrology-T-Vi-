package com.appliedastrology.tuvi.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Applied Astrology v0.0.4 – Thư viện Bửu Đình (611 bài / 13 chương) native.
 * Nạp lười từng chương (data/lib/NN.json); tìm kiếm qua libaacore (Android, nếu có) hoặc Java; bài liên quan theo cung.
 */
public final class Library {
    public interface Source { String read(String path); }
    public interface NativeSearch { String search(String folded, int max); }

    public static final class Chapter { public String id, name; public int n, count; public final List<String> titles = new ArrayList<String>(); public final List<Integer> lens = new ArrayList<Integer>(); }
    public static final class Article { public String ch, title, body; public int i; }
    public static final class Hit { public String ch, title, snippet, why; public int i, o = -1, score; }

    public static final Map<String, String> CH_KEY = new HashMap<String, String>();
    static {
        String[] k = {"chinh-tinh", "ch_chinh_tinh", "can-tinh", "ch_can_tinh", "chi-tinh", "ch_chi_tinh", "sao-khac", "ch_sao_khac", "cac-cung", "ch_cac_cung",
            "72-cach", "ch_72_cach", "an-sao", "ch_an_sao", "han", "ch_han", "la-so-nam", "ch_la_so_nam", "la-so-nu", "ch_la_so_nu", "tu-dien", "ch_tu_dien",
            "2-chinh-tinh", "ch_2_chinh_tinh", "cac-bo", "ch_cac_bo"};
        for (int i = 0; i < k.length; i += 2) CH_KEY.put(k[i], k[i + 1]);
    }

    private final Source src; private NativeSearch nativeSearch;
    public final List<Chapter> chapters = new ArrayList<Chapter>();
    private final Map<String, Chapter> byId = new HashMap<String, Chapter>();
    private final Map<String, List<Object>> loaded = new HashMap<String, List<Object>>();
    private final Map<String, String> foldCache = new HashMap<String, String>();

    public Library(Source src) {
        this.src = src;
        Map<String, Object> idx = Json.obj(Json.parse(src.read("data/lib/index.json")));
        if (idx == null) return;
        for (Object o : Json.arr(idx.get("chapters"))) {
            Map<String, Object> m = Json.obj(o); Chapter c = new Chapter();
            c.id = Json.str(m.get("id"), ""); c.n = Json.num(m.get("n"), 0); c.name = Json.str(m.get("name"), ""); c.count = Json.num(m.get("count"), 0);
            for (Object a : Json.arr(m.get("articles"))) { Map<String, Object> am = Json.obj(a); c.titles.add(Json.str(am.get("t"), "")); c.lens.add(Json.num(am.get("len"), 0)); }
            chapters.add(c); byId.put(c.id, c);
        }
    }
    public void setNativeSearch(NativeSearch ns) { nativeSearch = ns; }
    public Chapter chapter(String id) { return byId.get(id); }
    public Chapter chapterByNo(int n) { for (Chapter c : chapters) if (c.n == n) return c; return null; }
    public int totalArticles() { int n = 0; for (Chapter c : chapters) n += c.count; return n; }
    public String title(String id, int i) { Chapter c = byId.get(id); return c != null && i >= 0 && i < c.titles.size() ? c.titles.get(i) : ""; }

    private synchronized List<Object> load(String id) {
        List<Object> a = loaded.get(id);
        if (a != null) return a;
        Chapter c = byId.get(id); if (c == null) return null;
        String txt = src.read("data/lib/" + (c.n < 10 ? "0" : "") + c.n + ".json");
        a = Json.arr(Json.parse(txt));
        if (a != null) loaded.put(id, a);
        return a;
    }
    public Article get(String id, int i) {
        List<Object> a = load(id);
        if (a == null || i < 0 || i >= a.size()) return null;
        List<Object> x = Json.arr(a.get(i)); Article r = new Article();
        r.ch = id; r.i = i; r.title = Json.str(x.get(0), ""); r.body = Json.str(x.get(1), "");
        return r;
    }
    private String folded(String id, String k, String text) {
        String key = id + ":" + k; String f = foldCache.get(key);
        if (f == null) { f = Fold.fold(text); foldCache.put(key, f); }
        return f;
    }

    /** Tìm kiếm toàn văn (có dấu hoặc không dấu). */
    public List<Hit> search(String q) {
        String fq = Fold.fold(q).replaceAll("\\s+", " ").trim();
        List<Hit> res = new ArrayList<Hit>();
        if (fq.length() < 2) return res;
        if (nativeSearch != null) {
            String js = null;
            try { js = nativeSearch.search(fq, 300); } catch (Throwable t) { js = null; }
            List<Object> arr = Json.arr(Json.parse(js));
            if (arr != null) {
                for (Object o : arr) {
                    Map<String, Object> m = Json.obj(o); Chapter c = chapterByNo(Json.num(m.get("c"), 0)); if (c == null) continue;
                    Hit h = new Hit(); h.ch = c.id; h.i = Json.num(m.get("i"), 0); h.o = Json.num(m.get("o"), 0); h.score = Json.num(m.get("s"), 1); h.title = title(c.id, h.i);
                    res.add(h);
                }
                for (int k = 0; k < Math.min(40, res.size()); k++) {
                    Hit h = res.get(k); Article a = get(h.ch, h.i);
                    if (a != null) { int pos = folded(h.ch, String.valueOf(h.i), a.body).indexOf(fq); h.snippet = snippet(a.body, Math.max(pos, 0), fq.length()); }
                }
                return res;
            }
        }
        String[] words = fq.split(" ");
        for (Chapter c : chapters) {
            List<Object> arr = load(c.id); if (arr == null) continue;
            for (int k = 0; k < arr.size(); k++) {
                List<Object> x = Json.arr(arr.get(k)); String t = Json.str(x.get(0), ""), body = Json.str(x.get(1), "");
                String ft = folded(c.id, "t" + k, t), fb = folded(c.id, String.valueOf(k), body);
                int pos = fb.indexOf(fq), score = 0;
                if (ft.contains(fq)) score += 50;
                if (pos >= 0) {
                    score += 10; int cnt = 0, p = pos;
                    while (p >= 0 && cnt < 20) { cnt++; p = fb.indexOf(fq, p + fq.length()); }
                    score += cnt;
                } else if (words.length > 1) {
                    boolean all = true;
                    for (String w : words) if (!fb.contains(w) && !ft.contains(w)) { all = false; break; }
                    if (all) { score += 3; pos = fb.indexOf(words[0]); }
                }
                if (score > 0) { Hit h = new Hit(); h.ch = c.id; h.i = k; h.title = t; h.score = score; h.snippet = snippet(body, Math.max(pos, 0), fq.length()); res.add(h); }
            }
        }
        Collections.sort(res, new Comparator<Hit>() { public int compare(Hit a, Hit b) { return b.score - a.score; } });
        return res;
    }
    static String snippet(String body, int pos, int len) {
        pos = Math.min(pos, body.length());
        int s = Math.max(0, pos - 70), e = Math.min(body.length(), pos + len + 110);
        return (s > 0 ? "…" : "") + body.substring(s, e).replaceAll("\\s+", " ") + (e < body.length() ? "…" : "");
    }

    private static final Pattern[] CHI_RX = new Pattern[12];
    static {
        String[] r = {"t[ýí]", "sửu", "dần", "mão", "thìn", "t[ịỵ]", "ngọ", "mùi", "thân", "dậu", "tuất", "hợi"};
        String B1 = "(?:^|[\\s,.;:()\\-/])", B2 = "(?:$|[\\s,.;:()\\-/])";
        for (int i = 0; i < 12; i++) CHI_RX[i] = Pattern.compile(B1 + r[i] + B2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /** Bài viết liên quan tới một cung (dựa trên tiêu đề, không cần nạp toàn văn) – cùng thuật toán bản 0.0.3 */
    public List<Hit> related(Engine.Chart chart, final int chi) {
        Engine.Palace p = chart.palaces[chi];
        final List<Hit> out = new ArrayList<Hit>(); final Map<String, Hit> seen = new LinkedHashMap<String, Hit>();
        List<String> majors = new ArrayList<String>(); for (Engine.Star s : p.major) majors.add(s.name);
        List<String> minors = new ArrayList<String>();
        for (Engine.Star s : p.good) minors.add(s.name); for (Engine.Star s : p.bad) minors.add(s.name); for (Engine.Star s : p.neutral) minors.add(s.name);
        String pname = Fold.fold(p.name);
        Chapter cc = byId.get("cac-cung");
        if (cc != null) for (int k = 0; k < cc.titles.size(); k++) if (Fold.fold(cc.titles.get(k)).contains(pname)) push(out, seen, "cac-cung", k, p.name, 30);
        for (String mj : majors) {
            String fs = Fold.fold(mj);
            Chapter ct = byId.get("chinh-tinh");
            if (ct != null) for (int k = 0; k < ct.titles.size(); k++) if (Fold.fold(ct.titles.get(k)).contains(fs)) push(out, seen, "chinh-tinh", k, mj, 20);
            Chapter c72 = byId.get("72-cach");
            if (c72 != null) for (int k = 0; k < c72.titles.size(); k++) {
                String t = c72.titles.get(k);
                if (Fold.fold(t).contains(fs)) { int sc = 10; if (CHI_RX[chi].matcher(t).find()) sc += 40; push(out, seen, "72-cach", k, mj, sc); }
            }
        }
        if (majors.size() == 2) {
            String f1 = Fold.fold(majors.get(0)), f2 = Fold.fold(majors.get(1)); Chapter c2 = byId.get("2-chinh-tinh");
            if (c2 != null) for (int k = 0; k < c2.titles.size(); k++) { String ft = Fold.fold(c2.titles.get(k)); if (ft.contains(f1) && ft.contains(f2)) push(out, seen, "2-chinh-tinh", k, majors.get(0) + " – " + majors.get(1), 45); }
        }
        if (majors.isEmpty()) {
            Chapter c72 = byId.get("72-cach");
            if (c72 != null) for (int k = 0; k < c72.titles.size(); k++) { String t = c72.titles.get(k); if (Fold.fold(t).contains("vcd") && CHI_RX[chi].matcher(t).find()) push(out, seen, "72-cach", k, "VCD", 35); }
        }
        String[] groups = {"can-tinh", "chi-tinh", "sao-khac", "cac-bo"};
        for (String mn : minors) {
            String fm = Fold.fold(mn); if (fm.length() < 5) continue;
            for (String g : groups) { Chapter c = byId.get(g); if (c == null) continue; for (int k = 0; k < c.titles.size(); k++) if (Fold.fold(c.titles.get(k)).contains(fm)) push(out, seen, g, k, mn, 6); }
        }
        // sắp xếp ổn định giống Array.prototype.sort của V8 (TimSort ổn định)
        List<Hit> sorted = new ArrayList<Hit>(out);
        Collections.sort(sorted, new Comparator<Hit>() { public int compare(Hit x, Hit y) { return y.score - x.score; } });
        return sorted.size() > 12 ? new ArrayList<Hit>(sorted.subList(0, 12)) : sorted;
    }
    private void push(List<Hit> out, Map<String, Hit> seen, String ch, int idx, String why, int score) {
        String key = ch + ":" + idx; Hit h = seen.get(key);
        if (h != null) { h.score += score; return; }
        h = new Hit(); h.ch = ch; h.i = idx; h.title = title(ch, idx); h.why = why; h.score = score; seen.put(key, h); out.add(h);
    }
}
