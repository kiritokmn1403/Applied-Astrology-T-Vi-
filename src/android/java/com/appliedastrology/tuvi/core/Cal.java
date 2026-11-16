package com.appliedastrology.tuvi.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Applied Astrology v0.0.4 – Âm Lịch (lịch Âm Dương Việt Nam GMT+7): Can Chi, tiết khí, hoàng đạo/hắc đạo, 12 Trực,
 * giờ hoàng đạo, ngày lễ, Lục Thập Hoa Giáp & nạp âm (Frame 3 của SWF gốc). Native Java, chuyển từ calendar.js.
 * Tham khảo: hochanh/amlich, copthuy/amlich, wolfhong/LunarCalendar, XhinLiang/LunarCalendar, lwlsw/Chinese-Lunar-Calendar-ics.
 */
public final class Cal {
    private Cal() { }
    public static final String[] TIET = {"Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng", "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu",
        "Xử Thử", "Bạch Lộ", "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết", "Đông Chí", "Tiểu Hàn",
        "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập"};
    static final String[] GIO_HD = {"110100101100", "001101001011", "110011010010", "101100110100", "001011001101", "010010110011"};
    static final String[] THAN = {"Thanh Long", "Minh Đường", "Thiên Hình", "Chu Tước", "Kim Quỹ", "Kim Đường", "Bạch Hổ", "Ngọc Đường", "Thiên Lao", "Huyền Vũ", "Tư Mệnh", "Câu Trận"};
    static final int[] THAN_GOOD = {1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0};
    static final String[] TRUC = {"Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế"};
    static final Object[][] LUNAR_EVENTS = {{1, 1, "Tết Nguyên Đán"}, {2, 1, "Mùng 2 Tết"}, {3, 1, "Mùng 3 Tết"}, {15, 1, "Tết Nguyên Tiêu"}, {3, 3, "Tết Hàn Thực"},
        {10, 3, "Giỗ Tổ Hùng Vương"}, {15, 4, "Lễ Phật Đản"}, {5, 5, "Tết Đoan Ngọ"}, {15, 7, "Lễ Vu Lan"}, {15, 8, "Tết Trung Thu"},
        {9, 9, "Tết Trùng Cửu"}, {10, 10, "Tết Thường Tân"}, {15, 10, "Tết Hạ Nguyên"}, {23, 12, "Ông Công Ông Táo về trời"}};
    static final Object[][] SOLAR_EVENTS = {{1, 1, "Tết Dương lịch"}, {9, 1, "Ngày Học sinh – Sinh viên"}, {3, 2, "Thành lập Đảng CSVN"}, {14, 2, "Lễ Tình nhân"},
        {27, 2, "Ngày Thầy thuốc Việt Nam"}, {8, 3, "Quốc tế Phụ nữ"}, {26, 3, "Thành lập Đoàn TNCS HCM"}, {21, 4, "Ngày Sách Việt Nam"},
        {30, 4, "Ngày Giải phóng miền Nam"}, {1, 5, "Quốc tế Lao động"}, {7, 5, "Chiến thắng Điện Biên Phủ"}, {19, 5, "Ngày sinh Chủ tịch Hồ Chí Minh"},
        {1, 6, "Quốc tế Thiếu nhi"}, {21, 6, "Ngày Báo chí Việt Nam"}, {28, 6, "Ngày Gia đình Việt Nam"}, {27, 7, "Ngày Thương binh Liệt sĩ"},
        {19, 8, "Cách mạng tháng Tám"}, {2, 9, "Quốc khánh"}, {10, 10, "Giải phóng Thủ đô"}, {13, 10, "Ngày Doanh nhân Việt Nam"},
        {20, 10, "Ngày Phụ nữ Việt Nam"}, {20, 11, "Ngày Nhà giáo Việt Nam"}, {22, 12, "Thành lập QĐND Việt Nam"}, {24, 12, "Lễ Giáng sinh"}};

    public static final class Hour { public int chi; public String name, can, range; }
    public static final class Event { public boolean lunar; public String name; Event(boolean l, String n) { lunar = l; name = n; } }
    public static final class DayInfo {
        public int day, month, year, weekday, jd; public Lunar.LDate lunar;
        public String dayCC, monthCC, yearCC, napAmDay, napAmYear, tiet, than, truc, hourZero; public boolean tietStart, hoangDao, monthLeap;
        public final List<Hour> goodHours = new ArrayList<Hour>(), badHours = new ArrayList<Hour>();
        public final List<Event> events = new ArrayList<Event>();
    }

