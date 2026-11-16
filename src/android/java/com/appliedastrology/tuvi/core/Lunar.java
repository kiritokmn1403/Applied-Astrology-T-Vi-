package com.appliedastrology.tuvi.core;

/**
 * Applied Astrology v0.0.4 – Âm lịch Việt Nam (múi giờ +7), thuật toán thiên văn Hồ Ngọc Đức.
 * Viết lại native (Java) từ lunar.js v0.0.2–0.0.3; kết quả đối chiếu 1:1 với bản JS (tools/tests/native_crosscheck).
 */
public final class Lunar {
    private Lunar() { }
    public static final String[] CAN = {"Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"};
    public static final String[] CHI = {"Tí", "Sửu", "Dần", "Mão", "Thìn", "Tị", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"};
    private static final double PI = Math.PI;

    /** Ngày âm lịch */
    public static final class LDate {
        public final int day, month, year; public final boolean leap; public final int jd;
        LDate(int d, int m, int y, boolean l, int jd) { day = d; month = m; year = y; leap = l; this.jd = jd; }
    }

    private static int INT(double d) { return (int) Math.floor(d); }

    public static int jdFromDate(int dd, int mm, int yy) {
        int a = INT((14 - mm) / 12.0), y = yy + 4800 - a, m = mm + 12 * a - 3;
        int jd = dd + INT((153 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - INT(y / 100.0) + INT(y / 400.0) - 32045;
        if (jd < 2299161) jd = dd + INT((153 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - 32083;
        return jd;
    }

    public static int[] jdToDate(int jd) {
        int a, b, c;
        if (jd > 2299160) { a = jd + 32044; b = INT((4.0 * a + 3) / 146097); c = a - INT((b * 146097.0) / 4); }
        else { b = 0; c = jd + 32082; }
        int d = INT((4.0 * c + 3) / 1461), e = c - INT((1461.0 * d) / 4), m = INT((5.0 * e + 2) / 153);
        int day = e - INT((153.0 * m + 2) / 5) + 1, month = m + 3 - 12 * INT(m / 10.0), year = b * 100 + d - 4800 + INT(m / 10.0);
        return new int[]{day, month, year};
    }

    static double newMoon(int k) {
        double T = k / 1236.85, T2 = T * T, T3 = T2 * T, dr = PI / 180;
        double Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
        Jd1 = Jd1 + 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
        double M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
        double Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
        double F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
        double C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
        C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
        C1 = C1 - 0.0004 * Math.sin(dr * 3 * Mpr);
        C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
        C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
        C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
        C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
        double deltat;
        if (T < -11) deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3;
        else deltat = -0.000278 + 0.000265 * T + 0.000262 * T2;
        return Jd1 + C1 - deltat;
    }

    static double sunLongitude(double jdn) {
        double T = (jdn - 2451545.0) / 36525, T2 = T * T, dr = PI / 180;
        double M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
        double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
        double DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL = DL + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
        double L = (L0 + DL) * dr;
        L = L - PI * 2 * INT(L / (PI * 2));
        return L;
    }

    static int getNewMoonDay(int k, double tz) { return INT(newMoon(k) + 0.5 + tz / 24); }
    static int getSunLongitude(int dayNumber, double tz) { return INT(sunLongitude(dayNumber - 0.5 - tz / 24) / PI * 6); }

    static int getLunarMonth11(int yy, double tz) {
        int off = jdFromDate(31, 12, yy) - 2415021;
        int k = INT(off / 29.530588853);
        int nm = getNewMoonDay(k, tz);
        if (getSunLongitude(nm, tz) >= 9) nm = getNewMoonDay(k - 1, tz);
        return nm;
    }

    static int getLeapMonthOffset(int a11, double tz) {
        int k = INT((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        int last, i = 1, arc = getSunLongitude(getNewMoonDay(k + i, tz), tz);
        do { last = arc; i++; arc = getSunLongitude(getNewMoonDay(k + i, tz), tz); } while (arc != last && i < 14);
        return i - 1;
    }

    public static LDate solar2lunar(int dd, int mm, int yy) { return solar2lunar(dd, mm, yy, 7); }
    public static LDate solar2lunar(int dd, int mm, int yy, double tz) {
        int dayNumber = jdFromDate(dd, mm, yy);
        int k = INT((dayNumber - 2415021.076998695) / 29.530588853);
        int monthStart = getNewMoonDay(k + 1, tz);
        if (monthStart > dayNumber) monthStart = getNewMoonDay(k, tz);
        int a11 = getLunarMonth11(yy, tz), b11 = a11, lunarYear;
        if (a11 >= monthStart) { lunarYear = yy; a11 = getLunarMonth11(yy - 1, tz); }
        else { lunarYear = yy + 1; b11 = getLunarMonth11(yy + 1, tz); }
        int lunarDay = dayNumber - monthStart + 1;
        int diff = INT((monthStart - a11) / 29.0);
        boolean lunarLeap = false; int lunarMonth = diff + 11;
        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11, tz);
            if (diff >= leapMonthDiff) { lunarMonth = diff + 10; if (diff == leapMonthDiff) lunarLeap = true; }
        }
        if (lunarMonth > 12) lunarMonth = lunarMonth - 12;
        if (lunarMonth >= 11 && diff < 4) lunarYear -= 1;
        return new LDate(lunarDay, lunarMonth, lunarYear, lunarLeap, dayNumber);
    }

    /** Âm → Dương. null nếu ngày âm không tồn tại */
    public static int[] lunar2solar(int lunarDay, int lunarMonth, int lunarYear, boolean lunarLeap) { return lunar2solar(lunarDay, lunarMonth, lunarYear, lunarLeap, 7); }
    public static int[] lunar2solar(int lunarDay, int lunarMonth, int lunarYear, boolean lunarLeap, double tz) {
        int a11, b11;
        if (lunarMonth < 11) { a11 = getLunarMonth11(lunarYear - 1, tz); b11 = getLunarMonth11(lunarYear, tz); }
        else { a11 = getLunarMonth11(lunarYear, tz); b11 = getLunarMonth11(lunarYear + 1, tz); }
        int k = INT(0.5 + (a11 - 2415021.076998695) / 29.530588853);
        int off = lunarMonth - 11;
        if (off < 0) off += 12;
        if (b11 - a11 > 365) {
            int leapOff = getLeapMonthOffset(a11, tz), leapMonth = leapOff - 2;
            if (leapMonth < 0) leapMonth += 12;
            if (lunarLeap && lunarMonth != leapMonth) return null;
            if (lunarLeap || off >= leapOff) off += 1;
        } else if (lunarLeap) return null;
        int monthStart = getNewMoonDay(k + off, tz);
        return jdToDate(monthStart + lunarDay - 1);
    }

    public static int lunarMonthLength(int lunarMonth, int lunarYear, boolean leap) {
        int[] s = lunar2solar(1, lunarMonth, lunarYear, leap);
        if (s == null) return 0;
        int jd = jdFromDate(s[0], s[1], s[2]);
        int[] n = jdToDate(jd + 29);
        LDate next = solar2lunar(n[0], n[1], n[2]);
        return next.day == 1 ? 29 : 30;
    }

    public static int[] dayCanChi(int jd) { return new int[]{(jd + 9) % 10, (jd + 1) % 12}; }
    public static int tietKhiIndex(int jd) { return INT(sunLongitude(jd + 1 - 0.5 - 7 / 24.0) / PI * 12); }
    public static double sunDegree(int jd) { return sunLongitude(jd + 1 - 0.5 - 7 / 24.0) * 180 / PI; }

    /** Số ngày của tháng dương lịch */
    public static int daysInMonth(int m, int y) {
        if (m == 2) return ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) ? 29 : 28;
        return (m == 4 || m == 6 || m == 9 || m == 11) ? 30 : 31;
    }
    /** Thứ trong tuần theo kiểu JS Date.getDay(): 0 = Chủ Nhật */
    public static int weekday(int d, int m, int y) { return (jdFromDate(d, m, y) + 1) % 7; }
    public static boolean validSolar(int d, int m, int y) { return m >= 1 && m <= 12 && d >= 1 && d <= daysInMonth(m, y); }
}
