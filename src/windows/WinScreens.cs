// Applied Astrology v0.0.5 – các màn hình native Windows: Lá Số, Âm Lịch, Thư Viện, Luận Giải, Hồ Sơ, Cài Đặt (+ Liquid Glass), Giới Thiệu.
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Printing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using AppliedAstrology.Core;

namespace AppliedAstrology
{
    partial class MainForm
    {
        internal int CalY, CalM, CalD; internal string LibMode = "home", LastQuery = "", PrevMode = "chapter"; internal List<Hit> LastResults;
        internal readonly List<object[]> ChatLog = new List<object[]>();
        internal TextBox FName, FYear, FView; internal ComboBox FGender, FCal, FDay, FMonth, FHour; internal CheckBox FLeap; internal Box DetailCard;
        static readonly string[] CHI_HOURS = { "23–01", "01–03", "03–05", "05–07", "07–09", "09–11", "11–13", "13–15", "15–17", "17–19", "19–21", "21–23" };
        string T(string k) { return I18n.T(k); }
        string T(string k, params object[] kv) { return I18n.T(k, kv); }

        void BuildScreen(string v, Box p)
        {
            switch (v)
            {
                case "calendar": CalendarScreen(p); break; case "library": LibraryScreen(p); break; case "assistant": AssistantScreen(p); break;
                case "saved": SavedScreen(p); break; case "settings": SettingsScreen(p); break; case "about": AboutScreen(p); break; default: ChartScreen(p); break;
            }
        }

        // =============================== LÁ SỐ ===============================
        internal static ChartInput InputFrom(Dictionary<string, object> o)
        {
            return new ChartInput { Name = Json.Str(o.ContainsKey("name") ? o["name"] : null, "Thân Chủ"), Gender = Json.Str(o.ContainsKey("gender") ? o["gender"] : null, "Nam"), Calendar = Json.Str(o.ContainsKey("calendar") ? o["calendar"] : null, "solar"), Day = Json.Num(o.ContainsKey("day") ? o["day"] : null, 1), Month = Json.Num(o.ContainsKey("month") ? o["month"] : null, 1), Year = Json.Num(o.ContainsKey("year") ? o["year"] : null, 1990), HourChi = Json.Num(o.ContainsKey("hourChi") ? o["hourChi"] : null, 0), Leap = Json.Bool(o.ContainsKey("leap") ? o["leap"] : null, false) };
        }
        internal static Dictionary<string, object> InputMap(ChartInput i) { return Json.Map("name", i.Name, "gender", i.Gender, "calendar", i.Calendar, "day", (long)i.Day, "month", (long)i.Month, "year", (long)i.Year, "hourChi", (long)i.HourChi, "leap", i.Leap); }
        internal ChartInput WithMode(ChartInput i) { i.LeapMode = S("leap") ?? "same"; return i; }
        static IEnumerable<string> Nums(int a, int b) { for (int i = a; i <= b; i++) yield return i.ToString(); }

