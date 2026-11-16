// Applied Astrology v0.0.4 – lõi native C# (Windows 7+ x86/x64/ARM64, .NET Framework 4.x).
// Bản dịch 1:1 của lõi Java (src/android/java/.../core) – kiểm thử chéo với bản JS gốc trong tools/tests/native.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;
using System.Text.RegularExpressions;

namespace AppliedAstrology.Core
{
    // ============================ ÂM LỊCH (Hồ Ngọc Đức) ============================
    public sealed class LDate { public int Day, Month, Year, Jd; public bool Leap; public LDate(int d, int m, int y, bool l, int jd) { Day = d; Month = m; Year = y; Leap = l; Jd = jd; } }

    public static class Lunar
    {
        public static readonly string[] CAN = { "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý" };
        public static readonly string[] CHI = { "Tí", "Sửu", "Dần", "Mão", "Thìn", "Tị", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi" };
        static int INT(double d) { return (int)Math.Floor(d); }
        public static int JdFromDate(int dd, int mm, int yy)
        {
            int a = INT((14 - mm) / 12.0), y = yy + 4800 - a, m = mm + 12 * a - 3;
            int jd = dd + INT((153 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - INT(y / 100.0) + INT(y / 400.0) - 32045;
            if (jd < 2299161) jd = dd + INT((153 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - 32083;
            return jd;
        }
        public static int[] JdToDate(int jd)
        {
            int a, b, c;
            if (jd > 2299160) { a = jd + 32044; b = INT((4.0 * a + 3) / 146097); c = a - INT((b * 146097.0) / 4); }
            else { b = 0; c = jd + 32082; }
            int d = INT((4.0 * c + 3) / 1461), e = c - INT((1461.0 * d) / 4), m = INT((5.0 * e + 2) / 153);
            return new[] { e - INT((153.0 * m + 2) / 5) + 1, m + 3 - 12 * INT(m / 10.0), b * 100 + d - 4800 + INT(m / 10.0) };
        }
        static double NewMoon(int k)
        {
            double T = k / 1236.85, T2 = T * T, T3 = T2 * T, dr = Math.PI / 180;
            double Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
            Jd1 = Jd1 + 0.00033 * Math.Sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
            double M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
            double Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
            double F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
            double C1 = (0.1734 - 0.000393 * T) * Math.Sin(M * dr) + 0.0021 * Math.Sin(2 * dr * M);
            C1 = C1 - 0.4068 * Math.Sin(Mpr * dr) + 0.0161 * Math.Sin(dr * 2 * Mpr);
            C1 = C1 - 0.0004 * Math.Sin(dr * 3 * Mpr);
            C1 = C1 + 0.0104 * Math.Sin(dr * 2 * F) - 0.0051 * Math.Sin(dr * (M + Mpr));
            C1 = C1 - 0.0074 * Math.Sin(dr * (M - Mpr)) + 0.0004 * Math.Sin(dr * (2 * F + M));
            C1 = C1 - 0.0004 * Math.Sin(dr * (2 * F - M)) - 0.0006 * Math.Sin(dr * (2 * F + Mpr));
            C1 = C1 + 0.0010 * Math.Sin(dr * (2 * F - Mpr)) + 0.0005 * Math.Sin(dr * (2 * Mpr + M));
            double deltat = T < -11 ? 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3 : -0.000278 + 0.000265 * T + 0.000262 * T2;
            return Jd1 + C1 - deltat;
        }
        static double SunLongitude(double jdn)
        {
            double T = (jdn - 2451545.0) / 36525, T2 = T * T, dr = Math.PI / 180;
            double M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
            double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
            double DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.Sin(dr * M);
            DL = DL + (0.019993 - 0.000101 * T) * Math.Sin(dr * 2 * M) + 0.000290 * Math.Sin(dr * 3 * M);
            double L = (L0 + DL) * dr;
            L = L - Math.PI * 2 * INT(L / (Math.PI * 2));
            return L;
        }
        static int GetNewMoonDay(int k, double tz) { return INT(NewMoon(k) + 0.5 + tz / 24); }
        static int GetSunLongitude(int dayNumber, double tz) { return INT(SunLongitude(dayNumber - 0.5 - tz / 24) / Math.PI * 6); }
        static int GetLunarMonth11(int yy, double tz)
        {
            int off = JdFromDate(31, 12, yy) - 2415021, k = INT(off / 29.530588853), nm = GetNewMoonDay(k, tz);
            if (GetSunLongitude(nm, tz) >= 9) nm = GetNewMoonDay(k - 1, tz);
            return nm;
        }
        static int GetLeapMonthOffset(int a11, double tz)
        {
            int k = INT((a11 - 2415021.076998695) / 29.530588853 + 0.5), last, i = 1, arc = GetSunLongitude(GetNewMoonDay(k + i, tz), tz);
            do { last = arc; i++; arc = GetSunLongitude(GetNewMoonDay(k + i, tz), tz); } while (arc != last && i < 14);
            return i - 1;
        }
        public static LDate Solar2Lunar(int dd, int mm, int yy) { return Solar2Lunar(dd, mm, yy, 7); }
        public static LDate Solar2Lunar(int dd, int mm, int yy, double tz)
        {
            int dayNumber = JdFromDate(dd, mm, yy), k = INT((dayNumber - 2415021.076998695) / 29.530588853);
            int monthStart = GetNewMoonDay(k + 1, tz);
            if (monthStart > dayNumber) monthStart = GetNewMoonDay(k, tz);
            int a11 = GetLunarMonth11(yy, tz), b11 = a11, lunarYear;
            if (a11 >= monthStart) { lunarYear = yy; a11 = GetLunarMonth11(yy - 1, tz); }
            else { lunarYear = yy + 1; b11 = GetLunarMonth11(yy + 1, tz); }
            int lunarDay = dayNumber - monthStart + 1, diff = INT((monthStart - a11) / 29.0);
            bool leap = false; int lunarMonth = diff + 11;
            if (b11 - a11 > 365) { int lmd = GetLeapMonthOffset(a11, tz); if (diff >= lmd) { lunarMonth = diff + 10; if (diff == lmd) leap = true; } }
            if (lunarMonth > 12) lunarMonth -= 12;
            if (lunarMonth >= 11 && diff < 4) lunarYear -= 1;
            return new LDate(lunarDay, lunarMonth, lunarYear, leap, dayNumber);
        }
        public static int[] Lunar2Solar(int lunarDay, int lunarMonth, int lunarYear, bool lunarLeap) { return Lunar2Solar(lunarDay, lunarMonth, lunarYear, lunarLeap, 7); }
        public static int[] Lunar2Solar(int lunarDay, int lunarMonth, int lunarYear, bool lunarLeap, double tz)
        {
            int a11, b11;
            if (lunarMonth < 11) { a11 = GetLunarMonth11(lunarYear - 1, tz); b11 = GetLunarMonth11(lunarYear, tz); }
            else { a11 = GetLunarMonth11(lunarYear, tz); b11 = GetLunarMonth11(lunarYear + 1, tz); }
            int k = INT(0.5 + (a11 - 2415021.076998695) / 29.530588853), off = lunarMonth - 11;
            if (off < 0) off += 12;
            if (b11 - a11 > 365)
            {
                int leapOff = GetLeapMonthOffset(a11, tz), leapMonth = leapOff - 2;
                if (leapMonth < 0) leapMonth += 12;
                if (lunarLeap && lunarMonth != leapMonth) return null;
                if (lunarLeap || off >= leapOff) off += 1;
            }
            else if (lunarLeap) return null;
            return JdToDate(GetNewMoonDay(k + off, tz) + lunarDay - 1);
        }
        public static int LunarMonthLength(int m, int y, bool leap)
        {
            int[] s = Lunar2Solar(1, m, y, leap); if (s == null) return 0;
            int[] n = JdToDate(JdFromDate(s[0], s[1], s[2]) + 29);
            return Solar2Lunar(n[0], n[1], n[2]).Day == 1 ? 29 : 30;
        }
        public static int[] DayCanChi(int jd) { return new[] { (jd + 9) % 10, (jd + 1) % 12 }; }
        public static int TietKhiIndex(int jd) { return INT(SunLongitude(jd + 1 - 0.5 - 7 / 24.0) / Math.PI * 12); }
        public static double SunDegree(int jd) { return SunLongitude(jd + 1 - 0.5 - 7 / 24.0) * 180 / Math.PI; }
        public static int DaysInMonth(int m, int y) { if (m == 2) return ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) ? 29 : 28; return (m == 4 || m == 6 || m == 9 || m == 11) ? 30 : 31; }
        public static int Weekday(int d, int m, int y) { return (JdFromDate(d, m, y) + 1) % 7; }
        public static bool ValidSolar(int d, int m, int y) { return m >= 1 && m <= 12 && d >= 1 && d <= DaysInMonth(m, y); }
    }

    // ============================ ENGINE TỬ VI (Bửu Đình) ============================
    public sealed class Star { public string Name, Element, Kind, Bright, AttachedTo; public Star(string n, string e, string k) { Name = n; Element = e ?? ""; Kind = k; } }
    public sealed class Palace
    {
        public int Chi, Can, DaiHan; public string ChiName, CanChi, Element, Name, TieuHan = "", RingTrangSinh, RingBacSi, RingThaiTue; public bool IsMenh, IsThan, Tuan, Triet;
        public readonly List<Star> Major = new List<Star>(), Good = new List<Star>(), Bad = new List<Star>(), Neutral = new List<Star>();
        public readonly List<string[]> Hoa = new List<string[]>(); public readonly List<string> Luu = new List<string>();
        internal List<Star> ListOf(string k) { return k == "good" ? Good : k == "bad" ? Bad : Neutral; }
    }
    public sealed class ChartInput { public string Name = "Thân Chủ", Gender = "Nam", Calendar = "solar", LeapMode = "same"; public int Day, Month, Year, HourChi, ViewYear; public bool Leap; }
    public sealed class Chart
    {
        public string Name, Gender, YearCanChi, MonthCanChi, DayCanChi, HourCanChi, AmDuong, NapAm, BanMenh, CucName, MenhCuc, MenhChu, ThanChu, AmDuongLy, ViewYearCanChi;
        public int HourChi, CalcMonth, ViewYear, SDay, SMonth, SYear, CanYear, ChiYear, Menh, Than, Cuc, Age, CurrentDaiHan = -1, CurrentTieuHan = -1;
        public LDate LunarDate; public bool IsDuongYear, Thuan, Vocd;
        public readonly Palace[] Palaces = new Palace[12];
        public int Where(string star)
        {
            for (int i = 0; i < 12; i++) { var p = Palaces[i]; foreach (var l in new[] { p.Major, p.Good, p.Bad, p.Neutral }) foreach (var s in l) if (s.Name == star) return i; }
            return -1;
        }
        public int[] TamPhuong(int chi) { return new[] { chi, Engine.Mod(chi + 6, 12), Engine.Mod(chi + 4, 12), Engine.Mod(chi + 8, 12) }; }
        internal Star Add(int pos, string star) { var p = Palaces[Engine.Mod(pos, 12)]; string k = Engine.KindOf(star); string el; Engine.STAR_ELEMENT.TryGetValue(star, out el); var o = new Star(star, el, k); p.ListOf(k).Add(o); return o; }
        void Lu(int pos, string n) { Palaces[Engine.Mod(pos, 12)].Luu.Add(n); }
        public Chart ApplyYear(int year)
        {
            ViewYear = year; int can = Engine.Mod(year - 4, 10), chi = Engine.Mod(year - 4, 12);
            for (int i = 0; i < 12; i++) Palaces[i].Luu.Clear();
            int lt = Engine.LOC_TON[can];
            Lu(chi, "L.Thái Tuế"); Lu(lt, "L.Lộc Tồn"); Lu(lt + 1, "L.Kình Dương"); Lu(lt - 1, "L.Đà La");
            Lu(Engine.THIEN_MA[chi % 4], "L.Thiên Mã"); Lu(chi + 2, "L.Tang Môn"); Lu(chi + 8, "L.Bạch Hổ");
            var th = Engine.TU_HOA[can];
            for (int i = 0; i < 4; i++) { int at = Where(th[i]); if (at >= 0) Lu(at, "L." + Engine.HOA[i]); }
            Age = year - LunarDate.Year + 1; CurrentDaiHan = -1; CurrentTieuHan = -1;
            for (int i = 0; i < 12; i++) { var p = Palaces[i]; if (Age >= p.DaiHan && Age < p.DaiHan + 10) CurrentDaiHan = i; if (p.TieuHan == Engine.CHI[chi]) CurrentTieuHan = i; }
            ViewYearCanChi = Engine.CAN[can] + " " + Engine.CHI[chi];
            return this;
        }
    }

    public static class Engine
    {
        public static readonly string[] CAN = Lunar.CAN, CHI = Lunar.CHI;
        static readonly string[] CHI_ELEMENT = { "Thủy", "Thổ", "Mộc", "Mộc", "Thổ", "Hỏa", "Hỏa", "Thổ", "Kim", "Kim", "Thổ", "Thủy" };
        static readonly string[] NAP_AM = { "Hải Trung Kim", "Lư Trung Hỏa", "Đại Lâm Mộc", "Lộ Bàng Thổ", "Kiếm Phong Kim", "Sơn Đầu Hỏa", "Giản Hạ Thủy", "Thành Đầu Thổ", "Bạch Lạp Kim", "Dương Liễu Mộc", "Tuyền Trung Thủy", "Ốc Thượng Thổ", "Tích Lịch Hỏa", "Tùng Bách Mộc", "Trường Lưu Thủy", "Sa Trung Kim", "Sơn Hạ Hỏa", "Bình Địa Mộc", "Bích Thượng Thổ", "Kim Bạch Kim", "Phú Đăng Hỏa", "Thiên Hà Thủy", "Đại Trạch Thổ", "Thoa Xuyến Kim", "Tang Đố Mộc", "Đại Khê Thủy", "Sa Trung Thổ", "Thiên Thượng Hỏa", "Thạch Lựu Mộc", "Đại Hải Thủy" };
        static readonly string[] ELEMENTS = { "Kim", "Mộc", "Thủy", "Hỏa", "Thổ" };
        public static readonly string[] PALACES = { "Mệnh", "Phụ Mẫu", "Phúc Đức", "Điền Trạch", "Quan Lộc", "Nô Bộc", "Thiên Di", "Tật Ách", "Tài Bạch", "Tử Tức", "Phu Thê", "Huynh Đệ" };
        public static readonly string[] HOA = { "Hóa Lộc", "Hóa Quyền", "Hóa Khoa", "Hóa Kỵ" };
        internal static readonly Dictionary<string, string[]> BRIGHT = new Dictionary<string, string[]>();
        internal static readonly Dictionary<string, string> STAR_ELEMENT = new Dictionary<string, string>(), KIND = new Dictionary<string, string>();
        static Engine()
        {
            string[,] b = { { "Tử Vi", "B Đ M B V M M Đ M B V B" }, { "Liêm Trinh", "V Đ V H M H V Đ V H M H" }, { "Thiên Đồng", "V H M Đ H Đ H H M H H Đ" }, { "Vũ Khúc", "V M V Đ M H V M V Đ M H" }, { "Thái Dương", "H Đ V V V M M Đ H H H H" }, { "Thiên Cơ", "Đ Đ H M M V Đ Đ V M M H" }, { "Thiên Phủ", "M B M B V Đ M Đ M B V Đ" }, { "Thái Âm", "V Đ H H H H H Đ V M M M" }, { "Tham Lang", "H M Đ H V H H M Đ H V H" }, { "Cự Môn", "V H V M H H V H Đ M H Đ" }, { "Thiên Tướng", "V Đ M H V Đ V Đ M H V Đ" }, { "Thiên Lương", "V Đ V V M H M Đ V H M H" }, { "Thất Sát", "M Đ M H H V M Đ M H H V" }, { "Phá Quân", "M V H H Đ H M V H H Đ H" } };
            for (int i = 0; i < b.GetLength(0); i++) BRIGHT[b[i, 0]] = b[i, 1].Split(' ');
            string[] se = { "Tử Vi", "Thổ", "Liêm Trinh", "Hỏa", "Thiên Đồng", "Thủy", "Vũ Khúc", "Kim", "Thái Dương", "Hỏa", "Thiên Cơ", "Mộc", "Thiên Phủ", "Thổ", "Thái Âm", "Thủy", "Tham Lang", "Thủy", "Cự Môn", "Thủy", "Thiên Tướng", "Thủy", "Thiên Lương", "Mộc", "Thất Sát", "Kim", "Phá Quân", "Thủy", "Tả Phù", "Thổ", "Hữu Bật", "Thủy", "Văn Xương", "Kim", "Văn Khúc", "Thủy", "Lộc Tồn", "Thổ", "Kình Dương", "Kim", "Đà La", "Kim", "Hỏa Tinh", "Hỏa", "Linh Tinh", "Hỏa", "Địa Không", "Hỏa", "Địa Kiếp", "Hỏa", "Thiên Khôi", "Hỏa", "Thiên Việt", "Hỏa", "Thiên Mã", "Hỏa", "Hóa Lộc", "Mộc", "Hóa Quyền", "Mộc", "Hóa Khoa", "Thủy", "Hóa Kỵ", "Thủy", "Đào Hoa", "Mộc", "Hồng Loan", "Thủy", "Thiên Hỉ", "Thủy", "Thái Tuế", "Hỏa", "Bạch Hổ", "Kim", "Tang Môn", "Mộc", "Thiên Hình", "Hỏa", "Thiên Diêu", "Thủy", "Tam Thai", "Thổ", "Bát Tọa", "Thổ" };
            for (int i = 0; i < se.Length; i += 2) STAR_ELEMENT[se[i]] = se[i + 1];
            foreach (var s in "Tả Phù,Hữu Bật,Văn Xương,Văn Khúc,Thiên Khôi,Thiên Việt,Lộc Tồn,Thiên Mã,Hóa Lộc,Hóa Quyền,Hóa Khoa,Thiên Y,Tam Thai,Bát Tọa,Ân Quang,Thiên Quý,Thai Phụ,Phong Cáo,Long Trì,Phượng Các,Giải Thần,Thiên Đức,Nguyệt Đức,Hồng Loan,Thiên Hỉ,Hoa Cái,Quốc Ấn,Đường Phù,Thiên Quan,Thiên Phúc,Thiên Giải,Địa Giải,Thiên Trù".Split(',')) KIND[s] = "good";
            foreach (var s in "Hóa Kỵ,Kình Dương,Đà La,Hỏa Tinh,Linh Tinh,Địa Không,Địa Kiếp,Thiên Hình,Lưu Hà,Kiếp Sát,Phá Toái,Cô Thần,Quả Tú,Thiên Khốc,Thiên Hư,Tang Môn,Bạch Hổ,Điếu Khách,Tuế Phá,Thiên La,Địa Võng,Thiên Thương,Thiên Sứ,Quan Phù,Bệnh Phù,Đại Hao,Tiểu Hao,Phục Binh,Quan Phủ,Thiên Không".Split(',')) KIND[s] = "bad";
            foreach (var s in "Thiên Diêu,Đào Hoa,Thái Tuế,Phi Liêm".Split(',')) KIND[s] = "neutral";
        }
        internal static readonly int[] LOC_TON = { 2, 3, 5, 6, 5, 6, 8, 9, 11, 0 }, KHOI = { 1, 0, 11, 9, 7, 8, 7, 6, 5, 3 }, VIET = { 7, 8, 9, 11, 1, 0, 1, 2, 3, 5 }, LUU_HA = { 9, 10, 7, 8, 5, 6, 3, 4, 11, 2 }, THIEN_QUAN = { 7, 4, 5, 2, 3, 9, 11, 9, 10, 6 }, THIEN_PHUC = { 9, 8, 0, 11, 3, 2, 6, 5, 6, 5 }, THIEN_TRU = { 5, 6, 0, 5, 6, 8, 2, 6, 9, 10 };
        internal static readonly string[][] TU_HOA = { new[] { "Liêm Trinh", "Phá Quân", "Vũ Khúc", "Thái Dương" }, new[] { "Thiên Cơ", "Thiên Lương", "Tử Vi", "Thái Âm" }, new[] { "Thiên Đồng", "Thiên Cơ", "Văn Xương", "Liêm Trinh" }, new[] { "Thái Âm", "Thiên Đồng", "Thiên Cơ", "Cự Môn" }, new[] { "Tham Lang", "Thái Âm", "Hữu Bật", "Thiên Cơ" }, new[] { "Vũ Khúc", "Tham Lang", "Thiên Lương", "Văn Khúc" }, new[] { "Thái Dương", "Vũ Khúc", "Thái Âm", "Thiên Đồng" }, new[] { "Cự Môn", "Thái Dương", "Văn Khúc", "Văn Xương" }, new[] { "Thiên Lương", "Tử Vi", "Tả Phù", "Vũ Khúc" }, new[] { "Phá Quân", "Cự Môn", "Thái Âm", "Tham Lang" } };
        static readonly string[] BAC_SI = { "Bác Sĩ", "Lực Sĩ", "Thanh Long", "Tiểu Hao", "Tướng Quân", "Tấu Thư", "Phi Liêm", "Hỉ Thần", "Bệnh Phù", "Đại Hao", "Phục Binh", "Quan Phủ" };
        static readonly string[] THAI_TUE = { "Thái Tuế", "Thiếu Dương", "Tang Môn", "Thiếu Âm", "Quan Phù", "Tử Phù", "Tuế Phá", "Long Đức", "Bạch Hổ", "Phúc Đức", "Điếu Khách", "Trực Phù" };
        static readonly string[] TRANG_SINH = { "Tràng Sinh", "Mộc Dục", "Quan Đới", "Lâm Quan", "Đế Vượng", "Suy", "Bệnh", "Tử", "Mộ", "Tuyệt", "Thai", "Dưỡng" };
        internal static readonly int[] DAO_HOA = { 9, 6, 3, 0 }, KIEP_SAT = { 5, 2, 11, 8 }, THIEN_MA = { 2, 11, 8, 5 }, HOA_BASE = { 2, 3, 1, 9 }, LINH_BASE = { 10, 10, 3, 10 }, TIEU_HAN_START = { 10, 7, 4, 1 };
        static readonly string[] MENH_CHU = { "Tham Lang", "Cự Môn", "Lộc Tồn", "Văn Khúc", "Liêm Trinh", "Vũ Khúc", "Phá Quân", "Vũ Khúc", "Liêm Trinh", "Văn Khúc", "Lộc Tồn", "Cự Môn" };
        static readonly string[] THAN_CHU = { "Linh Tinh", "Thiên Tướng", "Thiên Lương", "Thiên Đồng", "Văn Xương", "Thiên Cơ", "Hỏa Tinh", "Thiên Tướng", "Thiên Lương", "Thiên Đồng", "Văn Xương", "Thiên Cơ" };
        public static int Mod(int a, int n) { return ((a % n) + n) % n; }
        static int Sexagenary(int can, int chi) { for (int i = 0; i < 60; i++) if (i % 10 == can && i % 12 == chi) return i; return 0; }
        public static string NapAm(int can, int chi) { return NAP_AM[Sexagenary(can, chi) / 2]; }
        public static string ElementOf(string n) { foreach (var e in ELEMENTS) if (n.Contains(e)) return e; return "Thổ"; }
        static int CanOfDan(int canYear) { return new[] { 2, 4, 6, 8, 0 }[canYear % 5]; }
        public static int CucOf(string e) { return e == "Thủy" ? 2 : e == "Mộc" ? 3 : e == "Kim" ? 4 : e == "Thổ" ? 5 : 6; }
        public static string CucName(int c) { return c == 2 ? "Thủy Nhị Cục" : c == 3 ? "Mộc Tam Cục" : c == 4 ? "Kim Tứ Cục" : c == 5 ? "Thổ Ngũ Cục" : "Hỏa Lục Cục"; }
        static int TrangSinhStart(int cuc) { return cuc == 2 || cuc == 5 ? 8 : cuc == 3 ? 11 : cuc == 4 ? 5 : 2; }
        public static string SinhKhac(string a, string b)
        {
            string[] e = { "Kim", "Thủy", "Mộc", "Hỏa", "Thổ" };
            if (a == b) return "hòa";
            int ia = Array.IndexOf(e, a), ib = Array.IndexOf(e, b);
            if ((ia + 1) % 5 == ib) return "sinh"; if ((ia + 2) % 5 == ib) return "khắc"; if ((ib + 1) % 5 == ia) return "được sinh"; return "bị khắc";
        }
        public static string KindOf(string star) { string k; return KIND.TryGetValue(star, out k) ? k : "neutral"; }

        public static Chart Build(ChartInput inp)
        {
            var c = new Chart();
            c.Name = string.IsNullOrEmpty((inp.Name ?? "").Trim()) ? "Thân Chủ" : inp.Name;
            c.Gender = inp.Gender == "Nữ" ? "Nữ" : "Nam"; c.HourChi = Mod(inp.HourChi, 12);
            if (inp.Calendar == "lunar")
            {
                var s = Lunar.Lunar2Solar(inp.Day, inp.Month, inp.Year, inp.Leap);
                if (s == null) throw new ArgumentException("INVALID_LUNAR_DATE");
                c.LunarDate = new LDate(inp.Day, inp.Month, inp.Year, inp.Leap, Lunar.JdFromDate(s[0], s[1], s[2])); c.SDay = s[0]; c.SMonth = s[1]; c.SYear = s[2];
            }
            else { c.SDay = inp.Day; c.SMonth = inp.Month; c.SYear = inp.Year; c.LunarDate = Lunar.Solar2Lunar(inp.Day, inp.Month, inp.Year); }
            int m = c.LunarDate.Month;
            if (c.LunarDate.Leap && inp.LeapMode == "next" && c.LunarDate.Day > 15) m = m == 12 ? 1 : m + 1;
            c.CalcMonth = m; c.ViewYear = inp.ViewYear > 0 ? inp.ViewYear : DateTime.Now.Year;
            BuildStars(c);
            return c;
        }
        static void BuildStars(Chart c)
        {
            int y = c.LunarDate.Year, d = c.LunarDate.Day, m = c.CalcMonth, h = c.HourChi, i;
            int canY = Mod(y - 4, 10), chiY = Mod(y - 4, 12); c.CanYear = canY; c.ChiYear = chiY;
            c.YearCanChi = CAN[canY] + " " + CHI[chiY];
            c.MonthCanChi = CAN[Mod(CanOfDan(canY) + (m - 1), 10)] + " " + CHI[Mod(m + 1, 12)];
            int jd = Lunar.JdFromDate(c.SDay, c.SMonth, c.SYear); var dcc = Lunar.DayCanChi(jd);
            c.DayCanChi = CAN[dcc[0]] + " " + CHI[dcc[1]]; c.HourCanChi = CAN[Mod(dcc[0] % 5 * 2 + h, 10)] + " " + CHI[h];
            c.IsDuongYear = canY % 2 == 0; c.AmDuong = (c.IsDuongYear ? "Dương " : "Âm ") + c.Gender;
            c.Thuan = (c.IsDuongYear && c.Gender == "Nam") || (!c.IsDuongYear && c.Gender == "Nữ");
            c.NapAm = NapAm(canY, chiY); c.BanMenh = ElementOf(c.NapAm);
            c.Menh = Mod(2 + (m - 1) - h, 12); c.Than = Mod(2 + (m - 1) + h, 12);
            int dan = CanOfDan(canY);
            for (i = 0; i < 12; i++)
            {
                var p = new Palace { Chi = i, ChiName = CHI[i], Can = Mod(dan + Mod(i - 2, 12), 10), Element = CHI_ELEMENT[i], Name = PALACES[Mod(i - c.Menh, 12)], IsMenh = i == c.Menh, IsThan = i == c.Than };
                p.CanChi = CAN[p.Can] + " " + CHI[i]; c.Palaces[i] = p;
            }
            var mp = c.Palaces[c.Menh];
            c.Cuc = CucOf(ElementOf(NapAm(mp.Can, mp.Chi))); c.CucName = CucName(c.Cuc); c.MenhCuc = SinhKhac(c.BanMenh, ElementOf(c.CucName));
            int cuc = c.Cuc, x = 0; while ((d + x) % cuc != 0) x++;
            int q = (d + x) / cuc, tuvi = 2 + (q - 1); tuvi = x % 2 == 1 ? tuvi - x : tuvi + x; tuvi = Mod(tuvi, 12);
            int phu = Mod(4 - tuvi, 12);
            object[][] majors = { new object[] { "Tử Vi", 0, tuvi }, new object[] { "Thiên Cơ", -1, tuvi }, new object[] { "Thái Dương", -3, tuvi }, new object[] { "Vũ Khúc", -4, tuvi }, new object[] { "Thiên Đồng", -5, tuvi }, new object[] { "Liêm Trinh", -8, tuvi }, new object[] { "Thiên Phủ", 0, phu }, new object[] { "Thái Âm", 1, phu }, new object[] { "Tham Lang", 2, phu }, new object[] { "Cự Môn", 3, phu }, new object[] { "Thiên Tướng", 4, phu }, new object[] { "Thiên Lương", 5, phu }, new object[] { "Thất Sát", 6, phu }, new object[] { "Phá Quân", 10, phu } };
            foreach (var mj in majors) { int pos = Mod((int)mj[2] + (int)mj[1], 12); string nm = (string)mj[0]; c.Palaces[pos].Major.Add(new Star(nm, STAR_ELEMENT[nm], "major") { Bright = BRIGHT[nm][pos] }); }
            int lt = LOC_TON[canY];
            c.Add(lt, "Lộc Tồn"); c.Add(lt + 1, "Kình Dương"); c.Add(lt - 1, "Đà La"); c.Add(lt + 8, "Quốc Ấn"); c.Add(lt + 5, "Đường Phù");
            c.Add(KHOI[canY], "Thiên Khôi"); c.Add(VIET[canY], "Thiên Việt");
            c.Add(LUU_HA[canY], "Lưu Hà"); c.Add(THIEN_QUAN[canY], "Thiên Quan"); c.Add(THIEN_PHUC[canY], "Thiên Phúc"); c.Add(THIEN_TRU[canY], "Thiên Trù");
            int dir = c.Thuan ? 1 : -1;
            for (i = 0; i < 12; i++) c.Palaces[Mod(lt + dir * i, 12)].RingBacSi = BAC_SI[i];
            for (i = 0; i < 12; i++) c.Palaces[Mod(chiY + i, 12)].RingThaiTue = THAI_TUE[i];
            c.Add(chiY, "Thái Tuế"); c.Add(chiY + 2, "Tang Môn"); c.Add(chiY + 4, "Quan Phù"); c.Add(chiY + 6, "Tuế Phá"); c.Add(chiY + 8, "Bạch Hổ"); c.Add(chiY + 10, "Điếu Khách");
            c.Add(chiY + 1, "Thiên Không"); c.Add(4 + chiY, "Long Trì"); c.Add(10 - chiY, "Phượng Các"); c.Add(10 - chiY, "Giải Thần");
            c.Add(6 - chiY, "Thiên Khốc"); c.Add(6 + chiY, "Thiên Hư"); c.Add(3 - chiY, "Hồng Loan"); c.Add(9 - chiY, "Thiên Hỉ");
            int g = chiY % 4;
            c.Add(DAO_HOA[g], "Đào Hoa"); c.Add(KIEP_SAT[g], "Kiếp Sát"); c.Add(THIEN_MA[g], "Thiên Mã"); c.Add(THIEN_MA[g] + 2, "Hoa Cái");
            int co = new[] { 2, 2, 5, 5, 5, 8, 8, 8, 11, 11, 11, 2 }[chiY];
            c.Add(co, "Cô Thần"); c.Add(co - 4, "Quả Tú"); c.Add(new[] { 5, 1, 9, 5, 1, 9, 5, 1, 9, 5, 1, 9 }[chiY], "Phá Toái");
            c.Add(9 + chiY, "Thiên Đức"); c.Add(5 + chiY, "Nguyệt Đức");
            int taPhu = Mod(4 + (m - 1), 12), huuBat = Mod(10 - (m - 1), 12);
            c.Add(taPhu, "Tả Phù"); c.Add(huuBat, "Hữu Bật"); c.Add(9 + (m - 1), "Thiên Hình"); c.Add(1 + (m - 1), "Thiên Diêu"); c.Add(1 + (m - 1), "Thiên Y");
            c.Add(8 + (m - 1), "Thiên Giải"); c.Add(7 + (m - 1), "Địa Giải");
            int xuong = Mod(10 - h, 12), khuc = Mod(4 + h, 12);
            c.Add(xuong, "Văn Xương"); c.Add(khuc, "Văn Khúc"); c.Add(11 + h, "Địa Kiếp"); c.Add(11 - h, "Địa Không"); c.Add(6 + h, "Thai Phụ"); c.Add(2 + h, "Phong Cáo");
            int hb = HOA_BASE[g], lb = LINH_BASE[g]; bool hl = c.Thuan;
            c.Add(hl ? hb + h : hb - h, "Hỏa Tinh"); c.Add(hl ? lb - h : lb + h, "Linh Tinh");
            c.Add(taPhu + (d - 1), "Tam Thai"); c.Add(huuBat - (d - 1), "Bát Tọa"); c.Add(xuong + (d - 1) - 1, "Ân Quang"); c.Add(khuc - (d - 1) + 1, "Thiên Quý");
            c.Add(4, "Thiên La"); c.Add(10, "Địa Võng"); c.Add(Mod(c.Menh + 5, 12), "Thiên Thương"); c.Add(Mod(c.Menh + 7, 12), "Thiên Sứ");
            int ts = TrangSinhStart(cuc);
            for (i = 0; i < 12; i++) c.Palaces[Mod(ts + dir * i, 12)].RingTrangSinh = TRANG_SINH[i];
            var th = TU_HOA[canY];
            for (i = 0; i < 4; i++) { int at = c.Where(th[i]); if (at >= 0) { var s = c.Add(at, HOA[i]); s.AttachedTo = th[i]; c.Palaces[at].Hoa.Add(new[] { HOA[i], th[i] }); } }
            int tuanStart = Mod(chiY - canY, 12);
            c.Palaces[Mod(tuanStart + 10, 12)].Tuan = true; c.Palaces[Mod(tuanStart + 11, 12)].Tuan = true;
            int[] triet = new[] { new[] { 8, 9 }, new[] { 6, 7 }, new[] { 4, 5 }, new[] { 2, 3 }, new[] { 0, 1 } }[canY % 5];
            c.Palaces[triet[0]].Triet = true; c.Palaces[triet[1]].Triet = true;
            for (i = 0; i < 12; i++) c.Palaces[Mod(c.Menh + dir * i, 12)].DaiHan = cuc + 10 * i;
            int thStart = TIEU_HAN_START[g], tdir = c.Gender == "Nam" ? 1 : -1;
            for (i = 0; i < 12; i++) c.Palaces[Mod(thStart + tdir * i, 12)].TieuHan = CHI[Mod(chiY + i, 12)];
            c.MenhChu = MENH_CHU[c.Menh]; c.ThanChu = THAN_CHU[chiY];
            c.AmDuongLy = ((c.Menh % 2 == 0) == c.IsDuongYear) ? "Âm Dương thuận lý" : "Âm Dương nghịch lý";
            c.Vocd = c.Palaces[c.Menh].Major.Count == 0;
            c.ApplyYear(c.ViewYear);
        }
    }

    // ============================ ÂM LỊCH (lịch tháng, tiết khí, hoàng đạo, lục thập hoa giáp) ============================
    public sealed class Hour { public int Chi; public string Name, Can, Range; }
    public sealed class CalEvent { public bool Lunar; public string Name; }
    public sealed class DayInfo
    {
        public int Day, Month, Year, Weekday, Jd; public LDate Lunar; public bool TietStart, HoangDao, MonthLeap;
        public string DayCC, MonthCC, YearCC, NapAmDay, NapAmYear, Tiet, Than, Truc, HourZero;
        public readonly List<Hour> GoodHours = new List<Hour>(), BadHours = new List<Hour>(); public readonly List<CalEvent> Events = new List<CalEvent>();
    }
    public sealed class Sixty { public int Idx; public string Name, NapAm, Element; public readonly List<int> Years = new List<int>(); }
    public sealed class YearInfo { public int Year, Age; public string Name, NapAm, Element; }
    public static class Cal
    {
        public static readonly string[] TIET = { "Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng", "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ", "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết", "Đông Chí", "Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập" };
        static readonly string[] GIO_HD = { "110100101100", "001101001011", "110011010010", "101100110100", "001011001101", "010010110011" };
        static readonly string[] THAN = { "Thanh Long", "Minh Đường", "Thiên Hình", "Chu Tước", "Kim Quỹ", "Kim Đường", "Bạch Hổ", "Ngọc Đường", "Thiên Lao", "Huyền Vũ", "Tư Mệnh", "Câu Trận" };
        static readonly int[] THAN_GOOD = { 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0 };
        static readonly string[] TRUC = { "Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế" };
        static readonly object[][] LUNAR_EVENTS = { new object[] { 1, 1, "Tết Nguyên Đán" }, new object[] { 2, 1, "Mùng 2 Tết" }, new object[] { 3, 1, "Mùng 3 Tết" }, new object[] { 15, 1, "Tết Nguyên Tiêu" }, new object[] { 3, 3, "Tết Hàn Thực" }, new object[] { 10, 3, "Giỗ Tổ Hùng Vương" }, new object[] { 15, 4, "Lễ Phật Đản" }, new object[] { 5, 5, "Tết Đoan Ngọ" }, new object[] { 15, 7, "Lễ Vu Lan" }, new object[] { 15, 8, "Tết Trung Thu" }, new object[] { 9, 9, "Tết Trùng Cửu" }, new object[] { 10, 10, "Tết Thường Tân" }, new object[] { 15, 10, "Tết Hạ Nguyên" }, new object[] { 23, 12, "Ông Công Ông Táo về trời" } };
        static readonly object[][] SOLAR_EVENTS = { new object[] { 1, 1, "Tết Dương lịch" }, new object[] { 9, 1, "Ngày Học sinh – Sinh viên" }, new object[] { 3, 2, "Thành lập Đảng CSVN" }, new object[] { 14, 2, "Lễ Tình nhân" }, new object[] { 27, 2, "Ngày Thầy thuốc Việt Nam" }, new object[] { 8, 3, "Quốc tế Phụ nữ" }, new object[] { 26, 3, "Thành lập Đoàn TNCS HCM" }, new object[] { 21, 4, "Ngày Sách Việt Nam" }, new object[] { 30, 4, "Ngày Giải phóng miền Nam" }, new object[] { 1, 5, "Quốc tế Lao động" }, new object[] { 7, 5, "Chiến thắng Điện Biên Phủ" }, new object[] { 19, 5, "Ngày sinh Chủ tịch Hồ Chí Minh" }, new object[] { 1, 6, "Quốc tế Thiếu nhi" }, new object[] { 21, 6, "Ngày Báo chí Việt Nam" }, new object[] { 28, 6, "Ngày Gia đình Việt Nam" }, new object[] { 27, 7, "Ngày Thương binh Liệt sĩ" }, new object[] { 19, 8, "Cách mạng tháng Tám" }, new object[] { 2, 9, "Quốc khánh" }, new object[] { 10, 10, "Giải phóng Thủ đô" }, new object[] { 13, 10, "Ngày Doanh nhân Việt Nam" }, new object[] { 20, 10, "Ngày Phụ nữ Việt Nam" }, new object[] { 20, 11, "Ngày Nhà giáo Việt Nam" }, new object[] { 22, 12, "Thành lập QĐND Việt Nam" }, new object[] { 24, 12, "Lễ Giáng sinh" } };
        static string Pad(int n) { return n < 10 ? "0" + n : n.ToString(); }
        static string CC(int c, int h) { return Lunar.CAN[c] + " " + Lunar.CHI[h]; }
        public static DayInfo DayInfo(int d, int m, int y)
        {
            var r = new DayInfo(); int jd = Lunar.JdFromDate(d, m, y); var lu = Lunar.Solar2Lunar(d, m, y);
            int dc = (jd + 9) % 10, dh = (jd + 1) % 12, mc = (lu.Year * 12 + lu.Month + 3) % 10, mh = (lu.Month + 1) % 12, yc = (lu.Year + 6) % 10, yh = (lu.Year + 8) % 12;
            int tk = Lunar.TietKhiIndex(jd), tkPrev = Lunar.TietKhiIndex(jd - 1);
            double deg = Lunar.SunDegree(jd); int tietMonthChi = (2 + (int)Math.Floor(((deg - 315 + 720) % 360) / 30)) % 12;
            int thanStart = new[] { 0, 2, 4, 6, 8, 10 }[(lu.Month - 1) % 6], than = (dh - thanStart + 12) % 12;
            string hd = GIO_HD[dh % 6]; int hc0 = (dc % 5) * 2;
            for (int i = 0; i < 12; i++) { var h = new Hour { Chi = i, Name = Lunar.CHI[i], Can = Lunar.CAN[(hc0 + i) % 10], Range = Pad((i * 2 + 23) % 24) + "–" + Pad((i * 2 + 1) % 24) }; if (hd[i] == '1') r.GoodHours.Add(h); else r.BadHours.Add(h); }
            foreach (var e in LUNAR_EVENTS) if (!lu.Leap && (int)e[0] == lu.Day && (int)e[1] == lu.Month) r.Events.Add(new CalEvent { Lunar = true, Name = (string)e[2] });
            if (!lu.Leap && lu.Month == 12 && lu.Day >= 29) { var n = Lunar.JdToDate(jd + 1); if (Lunar.Solar2Lunar(n[0], n[1], n[2]).Day == 1) r.Events.Add(new CalEvent { Lunar = true, Name = "Giao thừa (Trừ tịch)" }); }
            foreach (var e in SOLAR_EVENTS) if ((int)e[0] == d && (int)e[1] == m) r.Events.Add(new CalEvent { Lunar = false, Name = (string)e[2] });
            r.Day = d; r.Month = m; r.Year = y; r.Weekday = Lunar.Weekday(d, m, y); r.Jd = jd; r.Lunar = lu;
            r.DayCC = CC(dc, dh); r.MonthCC = CC(mc, mh); r.MonthLeap = lu.Leap; r.YearCC = CC(yc, yh);
            r.NapAmDay = Engine.NapAm(dc, dh); r.NapAmYear = Engine.NapAm(yc, yh);
            r.Tiet = TIET[tk]; r.TietStart = tk != tkPrev; r.Than = THAN[than]; r.HoangDao = THAN_GOOD[than] == 1;
            r.Truc = TRUC[(dh - tietMonthChi + 12) % 12]; r.HourZero = Lunar.CAN[hc0] + " " + Lunar.CHI[0];
            return r;
        }
        static List<Sixty> cache;
        public static List<Sixty> SixtyTable()
        {
            if (cache != null) return cache;
            var o = new List<Sixty>();
            for (int i = 0; i < 60; i++) { int c = i % 10, h = i % 12; var s = new Sixty { Idx = i, Name = CC(c, h), NapAm = Engine.NapAm(c, h) }; s.Element = Engine.ElementOf(s.NapAm); for (int y = 1804 + i; y <= 2100; y += 60) if (y >= 1840) s.Years.Add(y); o.Add(s); }
            return cache = o;
        }
        public static YearInfo Year(int y) { var s = SixtyTable()[((y - 1804) % 60 + 60) % 60]; return new YearInfo { Year = y, Name = s.Name, NapAm = s.NapAm, Element = s.Element, Age = DateTime.Now.Year - y + 1 }; }
    }

    // ============================ JSON tối giản ============================
    public static class Json
    {
        public static object Parse(string s) { if (s == null) return null; int p = 0; Ws(s, ref p); return Val(s, ref p); }
        public static Dictionary<string, object> Obj(object o) { return o as Dictionary<string, object>; }
        public static List<object> Arr(object o) { return o as List<object>; }
        public static string Str(object o, string def) { return o == null ? def : (o is string ? (string)o : Convert.ToString(o, CultureInfo.InvariantCulture)); }
        public static int Num(object o, int def) { if (o is long) return (int)(long)o; if (o is double) return (int)(double)o; if (o is int) return (int)o; int r; return o is string && int.TryParse((string)o, out r) ? r : def; }
        public static double Dbl(object o, double def) { if (o is long) return (long)o; if (o is double) return (double)o; if (o is int) return (int)o; return def; }
        public static bool Bool(object o, bool def) { return o is bool ? (bool)o : def; }
        static void Ws(string s, ref int p) { while (p < s.Length && s[p] <= ' ') p++; }
        static object Val(string s, ref int p)
        {
            if (p >= s.Length) return null; char c = s[p];
            if (c == '{')
            {
                p++; var m = new Dictionary<string, object>(); Ws(s, ref p);
                if (p < s.Length && s[p] == '}') { p++; return m; }
                while (p < s.Length) { Ws(s, ref p); string k = Str(s, ref p); Ws(s, ref p); p++; Ws(s, ref p); m[k] = Val(s, ref p); Ws(s, ref p); if (p < s.Length && s[p] == ',') { p++; continue; } p++; break; }
                return m;
            }
            if (c == '[')
            {
                p++; var l = new List<object>(); Ws(s, ref p);
                if (p < s.Length && s[p] == ']') { p++; return l; }
                while (p < s.Length) { Ws(s, ref p); l.Add(Val(s, ref p)); Ws(s, ref p); if (p < s.Length && s[p] == ',') { p++; continue; } p++; break; }
                return l;
            }
            if (c == '"') return Str(s, ref p);
            if (string.CompareOrdinal(s, p, "true", 0, 4) == 0) { p += 4; return true; }
            if (string.CompareOrdinal(s, p, "false", 0, 5) == 0) { p += 5; return false; }
            if (string.CompareOrdinal(s, p, "null", 0, 4) == 0) { p += 4; return null; }
            int st = p; bool fl = false;
            while (p < s.Length) { char d = s[p]; if ((d >= '0' && d <= '9') || d == '-' || d == '+') p++; else if (d == '.' || d == 'e' || d == 'E') { fl = true; p++; } else break; }
            string n = s.Substring(st, p - st); long lv; double dv;
            if (!fl && long.TryParse(n, NumberStyles.Integer, CultureInfo.InvariantCulture, out lv)) return lv;
            return double.TryParse(n, NumberStyles.Float, CultureInfo.InvariantCulture, out dv) ? dv : 0L;
        }
        static string Str(string s, ref int p)
        {
            var b = new StringBuilder(); p++;
            while (p < s.Length)
            {
                char c = s[p++]; if (c == '"') break;
                if (c == '\\' && p < s.Length)
                {
                    char e = s[p++];
                    switch (e) { case 'n': b.Append('\n'); break; case 't': b.Append('\t'); break; case 'r': b.Append('\r'); break; case 'b': b.Append('\b'); break; case 'f': b.Append('\f'); break; case 'u': b.Append((char)Convert.ToInt32(s.Substring(p, 4), 16)); p += 4; break; default: b.Append(e); break; }
                }
                else b.Append(c);
            }
            return b.ToString();
        }
        public static string Stringify(object o) { var b = new StringBuilder(); Write(b, o); return b.ToString(); }
        static void Write(StringBuilder b, object o)
        {
            if (o == null) b.Append("null");
            else if (o is string) Quote(b, (string)o);
            else if (o is bool) b.Append((bool)o ? "true" : "false");
            else if (o is double || o is float) { double d = Convert.ToDouble(o); if (d == Math.Round(d) && Math.Abs(d) < 1e15) b.Append(((long)d).ToString(CultureInfo.InvariantCulture)); else b.Append(d.ToString("R", CultureInfo.InvariantCulture)); }
            else if (o is int || o is long) b.Append(Convert.ToString(o, CultureInfo.InvariantCulture));
            else if (o is Dictionary<string, object>) { b.Append('{'); bool f = true; foreach (var kv in (Dictionary<string, object>)o) { if (!f) b.Append(','); f = false; Quote(b, kv.Key); b.Append(':'); Write(b, kv.Value); } b.Append('}'); }
            else if (o is System.Collections.IList) { b.Append('['); bool f = true; foreach (var x in (System.Collections.IList)o) { if (!f) b.Append(','); f = false; Write(b, x); } b.Append(']'); }
            else Quote(b, o.ToString());
        }
        public static void Quote(StringBuilder b, string s)
        {
            b.Append('"');
            foreach (char c in s) { switch (c) { case '"': b.Append("\\\""); break; case '\\': b.Append("\\\\"); break; case '\n': b.Append("\\n"); break; case '\r': b.Append("\\r"); break; case '\t': b.Append("\\t"); break; default: if (c < 0x20) b.Append("\\u" + ((int)c).ToString("x4")); else b.Append(c); break; } }
            b.Append('"');
        }
        public static Dictionary<string, object> Map(params object[] kv) { var m = new Dictionary<string, object>(); for (int i = 0; i + 1 < kv.Length; i += 2) m[(string)kv[i]] = kv[i + 1]; return m; }
    }

    // ============================ BỎ DẤU ============================
    public static class Fold
    {
        const string SRC = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
        const string DST = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";
        static readonly char[] MAP = new char[0x1F00];
        static Fold() { for (int i = 0; i < SRC.Length; i++) MAP[SRC[i]] = DST[i]; }
        public static string Do(string s)
        {
            if (s == null) return ""; string l = s.ToLowerInvariant(); var b = new StringBuilder(l.Length);
            foreach (char c in l) { if (c < MAP.Length && MAP[c] != 0) b.Append(MAP[c]); else if (c == '̀' || c == '́' || c == '̃' || c == '̉' || c == '̣') continue; else b.Append(c); }
            return b.ToString();
        }
    }
}
