package com.appliedastrology.tuvi.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applied Astrology v0.0.4 – Engine an sao Tử Vi theo phương pháp Bửu Đình (native Java).
 * Chuyển 1:1 từ tuvi_engine.js (v0.0.2–0.0.3, đã đối chiếu 60 mẫu lá số Can Chi trong SWF gốc).
 * Chỉ số địa chi: 0=Tí 1=Sửu 2=Dần ... 11=Hợi
 */
public final class Engine {
    private Engine() { }
    public static final String[] CAN = Lunar.CAN, CHI = Lunar.CHI;
    static final String[] CHI_ELEMENT = {"Thủy", "Thổ", "Mộc", "Mộc", "Thổ", "Hỏa", "Hỏa", "Thổ", "Kim", "Kim", "Thổ", "Thủy"};
    static final String[] NAP_AM = {"Hải Trung Kim", "Lư Trung Hỏa", "Đại Lâm Mộc", "Lộ Bàng Thổ", "Kiếm Phong Kim", "Sơn Đầu Hỏa",
        "Giản Hạ Thủy", "Thành Đầu Thổ", "Bạch Lạp Kim", "Dương Liễu Mộc", "Tuyền Trung Thủy", "Ốc Thượng Thổ",
        "Tích Lịch Hỏa", "Tùng Bách Mộc", "Trường Lưu Thủy", "Sa Trung Kim", "Sơn Hạ Hỏa", "Bình Địa Mộc",
        "Bích Thượng Thổ", "Kim Bạch Kim", "Phú Đăng Hỏa", "Thiên Hà Thủy", "Đại Trạch Thổ", "Thoa Xuyến Kim",
        "Tang Đố Mộc", "Đại Khê Thủy", "Sa Trung Thổ", "Thiên Thượng Hỏa", "Thạch Lựu Mộc", "Đại Hải Thủy"};
    static final String[] ELEMENTS = {"Kim", "Mộc", "Thủy", "Hỏa", "Thổ"};
    public static final String[] PALACES = {"Mệnh", "Phụ Mẫu", "Phúc Đức", "Điền Trạch", "Quan Lộc", "Nô Bộc",
        "Thiên Di", "Tật Ách", "Tài Bạch", "Tử Tức", "Phu Thê", "Huynh Đệ"};
    public static final String[] HOA = {"Hóa Lộc", "Hóa Quyền", "Hóa Khoa", "Hóa Kỵ"};