        void ChartScreen(Box p)
        {
            var fc = Card(p, "formCard"); var fl = new Box { Direction = Box.Dir.Wrap, Gap = D(8) };
            FName = Edit(Form0.Name, false, "fName"); fl.Add(Field(T("f_name"), FName, 220));
            FGender = Combo(new[] { T("male"), T("female") }, Form0.Gender == "Nữ" ? 1 : 0, null, "fGender"); fl.Add(Field(T("f_gender"), FGender, 104));
            FCal = Combo(new[] { T("solar"), T("lunar") }, Form0.Calendar == "lunar" ? 1 : 0, i => { Box.SetShown(FLeap, i == 1); Root.PerformLayout(); LayoutRoot(); }, "fCal"); fl.Add(Field(T("f_calendar"), FCal, 130));
            FDay = Combo(Nums(1, 31), Form0.Day - 1, null, "fDay"); fl.Add(Field(T("f_day"), FDay, 70));
            FMonth = Combo(Nums(1, 12), Form0.Month - 1, null, "fMonth"); fl.Add(Field(T("f_month"), FMonth, 70));
            FYear = Edit(Form0.Year.ToString(), true, "fYear"); fl.Add(Field(T("f_year"), FYear, 84));
            FHour = Combo(Enumerable.Range(0, 12).Select(i => Engine.CHI[i] + " (" + CHI_HOURS[i] + ")"), Form0.HourChi, null, "fHour"); fl.Add(Field(T("f_hour"), FHour, 150));
            FView = Edit((Form0.ViewYear > 0 ? Form0.ViewYear : DateTime.Now.Year).ToString(), true, "fView"); fl.Add(Field(T("f_viewyear"), FView, 104));
            fc.Add(fl);
            FLeap = Check(T("f_leap"), Form0.Leap, null, "fLeap"); fc.Add(FLeap); Box.SetShown(FLeap, Form0.Calendar == "lunar");
            fc.Add(Flow(Btn(T("btn_build"), true, () => BuildChart(), "btnBuild"), Btn(T("btn_save"), false, SaveProfile, "btnSave"), Btn(T("btn_png"), false, () => ExportPng(), "btnPng"),
                Btn(T("btn_qr"), false, () => ShowQr(), "btnQr"), Btn(T("btn_share"), false, Share, "btnShare"), Btn(T("btn_print"), false, Print, "btnPrint")));
            if (Chart == null) return;
            string layout = S("layout"); bool desk = IsDesktop; if (layout == "auto") layout = Scroll.ClientSize.Width < D(560) && !desk ? "list" : "board";
            var area = new Box { Direction = desk ? Box.Dir.H : Box.Dir.V, Gap = D(10) }; var left = new Box { Direction = Box.Dir.V, Gap = D(6) };
            Action<int> pick = chi => { SelChi = chi; Show("chart"); if (!IsDesktop) ScrollTo(DetailCard); };
            if (layout == "board") { var bc = Card(null, "boardCard"); bc.Pad = new Padding(D(4)); bc.Add(Board(-2, pick, "board")); left.Add(bc); }
            else { left.Tag = "palaceList"; left.Add(Board(-1, null, "center")); for (int i = 0; i < 12; i++) left.Add(Board((Chart.Menh + i) % 12, pick, "palace:" + ((Chart.Menh + i) % 12))); }
            DetailCard = Card(null, "detailBox"); FillDetail(DetailCard);
            if (desk) { area.Add(left, 3); area.Add(DetailCard, 2); } else { area.Add(left); area.Add(DetailCard); }
            p.Add(area);
        }
        ChartBoard Board(int single, Action<int> pick, string tag)
        {
            return new ChartBoard { Chart = Chart, Sel = SelChi, Single = single, Ts = B("ts"), Bs = B("bs"), Tt = B("tt"), Luu = B("luu"), Paper = Theme.Paper(S("paper")), GlassCells = G.ChartGlass && G.On, Th = G.Th, T = I18n, OnPick = pick, S = Dpi, Fs = Fs, Tag = tag, Cursor = pick != null ? Cursors.Hand : Cursors.Default };
        }
        internal ChartInput ReadForm()
        {
            int y, vy; int.TryParse(FYear.Text.Trim(), out y); if (!int.TryParse(FView.Text.Trim(), out vy)) vy = DateTime.Now.Year;
            return WithMode(new ChartInput { Name = string.IsNullOrEmpty(FName.Text.Trim()) ? "Thân Chủ" : FName.Text.Trim(), Gender = FGender.SelectedIndex == 1 ? "Nữ" : "Nam", Calendar = FCal.SelectedIndex == 1 ? "lunar" : "solar", Day = FDay.SelectedIndex + 1, Month = FMonth.SelectedIndex + 1, Year = y, HourChi = FHour.SelectedIndex, Leap = FLeap.Checked, ViewYear = vy });
        }
        internal Chart BuildChart()
        {
            var inp = FName != null && Current == "chart" && !FName.IsDisposed ? ReadForm() : WithMode(Form0);
            Host.Log("build " + inp.Calendar + "," + inp.Day + "," + inp.Month + "," + inp.Year + "," + inp.HourChi);
            if (!(inp.Year >= 1900 && inp.Year <= 2100) || (inp.Calendar == "solar" && !Lunar.ValidSolar(inp.Day, inp.Month, inp.Year))) { Toast(T("invalid_date")); return null; }
            try { Chart = Engine.Build(inp); } catch (ArgumentException) { Toast(T("invalid_lunar")); return null; }
            Form0 = inp; Host.Set("aa.last", InputMap(inp)); SelChi = Chart.Menh; if (Current == "chart") Show("chart"); return Chart;
        }
        void FillDetail(Box dc)
        {
            var p = Chart.Palaces[SelChi < 0 ? Chart.Menh : SelChi]; int chi = p.Chi;
            dc.Add(H2(I18n.Palace(p.Name) + " · " + p.CanChi + " (" + p.Element + ")")); dc.Add(Hint(T("dai_han") + " " + p.DaiHan + "–" + (p.DaiHan + 9) + " · " + T("tieu_han") + " " + p.TieuHan));
            dc.Add(Grp(T("major_stars"))); if (p.Major.Count == 0) dc.Add(Txt(T("vcd"), 10, G.Th.Muted, false));
            foreach (var s in p.Major) dc.Add(Txt(s.Name + " – " + s.Bright + " · " + s.Element, 10.5f, G.Th.Element(s.Element), true));
            Action<string, List<Star>, int> grp = (k, l, col) => { if (l.Count == 0) return; dc.Add(Grp(T(k))); var f = new Box { Direction = Box.Dir.Wrap, Gap = D(6) }; foreach (var s in l) f.Add(Txt(s.Name + (s.AttachedTo != null ? " (" + s.AttachedTo + ")" : "") + ",", 10, s.AttachedTo != null ? G.Th.Hoa(s.Name) : col, false, false)); dc.Add(f); };
            grp("good_stars", p.Good, G.Th.Good); grp("bad_stars", p.Bad, G.Th.Bad); grp("other_stars", p.Neutral, G.Th.Text);
            if (p.Luu.Count > 0) { dc.Add(Grp(T("luu_stars") + " " + Chart.ViewYear)); dc.Add(Txt(string.Join(", ", p.Luu), 10, G.Th.Purple, false)); }
            dc.Add(Grp(T("rings"))); dc.Add(Body(string.Join(" · ", new[] { p.RingTrangSinh, p.RingBacSi, p.RingThaiTue }.Where(x => x != null))));
            var tp = Chart.TamPhuong(chi); dc.Add(Grp(T("tam_phuong")));
            dc.Add(Body(string.Join(" | ", Enumerable.Range(1, 3).Select(k => { var q = Chart.Palaces[tp[k]]; var ms = q.Major.Select(s => s.Name).ToList(); return I18n.Palace(q.Name) + " (" + q.ChiName + "): " + (ms.Count == 0 ? T("vcd") : string.Join(", ", ms)); }))));
            dc.Add(H3(T("related"))); var rel = Lib.Related(Chart, chi); if (rel.Count == 0) dc.Add(Hint(T("no_related")));
            foreach (var h in rel) { var hh = h; dc.Add(Item(h.Title, I18n.ChapterName(Lib, h.Ch) + " · " + h.Why, null, () => OpenArticle(hh.Ch, hh.I))); }
        }
        internal void SaveProfile()
        {
            if (Chart == null && BuildChart() == null) return;
            var o = InputMap(Current == "chart" ? ReadForm() : Form0); o["saved"] = Host.NowMs(); o["summary"] = Chart.YearCanChi + " · " + Chart.CucName + " · " + T("menh") + " " + Engine.CHI[Chart.Menh];
            var l = Host.List("aa.saved"); l.Insert(0, o); while (l.Count > 200) l.RemoveAt(l.Count - 1); Host.Set("aa.saved", l); Toast(T("saved_ok"));
        }
        internal byte[] ChartPng() { using (var b = Board(-2, null, null).RenderPng()) using (var ms = new MemoryStream()) { b.Save(ms, ImageFormat.Png); return ms.ToArray(); } }
        internal byte[] ExportPng()
        {
            if (Chart == null && BuildChart() == null) return null;
            var png = ChartPng(); string name = "LaSo-" + string.Join("_", Chart.Name.Split(' ')) + "-" + Chart.SYear + ".png", where = Host.SaveFile(name, png);
            var b = new Box { Direction = Box.Dir.V, Gap = D(6) }; var pb = new PictureBox { Image = Image.FromStream(new MemoryStream(png)), SizeMode = PictureBoxSizeMode.Zoom, Height = D(420), BackColor = Color.Transparent }; b.Add(pb); b.Add(Hint(where ?? name));
            Modal(T("png_ok"), b); return png;
        }
        internal static string ChartCode(ChartInput o) { return "AA2|" + o.Name + "|" + (o.Gender == "Nữ" ? "F" : "M") + "|" + (o.Calendar == "lunar" ? "L" : "S") + "|" + o.Day + "|" + o.Month + "|" + o.Year + "|" + o.HourChi + "|" + (o.Leap ? 1 : 0); }
        internal QrCode ShowQr()
        {
            if (Chart == null && BuildChart() == null) return null;
            string code = ChartCode(Current == "chart" ? ReadForm() : Form0); var qr = QrCode.EncodeText(code, QrCode.Ecc.Medium);
            int sc = 8, bd = 4, sz = (qr.Size + bd * 2) * sc; var bmp = new Bitmap(sz, sz);
            using (var g = Graphics.FromImage(bmp)) { g.Clear(Color.White); for (int y = 0; y < qr.Size; y++) for (int x = 0; x < qr.Size; x++) if (qr.Get(x, y)) g.FillRectangle(Brushes.Black, (x + bd) * sc, (y + bd) * sc, sc, sc); }
            var b = new Box { Direction = Box.Dir.V, Gap = D(6) }; b.Add(new PictureBox { Image = bmp, SizeMode = PictureBoxSizeMode.Zoom, Height = D(260), Tag = "qrImage" }); b.Add(Hint(T("qr_desc"))); b.Add(Hint(code));
            Modal(T("qr_title"), b); return qr;
        }
        string SummaryText() { var p = Chart.Palaces[Chart.Menh]; var ms = p.Major.Select(s => s.Name).ToList(); return Chart.Name + " – " + Chart.AmDuong + ", " + Chart.YearCanChi + ", " + Chart.NapAm + ", " + Chart.CucName + ". " + T("menh") + " " + p.CanChi + ": " + (ms.Count == 0 ? T("vcd") : string.Join(", ", ms)) + ".\n" + ChartCode(Form0) + "\n(Applied Astrology " + Program.Version + ")"; }
        void Share()
        {
            if (Chart == null && BuildChart() == null) return;
            try { var d = new DataObject(); d.SetText(SummaryText()); using (var ms = new MemoryStream(ChartPng())) d.SetImage(Image.FromStream(ms)); Clipboard.SetDataObject(d, true); } catch { Host.Copy(SummaryText()); }
            Toast(T("ab_copied"));
        }
        void Print()
        {
            if (Chart == null && BuildChart() == null) return;
            try
            {
                var bmp = Board(-2, null, null).RenderPng(); var doc = new PrintDocument { DocumentName = "Applied Astrology – " + Chart.Name };
                doc.PrintPage += (s, e) => { var m = e.MarginBounds; float sc = Math.Min((float)m.Width / bmp.Width, (float)m.Height / bmp.Height); e.Graphics.DrawImage(bmp, m.X, m.Y, bmp.Width * sc, bmp.Height * sc); };
                using (var dlg = new PrintDialog { Document = doc, UseEXDialog = true }) if (dlg.ShowDialog(this) == DialogResult.OK) doc.Print();
            }
            catch (Exception e) { Toast(e.Message); }
        }

