package com.appliedastrology.tuvi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import com.appliedastrology.tuvi.core.Engine;
import com.appliedastrology.tuvi.core.I18n;
import com.appliedastrology.tuvi.core.Theme;

import java.util.ArrayList;
import java.util.List;

/** Vẽ lá số native: bàn 4×4 cổ điển, thẻ từng cung (bố cục danh sách), xuất ảnh PNG. */
public final class ChartViews {
    private ChartViews() { }
    public static final int[][] LAYOUT = {{5, 6, 7, 8}, {4, -1, -1, 9}, {3, -1, -1, 10}, {2, 1, 0, 11}};

    public static final class Opts {
        public int sel = -1; public boolean ts = true, bs = false, tt = true, luu = true, glass; public int[] paper; public Theme th; public I18n i18n; public String version = Host.VERSION;
        boolean inTp(Engine.Chart c, int chi) { if (sel < 0) return false; int[] tp = c.tamPhuong(sel); for (int k = 1; k < 4; k++) if (tp[k] == chi) return true; return false; }
    }

    /** Bộ vẽ dùng chung (đơn vị u = cỡ chữ cơ sở theo px) */
    static final class Drawer {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG), fill = new Paint(Paint.ANTI_ALIAS_FLAG), line = new Paint(Paint.ANTI_ALIAS_FLAG);
        Drawer() { line.setStyle(Paint.Style.STROKE); }
        float text(Canvas c, String s, float x, float y, float size, int color, boolean bold, int align, float maxW) {
            p.setTextSize(size); p.setColor(color); p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            p.setTextAlign(align == 0 ? Paint.Align.LEFT : align == 1 ? Paint.Align.CENTER : Paint.Align.RIGHT);
            if (maxW > 0 && p.measureText(s) > maxW) { int n = p.breakText(s, true, maxW - p.measureText("…"), null); s = s.substring(0, Math.max(0, n)) + "…"; }
            c.drawText(s, x, y - p.ascent(), p); return size * 1.22f;
        }
        static String tr(Opts o, String vn) { return o.i18n.palace(vn); }

