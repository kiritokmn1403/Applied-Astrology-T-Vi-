package com.appliedastrology.tuvi.core;

/** Bảng màu 4 giao diện (giữ nguyên màu của bản 0.0.3) + màu ngũ hành + 6 màu giấy lót nền của SWF gốc (Frame 3). */
public final class Theme {
    public final String id; public final boolean dark;
    public final int bg, bg2, card, card2, line, gold, gold2, text, muted, accent, purple, good, bad, sel, menh;
    private Theme(String id, boolean dark, int... c) {
        this.id = id; this.dark = dark;
        bg = c[0]; bg2 = c[1]; card = c[2]; card2 = c[3]; line = c[4]; gold = c[5]; gold2 = c[6]; text = c[7]; muted = c[8];
        accent = c[9]; purple = c[10]; good = c[11]; bad = c[12]; sel = c[13]; menh = c[14];
    }
    static int h(int rgb) { return 0xFF000000 | rgb; }
    public static final Theme CELESTIAL = new Theme("celestial-dark", true, h(0x080b16), h(0x0e1428), h(0x141b33), h(0x1b2442), h(0x2c3763), h(0xf2b84b), h(0xffe7a3),
        h(0xeef2fb), h(0x9aa6c4), h(0x7dd3fc), h(0xa78bfa), h(0x34d399), h(0xfb7185), h(0x2a2160), h(0xf2b84b));
    public static final Theme GOLD = new Theme("royal-gold", true, h(0x140f06), h(0x21190a), h(0x2a2010), h(0x372a14), h(0x5c4720), h(0xffcf4d), h(0xfff1c2),
        h(0xfff8e6), h(0xd7b779), h(0xffd98a), h(0xe3b5ff), h(0x9be27a), h(0xff8a7a), h(0x4a3712), h(0xffcf4d));
    public static final Theme IVORY = new Theme("ivory-light", false, h(0xf6f3ec), h(0xece6d8), h(0xffffff), h(0xfaf7f0), h(0xd9cdb2), h(0x9a6a12), h(0x6b4708),
        h(0x1f2333), h(0x6b6f80), h(0x0e6fa8), h(0x6d4fc2), h(0x0f8a5f), h(0xc0283d), h(0xf3e6c4), h(0x9a6a12));
    public static final Theme RED = new Theme("imperial-red", true, h(0x170709), h(0x250b10), h(0x311017), h(0x40151e), h(0x6d2733), h(0xf6b24a), h(0xffe0a6),
        h(0xfff1f2), h(0xe7a3ad), h(0xffc58f), h(0xf0a8ff), h(0x7ee0a8), h(0xff7b8f), h(0x5a1a25), h(0xf6b24a));
    public static final Theme[] ALL = {CELESTIAL, GOLD, IVORY, RED};
    public static Theme byId(String id) { for (Theme t : ALL) if (t.id.equals(id)) return t; return CELESTIAL; }

    public int element(String e) {
        if (dark) {
            if ("Kim".equals(e)) return h(0xe5e7eb); if ("Mộc".equals(e)) return h(0x4ade80); if ("Thủy".equals(e)) return h(0x60a5fa); if ("Hỏa".equals(e)) return h(0xf87171); if ("Thổ".equals(e)) return h(0xfbbf24);
            return text;
        }
        if ("Kim".equals(e)) return h(0x6b7280); if ("Mộc".equals(e)) return h(0x15803d); if ("Thủy".equals(e)) return h(0x1d4ed8); if ("Hỏa".equals(e)) return h(0xb91c1c); if ("Thổ".equals(e)) return h(0xa16207);
        return text;
    }
    public int hoa(String n) {
        if ("Hóa Lộc".equals(n)) return good; if ("Hóa Quyền".equals(n)) return gold; if ("Hóa Khoa".equals(n)) return accent; if ("Hóa Kỵ".equals(n)) return bad; return text;
    }
    /** Giấy lót nền lá số: {nền, chữ} hoặc null */
    public static int[] paper(String id) {
        if ("vangkyxao".equals(id)) return new int[]{h(0xf3e2b0), h(0x3a2a08)};
        if ("vangchanh".equals(id)) return new int[]{h(0xf7f1a8), h(0x2f2a05)};
        if ("xanhnhat".equals(id)) return new int[]{h(0xd9ecf7), h(0x0d2a3a)};
        if ("lucnhat".equals(id)) return new int[]{h(0xdcefd8), h(0x12301a)};
        if ("timnhat".equals(id)) return new int[]{h(0xe8dcf3), h(0x2a1440)};
        if ("vang".equals(id)) return new int[]{h(0xf6d77a), h(0x3a2804)};
        return null;
    }
    public static final String[] PAPERS = {"none", "vangkyxao", "vangchanh", "xanhnhat", "lucnhat", "timnhat", "vang"};
    public static int alpha(int color, int a) { return (color & 0x00FFFFFF) | ((a & 0xFF) << 24); }
    public static int mix(int c1, int c2, double t) {
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t), g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t), b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
