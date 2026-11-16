package com.appliedastrology.tuvi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import com.appliedastrology.tuvi.core.Cal;
import com.appliedastrology.tuvi.core.Lunar;
import com.appliedastrology.tuvi.core.Theme;

import java.util.Calendar;

/** Lịch tháng Âm Dương (native): ngày dương, ngày âm, tiết khí, chấm hoàng đạo, ngày lễ, hôm nay, ngày đang chọn. */
public final class MonthView extends View {
    public interface OnDay { void pick(int d); }
    int y, m, selD; String[] wds; Theme th; OnDay cb; boolean glass; float fs = 1f;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG), f = new Paint(Paint.ANTI_ALIAS_FLAG), s = new Paint(Paint.ANTI_ALIAS_FLAG);
    final float dens; Cal.DayInfo[] info = new Cal.DayInfo[32];
    public MonthView(Context c) { super(c); dens = c.getResources().getDisplayMetrics().density; s.setStyle(Paint.Style.STROKE); }
    public void set(int y, int m, int selD, String[] wds, Theme th, boolean glass, float fs, OnDay cb) {
        this.y = y; this.m = m; this.selD = selD; this.wds = wds; this.th = th; this.glass = glass; this.fs = fs; this.cb = cb;
        int n = Lunar.daysInMonth(m, y); for (int d = 1; d <= n; d++) info[d] = Cal.dayInfo(d, m, y);
        requestLayout(); invalidate();
    }
    int lead() { return (Lunar.weekday(1, m, y) + 6) % 7; }
    int rows() { return (lead() + Lunar.daysInMonth(m, y) + 6) / 7; }
    @Override protected void onMeasure(int ws, int hs) {
        int w = MeasureSpec.getSize(ws); float cw = w / 7f, ch = Math.max(56 * dens * fs, cw * 0.95f);
        setMeasuredDimension(w, (int) (28 * dens * fs + rows() * ch));
    }
    float cellH() { return (getHeight() - 28 * dens * fs) / rows(); }
    void text(Canvas c, String t, float x, float yy, float size, int color, boolean bold, Paint.Align al) {
        p.setTextSize(size); p.setColor(color); p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT); p.setTextAlign(al); c.drawText(t, x, yy - p.ascent(), p);
    }
    @Override protected void onDraw(Canvas c) {
        if (th == null) return;
        float w = getWidth(), cw = w / 7f, hh = 28 * dens * fs, ch = cellH(), g = 1.5f * dens;
        for (int i = 0; i < 7; i++) text(c, wds[i], cw * i + cw / 2, 6 * dens, 12 * dens * fs, i >= 5 ? th.bad : th.muted, true, Paint.Align.CENTER);
        Calendar now = Calendar.getInstance(); int lead = lead(), n = Lunar.daysInMonth(m, y);
        for (int d = 1; d <= n; d++) {
            int idx = lead + d - 1, col = idx % 7, row = idx / 7; Cal.DayInfo di = info[d];
            RectF r = new RectF(col * cw + g, hh + row * ch + g, (col + 1) * cw - g, hh + (row + 1) * ch - g);
            boolean today = d == now.get(Calendar.DAY_OF_MONTH) && m == now.get(Calendar.MONTH) + 1 && y == now.get(Calendar.YEAR), sel = d == selD;
            f.setColor(sel ? th.sel : (glass ? Theme.alpha(th.card, 150) : th.card)); c.drawRoundRect(r, 8 * dens, 8 * dens, f);
            s.setStrokeWidth(today ? 2 * dens : dens); s.setColor(today || sel ? th.gold : th.line); c.drawRoundRect(r, 8 * dens, 8 * dens, s);
            boolean we = col >= 5;
            text(c, String.valueOf(d), r.left + 5 * dens, r.top + 3 * dens, 16 * dens * fs, we ? th.bad : th.text, true, Paint.Align.LEFT);
            if (!di.events.isEmpty()) { f.setColor(th.accent); c.drawRect(r.left + 5 * dens, r.top + 3 * dens + 18 * dens * fs, r.left + 5 * dens + 12 * dens * fs, r.top + 4.5f * dens + 18 * dens * fs, f); }
            boolean ld = di.lunar.day == 1 || di.lunar.day == 15;
            String lt = di.lunar.day == 1 || d == 1 ? di.lunar.day + "/" + di.lunar.month + (di.lunar.leap ? "*" : "") : String.valueOf(di.lunar.day);
            text(c, lt, r.right - 4 * dens, r.bottom - 15 * dens * fs - (di.tietStart ? 10 * dens * fs : 0), 11 * dens * fs, ld ? th.gold : th.muted, ld, Paint.Align.RIGHT);
            if (di.tietStart) {
                p.setTextSize(8.5f * dens * fs); String tk = di.tiet; float mw = r.width() - 6 * dens;
                if (p.measureText(tk) > mw) tk = tk.substring(0, Math.max(1, p.breakText(tk, true, mw, null)));
                text(c, tk, r.right - 3 * dens, r.bottom - 11 * dens * fs, 8.5f * dens * fs, th.muted, false, Paint.Align.RIGHT);
            }
            if (di.hoangDao) { f.setColor(th.good); c.drawCircle(r.right - 7 * dens, r.top + 7 * dens, 3 * dens, f); }
        }
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (e.getAction() == MotionEvent.ACTION_UP) {
            float cw = getWidth() / 7f, hh = 28 * dens * fs; int col = (int) (e.getX() / cw), row = (int) ((e.getY() - hh) / cellH());
            int d = row * 7 + col - lead() + 1;
            if (e.getY() > hh && d >= 1 && d <= Lunar.daysInMonth(m, y) && cb != null) { performClick(); cb.pick(d); }
        }
        return true;
    }
    @Override public boolean performClick() { return super.performClick(); }
}
