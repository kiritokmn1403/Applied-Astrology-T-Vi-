package com.appliedastrology.tuvi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Process;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import com.appliedastrology.tuvi.core.Glass;
import com.appliedastrology.tuvi.core.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Liquid Glass native cho Android 6.0 → 17 (không cần RenderEffect/backdrop-filter).
 *
 * v0.0.5 – viết lại để hết lag/giật/treo:
 *  - Mọi phép tính điểm ảnh (tinh vân, làm mờ, khúc xạ viền) chạy trên 1 luồng nền ưu tiên thấp, không chặn luồng giao diện.
 *    Khi chưa xong, khung vẽ tạm bằng kính mờ (GPU) rồi tự chuyển sang khúc xạ thật.
 *  - Khúc xạ chỉ tính ở DẢI VIỀN VÁT (Glass.renderBand, ~5–10 % số điểm ảnh); lõi tấm kính do GPU vẽ bằng BitmapShader + ColorMatrix.
 *  - Cuộn: chỉ vẽ lại các thẻ đang gắn vào cửa sổ và THỰC SỰ đổi vị trí, gộp tối đa 1 lần mỗi khung hình (postOnAnimation).
 *    0.0.4 vẽ lại mọi thẻ (kể cả ngoài màn hình) ở mỗi pixel cuộn và tạo mới BitmapShader + LinearGradient mỗi lần vẽ.
 *  - Shader/gradient được giữ lại giữa các lần vẽ; thẻ không còn elevation (bóng đổ của hàng trăm thẻ rất tốn GPU).
 *  - Tinh vân chỉ dựng lại khi đổi chủ đề/kiểu nền/kích thước; đổi độ mờ chỉ làm mờ lại ảnh nhỏ.
 */
public final class GlassViews {
    private GlassViews() { }

