package com.appliedastrology.tuvi.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Trợ lý luận giải ngoại tuyến (native): phân tích dữ kiện thật của lá số (chính tinh, độ sáng, Tứ Hóa, Tuần/Triệt,
 * tam phương, đại/tiểu hạn) rồi trích đoạn liên quan nhất trong 611 bài Bửu Đình. Không cần mạng, không phải AI tạo sinh.
 */
public final class Assistant {
    public static final class Answer { public String text; public final List<Library.Hit> refs = new ArrayList<Library.Hit>(); }
    private final Library lib; private final I18n i18n;
    public Assistant(Library lib, I18n i18n) { this.lib = lib; this.i18n = i18n; }

    static final String[][] TOPICS = {
        {"Quan Lộc", "cong danh", "su nghiep", "nghe", "viec lam", "quan loc", "career", "job", "work"},
        {"Tài Bạch", "tai loc", "tien", "tai bach", "giau", "kinh doanh", "money", "wealth", "finance"},
        {"Phu Thê", "vo", "chong", "hon nhan", "tinh duyen", "phu the", "love", "marriage", "spouse"},
        {"Tử Tức", "con cai", "tu tuc", "children"},
        {"Tật Ách", "suc khoe", "benh", "tat ach", "health"},
        {"Phụ Mẫu", "cha me", "bo me", "phu mau", "parents"},
        {"Huynh Đệ", "anh em", "huynh de", "siblings"},
        {"Điền Trạch", "nha cua", "dat dai", "dien trach", "property", "house"},
        {"Nô Bộc", "ban be", "dong nghiep", "no boc", "friends"},
        {"Thiên Di", "di xa", "xuat ngoai", "thien di", "travel", "abroad"},
        {"Phúc Đức", "phuc duc", "ho hang", "mo ma", "fortune", "ancestors"},
        {"Mệnh", "menh", "tinh cach", "ban than", "life", "personality"}};
    static final String[] YEAR_KEYS = {"nam nay", "han", "van han", "tieu han", "dai han", "this year", "forecast", "luu nien"};

    private boolean vi() { return "vi".equals(i18n.current()); }
    private String L(String v, String e) { return vi() ? v : e; }
    static int palaceByName(Engine.Chart c, String name) { for (int i = 0; i < 12; i++) if (c.palaces[i].name.equals(name)) return i; return c.menh; }
    static double br(String b) { if ("M".equals(b) || "V".equals(b)) return 2; if ("Đ".equals(b)) return 1; if ("H".equals(b)) return -2; return 0; }

    public static double assess(Engine.Chart c, int chi) {
        Engine.Palace p = c.palaces[chi]; double s = 0; int[] tp = c.tamPhuong(chi);
        for (Engine.Star m : p.major) s += br(m.bright);
        if (p.major.isEmpty()) s -= 1;
        s += p.good.size() * 0.35 - p.bad.size() * 0.45;
        for (String[] h : p.hoa) s += h[0].equals("Hóa Kỵ") ? -1.5 : 1;
        if (p.tuan) s -= 0.5; if (p.triet) s -= 0.8;
        for (int j = 1; j < 4; j++) for (String[] h : c.palaces[tp[j]].hoa) s += h[0].equals("Hóa Kỵ") ? -0.5 : 0.4;
        return s;
    }
    String verdict(double s) {
        if (s >= 3) return L("Khá thuận lợi", "Quite favourable");
        if (s >= 0.5) return L("Trung bình khá", "Moderately favourable");
        if (s > -2) return L("Bình thường, cần nỗ lực", "Average, effort needed");
        return L("Nhiều thử thách", "Challenging");
    }
    static String join(List<String> l, String sep) { StringBuilder b = new StringBuilder(); for (int i = 0; i < l.size(); i++) { if (i > 0) b.append(sep); b.append(l.get(i)); } return b.toString(); }