        // =============================== ÂM LỊCH ===============================
        string[] Wds() { var w = T("cal_wd").Split(','); return w.Length == 7 ? w : new[] { "T2", "T3", "T4", "T5", "T6", "T7", "CN" }; }
        string WdFull(int wd) { var w = T("cal_wd_full").Split(','); return w.Length == 7 ? w[(wd + 6) % 7] : ""; }
        static string Pad2(int n) { return n < 10 ? "0" + n : n.ToString(); }
        void CalendarScreen(Box p)
        {
            var head = Card(p, "calHead"); var nav = new Box { Direction = Box.Dir.H, Gap = D(6) };
            nav.Add(Btn("←", false, () => CalMove(-1), "calPrev", true)); var title = Txt(T("cal_month_title", "m", CalM, "y", CalY), 12.5f, G.Th.Gold2, true); nav.Add(title, 1);
            nav.Add(Btn("→", false, () => CalMove(1), "calNext", true)); nav.Add(Btn(T("cal_today"), false, () => { var n = DateTime.Now; CalY = n.Year; CalM = n.Month; CalD = n.Day; Show("calendar"); }, "calToday", true));
            head.Add(nav);
            var pick = new Box { Direction = Box.Dir.Wrap, Gap = D(6) };
            var cm = Combo(Enumerable.Range(1, 12).Select(i => T("f_month") + " " + i), CalM - 1, i => { CalM = i + 1; CalD = Math.Min(CalD, Lunar.DaysInMonth(CalM, CalY)); Show("calendar"); }, "calM"); cm.Width = D(140); pick.Add(cm);
            var cy = Edit(CalY.ToString(), true, "calY"); cy.Width = D(90); cy.KeyDown += (s, e) => { int y; if (e.KeyCode == Keys.Enter && int.TryParse(cy.Text, out y) && y >= 1800 && y <= 2199) { CalY = y; CalD = Math.Min(CalD, Lunar.DaysInMonth(CalM, CalY)); Show("calendar"); } }; pick.Add(cy);
            head.Add(pick);
            var l1 = Lunar.Solar2Lunar(1, CalM, CalY); var lN = Lunar.Solar2Lunar(Lunar.DaysInMonth(CalM, CalY), CalM, CalY);
            head.Add(Hint(T("lunar_date") + ": " + l1.Month + (l1.Leap ? "*" : "") + "/" + l1.Year + (lN.Month != l1.Month ? " – " + lN.Month + (lN.Leap ? "*" : "") + "/" + lN.Year : "") + " · " + Lunar.CAN[(l1.Year + 6) % 10] + " " + Lunar.CHI[(l1.Year + 8) % 12] + " · " + T("cal_tz")));
            bool desk = Wide; var area = new Box { Direction = desk ? Box.Dir.H : Box.Dir.V, Gap = D(10) };
            var gc = Card(null, "calGrid"); gc.Pad = new Padding(D(6));
            var mg = new MonthGrid { Wds = Wds(), Th = G.Th, GlassCells = G.On, S = Dpi, Fs = Fs, Tag = "monthView", Cursor = Cursors.Hand, OnDay = d => { CalD = d; Show("calendar"); } }; mg.Set(CalY, CalM, CalD);
            gc.Add(mg); gc.Add(Hint("● " + T("cal_hd") + "   ▬ " + T("cal_events") + "   * " + T("cal_leap")));
            var det = Card(null, "calDetail"); CalDetail(det);
            if (desk) { area.Add(gc, 3); area.Add(det, 2); } else { area.Add(gc); area.Add(det); }
            p.Add(area);
            var cv = Card(p, "calConvert"); cv.Add(H2(T("cal_convert")));
            var cd = Edit(CalD.ToString(), true, "cvD"); var cmo = Edit(CalM.ToString(), true, "cvM"); var cyy = Edit(CalY.ToString(), true, "cvY");
            cv.Add(Flow(Field(T("f_day"), cd, 70), Field(T("f_month"), cmo, 70), Field(T("f_year"), cyy, 90)));
            var lp = Check(T("f_leap"), false, null, "cvLeap"); cv.Add(lp);
            var outB = new Box { Direction = Box.Dir.V, Tag = "cvOut", Gap = D(2) };
            cv.Add(Flow(Btn(T("cal_s2l"), false, () => Convert(true, cd, cmo, cyy, lp, outB), "cvS2L"), Btn(T("cal_l2s"), false, () => Convert(false, cd, cmo, cyy, lp, outB), "cvL2S")));
            cv.Add(outB);
            var sx = Card(p, "calSixty"); sx.Add(H2(T("cal_60"))); sx.Add(Hint(T("cal_60_d")));
            var y60 = Edit("", true, "c60Y"); y60.Width = D(110); var o60 = Body(""); o60.Tag = "c60Out";
            sx.Add(Flow(y60, Btn(T("cal_find_year"), true, () => FindYear(y60, o60), "c60Go"))); sx.Add(o60);
            var cur = Cal.Year(DateTime.Now.Year); var tbl = new Box { Direction = Box.Dir.V, Tag = "sixty" };
            foreach (var s in Cal.SixtyTable())
            {
                var row = new Box { Direction = Box.Dir.H, Gap = D(8), Pad = new Padding(D(4), D(3), D(4), D(3)), G = G, GlassKind = s.Name == cur.Name ? 1 : 0, Solid = s.Name == cur.Name ? G.Th.Sel : 0, Radius = D(6), Cursor = Cursors.Hand };
                var a = Txt((s.Idx + 1).ToString(), 9, G.Th.Muted, false, false); a.Width = D(28); row.Add(a);
                var nmt = Txt(s.Name, 10, G.Th.Text, true, false); nmt.Width = D(96); row.Add(nmt);
                var na = Txt(s.NapAm, 10, G.Th.Element(s.Element), false, false); na.Width = D(132); row.Add(na);
                row.Add(Txt(string.Join(" · ", s.Years), 9, G.Th.Muted, false), 1);
                var ss = s; EventHandler h = (o, e) => ShowYear(ss.Years[ss.Years.Count > 3 ? 3 : 0], o60); row.Click += h; foreach (Control c in row.Controls) c.Click += h;
                tbl.Add(row);
            }
            sx.Add(tbl);
        }
        void CalMove(int d) { CalM += d; if (CalM < 1) { CalM = 12; CalY--; } if (CalM > 12) { CalM = 1; CalY++; } CalD = Math.Min(CalD, Lunar.DaysInMonth(CalM, CalY)); Show("calendar"); }
        internal DayInfo CalDetail(Box box)
        {
            var inf = Cal.DayInfo(CalD, CalM, CalY); var lu = inf.Lunar;
            var big = new Box { Direction = Box.Dir.H, Gap = D(10) };
            big.Add(Txt(CalD.ToString(), 26, G.Th.Gold2, true, false)); var c1 = new Box { Direction = Box.Dir.V }; c1.Add(Body(WdFull(inf.Weekday))); c1.Add(Body(Pad2(CalD) + "/" + Pad2(CalM) + "/" + CalY)); big.Add(c1, 1);
            big.Add(Txt(lu.Day.ToString(), 26, G.Th.Gold2, true, false)); var c2 = new Box { Direction = Box.Dir.V }; c2.Add(Body(T("lunar_date"))); c2.Add(Body(lu.Day + "/" + lu.Month + (lu.Leap ? " " + T("cal_leap") : "") + "/" + lu.Year)); c2.FixedWidth = D(120); c2.Width = D(120); big.Add(c2);
            box.Add(big);
            box.Add(KV(T("day_cc"), inf.DayCC + "  (" + inf.NapAmDay + ")")); box.Add(KV(T("month_cc"), inf.MonthCC + (inf.MonthLeap ? " (" + T("cal_leap") + ")" : ""))); box.Add(KV(T("year_cc"), inf.YearCC + "  (" + inf.NapAmYear + ")"));
            box.Add(KV(T("cal_tiet"), inf.Tiet + (inf.TietStart ? " · " + T("cal_tiet_start") : ""))); box.Add(KV(T("cal_daytype"), (inf.HoangDao ? T("cal_hd") : T("cal_hkd")) + " (" + inf.Than + ") · " + T("cal_truc") + " " + inf.Truc)); box.Add(KV(T("cal_hour0"), inf.HourZero));
            box.Add(Grp(T("cal_good_hours"))); box.Add(Txt(string.Join("   ", inf.GoodHours.Select(h => h.Name + " (" + h.Range + ")")), 10, G.Th.Good, false));
            box.Add(Grp(T("cal_bad_hours"))); box.Add(Txt(string.Join("   ", inf.BadHours.Select(h => h.Name + " (" + h.Range + ")")), 9.5f, G.Th.Muted, false));
            if (inf.Events.Count > 0) { box.Add(Grp(T("cal_events"))); foreach (var e in inf.Events) box.Add(Body((e.Lunar ? "☾ " : "☀ ") + e.Name)); }
            box.Add(Hint("JD " + inf.Jd));
            box.Add(Flow(Btn(T("cal_build_chart"), true, () => { Form0 = WithMode(new ChartInput { Name = Form0.Name, Gender = Form0.Gender, Calendar = "solar", Day = CalD, Month = CalM, Year = CalY, HourChi = Form0.HourChi, ViewYear = Form0.ViewYear }); Show("chart"); BuildChart(); }, "calBuild")));
            return inf;
        }
        internal int[] Convert(bool s2l, TextBox cd, TextBox cm, TextBox cy, CheckBox lp, Box outB)
        {
            outB.Controls.Clear(); int d, m, y;
            Action<string> bad = msg => { outB.Add(Txt(msg, 10.5f, G.Th.Bad, true)); Root.PerformLayout(); LayoutRoot(); };
            if (!int.TryParse(cd.Text.Trim(), out d) || !int.TryParse(cm.Text.Trim(), out m) || !int.TryParse(cy.Text.Trim(), out y) || !(y >= 1800 && y <= 2199 && m >= 1 && m <= 12 && d >= 1 && d <= 31)) { bad(T("invalid_date")); return null; }
            int[] so; LDate lu; bool leap = lp.Checked;
            if (s2l) { if (!Lunar.ValidSolar(d, m, y)) { bad(T("invalid_date")); return null; } so = new[] { d, m, y }; lu = Lunar.Solar2Lunar(d, m, y); }
            else { so = Lunar.Lunar2Solar(d, m, y, leap); if (so == null || (d > 29 && Lunar.LunarMonthLength(m, y, leap) < d)) { bad(T("invalid_lunar")); return null; } lu = Lunar.Solar2Lunar(so[0], so[1], so[2]); }
            var inf = Cal.DayInfo(so[0], so[1], so[2]); var fs = so;
            outB.Add(KV(T("solar_date"), WdFull(inf.Weekday) + ", " + Pad2(so[0]) + "/" + Pad2(so[1]) + "/" + so[2]));
            outB.Add(KV(T("lunar_date"), lu.Day + "/" + lu.Month + (lu.Leap ? " " + T("cal_leap") : "") + "/" + lu.Year + " – " + inf.DayCC + ", " + T("month_cc").ToLower() + " " + inf.MonthCC + ", " + T("year_cc").ToLower() + " " + inf.YearCC));
            outB.Add(Flow(Btn(T("cal_open_day"), false, () => { CalY = fs[2]; CalM = fs[1]; CalD = fs[0]; Show("calendar"); }, "cvGoto", true)));
            Root.PerformLayout(); LayoutRoot(); Scroll.Invalidate(true);
            return new[] { so[0], so[1], so[2], lu.Day, lu.Month, lu.Year, lu.Leap ? 1 : 0 };
        }
        internal YearInfo ShowYear(int y, TextBlock o) { var yi = Cal.Year(y); o.Text = y + ": " + yi.Name + " · " + T("cal_napam") + " " + yi.NapAm + " (" + yi.Element + ") · " + T("cal_age") + " " + yi.Age; Root.PerformLayout(); LayoutRoot(); return yi; }
        internal YearInfo FindYear(TextBox e, TextBlock o) { int y; return int.TryParse(e.Text.Trim(), out y) && y >= 1800 && y <= 2199 ? ShowYear(y, o) : null; }

