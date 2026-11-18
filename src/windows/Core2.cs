// Applied Astrology v0.0.6 – lõi native C# (phần 2): Thư viện, Đa ngôn ngữ, Trợ lý luận giải, Bảng màu, Liquid Glass.
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace AppliedAstrology.Core
{
    public interface ISource { string Read(string path); }

    public sealed class Chapter { public string Id, Name; public int N, Count; public readonly List<string> Titles = new List<string>(); public readonly List<int> Lens = new List<int>(); }
    public sealed class Article { public string Ch, Title, Body; public int I; }
    public sealed class Hit { public string Ch, Title, Snippet, Why; public int I, Score; }

    /// <summary>Thư viện Bửu Đình: 611 bài / 13 chương, nạp lười theo chương, tìm kiếm có/không dấu, bài liên quan theo cung.</summary>
    public sealed class Library
    {
        public static readonly Dictionary<string, string> CH_KEY = new Dictionary<string, string> { { "chinh-tinh", "ch_chinh_tinh" }, { "can-tinh", "ch_can_tinh" }, { "chi-tinh", "ch_chi_tinh" }, { "sao-khac", "ch_sao_khac" }, { "cac-cung", "ch_cac_cung" }, { "72-cach", "ch_72_cach" }, { "an-sao", "ch_an_sao" }, { "han", "ch_han" }, { "la-so-nam", "ch_la_so_nam" }, { "la-so-nu", "ch_la_so_nu" }, { "tu-dien", "ch_tu_dien" }, { "2-chinh-tinh", "ch_2_chinh_tinh" }, { "cac-bo", "ch_cac_bo" } };
        readonly ISource src; public readonly List<Chapter> Chapters = new List<Chapter>();
        readonly Dictionary<string, Chapter> byId = new Dictionary<string, Chapter>();
        readonly Dictionary<string, List<object>> loaded = new Dictionary<string, List<object>>();
        readonly Dictionary<string, string> foldCache = new Dictionary<string, string>();
        public Library(ISource src)
        {
            this.src = src; var idx = Json.Obj(Json.Parse(src.Read("data/lib/index.json"))); if (idx == null) return;
            foreach (var o in Json.Arr(idx["chapters"]))
            {
                var m = Json.Obj(o); var c = new Chapter { Id = Json.Str(m["id"], ""), N = Json.Num(m["n"], 0), Name = Json.Str(m["name"], ""), Count = Json.Num(m["count"], 0) };
                foreach (var a in Json.Arr(m["articles"])) { var am = Json.Obj(a); c.Titles.Add(Json.Str(am["t"], "")); c.Lens.Add(Json.Num(am["len"], 0)); }
                Chapters.Add(c); byId[c.Id] = c;
            }
        }
        public Chapter Get(string id) { Chapter c; return id != null && byId.TryGetValue(id, out c) ? c : null; }
        public int TotalArticles() { return Chapters.Sum(c => c.Count); }
        public string Title(string id, int i) { var c = Get(id); return c != null && i >= 0 && i < c.Titles.Count ? c.Titles[i] : ""; }
        List<object> Load(string id)
        {
            lock (loaded)
            {
                List<object> a; if (loaded.TryGetValue(id, out a)) return a;
                var c = Get(id); if (c == null) return null;
                a = Json.Arr(Json.Parse(src.Read("data/lib/" + c.N.ToString("00") + ".json"))); if (a != null) loaded[id] = a; return a;
            }
        }
        public Article Art(string id, int i)
        {
            var a = Load(id); if (a == null || i < 0 || i >= a.Count) return null;
            var x = Json.Arr(a[i]); return new Article { Ch = id, I = i, Title = Json.Str(x[0], ""), Body = Json.Str(x[1], "") };
        }
        string Folded(string id, string k, string text)
        {
            string key = id + ":" + k, f; lock (foldCache) { if (foldCache.TryGetValue(key, out f)) return f; }
            f = Fold.Do(text); lock (foldCache) foldCache[key] = f; return f;
        }
        /// <summary>v0.0.6: nạp mọi chương và bỏ dấu sẵn (gọi ở luồng nền lúc khởi động) → tìm kiếm lần đầu nhanh như các lần sau</summary>
        public void Warm()
        {
            foreach (var c in Chapters.ToArray())
            {
                var arr = Load(c.Id); if (arr == null) continue;
                for (int k = 0; k < arr.Count; k++) { var x = Json.Arr(arr[k]); Folded(c.Id, "t" + k, Json.Str(x[0], "")); Folded(c.Id, k.ToString(), Json.Str(x[1], "")); }
            }
        }
        public List<Hit> Search(string q)
        {
            string fq = Regex.Replace(Fold.Do(q), "\\s+", " ").Trim(); var res = new List<Hit>(); if (fq.Length < 2) return res;
            var words = fq.Split(' ');
            foreach (var c in Chapters)
            {
                var arr = Load(c.Id); if (arr == null) continue;
                for (int k = 0; k < arr.Count; k++)
                {
                    var x = Json.Arr(arr[k]); string t = Json.Str(x[0], ""), body = Json.Str(x[1], "");
                    string ft = Folded(c.Id, "t" + k, t), fb = Folded(c.Id, k.ToString(), body);
                    int pos = fb.IndexOf(fq, StringComparison.Ordinal), score = 0;
                    if (ft.IndexOf(fq, StringComparison.Ordinal) >= 0) score += 50;
                    if (pos >= 0) { score += 10; int cnt = 0, p = pos; while (p >= 0 && cnt < 20) { cnt++; p = fb.IndexOf(fq, p + fq.Length, StringComparison.Ordinal); } score += cnt; }
                    else if (words.Length > 1) { bool all = words.All(w => fb.IndexOf(w, StringComparison.Ordinal) >= 0 || ft.IndexOf(w, StringComparison.Ordinal) >= 0); if (all) { score += 3; pos = fb.IndexOf(words[0], StringComparison.Ordinal); } }
                    if (score > 0) res.Add(new Hit { Ch = c.Id, I = k, Title = t, Score = score, Snippet = Snip(body, Math.Max(pos, 0), fq.Length) });
                }
            }
            return res.OrderByDescending(h => h.Score).ToList(); // OrderBy ổn định
        }
        static string Snip(string body, int pos, int len)
        {
            pos = Math.Min(pos, body.Length); int s = Math.Max(0, pos - 70), e = Math.Min(body.Length, pos + len + 110);
            return (s > 0 ? "…" : "") + Regex.Replace(body.Substring(s, e - s), "\\s+", " ") + (e < body.Length ? "…" : "");
        }
        static readonly Regex[] CHI_RX = new[] { "t[ýí]", "sửu", "dần", "mão", "thìn", "t[ịỵ]", "ngọ", "mùi", "thân", "dậu", "tuất", "hợi" }.Select(r => new Regex("(?:^|[\\s,.;:()\\-/])" + r + "(?:$|[\\s,.;:()\\-/])", RegexOptions.IgnoreCase)).ToArray();
        public List<Hit> Related(Chart chart, int chi)
        {
            var p = chart.Palaces[chi]; var outL = new List<Hit>(); var seen = new Dictionary<string, Hit>();
            Action<string, int, string, int> push = (ch, idx, why, score) => { string key = ch + ":" + idx; Hit h; if (seen.TryGetValue(key, out h)) { h.Score += score; return; } h = new Hit { Ch = ch, I = idx, Title = Title(ch, idx), Why = why, Score = score }; seen[key] = h; outL.Add(h); };
            var majors = p.Major.Select(s => s.Name).ToList(); var minors = p.Good.Concat(p.Bad).Concat(p.Neutral).Select(s => s.Name).ToList();
            string pname = Fold.Do(p.Name); var cc = Get("cac-cung");
            if (cc != null) for (int k = 0; k < cc.Titles.Count; k++) if (Fold.Do(cc.Titles[k]).Contains(pname)) push("cac-cung", k, p.Name, 30);
            foreach (var mj in majors)
            {
                string fs = Fold.Do(mj); var ct = Get("chinh-tinh");
                if (ct != null) for (int k = 0; k < ct.Titles.Count; k++) if (Fold.Do(ct.Titles[k]).Contains(fs)) push("chinh-tinh", k, mj, 20);
                var c72 = Get("72-cach");
                if (c72 != null) for (int k = 0; k < c72.Titles.Count; k++) { string t = c72.Titles[k]; if (Fold.Do(t).Contains(fs)) { int sc = 10; if (CHI_RX[chi].IsMatch(t)) sc += 40; push("72-cach", k, mj, sc); } }
            }
            if (majors.Count == 2)
            {
                string f1 = Fold.Do(majors[0]), f2 = Fold.Do(majors[1]); var c2 = Get("2-chinh-tinh");
                if (c2 != null) for (int k = 0; k < c2.Titles.Count; k++) { string ft = Fold.Do(c2.Titles[k]); if (ft.Contains(f1) && ft.Contains(f2)) push("2-chinh-tinh", k, majors[0] + " – " + majors[1], 45); }
            }
            if (majors.Count == 0) { var c72 = Get("72-cach"); if (c72 != null) for (int k = 0; k < c72.Titles.Count; k++) { string t = c72.Titles[k]; if (Fold.Do(t).Contains("vcd") && CHI_RX[chi].IsMatch(t)) push("72-cach", k, "VCD", 35); } }
            foreach (var mn in minors)
            {
                string fm = Fold.Do(mn); if (fm.Length < 5) continue;
                foreach (var g in new[] { "can-tinh", "chi-tinh", "sao-khac", "cac-bo" }) { var c = Get(g); if (c == null) continue; for (int k = 0; k < c.Titles.Count; k++) if (Fold.Do(c.Titles[k]).Contains(fm)) push(g, k, mn, 6); }
            }
            return outL.OrderByDescending(h => h.Score).Take(12).ToList();
        }
    }

    public sealed class Lang { public string Code, Name; public bool Rtl; }
    /// <summary>68 ngôn ngữ (Aniyomi/Nekoyomi moko-resources + Mangayomi l10n)</summary>
    public sealed class I18n
    {
        readonly ISource src; public readonly List<Lang> Langs = new List<Lang>(); readonly Dictionary<string, Dictionary<string, object>> dicts = new Dictionary<string, Dictionary<string, object>>();
        string cur = "vi";
        static readonly Dictionary<string, string> PALACE_KEY = new Dictionary<string, string> { { "Mệnh", "p_menh" }, { "Phụ Mẫu", "p_phumau" }, { "Phúc Đức", "p_phucduc" }, { "Điền Trạch", "p_dientrach" }, { "Quan Lộc", "p_quanloc" }, { "Nô Bộc", "p_nobOc" }, { "Thiên Di", "p_thiendi" }, { "Tật Ách", "p_tatach" }, { "Tài Bạch", "p_taibach" }, { "Tử Tức", "p_tutuc" }, { "Phu Thê", "p_phuthe" }, { "Huynh Đệ", "p_huynhde" } };
        public I18n(ISource src)
        {
            this.src = src; var l = Json.Arr(Json.Parse(src.Read("data/langs.json")));
            if (l != null) foreach (var o in l) { var m = Json.Obj(o); Langs.Add(new Lang { Code = Json.Str(m["code"], "en"), Name = Json.Str(m["name"], ""), Rtl = Json.Bool(m["rtl"], false) }); }
            D("vi"); D("en");
        }
        Dictionary<string, object> D(string code)
        {
            Dictionary<string, object> d; if (dicts.TryGetValue(code, out d)) return d;
            d = Json.Obj(Json.Parse(src.Read("data/lang/" + code + ".json"))) ?? new Dictionary<string, object>(); dicts[code] = d; return d;
        }
        public void SetLang(string code) { cur = Langs.Any(g => g.Code == code) ? code : "en"; D(cur); }
        public string Current { get { return cur; } }
        public bool Rtl { get { var g = Langs.FirstOrDefault(x => x.Code == cur); return g != null && g.Rtl; } }
        public string T(string key) { object v; if (D(cur).TryGetValue(key, out v) || D("en").TryGetValue(key, out v) || D("vi").TryGetValue(key, out v)) return Convert.ToString(v); return key; }
        public string T(string key, params object[] kv) { string s = T(key); for (int i = 0; i + 1 < kv.Length; i += 2) s = s.Replace("{" + kv[i] + "}", Convert.ToString(kv[i + 1])); return s; }
        public string TIn(string code, string key) { object v; return D(code).TryGetValue(key, out v) ? Convert.ToString(v) : null; }
        public string Palace(string vn) { string k; return PALACE_KEY.TryGetValue(vn, out k) ? T(k) : vn; }
        public string ChapterName(Library lib, string id) { string k; if (Library.CH_KEY.TryGetValue(id, out k)) return T(k); var c = lib.Get(id); return c != null ? c.Name : id; }
    }

    public sealed class Answer { public string Text; public readonly List<Hit> Refs = new List<Hit>(); }
    /// <summary>Trợ lý luận giải ngoại tuyến (không phải AI tạo sinh) – bản dịch 1:1 từ Assistant.java / assistant.js</summary>
    public sealed class Assistant
    {
        readonly Library lib; readonly I18n i18n; public Assistant(Library l, I18n i) { lib = l; i18n = i; }
        static readonly string[][] TOPICS = { new[] { "Quan Lộc", "cong danh", "su nghiep", "nghe", "viec lam", "quan loc", "career", "job", "work" }, new[] { "Tài Bạch", "tai loc", "tien", "tai bach", "giau", "kinh doanh", "money", "wealth", "finance" }, new[] { "Phu Thê", "vo", "chong", "hon nhan", "tinh duyen", "phu the", "love", "marriage", "spouse" }, new[] { "Tử Tức", "con cai", "tu tuc", "children" }, new[] { "Tật Ách", "suc khoe", "benh", "tat ach", "health" }, new[] { "Phụ Mẫu", "cha me", "bo me", "phu mau", "parents" }, new[] { "Huynh Đệ", "anh em", "huynh de", "siblings" }, new[] { "Điền Trạch", "nha cua", "dat dai", "dien trach", "property", "house" }, new[] { "Nô Bộc", "ban be", "dong nghiep", "no boc", "friends" }, new[] { "Thiên Di", "di xa", "xuat ngoai", "thien di", "travel", "abroad" }, new[] { "Phúc Đức", "phuc duc", "ho hang", "mo ma", "fortune", "ancestors" }, new[] { "Mệnh", "menh", "tinh cach", "ban than", "life", "personality" } };
        static readonly string[] YEAR_KEYS = { "nam nay", "han", "van han", "tieu han", "dai han", "this year", "forecast", "luu nien" };
        bool Vi { get { return i18n.Current == "vi"; } }
        string L(string v, string e) { return Vi ? v : e; }
        static double Br(string b) { return b == "M" || b == "V" ? 2 : b == "Đ" ? 1 : b == "H" ? -2 : 0; }
        public static double Assess(Chart c, int chi)
        {
            var p = c.Palaces[chi]; double s = 0; var tp = c.TamPhuong(chi);
            foreach (var m in p.Major) s += Br(m.Bright); if (p.Major.Count == 0) s -= 1;
            s += p.Good.Count * 0.35 - p.Bad.Count * 0.45;
            foreach (var h in p.Hoa) s += h[0] == "Hóa Kỵ" ? -1.5 : 1;
            if (p.Tuan) s -= 0.5; if (p.Triet) s -= 0.8;
            for (int j = 1; j < 4; j++) foreach (var h in c.Palaces[tp[j]].Hoa) s += h[0] == "Hóa Kỵ" ? -0.5 : 0.4;
            return s;
        }
        string Verdict(double s) { return s >= 3 ? L("Khá thuận lợi", "Quite favourable") : s >= 0.5 ? L("Trung bình khá", "Moderately favourable") : s > -2 ? L("Bình thường, cần nỗ lực", "Average, effort needed") : L("Nhiều thử thách", "Challenging"); }
        public string DescribePalace(Chart c, int chi)
        {
            var p = c.Palaces[chi]; var o = new List<string>(); var ms = p.Major.Select(m => m.Name + " (" + m.Bright + ")").ToList();
            o.Add("• " + L("Cung ", "Palace ") + i18n.Palace(p.Name) + " " + L("tại ", "at ") + p.CanChi + ": " + (ms.Count == 0 ? L("Vô Chính Diệu – mượn chính tinh cung xung chiếu ", "no major star – borrows from the opposite palace ") + c.Palaces[(chi + 6) % 12].ChiName : string.Join(", ", ms)));
            if (p.Good.Count > 0) o.Add("  " + L("Cát tinh: ", "Auspicious: ") + string.Join(", ", p.Good.Select(s => s.Name)));
            if (p.Bad.Count > 0) o.Add("  " + L("Sát/bại tinh: ", "Malefic: ") + string.Join(", ", p.Bad.Select(s => s.Name)));
            foreach (var h in p.Hoa) o.Add("  " + h[0] + " " + L("hóa từ ", "transforms ") + h[1]);
            if (p.Tuan || p.Triet) o.Add("  " + L("Gặp ", "Has ") + (p.Tuan ? "Tuần " : "") + (p.Triet ? "Triệt" : "") + L(" – tốt xấu đều giảm bớt, thường chậm trước nhanh sau.", " – effects are dampened."));
            var tp = c.TamPhuong(chi); var tps = new List<string>();
            for (int k = 1; k < 4; k++) { var q = c.Palaces[tp[k]]; var mq = q.Major.Select(s => s.Name).ToList(); tps.Add(i18n.Palace(q.Name) + ": " + (mq.Count == 0 ? "VCD" : string.Join(", ", mq))); }
            o.Add("  " + L("Tam phương tứ chính – ", "Triad – ") + string.Join("; ", tps));
            o.Add("  " + L("Nhận định sơ bộ: ", "Preliminary assessment: ") + Verdict(Assess(c, chi)) + L(" (theo độ sáng chính tinh, cát/sát tinh, Tứ Hóa, Tuần Triệt).", " (from star brightness, auspicious/malefic stars, transformations, voids)."));
            return string.Join("\n", o);
        }
        public string Overview(Chart c)
        {
            var o = new List<string>();
            o.Add(L("Lá số của ", "Chart of ") + c.Name + ": " + c.AmDuong + ", " + L("năm ", "year ") + c.YearCanChi + ", " + L("bản mệnh ", "element ") + c.NapAm + ", " + c.CucName + ".");
            o.Add(c.MenhCuc == "sinh" ? L("Mệnh sinh Cục: người tận tụy, cống hiến cho hoàn cảnh.", "Life element produces the bureau: devoted, giving.") : c.MenhCuc == "được sinh" ? L("Cục sinh Mệnh: được hoàn cảnh nâng đỡ, thời thế ưu ái.", "Bureau produces the life element: supported by circumstances.") : c.MenhCuc == "khắc" ? L("Mệnh khắc Cục: phải tự lực chinh phục hoàn cảnh.", "Life element controls the bureau: must conquer circumstances.") : c.MenhCuc == "bị khắc" ? L("Cục khắc Mệnh: hoàn cảnh gây sức ép, cần kiên nhẫn.", "Bureau controls the life element: pressure from circumstances.") : L("Mệnh Cục bình hòa: cân bằng với môi trường.", "Life and bureau in harmony."));
            o.Add(c.AmDuongLy + ". " + L("Mệnh chủ ", "Life lord ") + c.MenhChu + ", " + L("Thân chủ ", "body lord ") + c.ThanChu + ".");
            o.Add(DescribePalace(c, c.Menh));
            if (c.Than != c.Menh) o.Add(L("Thân cư ", "Body palace in ") + i18n.Palace(c.Palaces[c.Than].Name) + ":\n" + DescribePalace(c, c.Than));
            if (c.CurrentDaiHan >= 0) o.Add(L("Đại hạn hiện tại (tuổi ", "Current decade (age ") + c.Age + "):\n" + DescribePalace(c, c.CurrentDaiHan));
            return string.Join("\n\n", o);
        }
        public Answer Ask(Chart c, string question)
        {
            var ans = new Answer(); if (c == null) { ans.Text = i18n.T("as_need_chart"); return ans; }
            string fq = " " + Regex.Replace(Fold.Do(question), "[^a-z0-9]+", " ") + " "; int chi = -1; var text = new List<string>();
            bool isYear = YEAR_KEYS.Any(k => fq.Contains(" " + k + " "));
            foreach (var tp in TOPICS) { if (chi >= 0) break; for (int j = 1; j < tp.Length; j++) if (fq.Contains(" " + tp[j] + " ")) { chi = Array.FindIndex(c.Palaces, p => p.Name == tp[0]); if (chi < 0) chi = c.Menh; break; } }
            if (isYear)
            {
                int th = c.CurrentTieuHan, dh = c.CurrentDaiHan;
                text.Add(L("Hạn năm ", "Forecast for ") + c.ViewYear + " (" + c.ViewYearCanChi + "), " + L("tuổi âm ", "lunar age ") + c.Age + ".");
                if (dh >= 0) text.Add(L("Đại hạn đang đi qua cung ", "Decade runs through ") + i18n.Palace(c.Palaces[dh].Name) + ":\n" + DescribePalace(c, dh));
                if (th >= 0) text.Add(L("Tiểu hạn tại cung ", "Annual cycle at ") + i18n.Palace(c.Palaces[th].Name) + ":\n" + DescribePalace(c, th));
                text.Add(L("Sao lưu niên: ", "Annual stars: ") + string.Join(" | ", c.Palaces.Where(p => p.Luu.Count > 0).Select(p => i18n.Palace(p.Name) + ": " + string.Join(", ", p.Luu))));
                if (chi < 0) chi = th >= 0 ? th : c.Menh;
            }
            else { if (chi < 0) chi = c.Menh; text.Add(DescribePalace(c, chi)); }
            var rel = lib.Related(c, chi).Take(6).ToList(); var p0 = c.Palaces[chi]; var keys = new List<string> { Fold.Do(p0.Name) }; keys.AddRange(p0.Major.Select(s => Fold.Do(s.Name)));
            var found = new List<Tuple<Hit, string, double>>();
            foreach (var r in rel)
            {
                var a = lib.Art(r.Ch, r.I); if (a == null) continue; string best = null; int bestS = 0;
                foreach (var para in Regex.Split(a.Body, "\\n\\s*\\n|\\n"))
                {
                    if (para.Length < 60) continue; string fp = Fold.Do(para); int s = 0;
                    for (int q = 0; q < keys.Count; q++) if (fp.Contains(keys[q])) s += q == 0 ? 2 : 3;
                    if (s > bestS) { bestS = s; best = para; }
                }
                if (best != null) { r.Title = a.Title; found.Add(Tuple.Create(r, best.Length > 600 ? best.Substring(0, 600) + "…" : best, bestS + r.Score / 10.0)); }
            }
            var qs = found.OrderByDescending(x => x.Item3).Take(3).ToList();
            if (qs.Count > 0) { text.Add(L("Trích bình giải Bửu Đình:", "From Bửu Đình's commentary (original Vietnamese):")); foreach (var q in qs) { text.Add("« " + q.Item2 + " »\n— " + q.Item1.Title); ans.Refs.Add(q.Item1); } }
            text.Add(L("Lưu ý: đây là phân tích tham khảo theo nguyên tắc Tử Vi, mang tính ứng dụng – \"Tử Vi là để hiểu mình, không phải để ỷ lại số mệnh\".", "Note: reference analysis only."));
            ans.Text = string.Join("\n\n", text); return ans;
        }
    }

    /// <summary>Bảng màu 4 giao diện + ngũ hành + 6 giấy lót nền SWF (ARGB)</summary>
    public sealed class Theme
    {
        public readonly string Id; public readonly bool Dark; public readonly int Bg, Bg2, Card, Card2, Line, Gold, Gold2, Text, Muted, Accent, Purple, Good, Bad, Sel;
        Theme(string id, bool dark, params int[] c) { Id = id; Dark = dark; Bg = c[0]; Bg2 = c[1]; Card = c[2]; Card2 = c[3]; Line = c[4]; Gold = c[5]; Gold2 = c[6]; Text = c[7]; Muted = c[8]; Accent = c[9]; Purple = c[10]; Good = c[11]; Bad = c[12]; Sel = c[13]; }
        static int H(int rgb) { return unchecked((int)0xFF000000) | rgb; }
        public static readonly Theme CELESTIAL = new Theme("celestial-dark", true, H(0x080b16), H(0x0e1428), H(0x141b33), H(0x1b2442), H(0x2c3763), H(0xf2b84b), H(0xffe7a3), H(0xeef2fb), H(0x9aa6c4), H(0x7dd3fc), H(0xa78bfa), H(0x34d399), H(0xfb7185), H(0x2a2160));
        public static readonly Theme GOLD = new Theme("royal-gold", true, H(0x140f06), H(0x21190a), H(0x2a2010), H(0x372a14), H(0x5c4720), H(0xffcf4d), H(0xfff1c2), H(0xfff8e6), H(0xd7b779), H(0xffd98a), H(0xe3b5ff), H(0x9be27a), H(0xff8a7a), H(0x4a3712));
        public static readonly Theme IVORY = new Theme("ivory-light", false, H(0xf6f3ec), H(0xece6d8), H(0xffffff), H(0xfaf7f0), H(0xd9cdb2), H(0x9a6a12), H(0x6b4708), H(0x1f2333), H(0x6b6f80), H(0x0e6fa8), H(0x6d4fc2), H(0x0f8a5f), H(0xc0283d), H(0xf3e6c4));
        public static readonly Theme RED = new Theme("imperial-red", true, H(0x170709), H(0x250b10), H(0x311017), H(0x40151e), H(0x6d2733), H(0xf6b24a), H(0xffe0a6), H(0xfff1f2), H(0xe7a3ad), H(0xffc58f), H(0xf0a8ff), H(0x7ee0a8), H(0xff7b8f), H(0x5a1a25));
        public static readonly Theme[] ALL = { CELESTIAL, GOLD, IVORY, RED };
        public static Theme ById(string id) { return ALL.FirstOrDefault(t => t.Id == id) ?? CELESTIAL; }
        public int Element(string e)
        {
            if (Dark) return e == "Kim" ? H(0xe5e7eb) : e == "Mộc" ? H(0x4ade80) : e == "Thủy" ? H(0x60a5fa) : e == "Hỏa" ? H(0xf87171) : e == "Thổ" ? H(0xfbbf24) : Text;
            return e == "Kim" ? H(0x6b7280) : e == "Mộc" ? H(0x15803d) : e == "Thủy" ? H(0x1d4ed8) : e == "Hỏa" ? H(0xb91c1c) : e == "Thổ" ? H(0xa16207) : Text;
        }
        public int Hoa(string n) { return n == "Hóa Lộc" ? Good : n == "Hóa Quyền" ? Gold : n == "Hóa Khoa" ? Accent : n == "Hóa Kỵ" ? Bad : Text; }
        public static readonly string[] PAPERS = { "none", "vangkyxao", "vangchanh", "xanhnhat", "lucnhat", "timnhat", "vang" };
        public static int[] Paper(string id)
        {
            switch (id) { case "vangkyxao": return new[] { H(0xf3e2b0), H(0x3a2a08) }; case "vangchanh": return new[] { H(0xf7f1a8), H(0x2f2a05) }; case "xanhnhat": return new[] { H(0xd9ecf7), H(0x0d2a3a) }; case "lucnhat": return new[] { H(0xdcefd8), H(0x12301a) }; case "timnhat": return new[] { H(0xe8dcf3), H(0x2a1440) }; case "vang": return new[] { H(0xf6d77a), H(0x3a2804) }; }
            return null;
        }
        public static int Alpha(int c, int a) { return (c & 0x00FFFFFF) | ((a & 0xFF) << 24); }
        public static int Mix(int c1, int c2, double t)
        {
            int r = (int)(((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t), g = (int)(((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t), b = (int)((c1 & 255) * (1 - t) + (c2 & 255) * t);
            return unchecked((int)0xFF000000) | (r << 16) | (g << 8) | b;
        }
    }

    /// <summary>Liquid Glass – bản dịch 1:1 của Glass.java (archisvaze/liquid-glass, rdev/liquid-glass-react, dashersw/liquid-glass-js, gist GLSL)</summary>
    public static class Glass
    {
        public sealed class Params { public double Blur = 18, Refraction = 0.6, Bezel = 18, Thickness = 40, Ior = 1.5, Tint = 0.35, Specular = 0.55, Saturation = 1.35; public string Key() { return Blur + "/" + Refraction + "/" + Bezel + "/" + Thickness + "/" + Ior + "/" + Tint + "/" + Specular + "/" + Saturation; } }
        static double Surface(double x) { return Math.Pow(1 - Math.Pow(1 - x, 4), 0.25); }
        public static double[] Profile(double thickness, double bezel, double ior, int samples)
        {
            double eta = 1 / ior; var prof = new double[samples];
            for (int i = 0; i < samples; i++)
            {
                double x = (double)i / samples, y = Surface(x), dx = x < 1 ? 0.0001 : -0.0001, deriv = (Surface(x + dx) - y) / dx, mag = Math.Sqrt(deriv * deriv + 1);
                double nx = -deriv / mag, ny = -1 / mag, dot = ny, k = 1 - eta * eta * (1 - dot * dot);
                if (k < 0) { prof[i] = 0; continue; }
                double sq = Math.Sqrt(k), rx = -(eta * dot + sq) * nx, ry = eta - (eta * dot + sq) * ny; prof[i] = rx * ((y * bezel + thickness) / ry);
            }
            return prof;
        }
        /// <summary>v0.0.5: bản đồ khúc xạ THƯA (chỉ dải viền vát), bản dịch 1:1 của Glass.java. RowL[y] = độ rộng dải viền bên trái hàng y.</summary>
        public sealed class Maps { public int W, H, Radius, N; public int[] Idx, RowL; public float[] Dx, Dy, Mask, Spec; public double MaxDisp; }
        static readonly Dictionary<string, Maps> cache = new Dictionary<string, Maps>(); static readonly LinkedList<string> order = new LinkedList<string>();
        static double Sdf(double px, double py, int w, int h, int r)
        {
            double qx = Math.Abs(px - w / 2.0) - (w / 2.0 - r), qy = Math.Abs(py - h / 2.0) - (h / 2.0 - r);
            double ox = Math.Max(qx, 0), oy = Math.Max(qy, 0); return Math.Sqrt(ox * ox + oy * oy) + Math.Min(Math.Max(qx, qy), 0) - r;
        }
        public static Maps GetMaps(int w, int h, int radius, Params p)
        {
            string key = w + "x" + h + "r" + radius + "/" + p.Bezel + "/" + p.Thickness + "/" + p.Ior;
            lock (cache)
            {
                Maps m; if (cache.TryGetValue(key, out m)) return m;
                int r = Math.Max(1, Math.Min(radius, Math.Min(w, h) / 2)); double bezel = Math.Max(1, Math.Min(p.Bezel, Math.Min(w, h) / 2.0 - 1));
                var prof = Profile(p.Thickness, bezel, p.Ior, 128); double maxDisp = prof.Max(v => Math.Abs(v)); if (maxDisp == 0) maxDisp = 1;
                m = new Maps { W = w, H = h, Radius = r, MaxDisp = maxDisp, RowL = new int[h] };
                int total = 0, half = (w + 1) / 2;
                for (int y = 0; y < h; y++) { int xl = 0; while (xl < half && Sdf(xl + 0.5, y + 0.5, w, h, r) > -bezel) xl++; m.RowL[y] = xl; total += xl >= half ? w : 2 * xl; }
                m.Idx = new int[total]; m.Dx = new float[total]; m.Dy = new float[total]; m.Mask = new float[total]; m.Spec = new float[total];
                double ang = Math.PI / 3, sx = Math.Cos(ang), sy = Math.Sin(ang); int S = prof.Length, n = 0;
                for (int y1 = 0; y1 < h; y1++)
                {
                    int xl = m.RowL[y1];
                    for (int x1 = 0; x1 < w; x1++)
                    {
                        if (x1 == xl && xl < half) { x1 = w - xl - 1; continue; }
                        double qx = Math.Abs(x1 + 0.5 - w / 2.0) - (w / 2.0 - r), qy = Math.Abs(y1 + 0.5 - h / 2.0) - (h / 2.0 - r);
                        double ox = Math.Max(qx, 0), oy = Math.Max(qy, 0), sd = Math.Sqrt(ox * ox + oy * oy) + Math.Min(Math.Max(qx, qy), 0) - r;
                        double a = sd >= 0.5 ? 0 : sd <= -0.5 ? 1 : 0.5 - sd; int i = n++; m.Idx[i] = y1 * w + x1; m.Mask[i] = (float)a; if (a <= 0) continue;
                        double fromSide = -sd; if (fromSide >= bezel) continue;
                        double gx, gy;
                        if (qx > 0 && qy > 0) { double l = Math.Sqrt(qx * qx + qy * qy); gx = qx / l; gy = qy / l; } else if (qx > qy) { gx = 1; gy = 0; } else { gx = 0; gy = 1; }
                        if (x1 + 0.5 < w / 2.0) gx = -gx; if (y1 + 0.5 < h / 2.0) gy = -gy;
                        int bi = Math.Min((int)((fromSide / bezel) * S), S - 1); if (bi < 0) bi = 0;
                        double disp = prof[bi] / maxDisp; m.Dx[i] = (float)(-gx * disp * a); m.Dy[i] = (float)(-gy * disp * a);
                        double dot = Math.Abs(gx * sx - gy * sy), rim = Math.Exp(-fromSide / 1.4), bevel = Math.Exp(-fromSide / (bezel * 0.45)) * 0.28;
                        m.Spec[i] = (float)(Math.Min(1, Math.Pow(dot, 1.3) * (rim + bevel)) * a);
                    }
                }
                m.N = n;
                cache[key] = m; order.AddLast(key); if (order.Count > 12) { cache.Remove(order.First.Value); order.RemoveFirst(); }
                return m;
            }
        }
        public static void BoxBlur(int[] px, int w, int h, int radius)
        {
            if (radius < 1) return; var tmp = new int[px.Length];
            for (int pass = 0; pass < 3; pass++) { BlurH(px, tmp, w, h, radius); var t = Transpose(tmp, w, h); BlurH(t, px, h, w, radius); var t2 = Transpose(px, h, w); Array.Copy(t2, px, px.Length); }
        }
        static int[] Transpose(int[] a, int w, int h) { var o = new int[a.Length]; for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) o[x * h + y] = a[y * w + x]; return o; }
        static void BlurH(int[] inp, int[] outp, int w, int h, int r)
        {
            int div = r * 2 + 1;
            for (int y = 0; y < h; y++)
            {
                int row = y * w, sr = 0, sg = 0, sb = 0;
                for (int k = -r; k <= r; k++) { int c = inp[row + Math.Min(w - 1, Math.Max(0, k))]; sr += (c >> 16) & 255; sg += (c >> 8) & 255; sb += c & 255; }
                for (int x = 0; x < w; x++)
                {
                    outp[row + x] = unchecked((int)0xFF000000) | ((sr / div) << 16) | ((sg / div) << 8) | (sb / div);
                    int cOut = inp[row + Math.Max(0, x - r)], cIn = inp[row + Math.Min(w - 1, x + r + 1)];
                    sr += ((cIn >> 16) & 255) - ((cOut >> 16) & 255); sg += ((cIn >> 8) & 255) - ((cOut >> 8) & 255); sb += (cIn & 255) - (cOut & 255);
                }
            }
        }
        static int Clamp(double v) { return v < 0 ? 0 : v > 255 ? 255 : (int)v; }
        static int Shade(int[] bl, int bw, int bh, double fx, double fy, double sat, double ta, int tr, int tg, int tb, double s, double a)
        {
            if (fx < 0) fx = 0; else if (fx > bw - 1) fx = bw - 1; if (fy < 0) fy = 0; else if (fy > bh - 1) fy = bh - 1;
            int x0 = (int)fx, y0 = (int)fy, x1 = x0 + 1 < bw ? x0 + 1 : x0, y1 = y0 + 1 < bh ? y0 + 1 : y0; double tx = fx - x0, ty = fy - y0;
            int c00 = bl[y0 * bw + x0], c10 = bl[y0 * bw + x1], c01 = bl[y1 * bw + x0], c11 = bl[y1 * bw + x1];
            double w00 = (1 - tx) * (1 - ty), w10 = tx * (1 - ty), w01 = (1 - tx) * ty, w11 = tx * ty;
            double r = ((c00 >> 16) & 255) * w00 + ((c10 >> 16) & 255) * w10 + ((c01 >> 16) & 255) * w01 + ((c11 >> 16) & 255) * w11;
            double g = ((c00 >> 8) & 255) * w00 + ((c10 >> 8) & 255) * w10 + ((c01 >> 8) & 255) * w01 + ((c11 >> 8) & 255) * w11;
            double b = (c00 & 255) * w00 + (c10 & 255) * w10 + (c01 & 255) * w01 + (c11 & 255) * w11;
            double l = 0.2126 * r + 0.7152 * g + 0.0722 * b;
            r = l + (r - l) * sat; g = l + (g - l) * sat; b = l + (b - l) * sat;
            r = r * (1 - ta) + tr * ta; g = g * (1 - ta) + tg * ta; b = b * (1 - ta) + tb * ta;
            r += (255 - r) * s; g += (255 - g) * s; b += (255 - b) * s;
            int A = (int)(a * 255 + 0.5); return (A << 24) | (Clamp(r) << 16) | (Clamp(g) << 8) | Clamp(b);
        }
        /// <summary>Dựng toàn bộ tấm kính (lõi + dải viền)</summary>
        public static void Render(int[] blurred, int bw, int bh, double scale, int ox, int oy, Maps m, Params p, int tint, int[] outp)
        {
            int tr = (tint >> 16) & 255, tg = (tint >> 8) & 255, tb = tint & 255; double ta = p.Tint, sat = p.Saturation; int w = m.W, half = (w + 1) / 2;
            for (int y = 0; y < m.H; y++)
            {
                int xl = m.RowL[y]; if (xl >= half) continue; double fy = (oy + y + 0.5) / scale - 0.5;
                for (int x = xl; x < w - xl; x++) outp[y * w + x] = Shade(blurred, bw, bh, (ox + x + 0.5) / scale - 0.5, fy, sat, ta, tr, tg, tb, 0, 1);
            }
            RenderBand(blurred, bw, bh, scale, ox, oy, m, p, tint, outp);
        }
        /// <summary>Chỉ dựng dải viền vát khúc xạ</summary>
        public static void RenderBand(int[] blurred, int bw, int bh, double scale, int ox, int oy, Maps m, Params p, int tint, int[] outp)
        {
            double amp = m.MaxDisp * p.Refraction; int tr = (tint >> 16) & 255, tg = (tint >> 8) & 255, tb = tint & 255; double ta = p.Tint, sat = p.Saturation, spec = p.Specular; int w = m.W;
            for (int i = 0; i < m.N; i++)
            {
                int k = m.Idx[i]; float a = m.Mask[i]; if (a <= 0) { outp[k] = 0; continue; }
                int x = k % w, y = k / w;
                outp[k] = Shade(blurred, bw, bh, (ox + x + 0.5 + m.Dx[i] * amp) / scale - 0.5, (oy + y + 0.5 + m.Dy[i] * amp) / scale - 0.5, sat, ta, tr, tg, tb, m.Spec[i] * spec, a);
            }
        }
        static readonly double EDGE = Math.Exp(-1.6 * 2.2);
        public static int[] Nebula(int w, int h, Theme th, string style, long seed)
        {
            var px = new int[w * h];
            if (style == "plain") { for (int i = 0; i < px.Length; i++) px[i] = Theme.Mix(th.Bg, th.Bg2, (double)(i / w) / h); return px; }
            bool aurora = style == "aurora";
            double[][] blobs = aurora ? new[] { new[] { 0.15, 0.25, 0.55, 0 }, new[] { 0.85, 0.15, 0.45, 2 }, new[] { 0.5, 0.75, 0.6, 1 }, new[] { 0.1, 0.9, 0.4, 2 } } : new[] { new[] { 0.18, 0.18, 0.42, 0 }, new[] { 0.82, 0.32, 0.38, 1 }, new[] { 0.35, 0.82, 0.46, 2 }, new[] { 0.9, 0.9, 0.3, 0 } };
            int[] cols = { th.Purple, th.Gold, th.Accent }; double inten = th.Dark ? 0.55 : 0.35; int b2 = th.Bg2, bs = th.Bg;
            for (int y = 0; y < h; y++)
            {
                double fy = (double)y / h;
                for (int x = 0; x < w; x++)
                {
                    double fx = (double)x / w, r = (bs >> 16) & 255, g = (bs >> 8) & 255, b = bs & 255, grad = fy * 0.25;
                    r = r * (1 - grad) + ((b2 >> 16) & 255) * grad; g = g * (1 - grad) + ((b2 >> 8) & 255) * grad; b = b * (1 - grad) + (b2 & 255) * grad;
                    foreach (var bl in blobs)
                    {
                        double ddx = (fx - bl[0]) * (w > h ? (double)w / h : 1), ddy = (fy - bl[1]) * (h > w ? (double)h / w : 1), d2 = (ddx * ddx + ddy * ddy) / (bl[2] * bl[2]);
                        if (d2 > 1.6) continue; double k = (Math.Exp(-d2 * 2.2) - EDGE) / (1 - EDGE) * inten; int cc = cols[(int)bl[3]];
                        r += (((cc >> 16) & 255) - r) * k; g += (((cc >> 8) & 255) - g) * k; b += ((cc & 255) - b) * k;
                    }
                    px[y * w + x] = unchecked((int)0xFF000000) | (Clamp(r) << 16) | (Clamp(g) << 8) | Clamp(b);
                }
            }
            ulong s = (ulong)seed; int n = w * h / 2600;
            for (int k = 0; k < n; k++)
            {
                s = s * 6364136223846793005UL + 1442695040888963407UL; int x = (int)((s >> 33) % (ulong)w);
                s = s * 6364136223846793005UL + 1442695040888963407UL; int y = (int)((s >> 33) % (ulong)h);
                s = s * 6364136223846793005UL + 1442695040888963407UL; int br = 120 + (int)((s >> 33) % 135UL);
                int c = th.Dark ? (unchecked((int)0xFF000000) | (br << 16) | (br << 8) | Math.Min(255, br + 20)) : Theme.Mix(th.Bg, th.Gold, br / 400.0);
                px[y * w + x] = c; if (br > 230 && x + 1 < w && y + 1 < h) { px[y * w + x + 1] = c; px[(y + 1) * w + x] = c; }
            }
            return px;
        }
    }
}