        /** Vẽ một cung trong khung r; trả về chiều cao thực dùng */
        float palace(Canvas c, Engine.Chart ch, int chi, RectF r, Opts o, float u, boolean measureOnly) {
            Engine.Palace pa = ch.palaces[chi]; Theme th = o.th;
            int textC = th.text, muted = th.muted, nameC = th.gold2;
            if (o.paper != null) { textC = o.paper[1]; muted = Theme.alpha(o.paper[1], 200); nameC = o.paper[1]; }
            if (!measureOnly) {
                int bgc = o.paper != null ? o.paper[0] : (o.sel == chi ? th.sel : th.card2);
                fill.setColor(o.glass && o.paper == null && o.sel != chi ? Theme.alpha(bgc, 150) : bgc);
                float rad = u * 0.7f; c.drawRoundRect(r, rad, rad, fill);
                line.setStrokeWidth(o.sel == chi ? u * 0.16f : Math.max(1, u * 0.07f));
                line.setColor(o.sel == chi ? th.gold : o.inTp(ch, chi) ? th.accent : pa.isMenh ? Theme.alpha(th.gold, 170) : th.line);
                c.drawRoundRect(r, rad, rad, line);
            }
            float pad = u * 0.45f, x = r.left + pad, w = r.width() - pad * 2, y = r.top + pad;
            // tiêu đề: tên cung + huy hiệu | Can Chi
            String name = tr(o, pa.name).toUpperCase();
            if (!measureOnly) {
                text(c, pa.canChi, r.right - pad, y + u * 0.1f, u * 0.92f, muted, false, 2, 0);
                float cw = measure(pa.canChi, u * 0.92f, false) + u * 0.6f;
                float nw = Math.min(w - cw, measure(name, u * 1.08f, true));
                text(c, name, x, y, u * 1.08f, nameC, true, 0, w - cw);
                float bx = x + nw + u * 0.3f;
                List<String[]> badges = new ArrayList<String[]>();
                if (pa.isMenh) badges.add(new String[]{o.i18n.t("menh"), "g"});
                if (pa.isThan) badges.add(new String[]{o.i18n.t("than"), "a"});
                if (pa.tuan) badges.add(new String[]{o.i18n.t("tuan"), "b"});
                if (pa.triet) badges.add(new String[]{o.i18n.t("triet"), "b"});
                for (String[] b : badges) {
                    float bwid = measure(b[0], u * 0.75f, true) + u * 0.5f;
                    if (bx + bwid > r.right - pad - cw) break;
                    fill.setColor(b[1].equals("g") ? th.gold : b[1].equals("a") ? th.accent : th.bad);
                    RectF br = new RectF(bx, y + u * 0.15f, bx + bwid, y + u * 1.15f); c.drawRoundRect(br, u * 0.25f, u * 0.25f, fill);
                    text(c, b[0], bx + bwid / 2, y + u * 0.22f, u * 0.75f, b[1].equals("b") ? 0xFFFFFFFF : (th.dark ? 0xFF1A1205 : 0xFFFFFFFF), true, 1, 0);
                    bx += bwid + u * 0.2f;
                }
            }
            y += u * 1.45f;
            // chính tinh
            if (pa.major.isEmpty()) { if (!measureOnly) text(c, o.i18n.t("vcd"), x, y, u * 1.0f, muted, true, 0, w); y += u * 1.25f; }
            for (Engine.Star s : pa.major) {
                if (!measureOnly) {
                    text(c, s.name, x, y, u * 1.02f, o.paper != null ? textC : th.element(s.element), true, 0, w);
                    float nx = x + measure(s.name, u * 1.02f, true) + u * 0.3f;
                    text(c, "(" + s.bright + ")", nx, y + u * 0.12f, u * 0.82f, muted, false, 0, 0);
                }
                y += u * 1.25f;
            }
            // phụ tinh 2 cột
            List<Object[]> left = new ArrayList<Object[]>(), right = new ArrayList<Object[]>();
            for (Engine.Star s : pa.good) left.add(new Object[]{s.name, s.attachedTo != null ? th.hoa(s.name) : th.good});
            for (Engine.Star s : pa.neutral) left.add(new Object[]{s.name, textC});
            for (Engine.Star s : pa.bad) right.add(new Object[]{s.name, s.attachedTo != null ? th.hoa(s.name) : th.bad});
            if (o.luu) for (String l : pa.luu) right.add(new Object[]{l, th.purple});
            int rows = Math.max(left.size(), right.size());
            float ry = y + u * 0.15f, colW = w / 2 - u * 0.2f;
            if (!measureOnly) for (int i = 0; i < rows; i++) {
                if (i < left.size()) text(c, (String) left.get(i)[0], x, ry, u * 0.86f, o.paper != null ? textC : (Integer) left.get(i)[1], false, 0, colW);
                if (i < right.size()) text(c, (String) right.get(i)[0], x + w / 2 + u * 0.2f, ry, u * 0.86f, o.paper != null ? textC : (Integer) right.get(i)[1], false, 0, colW);
                ry += u * 1.06f;
            }
            y += u * 0.15f + rows * u * 1.06f;
            // vòng sao
            List<String> ring = new ArrayList<String>();
            if (o.ts && pa.ringTrangSinh != null) ring.add(pa.ringTrangSinh); if (o.bs && pa.ringBacSi != null) ring.add(pa.ringBacSi); if (o.tt && pa.ringThaiTue != null) ring.add(pa.ringThaiTue);
            if (!ring.isEmpty()) { if (!measureOnly) text(c, join(ring, " · "), x, y + u * 0.1f, u * 0.8f, muted, false, 0, w); y += u * 1.05f; }
            // chân: đại hạn | tiểu hạn
            y += u * 0.2f;
            if (!measureOnly) {
                line.setStrokeWidth(Math.max(1, u * 0.05f)); line.setColor(Theme.alpha(th.muted, 90)); c.drawLine(x, y, x + w, y, line);
                boolean cdh = ch.currentDaiHan == chi, cth = ch.currentTieuHan == chi;
                text(c, o.i18n.t("dai_han") + " " + pa.daiHan, x, y + u * 0.2f, u * 0.82f, cdh ? th.gold : muted, cdh, 0, w * 0.7f);
                text(c, pa.tieuHan, x + w, y + u * 0.2f, u * 0.82f, cth ? th.accent : muted, cth, 2, 0);
            }
            y += u * 1.25f + pad;
            return y - r.top;
        }
        float measure(String s, float size, boolean bold) { p.setTextSize(size); p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT); return p.measureText(s); }
        static String join(List<String> l, String sep) { StringBuilder b = new StringBuilder(); for (int i = 0; i < l.size(); i++) { if (i > 0) b.append(sep); b.append(l.get(i)); } return b.toString(); }

        List<String[]> centerRows(Engine.Chart ch, Opts o) {
            I18n t = o.i18n; List<String[]> rows = new ArrayList<String[]>();
            rows.add(new String[]{t.t("solar_date"), ch.sDay + "/" + ch.sMonth + "/" + ch.sYear});
            rows.add(new String[]{t.t("lunar_date"), ch.lunar.day + "/" + ch.lunar.month + (ch.lunar.leap ? " (N)" : "") + "/" + ch.lunar.year});
            rows.add(new String[]{t.t("year_cc"), ch.yearCanChi}); rows.add(new String[]{t.t("month_cc"), ch.monthCanChi});
            rows.add(new String[]{t.t("day_cc"), ch.dayCanChi}); rows.add(new String[]{t.t("hour_cc"), ch.hourCanChi});
            rows.add(new String[]{t.t("am_duong"), ch.amDuong + " · " + ch.amDuongLy}); rows.add(new String[]{t.t("ban_menh"), ch.napAm});
            rows.add(new String[]{t.t("cuc"), ch.cucName}); rows.add(new String[]{t.t("menh_cuc"), ch.menhCuc});
            rows.add(new String[]{t.t("menh_chu"), ch.menhChu}); rows.add(new String[]{t.t("than_chu"), ch.thanChu});
            rows.add(new String[]{t.t("f_viewyear"), ch.viewYear + " (" + ch.viewYearCanChi + ") · " + t.t("age") + " " + ch.age});
            return rows;
        }
        float center(Canvas c, Engine.Chart ch, RectF r, Opts o, float u, boolean measureOnly) {
            Theme th = o.th;
            if (!measureOnly) {
                fill.setColor(o.paper != null ? o.paper[0] : (o.glass ? Theme.alpha(th.card, 150) : th.card)); c.drawRoundRect(r, u * 0.8f, u * 0.8f, fill);
                line.setStrokeWidth(Math.max(1, u * 0.08f)); line.setColor(Theme.alpha(th.gold, 200)); c.drawRoundRect(r, u * 0.8f, u * 0.8f, line);
                // biểu tượng Âm Dương
                float cx = r.centerX(), cy = r.top + u * 2.4f, rr = u * 1.8f;
                fill.setColor(th.gold); c.drawArc(new RectF(cx - rr, cy - rr, cx + rr, cy + rr), -90, 180, true, fill);
                fill.setColor(0xFF3B2A6B); c.drawArc(new RectF(cx - rr, cy - rr, cx + rr, cy + rr), 90, 180, true, fill);
                fill.setColor(th.gold); c.drawCircle(cx, cy + rr / 2, rr / 2, fill); fill.setColor(0xFF3B2A6B); c.drawCircle(cx, cy - rr / 2, rr / 2, fill);
                fill.setColor(0xFF3B2A6B); c.drawCircle(cx, cy + rr / 2, rr / 7, fill); fill.setColor(th.gold); c.drawCircle(cx, cy - rr / 2, rr / 7, fill);
            }
            float y = r.top + u * 4.6f;
            int nameC = o.paper != null ? o.paper[1] : th.gold2, txt = o.paper != null ? o.paper[1] : th.text, mut = o.paper != null ? o.paper[1] : th.muted;
            if (!measureOnly) text(c, ch.name, r.centerX(), y, u * 1.35f, nameC, true, 1, r.width() - u);
            y += u * 1.8f;
            for (String[] row : centerRows(ch, o)) {
                if (!measureOnly) {
                    float lw = measure(row[0] + ": ", u * 0.88f, false), vw = measure(row[1], u * 0.92f, true), tot = Math.min(r.width() - u, lw + vw), sx = r.centerX() - tot / 2;
                    text(c, row[0] + ": ", sx, y, u * 0.88f, mut, false, 0, 0);
                    text(c, row[1], sx + lw, y, u * 0.92f, txt, true, 0, r.width() - u - lw);
                }
                y += u * 1.2f;
            }
            return y + u * 0.6f - r.top;
        }
    }

    /** Bàn 4×4 (đặt trong HorizontalScrollView, rộng tối thiểu 620dp) */
    public static final class Board extends View {
        Engine.Chart chart; Opts o; final Drawer d = new Drawer(); OnPalace cb; final float dens;
        public interface OnPalace { void pick(int chi); }
        public Board(Context c) { super(c); dens = c.getResources().getDisplayMetrics().density; }
        public void set(Engine.Chart ch, Opts o, OnPalace cb) { chart = ch; this.o = o; this.cb = cb; requestLayout(); invalidate(); }
        float unit(int w) { return w / 4f / 12.4f; }
        @Override protected void onMeasure(int ws, int hs) {
            int w = Math.max(MeasureSpec.getSize(ws), (int) (620 * dens));
            if (chart == null) { setMeasuredDimension(w, (int) (200 * dens)); return; }
            float u = unit(w), cw = w / 4f, maxH = 0;
            for (int chi = 0; chi < 12; chi++) maxH = Math.max(maxH, d.palace(null, chart, chi, new RectF(0, 0, cw - u * 0.4f, 9999), o, u, true));
            float centerH = d.center(null, chart, new RectF(0, 0, cw * 2, 9999), o, u, true) / 2;
            float cellH = Math.max(Math.max(maxH, centerH), cw * 1.05f) + u * 0.4f;
            setMeasuredDimension(w, (int) (cellH * 4));
        }
        @Override protected void onDraw(Canvas c) {
            if (chart == null) return;
            int w = getWidth(); float u = unit(w), cw = w / 4f, chh = getHeight() / 4f, g = u * 0.2f;
            for (int r = 0; r < 4; r++) for (int col = 0; col < 4; col++) {
                int chi = LAYOUT[r][col]; if (chi < 0) continue;
                d.palace(c, chart, chi, new RectF(col * cw + g, r * chh + g, (col + 1) * cw - g, (r + 1) * chh - g), o, u, false);
            }
            d.center(c, chart, new RectF(cw + g, chh + g, cw * 3 - g, chh * 3 - g), o, u, false);
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if (chart == null) return false;
            if (e.getAction() == MotionEvent.ACTION_DOWN) return true;
            if (e.getAction() == MotionEvent.ACTION_UP) {
                int col = (int) (e.getX() / (getWidth() / 4f)), r = (int) (e.getY() / (getHeight() / 4f));
                if (col >= 0 && col < 4 && r >= 0 && r < 4 && LAYOUT[r][col] >= 0 && cb != null) { performClick(); cb.pick(LAYOUT[r][col]); }
                return true;
            }
            return true;
        }
        @Override public boolean performClick() { return super.performClick(); }
    }

    /** Một cung trong bố cục danh sách, hoặc Thiên Bàn (chi = -1) */
    public static final class Item extends View {
        Engine.Chart chart; Opts o; int chi; final Drawer d = new Drawer(); final float dens; Board.OnPalace cb;
        public Item(Context c, Engine.Chart ch, int chi, Opts o, Board.OnPalace cb) { super(c); chart = ch; this.chi = chi; this.o = o; this.cb = cb; dens = c.getResources().getDisplayMetrics().density; setClickable(chi >= 0); }
        float u() { return 13.2f * dens * fontScale; }
        public float fontScale = 1f;
        @Override protected void onMeasure(int ws, int hs) {
            int w = MeasureSpec.getSize(ws); float u = u();
            float h = chi < 0 ? d.center(null, chart, new RectF(0, 0, w, 9999), o, u, true) : d.palace(null, chart, chi, new RectF(0, 0, w, 9999), o, u, true);
            setMeasuredDimension(w, (int) h + 1);
        }
        @Override protected void onDraw(Canvas c) {
            RectF r = new RectF(0, 0, getWidth(), getHeight());
            if (chi < 0) d.center(c, chart, r, o, u(), false); else d.palace(c, chart, chi, r, o, u(), false);
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if (chi < 0) return false;
            if (e.getAction() == MotionEvent.ACTION_UP && cb != null) { performClick(); cb.pick(chi); }
            return true;
        }
        @Override public boolean performClick() { return super.performClick(); }
    }

    /** Ảnh PNG lá số 1400×1460 (giống bản 0.0.3) */
    public static Bitmap render(Engine.Chart ch, Opts o) {
        int W = 1400, H = 1400; Bitmap b = Bitmap.createBitmap(W, H + 60, Bitmap.Config.ARGB_8888); Canvas c = new Canvas(b);
        c.drawColor(o.th.bg); Drawer d = new Drawer(); float u = W / 4f / 12.4f, cw = W / 4f, chh = H / 4f, g = 4;
        Opts po = new Opts(); po.th = o.th; po.i18n = o.i18n; po.ts = o.ts; po.bs = o.bs; po.tt = o.tt; po.luu = o.luu; po.paper = o.paper; po.sel = -1;
        for (int r = 0; r < 4; r++) for (int col = 0; col < 4; col++) {
            int chi = LAYOUT[r][col]; if (chi < 0) continue;
            c.save(); RectF rr = new RectF(col * cw + g, r * chh + g, (col + 1) * cw - g, (r + 1) * chh - g); c.clipRect(rr);
            d.palace(c, ch, chi, rr, po, u, false); c.restore();
        }
        d.center(c, ch, new RectF(cw + g, chh + g, cw * 3 - g, chh * 3 - g), po, u * 1.15f, false);
        d.text(c, "Applied Astrology " + o.version + " · Tử Vi Ứng Dụng – Bửu Đình", W / 2f, H + 16, 20, o.th.muted, false, 1, 0);
        return b;
    }
}