        // =============================== THƯ VIỆN ===============================
        internal bool LibBack()
        {
            if (LibMode.StartsWith("read:")) { var k = LibMode.Split(':'); LibMode = PrevMode == "search" && LastResults != null ? "search:" + LastQuery : "chapter:" + k[1]; Show("library"); return true; }
            if (LibMode != "home") { LibMode = "home"; Show("library"); return true; }
            return false;
        }
        void LibraryScreen(Box p)
        {
            var top = Card(p, "libTop"); top.Add(H2(T("lib_title"))); top.Add(Hint(T("lib_desc", "n", Lib.TotalArticles(), "c", Lib.Chapters.Count)));
            var sr = new Box { Direction = Box.Dir.H, Gap = D(6) }; var q = Edit(LibMode.StartsWith("search:") ? LastQuery : "", false, "libQ");
            q.KeyDown += (s, e) => { if (e.KeyCode == Keys.Enter) { e.SuppressKeyPress = true; Search(q.Text); } };
            sr.Add(q, 1); sr.Add(Btn(T("btn_search"), true, () => Search(q.Text), "libGo")); top.Add(sr);
            var rec = Host.List("aa.recent");
            if (rec.Count > 0 && LibMode == "home") { var r0 = Json.Obj(rec[0]); top.Add(Flow(Btn(T("lib_continue") + ": " + Json.Str(r0["t"], ""), false, () => OpenArticle(Json.Str(r0["ch"], ""), Json.Num(r0["i"], 0)), "libContinue", true))); }
            if (LibMode.StartsWith("chapter:")) ChapterView(p, LibMode.Substring(8));
            else if (LibMode.StartsWith("read:")) { var k = LibMode.Split(':'); Reader(p, k[1], int.Parse(k[2])); }
            else if (LibMode.StartsWith("search:")) Results(p);
            else
            {
                var grid = new Box { Direction = Box.Dir.Wrap, Gap = D(8), Tag = "chapters" };
                foreach (var c in Lib.Chapters) { var cc = c; var it = Item(c.N + ". " + I18n.ChapterName(Lib, c.Id), c.Count + " " + T("articles"), null, () => { LibMode = "chapter:" + cc.Id; Show("library"); }, "ch:" + c.Id); it.FixedWidth = D(250); it.Width = D(250); grid.Add(it); }
                p.Add(grid);
            }
        }
        void ChapterView(Box p, string id)
        {
            var c = Lib.Get(id); var f = Card(p, "chapterList");
            f.Add(Flow(Btn("← " + T("btn_back"), false, () => { LibMode = "home"; Show("library"); }, null, true))); f.Add(H2(c.N + ". " + I18n.ChapterName(Lib, id)));
            for (int i = 0; i < c.Titles.Count; i++) { int ii = i; f.Add(Item((i + 1) + ". " + c.Titles[i], c.Lens[i].ToString("N0") + " " + T("chars") + (IsBookmarked(id, i) ? " · ★" : ""), null, () => OpenArticle(id, ii), "art:" + i)); }
        }
        internal void OpenArticle(string ch, int i) { PrevMode = LibMode.StartsWith("search:") ? "search" : "chapter"; LibMode = "read:" + ch + ":" + i; Show("library"); }
        internal TextBlock ReaderBody;
        internal Article Reader(Box p, string ch, int i)
        {
            var art = Lib.Art(ch, i); var c = Lib.Get(ch); var f = Card(p, "reader");
            if (art == null) { f.Add(Body(T("no_related"))); return null; }
            var tools = Flow(Btn("← " + I18n.ChapterName(Lib, ch), false, () => { LibMode = "chapter:" + ch; Show("library"); }, null, true));
            if (i > 0) tools.Add(Btn(T("btn_prev"), false, () => OpenArticle(ch, i - 1), "rPrev", true));
            if (i < c.Count - 1) tools.Add(Btn(T("btn_next"), false, () => OpenArticle(ch, i + 1), "rNext", true));
            GButton bm = null; bm = Btn((IsBookmarked(ch, i) ? "★ " : "☆ ") + T("btn_bookmark"), false, () => { bool on = ToggleBookmark(ch, i); bm.Text = (on ? "★ " : "☆ ") + T("btn_bookmark"); bm.Invalidate(); }, "rBm", true); tools.Add(bm);
            tools.Add(Btn("A−", false, () => FontStep(-0.08), "rFontM", true)); tools.Add(Btn("A+", false, () => FontStep(0.08), "rFontP", true));
            if (I18n.Current != "vi") tools.Add(Btn(T("btn_translate"), false, () => Host.OpenUrl("https://translate.google.com/?sl=vi&tl=" + Uri.EscapeDataString(I18n.Current.Replace("-BR", "").Replace("-419", "")) + "&op=translate&text=" + Uri.EscapeDataString(art.Body.Substring(0, Math.Min(4500, art.Body.Length)))), "rTr", true));
            f.Add(tools); f.Add(Txt(art.Title, 15, G.Th.Gold2, true)); f.Add(Hint(I18n.ChapterName(Lib, ch) + " · " + (i + 1) + "/" + c.Count + " · " + T("reader_original_note")));
            double fsz = Json.Dbl(Host.Get("aa.readerFont"), 1.02);
            ReaderBody = Txt(art.Body, (float)(11.5 * fsz), G.Th.Text, false); ReaderBody.Tag = "rBody"; ReaderBody.Margin = new Padding(0, D(8), 0, D(6)); f.Add(ReaderBody);
            if (i < c.Count - 1) f.Add(Flow(Btn(T("btn_next") + " →", true, () => OpenArticle(ch, i + 1))));
            MarkRead(ch, i); return art;
        }
        void FontStep(double d) { double f = Math.Max(0.8, Math.Min(1.8, Json.Dbl(Host.Get("aa.readerFont"), 1.02) + d)); Host.Set("aa.readerFont", f); if (ReaderBody != null) { ReaderBody.Font = new Font("Segoe UI", (float)(11.5 * f) * Fs); Root.PerformLayout(); LayoutRoot(); } }
        internal List<Hit> Search(string q) { LastQuery = (q ?? "").Trim(); Host.Log("search " + LastQuery); LastResults = Lib.Search(LastQuery); LibMode = "search:" + LastQuery; if (Current == "library") Show("library"); return LastResults; }
        void Results(Box p)
        {
            var f = Card(p, "results"); f.Add(Flow(Btn("← " + T("btn_back"), false, () => { LibMode = "home"; Show("library"); }, null, true), Txt(T("lib_results", "n", LastResults == null ? 0 : LastResults.Count), 11, G.Th.Text, true, false)));
            if (LastResults == null) return;
            foreach (var h in LastResults.Take(120)) { var hh = h; f.Add(Item(h.Title, I18n.ChapterName(Lib, h.Ch), h.Snippet, () => OpenArticle(hh.Ch, hh.I))); }
        }
        internal bool IsBookmarked(string ch, int i) { return Host.List("aa.bookmarks").Any(o => Json.Str(Json.Obj(o)["k"], "") == ch + ":" + i); }
        internal bool ToggleBookmark(string ch, int i)
        {
            var bm = Host.List("aa.bookmarks"); string key = ch + ":" + i; int found = bm.FindIndex(o => Json.Str(Json.Obj(o)["k"], "") == key);
            if (found >= 0) bm.RemoveAt(found); else bm.Insert(0, Json.Map("k", key, "ch", ch, "i", (long)i, "t", Lib.Title(ch, i), "d", Host.NowMs()));
            Host.Set("aa.bookmarks", bm); return found < 0;
        }
        void MarkRead(string ch, int i)
        {
            var r = Host.List("aa.recent"); string key = ch + ":" + i; r.RemoveAll(o => Json.Str(Json.Obj(o)["k"], "") == key);
            r.Insert(0, Json.Map("k", key, "ch", ch, "i", (long)i, "t", Lib.Title(ch, i), "s", 0L, "d", Host.NowMs())); while (r.Count > 30) r.RemoveAt(r.Count - 1); Host.Set("aa.recent", r);
        }

