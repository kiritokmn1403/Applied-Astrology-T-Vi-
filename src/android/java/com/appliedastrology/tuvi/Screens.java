package com.appliedastrology.tuvi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.appliedastrology.tuvi.core.Assistant;
import com.appliedastrology.tuvi.core.Cal;
import com.appliedastrology.tuvi.core.Engine;
import com.appliedastrology.tuvi.core.Fold;
import com.appliedastrology.tuvi.core.Json;
import com.appliedastrology.tuvi.core.Library;
import com.appliedastrology.tuvi.core.Lunar;
import com.appliedastrology.tuvi.core.Theme;
import com.appliedastrology.tuvi.GlassViews.Frame;

import io.nayuki.qrcodegen.QrCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Các màn hình native: Lá Số, Âm Lịch, Thư Viện, Luận Giải, Hồ Sơ, Cài Đặt, Giới Thiệu. */
public final class Screens {
    final MainActivity a; final Ui ui; final Updates updates;
    Engine.Chart chart; int selChi = -1;
    // biểu mẫu lá số
    Engine.Input form;
    EditText fName, fYear, fView; Spinner fGender, fCal, fDay, fMonth, fHour; CheckBox fLeap; View detailCard;
    // âm lịch
    int calY, calM, calD;
    // thư viện: home | chapter:<id> | read:<id>:<i> | search:<q>
    String libMode = "home"; List<Library.Hit> lastResults; String lastQuery = "";
    // luận giải
    final List<Object[]> chat = new ArrayList<Object[]>(); // {role, text, refs}
    View aboutRoot; TextView dlState; View dlBar;
    static final String[] CHI_HOURS = {"23–01", "01–03", "03–05", "05–07", "07–09", "09–11", "11–13", "13–15", "15–17", "17–19", "19–21", "21–23"};

    Screens(MainActivity a) {
        this.a = a; ui = a.ui; updates = new Updates(a);
        Calendar n = Calendar.getInstance(); calY = n.get(Calendar.YEAR); calM = n.get(Calendar.MONTH) + 1; calD = n.get(Calendar.DAY_OF_MONTH);
        form = new Engine.Input(); form.name = "Nguyễn Văn An"; form.gender = "Nam"; form.day = 15; form.month = 8; form.year = 1995; form.hourChi = 6;
        Map<String, Object> last = Json.obj(a.host.get("aa.last")); if (last != null) form = inputFrom(last);
        form.viewYear = MainActivity.year();
        try { chart = Engine.build(withMode(form)); selChi = chart.menh; } catch (Exception e) { chart = null; }
    }
    String t(String k) { return a.i18n.t(k); }
    String t(String k, Object... kv) { return a.i18n.t(k, kv); }
    Theme th() { return a.gcfg.th; }

    View build(String v) {
        if ("calendar".equals(v)) return calendar();
        if ("library".equals(v)) return library();
        if ("assistant".equals(v)) return assistant();
        if ("saved".equals(v)) return saved();
        if ("settings".equals(v)) return settings();
        if ("about".equals(v)) return about();
        return chartScreen();
    }
    LinearLayout page() { return ui.col(); }
    Frame card(LinearLayout parent) { Frame f = a.card(); parent.addView(f, a.cardLp()); return f; }

    // =====================================================================================
    // LÁ SỐ
    // =====================================================================================
    static Engine.Input inputFrom(Map<String, Object> o) {
        Engine.Input in = new Engine.Input();
        in.name = Json.str(o.get("name"), "Thân Chủ"); in.gender = Json.str(o.get("gender"), "Nam"); in.calendar = Json.str(o.get("calendar"), "solar");
        in.day = Json.num(o.get("day"), 1); in.month = Json.num(o.get("month"), 1); in.year = Json.num(o.get("year"), 1990); in.hourChi = Json.num(o.get("hourChi"), 0); in.leap = Json.bool(o.get("leap"), false);
        return in;
    }
    static Map<String, Object> inputMap(Engine.Input in) {
        return Json.map("name", in.name, "gender", in.gender, "calendar", in.calendar, "day", (long) in.day, "month", (long) in.month, "year", (long) in.year, "hourChi", (long) in.hourChi, "leap", in.leap);
    }
    Engine.Input withMode(Engine.Input in) { in.leapMode = a.s("leap"); return in; }
    List<String> nums(int from, int to) { List<String> l = new ArrayList<String>(); for (int i = from; i <= to; i++) l.add(String.valueOf(i)); return l; }

