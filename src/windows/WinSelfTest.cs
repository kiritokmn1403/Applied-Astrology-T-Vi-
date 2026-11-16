// Applied Astrology v0.0.4 – tự kiểm thử Windows (--selftest --out <thư mục>): 44 bước, ảnh chụp từng bước, results.json (cùng giao thức 0.0.3).
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using AppliedAstrology.Core;

namespace AppliedAstrology
{
    class SelfTestWin
    {
        readonly MainForm f; readonly List<Tuple<string, Action<Action>>> steps = new List<Tuple<string, Action<Action>>>(); readonly List<Dictionary<string, object>> results = new List<Dictionary<string, object>>(); int idx; const int DELAY = 1100;
        public SelfTestWin(MainForm f) { this.f = f; Define(); }
        void Step(string n, Action<Action> a) { steps.Add(Tuple.Create(n, a)); }
        void Rec(string n, bool ok, string d) { results.Add(Json.Map("step", n, "ok", ok, "detail", d ?? "")); Host.Log("SELFTEST " + (ok ? "PASS" : "FAIL") + " " + n + " " + d); }
        Control Find(Control c, string tag) { if (c.Tag is string && (string)c.Tag == tag) return c; foreach (Control k in c.Controls) { var r = Find(k, tag); if (r != null) return r; } return null; }
        void Click(string tag) { var c = Find(f, tag); var b = c as GButton; if (b != null) b.PerformClickPublic(); }
        void Set(string k, object v) { f.St[k] = v; f.Save(); }
        ChartInput In(string cal, int d, int m, int y, int h, string g, bool leap) { return f.WithMode(new ChartInput { Name = "Nguyễn Văn An", Gender = g, Calendar = cal, Day = d, Month = m, Year = y, HourChi = h, Leap = leap, ViewYear = DateTime.Now.Year }); }
        int Kids(string tag) { var c = Find(f, tag); return c == null ? 0 : c.Controls.Count; }