    /** Luồng nền dùng chung cho mọi phép tính ảnh của kính */
    static final ExecutorService WORK = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(final Runnable r) {
            Thread t = new Thread(new Runnable() { public void run() { try { Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND); } catch (Throwable ignored) { } r.run(); } }, "aa-glass");
            t.setDaemon(true); return t;
        }
    });

    public static final class Cfg {
        public String mode = "liquid"; // off | frost | liquid (mode hiệu dụng: lgOn = false → off)
        public String bg = "nebula";   // nebula | aurora | plain
        public boolean cards = true, chart = false;
        public final Glass.Params p = new Glass.Params();
        public Theme th = Theme.CELESTIAL;
        public int version;
        ColorMatrixColorFilter satFilter; double satFor = -1;
        ColorMatrixColorFilter sat() {
            if (satFilter == null || satFor != p.saturation) { ColorMatrix cm = new ColorMatrix(); cm.setSaturation((float) p.saturation); satFilter = new ColorMatrixColorFilter(cm); satFor = p.saturation; }
            return satFilter;
        }
    }

    /** Thống kê hiệu năng (selftest đọc để chấm điểm) */
    public static volatile long lastBgMs = -1, lastBandMs = -1, bandCount = 0, scrollTicks = 0, scrollRedraws = 0;

    public static final class Bg extends View {
        final Cfg cfg; Bitmap full, blurBmp; int[] blur; int bw, bh; static final int SCALE = 4;
        int gen; String nebKey, blurKey, pendingKey; int[] small; int sw, sh;   // small = tinh vân thu nhỏ chưa làm mờ (để chỉ làm mờ lại)
        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG); final RectF dst = new RectF();
        final List<Frame> frames = new ArrayList<Frame>();
        boolean scrollPending;
        final Runnable scrollTick = new Runnable() { public void run() {
            scrollPending = false; scrollTicks++;
            for (int i = 0; i < frames.size(); i++) { Frame f = frames.get(i); if (f.kind == 0 && f.glassOn() && f.moved()) { f.invalidate(); scrollRedraws++; } }
        } };
        public Bg(Context c, Cfg cfg) { super(c); this.cfg = cfg; }
        void attach(Frame f) { if (!frames.contains(f)) frames.add(f); }
        void detach(Frame f) { frames.remove(f); }
        public int frameCount() { return frames.size(); }
        public boolean ready() { return blur != null && pendingKey == null; }
        public void invalidateFrames() { for (int i = 0; i < frames.size(); i++) frames.get(i).invalidate(); }
        /** Gọi khi nội dung cuộn: gộp lại, tối đa 1 lần mỗi khung hình */
        public void onScroll() { if (!scrollPending) { scrollPending = true; postOnAnimation(scrollTick); } }

        /** Dựng (bất đồng bộ) ảnh nền và bản thu nhỏ đã làm mờ khi chủ đề/kiểu nền/kích thước/độ mờ thay đổi */
        void ensure() {
            final int w = getWidth(), h = getHeight(); if (w < 4 || h < 4) return;
            final Theme th = cfg.th; final String style = "off".equals(cfg.mode) ? "plain" : cfg.bg;
            final int radius = Math.max(1, (int) Math.round(cfg.p.blur / SCALE));
            final String nk = w + "x" + h + "/" + th.id + "/" + style, bk = nk + "/" + radius;
            if (bk.equals(blurKey) || bk.equals(pendingKey)) return;
            pendingKey = bk; final int g = ++gen;
            final boolean reuse = nk.equals(nebKey) && small != null; final int[] small0 = small; final int sw0 = sw, sh0 = sh;
            WORK.execute(new Runnable() { public void run() {
                long t0 = System.nanoTime();
                Bitmap nFull = null; int[] sm = small0; int smw = sw0, smh = sh0;
                try {
                    if (!reuse) {
                        int hw = Math.max(2, w / 2), hh = Math.max(2, h / 2);
                        int[] px = Glass.nebula(hw, hh, th, style, 20261116);
                        nFull = Bitmap.createBitmap(px, hw, hh, Bitmap.Config.ARGB_8888);
                        smw = Math.max(2, w / SCALE); smh = Math.max(2, h / SCALE);
                        sm = downsample(px, hw, hh, smw, smh);
                    }
                    final int[] bl = sm.clone(); Glass.boxBlur(bl, smw, smh, radius);
                    final Bitmap nBlur = Bitmap.createBitmap(bl, smw, smh, Bitmap.Config.ARGB_8888);
                    final Bitmap fFull = nFull; final int[] fSm = sm; final int fw = smw, fh = smh;
                    lastBgMs = (System.nanoTime() - t0) / 1000000;
                    post(new Runnable() { public void run() {
                        if (g != gen) return;   // đã có yêu cầu mới hơn
                        if (fFull != null) { full = fFull; nebKey = nk; small = fSm; sw = fw; sh = fh; }
                        blur = bl; bw = fw; bh = fh; blurBmp = nBlur; blurKey = bk; pendingKey = null;
                        invalidate(); invalidateFrames();
                    } });
                } catch (Throwable e) { post(new Runnable() { public void run() { if (g == gen) pendingKey = null; } }); }
            } });
        }
        /** Thu nhỏ bằng trung bình khối (nhanh, không cần Bitmap/Canvas ở luồng nền) */
        static int[] downsample(int[] px, int w, int h, int ow, int oh) {
            int[] o = new int[ow * oh];
            for (int y = 0; y < oh; y++) {
                int y0 = y * h / oh, y1 = Math.max(y0 + 1, (y + 1) * h / oh);
                for (int x = 0; x < ow; x++) {
                    int x0 = x * w / ow, x1 = Math.max(x0 + 1, (x + 1) * w / ow); long r = 0, g = 0, b = 0; int n = 0;
                    for (int yy = y0; yy < y1 && yy < h; yy++) for (int xx = x0; xx < x1 && xx < w; xx++) { int c = px[yy * w + xx]; r += (c >> 16) & 255; g += (c >> 8) & 255; b += c & 255; n++; }
                    if (n == 0) n = 1; o[y * ow + x] = 0xFF000000 | ((int) (r / n) << 16) | ((int) (g / n) << 8) | (int) (b / n);
                }
            }
            return o;
        }
        @Override protected void onSizeChanged(int w, int h, int ow, int oh) { super.onSizeChanged(w, h, ow, oh); post(new Runnable() { public void run() { ensure(); } }); }
        @Override protected void onDraw(Canvas c) {
            ensure();
            if (full == null) { c.drawColor(cfg.th.bg); return; }
            dst.set(0, 0, getWidth(), getHeight()); c.drawBitmap(full, null, dst, paint);
        }
    }

    /** Khung kính. kind: 0 = thẻ (kính mờ), 1 = thanh/hộp thoại (khúc xạ thật nếu chế độ liquid), 2 = khối màu đặc (thông báo) */
    public static class Frame extends LinearLayout {
        final Bg bg; final Cfg cfg; final int kind; float radius; final float dens;
        Bitmap band; String bandKey, wantKey;
        BitmapShader shader; Bitmap shaderSrc; LinearGradient grad; String gradKey;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG), stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int[] loc = new int[2], bgLoc = new int[2]; final RectF r = new RectF(); final Matrix m = new Matrix();
        int lastOx = Integer.MIN_VALUE, lastOy = Integer.MIN_VALUE;
        public int solidColor; public boolean selected;
        public Frame(Context c, Bg bg, Cfg cfg, int kind, float radiusDp) {
            super(c); this.bg = bg; this.cfg = cfg; this.kind = kind; dens = c.getResources().getDisplayMetrics().density; radius = radiusDp * dens;
            setWillNotDraw(false); setOrientation(VERTICAL);
            if (kind != 0) {
                setOutlineProvider(new ViewOutlineProvider() { @Override public void getOutline(View v, Outline o) { o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius); o.setAlpha(0.55f); } });
                setElevation((kind == 1 ? 3 : 2) * dens);
            }
        }
        @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); bg.attach(this); }
        @Override protected void onDetachedFromWindow() { bg.detach(this); super.onDetachedFromWindow(); }
        public void setRadius(float dp) { radius = dp * dens; if (kind != 0) invalidateOutline(); invalidate(); }
        public boolean hasBand() { return band != null && bandKey != null && bandKey.equals(wantKey); }
        boolean glassOn() { return kind != 2 && !"off".equals(cfg.mode) && (kind != 0 || cfg.cards); }
        /** Vị trí so với nền có đổi không (chỉ khi đổi mới phải vẽ lại phần nền kính) */
        boolean moved() {
            if (getWidth() <= 0 || !isShown()) return false;
            getLocationInWindow(loc); bg.getLocationInWindow(bgLoc);
            int ox = loc[0] - bgLoc[0], oy = loc[1] - bgLoc[1];
            if (oy + getHeight() < 0 || oy > bg.getHeight()) { lastOx = Integer.MIN_VALUE; return false; }   // ngoài màn hình
            return ox != lastOx || oy != lastOy;
        }
        void ensureShader() {
            if (shader == null || shaderSrc != bg.blurBmp) { shaderSrc = bg.blurBmp; shader = new BitmapShader(shaderSrc, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP); }
        }
        @Override protected void onDraw(Canvas c) {
            int w = getWidth(), h = getHeight(); if (w <= 0 || h <= 0) return;
            r.set(0, 0, w, h);
            Theme th = cfg.th;
            int base = solidColor != 0 ? solidColor : (kind == 1 ? th.bg2 : th.card);
            if (!glassOn() || bg.blurBmp == null) {
                p.setShader(null); p.setColorFilter(null); p.setColor(base); c.drawRoundRect(r, radius, radius, p);
                if (kind != 2) {
                    stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(dens); stroke.setShader(null); stroke.setColor(selected ? th.gold : th.line);
                    r.inset(dens / 2, dens / 2); c.drawRoundRect(r, radius, radius, stroke);
                }
                return;
            }
            getLocationInWindow(loc); bg.getLocationInWindow(bgLoc);
            int ox = loc[0] - bgLoc[0], oy = loc[1] - bgLoc[1]; lastOx = ox; lastOy = oy;
            boolean liquid = kind == 1 && "liquid".equals(cfg.mode) && (long) w * h <= 4000000L;
            // lõi kính: nền đã làm mờ đúng vị trí màn hình (GPU)
            ensureShader();
            m.reset(); m.setScale(Bg.SCALE, Bg.SCALE); m.postTranslate(-ox, -oy); shader.setLocalMatrix(m);
            p.setShader(shader); p.setColorFilter(liquid ? cfg.sat() : null);
            if (liquid) r.inset(1, 1);
            c.drawRoundRect(r, radius, radius, p); p.setShader(null); p.setColorFilter(null);
            double ta = liquid ? cfg.p.tint : Math.min(0.92, cfg.p.tint + 0.12);
            p.setColor(Theme.alpha(base, (int) Math.round(255 * ta))); c.drawRoundRect(r, radius, radius, p);
            r.set(0, 0, w, h);
            boolean drawnBand = false;
            if (liquid) {
                String key = w + "x" + h + "@" + ox + "," + oy + "/" + cfg.version + "/" + cfg.p.key() + "/" + base + "/" + radius + "/" + bg.blurKey;
                if (key.equals(bandKey) && band != null) { c.drawBitmap(band, 0, 0, null); drawnBand = true; }
                else requestBand(key, w, h, ox, oy, base);
            }
            if (!drawnBand) {
                // viền phản xạ: sáng ở cạnh trên-trái, mờ dần (giữ gradient giữa các lần vẽ)
                float a = (float) cfg.p.specular;
                String gk = w + "x" + h + "/" + a;
                if (!gk.equals(gradKey)) { grad = new LinearGradient(0, 0, w * 0.6f, h, Theme.alpha(0xFFFFFFFF, (int) (230 * a)), Theme.alpha(0xFFFFFFFF, (int) (40 * a)), Shader.TileMode.CLAMP); gradKey = gk; }
                stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(Math.max(1f, dens)); stroke.setShader(grad);
                r.inset(dens / 2, dens / 2); c.drawRoundRect(r, radius, radius, stroke); stroke.setShader(null);
            }
            if (selected) { stroke.setStyle(Paint.Style.STROKE); stroke.setStrokeWidth(2 * dens); stroke.setColor(th.gold); r.set(dens, dens, w - dens, h - dens); c.drawRoundRect(r, radius, radius, stroke); }
        }
        /** Tính dải viền khúc xạ ở luồng nền rồi vẽ lại */
        void requestBand(final String key, final int w, final int h, final int ox, final int oy, final int base) {
            if (key.equals(wantKey)) return;
            wantKey = key;
            final int[] bl = bg.blur; final int bw = bg.bw, bh = bg.bh; final int rad = Math.round(radius);
            final Glass.Params pp = new Glass.Params();
            pp.blur = cfg.p.blur; pp.refraction = cfg.p.refraction; pp.bezel = cfg.p.bezel; pp.thickness = cfg.p.thickness; pp.ior = cfg.p.ior; pp.tint = cfg.p.tint; pp.specular = cfg.p.specular; pp.saturation = cfg.p.saturation;
            WORK.execute(new Runnable() { public void run() {
                if (!key.equals(wantKey)) return;   // đã có yêu cầu mới hơn (cửa sổ đổi cỡ/di chuyển)
                long t0 = System.nanoTime();
                try {
                    Glass.Maps mp = Glass.maps(w, h, rad, pp);
                    int[] buf = new int[w * h];
                    Glass.renderBand(bl, bw, bh, Bg.SCALE, ox, oy, mp, pp, base, buf);
                    final Bitmap b = Bitmap.createBitmap(buf, w, h, Bitmap.Config.ARGB_8888);
                    lastBandMs = (System.nanoTime() - t0) / 1000000; bandCount++;
                    post(new Runnable() { public void run() { if (key.equals(wantKey)) { band = b; bandKey = key; invalidate(); } } });
                } catch (Throwable e) { post(new Runnable() { public void run() { if (key.equals(wantKey)) wantKey = null; } }); }
            } });
        }
    }
}