    static final Map<String, String[]> BRIGHT = new HashMap<String, String[]>();
    static final Map<String, String> STAR_ELEMENT = new HashMap<String, String>();
    static final Map<String, String> KIND = new HashMap<String, String>();
    static {
        String[][] b = {
            {"Tử Vi", "B Đ M B V M M Đ M B V B"}, {"Liêm Trinh", "V Đ V H M H V Đ V H M H"}, {"Thiên Đồng", "V H M Đ H Đ H H M H H Đ"},
            {"Vũ Khúc", "V M V Đ M H V M V Đ M H"}, {"Thái Dương", "H Đ V V V M M Đ H H H H"}, {"Thiên Cơ", "Đ Đ H M M V Đ Đ V M M H"},
            {"Thiên Phủ", "M B M B V Đ M Đ M B V Đ"}, {"Thái Âm", "V Đ H H H H H Đ V M M M"}, {"Tham Lang", "H M Đ H V H H M Đ H V H"},
            {"Cự Môn", "V H V M H H V H Đ M H Đ"}, {"Thiên Tướng", "V Đ M H V Đ V Đ M H V Đ"}, {"Thiên Lương", "V Đ V V M H M Đ V H M H"},
            {"Thất Sát", "M Đ M H H V M Đ M H H V"}, {"Phá Quân", "M V H H Đ H M V H H Đ H"}};
        for (String[] x : b) BRIGHT.put(x[0], x[1].split(" "));
        String[] se = {"Tử Vi", "Thổ", "Liêm Trinh", "Hỏa", "Thiên Đồng", "Thủy", "Vũ Khúc", "Kim", "Thái Dương", "Hỏa", "Thiên Cơ", "Mộc",
            "Thiên Phủ", "Thổ", "Thái Âm", "Thủy", "Tham Lang", "Thủy", "Cự Môn", "Thủy", "Thiên Tướng", "Thủy", "Thiên Lương", "Mộc",
            "Thất Sát", "Kim", "Phá Quân", "Thủy", "Tả Phù", "Thổ", "Hữu Bật", "Thủy", "Văn Xương", "Kim", "Văn Khúc", "Thủy",
            "Lộc Tồn", "Thổ", "Kình Dương", "Kim", "Đà La", "Kim", "Hỏa Tinh", "Hỏa", "Linh Tinh", "Hỏa", "Địa Không", "Hỏa",
            "Địa Kiếp", "Hỏa", "Thiên Khôi", "Hỏa", "Thiên Việt", "Hỏa", "Thiên Mã", "Hỏa", "Hóa Lộc", "Mộc", "Hóa Quyền", "Mộc",
            "Hóa Khoa", "Thủy", "Hóa Kỵ", "Thủy", "Đào Hoa", "Mộc", "Hồng Loan", "Thủy", "Thiên Hỉ", "Thủy", "Thái Tuế", "Hỏa",
            "Bạch Hổ", "Kim", "Tang Môn", "Mộc", "Thiên Hình", "Hỏa", "Thiên Diêu", "Thủy", "Tam Thai", "Thổ", "Bát Tọa", "Thổ"};
        for (int i = 0; i < se.length; i += 2) STAR_ELEMENT.put(se[i], se[i + 1]);
        String good = "Tả Phù,Hữu Bật,Văn Xương,Văn Khúc,Thiên Khôi,Thiên Việt,Lộc Tồn,Thiên Mã,Hóa Lộc,Hóa Quyền,Hóa Khoa,Thiên Y,Tam Thai,Bát Tọa,Ân Quang,"
            + "Thiên Quý,Thai Phụ,Phong Cáo,Long Trì,Phượng Các,Giải Thần,Thiên Đức,Nguyệt Đức,Hồng Loan,Thiên Hỉ,Hoa Cái,Quốc Ấn,Đường Phù,Thiên Quan,"
            + "Thiên Phúc,Thiên Giải,Địa Giải,Thiên Trù";
        String bad = "Hóa Kỵ,Kình Dương,Đà La,Hỏa Tinh,Linh Tinh,Địa Không,Địa Kiếp,Thiên Hình,Lưu Hà,Kiếp Sát,Phá Toái,Cô Thần,Quả Tú,Thiên Khốc,"
            + "Thiên Hư,Tang Môn,Bạch Hổ,Điếu Khách,Tuế Phá,Thiên La,Địa Võng,Thiên Thương,Thiên Sứ,Quan Phù,Bệnh Phù,Đại Hao,Tiểu Hao,Phục Binh,Quan Phủ,Thiên Không";
        String neutral = "Thiên Diêu,Đào Hoa,Thái Tuế,Phi Liêm";
        for (String s : good.split(",")) KIND.put(s, "good");
        for (String s : bad.split(",")) KIND.put(s, "bad");
        for (String s : neutral.split(",")) KIND.put(s, "neutral");
    }