    static String pad(int n) { return n < 10 ? "0" + n : String.valueOf(n); }
    static String canChi(int c, int h) { return Lunar.CAN[c] + " " + Lunar.CHI[h]; }

    public static DayInfo dayInfo(int d, int m, int y) {
        DayInfo r = new DayInfo();
        int jd = Lunar.jdFromDate(d, m, y); Lunar.LDate lu = Lunar.solar2lunar(d, m, y);
        int dc = (jd + 9) % 10, dh = (jd + 1) % 12;
        int mc = (lu.year * 12 + lu.month + 3) % 10, mh = (lu.month + 1) % 12;
        int yc = (lu.year + 6) % 10, yh = (lu.year + 8) % 12;
        int tk = Lunar.tietKhiIndex(jd), tkPrev = Lunar.tietKhiIndex(jd - 1);
        double deg = Lunar.sunDegree(jd);
        int tietMonthChi = (2 + (int) Math.floor(((deg - 315 + 720) % 360) / 30)) % 12;
        int thanStart = new int[]{0, 2, 4, 6, 8, 10}[(lu.month - 1) % 6];
        int than = (dh - thanStart + 12) % 12;
        String hd = GIO_HD[dh % 6]; int hc0 = (dc % 5) * 2;
        for (int i = 0; i < 12; i++) {
            Hour h = new Hour(); h.chi = i; h.name = Lunar.CHI[i]; h.can = Lunar.CAN[(hc0 + i) % 10];
            h.range = pad((i * 2 + 23) % 24) + "–" + pad((i * 2 + 1) % 24);
            if (hd.charAt(i) == '1') r.goodHours.add(h); else r.badHours.add(h);
        }
        for (Object[] e : LUNAR_EVENTS) if (!lu.leap && (Integer) e[0] == lu.day && (Integer) e[1] == lu.month) r.events.add(new Event(true, (String) e[2]));
        if (!lu.leap && lu.month == 12 && lu.day >= 29) {
            int[] n = Lunar.jdToDate(jd + 1); Lunar.LDate nx = Lunar.solar2lunar(n[0], n[1], n[2]);
            if (nx.day == 1) r.events.add(new Event(true, "Giao thừa (Trừ tịch)"));
        }
        for (Object[] e : SOLAR_EVENTS) if ((Integer) e[0] == d && (Integer) e[1] == m) r.events.add(new Event(false, (String) e[2]));
        r.day = d; r.month = m; r.year = y; r.weekday = Lunar.weekday(d, m, y); r.jd = jd; r.lunar = lu;
        r.dayCC = canChi(dc, dh); r.monthCC = canChi(mc, mh); r.monthLeap = lu.leap; r.yearCC = canChi(yc, yh);
        r.napAmDay = Engine.napAm(dc, dh); r.napAmYear = Engine.napAm(yc, yh);
        r.tiet = TIET[tk]; r.tietStart = tk != tkPrev; r.than = THAN[than]; r.hoangDao = THAN_GOOD[than] == 1;
        r.truc = TRUC[(dh - tietMonthChi + 12) % 12]; r.hourZero = Lunar.CAN[hc0] + " " + Lunar.CHI[0];
        return r;
    }

    public static final class Sixty { public int idx; public String name, napAm, element; public final List<Integer> years = new ArrayList<Integer>(); }
    private static List<Sixty> sixtyCache;
    public static synchronized List<Sixty> sixty() {
        if (sixtyCache != null) return sixtyCache;
        List<Sixty> out = new ArrayList<Sixty>();
        for (int i = 0; i < 60; i++) {
            Sixty s = new Sixty(); int c = i % 10, h = i % 12;
            s.idx = i; s.name = canChi(c, h); s.napAm = Engine.napAm(c, h); s.element = Engine.elementOf(s.napAm);
            for (int y = 1804 + i; y <= 2100; y += 60) if (y >= 1840) s.years.add(y);
            out.add(s);
        }
        sixtyCache = out;
        return out;
    }
    public static final class YearInfo { public int year, age; public String name, napAm, element; }
    public static YearInfo yearInfo(int y) {
        int i = ((y - 1804) % 60 + 60) % 60; Sixty s = sixty().get(i);
        YearInfo r = new YearInfo(); r.year = y; r.name = s.name; r.napAm = s.napAm; r.element = s.element;
        r.age = Calendar.getInstance().get(Calendar.YEAR) - y + 1;
        return r;
    }
}
