package com.appliedastrology.tuvi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import com.appliedastrology.tuvi.core.Glass;
import com.appliedastrology.tuvi.core.Theme;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Liquid Glass native cho Android 6.0 → 17 (không cần RenderEffect/backdrop-filter):
 *  - Bg: vẽ nền tinh vân (Glass.nebula) và giữ bản thu nhỏ đã làm mờ để các tấm kính lấy mẫu.
 *  - Frame: LinearLayout có nền kính. Thanh tiêu đề/thanh tab/hộp thoại dùng khúc xạ thật (Glass.render, có bộ đệm);
 *    thẻ nội dung cuộn dùng kính mờ (BitmapShader của nền đã làm mờ theo vị trí trên màn hình) + phủ màu + viền phản xạ
 *    để cuộn mượt cả trên máy yếu.
 */
public final class GlassViews {
    private GlassViews() { }

    public static final class Cfg {
        public String mode = "liquid"; // off | frost | liquid
        public String bg = "nebula";   // nebula | aurora | plain
        public boolean cards = true, chart = false;
        public final Glass.Params p = new Glass.Params();
        public Theme th = Theme.CELESTIAL;
        public int version;
    }

    public static final class Bg extends View {
        final Cfg cfg; Bitmap full, blurBmp; int[] blur; int bw, bh; static final int SCALE = 4; int builtFor = -1, bW, bH;
        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        final List<WeakReference<Frame>> frames = new ArrayList<WeakReference<Frame>>();
        public Bg(Context c, Cfg cfg) { super(c); this.cfg = cfg; }
        void register(Frame f) { frames.add(new WeakReference<Frame>(f)); }
        public void invalidateFrames() {
            for (Iterator<WeakReference<Frame>> it = frames.iterator(); it.hasNext(); ) { Frame f = it.next().get(); if (f == null) it.remove(); else f.invalidate(); }
        }
        void ensure() {
            int w = getWidth(), h = getHeight(); if (w < 4 || h < 4) return;
            if (builtFor == cfg.version && bW == w && bH == h && full != null) return;
            builtFor = cfg.version; bW = w; bH = h;
            int hw = Math.max(2, w / 2), hh = Math.max(2, h / 2);
            int[] px = Glass.nebula(hw, hh, cfg.th, "off".equals(cfg.mode) ? "plain" : cfg.bg, 20261116);
            if (full != null) full.recycle();
            full = Bitmap.createBitmap(px, hw, hh, Bitmap.Config.ARGB_8888);
            bw = Math.max(2, w / SCALE); bh = Math.max(2, h / SCALE);
            Bitmap sm = Bitmap.createScaledBitmap(full, bw, bh, true);
            blur = new int[bw * bh]; sm.getPixels(blur, 0, bw, 0, 0, bw, bh); sm.recycle();
            Glass.boxBlur(blur, bw, bh, Math.max(1, (int) Math.round(cfg.p.blur / SCALE)));
            if (blurBmp != null) blurBmp.recycle();
            blurBmp = Bitmap.createBitmap(blur, bw, bh, Bitmap.Config.ARGB_8888);
            invalidateFrames();
        }
        @Override protected void onSizeChanged(int w, int h, int ow, int oh) { super.onSizeChanged(w, h, ow, oh); post(new Runnable() { public void run() { ensure(); invalidate(); } }); }
        @Override protected void onDraw(Canvas c) {
            ensure();
            if (full == null) { c.drawColor(cfg.th.bg); return; }
            c.drawBitmap(full, null, new RectF(0, 0, getWidth(), getHeight()), paint);
        }
    }