    static final int[] LOC_TON = {2, 3, 5, 6, 5, 6, 8, 9, 11, 0};
    static final int[] KHOI = {1, 0, 11, 9, 7, 8, 7, 6, 5, 3};
    static final int[] VIET = {7, 8, 9, 11, 1, 0, 1, 2, 3, 5};
    static final int[] LUU_HA = {9, 10, 7, 8, 5, 6, 3, 4, 11, 2};
    static final int[] THIEN_QUAN = {7, 4, 5, 2, 3, 9, 11, 9, 10, 6};
    static final int[] THIEN_PHUC = {9, 8, 0, 11, 3, 2, 6, 5, 6, 5};
    static final int[] THIEN_TRU = {5, 6, 0, 5, 6, 8, 2, 6, 9, 10};
    static final String[][] TU_HOA = {
        {"Liêm Trinh", "Phá Quân", "Vũ Khúc", "Thái Dương"}, {"Thiên Cơ", "Thiên Lương", "Tử Vi", "Thái Âm"},
        {"Thiên Đồng", "Thiên Cơ", "Văn Xương", "Liêm Trinh"}, {"Thái Âm", "Thiên Đồng", "Thiên Cơ", "Cự Môn"},
        {"Tham Lang", "Thái Âm", "Hữu Bật", "Thiên Cơ"}, {"Vũ Khúc", "Tham Lang", "Thiên Lương", "Văn Khúc"},
        {"Thái Dương", "Vũ Khúc", "Thái Âm", "Thiên Đồng"}, {"Cự Môn", "Thái Dương", "Văn Khúc", "Văn Xương"},
        {"Thiên Lương", "Tử Vi", "Tả Phù", "Vũ Khúc"}, {"Phá Quân", "Cự Môn", "Thái Âm", "Tham Lang"}};
    static final String[] BAC_SI = {"Bác Sĩ", "Lực Sĩ", "Thanh Long", "Tiểu Hao", "Tướng Quân", "Tấu Thư", "Phi Liêm", "Hỉ Thần", "Bệnh Phù", "Đại Hao", "Phục Binh", "Quan Phủ"};
    static final String[] THAI_TUE = {"Thái Tuế", "Thiếu Dương", "Tang Môn", "Thiếu Âm", "Quan Phù", "Tử Phù", "Tuế Phá", "Long Đức", "Bạch Hổ", "Phúc Đức", "Điếu Khách", "Trực Phù"};
    static final String[] TRANG_SINH = {"Tràng Sinh", "Mộc Dục", "Quan Đới", "Lâm Quan", "Đế Vượng", "Suy", "Bệnh", "Tử", "Mộ", "Tuyệt", "Thai", "Dưỡng"};
    static int trangSinhStart(int cuc) { switch (cuc) { case 2: case 5: return 8; case 3: return 11; case 4: return 5; default: return 2; } }
    static int tamHopGroup(int chi) { return chi % 4; }
    static final int[] DAO_HOA = {9, 6, 3, 0}, KIEP_SAT = {5, 2, 11, 8}, THIEN_MA = {2, 11, 8, 5};
    static final int[] HOA_BASE = {2, 3, 1, 9}, LINH_BASE = {10, 10, 3, 10};
    static final int[] TIEU_HAN_START = {10, 7, 4, 1};
    static final String[] MENH_CHU = {"Tham Lang", "Cự Môn", "Lộc Tồn", "Văn Khúc", "Liêm Trinh", "Vũ Khúc", "Phá Quân", "Vũ Khúc", "Liêm Trinh", "Văn Khúc", "Lộc Tồn", "Cự Môn"};
    static final String[] THAN_CHU = {"Linh Tinh", "Thiên Tướng", "Thiên Lương", "Thiên Đồng", "Văn Xương", "Thiên Cơ", "Hỏa Tinh", "Thiên Tướng", "Thiên Lương", "Thiên Đồng", "Văn Xương", "Thiên Cơ"};

    public static int mod(int a, int n) { return ((a % n) + n) % n; }
    static int sexagenary(int can, int chi) { for (int i = 0; i < 60; i++) if (i % 10 == can && i % 12 == chi) return i; return 0; }
    public static String napAm(int can, int chi) { return NAP_AM[sexagenary(can, chi) / 2]; }
    public static String elementOf(String name) { for (String e : ELEMENTS) if (name.contains(e)) return e; return "Thổ"; }
    static int canOfDan(int canYear) { return new int[]{2, 4, 6, 8, 0}[canYear % 5]; }
    public static int cucOf(String element) {
        if (element.equals("Thủy")) return 2; if (element.equals("Mộc")) return 3; if (element.equals("Kim")) return 4; if (element.equals("Thổ")) return 5; return 6;
    }
    public static String cucName(int c) {
        switch (c) { case 2: return "Thủy Nhị Cục"; case 3: return "Mộc Tam Cục"; case 4: return "Kim Tứ Cục"; case 5: return "Thổ Ngũ Cục"; default: return "Hỏa Lục Cục"; }
    }
    public static String sinhKhac(String a, String b) {
        String[] e = {"Kim", "Thủy", "Mộc", "Hỏa", "Thổ"}; // vòng tương sinh
        if (a.equals(b)) return "hòa";
        int ia = idx(e, a), ib = idx(e, b);
        if ((ia + 1) % 5 == ib) return "sinh";
        if ((ia + 2) % 5 == ib) return "khắc";
        if ((ib + 1) % 5 == ia) return "được sinh";
        return "bị khắc";
    }
    static int idx(String[] a, String s) { for (int i = 0; i < a.length; i++) if (a[i].equals(s)) return i; return -1; }
    public static String kindOf(String star) { String k = KIND.get(star); return k == null ? "neutral" : k; }

