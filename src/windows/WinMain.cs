// Applied Astrology v0.0.5 – cửa sổ chính native Windows: khung, thanh tab, 7 màn hình, hộp thoại, thông báo.
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
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
    partial class MainForm : Form
    {
        internal Library Lib; internal I18n I18n; internal Assistant Asst; internal Dictionary<string, object> St; internal readonly GlassCtx G = new GlassCtx();
        internal float Dpi = 1; internal string Current = "chart"; internal Box Header, Root; internal NavBar Nav; internal Panel Scroll; internal Overlay Ov; internal SelfTestWin Test;
        internal Chart Chart; internal int SelChi = -1; internal ChartInput Form0; internal bool Wide = true;
        static readonly string[] VIEWS = { "chart", "calendar", "library", "assistant", "saved", "settings" };
        static readonly string[] TAB_KEYS = { "tab_chart", "tab_calendar", "tab_library", "tab_assistant", "tab_saved", "tab_settings" };

        internal static Dictionary<string, object> Defaults()
        {
            return Json.Map("lang", "vi", "theme", "celestial-dark", "font", "normal", "layout", "auto", "desktop", null, "ts", true, "bs", false, "tt", true, "luu", true, "leap", "same", "paper", "none", "autoUpdate", true, "autoInstall", true, "logs", true,
                "lgOn", true, "lgMode", "liquid", "lgBg", "nebula", "lgBlur", 18L, "lgRefr", 60L, "lgBezel", 18L, "lgTint", 35L, "lgSpec", 55L, "lgSat", 135L, "lgCards", true, "lgChart", false);
        }
        internal string S(string k) { object v; return St.TryGetValue(k, out v) && v != null ? System.Convert.ToString(v) : null; }
        internal bool B(string k) { object v; return St.TryGetValue(k, out v) && v is bool && (bool)v; }
        internal int N(string k) { object v; St.TryGetValue(k, out v); return Json.Num(v, 0); }
        internal void Save() { Host.Set("aa.settings", St); Host.LogEnabled = B("logs"); }
        internal void SetS(string k, object v) { St[k] = v; Save(); Host.Log("setting " + k + "=" + v); }

        public MainForm()
        {
            Text = "Applied Astrology " + Program.Version + " – Tử Vi Ứng Dụng";
            try { Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath); } catch { }
            using (var g = CreateGraphics()) Dpi = Math.Max(1f, g.DpiX / 96f);
            Width = (int)(1280 * Dpi); Height = (int)(860 * Dpi); MinimumSize = new Size((int)(420 * Dpi), (int)(520 * Dpi)); StartPosition = FormStartPosition.CenterScreen;
            SetStyle(ControlStyles.OptimizedDoubleBuffer | ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint | ControlStyles.ResizeRedraw, true);
            Font = new Font("Segoe UI", 9.5f);
            St = new Dictionary<string, object>(Defaults()); var saved = Json.Obj(Host.Get("aa.settings")); if (saved != null) foreach (var kv in saved) St[kv.Key] = kv.Value;
            // v0.0.5: Liquid Glass có công tắc riêng (lgOn); bản cũ lưu lgMode = "off" → lgOn = false
            if (saved != null && saved.ContainsKey("lgMode") && System.Convert.ToString(saved["lgMode"]) == "off") { St["lgOn"] = false; St["lgMode"] = "liquid"; }
            Host.LogEnabled = B("logs");
            var src = new Host.Src(); Lib = new Library(src); I18n = new I18n(src); I18n.SetLang(S("lang")); Asst = new Assistant(Lib, I18n);
            Form0 = new ChartInput { Name = "Nguyễn Văn An", Gender = "Nam", Day = 15, Month = 8, Year = 1995, HourChi = 6 };
            var last = Json.Obj(Host.Get("aa.last")); if (last != null) Form0 = InputFrom(last);
            Form0.ViewYear = DateTime.Now.Year;
            try { Chart = Engine.Build(WithMode(Form0)); SelChi = Chart.Menh; } catch { Chart = null; }
            var n = DateTime.Now; CalY = n.Year; CalM = n.Month; CalD = n.Day;
            Header = new Box { G = G, GlassKind = 2 }; Nav = new NavBar(this); Scroll = new DBPanel { AutoScroll = true, BackColor = Color.Transparent }; Ov = new Overlay(this) { Visible = false };
            Controls.Add(Ov); Controls.Add(Scroll); Controls.Add(Nav); Controls.Add(Header);
            // v0.0.5: gộp yêu cầu vẽ lại khi cuộn (nhiều sự kiện bánh xe → 1 lần vẽ)
            Scroll.Scroll += (s, e) => QueueScrollPaint();
            Scroll.MouseWheel += (s, e) => QueueScrollPaint();
            Resize += (s, e) => { DoLayout(); };
            ApplySettings();
            Shown += (s, e) =>
            {
                DoLayout();
                if (Environment.GetEnvironmentVariable("AA_DUMP") != null) Delay(3000, () => Host.Log("TREE\n" + DumpTree(this, 0)));
                if (Program.SelfTest) { Test = new SelfTestWin(this); Test.Start(); }
                else if (B("autoUpdate"))
                {
                    var lc = Json.Obj(Host.Get("aa.lastUpdateCheck")); double at = lc == null ? 0 : Json.Dbl(lc["at"], 0);
                    if (Host.NowMs() - at > 12 * 3600 * 1000.0) Delay(4000, () => CheckUpdate(true, null));
                }
            };
        }
        bool scrollPaintQueued; internal static long ScrollPaints;
        void QueueScrollPaint()
        {
            if (scrollPaintQueued || !IsHandleCreated) return; scrollPaintQueued = true;
            BeginInvoke((Action)(() => { scrollPaintQueued = false; ScrollPaints++; Scroll.Invalidate(true); Scroll.Update(); }));
        }
        internal static string DumpTree(Control c, int depth) { var sb = new StringBuilder(); if (depth < 7) { sb.Append(new string(' ', depth * 2) + c.GetType().Name + " " + c.Tag + " " + c.Bounds + (c.Visible ? "" : " HIDDEN") + "\n"); foreach (Control k in c.Controls) sb.Append(DumpTree(k, depth + 1)); } return sb.ToString(); }
        internal void Delay(int ms, Action a) { var t = new System.Windows.Forms.Timer { Interval = Math.Max(1, ms) }; t.Tick += (s, e) => { t.Stop(); t.Dispose(); a(); }; t.Start(); }
        internal int D(float v) { return (int)Math.Round(v * Dpi); }
        internal float Fs { get { string f = S("font"); return f == "compact" ? 0.87f : f == "large" ? 1.13f : f == "xlarge" ? 1.27f : 1f; } }
        internal bool IsDesktop { get { object d; St.TryGetValue("desktop", out d); return d is bool ? (bool)d : ClientSize.Width >= D(1000); } }

        // ---------------- cài đặt → giao diện ----------------
        internal void ApplyGlass()
        {
            G.Th = Theme.ById(S("theme")); G.Mode = B("lgOn") ? (S("lgMode") ?? "liquid") : "off"; if (G.Mode != "frost" && G.Mode != "liquid") G.Mode = "off"; G.BgStyle = S("lgBg") ?? "nebula"; G.Cards = B("lgCards"); G.ChartGlass = B("lgChart");
            G.P.Blur = N("lgBlur"); G.P.Refraction = N("lgRefr") / 100.0; G.P.Bezel = N("lgBezel") * Dpi; G.P.Tint = N("lgTint") / 100.0; G.P.Specular = N("lgSpec") / 100.0; G.P.Saturation = N("lgSat") / 100.0; G.P.Thickness = 40 * Dpi;
            G.Version++;
        }
        internal void ApplySettings()
        {
            ApplyGlass(); BackColor = Gfx.C(G.Th.Bg);
            RightToLeft = I18n.Rtl ? RightToLeft.Yes : RightToLeft.No;
            BuildHeader(); Nav.Invalidate(); DoLayout(); Show(Current);
        }
        protected override void OnPaintBackground(PaintEventArgs e)
        {
            G.Ensure(ClientSize);
            if (G.Full != null) e.Graphics.DrawImageUnscaled(G.Full, 0, 0); else e.Graphics.Clear(Gfx.C(G.Th.Bg));   // v0.0.5: không co giãn
        }
        void BuildHeader()
        {
            Header.Controls.Clear(); Header.Direction = Box.Dir.H; Header.Pad = new Padding(D(14), D(8), D(14), D(8)); Header.Radius = G.On ? D(18) : 0; Header.Gap = D(10);
            var logo = new LogoCtl { Width = D(40), Height = D(40) }; Header.Add(logo);
            var tt = new Box { Direction = Box.Dir.V }; tt.Add(Txt(I18n.T("app_name"), 13.5f, G.Th.Gold2, true)); tt.Add(Txt(I18n.T("app_sub"), 9f, G.Th.Muted, false));
            Header.Add(tt, 1);
        }
        internal void DoLayout()
        {
            if (Header == null) return;
            int cw = Math.Max(D(720), ClientSize.Width), ch = Math.Max(D(520), ClientSize.Height);
            G.Ensure(new Size(cw, ch));
            Wide = cw >= D(720) || IsDesktop;
            int m = G.On ? D(8) : 0, w = cw - m * 2, hh = Header.PrefHeight(w), nh = D(Wide ? 52 : 58);
            Header.SetBounds(m, G.On ? D(6) : 0, w, hh);
            int top = Header.Bottom + (G.On ? D(4) : 0);
            if (Wide) { Nav.SetBounds(m, top, w, nh); Scroll.SetBounds(0, Nav.Bottom + (G.On ? D(4) : 0), cw, ch - Nav.Bottom - (G.On ? D(4) : 0)); }
            else { Nav.SetBounds(m, ch - nh - m, w, nh); Scroll.SetBounds(0, top, cw, Nav.Top - top - (G.On ? D(4) : 0)); }
            Nav.Radius = G.On ? D(22) : 0; LayoutRoot(); Header.PerformLayout(); Invalidate(true);
            if (Ov.Visible) Ov.SetBounds(0, 0, cw, ch);
        }
        internal void LayoutRoot()
        {
            if (Root == null) return;
            int w = Scroll.ClientSize.Width - (Scroll.VerticalScroll.Visible ? 0 : SystemInformation.VerticalScrollBarWidth); w = Math.Max(D(300), Scroll.ClientSize.Width - SystemInformation.VerticalScrollBarWidth);
            int h = Root.PrefHeight(w); Root.SetBounds(Root.Left, Root.Top, w, h); Root.PerformLayout();
        }

        /// <summary>Hiển thị một màn hình</summary>
        internal void Show(string view)
        {
            Current = view; Nav.Invalidate();
            Scroll.SuspendLayout();
            var old = Root; Root = new Box { Direction = Box.Dir.V, Pad = new Padding(D(8), D(6), D(8), D(16)), Gap = D(10) };
            try { BuildScreen(view, Root); }
            catch (Exception e) { Host.WriteCrash("screen " + view, e); Root.Add(Txt(e.ToString(), 9, G.Th.Bad, false)); }
            Scroll.Controls.Add(Root); if (old != null) { Scroll.Controls.Remove(old); old.Dispose(); }
            Scroll.AutoScrollPosition = new Point(0, 0); Root.Location = new Point(0, 0);
            LayoutRoot(); Scroll.ResumeLayout(); Scroll.Invalidate(true);
        }
        internal void ScrollTo(Control c) { if (c != null) { Scroll.ScrollControlIntoView(c); Scroll.Invalidate(true); } }

        // ---------------- tiện ích dựng giao diện ----------------
        internal TextBlock Txt(string s, float pt, int color, bool bold, bool wrap = true)
        {
            return new TextBlock { Text = s ?? "", Font = new Font("Segoe UI", pt * Fs, bold ? FontStyle.Bold : FontStyle.Regular), Color = Gfx.C(color), Wrap = wrap, Rtl = I18n.Rtl };
        }
        internal TextBlock H2(string s) { var t = Txt(s, 12.5f, G.Th.Gold2, true); t.Margin = new Padding(0, 0, 0, D(4)); return t; }
        internal TextBlock H3(string s) { var t = Txt(s, 11f, G.Th.Gold2, true); t.Margin = new Padding(0, D(6), 0, D(2)); return t; }
        internal TextBlock Hint(string s) { return Txt(s, 9f, G.Th.Muted, false); }
        internal TextBlock Body(string s) { return Txt(s, 10f, G.Th.Text, false); }
        internal TextBlock Grp(string s) { var t = Txt(s, 9f, G.Th.Muted, true); t.Margin = new Padding(0, D(6), 0, 0); return t; }
        internal Box Card(Box parent = null, string tag = null)
        {
            var c = new Box { G = G, GlassKind = 1, Radius = D(12), Pad = new Padding(D(14), D(12), D(14), D(12)), Gap = D(4), Tag = tag };
            if (parent != null) parent.Add(c); return c;
        }
        internal GButton Btn(string s, bool primary, Action a, string tag = null, bool small = false)
        {
            var b = new GButton { Text = s, Primary = primary, SmallBtn = small, G = G, S = Dpi, Tag = tag, Font = new Font("Segoe UI", (small ? 9f : 10f) * Fs, primary ? FontStyle.Bold : FontStyle.Regular) };
            b.Click += (x, e) => { try { a(); } catch (Exception ex) { Host.WriteCrash("click " + s, ex); } }; return b;
        }
        internal GButton Chip(string s, Action a, string tag = null) { var b = Btn(s, false, a, tag, true); b.Chip = true; return b; }
        internal Box Flow(params Control[] cs) { var f = new Box { Direction = Box.Dir.Wrap, Gap = D(6) }; foreach (var c in cs) f.Add(c); return f; }
        internal TextBox Edit(string v, bool number, string tag = null, bool multi = false)
        {
            var t = new TextBox { Text = v ?? "", BorderStyle = BorderStyle.FixedSingle, BackColor = Gfx.C(Theme.Mix(G.Th.Card2, G.Th.Bg, 0.1)), ForeColor = Gfx.C(G.Th.Text), Font = new Font("Segoe UI", 10.5f * Fs), Tag = tag, Multiline = multi };
            t.Height = multi ? D(70) : t.PreferredHeight; if (number) t.KeyPress += (s, e) => { if (!char.IsDigit(e.KeyChar) && !char.IsControl(e.KeyChar) && e.KeyChar != '-') e.Handled = true; };
            return t;
        }
        internal ComboBox Combo(IEnumerable<string> items, int sel, Action<int> pick, string tag = null)
        {
            var c = new ComboBox { DropDownStyle = ComboBoxStyle.DropDownList, FlatStyle = FlatStyle.Flat, BackColor = Gfx.C(Theme.Mix(G.Th.Card2, G.Th.Bg, 0.1)), ForeColor = Gfx.C(G.Th.Text), Font = new Font("Segoe UI", 10f * Fs), Tag = tag, IntegralHeight = false, MaxDropDownItems = 16 };
            c.Items.AddRange(items.Cast<object>().ToArray()); if (sel >= 0 && sel < c.Items.Count) c.SelectedIndex = sel;
            c.Height = c.PreferredHeight; if (pick != null) c.SelectionChangeCommitted += (s, e) => pick(c.SelectedIndex); return c;
        }
        internal CheckBox Check(string label, bool on, Action<bool> cb, string tag = null)
        {
            var c = new CheckBox { Text = label, Checked = on, AutoSize = false, FlatStyle = FlatStyle.Standard, ForeColor = Gfx.C(G.Th.Text), BackColor = Color.Transparent, Font = new Font("Segoe UI", 10f * Fs), Tag = tag, UseCompatibleTextRendering = false };
            c.Height = D(26) + (int)((Fs - 1) * 10); c.Width = TextRenderer.MeasureText(label, c.Font).Width + D(28); if (cb != null) c.CheckedChanged += (s, e) => cb(c.Checked); return c;
        }
        internal Box Field(string label, Control ctl, int widthDp)
        {
            var b = new Box { Direction = Box.Dir.V, FixedWidth = D(widthDp), Width = D(widthDp), Gap = D(2) }; b.Add(Txt(label, 9f, G.Th.Muted, false)); ctl.Width = D(widthDp); b.Add(ctl); return b;
        }
        internal Box Slider(string label, int min, int max, int value, string unit, Action<int> cb, string tag)
        {
            var b = new Box { Direction = Box.Dir.V, Tag = tag, Gap = 0 }; var l = Txt(label + ": " + value + unit, 10f, G.Th.Text, false); b.Add(l);
            var tb = new TrackBar { Minimum = min, Maximum = max, Value = Math.Max(min, Math.Min(max, value)), TickStyle = TickStyle.None, Height = D(30), BackColor = Gfx.C(Theme.Mix(G.Th.Card, G.Th.Bg, 0.35)), Tag = tag + "_bar" };
            tb.ValueChanged += (s, e) => { l.Text = label + ": " + tb.Value + unit; };
            tb.MouseUp += (s, e) => cb(tb.Value); tb.KeyUp += (s, e) => cb(tb.Value);
            b.Add(tb); return b;
        }
        internal Box Item(string title, string sub, string sub2, Action click, string tag = null)
        {
            var f = new Box { G = G, GlassKind = 1, Radius = D(10), Pad = new Padding(D(12), D(8), D(12), D(8)), Gap = D(1), Tag = tag, Cursor = click != null ? Cursors.Hand : Cursors.Default };
            f.Add(Txt(title, 10.5f, G.Th.Text, true)); if (!string.IsNullOrEmpty(sub)) f.Add(Txt(sub, 9f, G.Th.Muted, false)); if (!string.IsNullOrEmpty(sub2)) f.Add(Txt(sub2, 9f, G.Th.Muted, false));
            if (click != null) { EventHandler h = (s, e) => click(); f.Click += h; foreach (Control c in f.Controls) c.Click += h; }
            return f;
        }
        internal Box KV(string k, string v) { var b = new Box { Direction = Box.Dir.Wrap, Gap = D(4) }; b.Add(Txt(k + ":", 10f, G.Th.Muted, false, false)); b.Add(Txt(v, 10f, G.Th.Text, false, false)); return b; }

        // ---------------- hộp thoại & thông báo ----------------
        internal void Modal(string title, Control body) { Ov.Open(title, body); }
        internal void CloseModal() { Ov.Close(); }
        internal bool ModalOpen { get { return Ov.Visible; } }
        ToastCtl toast;
        internal void Toast(string msg)
        {
            if (toast != null) { Controls.Remove(toast); toast.Dispose(); }
            toast = new ToastCtl { Text = msg, Font = new Font("Segoe UI", 10f * Fs, FontStyle.Bold), Back = G.Th.Gold };
            var sz = TextRenderer.MeasureText(msg, toast.Font); toast.Size = new Size(Math.Min(ClientSize.Width - D(40), sz.Width + D(28)), sz.Height + D(18));
            toast.Location = new Point((ClientSize.Width - toast.Width) / 2, Wide ? ClientSize.Height - toast.Height - D(28) : Nav.Top - toast.Height - D(14));
            Controls.Add(toast); toast.BringToFront(); var t = toast; Delay(2200, () => { if (t == toast) { Controls.Remove(t); t.Dispose(); toast = null; } });
            Host.Log("toast " + msg);
        }
        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            if (keyData == Keys.Escape) { if (ModalOpen) { CloseModal(); return true; } if (Current == "about") { Show("settings"); return true; } if (Current == "library" && LibBack()) return true; }
            return base.ProcessCmdKey(ref msg, keyData);
        }
    }

    class DBPanel : Panel { public DBPanel() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint, true); } protected override void OnPaintBackground(PaintEventArgs e) { if (Parent != null) InvokePaintBackground(Parent, new PaintEventArgs(e.Graphics, new Rectangle(Left, Top, Width, Height))); var st = e.Graphics.Save(); e.Graphics.TranslateTransform(-Left, -Top); var f = FindForm() as MainForm; if (f != null && f.G.Full != null) { e.Graphics.InterpolationMode = InterpolationMode.Bilinear; e.Graphics.DrawImage(f.G.Full, new Rectangle(0, 0, f.ClientSize.Width, f.ClientSize.Height)); } e.Graphics.Restore(st); } }
    class LogoCtl : Control { public LogoCtl() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint, true); BackColor = Color.Transparent; } protected override void OnPaint(PaintEventArgs e) { Icons.Logo(e.Graphics, new RectangleF(0, 0, Width - 1, Height - 1)); } }
    class ToastCtl : Control
    {
        public int Back; public ToastCtl() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint, true); BackColor = Color.Transparent; }
        protected override void OnPaint(PaintEventArgs e) { e.Graphics.SmoothingMode = SmoothingMode.AntiAlias; using (var p = Gfx.Round(new RectangleF(0, 0, Width - 1, Height - 1), 12)) using (var b = new SolidBrush(Gfx.C(Back))) e.Graphics.FillPath(b, p); TextRenderer.DrawText(e.Graphics, Text, Font, ClientRectangle, Color.FromArgb(0x1A, 0x12, 0x05), TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter | TextFormatFlags.NoPrefix); }
    }

    /// <summary>Thanh tab kính (vẽ tay, biểu tượng vector)</summary>
    class NavBar : Box
    {
        readonly MainForm f; static readonly string[] VIEWS = { "chart", "calendar", "library", "assistant", "saved", "settings" }, KEYS = { "tab_chart", "tab_calendar", "tab_library", "tab_assistant", "tab_saved", "tab_settings" };
        public NavBar(MainForm f) { this.f = f; G = f.G; GlassKind = 2; Cursor = Cursors.Hand; }
        internal Rectangle TabRect(int i) { float w = Width / 6f; return new Rectangle((int)(i * w), 0, (int)w, Height); }
        internal string TabText(string view) { int i = Array.IndexOf(VIEWS, view); return i < 0 ? "" : f.I18n.T(KEYS[i]); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias;
            for (int i = 0; i < 6; i++)
            {
                var r = TabRect(i); bool act = VIEWS[i] == f.Current || (f.Current == "about" && VIEWS[i] == "settings"); var c = Gfx.C(act ? f.G.Th.Gold : f.G.Th.Muted);
                float isz = f.D(f.Wide ? 18 : 20); Icons.Draw(g, i, new RectangleF(r.X + (r.Width - isz) / 2, r.Y + f.D(f.Wide ? 5 : 6), isz, isz), c);
                using (var font = new Font("Segoe UI", (f.Wide ? 9.5f : 8.5f) * f.Fs, act ? FontStyle.Bold : FontStyle.Regular))
                    TextRenderer.DrawText(g, KEYS[i] == null ? "" : f.I18n.T(KEYS[i]), font, new Rectangle(r.X, r.Y + (int)isz + f.D(6), r.Width, r.Height - (int)isz - f.D(8)), c, TextFormatFlags.HorizontalCenter | TextFormatFlags.Top | TextFormatFlags.EndEllipsis | TextFormatFlags.NoPrefix);
                if (act) using (var b = new SolidBrush(c)) g.FillRectangle(b, r.X + r.Width / 2 - f.D(13), r.Bottom - f.D(5), f.D(26), f.D(3));
            }
        }
        protected override void OnMouseUp(MouseEventArgs e) { base.OnMouseUp(e); int i = (int)(e.X / (Width / 6f)); if (i >= 0 && i < 6) f.Show(VIEWS[i]); }
    }

    /// <summary>Lớp phủ hộp thoại: nền tối mờ + khung kính ở giữa</summary>
    class Overlay : Control
    {
        readonly MainForm f; Bitmap shot; internal Box BoxCtl; Panel sc;
        public Overlay(MainForm f) { this.f = f; SetStyle(ControlStyles.OptimizedDoubleBuffer | ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint, true); }
        public void Open(string title, Control body)
        {
            Close();
            // v0.0.5: chụp nhanh bằng BitBlt màn hình (DrawToBitmap toàn cửa sổ mất hàng trăm ms khi có nhiều điều khiển)
            int cw = Math.Max(f.D(720), f.ClientSize.Width), ch = Math.Max(f.D(520), f.ClientSize.Height);
            try
            {
                shot = new Bitmap(cw, ch, PixelFormat.Format32bppPArgb); bool ok = false;
                try { if (f.Visible && f.WindowState != FormWindowState.Minimized && Form.ActiveForm == f) { using (var g = Graphics.FromImage(shot)) g.CopyFromScreen(f.PointToScreen(Point.Empty), Point.Empty, shot.Size); ok = true; } } catch { ok = false; }
                if (!ok) f.DrawToBitmap(shot, new Rectangle(0, 0, shot.Width, shot.Height));
            }
            catch { shot = null; }
            SetBounds(0, 0, cw, ch); Visible = true; BringToFront();
            int w = Math.Min(cw - f.D(28), f.D(620));
            BoxCtl = new Box { G = f.G, GlassKind = 2, Radius = f.D(16), Pad = new Padding(f.D(16), f.D(14), f.D(16), f.D(12)), Gap = f.D(8), Tag = "modal" };
            if (title != null) BoxCtl.Add(f.H2(title));
            sc = new Panel { AutoScroll = true, BackColor = Gfx.C(Theme.Mix(f.G.Th.Card, f.G.Th.Bg, 0.2)) };
            var inner = new Box { Direction = Box.Dir.V, Pad = new Padding(f.D(4)) }; inner.Add(body); sc.Controls.Add(inner);
            int iw = w - f.D(32) - SystemInformation.VerticalScrollBarWidth, ih = inner.PrefHeight(iw); inner.SetBounds(0, 0, iw, ih); inner.PerformLayout();
            int maxH = (int)(f.ClientSize.Height * 0.72); sc.Height = Math.Min(maxH, ih + 4);
            BoxCtl.Add(sc);
            var close = f.Btn(f.I18n.T("btn_close"), false, Close, "mClose"); BoxCtl.Add(f.Flow(close));
            int h = BoxCtl.PrefHeight(w); Controls.Add(BoxCtl); BoxCtl.SetBounds((Width - w) / 2, Math.Max(f.D(10), (Height - h) / 2), w, h); BoxCtl.PerformLayout();
            Invalidate(true);
        }
        public void Close() { if (BoxCtl != null) { Controls.Remove(BoxCtl); BoxCtl.Dispose(); BoxCtl = null; } if (shot != null) { shot.Dispose(); shot = null; } Visible = false; }
        protected override void OnPaintBackground(PaintEventArgs e)
        {
            var f2 = f.G; if (f2.Full != null) e.Graphics.DrawImageUnscaled(f2.Full, 0, 0);
            if (shot != null) e.Graphics.DrawImageUnscaled(shot, 0, 0);
            using (var b = new SolidBrush(Color.FromArgb(150, 0, 0, 0))) e.Graphics.FillRectangle(b, ClientRectangle);
        }
        protected override void OnMouseUp(MouseEventArgs e) { base.OnMouseUp(e); if (BoxCtl != null && !BoxCtl.Bounds.Contains(e.Location)) Close(); }
    }
}