        // =============================== LUẬN GIẢI ===============================
        void AssistantScreen(Box p)
        {
            var f = Card(p, "asCard"); f.Add(H2(T("as_title"))); f.Add(Hint(T("as_note")));
            var chips = new Box { Direction = Box.Dir.Wrap, Gap = D(6), Tag = "asChips" };
            foreach (var c in new[] { new[] { "__overview", "as_overview" }, new[] { "mệnh tính cách", "as_q_menh" }, new[] { "công danh sự nghiệp", "as_q_career" }, new[] { "tài lộc tiền", "as_q_money" }, new[] { "hôn nhân phu thê", "as_q_love" }, new[] { "sức khỏe bệnh", "as_q_health" }, new[] { "hạn năm nay", "as_q_year" } })
            { var cc = c; chips.Add(Chip(T(c[1]), () => Ask(cc[0] == "__overview" ? "__overview" : cc[0] + " " + T(cc[1])), "chip:" + c[1])); }
            f.Add(chips);
            var box = new Box { Direction = Box.Dir.V, Gap = D(6), Tag = "chat" };
            foreach (var m in ChatLog)
            {
                bool user = (string)m[0] == "user"; var b = new Box { G = G, GlassKind = 1, Radius = D(12), Pad = new Padding(D(12), D(8), D(12), D(8)), Solid = user ? Theme.Alpha(G.Th.Sel, 235) : 0, Margin = new Padding(user ? D(60) : 0, 0, user ? 0 : D(30), 0) };
                b.Add(Body((string)m[1]));
                var refs = m[2] as List<Hit>; if (refs != null) foreach (var h in refs) { var hh = h; var r = Txt("→ " + h.Title, 10, G.Th.Accent, false); r.Cursor = Cursors.Hand; r.Click += (s, e) => OpenArticle(hh.Ch, hh.I); b.Add(r); }
                box.Add(b);
            }
            f.Add(box);
            var ir = new Box { Direction = Box.Dir.H, Gap = D(6) }; var q = Edit("", false, "asQ");
            q.KeyDown += (s, e) => { if (e.KeyCode == Keys.Enter) { e.SuppressKeyPress = true; var t = q.Text.Trim(); if (t.Length > 0) Ask(t); } };
            ir.Add(q, 1); ir.Add(Btn(T("btn_send"), true, () => { var t = q.Text.Trim(); if (t.Length > 0) Ask(t); }, "asSend")); f.Add(ir);
        }
        internal string Ask(string q)
        {
            if (Chart == null) BuildChart(); string o;
            if (q == "__overview") { o = Chart == null ? T("as_need_chart") : Asst.Overview(Chart); ChatLog.Add(new object[] { "ai", o, null }); }
            else { ChatLog.Add(new object[] { "user", q, null }); var r = Asst.Ask(Chart, q); o = r.Text; ChatLog.Add(new object[] { "ai", o, r.Refs }); }
            while (ChatLog.Count > 40) ChatLog.RemoveAt(0);
            if (Current == "assistant") { Show("assistant"); Scroll.AutoScrollPosition = new Point(0, Root.Height); Scroll.Invalidate(true); }
            return o;
        }