    // ---------------- Mô hình dữ liệu ----------------
    public static final class Star {
        public final String name, element, kind; public String bright, attachedTo;
        Star(String n, String e, String k) { name = n; element = e == null ? "" : e; kind = k; }
    }
    public static final class Palace {
        public int chi, can; public String chiName, canChi, element, name; public boolean isMenh, isThan, tuan, triet;
        public final List<Star> major = new ArrayList<Star>(), good = new ArrayList<Star>(), bad = new ArrayList<Star>(), neutral = new ArrayList<Star>();
        public String ringTrangSinh, ringBacSi, ringThaiTue;
        public final List<String[]> hoa = new ArrayList<String[]>(); // {hóa, sao}
        public int daiHan; public String tieuHan = "";
        public final List<String> luu = new ArrayList<String>();
        List<Star> list(String kind) { return kind.equals("good") ? good : kind.equals("bad") ? bad : neutral; }
    }

    /** Thông tin đầu vào */
    public static final class Input {
        public String name = "Thân Chủ", gender = "Nam", calendar = "solar", leapMode = "same";
        public int day, month, year, hourChi, viewYear; public boolean leap;
    }

    public static final class Chart {
        public String name, gender; public int hourChi, calcMonth, viewYear;
        public int sDay, sMonth, sYear; public Lunar.LDate lunar;
        public int canYear, chiYear, menh, than, cuc, age, currentDaiHan = -1, currentTieuHan = -1;
        public String yearCanChi, monthCanChi, dayCanChi, hourCanChi, amDuong, napAm, banMenh, cucName, menhCuc, menhChu, thanChu, amDuongLy, viewYearCanChi;
        public boolean isDuongYear, thuan, vocd;
        public final Palace[] palaces = new Palace[12];

        public int where(String star) {
            for (int i = 0; i < 12; i++) {
                Palace p = palaces[i];
                for (Star s : p.major) if (s.name.equals(star)) return i;
                for (Star s : p.good) if (s.name.equals(star)) return i;
                for (Star s : p.bad) if (s.name.equals(star)) return i;
                for (Star s : p.neutral) if (s.name.equals(star)) return i;
            }
            return -1;
        }
        public int[] tamPhuong(int chi) { return new int[]{chi, mod(chi + 6, 12), mod(chi + 4, 12), mod(chi + 8, 12)}; }
        public List<Star> allStars(int chi) {
            Palace p = palaces[chi]; List<Star> r = new ArrayList<Star>(p.major); r.addAll(p.good); r.addAll(p.bad); r.addAll(p.neutral); return r;
        }
        Star add(int pos, String star) {
            Palace p = palaces[mod(pos, 12)]; String kind = kindOf(star);
            Star o = new Star(star, STAR_ELEMENT.get(star), kind); p.list(kind).add(o); return o;
        }