    /** Khung kính. kind: 0 = thẻ (kính mờ), 1 = thanh/hộp thoại (khúc xạ thật nếu chế độ liquid) */
    public static class Frame extends LinearLayout {
        final Bg bg; final Cfg cfg; final int kind; float radius; final float dens;
        Bitmap cache; String cacheKey; int[] buf;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG), stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int[] loc = new int[2], bgLoc = new int[2]; final RectF r = new RectF(); final Matrix m = new Matrix();
        public int solidColor; public boolean selected;
        public Frame(Context c, Bg bg, Cfg cfg, int kind, float radiusDp) {
            super(c); this.bg = bg; this.cfg = cfg; this.kind = kind; dens = c.getResources().getDisplayMetrics().density; radius = radiusDp * dens;
            setWillNotDraw(false); setOrientation(VERTICAL); bg.register(this);
            setOutlineProvider(new ViewOutlineProvider() { @Override public void getOutline(View v, Outline o) { o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius); o.setAlpha(0.55f); } });
            if (kind == 1) setElevation(3 * dens); else if (kind == 0 && !"off".equals(cfg.mode)) setElevation(1.5f * dens);
        }
        public void setRadius(float dp) { radius = dp * dens; invalidateOutline(); invalidate(); }
        boolean glassOn() { return !"off".equals(cfg.mode) && (kind != 0 || cfg.cards); }
        @Override protected void onDraw(Canvas c) {
            int w = getWidth(), h = getHeight(); if (w <= 0 || h <= 0) return;
            r.set(0, 0, w, h);
            Theme th = cfg.th;
            int base = solidColor != 0 ? solidColor : (kind == 1 ? th.bg2 : th.card);
            if (!glassOn() || bg.blurBmp == null) {
                p.setShader(null); p.setColor(base); c.drawRoundRect(r, radius, radius, p);
                stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(dens); stroke.setShader(null); stroke.setColor(selected ? th.gold : th.line);
                r.inset(dens / 2, dens / 2); c.drawRoundRect(r, radius, radius, stroke); return;
            }
            getLocationInWindow(loc); bg.getLocationInWindow(bgLoc);
            int ox = loc[0] - bgLoc[0], oy = loc[1] - bgLoc[1];
            if (kind == 1 && "liquid".equals(cfg.mode) && w * h <= 1600000) {
                String key = w + "x" + h + "@" + ox + "," + oy + "/" + cfg.version + "/" + cfg.p.key() + "/" + base + "/" + radius;
                if (!key.equals(cacheKey)) {
                    Glass.Maps mp = Glass.maps(w, h, Math.round(radius), cfg.p);
                    if (buf == null || buf.length != w * h) buf = new int[w * h];
                    Glass.render(bg.blur, bg.bw, bg.bh, Bg.SCALE, ox, oy, mp, cfg.p, base, buf);
                    if (cache == null || cache.getWidth() != w || cache.getHeight() != h) { if (cache != null) cache.recycle(); cache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); }
                    cache.setPixels(buf, 0, w, 0, 0, w, h); cacheKey = key;
                }
                p.setShader(null); c.drawBitmap(cache, 0, 0, p);
            } else {
                // kính mờ: nền đã làm mờ ở đúng vị trí màn hình
                BitmapShader sh = new BitmapShader(bg.blurBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                m.reset(); m.setScale(Bg.SCALE, Bg.SCALE); m.postTranslate(-ox, -oy); sh.setLocalMatrix(m);
                p.setShader(sh); c.drawRoundRect(r, radius, radius, p); p.setShader(null);
                p.setColor(Theme.alpha(base, (int) Math.round(255 * Math.min(0.92, cfg.p.tint + 0.12)))); c.drawRoundRect(r, radius, radius, p);
                // viền phản xạ: sáng ở cạnh trên-trái, mờ dần
                float a = (float) cfg.p.specular;
                stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(Math.max(1f, dens));
                stroke.setShader(new LinearGradient(0, 0, w * 0.6f, h, Theme.alpha(0xFFFFFFFF, (int) (230 * a)), Theme.alpha(0xFFFFFFFF, (int) (40 * a)), Shader.TileMode.CLAMP));
                r.inset(dens / 2, dens / 2); c.drawRoundRect(r, radius, radius, stroke); stroke.setShader(null);
            }
            if (selected) { stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(2 * dens); stroke.setColor(th.gold); r.set(dens, dens, w - dens, h - dens); c.drawRoundRect(r, radius, radius, stroke); }
        }
    }
}