        // =============================== HỒ SƠ ===============================
        void SavedScreen(Box p)
        {
            var f = Card(p, "savedList"); f.Add(H2(T("saved_charts"))); var list = Host.List("aa.saved"); if (list.Count == 0) f.Add(Hint(T("saved_empty")));
            for (int i = 0; i < list.Count; i++)
            {
                int idx = i; var o = Json.Obj(list[i]);
                var it = Item(Json.Str(o["name"], "") + " – " + Json.Num(o["day"], 0) + "/" + Json.Num(o["month"], 0) + "/" + Json.Num(o["year"], 0) + " (" + (Json.Str(o["calendar"], "") == "lunar" ? T("lunar") : T("solar")) + ")", Json.Str(o.ContainsKey("summary") ? o["summary"] : null, ""), null, null);
                it.Add(Flow(Btn(T("btn_open"), false, () => { var inp = InputFrom(o); inp.ViewYear = DateTime.Now.Year; Form0 = WithMode(inp); Show("chart"); BuildChart(); }, "load:" + i, true), Btn(T("btn_delete"), false, () => { var l = Host.List("aa.saved"); if (idx < l.Count) l.RemoveAt(idx); Host.Set("aa.saved", l); Show("saved"); Toast(T("deleted")); }, "del:" + i, true)));
                f.Add(it);
            }
            var bm = Card(p, "bmList"); bm.Add(H2(T("lib_bookmarks"))); RefList(bm, Host.List("aa.bookmarks"));
            var rc = Card(p, "recentList"); rc.Add(H2(T("lib_recent"))); RefList(rc, Host.List("aa.recent"));
        }
        void RefList(Box f, List<object> arr) { if (arr.Count == 0) f.Add(Hint("—")); foreach (var x in arr) { var o = Json.Obj(x); f.Add(Item(Json.Str(o["t"], ""), I18n.ChapterName(Lib, Json.Str(o["ch"], "")), null, () => OpenArticle(Json.Str(o["ch"], ""), Json.Num(o["i"], 0)))); } }