        /** Lưu niên: lưu Thái Tuế, Lộc Tồn, Kình, Đà, Thiên Mã, Tang Môn, Bạch Hổ, Tứ Hóa; đại hạn và tiểu hạn đang đi */
        public Chart applyYear(int year) {
            viewYear = year;
            int can = mod(year - 4, 10), chi = mod(year - 4, 12);
            for (int i = 0; i < 12; i++) palaces[i].luu.clear();
            int lt = LOC_TON[can];
            lu(chi, "L.Thái Tuế"); lu(lt, "L.Lộc Tồn"); lu(lt + 1, "L.Kình Dương"); lu(lt - 1, "L.Đà La");
            lu(THIEN_MA[tamHopGroup(chi)], "L.Thiên Mã"); lu(chi + 2, "L.Tang Môn"); lu(chi + 8, "L.Bạch Hổ");
            String[] th = TU_HOA[can];
            for (int i = 0; i < 4; i++) { int at = where(th[i]); if (at >= 0) lu(at, "L." + HOA[i]); }
            age = year - lunar.year + 1;
            currentDaiHan = -1; currentTieuHan = -1;
            for (int i = 0; i < 12; i++) {
                Palace p = palaces[i];
                if (age >= p.daiHan && age < p.daiHan + 10) currentDaiHan = i;
                if (p.tieuHan.equals(CHI[chi])) currentTieuHan = i;
            }
            viewYearCanChi = CAN[can] + " " + CHI[chi];
            return this;
        }
        private void lu(int pos, String n) { palaces[mod(pos, 12)].luu.add(n); }
    }

    /** Lập lá số. Ném IllegalArgumentException("INVALID_LUNAR_DATE") nếu ngày âm không tồn tại. */
    public static Chart build(Input in) {
        Chart c = new Chart();
        c.name = in.name == null || in.name.trim().isEmpty() ? "Thân Chủ" : in.name;
        c.gender = "Nữ".equals(in.gender) ? "Nữ" : "Nam";
        c.hourChi = mod(in.hourChi, 12);
        if ("lunar".equals(in.calendar)) {
            int[] s = Lunar.lunar2solar(in.day, in.month, in.year, in.leap);
            if (s == null) throw new IllegalArgumentException("INVALID_LUNAR_DATE");
            c.lunar = new Lunar.LDate(in.day, in.month, in.year, in.leap, Lunar.jdFromDate(s[0], s[1], s[2]));
            c.sDay = s[0]; c.sMonth = s[1]; c.sYear = s[2];
        } else {
            c.sDay = in.day; c.sMonth = in.month; c.sYear = in.year;
            c.lunar = Lunar.solar2lunar(in.day, in.month, in.year);
        }
        int m = c.lunar.month;
        if (c.lunar.leap && "next".equals(in.leapMode) && c.lunar.day > 15) m = m == 12 ? 1 : m + 1;
        c.calcMonth = m;
        c.viewYear = in.viewYear > 0 ? in.viewYear : Calendar.getInstance().get(Calendar.YEAR);
        buildStars(c);
        return c;
    }