    public String describePalace(Engine.Chart c, int chi) {
        Engine.Palace p = c.palaces[chi]; List<String> out = new ArrayList<String>(), ms = new ArrayList<String>();
        for (Engine.Star m : p.major) ms.add(m.name + " (" + m.bright + ")");
        out.add("• " + L("Cung ", "Palace ") + i18n.palace(p.name) + " " + L("tại ", "at ") + p.canChi + ": " +
            (ms.isEmpty() ? L("Vô Chính Diệu – mượn chính tinh cung xung chiếu ", "no major star – borrows from the opposite palace ") + c.palaces[(chi + 6) % 12].chiName : join(ms, ", ")));
        List<String> g = new ArrayList<String>(), b = new ArrayList<String>();
        for (Engine.Star s : p.good) g.add(s.name); for (Engine.Star s : p.bad) b.add(s.name);
        if (!g.isEmpty()) out.add("  " + L("Cát tinh: ", "Auspicious: ") + join(g, ", "));
        if (!b.isEmpty()) out.add("  " + L("Sát/bại tinh: ", "Malefic: ") + join(b, ", "));
        for (String[] h : p.hoa) out.add("  " + h[0] + " " + L("hóa từ ", "transforms ") + h[1]);
        if (p.tuan || p.triet) out.add("  " + L("Gặp ", "Has ") + (p.tuan ? "Tuần " : "") + (p.triet ? "Triệt" : "") + L(" – tốt xấu đều giảm bớt, thường chậm trước nhanh sau.", " – effects are dampened."));
        int[] tp = c.tamPhuong(chi); List<String> tps = new ArrayList<String>();
        for (int k = 1; k < 4; k++) {
            Engine.Palace q = c.palaces[tp[k]]; List<String> mq = new ArrayList<String>();
            for (Engine.Star s : q.major) mq.add(s.name);
            tps.add(i18n.palace(q.name) + ": " + (mq.isEmpty() ? "VCD" : join(mq, ", ")));
        }
        out.add("  " + L("Tam phương tứ chính – ", "Triad – ") + join(tps, "; "));
        out.add("  " + L("Nhận định sơ bộ: ", "Preliminary assessment: ") + verdict(assess(c, chi)) + L(" (theo độ sáng chính tinh, cát/sát tinh, Tứ Hóa, Tuần Triệt).", " (from star brightness, auspicious/malefic stars, transformations, voids)."));
        return join(out, "\n");
    }

    public String overview(Engine.Chart c) {
        List<String> out = new ArrayList<String>();
        out.add(L("Lá số của ", "Chart of ") + c.name + ": " + c.amDuong + ", " + L("năm ", "year ") + c.yearCanChi + ", " + L("bản mệnh ", "element ") + c.napAm + ", " + c.cucName + ".");
        String rel;
        if (c.menhCuc.equals("sinh")) rel = L("Mệnh sinh Cục: người tận tụy, cống hiến cho hoàn cảnh.", "Life element produces the bureau: devoted, giving.");
        else if (c.menhCuc.equals("được sinh")) rel = L("Cục sinh Mệnh: được hoàn cảnh nâng đỡ, thời thế ưu ái.", "Bureau produces the life element: supported by circumstances.");
        else if (c.menhCuc.equals("khắc")) rel = L("Mệnh khắc Cục: phải tự lực chinh phục hoàn cảnh.", "Life element controls the bureau: must conquer circumstances.");
        else if (c.menhCuc.equals("bị khắc")) rel = L("Cục khắc Mệnh: hoàn cảnh gây sức ép, cần kiên nhẫn.", "Bureau controls the life element: pressure from circumstances.");
        else rel = L("Mệnh Cục bình hòa: cân bằng với môi trường.", "Life and bureau in harmony.");
        out.add(rel);
        out.add(c.amDuongLy + ". " + L("Mệnh chủ ", "Life lord ") + c.menhChu + ", " + L("Thân chủ ", "body lord ") + c.thanChu + ".");
        out.add(describePalace(c, c.menh));
        if (c.than != c.menh) out.add(L("Thân cư ", "Body palace in ") + i18n.palace(c.palaces[c.than].name) + ":\n" + describePalace(c, c.than));
        if (c.currentDaiHan >= 0) out.add(L("Đại hạn hiện tại (tuổi ", "Current decade (age ") + c.age + "):\n" + describePalace(c, c.currentDaiHan));
        return join(out, "\n\n");
    }