        // =============================== CÀI ĐẶT ===============================
        Box Setting(Box p, string title, string hint, string tag = null) { var f = Card(p, tag); f.Add(H3(title)); if (hint != null) f.Add(Hint(hint)); return f; }
        static int IdxOf(string[] a, string v) { int i = Array.IndexOf(a, v); return i < 0 ? 0 : i; }
        void SettingsScreen(Box p)
        {
            var f = Setting(p, T("s_language"), null);
            var codes = I18n.Langs.Select(l => l.Code).ToArray();
            f.Add(Combo(I18n.Langs.Select(l => l.Name + " (" + l.Code + ")"), IdxOf(codes, S("lang")), i => SetLang(codes[i]), "sLang"));
            var themes = new[] { "celestial-dark", "royal-gold", "ivory-light", "imperial-red" };
            f = Setting(p, T("s_theme"), null); f.Add(Combo(new[] { T("th_celestial"), T("th_gold"), T("th_ivory"), T("th_red") }, IdxOf(themes, S("theme")), i => { SetS("theme", themes[i]); ApplySettings(); }, "sTheme"));
            GlassSettings(p);
            var fonts = new[] { "compact", "normal", "large", "xlarge" };
            f = Setting(p, T("s_font"), null); f.Add(Combo(new[] { T("fs_compact"), T("fs_normal"), T("fs_large"), T("fs_xlarge") }, IdxOf(fonts, S("font")), i => { SetS("font", fonts[i]); ApplySettings(); }, "sFont"));
            var lays = new[] { "auto", "board", "list" };
            f = Setting(p, T("s_layout"), null); f.Add(Combo(new[] { T("lay_auto"), T("lay_board"), T("lay_list") }, IdxOf(lays, S("layout")), i => SetS("layout", lays[i]), "sLayout"));
            f = Setting(p, T("s_desktop"), T("s_desktop_d")); f.Add(Check(T("s_desktop"), IsDesktop, v => { SetS("desktop", v); DoLayout(); Show("settings"); }, "sDesktop"));
            f = Setting(p, T("s_rings"), null);
            f.Add(Flow(Check(T("s_trangsinh"), B("ts"), v => SetS("ts", v), "sTS"), Check(T("s_bacsi"), B("bs"), v => SetS("bs", v), "sBS"), Check(T("s_thaitue"), B("tt"), v => SetS("tt", v), "sTT"), Check(T("s_luu"), B("luu"), v => SetS("luu", v), "sLuu")));
            f = Setting(p, T("s_paper"), T("s_paper_d")); f.Add(Combo(Theme.PAPERS.Select(x => T("paper_" + x)), IdxOf(Theme.PAPERS, S("paper")), i => SetS("paper", Theme.PAPERS[i]), "sPaper"));
            var leaps = new[] { "same", "next" };
            f = Setting(p, T("s_leap"), null); f.Add(Combo(new[] { T("s_leap_same"), T("s_leap_next") }, IdxOf(leaps, S("leap")), i => { SetS("leap", leaps[i]); if (Chart != null) { Form0.LeapMode = leaps[i]; try { Chart = Engine.Build(Form0); } catch { } } }, "sLeap"));
            f = Setting(p, T("s_network"), T("s_network_d")); var netOut = Body(""); netOut.Tag = "netOut"; f.Add(Flow(Btn(T("s_check"), false, () => CheckNetwork(netOut), "sNet"))); f.Add(netOut);
            f = Setting(p, T("s_backup"), null);
            f.Add(Flow(Btn(T("s_export"), false, () => ExportBackup(), "sExport"), Btn(T("s_import"), false, ImportFile, "sImport")));
            var imp = Edit("", false, "sImportText", true); f.Add(imp);
            f.Add(Flow(Btn(T("s_import_text"), false, () => ImportBackup(imp.Text), "sImportGo", true), Btn(T("btn_reset"), false, ResetDefaults, "sReset", true), Btn(T("s_clear"), false, ClearData, "sClear", true)));
            f = Setting(p, T("s_about"), T("s_about_d"));
            f.Add(Hint(T("s_version") + ": " + Program.Version + " · " + T("s_platform") + ": Windows native · " + Host.OsName() + " · " + Host.ProcArch() + "\n" + Lib.TotalArticles() + " " + T("articles") + " · " + I18n.Langs.Count + " " + T("s_language").ToLower() + "\nQR: Project Nayuki (MIT) · Âm lịch: thuật toán Hồ Ngọc Đức"));
            f.Add(Flow(Btn(T("ab_open"), true, () => Show("about"), "sAboutOpen")));
        }
        void GlassSettings(Box p)
        {
            var f = Setting(p, T("s_glass"), T("s_glass_d"), "glassSettings");
            // v0.0.5: công tắc bật/tắt Liquid Glass (Creat & Update mục 2)
            f.Add(Check(T("lg_enable"), B("lgOn"), v => { SetS("lgOn", v); ApplySettings(); Toast(T(v ? "lg_enabled" : "lg_disabled")); }, "sLgOn"));
            f.Add(Hint(T("lg_enable_d")));
            if (!B("lgOn")) { f.Add(Flow(Btn(T("lg_reset"), false, ResetGlass, "sLgReset", true))); return; }
            var modes = new[] { "frost", "liquid" }; f.Add(Txt(T("lg_mode"), 9, G.Th.Muted, false));
            f.Add(Combo(new[] { T("lg_frost"), T("lg_liquid") }, Math.Max(0, IdxOf(modes, S("lgMode"))), i => { SetS("lgMode", modes[i]); ApplySettings(); }, "sLgMode"));
            var bgs = new[] { "nebula", "aurora", "plain" }; f.Add(Txt(T("lg_bg"), 9, G.Th.Muted, false));
            f.Add(Combo(new[] { T("lg_bg_nebula"), T("lg_bg_aurora"), T("lg_bg_plain") }, IdxOf(bgs, S("lgBg")), i => { SetS("lgBg", bgs[i]); ApplySettings(); }, "sLgBg"));
            f.Add(Slider(T("lg_blur"), 0, 40, N("lgBlur"), "", v => { SetS("lgBlur", (long)v); ApplySettings(); }, "sLgBlur"));
            f.Add(Slider(T("lg_refr"), 0, 100, N("lgRefr"), "%", v => { SetS("lgRefr", (long)v); ApplySettings(); }, "sLgRefr"));
            f.Add(Slider(T("lg_bezel"), 4, 40, N("lgBezel"), "", v => { SetS("lgBezel", (long)v); ApplySettings(); }, "sLgBezel"));
            f.Add(Slider(T("lg_tint"), 0, 90, N("lgTint"), "%", v => { SetS("lgTint", (long)v); ApplySettings(); }, "sLgTint"));
            f.Add(Slider(T("lg_spec"), 0, 100, N("lgSpec"), "%", v => { SetS("lgSpec", (long)v); ApplySettings(); }, "sLgSpec"));
            f.Add(Slider(T("lg_sat"), 50, 200, N("lgSat"), "%", v => { SetS("lgSat", (long)v); ApplySettings(); }, "sLgSat"));
            f.Add(Check(T("lg_cards"), B("lgCards"), v => { SetS("lgCards", v); ApplySettings(); }, "sLgCards"));
            f.Add(Check(T("lg_chart"), B("lgChart"), v => { SetS("lgChart", v); ApplySettings(); }, "sLgChart"));
            f.Add(Flow(Btn(T("lg_reset"), false, ResetGlass, "sLgReset", true)));
        }
        internal void ResetGlass() { foreach (var kv in Defaults().Where(k => k.Key.StartsWith("lg"))) St[kv.Key] = kv.Value; Save(); ApplySettings(); Toast(T("lg_reset")); }
        internal void SetLang(string code) { SetS("lang", code); I18n.SetLang(code); ApplySettings(); }
        internal void CheckNetwork(TextBlock o)
        {
            o.Text = T("net_checking");
            ThreadPool.QueueUserWorkItem(_ =>
            {
                var r = Host.HttpGet("https://www.cloudflare.com/cdn-cgi/trace", 8000);
                BeginInvoke(new MethodInvoker(() =>
                {
                    if (o.IsDisposed) return;
                    if (!r.Ok) { o.Text = T("net_fail"); o.Color = Gfx.C(G.Th.Bad); Host.Log("net fail " + r.Status); return; }
                    var kv = r.Body.Split('\n').Where(l => l.Contains('=')).ToDictionary(l => l.Substring(0, l.IndexOf('=')), l => l.Substring(l.IndexOf('=') + 1).Trim());
                    Func<string, string> Get = k2 => { string v; return kv.TryGetValue(k2, out v) ? v : "?"; };
                    bool warp = kv.ContainsKey("warp") && kv["warp"] != "off";
                    o.Text = T("net_ok") + " · " + T("net_latency") + ": " + r.Ms + " ms\n" + T("net_ip") + ": " + Get("ip") + " · " + T("net_loc") + ": " + Get("loc") + (kv.ContainsKey("colo") ? " · PoP " + kv["colo"] : "") + " · " + (warp ? T("net_vpn_on") : T("net_vpn_off"));
                    o.Color = Gfx.C(G.Th.Good); Host.Log("net " + r.Ms + "ms ip=" + Get("ip") + " warp=" + Get("warp")); Root.PerformLayout(); LayoutRoot();
                }));
            });
        }
        internal Dictionary<string, object> ExportBackup()
        {
            var d = Json.Map("app", "Applied Astrology", "version", Program.Version, "date", DateTime.Now.ToString("yyyy-MM-ddTHH:mm:ss"), "settings", St, "saved", Host.List("aa.saved"), "bookmarks", Host.List("aa.bookmarks"), "recent", Host.List("aa.recent"));
            var r = Host.SaveFile("AppliedAstrology-backup-" + DateTime.Now.ToString("yyyy-MM-dd") + ".json", Encoding.UTF8.GetBytes(Json.Stringify(d))); Toast(r != null ? T("s_export") + ": " + r : T("import_fail")); return d;
        }
        void ImportFile() { using (var dlg = new OpenFileDialog { Filter = "JSON|*.json|*.*|*.*" }) if (dlg.ShowDialog(this) == DialogResult.OK) ImportBackup(File.ReadAllText(dlg.FileName, Encoding.UTF8)); }
        internal bool ImportBackup(string text)
        {
            try
            {
                var d = Json.Obj(Json.Parse(text)); if (d == null || Json.Str(d.ContainsKey("app") ? d["app"] : null, "") != "Applied Astrology") throw new Exception("bad");
                var st = d.ContainsKey("settings") ? Json.Obj(d["settings"]) : null; if (st != null) { St = new Dictionary<string, object>(Defaults()); foreach (var kv in st) St[kv.Key] = kv.Value; Save(); }
                foreach (var k in new[] { "saved", "bookmarks", "recent" }) if (d.ContainsKey(k) && d[k] != null) Host.Set("aa." + k, d[k]);
                I18n.SetLang(S("lang")); ApplySettings(); Toast(T("import_ok")); return true;
            }
            catch { Toast(T("import_fail")); return false; }
        }
        internal void ResetDefaults() { St = new Dictionary<string, object>(Defaults()); Save(); I18n.SetLang(S("lang")); ApplySettings(); }
        void ClearData()
        {
            if (Program.SelfTest || MessageBox.Show(this, T("confirm_clear"), "Applied Astrology", MessageBoxButtons.OKCancel, MessageBoxIcon.Warning) == DialogResult.OK)
            { Host.Set("aa.saved", new List<object>()); Host.Set("aa.bookmarks", new List<object>()); Host.Set("aa.recent", new List<object>()); Host.Set("aa.last", null); ResetDefaults(); Toast(T("deleted")); }
        }