        void Define()
        {
            Step("01_chart_build", done => { f.Show("chart"); f.Form0 = In("solar", 15, 8, 1995, 6, "Nam", false); f.Show("chart"); var c = f.BuildChart(); Rec("01_chart_build", c != null && c.LunarDate.Day == 20 && c.LunarDate.Month == 7 && Find(f, "detailBox") != null, c == null ? "null" : "lunar=" + c.LunarDate.Day + "/" + c.LunarDate.Month + "/" + c.LunarDate.Year + " cuc=" + c.CucName + " menh=" + Engine.CHI[c.Menh]); done(); });
            Step("02_palace_detail", done => { f.SelChi = (f.Chart.Menh + 4) % 12; f.Show("chart"); var rel = f.Lib.Related(f.Chart, f.SelChi); Rec("02_palace_detail", rel.Count > 0 && Find(f, "detailBox") != null, "related=" + rel.Count); done(); });
            Step("03_lunar_input", done => { f.Form0 = In("lunar", 1, 2, 2023, 3, "Nữ", true); f.Show("chart"); var c = f.BuildChart(); Rec("03_lunar_input", c != null && c.SDay == 22 && c.SMonth == 3, c == null ? "null" : "solar=" + c.SDay + "/" + c.SMonth + "/" + c.SYear); done(); });
            Step("04_qr", done => { var q = f.ShowQr(); Rec("04_qr", q != null && q.Size >= 21 && f.ModalOpen, q == null ? "" : "size=" + q.Size); Later(done, true); });
            Step("05_png", done => { var p = f.ExportPng(); Rec("05_png", p != null && p.Length > 50000, "bytes=" + (p == null ? 0 : p.Length)); Later(done, true); });
            Step("06_save_profile", done => { int n = Host.List("aa.saved").Count; f.SaveProfile(); f.Show("saved"); Rec("06_save_profile", Host.List("aa.saved").Count == n + 1, "saved=" + Host.List("aa.saved").Count); done(); });
            Step("07_library_home", done => { f.LibMode = "home"; f.Show("library"); Rec("07_library_home", Kids("chapters") == 13, "chapters=" + Kids("chapters") + " articles=" + f.Lib.TotalArticles()); done(); });
            Step("08_library_chapter", done => { f.LibMode = "chapter:an-sao"; f.Show("library"); Rec("08_library_chapter", Kids("chapterList") > 5, "items=" + Kids("chapterList")); done(); });
            Step("09_reader", done => { f.OpenArticle("chinh-tinh", 0); var b = Find(f, "rBody") as TextBlock; int n = b == null ? 0 : b.Text.Length; Click("rBm"); Rec("09_reader", n > 1000, "chars=" + n); done(); });
            Step("10_search", done => { var sw = Stopwatch.StartNew(); f.Show("library"); var r = f.Search("Hóa Kỵ"); Rec("10_search", r.Count > 20, "results=" + r.Count + " " + sw.ElapsedMilliseconds + "ms"); done(); });
            Step("11_search_noaccent", done => { var r = f.Search("thien phu"); Rec("11_search_noaccent", r.Count > 20, "results=" + r.Count); done(); });
            Step("12_assistant_overview", done => { f.Show("assistant"); var s = f.Ask("__overview"); Rec("12_assistant_overview", s.Length > 200, "chars=" + s.Length); done(); });
            Step("13_assistant_question", done => { var s = f.Ask("Công danh sự nghiệp của tôi thế nào?"); Rec("13_assistant_question", s.Contains("Quan Lộc"), "chars=" + s.Length); done(); });
            Step("14_assistant_year", done => { var s = f.Ask("Hạn năm nay ra sao?"); Rec("14_assistant_year", s.Contains(f.Chart.ViewYear.ToString()), "chars=" + s.Length); done(); });
            Step("15_settings_theme", done => { f.Show("settings"); Set("theme", "ivory-light"); f.ApplySettings(); Rec("15_settings_theme", f.G.Th.Id == "ivory-light" && !f.G.Th.Dark, f.G.Th.Id); done(); });
            Step("16_settings_language_en", done => { f.SetLang("en"); Rec("16_settings_language_en", f.Nav.TabText("settings") == "Settings", f.Nav.TabText("settings")); done(); });
            Step("17_settings_language_ja", done => { f.SetLang("ja"); var s = f.Nav.TabText("library"); Rec("17_settings_language_ja", s != "Library" && s != "Thư Viện" && s.Length > 0, s); done(); });
            Step("18_settings_font_layout", done => { Set("font", "large"); Set("layout", "list"); f.ApplySettings(); Rec("18_settings_font_layout", f.S("font") == "large" && f.S("layout") == "list", ""); done(); });
            Step("19_chart_list_layout", done => { f.Show("chart"); Rec("19_chart_list_layout", Find(f, "palaceList") != null, ""); done(); });
            Step("20_desktop_dex", done => { Set("desktop", true); Set("layout", "board"); f.ApplySettings(); f.Show("chart"); Rec("20_desktop_dex", f.IsDesktop && Find(f, "board") != null, "desktop=" + f.IsDesktop); done(); });
            Step("21_backup_export", done => { f.Show("settings"); var b = f.ExportBackup(); Rec("21_backup_export", b != null && Json.Arr(b["saved"]).Count > 0, ""); done(); });
            Step("22_backup_import", done => { var st = new Dictionary<string, object>(f.St); st["theme"] = "royal-gold"; bool ok = f.ImportBackup(Json.Stringify(Json.Map("app", "Applied Astrology", "settings", st))); Rec("22_backup_import", ok && f.G.Th.Id == "royal-gold", f.G.Th.Id); done(); });
            Step("23_network_check", done => { f.Show("settings"); Click("sNet"); f.Delay(7000, () => { var o = Find(f, "netOut") as TextBlock; string s = o == null ? "" : o.Text; Rec("23_network_check", s.Length > 5 && s != f.I18n.T("net_checking"), s.Length > 140 ? s.Substring(0, 140) : s); done(); }); });
            Step("24_calendar_view", done => { var n = DateTime.Now; f.CalY = n.Year; f.CalM = n.Month; f.CalD = n.Day; f.Show("calendar"); var mg = Find(f, "monthView") as MonthGrid; int dim = Lunar.DaysInMonth(f.CalM, f.CalY); Rec("24_calendar_view", mg != null && mg.Info(dim) != null && Find(f, "calDetail") != null, "days=" + dim + " today=" + f.CalD + "/" + f.CalM); done(); });
            Step("25_calendar_tet_2026", done => { f.CalY = 2026; f.CalM = 2; f.CalD = 17; f.Show("calendar"); var i = Cal.DayInfo(17, 2, 2026); Rec("25_calendar_tet_2026", i.Lunar.Day == 1 && i.Lunar.Month == 1 && i.Lunar.Year == 2026 && i.YearCC == "Bính Ngọ" && i.GoodHours.Count == 6 && i.Events.Count > 0, i.Lunar.Day + "/" + i.Lunar.Month + "/" + i.Lunar.Year + " " + i.DayCC + " | " + i.MonthCC + " | " + i.YearCC + " tiet=" + i.Tiet + " " + i.Than + " truc=" + i.Truc + " ev=" + (i.Events.Count > 0 ? i.Events[0].Name : "")); done(); });
            Step("26_calendar_convert", done =>
            {
                var cd = (TextBox)Find(f, "cvD"); var cm = (TextBox)Find(f, "cvM"); var cy = (TextBox)Find(f, "cvY"); var lp = (CheckBox)Find(f, "cvLeap"); var o = (Box)Find(f, "cvOut");
                cd.Text = "1"; cm.Text = "1"; cy.Text = "2027"; lp.Checked = false; var r = f.Convert(false, cd, cm, cy, lp, o); cd.Text = "3"; cm.Text = "11"; cy.Text = "2026"; var r2 = f.Convert(true, cd, cm, cy, lp, o);
                Rec("26_calendar_convert", r != null && r[0] == 6 && r[1] == 2 && r[2] == 2027 && r2 != null && r2[3] == 25 && r2[4] == 9, "Tet2027=" + (r == null ? "-" : r[0] + "/" + r[1] + "/" + r[2]) + " 3/11/2026->" + (r2 == null ? "-" : r2[3] + "/" + r2[4] + "/" + r2[5])); done();
            });
            Step("27_calendar_sixty", done => { var e = (TextBox)Find(f, "c60Y"); e.Text = "1995"; var yi = f.FindYear(e, (TextBlock)Find(f, "c60Out")); int rows = Kids("sixty"); f.ScrollTo(Find(f, "calSixty")); Rec("27_calendar_sixty", rows == 60 && yi != null && yi.Name == "Ất Hợi" && yi.NapAm == "Sơn Đầu Hỏa", "rows=" + rows + " 1995=" + (yi == null ? "" : yi.Name + " " + yi.NapAm)); done(); });
            Step("28_paper_background", done => { Set("paper", "vangchanh"); f.Show("chart"); var b = Find(f, "board") as ChartBoard; Rec("28_paper_background", b != null && b.Paper != null && b.Paper[0] == unchecked((int)0xFFF7F1A8), "paper=" + f.S("paper")); done(); });
            Step("29_about_view", done => { Set("paper", "none"); f.Show("settings"); Click("sAboutOpen"); Rec("29_about_view", f.Current == "about" && Find(f, "abCheck") != null && Find(f, "abDiag") != null && Find(f, "abUpdates") != null, "current=" + f.Current); done(); });
            Step("30_update_check", done => { f.CheckUpdate(false, r => { bool ok = Json.Bool(r["ok"], false); Rec("30_update_check", ok && r.ContainsKey("latest"), ok ? "latest=" + r["latest"] + " current=" + r["current"] + " newer=" + r["newer"] + " assets=" + r["assets"] + " asset=" + r["asset"] : "status=" + r["status"]); f.Show("about"); done(); }); });
            Step("31_update_download", done =>
            {
                if (Updater.Asset == null) { Rec("31_update_download", false, "no asset for this install kind"); done(); return; }
                var sw = Stopwatch.StartNew(); bool started = Updater.Download(Json.Str(Updater.Asset["browser_download_url"], ""), Json.Str(Updater.Asset["name"], "x"));
                Action poll = null; poll = () => { if (Updater.State == "running" && sw.ElapsedMilliseconds < 120000) { f.Delay(700, poll); return; } Rec("31_update_download", started && Updater.State == "done" && Updater.Got > 100000 && (Updater.Total <= 0 || Updater.Got == Updater.Total), Updater.Asset["name"] + " " + Updater.State + " " + Updater.Got + "/" + Updater.Total + " " + sw.ElapsedMilliseconds + "ms " + Updater.Error); done(); };
                f.Delay(700, poll);
            });
            Step("32_app_logs", done => { string mk = "SELFTEST-LOG-" + DateTime.Now.Ticks; Host.Log(mk); var l = Host.ReadLog(); Rec("32_app_logs", l.Contains(mk), "native=" + l.Length + " chars"); done(); });
            Step("33_error_report", done => { Host.WriteCrash("SELFTEST", new InvalidOperationException("SELFTEST synthetic error")); f.Show("about"); var cr = Host.CrashList(); bool ok = cr.Count > 0 && cr[0][1].Contains("SELFTEST synthetic error") && Updater.ReportUrl(f).Contains("/issues/new") && Updater.DebugInfo(f).Contains(Program.Version); Rec("33_error_report", ok, "crash files=" + cr.Count + " url=" + Updater.ReportUrl(f).Length + " chars"); Host.ClearCrash(); done(); });
            Step("34_safe_area", done => { Set("desktop", null); f.ApplySettings(); f.Show("chart"); var cr = f.ClientRectangle; bool ok = cr.Contains(f.Nav.Bounds) && cr.Contains(f.Header.Bounds) && !f.Nav.Bounds.IntersectsWith(f.Scroll.Bounds); Rec("34_safe_area", ok, "client=" + cr.Size + " header=" + f.Header.Bounds + " nav=" + f.Nav.Bounds + " content=" + f.Scroll.Bounds); done(); });
            Step("35_language_fr_new", done => { f.SetLang("fr"); var s = f.Nav.TabText("calendar"); Rec("35_language_fr_new", s.Length > 0 && s != f.I18n.TIn("en", "tab_calendar") && s != f.I18n.TIn("vi", "tab_calendar"), s); done(); });
            Step("36_restore_defaults", done => { f.ResetDefaults(); f.Form0 = In("solar", 15, 8, 1995, 6, "Nam", false); f.Show("chart"); f.BuildChart(); Rec("36_restore_defaults", f.S("theme") == "celestial-dark" && f.S("lang") == "vi" && f.S("paper") == "none" && f.S("lgMode") == "liquid", ""); done(); });
            Step("37_glass_liquid", done => { Set("lgMode", "liquid"); f.ApplySettings(); f.Show("settings"); Rec("37_glass_liquid", f.G.Mode == "liquid" && f.G.BlurBmp != null && Find(f, "glassSettings") != null, "blur=" + (f.G.BlurBmp == null ? "-" : f.G.Bw + "x" + f.G.Bh) + " header=" + f.Header.Size); done(); });
            Step("38_glass_frost", done => { Set("lgMode", "frost"); f.ApplySettings(); f.ScrollTo(Find(f, "glassSettings")); Rec("38_glass_frost", f.G.Mode == "frost", f.G.Mode); done(); });
            Step("39_glass_off", done => { Set("lgMode", "off"); f.ApplySettings(); f.Show("chart"); Rec("39_glass_off", f.G.Mode == "off" && !f.G.On, f.G.Mode); done(); });
            Step("40_glass_sliders", done => { Set("lgMode", "liquid"); Set("lgBlur", 30L); Set("lgRefr", 90L); Set("lgSpec", 80L); f.ApplySettings(); f.Show("calendar"); Rec("40_glass_sliders", f.G.P.Blur == 30 && Math.Abs(f.G.P.Refraction - 0.9) < 1e-6 && Math.Abs(f.G.P.Specular - 0.8) < 1e-6, "blur=" + f.G.P.Blur + " refr=" + f.G.P.Refraction + " spec=" + f.G.P.Specular); done(); });
            Step("41_glass_bg_aurora", done => { Set("lgBg", "aurora"); Set("lgChart", true); f.ApplySettings(); f.Show("chart"); Rec("41_glass_bg_aurora", f.G.BgStyle == "aurora" && f.G.ChartGlass, f.G.BgStyle); done(); });
            Step("42_glass_reset", done => { f.Show("settings"); f.ResetGlass(); Rec("42_glass_reset", f.S("lgBg") == "nebula" && f.N("lgBlur") == 18 && f.S("lgMode") == "liquid" && !f.B("lgChart"), "bg=" + f.S("lgBg") + " blur=" + f.N("lgBlur")); done(); });
            Step("43_native_no_webview", done =>
            {
                f.Show("chart"); var asm = AppDomain.CurrentDomain.GetAssemblies().Select(a => a.GetName().Name).Where(n => n.IndexOf("WebView", StringComparison.OrdinalIgnoreCase) >= 0 || n.IndexOf("mshtml", StringComparison.OrdinalIgnoreCase) >= 0).ToList();
                int wb = CountType(f, typeof(WebBrowser)); int child = 0; try { child = Process.GetProcessesByName("msedgewebview2").Count(p => { try { return p.StartTime > Process.GetCurrentProcess().StartTime && ParentOf(p) == Process.GetCurrentProcess().Id; } catch { return false; } }); } catch { }
                Rec("43_native_no_webview", asm.Count == 0 && wb == 0 && child == 0, "webview assemblies=" + asm.Count + " WebBrowser=" + wb + " msedgewebview2 children=" + child + " engine=" + Host.Engine() + " process=" + Process.GetCurrentProcess().ProcessName); done();
            });
            Step("44_lang_all_68", done => { int full = 0; var miss = new StringBuilder(); foreach (var l in f.I18n.Langs) { bool ok = f.I18n.TIn(l.Code, "s_glass") != null && f.I18n.TIn(l.Code, "lg_liquid") != null && f.I18n.TIn(l.Code, "tab_calendar") != null && f.I18n.TIn(l.Code, "ab_report") != null; if (ok) full++; else miss.Append(l.Code + " "); } Rec("44_lang_all_68", full == f.I18n.Langs.Count && full == 68, full + "/" + f.I18n.Langs.Count + " " + miss); done(); });
        }
        static int CountType(Control c, Type t) { int n = t.IsInstanceOfType(c) ? 1 : 0; foreach (Control k in c.Controls) n += CountType(k, t); return n; }
        static int ParentOf(Process p) { return 0; }
        void Later(Action done, bool close) { f.Delay(DELAY, () => { if (close) { Capture(steps[idx - 1].Item1 + "_modal"); f.CloseModal(); } done(); }); }
        public void Start() { Host.Log("SELFTEST start " + steps.Count + " steps"); f.Delay(800, Next); }
        void Next()
        {
            if (idx >= steps.Count) { Finish(); return; }
            var s = steps[idx++];
            try { s.Item2(() => f.Delay(350, () => { Capture(s.Item1); f.Delay(DELAY, Next); })); }
            catch (Exception e) { Rec(s.Item1, false, "EXCEPTION " + e.Message); Host.WriteCrash("selftest " + s.Item1, e); f.Delay(DELAY, () => { Capture(s.Item1); Next(); }); }
        }
        void Capture(string name)
        {
            try
            {
                Application.DoEvents();
                using (var b = new Bitmap(f.ClientSize.Width, f.ClientSize.Height))
                {
                    bool ok = false;
                    // Ảnh thật trên màn hình (giống người dùng thấy); cửa sổ được giữ trên cùng trong lúc kiểm thử
                    try { if (f.Visible && f.WindowState != FormWindowState.Minimized && Environment.OSVersion.Platform == PlatformID.Win32NT) { f.TopMost = true; f.Activate(); Application.DoEvents(); using (var g = Graphics.FromImage(b)) g.CopyFromScreen(f.PointToScreen(Point.Empty), Point.Empty, b.Size); ok = true; } } catch { }
                    if (!ok) f.DrawToBitmap(b, new Rectangle(0, 0, b.Width, b.Height));
                    b.Save(Path.Combine(Program.TestOut, name + ".png"), ImageFormat.Png);
                }
                File.AppendAllText(Path.Combine(Program.TestOut, "steps.jsonl"), Json.Stringify(Json.Map("step", name, "data", results.Count > 0 ? results[results.Count - 1] : null)) + "\n", new UTF8Encoding(false));
            }
            catch (Exception e) { Host.Log("capture " + e.Message); }
        }
        void Finish()
        {
            int pass = results.Count(r => Json.Bool(r["ok"], false));
            var sum = Json.Map("app", "Applied Astrology", "version", Program.Version, "platform", Json.Map("kind", "windows", "os", Host.OsName(), "arch", Host.ProcArch(), "install", Host.InstallKind(), "engine", Host.Engine(), "device", Environment.MachineName), "pass", (long)pass, "total", (long)results.Count, "results", results);
            File.WriteAllText(Path.Combine(Program.TestOut, "results.json"), Json.Stringify(sum), new UTF8Encoding(false));
            Host.Log("SELFTEST_DONE " + pass + "/" + results.Count);
            var tb = f.Txt(string.Join("\n", results.Select(r => (Json.Bool(r["ok"], false) ? "✔ " : "✘ ") + r["step"] + "  " + r["detail"])), 8.5f, f.G.Th.Text, false); tb.Font = new Font("Consolas", 8.5f);
            f.Modal("SELFTEST " + pass + "/" + results.Count, tb);
            f.Delay(1500, () => { Capture("99_done"); f.Delay(500, () => f.Close()); });
        }
    }
}
