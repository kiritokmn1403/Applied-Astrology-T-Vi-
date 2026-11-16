package com.appliedastrology.tuvi.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Applied Astrology v0.0.4 – Liquid Glass native (dùng chung Android & Windows, bản C# là bản dịch 1:1).
 * Tích hợp từ 4 project trong mục "Liquid Glass" của sheet Project GitHub:
 *  - archisvaze/liquid-glass: hồ sơ khúc xạ vật lý (định luật Snell, IOR, độ dày kính, bề rộng viền vát) → bản đồ dịch chuyển
 *    + bản đồ phản xạ (specular) theo góc chiếu sáng; mặt cong "convex squircle".
 *  - rdev/liquid-glass-react: các tham số displacementScale / blurAmount / saturation / aberration và viền sáng theo chế độ.
 *  - dashersw/liquid-glass-js: kính dạng thanh/nút/khối bo tròn, bóng đổ ngoài 0 25 50 rgba(0,0,0,.25).
 *  - gist leiniaozl229 (React Three Fiber/GLSL): mặt nạ SDF hộp bo góc + smoothstep, làm mờ lấy mẫu theo vòng, viền sáng rim.
 * Khác bản web: không cần backdrop-filter/SVG/WebGL nên chạy được cả Android 6.0 và Windows 7; nền phía sau là ảnh nền do
 * ứng dụng tự vẽ (tinh vân), được làm mờ một lần rồi khúc xạ từng điểm ảnh trong vùng viền vát.
 */
public final class Glass {
    private Glass() { }

    public static final class Params {
        public double blur = 18;          // bán kính mờ (px, trên ảnh gốc)
        public double refraction = 0.6;   // tỉ lệ dịch chuyển (0..1)
        public double bezel = 18;         // bề rộng viền vát (px)
        public double thickness = 40;     // độ dày kính
        public double ior = 1.5;          // chiết suất
        public double tint = 0.35;        // độ phủ màu (0..1)
        public double specular = 0.55;    // độ sáng viền phản xạ (0..1)
        public double saturation = 1.35;  // tăng bão hòa nền sau kính
        public String key() { return blur + "/" + refraction + "/" + bezel + "/" + thickness + "/" + ior + "/" + tint + "/" + specular + "/" + saturation; }
    }

    /** Mặt cong lồi kiểu squircle (archisvaze SURFACE_FNS.convex_squircle) */
    static double surface(double x) { return Math.pow(1 - Math.pow(1 - x, 4), 0.25); }

    /** Hồ sơ dịch chuyển theo khoảng cách tới mép (calculateRefractionProfile) */
    public static double[] profile(double thickness, double bezel, double ior, int samples) {
        double eta = 1 / ior; double[] prof = new double[samples];
        for (int i = 0; i < samples; i++) {
            double x = (double) i / samples, y = surface(x), dx = x < 1 ? 0.0001 : -0.0001;
            double deriv = (surface(x + dx) - y) / dx, mag = Math.sqrt(deriv * deriv + 1);
            double nx = -deriv / mag, ny = -1 / mag, dot = ny, k = 1 - eta * eta * (1 - dot * dot);
            if (k < 0) { prof[i] = 0; continue; }
            double sq = Math.sqrt(k), rx = -(eta * dot + sq) * nx, ry = eta - (eta * dot + sq) * ny;
            prof[i] = rx * ((y * bezel + thickness) / ry);
        }
        return prof;
    }

    /** Bản đồ dịch chuyển (dx, dy ∈ [-1,1]), mặt nạ bo góc khử răng cưa, và viền phản xạ */
    public static final class Maps {
        public final int w, h, radius; public final float[] dx, dy, mask, spec; public double maxDisp;
        Maps(int w, int h, int r) { this.w = w; this.h = h; radius = r; dx = new float[w * h]; dy = new float[w * h]; mask = new float[w * h]; spec = new float[w * h]; }
    }
    private static final Map<String, Maps> CACHE = new LinkedHashMap<String, Maps>(16, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Maps> e) { return size() > 24; }
    };

    public static synchronized Maps maps(int w, int h, int radius, Params p) {
        String key = w + "x" + h + "r" + radius + "/" + p.bezel + "/" + p.thickness + "/" + p.ior;
        Maps m = CACHE.get(key);
        if (m != null) return m;
        int r = Math.max(1, Math.min(radius, Math.min(w, h) / 2));
        double bezel = Math.max(1, Math.min(p.bezel, Math.min(w, h) / 2.0 - 1));
        double[] prof = profile(p.thickness, bezel, p.ior, 128);
        double maxDisp = 0; for (double v : prof) maxDisp = Math.max(maxDisp, Math.abs(v)); if (maxDisp == 0) maxDisp = 1;
        m = new Maps(w, h, r); m.maxDisp = maxDisp;
        double angle = Math.PI / 3, sx = Math.cos(angle), sy = Math.sin(angle);
        int S = prof.length;
        for (int y1 = 0; y1 < h; y1++) {
            for (int x1 = 0; x1 < w; x1++) {
                int i = y1 * w + x1;
                // SDF hộp bo góc (gist GLSL): khoảng cách tới mép, âm ở trong
                double qx = Math.abs(x1 + 0.5 - w / 2.0) - (w / 2.0 - r), qy = Math.abs(y1 + 0.5 - h / 2.0) - (h / 2.0 - r);
                double ox = Math.max(qx, 0), oy = Math.max(qy, 0);
                double sd = Math.sqrt(ox * ox + oy * oy) + Math.min(Math.max(qx, qy), 0) - r;
                double a = sd >= 0.5 ? 0 : sd <= -0.5 ? 1 : 0.5 - sd;   // smoothstep 1px
                m.mask[i] = (float) a;
                if (a <= 0) continue;
                double fromSide = -sd;                                    // khoảng cách vào trong tính từ mép
                if (fromSide >= bezel) continue;                          // phần phẳng giữa tấm kính: không khúc xạ
                // pháp tuyến: gradient của SDF
                double gx, gy;
                if (qx > 0 && qy > 0) { double l = Math.sqrt(qx * qx + qy * qy); gx = qx / l; gy = qy / l; }
                else if (qx > qy) { gx = 1; gy = 0; } else { gx = 0; gy = 1; }
                if (x1 + 0.5 < w / 2.0) gx = -gx; if (y1 + 0.5 < h / 2.0) gy = -gy;
                int bi = Math.min((int) ((fromSide / bezel) * S), S - 1); if (bi < 0) bi = 0;
                double disp = prof[bi] / maxDisp;
                m.dx[i] = (float) (-gx * disp * a); m.dy[i] = (float) (-gy * disp * a);
                // viền phản xạ (generateSpecularMap): mạnh ở mép hướng về nguồn sáng
                // archisvaze: edge = sqrt(1-(1-fromSide)^2) → chỉ 1–2 px sát mép sáng; thêm lớp ánh vát mềm theo hướng sáng
                double dot = Math.abs(gx * sx - gy * sy);
                double rim = Math.exp(-fromSide / 1.4), bevel = Math.exp(-fromSide / (bezel * 0.45)) * 0.28;
                m.spec[i] = (float) (Math.min(1, Math.pow(dot, 1.3) * (rim + bevel)) * a);
            }
        }
        CACHE.put(key, m);
        return m;
    }

    /** Làm mờ hộp 3 lượt (xấp xỉ Gauss) trên ảnh ARGB */
    public static void boxBlur(int[] px, int w, int h, int radius) {
        if (radius < 1) return;
        int[] tmp = new int[px.length];
        for (int pass = 0; pass < 3; pass++) { blurH(px, tmp, w, h, radius); blurH(transpose(tmp, w, h), px, h, w, radius); int[] t2 = transpose(px, h, w); System.arraycopy(t2, 0, px, 0, px.length); }
    }
    private static int[] transpose(int[] a, int w, int h) { int[] o = new int[a.length]; for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) o[x * h + y] = a[y * w + x]; return o; }
    private static void blurH(int[] in, int[] out, int w, int h, int r) {
        int div = r * 2 + 1;
        for (int y = 0; y < h; y++) {
            int row = y * w, sr = 0, sg = 0, sb = 0;
            for (int k = -r; k <= r; k++) { int c = in[row + Math.min(w - 1, Math.max(0, k))]; sr += (c >> 16) & 255; sg += (c >> 8) & 255; sb += c & 255; }
            for (int x = 0; x < w; x++) {
                out[row + x] = 0xFF000000 | ((sr / div) << 16) | ((sg / div) << 8) | (sb / div);
                int cOut = in[row + Math.max(0, x - r)], cIn = in[row + Math.min(w - 1, x + r + 1)];
                sr += ((cIn >> 16) & 255) - ((cOut >> 16) & 255); sg += ((cIn >> 8) & 255) - ((cOut >> 8) & 255); sb += (cIn & 255) - (cOut & 255);
            }
        }
    }

    /**
     * Dựng tấm kính: lấy mẫu ảnh nền đã làm mờ (bw×bh, thu nhỏ theo hệ số scale so với màn hình) tại vị trí (ox, oy) của tấm kính,
     * dịch chuyển theo bản đồ khúc xạ, tăng bão hòa, phủ màu, cộng viền phản xạ. Kết quả ARGB w×h (ngoài bo góc trong suốt).
     */
    public static void render(int[] blurred, int bw, int bh, double scale, int ox, int oy, Maps m, Params p, int tint, int[] out) {
        double amp = m.maxDisp * p.refraction;
        int tr = (tint >> 16) & 255, tg = (tint >> 8) & 255, tb = tint & 255; double ta = p.tint, sat = p.saturation;
        for (int y = 0; y < m.h; y++) {
            for (int x = 0; x < m.w; x++) {
                int i = y * m.w + x; float a = m.mask[i];
                if (a <= 0) { out[i] = 0; continue; }
                double sxp = (ox + x + m.dx[i] * amp) / scale, syp = (oy + y + m.dy[i] * amp) / scale;
                int ix = (int) sxp, iy = (int) syp;
                if (ix < 0) ix = 0; else if (ix >= bw) ix = bw - 1; if (iy < 0) iy = 0; else if (iy >= bh) iy = bh - 1;
                int c = blurred[iy * bw + ix];
                double r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255, l = 0.2126 * r + 0.7152 * g + 0.0722 * b;
                r = l + (r - l) * sat; g = l + (g - l) * sat; b = l + (b - l) * sat;
                r = r * (1 - ta) + tr * ta; g = g * (1 - ta) + tg * ta; b = b * (1 - ta) + tb * ta;
                double s = m.spec[i] * p.specular;
                r += (255 - r) * s; g += (255 - g) * s; b += (255 - b) * s;
                int A = (int) (a * 255 + 0.5);
                out[i] = (A << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
            }
        }
    }
    static int clamp(double v) { return v < 0 ? 0 : v > 255 ? 255 : (int) v; }

    /** Ảnh nền tinh vân (để kính có gì khúc xạ): gradient sâu + các đám tinh vân màu chủ đề + sao. ARGB w×h. */
    public static int[] nebula(int w, int h, Theme th, String style, long seed) {
        int[] px = new int[w * h];
        int base = th.bg, c1 = th.purple, c2 = th.gold, c3 = th.accent;
        if ("plain".equals(style)) { for (int i = 0; i < px.length; i++) px[i] = Theme.mix(th.bg, th.bg2, (double) (i / w) / h); return px; }
        boolean aurora = "aurora".equals(style);
        double[][] blobs = aurora
            ? new double[][]{{0.15, 0.25, 0.55, 0}, {0.85, 0.15, 0.45, 2}, {0.5, 0.75, 0.6, 1}, {0.1, 0.9, 0.4, 2}}
            : new double[][]{{0.18, 0.18, 0.42, 0}, {0.82, 0.32, 0.38, 1}, {0.35, 0.82, 0.46, 2}, {0.9, 0.9, 0.3, 0}};
        int[] cols = {c1, c2, c3};
        double inten = th.dark ? 0.55 : 0.35;
        for (int y = 0; y < h; y++) {
            double fy = (double) y / h;
            for (int x = 0; x < w; x++) {
                double fx = (double) x / w;
                double r = (base >> 16) & 255, g = (base >> 8) & 255, b = base & 255;
                double grad = fy * 0.25; int b2 = th.bg2; r = r * (1 - grad) + ((b2 >> 16) & 255) * grad; g = g * (1 - grad) + ((b2 >> 8) & 255) * grad; b = b * (1 - grad) + (b2 & 255) * grad;
                for (double[] bl : blobs) {
                    double ddx = (fx - bl[0]) * (w > h ? (double) w / h : 1), ddy = (fy - bl[1]) * (h > w ? (double) h / w : 1);
                    double d2 = (ddx * ddx + ddy * ddy) / (bl[2] * bl[2]); if (d2 > 1.6) continue;
                    double k = Math.exp(-d2 * 2.2) * inten; int cc = cols[(int) bl[3]];
                    r += (((cc >> 16) & 255) - r) * k; g += (((cc >> 8) & 255) - g) * k; b += ((cc & 255) - b) * k;
                }
                px[y * w + x] = 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
            }
        }
        // sao
        long s = seed; int n = w * h / 2600;
        for (int k = 0; k < n; k++) {
            s = (s * 6364136223846793005L + 1442695040888963407L); int x = (int) ((s >>> 33) % w);
            s = (s * 6364136223846793005L + 1442695040888963407L); int y = (int) ((s >>> 33) % h);
            s = (s * 6364136223846793005L + 1442695040888963407L); int br = 120 + (int) ((s >>> 33) % 135);
            int c = th.dark ? (0xFF000000 | (br << 16) | (br << 8) | Math.min(255, br + 20)) : Theme.mix(th.bg, th.gold, br / 400.0);
            px[y * w + x] = c; if (br > 230 && x + 1 < w && y + 1 < h) { px[y * w + x + 1] = c; px[(y + 1) * w + x] = c; }
        }
        return px;
    }
}