    private static void buildStars(Chart c) {
        int y = c.lunar.year, d = c.lunar.day, m = c.calcMonth, h = c.hourChi, i;
        int canY = mod(y - 4, 10), chiY = mod(y - 4, 12);
        c.canYear = canY; c.chiYear = chiY;
        c.yearCanChi = CAN[canY] + " " + CHI[chiY];
        int monthCan = mod(canOfDan(canY) + (m - 1), 10), monthChi = mod(m + 1, 12);
        c.monthCanChi = CAN[monthCan] + " " + CHI[monthChi];
        int jd = Lunar.jdFromDate(c.sDay, c.sMonth, c.sYear); int[] dcc = Lunar.dayCanChi(jd);
        c.dayCanChi = CAN[dcc[0]] + " " + CHI[dcc[1]];
        int hourCan = mod(dcc[0] % 5 * 2 + h, 10);
        c.hourCanChi = CAN[hourCan] + " " + CHI[h];
        c.isDuongYear = canY % 2 == 0;
        c.amDuong = (c.isDuongYear ? "Dương " : "Âm ") + c.gender;
        c.thuan = (c.isDuongYear && c.gender.equals("Nam")) || (!c.isDuongYear && c.gender.equals("Nữ"));
        c.napAm = napAm(canY, chiY);
        c.banMenh = elementOf(c.napAm);
        c.menh = mod(2 + (m - 1) - h, 12);
        c.than = mod(2 + (m - 1) + h, 12);
        int dan = canOfDan(canY);
        for (i = 0; i < 12; i++) {
            Palace p = new Palace();
            p.chi = i; p.chiName = CHI[i]; p.can = mod(dan + mod(i - 2, 12), 10); p.canChi = CAN[p.can] + " " + CHI[i]; p.element = CHI_ELEMENT[i];
            p.name = PALACES[mod(i - c.menh, 12)]; p.isMenh = i == c.menh; p.isThan = i == c.than;
            c.palaces[i] = p;
        }
        Palace menhP = c.palaces[c.menh];
        c.cuc = cucOf(elementOf(napAm(menhP.can, menhP.chi)));
        c.cucName = cucName(c.cuc);
        c.menhCuc = sinhKhac(c.banMenh, elementOf(c.cucName));

        // Tử Vi
        int cuc = c.cuc, x = 0;
        while ((d + x) % cuc != 0) x++;
        int q = (d + x) / cuc, tuvi = 2 + (q - 1);
        tuvi = x % 2 == 1 ? tuvi - x : tuvi + x;
        tuvi = mod(tuvi, 12);
        int phu = mod(4 - tuvi, 12);
        Object[][] majors = {{"Tử Vi", 0, tuvi}, {"Thiên Cơ", -1, tuvi}, {"Thái Dương", -3, tuvi}, {"Vũ Khúc", -4, tuvi}, {"Thiên Đồng", -5, tuvi}, {"Liêm Trinh", -8, tuvi},
            {"Thiên Phủ", 0, phu}, {"Thái Âm", 1, phu}, {"Tham Lang", 2, phu}, {"Cự Môn", 3, phu}, {"Thiên Tướng", 4, phu}, {"Thiên Lương", 5, phu}, {"Thất Sát", 6, phu}, {"Phá Quân", 10, phu}};
        for (Object[] mj : majors) {
            int pos = mod((Integer) mj[2] + (Integer) mj[1], 12); String nm = (String) mj[0];
            Star s = new Star(nm, STAR_ELEMENT.get(nm), "major"); s.bright = BRIGHT.get(nm)[pos];
            c.palaces[pos].major.add(s);
        }
        // Theo Can năm
        int lt = LOC_TON[canY];
        c.add(lt, "Lộc Tồn"); c.add(lt + 1, "Kình Dương"); c.add(lt - 1, "Đà La");
        c.add(lt + 8, "Quốc Ấn"); c.add(lt + 5, "Đường Phù");
        c.add(KHOI[canY], "Thiên Khôi"); c.add(VIET[canY], "Thiên Việt");
        c.add(LUU_HA[canY], "Lưu Hà"); c.add(THIEN_QUAN[canY], "Thiên Quan"); c.add(THIEN_PHUC[canY], "Thiên Phúc"); c.add(THIEN_TRU[canY], "Thiên Trù");
        int dir = c.thuan ? 1 : -1;
        for (i = 0; i < 12; i++) c.palaces[mod(lt + dir * i, 12)].ringBacSi = BAC_SI[i];
        // Theo Chi năm
        for (i = 0; i < 12; i++) c.palaces[mod(chiY + i, 12)].ringThaiTue = THAI_TUE[i];
        c.add(chiY, "Thái Tuế"); c.add(chiY + 2, "Tang Môn"); c.add(chiY + 4, "Quan Phù"); c.add(chiY + 6, "Tuế Phá"); c.add(chiY + 8, "Bạch Hổ"); c.add(chiY + 10, "Điếu Khách");
        c.add(chiY + 1, "Thiên Không");
        c.add(4 + chiY, "Long Trì"); c.add(10 - chiY, "Phượng Các"); c.add(10 - chiY, "Giải Thần");
        c.add(6 - chiY, "Thiên Khốc"); c.add(6 + chiY, "Thiên Hư");
        c.add(3 - chiY, "Hồng Loan"); c.add(9 - chiY, "Thiên Hỉ");
        int g = tamHopGroup(chiY);
        c.add(DAO_HOA[g], "Đào Hoa"); c.add(KIEP_SAT[g], "Kiếp Sát"); c.add(THIEN_MA[g], "Thiên Mã"); c.add(THIEN_MA[g] + 2, "Hoa Cái");
        int co = new int[]{2, 2, 5, 5, 5, 8, 8, 8, 11, 11, 11, 2}[chiY];
        c.add(co, "Cô Thần"); c.add(co - 4, "Quả Tú");
        c.add(new int[]{5, 1, 9, 5, 1, 9, 5, 1, 9, 5, 1, 9}[chiY], "Phá Toái");
        c.add(9 + chiY, "Thiên Đức"); c.add(5 + chiY, "Nguyệt Đức");
        // Theo tháng
        int taPhu = mod(4 + (m - 1), 12), huuBat = mod(10 - (m - 1), 12);
        c.add(taPhu, "Tả Phù"); c.add(huuBat, "Hữu Bật");
        c.add(9 + (m - 1), "Thiên Hình"); c.add(1 + (m - 1), "Thiên Diêu"); c.add(1 + (m - 1), "Thiên Y");
        c.add(8 + (m - 1), "Thiên Giải"); c.add(7 + (m - 1), "Địa Giải");
        // Theo giờ
        int xuong = mod(10 - h, 12), khuc = mod(4 + h, 12);
        c.add(xuong, "Văn Xương"); c.add(khuc, "Văn Khúc");
        c.add(11 + h, "Địa Kiếp"); c.add(11 - h, "Địa Không");
        c.add(6 + h, "Thai Phụ"); c.add(2 + h, "Phong Cáo");
        int hb = HOA_BASE[g], lb = LINH_BASE[g];
        boolean hl = c.thuan;
        c.add(hl ? hb + h : hb - h, "Hỏa Tinh"); c.add(hl ? lb - h : lb + h, "Linh Tinh");
        // Theo ngày
        c.add(taPhu + (d - 1), "Tam Thai"); c.add(huuBat - (d - 1), "Bát Tọa");
        c.add(xuong + (d - 1) - 1, "Ân Quang"); c.add(khuc - (d - 1) + 1, "Thiên Quý");
        // Cố định / theo cung
        c.add(4, "Thiên La"); c.add(10, "Địa Võng");
        c.add(mod(c.menh + 5, 12), "Thiên Thương");
        c.add(mod(c.menh + 7, 12), "Thiên Sứ");
        // Tràng Sinh
        int ts = trangSinhStart(cuc);
        for (i = 0; i < 12; i++) c.palaces[mod(ts + dir * i, 12)].ringTrangSinh = TRANG_SINH[i];
        // Tứ Hóa
        String[] th = TU_HOA[canY];
        for (i = 0; i < 4; i++) {
            int at = c.where(th[i]);
            if (at >= 0) { Star s = c.add(at, HOA[i]); s.attachedTo = th[i]; c.palaces[at].hoa.add(new String[]{HOA[i], th[i]}); }
        }
        // Tuần, Triệt
        int tuanStart = mod(chiY - canY, 12);
        c.palaces[mod(tuanStart + 10, 12)].tuan = true; c.palaces[mod(tuanStart + 11, 12)].tuan = true;
        int[] triet = new int[][]{{8, 9}, {6, 7}, {4, 5}, {2, 3}, {0, 1}}[canY % 5];
        c.palaces[triet[0]].triet = true; c.palaces[triet[1]].triet = true;
        // Đại hạn, tiểu hạn
        for (i = 0; i < 12; i++) c.palaces[mod(c.menh + dir * i, 12)].daiHan = cuc + 10 * i;
        int thStart = TIEU_HAN_START[g], tdir = c.gender.equals("Nam") ? 1 : -1;
        for (i = 0; i < 12; i++) c.palaces[mod(thStart + tdir * i, 12)].tieuHan = CHI[mod(chiY + i, 12)];
        c.menhChu = MENH_CHU[c.menh];
        c.thanChu = THAN_CHU[chiY];
        c.amDuongLy = ((c.menh % 2 == 0) == c.isDuongYear) ? "Âm Dương thuận lý" : "Âm Dương nghịch lý";
        c.vocd = c.palaces[c.menh].major.isEmpty();
        c.applyYear(c.viewYear);
    }
}