        // =============================== GIỚI THIỆU ===============================
        internal void CheckUpdate(bool silent, Action<Dictionary<string, object>> cb)
        {
            ThreadPool.QueueUserWorkItem(_ =>
            {
                var r = Updater.Check();
                BeginInvoke(new MethodInvoker(() =>
                {
                    Updater.LastMsg = Json.Bool(r["ok"], false) ? (Updater.Newer ? T("ab_new", "v", r["latest"]) : T("ab_latest", "v", Program.Version)) : T("ab_update_fail");
                    if (Updater.Newer && silent) Toast(Updater.LastMsg);
                    if (cb != null) cb(r); else if (Current == "about") Show("about");
                }));
            });
        }
        System.Windows.Forms.Timer dlTimer;
        internal void StartDownload()
        {
            if (Updater.Asset == null) return; Updater.Download(Json.Str(Updater.Asset["browser_download_url"], ""), Json.Str(Updater.Asset["name"], "update"));
            if (dlTimer != null) dlTimer.Dispose(); dlTimer = new System.Windows.Forms.Timer { Interval = 500 };
            dlTimer.Tick += (s, e) => { if (Updater.State != "running") { dlTimer.Stop(); if (Updater.State == "done" && B("autoInstall") && !Program.SelfTest) Updater.Install(Updater.PathDone, this); } if (Current == "about") Show("about"); };
            dlTimer.Start(); Show("about");
        }
        void AboutScreen(Box p)
        {
            var hd = Card(p, "aboutHead"); var hr = new Box { Direction = Box.Dir.H, Gap = D(12) }; hr.Add(new LogoCtl { Width = D(64), Height = D(64) });
            var ht = new Box { Direction = Box.Dir.V }; ht.Add(Txt(T("app_name"), 15, G.Th.Gold2, true)); ht.Add(Hint("Applied Astrology · " + T("s_version") + " " + Program.Version + " (" + Program.BuildDate + ") · native")); ht.Add(Hint(T("s_about_d"))); hr.Add(ht, 1); hd.Add(hr);
            hd.Add(Flow(Btn("← " + T("tab_settings"), false, () => Show("settings"), "aboutBack", true)));
            var up = Setting(p, T("ab_updates"), null, "abUpdates");
            up.Add(Check(T("ab_auto_check"), B("autoUpdate"), v => SetS("autoUpdate", v), "abAuto")); up.Add(Check(T("ab_auto_install"), B("autoInstall"), v => SetS("autoInstall", v), "abAutoInst"));
            up.Add(Flow(Btn(T("ab_check"), true, () => { Updater.LastMsg = T("ab_checking"); Show("about"); CheckUpdate(false, null); }, "abCheck"), Btn(T("ab_releases"), false, () => Host.OpenUrl(Updater.RELEASES_URL))));
            var lc = Json.Obj(Host.Get("aa.lastUpdateCheck")); if (lc != null) up.Add(Hint(T("ab_last_check") + ": " + Host.FromMs((long)Json.Dbl(lc["at"], 0)).ToString("g") + " (" + Json.Str(lc["latest"], "") + ")"));
            var outB = new Box { Direction = Box.Dir.V, Tag = "abUpdOut", Gap = D(3) };
            if (!string.IsNullOrEmpty(Updater.LastMsg)) outB.Add(Txt(Updater.LastMsg, 10.5f, Updater.CheckState == "fail" ? G.Th.Bad : G.Th.Good, true));
            if (Updater.Release != null && Updater.Newer)
            {
                outB.Add(Txt(T("ab_whats_new") + " – " + Json.Str(Updater.Release.ContainsKey("name") ? Updater.Release["name"] : null, ""), 10, G.Th.Gold2, true));
                string body = Json.Str(Updater.Release.ContainsKey("body") ? Updater.Release["body"] : null, "").Replace("#", "").Replace("*", "").Replace("`", ""); outB.Add(Hint(body.Length > 2500 ? body.Substring(0, 2500) + "…" : body));
                if (Updater.Asset != null)
                {
                    outB.Add(Flow(Btn(T("ab_download"), true, StartDownload, "abDl"))); outB.Add(Hint(Json.Str(Updater.Asset["name"], "") + " · " + (Json.Dbl(Updater.Asset["size"], 0) / 1048576.0).ToString("0.0") + " MB"));
                    if (Updater.State == "running") outB.Add(Hint(T("ab_downloading", "p", Updater.Total > 0 ? Updater.Got * 100 / Updater.Total : 0)));
                    if (Updater.State == "done") { outB.Add(Hint(T("ab_dl_done"))); outB.Add(Flow(Btn(T("ab_install"), true, () => Updater.Install(Updater.PathDone, this), "abInst"))); outB.Add(Hint(T("ab_install_note_win"))); }
                    if (Updater.State == "error") outB.Add(Txt(T("ab_dl_fail") + ": " + Updater.Error, 10, G.Th.Bad, false));
                }
                else outB.Add(Hint(T("ab_no_asset")));
            }
            up.Add(outB);
            var wn = Setting(p, T("ab_whats_new"), null); var cl = Json.Arr(Json.Parse(new Host.Src().Read("data/changelog.json"))); string lang = I18n.Current == "vi" ? "vi" : "en";
            if (cl != null) for (int k = 0; k < cl.Count; k++) { var e = Json.Obj(cl[k]); wn.Add(Txt(Json.Str(e["v"], "") + " · " + Json.Str(e["d"], ""), 10.5f, G.Th.Gold2, true)); var items = Json.Arr(e.ContainsKey(lang) ? e[lang] : e["en"]); if (items != null) foreach (var it in items.Take(k == 0 ? 99 : 3)) wn.Add(Body("• " + it)); }
            var dg = Setting(p, T("ab_diag"), null, "abDiag");
            dg.Add(Check(T("ab_logs_on"), B("logs"), v => SetS("logs", v), "abLogOn"));
            dg.Add(Flow(Btn(T("ab_logs_view"), false, () => { var lt = LogText(); var tb = Txt(lt.Substring(Math.Max(0, lt.Length - 20000)), 8.5f, G.Th.Text, false); tb.Font = new Font("Consolas", 8.5f); Modal(T("ab_logs"), tb); }, "abLogView", true),
                Btn(T("ab_logs_share"), false, () => { var r = Host.SaveFile("AppliedAstrology-log-" + DateTime.Now.Ticks + ".txt", Encoding.UTF8.GetBytes(LogText())); Toast(r ?? T("ab_copied")); }, "abLogExport", true),
                Btn(T("ab_logs_clear"), false, () => { Host.ClearLog(); Toast(T("deleted")); }, "abLogClear", true)));
            dg.Add(H3(T("ab_errors"))); dg.Add(Hint(T("ab_errors_d")));
            var el = new Box { Direction = Box.Dir.V, Tag = "abErrList", Gap = D(4) }; var cr = Host.CrashList(); if (cr.Count == 0) el.Add(Hint(T("ab_errors_none")));
            foreach (var c in cr) { var ci = Item("⚠ " + c[0], null, null, null); var tx = Txt(c[1].Substring(0, Math.Min(3000, c[1].Length)), 8.5f, G.Th.Text, false); tx.Font = new Font("Consolas", 8.5f); ci.Add(tx); el.Add(ci); }
            dg.Add(el);
            dg.Add(Flow(Btn(T("ab_report"), true, () => Host.OpenUrl(Updater.ReportUrl(this)), "abReport"), Btn(T("ab_copy_debug"), false, () => { Host.Copy(Updater.DebugInfo(this)); Toast(T("ab_copied")); }, "abCopy"), Btn(T("ab_errors_clear"), false, () => { Host.ClearCrash(); Show("about"); Toast(T("deleted")); }, "abErrClear")));
            dg.Add(Hint(T("ab_report_d")));
            var dbg = Txt(Updater.DebugInfo(this), 8.5f, G.Th.Text, false); dbg.Font = new Font("Consolas", 8.5f); dbg.Tag = "abDebug"; dg.Add(dbg);
            var li = Setting(p, T("ab_licenses"), null); foreach (var l in Updater.LICENSES) { var ll = l; li.Add(Item(l[0], l[1], null, string.IsNullOrEmpty(l[2]) ? (Action)null : () => Host.OpenUrl(ll[2]))); }
            var lk = Setting(p, T("ab_links"), null); lk.Add(Flow(Btn("GitHub", false, () => Host.OpenUrl(Updater.REPO_URL)), Btn("Releases", false, () => Host.OpenUrl(Updater.RELEASES_URL)), Btn("Issues", false, () => Host.OpenUrl(Updater.REPO_URL + "/issues"))));
        }
        internal string LogText() { var d = Updater.DebugInfo(this); return "=== " + d.Split('\n')[0] + " ===\n" + d + "\n\n=== Native log ===\n" + Host.ReadLog(); }
    }
}