    private static final class Quote { Library.Hit hit; String text; double score; }
    List<Quote> quotes(Engine.Chart c, int chi) {
        List<Library.Hit> rel = lib.related(c, chi); if (rel.size() > 6) rel = rel.subList(0, 6);
        Engine.Palace p = c.palaces[chi]; List<String> keys = new ArrayList<String>(); keys.add(Fold.fold(p.name));
        for (Engine.Star s : p.major) keys.add(Fold.fold(s.name));
        List<Quote> found = new ArrayList<Quote>();
        for (Library.Hit r : rel) {
            Library.Article a = lib.get(r.ch, r.i); if (a == null) continue;
            String[] paras = a.body.split("\\n\\s*\\n|\\n"); String best = null; int bestS = 0;
            for (String para : paras) {
                if (para.length() < 60) continue;
                String fp = Fold.fold(para); int s = 0;
                for (int q = 0; q < keys.size(); q++) if (fp.contains(keys.get(q))) s += q == 0 ? 2 : 3;
                if (s > bestS) { bestS = s; best = para; }
            }
            if (best != null) {
                Quote q = new Quote(); q.hit = r; q.hit.title = a.title; q.text = best.length() > 600 ? best.substring(0, 600) + "…" : best; q.score = bestS + r.score / 10.0; found.add(q);
            }
        }
        Collections.sort(found, new Comparator<Quote>() { public int compare(Quote x, Quote y) { return Double.compare(y.score, x.score); } });
        return found.size() > 3 ? found.subList(0, 3) : found;
    }

    public Answer answer(Engine.Chart c, String question) {
        Answer ans = new Answer();
        if (c == null) { ans.text = i18n.t("as_need_chart"); return ans; }
        String fq = " " + Fold.fold(question).replaceAll("[^a-z0-9]+", " ") + " ";
        int chi = -1; List<String> text = new ArrayList<String>(); boolean isYear = false;
        for (String k : YEAR_KEYS) if (fq.contains(" " + k + " ")) isYear = true;
        outer:
        for (String[] tp : TOPICS) for (int j = 1; j < tp.length; j++) if (fq.contains(" " + tp[j] + " ")) { chi = palaceByName(c, tp[0]); break outer; }
        if (isYear) {
            int th = c.currentTieuHan, dh = c.currentDaiHan;
            text.add(L("Hạn năm ", "Forecast for ") + c.viewYear + " (" + c.viewYearCanChi + "), " + L("tuổi âm ", "lunar age ") + c.age + ".");
            if (dh >= 0) text.add(L("Đại hạn đang đi qua cung ", "Decade runs through ") + i18n.palace(c.palaces[dh].name) + ":\n" + describePalace(c, dh));
            if (th >= 0) text.add(L("Tiểu hạn tại cung ", "Annual cycle at ") + i18n.palace(c.palaces[th].name) + ":\n" + describePalace(c, th));
            List<String> luu = new ArrayList<String>();
            for (int k = 0; k < 12; k++) if (!c.palaces[k].luu.isEmpty()) luu.add(i18n.palace(c.palaces[k].name) + ": " + join(c.palaces[k].luu, ", "));
            text.add(L("Sao lưu niên: ", "Annual stars: ") + join(luu, " | "));
            if (chi < 0) chi = th >= 0 ? th : c.menh;
        } else {
            if (chi < 0) chi = c.menh;
            text.add(describePalace(c, chi));
        }
        List<Quote> qs = quotes(c, chi);
        if (!qs.isEmpty()) {
            text.add(L("Trích bình giải Bửu Đình:", "From Bửu Đình's commentary (original Vietnamese):"));
            for (Quote q : qs) { text.add("« " + q.text + " »\n— " + q.hit.title); ans.refs.add(q.hit); }
        }
        text.add(L("Lưu ý: đây là phân tích tham khảo theo nguyên tắc Tử Vi, mang tính ứng dụng – \"Tử Vi là để hiểu mình, không phải để ỷ lại số mệnh\".", "Note: reference analysis only."));
        ans.text = join(text, "\n\n");
        return ans;
    }
}