    View chartScreen() {
        LinearLayout p = page();
        Frame fc = card(p);
        Ui.Flow fl = ui.flow();
        fName = ui.edit(form.name, t("f_name"), false); fName.setTag("fName");
        fl.addView(fieldBox(t("f_name"), fName, 220));
        fGender = ui.spinner(Arrays.asList(t("male"), t("female")), "Nữ".equals(form.gender) ? 1 : 0, null); fGender.setTag("fGender");
        fl.addView(fieldBox(t("f_gender"), fGender, 104));
        fCal = ui.spinner(Arrays.asList(t("solar"), t("lunar")), "lunar".equals(form.calendar) ? 1 : 0, new Ui.OnPick() { public void pick(int pos) { fLeap.setVisibility(pos == 1 ? View.VISIBLE : View.GONE); } });
        fCal.setTag("fCal");
        fl.addView(fieldBox(t("f_calendar"), fCal, 130));
        fDay = ui.spinner(nums(1, 31), form.day - 1, null); fDay.setTag("fDay"); fl.addView(fieldBox(t("f_day"), fDay, 80));
        fMonth = ui.spinner(nums(1, 12), form.month - 1, null); fMonth.setTag("fMonth"); fl.addView(fieldBox(t("f_month"), fMonth, 80));
        fYear = ui.edit(String.valueOf(form.year), "1995", true); fYear.setTag("fYear"); fl.addView(fieldBox(t("f_year"), fYear, 90));
        List<String> hours = new ArrayList<String>(); for (int i = 0; i < 12; i++) hours.add(Engine.CHI[i] + " (" + CHI_HOURS[i] + ")");
        fHour = ui.spinner(hours, form.hourChi, null); fHour.setTag("fHour"); fl.addView(fieldBox(t("f_hour"), fHour, 150));
        fView = ui.edit(String.valueOf(form.viewYear > 0 ? form.viewYear : MainActivity.year()), "", true); fView.setTag("fView"); fl.addView(fieldBox(t("f_viewyear"), fView, 110));
        fc.addView(fl, Ui.fill());
        fLeap = ui.check(t("f_leap"), form.leap, null); fLeap.setVisibility("lunar".equals(form.calendar) ? View.VISIBLE : View.GONE); fc.addView(fLeap);
        Ui.Flow btns = ui.flow();
        btns.addView(tag(ui.button(t("btn_build"), true, new View.OnClickListener() { public void onClick(View v) { build(); } }), "btnBuild"));
        btns.addView(tag(ui.button(t("btn_save"), false, new View.OnClickListener() { public void onClick(View v) { saveProfile(); } }), "btnSave"));
        btns.addView(tag(ui.button(t("btn_png"), false, new View.OnClickListener() { public void onClick(View v) { exportPng(); } }), "btnPng"));
        btns.addView(tag(ui.button(t("btn_qr"), false, new View.OnClickListener() { public void onClick(View v) { showQr(); } }), "btnQr"));
        btns.addView(tag(ui.button(t("btn_share"), false, new View.OnClickListener() { public void onClick(View v) { share(); } }), "btnShare"));
        btns.addView(tag(ui.button(t("btn_print"), false, new View.OnClickListener() { public void onClick(View v) { print(); } }), "btnPrint"));
        LinearLayout.LayoutParams bp = Ui.fill(); bp.topMargin = ui.dp(6); fc.addView(btns, bp);
        if (chart != null) {
            String layout = a.s("layout"); boolean desk = a.isDesktop();
            if ("auto".equals(layout)) layout = a.getResources().getConfiguration().screenWidthDp < 560 && !desk ? "list" : "board";
            LinearLayout area = desk ? ui.row() : ui.col();
            if (desk) area.setGravity(Gravity.TOP);
            LinearLayout left = ui.col();
            ChartViews.Opts o = opts();
            ChartViews.Board.OnPalace pick = new ChartViews.Board.OnPalace() { public void pick(int chi) { selChi = chi; a.show("chart"); if (!a.isDesktop()) a.scrollTo(detailCard); } };
            if ("board".equals(layout)) {
                Frame bc = a.card(); bc.setPadding(ui.dp(4), ui.dp(4), ui.dp(4), ui.dp(4));
                HorizontalScrollView hs = new HorizontalScrollView(a); hs.setFillViewport(true);
                ChartViews.Board board = new ChartViews.Board(a); board.setTag("board"); board.set(chart, o, pick);
                hs.addView(board); bc.addView(hs, Ui.fill()); left.addView(bc, a.cardLp());
            } else {
                LinearLayout lst = ui.col(); lst.setTag("palaceList");
                ChartViews.Item c0 = new ChartViews.Item(a, chart, -1, o, null); c0.fontScale = ui.fs; lst.addView(c0, ui.margins(Ui.fill(), 0, 0, 0, 6));
                for (int i = 0; i < 12; i++) { ChartViews.Item it = new ChartViews.Item(a, chart, (chart.menh + i) % 12, o, pick); it.fontScale = ui.fs; it.setTag("palace:" + ((chart.menh + i) % 12)); lst.addView(it, ui.margins(Ui.fill(), 0, 0, 0, 6)); }
                left.addView(lst, Ui.fill());
            }
            Frame dc = a.card(); detailCard = dc; dc.setTag("detailBox");
            fillDetail(dc);
            if (desk) { area.addView(left, Ui.weight(3)); LinearLayout.LayoutParams rp = Ui.weight(2); rp.leftMargin = ui.dp(8); area.addView(dc, rp); }
            else { area.addView(left, Ui.fill()); area.addView(dc, a.cardLp()); }
            p.addView(area, Ui.fill());
        }
        return p;
    }
    View tag(View v, String tag) { v.setTag(tag); return v; }
    LinearLayout fieldBox(String label, View control, int minDp) {
        LinearLayout c = ui.col(); c.addView(ui.text(label, 12, th().muted, false)); c.addView(control, Ui.lp(ui.dp(minDp), -2)); c.setPadding(0, 0, ui.dp(4), ui.dp(2)); return c;
    }
    ChartViews.Opts opts() {
        ChartViews.Opts o = new ChartViews.Opts(); o.sel = selChi; o.th = th(); o.i18n = a.i18n; o.ts = a.b("ts"); o.bs = a.b("bs"); o.tt = a.b("tt"); o.luu = a.b("luu");
        o.paper = Theme.paper(a.s("paper")); o.glass = a.gcfg.chart && !"off".equals(a.gcfg.mode); return o;
    }
    Engine.Input readForm() {
        Engine.Input in = new Engine.Input();
        in.name = fName.getText().toString().trim(); if (in.name.isEmpty()) in.name = "Thân Chủ";
        in.gender = fGender.getSelectedItemPosition() == 1 ? "Nữ" : "Nam"; in.calendar = fCal.getSelectedItemPosition() == 1 ? "lunar" : "solar";
        in.day = fDay.getSelectedItemPosition() + 1; in.month = fMonth.getSelectedItemPosition() + 1;
        try { in.year = Integer.parseInt(fYear.getText().toString().trim()); } catch (Exception e) { in.year = 0; }
        in.hourChi = fHour.getSelectedItemPosition(); in.leap = fLeap.isChecked();
        try { in.viewYear = Integer.parseInt(fView.getText().toString().trim()); } catch (Exception e) { in.viewYear = MainActivity.year(); }
        return withMode(in);
    }
    void setForm(Engine.Input in) { form = in; if (form.viewYear <= 0) form.viewYear = MainActivity.year(); }
    Engine.Chart build() {
        Engine.Input in = fName != null && "chart".equals(a.current) ? readForm() : withMode(form);
        a.host.log("build " + in.calendar + "," + in.day + "," + in.month + "," + in.year + "," + in.hourChi);
        if (!(in.year >= 1900 && in.year <= 2100) || ("solar".equals(in.calendar) && !Lunar.validSolar(in.day, in.month, in.year))) { a.toast(t("invalid_date")); return null; }
        try { chart = Engine.build(in); }
        catch (IllegalArgumentException e) { a.toast(t("invalid_lunar")); return null; }
        form = in; a.host.set("aa.last", inputMap(in)); selChi = chart.menh;
        if ("chart".equals(a.current)) a.show("chart");
        return chart;
    }
    void fillDetail(LinearLayout dc) {
        Engine.Palace p = chart.palaces[selChi < 0 ? chart.menh : selChi]; int chi = p.chi;
        dc.addView(ui.h2(a.i18n.palace(p.name) + " · " + p.canChi + " (" + p.element + ")"));
        dc.addView(ui.hint(t("dai_han") + " " + p.daiHan + "–" + (p.daiHan + 9) + " · " + t("tieu_han") + " " + p.tieuHan));
        dc.addView(ui.grp(t("major_stars")));
        if (p.major.isEmpty()) dc.addView(ui.text(t("vcd"), 14, th().muted, false));
        for (Engine.Star s : p.major) dc.addView(ui.text(s.name + " – " + s.bright + " · " + s.element, 14, th().element(s.element), true));
        stars(dc, "good_stars", p.good, th().good); stars(dc, "bad_stars", p.bad, th().bad); stars(dc, "other_stars", p.neutral, th().text);
        if (!p.luu.isEmpty()) { dc.addView(ui.grp(t("luu_stars") + " " + chart.viewYear)); dc.addView(ui.text(join(p.luu, ", "), 14, th().purple, false)); }
        List<String> rg = new ArrayList<String>(); if (p.ringTrangSinh != null) rg.add(p.ringTrangSinh); if (p.ringBacSi != null) rg.add(p.ringBacSi); if (p.ringThaiTue != null) rg.add(p.ringThaiTue);
        dc.addView(ui.grp(t("rings"))); dc.addView(ui.body(join(rg, " · ")));
        int[] tp = chart.tamPhuong(chi); List<String> tpn = new ArrayList<String>();
        for (int k = 1; k < 4; k++) { Engine.Palace q = chart.palaces[tp[k]]; List<String> ms = new ArrayList<String>(); for (Engine.Star s : q.major) ms.add(s.name); tpn.add(a.i18n.palace(q.name) + " (" + q.chiName + "): " + (ms.isEmpty() ? t("vcd") : join(ms, ", "))); }
        dc.addView(ui.grp(t("tam_phuong"))); dc.addView(ui.body(join(tpn, " | ")));
        dc.addView(ui.h3(t("related")));
        List<Library.Hit> rel = a.lib.related(chart, chi);
        if (rel.isEmpty()) dc.addView(ui.hint(t("no_related")));
        for (final Library.Hit h : rel) dc.addView(listItem(h.title, a.i18n.chapterName(a.lib, h.ch) + " · " + h.why, null, open(h.ch, h.i)), ui.margins(Ui.fill(), 0, 4, 0, 0));
    }
    void stars(LinearLayout dc, String key, List<Engine.Star> l, int color) {
        if (l.isEmpty()) return;
        dc.addView(ui.grp(t(key)));
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int i = 0; i < l.size(); i++) {
            Engine.Star s = l.get(i); if (i > 0) sb.append(", ");
            int st = sb.length(); sb.append(s.name + (s.attachedTo != null ? " (" + s.attachedTo + ")" : ""));
            sb.setSpan(new ForegroundColorSpan(s.attachedTo != null ? th().hoa(s.name) : color), st, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        dc.addView(ui.body(sb));
    }
    static String join(List<String> l, String sep) { StringBuilder b = new StringBuilder(); for (int i = 0; i < l.size(); i++) { if (i > 0) b.append(sep); b.append(l.get(i)); } return b.toString(); }
    Frame listItem(CharSequence title, CharSequence sub, CharSequence sub2, View.OnClickListener l) {
        Frame f = new Frame(a, a.bg, a.gcfg, 0, 10); f.setPadding(ui.dp(12), ui.dp(9), ui.dp(12), ui.dp(9));
        f.addView(ui.text(title, 14.5f, th().text, true));
        if (sub != null) f.addView(ui.text(sub, 12.5f, th().muted, false));
        if (sub2 != null) f.addView(ui.text(sub2, 12.5f, th().muted, false));
        if (l != null) { f.setClickable(true); f.setOnClickListener(l); }
        return f;
    }
    View.OnClickListener open(final String ch, final int i) { return new View.OnClickListener() { public void onClick(View v) { openArticle(ch, i); } }; }

    void saveProfile() {
        if (chart == null && build() == null) return;
        Engine.Input in = "chart".equals(a.current) ? readForm() : form;
        Map<String, Object> o = inputMap(in);
        o.put("saved", (double) System.currentTimeMillis()); o.put("summary", chart.yearCanChi + " · " + chart.cucName + " · " + t("menh") + " " + Engine.CHI[chart.menh]);
        List<Object> l = a.host.list("aa.saved"); l.add(0, o); while (l.size() > 200) l.remove(l.size() - 1);
        a.host.set("aa.saved", l); a.toast(t("saved_ok"));
    }
    byte[] chartPng() {
        Bitmap b = ChartViews.render(chart, opts()); ByteArrayOutputStream bo = new ByteArrayOutputStream(); b.compress(Bitmap.CompressFormat.PNG, 100, bo); b.recycle(); return bo.toByteArray();
    }
    String fileName() { return "LaSo-" + chart.name.replaceAll("[^\\p{L}\\p{N}]+", "_") + "-" + chart.sYear + ".png"; }
    byte[] exportPng() {
        if (chart == null && build() == null) return null;
        byte[] png = chartPng(); String where = a.host.saveFile(fileName(), png, "image/png");
        ImageView iv = new ImageView(a); iv.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(png, 0, png.length)); iv.setAdjustViewBounds(true);
        LinearLayout b = ui.col(); b.addView(iv, Ui.fill()); b.addView(ui.hint(where == null ? fileName() : where));
        a.modal(t("png_ok"), b);
        return png;
    }
    static String chartCode(Engine.Input o) { return "AA2|" + o.name + "|" + ("Nữ".equals(o.gender) ? "F" : "M") + "|" + ("lunar".equals(o.calendar) ? "L" : "S") + "|" + o.day + "|" + o.month + "|" + o.year + "|" + o.hourChi + "|" + (o.leap ? 1 : 0); }
    static Engine.Input parseCode(String code) {
        String[] p = String.valueOf(code).split("\\|", -1); if (p.length < 9 || !"AA2".equals(p[0])) return null;
        Engine.Input in = new Engine.Input(); in.name = p[1]; in.gender = "F".equals(p[2]) ? "Nữ" : "Nam"; in.calendar = "L".equals(p[3]) ? "lunar" : "solar";
        try { in.day = Integer.parseInt(p[4]); in.month = Integer.parseInt(p[5]); in.year = Integer.parseInt(p[6]); in.hourChi = Integer.parseInt(p[7]); } catch (Exception e) { return null; }
        in.leap = "1".equals(p[8]); return in;
    }
    QrCode showQr() {
        if (chart == null && build() == null) return null;
        String code = chartCode("chart".equals(a.current) ? readForm() : form);
        QrCode qr = QrCode.encodeText(code, QrCode.Ecc.MEDIUM);
        int sc = 8, border = 4, sz = (qr.size + border * 2) * sc; Bitmap bm = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888); bm.eraseColor(0xFFFFFFFF);
        for (int y = 0; y < qr.size; y++) for (int x = 0; x < qr.size; x++) if (qr.getModule(x, y)) for (int dy = 0; dy < sc; dy++) for (int dx = 0; dx < sc; dx++) bm.setPixel((x + border) * sc + dx, (y + border) * sc + dy, 0xFF000000);
        ImageView iv = new ImageView(a); iv.setImageBitmap(bm); iv.setAdjustViewBounds(true); iv.setTag("qrImage");
        LinearLayout b = ui.col(); b.setGravity(Gravity.CENTER_HORIZONTAL); b.addView(iv, Ui.lp(ui.dp(240), ui.dp(240))); b.addView(ui.hint(t("qr_desc"))); TextView c = ui.hint(code); c.setTextIsSelectable(true); b.addView(c);
        a.modal(t("qr_title"), b);
        return qr;
    }
    String summaryText() {
        Engine.Palace p = chart.palaces[chart.menh]; List<String> ms = new ArrayList<String>(); for (Engine.Star s : p.major) ms.add(s.name);
        return chart.name + " – " + chart.amDuong + ", " + chart.yearCanChi + ", " + chart.napAm + ", " + chart.cucName + ". " + t("menh") + " " + p.canChi + ": " + (ms.isEmpty() ? t("vcd") : join(ms, ", ")) + ".\n" + chartCode(form) + "\n(Applied Astrology " + Host.VERSION + ")";
    }
    void share() {
        if (chart == null && build() == null) return;
        try { File f = new File(a.host.shareDir(), "AppliedAstrology-chart.png"); FileOutputStream fo = new FileOutputStream(f); fo.write(chartPng()); fo.close(); a.host.log("share image " + f.length()); }
        catch (Exception e) { a.host.log("share " + e); }
        a.host.share(summaryText());
    }
    void print() {
        if (chart == null && build() == null) return;
        final Bitmap bmp = ChartViews.render(chart, opts());
        PrintManager pm = (PrintManager) a.getSystemService(Context.PRINT_SERVICE);
        try {
            pm.print("Applied Astrology – " + chart.name, new PrintDocumentAdapter() {
                PrintAttributes attrs;
                @Override public void onLayout(PrintAttributes o, PrintAttributes n, CancellationSignal cs, LayoutResultCallback cb, Bundle ex) {
                    attrs = n; cb.onLayoutFinished(new PrintDocumentInfo.Builder("LaSo.pdf").setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(1).build(), true);
                }
                @Override public void onWrite(PageRange[] pages, ParcelFileDescriptor dest, CancellationSignal cs, WriteResultCallback cb) {
                    PdfDocument doc = new PdfDocument(); PrintAttributes.MediaSize ms = attrs != null && attrs.getMediaSize() != null ? attrs.getMediaSize() : PrintAttributes.MediaSize.ISO_A4;
                    int w = ms.getWidthMils() * 72 / 1000, h = ms.getHeightMils() * 72 / 1000;
                    PdfDocument.Page pg = doc.startPage(new PdfDocument.PageInfo.Builder(w, h, 1).create());
                    Canvas c = pg.getCanvas(); float sc = Math.min((w - 36f) / bmp.getWidth(), (h - 36f) / bmp.getHeight());
                    c.drawBitmap(bmp, null, new RectF(18, 18, 18 + bmp.getWidth() * sc, 18 + bmp.getHeight() * sc), null); doc.finishPage(pg);
                    try { FileOutputStream fo = new FileOutputStream(dest.getFileDescriptor()); doc.writeTo(fo); fo.close(); cb.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES}); }
                    catch (Exception e) { cb.onWriteFailed(String.valueOf(e)); }
                    doc.close();
                }
            }, null);
        } catch (Exception e) { a.host.log("print " + e); a.toast(String.valueOf(e)); }
    }

    // =====================================================================================
    // ÂM LỊCH
    // =====================================================================================
    String[] wds() { String[] w = t("cal_wd").split(","); return w.length == 7 ? w : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}; }
    String wdFull(int wd) { String[] w = t("cal_wd_full").split(","); return w.length == 7 ? w[(wd + 6) % 7] : ""; }
    static String pad(int n) { return n < 10 ? "0" + n : String.valueOf(n); }
    View calendar() {
        LinearLayout p = page();
        Frame head = card(p);
        LinearLayout nav = ui.row();
        nav.addView(tag(ui.small("←", new View.OnClickListener() { public void onClick(View v) { calMove(-1); } }), "calPrev"));
        TextView title = ui.text(t("cal_month_title", "m", calM, "y", calY), 16, th().gold2, true); title.setGravity(Gravity.CENTER);
        nav.addView(title, Ui.weight(1));
        nav.addView(tag(ui.small("→", new View.OnClickListener() { public void onClick(View v) { calMove(1); } }), "calNext"));
        nav.addView(tag(ui.small(t("cal_today"), new View.OnClickListener() { public void onClick(View v) { Calendar n = Calendar.getInstance(); calY = n.get(Calendar.YEAR); calM = n.get(Calendar.MONTH) + 1; calD = n.get(Calendar.DAY_OF_MONTH); a.show("calendar"); } }), "calToday"));
        head.addView(nav, Ui.fill());
        LinearLayout pick = ui.row();
        List<String> ms = new ArrayList<String>(); for (int i = 1; i <= 12; i++) ms.add(t("f_month") + " " + i);
        pick.addView(ui.spinner(ms, calM - 1, new Ui.OnPick() { public void pick(int pos) { calM = pos + 1; calD = Math.min(calD, Lunar.daysInMonth(calM, calY)); a.show("calendar"); } }), Ui.weight(1));
        final EditText yy = ui.edit(String.valueOf(calY), "", true);
        yy.setImeOptions(EditorInfo.IME_ACTION_DONE);
        yy.setOnEditorActionListener(new TextView.OnEditorActionListener() { public boolean onEditorAction(TextView v, int id, KeyEvent e) {
            try { int y = Integer.parseInt(yy.getText().toString()); if (y >= 1800 && y <= 2199) { calY = y; calD = Math.min(calD, Lunar.daysInMonth(calM, calY)); a.show("calendar"); } } catch (Exception ignored) { }
            return true; } });
        pick.addView(yy, ui.margins(Ui.weight(1), 6, 0, 0, 0));
        head.addView(pick, ui.margins(Ui.fill(), 0, 6, 0, 0));
        Lunar.LDate l1 = Lunar.solar2lunar(1, calM, calY), lN = Lunar.solar2lunar(Lunar.daysInMonth(calM, calY), calM, calY);
        head.addView(ui.hint(t("lunar_date") + ": " + l1.month + (l1.leap ? "*" : "") + "/" + l1.year + (lN.month != l1.month ? " – " + lN.month + (lN.leap ? "*" : "") + "/" + lN.year : "") + " · " + Lunar.CAN[(l1.year + 6) % 10] + " " + Lunar.CHI[(l1.year + 8) % 12] + " · " + t("cal_tz")));
        boolean desk = a.isDesktop() || a.getResources().getConfiguration().screenWidthDp >= 720;
        LinearLayout area = desk ? ui.row() : ui.col(); if (desk) area.setGravity(Gravity.TOP);
        Frame gc = a.card(); gc.setPadding(ui.dp(6), ui.dp(6), ui.dp(6), ui.dp(6));
        MonthView mv = new MonthView(a); mv.setTag("monthView");
        mv.set(calY, calM, calD, wds(), th(), !"off".equals(a.gcfg.mode), ui.fs, new MonthView.OnDay() { public void pick(int d) { calD = d; a.show("calendar"); } });
        gc.addView(mv, Ui.fill());
        gc.addView(ui.hint("● " + t("cal_hd") + "   ▬ " + t("cal_events") + "   * " + t("cal_leap")));
        Frame det = a.card(); det.setTag("calDetail"); calDetail(det);
        if (desk) { area.addView(gc, Ui.weight(3)); LinearLayout.LayoutParams rp = Ui.weight(2); rp.leftMargin = ui.dp(8); area.addView(det, rp); }
        else { area.addView(gc, a.cardLp()); area.addView(det, a.cardLp()); }
        p.addView(area, Ui.fill());
        // đổi ngày
        Frame cv = card(p); cv.addView(ui.h2(t("cal_convert")));
        LinearLayout r = ui.row();
        final EditText cvD = ui.edit(String.valueOf(calD), t("f_day"), true), cvM = ui.edit(String.valueOf(calM), t("f_month"), true), cvY = ui.edit(String.valueOf(calY), t("f_year"), true);
        cvD.setTag("cvD"); cvM.setTag("cvM"); cvY.setTag("cvY");
        r.addView(ui.field(t("f_day"), cvD, 1)); r.addView(ui.field(t("f_month"), cvM, 1)); r.addView(ui.field(t("f_year"), cvY, 1.4f));
        cv.addView(r, Ui.fill());
        final CheckBox cvLeap = ui.check(t("f_leap"), false, null); cvLeap.setTag("cvLeap"); cv.addView(cvLeap);
        final LinearLayout out = ui.col(); out.setTag("cvOut");
        Ui.Flow b = ui.flow();
        b.addView(tag(ui.button(t("cal_s2l"), false, new View.OnClickListener() { public void onClick(View v) { convert(true, cvD, cvM, cvY, cvLeap, out); } }), "cvS2L"));
        b.addView(tag(ui.button(t("cal_l2s"), false, new View.OnClickListener() { public void onClick(View v) { convert(false, cvD, cvM, cvY, cvLeap, out); } }), "cvL2S"));
        cv.addView(b, Ui.fill()); cv.addView(out, Ui.fill());
        // lục thập hoa giáp
        Frame sx = card(p); sx.addView(ui.h2(t("cal_60"))); sx.addView(ui.hint(t("cal_60_d")));
        LinearLayout fr = ui.row(); final EditText y60 = ui.edit("", t("f_year"), true); y60.setTag("c60Y");
        final TextView o60 = ui.body(""); o60.setTag("c60Out");
        fr.addView(y60, Ui.weight(1));
        fr.addView(tag(ui.button(t("cal_find_year"), true, new View.OnClickListener() { public void onClick(View v) { findYear(y60, o60); } }), "c60Go"), ui.margins(Ui.wrap(), 6, 0, 0, 0));
        sx.addView(fr, ui.margins(Ui.fill(), 0, 6, 0, 4)); sx.addView(o60);
        List<Cal.Sixty> six = Cal.sixty(); Cal.YearInfo cur = Cal.yearInfo(MainActivity.year());
        LinearLayout tbl = ui.col(); tbl.setTag("sixty");
        for (final Cal.Sixty s : six) {
            LinearLayout row = ui.row(); row.setPadding(ui.dp(4), ui.dp(5), ui.dp(4), ui.dp(5));
            if (s.name.equals(cur.name)) row.setBackgroundColor(Theme.alpha(th().sel, 220));
            row.addView(ui.text(String.valueOf(s.idx + 1), 12.5f, th().muted, false), Ui.lp(ui.dp(30), -2));
            row.addView(ui.text(s.name, 13.5f, th().text, true), Ui.lp(ui.dp(92), -2));
            row.addView(ui.text(s.napAm, 13, th().element(s.element), false), Ui.lp(ui.dp(124), -2));
            StringBuilder ys = new StringBuilder(); for (int i = 0; i < s.years.size(); i++) { if (i > 0) ys.append(" · "); ys.append(s.years.get(i)); }
            row.addView(ui.text(ys.toString(), 12, th().muted, false), Ui.weight(1));
            row.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { showYear(s.years.get(s.years.size() > 3 ? 3 : 0), o60); } });
            tbl.addView(row, Ui.fill());
        }
        HorizontalScrollView hs = new HorizontalScrollView(a); LinearLayout wrapT = ui.col(); wrapT.setMinimumWidth(ui.dp(520)); wrapT.addView(tbl, Ui.fill());
        hs.addView(wrapT); hs.setFillViewport(true); sx.addView(hs, Ui.fill());
        return p;
    }
    void calMove(int d) { calM += d; if (calM < 1) { calM = 12; calY--; } if (calM > 12) { calM = 1; calY++; } calD = Math.min(calD, Lunar.daysInMonth(calM, calY)); a.show("calendar"); }
    String hours(List<Cal.Hour> l) { StringBuilder b = new StringBuilder(); for (Cal.Hour h : l) { if (b.length() > 0) b.append("   "); b.append(h.name).append(" (").append(h.range).append(')'); } return b.toString(); }
    Cal.DayInfo calDetail(LinearLayout box) {
        Cal.DayInfo inf = Cal.dayInfo(calD, calM, calY); Lunar.LDate lu = inf.lunar;
        LinearLayout big = ui.row();
        big.addView(ui.text(String.valueOf(calD), 38, th().gold2, true)); LinearLayout l1 = ui.col(); l1.setPadding(ui.dp(10), 0, 0, 0);
        l1.addView(ui.body(wdFull(inf.weekday))); l1.addView(ui.body(pad(calD) + "/" + pad(calM) + "/" + calY)); big.addView(l1, Ui.weight(1));
        LinearLayout l2 = ui.col(); l2.setGravity(Gravity.END); l2.addView(ui.text(String.valueOf(lu.day), 38, th().gold2, true)); big.addView(l2);
        LinearLayout l3 = ui.col(); l3.setPadding(ui.dp(8), 0, 0, 0); l3.addView(ui.body(t("lunar_date"))); l3.addView(ui.body(lu.day + "/" + lu.month + (lu.leap ? " " + t("cal_leap") : "") + "/" + lu.year)); big.addView(l3);
        box.addView(big, Ui.fill());
        box.addView(kv(t("day_cc"), inf.dayCC + "  (" + inf.napAmDay + ")"));
        box.addView(kv(t("month_cc"), inf.monthCC + (inf.monthLeap ? " (" + t("cal_leap") + ")" : "")));
        box.addView(kv(t("year_cc"), inf.yearCC + "  (" + inf.napAmYear + ")"));
        box.addView(kv(t("cal_tiet"), inf.tiet + (inf.tietStart ? " · " + t("cal_tiet_start") : "")));
        TextView dt = kv(t("cal_daytype"), (inf.hoangDao ? t("cal_hd") : t("cal_hkd")) + " (" + inf.than + ") · " + t("cal_truc") + " " + inf.truc); box.addView(dt);
        box.addView(kv(t("cal_hour0"), inf.hourZero));
        box.addView(ui.grp(t("cal_good_hours"))); box.addView(ui.text(hours(inf.goodHours), 13.5f, th().good, false));
        box.addView(ui.grp(t("cal_bad_hours"))); box.addView(ui.text(hours(inf.badHours), 13, th().muted, false));
        if (!inf.events.isEmpty()) { box.addView(ui.grp(t("cal_events"))); for (Cal.Event e : inf.events) box.addView(ui.body((e.lunar ? "☾ " : "☀ ") + e.name)); }
        box.addView(ui.hint("JD " + inf.jd));
        box.addView(tag(ui.button(t("cal_build_chart"), true, new View.OnClickListener() { public void onClick(View v) {
            Engine.Input in = new Engine.Input(); in.name = form.name; in.gender = form.gender; in.calendar = "solar"; in.day = calD; in.month = calM; in.year = calY; in.hourChi = form.hourChi; in.viewYear = form.viewYear;
            form = withMode(in); a.show("chart"); build(); } }), "calBuild"), ui.margins(Ui.wrap(), 0, 8, 0, 0));
        return inf;
    }
    TextView kv(String k, String v) {
        SpannableStringBuilder sb = new SpannableStringBuilder(k + ": "); sb.setSpan(new ForegroundColorSpan(th().muted), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(v); TextView t = ui.body(sb); t.setPadding(0, ui.dp(2), 0, ui.dp(2)); return t;
    }
    /** Đổi ngày; trả về {d,m,y, lunarDay, lunarMonth, lunarYear, leap} hoặc null */
    int[] convert(boolean s2l, EditText cd, EditText cm, EditText cy, CheckBox lp, LinearLayout out) {
        out.removeAllViews(); int d, m, y;
        try { d = Integer.parseInt(cd.getText().toString().trim()); m = Integer.parseInt(cm.getText().toString().trim()); y = Integer.parseInt(cy.getText().toString().trim()); }
        catch (Exception e) { out.addView(ui.text(t("invalid_date"), 14, th().bad, true)); return null; }
        if (!(y >= 1800 && y <= 2199 && m >= 1 && m <= 12 && d >= 1 && d <= 31)) { out.addView(ui.text(t("invalid_date"), 14, th().bad, true)); return null; }
        int[] so; Lunar.LDate lu; boolean leap = lp.isChecked();
        if (s2l) {
            if (!Lunar.validSolar(d, m, y)) { out.addView(ui.text(t("invalid_date"), 14, th().bad, true)); return null; }
            so = new int[]{d, m, y}; lu = Lunar.solar2lunar(d, m, y);
        } else {
            so = Lunar.lunar2solar(d, m, y, leap);
            if (so == null || (d > 29 && Lunar.lunarMonthLength(m, y, leap) < d)) { out.addView(ui.text(t("invalid_lunar"), 14, th().bad, true)); return null; }
            lu = Lunar.solar2lunar(so[0], so[1], so[2]);
        }
        final int[] fs = so; Cal.DayInfo inf = Cal.dayInfo(so[0], so[1], so[2]);
        out.addView(kv(t("solar_date"), wdFull(inf.weekday) + ", " + pad(so[0]) + "/" + pad(so[1]) + "/" + so[2]));
        out.addView(kv(t("lunar_date"), lu.day + "/" + lu.month + (lu.leap ? " " + t("cal_leap") : "") + "/" + lu.year + " – " + inf.dayCC + ", " + t("month_cc").toLowerCase() + " " + inf.monthCC + ", " + t("year_cc").toLowerCase() + " " + inf.yearCC));
        out.addView(tag(ui.small(t("cal_open_day"), new View.OnClickListener() { public void onClick(View v) { calY = fs[2]; calM = fs[1]; calD = fs[0]; a.show("calendar"); a.scrollTop(); } }), "cvGoto"));
        return new int[]{so[0], so[1], so[2], lu.day, lu.month, lu.year, lu.leap ? 1 : 0};
    }
    Cal.YearInfo showYear(int y, TextView out) {
        Cal.YearInfo yi = Cal.yearInfo(y);
        SpannableStringBuilder sb = new SpannableStringBuilder(y + ": " + yi.name + " · " + t("cal_napam") + " ");
        int st = sb.length(); sb.append(yi.napAm); sb.setSpan(new ForegroundColorSpan(th().element(yi.element)), st, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(" (" + yi.element + ") · " + t("cal_age") + " " + yi.age); out.setText(sb); return yi;
    }
    Cal.YearInfo findYear(EditText e, TextView out) { try { int y = Integer.parseInt(e.getText().toString().trim()); if (y >= 1800 && y <= 2199) return showYear(y, out); } catch (Exception ignored) { } return null; }

    // =====================================================================================
    // THƯ VIỆN
    // =====================================================================================
    boolean libBack() {
        if (libMode.startsWith("read:")) { String[] k = libMode.split(":"); libMode = lastResults != null && libMode.equals("read:" + k[1] + ":" + k[2]) && "search".equals(prevMode) ? "search:" + lastQuery : "chapter:" + k[1]; a.show("library"); return true; }
        if (!"home".equals(libMode)) { libMode = "home"; a.show("library"); return true; }
        return false;
    }
    String prevMode = "home";
    View library() {
        LinearLayout p = page();
        Frame top = card(p);
        top.addView(ui.h2(t("lib_title")));
        top.addView(ui.hint(t("lib_desc", "n", a.lib.totalArticles(), "c", a.lib.chapters.size())));
        LinearLayout sr = ui.row();
        final EditText q = ui.edit("search".equals(libMode.split(":")[0]) ? lastQuery : "", t("lib_search_ph"), false); q.setTag("libQ");
        q.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        q.setOnEditorActionListener(new TextView.OnEditorActionListener() { public boolean onEditorAction(TextView v, int id, KeyEvent e) { search(q.getText().toString()); return true; } });
        sr.addView(q, Ui.weight(1));
        sr.addView(tag(ui.button(t("btn_search"), true, new View.OnClickListener() { public void onClick(View v) { search(q.getText().toString()); } }), "libGo"), ui.margins(Ui.wrap(), 6, 0, 0, 0));
        top.addView(sr, ui.margins(Ui.fill(), 0, 6, 0, 0));
        List<Object> rec = a.host.list("aa.recent");
        if (!rec.isEmpty() && "home".equals(libMode)) { Map<String, Object> r0 = Json.obj(rec.get(0)); top.addView(tag(ui.small(t("lib_continue") + ": " + Json.str(r0.get("t"), ""), open(Json.str(r0.get("ch"), ""), Json.num(r0.get("i"), 0))), "libContinue"), ui.margins(Ui.wrap(), 0, 6, 0, 0)); }
        if (libMode.startsWith("chapter:")) chapterView(p, libMode.substring(8));
        else if (libMode.startsWith("read:")) { String[] k = libMode.split(":"); reader(p, k[1], Integer.parseInt(k[2])); }
        else if (libMode.startsWith("search:")) results(p);
        else {
            LinearLayout grid = ui.col(); grid.setTag("chapters");
            for (final Library.Chapter c : a.lib.chapters) grid.addView(tag(listItem(c.n + ". " + a.i18n.chapterName(a.lib, c.id), c.count + " " + t("articles"), null, new View.OnClickListener() { public void onClick(View v) { libMode = "chapter:" + c.id; a.show("library"); a.scrollTop(); } }), "ch:" + c.id), ui.margins(Ui.fill(), 0, 0, 0, 6));
            p.addView(grid, Ui.fill());
        }
        return p;
    }
    void chapterView(LinearLayout p, final String id) {
        Library.Chapter c = a.lib.chapter(id); Frame f = card(p); f.setTag("chapterList");
        f.addView(ui.small("← " + t("btn_back"), new View.OnClickListener() { public void onClick(View v) { libMode = "home"; a.show("library"); } }));
        f.addView(ui.h2(c.n + ". " + a.i18n.chapterName(a.lib, id)));
        for (int i = 0; i < c.titles.size(); i++) {
            boolean bm = isBookmarked(id, i);
            f.addView(tag(listItem((i + 1) + ". " + c.titles.get(i), String.format("%,d", c.lens.get(i)) + " " + t("chars") + (bm ? " · ★" : ""), null, open(id, i)), "art:" + i), ui.margins(Ui.fill(), 0, 4, 0, 0));
        }
    }
    void openArticle(String ch, int i) { prevMode = libMode.startsWith("search:") ? "search" : "chapter"; libMode = "read:" + ch + ":" + i; a.show("library"); a.scrollTop(); }
    TextView readerBody;
    Library.Article reader(LinearLayout p, final String ch, final int i) {
        final Library.Article art = a.lib.get(ch, i); Library.Chapter c = a.lib.chapter(ch);
        Frame f = card(p); f.setTag("reader");
        if (art == null) { f.addView(ui.body(t("no_related"))); return null; }
        Ui.Flow tools = ui.flow();
        tools.addView(ui.small("← " + a.i18n.chapterName(a.lib, ch), new View.OnClickListener() { public void onClick(View v) { libMode = "chapter:" + ch; a.show("library"); } }));
        if (i > 0) tools.addView(tag(ui.small(t("btn_prev"), open(ch, i - 1)), "rPrev"));
        if (i < c.count - 1) tools.addView(tag(ui.small(t("btn_next"), open(ch, i + 1)), "rNext"));
        final TextView bm = ui.small((isBookmarked(ch, i) ? "★ " : "☆ ") + t("btn_bookmark"), null); bm.setTag("rBm");
        bm.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { boolean on = toggleBookmark(ch, i); bm.setText((on ? "★ " : "☆ ") + t("btn_bookmark")); } });
        tools.addView(bm);
        tools.addView(tag(ui.small("A−", new View.OnClickListener() { public void onClick(View v) { font(-0.08); } }), "rFontM"));
        tools.addView(tag(ui.small("A+", new View.OnClickListener() { public void onClick(View v) { font(0.08); } }), "rFontP"));
        if (!"vi".equals(a.i18n.current())) tools.addView(tag(ui.small(t("btn_translate"), new View.OnClickListener() { public void onClick(View v) {
            try { a.host.openUrl("https://translate.google.com/?sl=vi&tl=" + java.net.URLEncoder.encode(a.i18n.current().replace("-BR", "").replace("-419", ""), "UTF-8") + "&op=translate&text=" + java.net.URLEncoder.encode(art.body.substring(0, Math.min(4500, art.body.length())), "UTF-8")); } catch (Exception ignored) { } } }), "rTr"));
        f.addView(tools, Ui.fill());
        f.addView(ui.text(art.title, 19, th().gold2, true), ui.margins(Ui.fill(), 0, 8, 0, 2));
        f.addView(ui.hint(a.i18n.chapterName(a.lib, ch) + " · " + (i + 1) + "/" + c.count + " · " + t("reader_original_note")));
        readerBody = ui.text(art.body, 15.3f, th().text, false); readerBody.setTag("rBody"); readerBody.setTextIsSelectable(true);
        double fsz = Json.dbl(a.host.get("aa.readerFont"), 1.02); readerBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (15 * fsz) * ui.fs); readerBody.setLineSpacing(0, 1.42f);
        f.addView(readerBody, ui.margins(Ui.fill(), 0, 10, 0, 6));
        if (i < c.count - 1) f.addView(ui.button(t("btn_next") + " →", true, open(ch, i + 1)));
        markRead(ch, i);
        return art;
    }
    void font(double d) {
        double f = Math.max(0.8, Math.min(1.8, Json.dbl(a.host.get("aa.readerFont"), 1.02) + d)); a.host.set("aa.readerFont", f);
        if (readerBody != null) readerBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (15 * f) * ui.fs);
    }
    List<Library.Hit> search(String q) {
        lastQuery = q == null ? "" : q.trim(); a.host.log("search " + lastQuery);
        lastResults = a.lib.search(lastQuery); libMode = "search:" + lastQuery;
        if ("library".equals(a.current)) a.show("library");
        return lastResults;
    }
    void results(LinearLayout p) {
        Frame f = card(p); f.setTag("results");
        LinearLayout hr = ui.row();
        hr.addView(ui.small("← " + t("btn_back"), new View.OnClickListener() { public void onClick(View v) { libMode = "home"; a.show("library"); } }));
        hr.addView(ui.text("  " + t("lib_results", "n", lastResults == null ? 0 : lastResults.size()), 15, th().text, true));
        f.addView(hr);
        if (lastResults == null) return;
        String fq = Fold.fold(lastQuery).trim();
        for (int k = 0; k < Math.min(120, lastResults.size()); k++) {
            Library.Hit h = lastResults.get(k);
            f.addView(listItem(h.title, a.i18n.chapterName(a.lib, h.ch), highlight(h.snippet == null ? "" : h.snippet, fq), open(h.ch, h.i)), ui.margins(Ui.fill(), 0, 5, 0, 0));
        }
    }
    CharSequence highlight(String sn, String fq) {
        SpannableStringBuilder sb = new SpannableStringBuilder(sn); if (fq.isEmpty()) return sb;
        int pp = Fold.fold(sn).indexOf(fq);
        if (pp >= 0 && pp + fq.length() <= sn.length()) { sb.setSpan(new BackgroundColorSpan(th().gold), pp, pp + fq.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); sb.setSpan(new ForegroundColorSpan(0xFF000000), pp, pp + fq.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); }
        return sb;
    }
    // đánh dấu & lịch sử (cùng định dạng aa.bookmarks / aa.recent của 0.0.3)
    boolean isBookmarked(String ch, int i) { for (Object o : a.host.list("aa.bookmarks")) if ((ch + ":" + i).equals(Json.str(Json.obj(o).get("k"), ""))) return true; return false; }
    boolean toggleBookmark(String ch, int i) {
        List<Object> bm = a.host.list("aa.bookmarks"); String key = ch + ":" + i; int found = -1;
        for (int k = 0; k < bm.size(); k++) if (key.equals(Json.str(Json.obj(bm.get(k)).get("k"), ""))) found = k;
        if (found >= 0) bm.remove(found); else bm.add(0, Json.map("k", key, "ch", ch, "i", (long) i, "t", a.lib.title(ch, i), "d", (double) System.currentTimeMillis()));
        a.host.set("aa.bookmarks", bm); return found < 0;
    }
    void markRead(String ch, int i) {
        List<Object> r = a.host.list("aa.recent"); String key = ch + ":" + i;
        for (int k = r.size() - 1; k >= 0; k--) if (key.equals(Json.str(Json.obj(r.get(k)).get("k"), ""))) r.remove(k);
        r.add(0, Json.map("k", key, "ch", ch, "i", (long) i, "t", a.lib.title(ch, i), "s", 0L, "d", (double) System.currentTimeMillis()));
        while (r.size() > 30) r.remove(r.size() - 1);
        a.host.set("aa.recent", r);
    }

    // =====================================================================================
    // LUẬN GIẢI
    // =====================================================================================
    View assistant() {
        LinearLayout p = page();
        Frame f = card(p);
        f.addView(ui.h2(t("as_title"))); f.addView(ui.hint(t("as_note")));
        Ui.Flow chips = ui.flow(); chips.setTag("asChips");
        String[][] cs = {{"__overview", "as_overview"}, {"mệnh tính cách", "as_q_menh"}, {"công danh sự nghiệp", "as_q_career"}, {"tài lộc tiền", "as_q_money"}, {"hôn nhân phu thê", "as_q_love"}, {"sức khỏe bệnh", "as_q_health"}, {"hạn năm nay", "as_q_year"}};
        for (final String[] c : cs) chips.addView(tag(ui.chip(t(c[1]), new View.OnClickListener() { public void onClick(View v) { ask("__overview".equals(c[0]) ? "__overview" : c[0] + " " + t(c[1])); } }), "chip:" + c[1]));
        f.addView(chips, ui.margins(Ui.fill(), 0, 6, 0, 6));
        LinearLayout box = ui.col(); box.setTag("chat");
        for (Object[] m : chat) box.addView(bubble((String) m[0], (String) m[1], m[2]), ui.margins(Ui.fill(), "user".equals(m[0]) ? 40 : 0, 4, "user".equals(m[0]) ? 0 : 24, 4));
        f.addView(box, Ui.fill());
        LinearLayout ir = ui.row(); final EditText q = ui.edit("", t("as_ph"), false); q.setTag("asQ");
        q.setImeOptions(EditorInfo.IME_ACTION_SEND);
        q.setOnEditorActionListener(new TextView.OnEditorActionListener() { public boolean onEditorAction(TextView v, int id, KeyEvent e) { String s = q.getText().toString().trim(); q.setText(""); if (!s.isEmpty()) ask(s); return true; } });
        ir.addView(q, Ui.weight(1));
        ir.addView(tag(ui.button(t("btn_send"), true, new View.OnClickListener() { public void onClick(View v) { String s = q.getText().toString().trim(); q.setText(""); if (!s.isEmpty()) ask(s); } }), "asSend"), ui.margins(Ui.wrap(), 6, 0, 0, 0));
        f.addView(ir, ui.margins(Ui.fill(), 0, 8, 0, 0));
        return p;
    }
    @SuppressWarnings("unchecked")
    View bubble(String role, String text, Object refs) {
        Frame b = new Frame(a, a.bg, a.gcfg, 0, 12); b.setPadding(ui.dp(12), ui.dp(9), ui.dp(12), ui.dp(9));
        if ("user".equals(role)) b.solidColor = Theme.alpha(th().sel, 235);
        TextView tv = ui.body(text); tv.setTextIsSelectable(true); b.addView(tv);
        if (refs != null) for (final Library.Hit h : (List<Library.Hit>) refs) { TextView r = ui.text("→ " + h.title, 13.5f, th().accent, false); r.setPadding(0, ui.dp(5), 0, 0); r.setOnClickListener(open(h.ch, h.i)); b.addView(r); }
        return b;
    }
    String ask(String q) {
        if (chart == null) build();
        String out;
        if ("__overview".equals(q)) { out = chart == null ? t("as_need_chart") : a.asst.overview(chart); chat.add(new Object[]{"ai", out, null}); }
        else { chat.add(new Object[]{"user", q, null}); Assistant.Answer r = a.asst.answer(chart, q); out = r.text; chat.add(new Object[]{"ai", out, r.refs}); }
        while (chat.size() > 40) chat.remove(0);
        if ("assistant".equals(a.current)) { a.show("assistant"); final ScrollView0 s = new ScrollView0(); s.bottom(); }
        return out;
    }
    final class ScrollView0 { void bottom() { final android.widget.ScrollView s = a.scroller(); if (s != null) s.post(new Runnable() { public void run() { s.fullScroll(View.FOCUS_DOWN); } }); } }

    // =====================================================================================
    // HỒ SƠ
    // =====================================================================================
    View saved() {
        LinearLayout p = page();
        Frame f = card(p); f.setTag("savedList"); f.addView(ui.h2(t("saved_charts")));
        final List<Object> list = a.host.list("aa.saved");
        if (list.isEmpty()) f.addView(ui.hint(t("saved_empty")));
        for (int i = 0; i < list.size(); i++) {
            final int idx = i; final Map<String, Object> o = Json.obj(list.get(i));
            Frame it = listItem(Json.str(o.get("name"), "") + " – " + Json.num(o.get("day"), 0) + "/" + Json.num(o.get("month"), 0) + "/" + Json.num(o.get("year"), 0) + " (" + ("lunar".equals(o.get("calendar")) ? t("lunar") : t("solar")) + ")", Json.str(o.get("summary"), ""), null, null);
            Ui.Flow b = ui.flow();
            b.addView(tag(ui.small(t("btn_open"), new View.OnClickListener() { public void onClick(View v) { Engine.Input in = inputFrom(o); in.viewYear = MainActivity.year(); form = withMode(in); a.show("chart"); build(); } }), "load:" + i));
            b.addView(tag(ui.small(t("btn_delete"), new View.OnClickListener() { public void onClick(View v) { List<Object> l = a.host.list("aa.saved"); if (idx < l.size()) l.remove(idx); a.host.set("aa.saved", l); a.show("saved"); a.toast(t("deleted")); } }), "del:" + i));
            it.addView(b, ui.margins(Ui.fill(), 0, 4, 0, 0));
            f.addView(it, ui.margins(Ui.fill(), 0, 4, 0, 0));
        }
        Frame bmc = card(p); bmc.setTag("bmList"); bmc.addView(ui.h2(t("lib_bookmarks"))); refList(bmc, a.host.list("aa.bookmarks"));
        Frame rc = card(p); rc.setTag("recentList"); rc.addView(ui.h2(t("lib_recent"))); refList(rc, a.host.list("aa.recent"));
        return p;
    }
    void refList(LinearLayout f, List<Object> arr) {
        if (arr.isEmpty()) f.addView(ui.hint("—"));
        for (Object x : arr) { Map<String, Object> o = Json.obj(x); f.addView(listItem(Json.str(o.get("t"), ""), a.i18n.chapterName(a.lib, Json.str(o.get("ch"), "")), null, open(Json.str(o.get("ch"), ""), Json.num(o.get("i"), 0))), ui.margins(Ui.fill(), 0, 4, 0, 0)); }
    }

    // =====================================================================================
    // CÀI ĐẶT (mọi tùy chọn chỉ nằm ở đây)
    // =====================================================================================
    Frame setting(LinearLayout p, String title, String hint) { Frame f = card(p); f.addView(ui.h3(title)); if (hint != null) f.addView(ui.hint(hint)); return f; }
    void setS(String k, Object v) { a.S.put(k, v); a.save(); a.host.log("setting " + k + "=" + v); }
    int idxOf(String[] arr, String v) { for (int i = 0; i < arr.length; i++) if (arr[i].equals(v)) return i; return 0; }
    View settings() {
        LinearLayout p = page();
        // ngôn ngữ
        Frame f = setting(p, t("s_language"), null);
        final List<String> names = new ArrayList<String>(); final List<String> codes = new ArrayList<String>(); int sel = 0;
        for (int i = 0; i < a.i18n.langs.size(); i++) { names.add(a.i18n.langs.get(i).name + " (" + a.i18n.langs.get(i).code + ")"); codes.add(a.i18n.langs.get(i).code); if (codes.get(i).equals(a.s("lang"))) sel = i; }
        f.addView(tag(ui.spinner(names, sel, new Ui.OnPick() { public void pick(int pos) { setLang(codes.get(pos)); } }), "sLang"), Ui.fill());
        // giao diện
        final String[] themes = {"celestial-dark", "royal-gold", "ivory-light", "imperial-red"};
        f = setting(p, t("s_theme"), null);
        f.addView(tag(ui.spinner(Arrays.asList(t("th_celestial"), t("th_gold"), t("th_ivory"), t("th_red")), idxOf(themes, a.s("theme")), new Ui.OnPick() { public void pick(int pos) { setS("theme", themes[pos]); a.applySettings(); } }), "sTheme"), Ui.fill());
        // Liquid Glass (v0.0.4)
        glassSettings(p);
        final String[] fonts = {"compact", "normal", "large", "xlarge"};
        f = setting(p, t("s_font"), null);
        f.addView(tag(ui.spinner(Arrays.asList(t("fs_compact"), t("fs_normal"), t("fs_large"), t("fs_xlarge")), idxOf(fonts, a.s("font")), new Ui.OnPick() { public void pick(int pos) { setS("font", fonts[pos]); a.applySettings(); } }), "sFont"), Ui.fill());
        final String[] lays = {"auto", "board", "list"};
        f = setting(p, t("s_layout"), null);
        f.addView(tag(ui.spinner(Arrays.asList(t("lay_auto"), t("lay_board"), t("lay_list")), idxOf(lays, a.s("layout")), new Ui.OnPick() { public void pick(int pos) { setS("layout", lays[pos]); } }), "sLayout"), Ui.fill());
        f = setting(p, t("s_desktop"), t("s_desktop_d"));
        f.addView(tag(ui.toggle(t("s_desktop"), a.isDesktop(), new Ui.OnBool() { public void set(boolean v) { setS("desktop", v); a.buildShell(); a.show("settings"); } }), "sDesktop"));
        f = setting(p, t("s_rings"), null);
        f.addView(tag(ui.check(t("s_trangsinh"), a.b("ts"), new Ui.OnBool() { public void set(boolean v) { setS("ts", v); } }), "sTS"));
        f.addView(tag(ui.check(t("s_bacsi"), a.b("bs"), new Ui.OnBool() { public void set(boolean v) { setS("bs", v); } }), "sBS"));
        f.addView(tag(ui.check(t("s_thaitue"), a.b("tt"), new Ui.OnBool() { public void set(boolean v) { setS("tt", v); } }), "sTT"));
        f.addView(tag(ui.check(t("s_luu"), a.b("luu"), new Ui.OnBool() { public void set(boolean v) { setS("luu", v); } }), "sLuu"));
        f = setting(p, t("s_paper"), t("s_paper_d"));
        List<String> pn = new ArrayList<String>(); for (String id : Theme.PAPERS) pn.add(t("paper_" + id));
        f.addView(tag(ui.spinner(pn, idxOf(Theme.PAPERS, a.s("paper")), new Ui.OnPick() { public void pick(int pos) { setS("paper", Theme.PAPERS[pos]); } }), "sPaper"), Ui.fill());
        final String[] leaps = {"same", "next"};
        f = setting(p, t("s_leap"), null);
        f.addView(tag(ui.spinner(Arrays.asList(t("s_leap_same"), t("s_leap_next")), idxOf(leaps, a.s("leap")), new Ui.OnPick() { public void pick(int pos) { setS("leap", leaps[pos]); if (chart != null) { form.leapMode = leaps[pos]; try { chart = Engine.build(form); } catch (Exception ignored) { } } } }), "sLeap"), Ui.fill());
        f = setting(p, t("s_network"), t("s_network_d"));
        final TextView netOut = ui.body(""); netOut.setTag("netOut");
        f.addView(tag(ui.button(t("s_check"), false, new View.OnClickListener() { public void onClick(View v) { checkNetwork(netOut); } }), "sNet"));
        f.addView(netOut);
        f = setting(p, t("s_backup"), null);
        Ui.Flow bb = ui.flow();
        bb.addView(tag(ui.button(t("s_export"), false, new View.OnClickListener() { public void onClick(View v) { exportBackup(); } }), "sExport"));
        bb.addView(tag(ui.button(t("s_import"), false, new View.OnClickListener() { public void onClick(View v) { a.pickFile(); } }), "sImport"));
        f.addView(bb, Ui.fill());
        final EditText imp = ui.edit("", t("s_import_text"), false); imp.setSingleLine(false); imp.setMinLines(3); imp.setTag("sImportText");
        f.addView(imp, ui.margins(Ui.fill(), 0, 6, 0, 0));
        Ui.Flow bb2 = ui.flow();
        bb2.addView(tag(ui.small(t("s_import_text"), new View.OnClickListener() { public void onClick(View v) { importBackup(imp.getText().toString()); } }), "sImportGo"));
        bb2.addView(tag(ui.small(t("btn_reset"), new View.OnClickListener() { public void onClick(View v) { resetDefaults(); } }), "sReset"));
        bb2.addView(tag(ui.small(t("s_clear"), new View.OnClickListener() { public void onClick(View v) { clearData(); } }), "sClear"));
        f.addView(bb2, ui.margins(Ui.fill(), 0, 6, 0, 0));
        f = setting(p, t("s_about"), t("s_about_d"));
        f.addView(ui.hint(t("s_version") + ": " + Host.VERSION + " · " + t("s_platform") + ": Android native · " + a.host.os() + " · " + a.host.abi() + "\n" + a.lib.totalArticles() + " " + t("articles") + " · " + a.i18n.langs.size() + " " + t("s_language").toLowerCase() + "\nQR: Project Nayuki (MIT) · Âm lịch: thuật toán Hồ Ngọc Đức"));
        f.addView(tag(ui.button(t("ab_open"), true, new View.OnClickListener() { public void onClick(View v) { a.show("about"); } }), "sAboutOpen"), ui.margins(Ui.wrap(), 0, 6, 0, 0));
        return p;
    }
    void glassSettings(LinearLayout p) {
        Frame f = setting(p, t("s_glass"), t("s_glass_d")); f.setTag("glassSettings");
        // v0.0.5: công tắc bật/tắt Liquid Glass (Creat & Update mục 2)
        f.addView(tag(ui.toggle(t("lg_enable"), a.b("lgOn"), new Ui.OnBool() { public void set(boolean v) { setS("lgOn", v); a.applySettings(); a.toast(t(v ? "lg_enabled" : "lg_disabled")); } }), "sLgOn"));
        f.addView(ui.hint(t("lg_enable_d")));
        if (!a.b("lgOn")) { f.addView(tag(ui.small(t("lg_reset"), new View.OnClickListener() { public void onClick(View v) { resetGlass(); } }), "sLgReset"), ui.margins(Ui.wrap(), 0, 6, 0, 0)); return; }
        final String[] modes = {"frost", "liquid"};
        f.addView(ui.text(t("lg_mode"), 12.5f, th().muted, false), ui.margins(Ui.fill(), 0, 6, 0, 0));
        f.addView(tag(ui.spinner(Arrays.asList(t("lg_frost"), t("lg_liquid")), Math.max(0, idxOf(modes, a.s("lgMode"))), new Ui.OnPick() { public void pick(int pos) { setS("lgMode", modes[pos]); a.applySettings(); } }), "sLgMode"), Ui.fill());
        final String[] bgs = {"nebula", "aurora", "plain"};
        f.addView(ui.text(t("lg_bg"), 12.5f, th().muted, false), ui.margins(Ui.fill(), 0, 6, 0, 0));
        f.addView(tag(ui.spinner(Arrays.asList(t("lg_bg_nebula"), t("lg_bg_aurora"), t("lg_bg_plain")), idxOf(bgs, a.s("lgBg")), new Ui.OnPick() { public void pick(int pos) { setS("lgBg", bgs[pos]); a.applySettings(); } }), "sLgBg"), Ui.fill());
        f.addView(tag(ui.slider(t("lg_blur"), 0, 40, a.n("lgBlur"), "", new Ui.OnInt() { public void set(int v) { setS("lgBlur", (long) v); a.applySettings(); } }), "sLgBlur"), ui.margins(Ui.fill(), 0, 6, 0, 0));
        f.addView(tag(ui.slider(t("lg_refr"), 0, 100, a.n("lgRefr"), "%", new Ui.OnInt() { public void set(int v) { setS("lgRefr", (long) v); a.applySettings(); } }), "sLgRefr"), Ui.fill());
        f.addView(tag(ui.slider(t("lg_bezel"), 4, 40, a.n("lgBezel"), "", new Ui.OnInt() { public void set(int v) { setS("lgBezel", (long) v); a.applySettings(); } }), "sLgBezel"), Ui.fill());
        f.addView(tag(ui.slider(t("lg_tint"), 0, 90, a.n("lgTint"), "%", new Ui.OnInt() { public void set(int v) { setS("lgTint", (long) v); a.applySettings(); } }), "sLgTint"), Ui.fill());
        f.addView(tag(ui.slider(t("lg_spec"), 0, 100, a.n("lgSpec"), "%", new Ui.OnInt() { public void set(int v) { setS("lgSpec", (long) v); a.applySettings(); } }), "sLgSpec"), Ui.fill());
        f.addView(tag(ui.slider(t("lg_sat"), 50, 200, a.n("lgSat"), "%", new Ui.OnInt() { public void set(int v) { setS("lgSat", (long) v); a.applySettings(); } }), "sLgSat"), Ui.fill());
        f.addView(tag(ui.toggle(t("lg_cards"), a.b("lgCards"), new Ui.OnBool() { public void set(boolean v) { setS("lgCards", v); a.applySettings(); } }), "sLgCards"));
        f.addView(tag(ui.toggle(t("lg_chart"), a.b("lgChart"), new Ui.OnBool() { public void set(boolean v) { setS("lgChart", v); a.applySettings(); } }), "sLgChart"));
        f.addView(tag(ui.small(t("lg_reset"), new View.OnClickListener() { public void onClick(View v) { resetGlass(); } }), "sLgReset"), ui.margins(Ui.wrap(), 0, 6, 0, 0));
    }
    void resetGlass() { Map<String, Object> d = MainActivity.defaults(); for (String k : d.keySet()) if (k.startsWith("lg")) a.S.put(k, d.get(k)); a.save(); a.applySettings(); a.toast(t("lg_reset")); }
    void setLang(String code) { setS("lang", code); a.i18n.setLang(code); a.applySettings(); }
    void checkNetwork(final TextView out) {
        out.setText(t("net_checking"));
        new Thread(new Runnable() { public void run() {
            final Host.Resp r = a.host.httpGet("https://www.cloudflare.com/cdn-cgi/trace", 8000);
            a.runOnUiThread(new Runnable() { public void run() {
                if (!r.ok) { out.setText(t("net_fail")); out.setTextColor(th().bad); a.host.log("net fail " + r.status); return; }
                Map<String, String> kv = new java.util.HashMap<String, String>();
                for (String l : r.body.split("\n")) { int i = l.indexOf('='); if (i > 0) kv.put(l.substring(0, i), l.substring(i + 1)); }
                boolean warp = kv.get("warp") != null && !"off".equals(kv.get("warp"));
                out.setText(t("net_ok") + " · " + t("net_latency") + ": " + r.ms + " ms\n" + t("net_ip") + ": " + kv.get("ip") + " · " + t("net_loc") + ": " + kv.get("loc") + (kv.get("colo") != null ? " · PoP " + kv.get("colo") : "") + " · " + (warp ? t("net_vpn_on") : t("net_vpn_off")));
                out.setTextColor(th().good); a.host.log("net " + r.ms + "ms ip=" + kv.get("ip") + " warp=" + kv.get("warp"));
            } });
        } }).start();
    }
    Map<String, Object> exportBackup() {
        Map<String, Object> d = Json.map("app", "Applied Astrology", "version", Host.VERSION, "date", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(new Date()), "settings", a.S,
            "saved", a.host.list("aa.saved"), "bookmarks", a.host.list("aa.bookmarks"), "recent", a.host.list("aa.recent"));
        String name = "AppliedAstrology-backup-" + Json.str(d.get("date"), "").substring(0, 10) + ".json";
        try { String r = a.host.saveFile(name, Json.stringify(d).getBytes("UTF-8"), "application/json"); a.toast(r != null ? t("s_export") + ": " + r : t("import_fail")); } catch (Exception e) { a.toast(t("import_fail")); }
        return d;
    }
    boolean importBackup(String text) {
        try {
            Map<String, Object> d = Json.obj(Json.parse(text));
            if (d == null || !"Applied Astrology".equals(d.get("app"))) throw new Exception("bad");
            Map<String, Object> st = Json.obj(d.get("settings"));
            if (st != null) { a.S.clear(); a.S.putAll(MainActivity.defaults()); a.S.putAll(st); a.save(); }
            if (d.get("saved") != null) a.host.set("aa.saved", d.get("saved"));
            if (d.get("bookmarks") != null) a.host.set("aa.bookmarks", d.get("bookmarks"));
            if (d.get("recent") != null) a.host.set("aa.recent", d.get("recent"));
            a.i18n.setLang(a.s("lang")); a.applySettings(); a.toast(t("import_ok")); return true;
        } catch (Exception e) { a.toast(t("import_fail")); return false; }
    }
    void resetDefaults() { a.S.clear(); a.S.putAll(MainActivity.defaults()); a.save(); a.i18n.setLang(a.s("lang")); a.applySettings(); }
    void clearData() {
        new android.app.AlertDialog.Builder(a).setMessage(t("confirm_clear")).setPositiveButton(t("btn_delete"), new android.content.DialogInterface.OnClickListener() { public void onClick(android.content.DialogInterface d, int w) {
            a.host.set("aa.saved", new ArrayList<Object>()); a.host.set("aa.bookmarks", new ArrayList<Object>()); a.host.set("aa.recent", new ArrayList<Object>()); a.host.prefs.edit().remove("aa.last").apply();
            resetDefaults(); a.toast(t("deleted")); } }).setNegativeButton(t("btn_close"), null).show();
    }

    // =====================================================================================
    // GIỚI THIỆU (kiểu Aniyomi / Mangayomi)
    // =====================================================================================
    void refreshAbout() { if ("about".equals(a.current)) { int y = a.scroller() != null ? a.scroller().getScrollY() : 0; a.show("about"); final int fy = y; a.scroller().post(new Runnable() { public void run() { a.scroller().scrollTo(0, fy); } }); } }
    void updateProgress() {
        if (dlState == null) return; long g = a.host.dlGot, tot = a.host.dlTotal; int pct = tot > 0 ? (int) (g * 100 / tot) : 0;
        dlState.setText(t("ab_downloading", "p", pct) + " (" + String.format(java.util.Locale.US, "%.1f", g / 1048576.0) + " MB)");
        if (dlBar != null) { dlBar.getLayoutParams().width = (int) (((View) dlBar.getParent()).getWidth() * pct / 100.0); dlBar.requestLayout(); }
    }
    View about() {
        LinearLayout p = page();
        Frame hd = card(p); hd.setTag("aboutHead");
        LinearLayout hr = ui.row(); MainActivity.Logo lg = new MainActivity.Logo(a); hr.addView(lg, ui.margins(Ui.lp(ui.dp(64), ui.dp(64)), 0, 0, 12, 0));
        LinearLayout ht = ui.col(); ht.addView(ui.text(t("app_name"), 19, th().gold2, true)); ht.addView(ui.hint("Applied Astrology · " + t("s_version") + " " + Host.VERSION + " (" + Host.BUILD_DATE + ") · native")); ht.addView(ui.hint(t("s_about_d")));
        hr.addView(ht, Ui.weight(1)); hd.addView(hr, Ui.fill());
        hd.addView(tag(ui.small("← " + t("tab_settings"), new View.OnClickListener() { public void onClick(View v) { a.show("settings"); } }), "aboutBack"), ui.margins(Ui.wrap(), 0, 8, 0, 0));
        // cập nhật
        Frame up = setting(p, t("ab_updates"), null); up.setTag("abUpdates");
        up.addView(tag(ui.toggle(t("ab_auto_check"), a.b("autoUpdate"), new Ui.OnBool() { public void set(boolean v) { setS("autoUpdate", v); } }), "abAuto"));
        up.addView(tag(ui.toggle(t("ab_auto_install"), a.b("autoInstall"), new Ui.OnBool() { public void set(boolean v) { setS("autoInstall", v); } }), "abAutoInst"));
        Ui.Flow ub = ui.flow();
        ub.addView(tag(ui.button(t("ab_check"), true, new View.OnClickListener() { public void onClick(View v) { updates.lastMsg = t("ab_checking"); refreshAbout(); updates.check(false, null); } }), "abCheck"));
        ub.addView(ui.button(t("ab_releases"), false, new View.OnClickListener() { public void onClick(View v) { a.host.openUrl(Updates.RELEASES_URL); } }));
        up.addView(ub, Ui.fill());
        Map<String, Object> lc = Json.obj(a.host.get("aa.lastUpdateCheck"));
        if (lc != null) up.addView(ui.hint(t("ab_last_check") + ": " + DateFormat.getDateTimeInstance().format(new Date((long) Json.dbl(lc.get("at"), 0))) + " (" + Json.str(lc.get("latest"), "") + ")"));
        LinearLayout out = ui.col(); out.setTag("abUpdOut");
        if (!updates.lastMsg.isEmpty()) out.addView(ui.text(updates.lastMsg, 14, "fail".equals(updates.state) ? th().bad : th().good, true));
        if (updates.release != null && updates.newer) {
            out.addView(ui.text(t("ab_whats_new") + " – " + Json.str(updates.release.get("name"), ""), 13.5f, th().gold2, true));
            String body = Json.str(updates.release.get("body"), "").replaceAll("[#*`>]", ""); out.addView(ui.hint(body.length() > 2500 ? body.substring(0, 2500) + "…" : body));
            if (updates.asset != null) {
                out.addView(tag(ui.button(t("ab_download"), true, new View.OnClickListener() { public void onClick(View v) { updates.startDownload(); refreshAbout(); } }), "abDl"), ui.margins(Ui.wrap(), 0, 6, 0, 0));
                out.addView(ui.hint(Json.str(updates.asset.get("name"), "") + " · " + String.format(java.util.Locale.US, "%.1f MB", Json.dbl(updates.asset.get("size"), 0) / 1048576.0)));
                LinearLayout bar = new LinearLayout(a); bar.setBackgroundColor(th().line); dlBar = new View(a); dlBar.setBackgroundColor(th().gold); bar.addView(dlBar, Ui.lp(0, ui.dp(8)));
                out.addView(bar, ui.margins(Ui.lp(-1, ui.dp(8)), 0, 6, 0, 2));
                dlState = ui.hint(""); out.addView(dlState);
                if ("downloading".equals(updates.state)) updateProgress();
                if ("downloaded".equals(updates.state)) { dlState.setText(t("ab_dl_done")); out.addView(tag(ui.button(t("ab_install"), true, new View.OnClickListener() { public void onClick(View v) { updates.install(); } }), "abInst")); out.addView(ui.hint(t("ab_install_note_android"))); }
                if ("error".equals(updates.state)) { dlState.setText(t("ab_dl_fail") + ": " + a.host.dlError); dlState.setTextColor(th().bad); }
            } else out.addView(ui.hint(t("ab_no_asset")));
        }
        up.addView(out, Ui.fill());
        // có gì mới
        Frame wn = setting(p, t("ab_whats_new"), null);
        List<Object> cl = Json.arr(Json.parse(a.host.read("data/changelog.json")));
        String lang = "vi".equals(a.i18n.current()) ? "vi" : "en";
        if (cl != null) for (int k = 0; k < cl.size(); k++) {
            Map<String, Object> e = Json.obj(cl.get(k));
            wn.addView(ui.text(Json.str(e.get("v"), "") + " · " + Json.str(e.get("d"), ""), 14, th().gold2, true), ui.margins(Ui.fill(), 0, 6, 0, 2));
            List<Object> items = Json.arr(e.get(lang)); if (items == null) items = Json.arr(e.get("en"));
            if (items != null) for (int j = 0; j < (k == 0 ? items.size() : Math.min(items.size(), 3)); j++) wn.addView(ui.body("• " + items.get(j)));
        }
        // chẩn đoán
        Frame dg = setting(p, t("ab_diag"), null); dg.setTag("abDiag");
        dg.addView(tag(ui.toggle(t("ab_logs_on"), a.b("logs"), new Ui.OnBool() { public void set(boolean v) { setS("logs", v); } }), "abLogOn"));
        Ui.Flow lb = ui.flow();
        lb.addView(tag(ui.small(t("ab_logs_view"), new View.OnClickListener() { public void onClick(View v) { String lt = updates.logText(); TextView tv = ui.text(lt.substring(Math.max(0, lt.length() - 20000)), 11.5f, th().text, false); tv.setTypeface(android.graphics.Typeface.MONOSPACE); tv.setTextIsSelectable(true); a.modal(t("ab_logs"), tv); } }), "abLogView"));
        lb.addView(tag(ui.small(t("ab_logs_share"), new View.OnClickListener() { public void onClick(View v) { try { String r = a.host.saveFile("AppliedAstrology-log-" + System.currentTimeMillis() + ".txt", updates.logText().getBytes("UTF-8"), "text/plain"); a.toast(r != null ? r : t("ab_copied")); if (r == null) a.host.share(updates.logText()); } catch (Exception ignored) { } } }), "abLogExport"));
        lb.addView(tag(ui.small(t("ab_logs_clear"), new View.OnClickListener() { public void onClick(View v) { a.host.clearLog(); a.toast(t("deleted")); } }), "abLogClear"));
        dg.addView(lb, Ui.fill());
        dg.addView(ui.h3(t("ab_errors"))); dg.addView(ui.hint(t("ab_errors_d")));
        LinearLayout el = ui.col(); el.setTag("abErrList");
        List<String[]> cr = a.host.crashList();
        if (cr.isEmpty()) el.addView(ui.hint(t("ab_errors_none")));
        for (String[] c : cr) { Frame ci = listItem("⚠ " + c[0], null, null, null); TextView tx = ui.text(c[1].substring(0, Math.min(3000, c[1].length())), 11, th().text, false); tx.setTypeface(android.graphics.Typeface.MONOSPACE); ci.addView(tx); el.addView(ci, ui.margins(Ui.fill(), 0, 4, 0, 0)); }
        dg.addView(el, Ui.fill());
        Ui.Flow rb = ui.flow();
        rb.addView(tag(ui.button(t("ab_report"), true, new View.OnClickListener() { public void onClick(View v) { a.host.openUrl(updates.reportUrl()); } }), "abReport"));
        rb.addView(tag(ui.button(t("ab_copy_debug"), false, new View.OnClickListener() { public void onClick(View v) { a.host.copy(updates.debugInfo()); a.toast(t("ab_copied")); } }), "abCopy"));
        rb.addView(tag(ui.button(t("ab_errors_clear"), false, new View.OnClickListener() { public void onClick(View v) { a.host.clearCrash(); a.show("about"); a.toast(t("deleted")); } }), "abErrClear"));
        dg.addView(rb, ui.margins(Ui.fill(), 0, 6, 0, 0)); dg.addView(ui.hint(t("ab_report_d")));
        TextView dbg = ui.text(updates.debugInfo(), 11.5f, th().text, false); dbg.setTypeface(android.graphics.Typeface.MONOSPACE); dbg.setTextIsSelectable(true); dbg.setTag("abDebug");
        dbg.setBackgroundColor(Theme.alpha(th().card2, 200)); dbg.setPadding(ui.dp(8), ui.dp(8), ui.dp(8), ui.dp(8)); dg.addView(dbg, ui.margins(Ui.fill(), 0, 6, 0, 0));
        // giấy phép
        Frame li = setting(p, t("ab_licenses"), null);
        for (final String[] l : Updates.LICENSES) li.addView(listItem(l[0], l[1], null, l[2].isEmpty() ? null : new View.OnClickListener() { public void onClick(View v) { a.host.openUrl(l[2]); } }), ui.margins(Ui.fill(), 0, 4, 0, 0));
        Frame lk = setting(p, t("ab_links"), null); Ui.Flow lf = ui.flow();
        lf.addView(ui.button("GitHub", false, new View.OnClickListener() { public void onClick(View v) { a.host.openUrl(Updates.REPO_URL); } }));
        lf.addView(ui.button("Releases", false, new View.OnClickListener() { public void onClick(View v) { a.host.openUrl(Updates.RELEASES_URL); } }));
        lf.addView(ui.button("Issues", false, new View.OnClickListener() { public void onClick(View v) { a.host.openUrl(Updates.REPO_URL + "/issues"); } }));
        lk.addView(lf, Ui.fill());
        return p;
    }
}
