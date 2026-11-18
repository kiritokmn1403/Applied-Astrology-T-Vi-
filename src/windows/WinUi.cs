// Applied Astrology v0.0.6 – bộ điều khiển giao diện native Windows (GDI+): khung kính Liquid Glass, bố cục, chữ, nút, lá số, lịch tháng.
using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Drawing.Text;
using System.Linq;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using AppliedAstrology.Core;

namespace AppliedAstrology
{
    static class Gfx
    {
        public static Color C(int argb) { return Color.FromArgb(argb); }
        public static Color A(int argb, int a) { return Color.FromArgb(a, Color.FromArgb(argb)); }
        public static GraphicsPath Round(RectangleF r, float rad)
        {
            var p = new GraphicsPath(); float d = Math.Min(rad * 2, Math.Min(r.Width, r.Height));
            if (d <= 1) { p.AddRectangle(r); return p; }
            p.AddArc(r.X, r.Y, d, d, 180, 90); p.AddArc(r.Right - d, r.Y, d, d, 270, 90); p.AddArc(r.Right - d, r.Bottom - d, d, d, 0, 90); p.AddArc(r.X, r.Bottom - d, d, d, 90, 90); p.CloseFigure(); return p;
        }
        public static Bitmap FromArgb(int[] px, int w, int h)
        {
            var b = new Bitmap(w, h, PixelFormat.Format32bppArgb); var d = b.LockBits(new Rectangle(0, 0, w, h), ImageLockMode.WriteOnly, PixelFormat.Format32bppArgb);
            for (int y = 0; y < h; y++) Marshal.Copy(px, y * w, d.Scan0 + y * d.Stride, w); b.UnlockBits(d); return b;
        }
        /// <summary>Vẽ ảnh nguồn (phủ khung vẽ cw×ch) vào bitmap PArgb đúng kích thước w×h, góc trên-trái trùng nhau</summary>
        public static Bitmap Scaled(Bitmap src, int cw, int ch, int w, int h)
        {
            var b = new Bitmap(w, h, PixelFormat.Format32bppPArgb);
            using (var g = Graphics.FromImage(b)) { g.InterpolationMode = InterpolationMode.HighQualityBilinear; g.PixelOffsetMode = PixelOffsetMode.HighQuality; g.DrawImage(src, new Rectangle(0, 0, cw, ch), new Rectangle(0, 0, src.Width, src.Height), GraphicsUnit.Pixel); }
            return b;
        }
        public static Bitmap ToPArgb(Bitmap src) { var b = new Bitmap(src.Width, src.Height, PixelFormat.Format32bppPArgb); using (var g = Graphics.FromImage(b)) g.DrawImageUnscaled(src, 0, 0); return b; }
        public static int[] ToArgb(Bitmap b)
        {
            int w = b.Width, h = b.Height; var px = new int[w * h]; var d = b.LockBits(new Rectangle(0, 0, w, h), ImageLockMode.ReadOnly, PixelFormat.Format32bppArgb);
            for (int y = 0; y < h; y++) Marshal.Copy(d.Scan0 + y * d.Stride, px, y * w, w); b.UnlockBits(d); return px;
        }
    }

    /// <summary>
    /// Cấu hình kính + nền tinh vân dùng chung cho cả cửa sổ.
    /// v0.0.5 (sửa lag): tinh vân dựng 1 lần trên "khung vẽ" phủ cả màn hình (đổi cỡ/phóng to cửa sổ không dựng lại);
    /// Full/BlurBmp là ảnh ĐÃ co giãn đúng kích thước vùng khách, định dạng 32bppPArgb → mọi lần vẽ đều là DrawImageUnscaled (nhanh nhất của GDI+).
    /// 0.0.4 vẽ DrawImage CÓ co giãn (nội suy) cho mỗi lần một điều khiển trong suốt cần nền → cuộn rất giật.
    /// </summary>
    sealed class GlassCtx
    {
        public string Mode = "liquid", BgStyle = "nebula"; public bool Cards = true, ChartGlass; public readonly Glass.Params P = new Glass.Params(); public Theme Th = Theme.CELESTIAL; public int Version;
        public Bitmap Full, BlurBmp; public int[] Blur; public int Bw, Bh; public const int SCALE = 4; public string BlurKey = "";
        Bitmap neb, blurSmall; int[] small; int cw, ch, clientW = -1, clientH = -1; string nebKey;
        public static long LastNebulaMs = -1, LastScaleMs = -1, Builds;
        public bool On { get { return Mode != "off"; } }
        public void Ensure(Size client)
        {
            int w = Math.Max(4, client.Width), h = Math.Max(4, client.Height);
            string style = On ? BgStyle : "plain"; int radius = Math.Max(1, (int)Math.Round(P.Blur / SCALE));
            if (neb == null || w > cw || h > ch)
            {
                Rectangle scr; try { scr = Screen.PrimaryScreen.Bounds; } catch { scr = new Rectangle(0, 0, 1920, 1080); }
                cw = (Math.Max(w, scr.Width) + 63) / 64 * 64; ch = (Math.Max(h, scr.Height) + 63) / 64 * 64; nebKey = null;
            }
            string nk = cw + "x" + ch + "/" + Th.Id + "/" + style;
            if (nk != nebKey)
            {
                var sw = Stopwatch.StartNew(); int hw = cw / 2, hh = ch / 2;
                var px = Glass.Nebula(hw, hh, Th, style, 20261116);
                if (neb != null) neb.Dispose(); neb = Gfx.FromArgb(px, hw, hh);
                small = Downsample(px, hw, hh, cw / SCALE, ch / SCALE); nebKey = nk; BlurKey = ""; clientW = -1; Builds++;
                LastNebulaMs = sw.ElapsedMilliseconds;
            }
            string bk = nk + "/" + radius;
            if (bk != BlurKey)
            {
                Bw = cw / SCALE; Bh = ch / SCALE; var bl = (int[])small.Clone(); Glass.BoxBlur(bl, Bw, Bh, radius); Blur = bl;
                if (blurSmall != null) blurSmall.Dispose(); blurSmall = Gfx.FromArgb(bl, Bw, Bh); BlurKey = bk; clientW = -1;
            }
            if (clientW != w || clientH != h)
            {
                var sw = Stopwatch.StartNew();
                var f2 = Gfx.Scaled(neb, cw, ch, w, h); var b2 = Gfx.Scaled(blurSmall, cw, ch, w, h);
                if (Full != null) Full.Dispose(); if (BlurBmp != null) BlurBmp.Dispose(); Full = f2; BlurBmp = b2; clientW = w; clientH = h;
                LastScaleMs = sw.ElapsedMilliseconds;
            }
        }
        static int[] Downsample(int[] px, int w, int h, int ow, int oh)
        {
            var o = new int[ow * oh];
            for (int y = 0; y < oh; y++)
            {
                int y0 = y * h / oh, y1 = Math.Max(y0 + 1, (y + 1) * h / oh);
                for (int x = 0; x < ow; x++)
                {
                    int x0 = x * w / ow, x1 = Math.Max(x0 + 1, (x + 1) * w / ow); long r = 0, g = 0, b = 0; int n = 0;
                    for (int yy = y0; yy < y1 && yy < h; yy++) for (int xx = x0; xx < x1 && xx < w; xx++) { int c = px[yy * w + xx]; r += (c >> 16) & 255; g += (c >> 8) & 255; b += c & 255; n++; }
                    if (n == 0) n = 1; o[y * ow + x] = unchecked((int)0xFF000000) | ((int)(r / n) << 16) | ((int)(g / n) << 8) | (int)(b / n);
                }
            }
            return o;
        }
    }

    /// <summary>Khung bố cục: dọc / ngang (có trọng số) / xuống dòng; có thể vẽ nền kính (thẻ hoặc thanh)</summary>
    class Box : Control
    {
        public enum Dir { V, H, Wrap }
        public Dir Direction = Dir.V; public int Gap; public Padding Pad; public int GlassKind; // 0 không vẽ, 1 thẻ (kính mờ), 2 thanh/hộp thoại (khúc xạ)
        public float Radius; public bool Selected; public int Solid; public GlassCtx G; public readonly Dictionary<Control, float> Weights = new Dictionary<Control, float>();
        Bitmap cache; string cacheKey;
        public Box() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint | ControlStyles.ResizeRedraw, true); BackColor = Color.Transparent; }
        public Box Add(Control c, float weight = 0) { Controls.Add(c); if (weight > 0) Weights[c] = weight; return this; }
        /// <summary>Ẩn/hiện có chủ đích (Control.Visible trả về false khi cửa sổ chưa hiển thị nên không dùng để bố cục)</summary>
        public static readonly HashSet<Control> Collapsed = new HashSet<Control>();
        public static void SetShown(Control c, bool on) { if (on) Collapsed.Remove(c); else Collapsed.Add(c); c.Visible = on; var b = c.Parent as Box; if (b != null) b.Dirty(); }
        static bool Shown(Control c) { return !Collapsed.Contains(c); }
        int cacheW = -1, cacheH; internal void Dirty() { cacheW = -1; var b = Parent as Box; if (b != null) b.Dirty(); }
        protected override void OnControlAdded(ControlEventArgs e) { base.OnControlAdded(e); Dirty(); }
        protected override void OnControlRemoved(ControlEventArgs e) { base.OnControlRemoved(e); Dirty(); }
        public virtual int PrefHeight(int width)
        {
            if (width == cacheW) return cacheH;
            cacheH = PrefHeightCore(width); cacheW = width; return cacheH;
        }
        int PrefHeightCore(int width)
        {
            int inner = Math.Max(1, width - Pad.Horizontal), h = 0;
            var vis = Controls.Cast<Control>().Where(Shown).ToList();
            if (Direction == Dir.V) { for (int i = 0; i < vis.Count; i++) { var c = vis[i]; h += c.Margin.Vertical + Pref(c, inner - c.Margin.Horizontal) + (i > 0 ? Gap : 0); } }
            else if (Direction == Dir.H) { foreach (var w in HWidths(vis, inner)) h = Math.Max(h, w.Item1.Margin.Vertical + Pref(w.Item1, w.Item2)); }
            else { int x = 0, lineH = 0; foreach (var c in vis) { var sz = WrapSize(c, inner); if (x > 0 && x + sz.Width > inner) { h += lineH + Gap; x = 0; lineH = 0; } x += sz.Width + Gap; lineH = Math.Max(lineH, sz.Height); } h += lineH; }
            return h + Pad.Vertical;
        }
        static int Pref(Control c, int w) { var b = c as Box; if (b != null) return b.PrefHeight(w); var t = c as IPref; if (t != null) return t.PrefHeight(w); return c.Height; }
        static Size WrapSize(Control c, int max) { var t = c as IPref; if (t != null) { int w = Math.Min(max, t.PrefWidth()); return new Size(w, t.PrefHeight(w)); } var b = c as Box; if (b != null && b.FixedWidth > 0) return new Size(Math.Min(max, b.FixedWidth), b.PrefHeight(Math.Min(max, b.FixedWidth))); return c.Size; }
        public int FixedWidth;
        List<Tuple<Control, int>> HWidths(List<Control> vis, int inner)
        {
            int fixedW = vis.Where(c => !Weights.ContainsKey(c)).Sum(c => (c is IPref ? ((IPref)c).PrefWidth() : (c is Box && ((Box)c).FixedWidth > 0 ? ((Box)c).FixedWidth : c.Width)) + c.Margin.Horizontal) + Gap * Math.Max(0, vis.Count - 1);
            float tw = vis.Where(c => Weights.ContainsKey(c)).Sum(c => Weights[c]); int free = Math.Max(0, inner - fixedW);
            return vis.Select(c => Tuple.Create(c, Weights.ContainsKey(c) ? (int)(free * Weights[c] / tw) - c.Margin.Horizontal : (c is IPref ? ((IPref)c).PrefWidth() : (c is Box && ((Box)c).FixedWidth > 0 ? ((Box)c).FixedWidth : c.Width)))).ToList();
        }
        protected override void OnLayout(LayoutEventArgs e)
        {
            int inner = Math.Max(1, Width - Pad.Horizontal), x = Pad.Left, y = Pad.Top;
            var vis = Controls.Cast<Control>().Where(Shown).ToList();
            if (Direction == Dir.V) foreach (var c in vis) { int w = inner - c.Margin.Horizontal, h = Pref(c, w); c.SetBounds(Pad.Left + c.Margin.Left, y + c.Margin.Top, w, h); y += h + c.Margin.Vertical + Gap; }
            else if (Direction == Dir.H) { foreach (var t in HWidths(vis, inner)) { var c = t.Item1; int h = Pref(c, t.Item2); c.SetBounds(x + c.Margin.Left, Pad.Top + c.Margin.Top, t.Item2, Math.Max(h, Height - Pad.Vertical - c.Margin.Vertical > h && c is Box ? h : h)); x += t.Item2 + c.Margin.Horizontal + Gap; } }
            else { int lineH = 0; foreach (var c in vis) { var sz = WrapSize(c, inner); if (x > Pad.Left && x - Pad.Left + sz.Width > inner) { x = Pad.Left; y += lineH + Gap; lineH = 0; } c.SetBounds(x, y, sz.Width, sz.Height); x += sz.Width + Gap; lineH = Math.Max(lineH, sz.Height); } }
            foreach (var b in vis.OfType<Box>()) b.PerformLayout();
        }
        /// <summary>
        /// v0.0.5: nền của mỗi khung kính (gồm phần nền phía sau ở góc bo + kính) được dựng MỘT lần vào bộ đệm PArgb theo khóa
        /// (kích thước, vị trí, phiên bản cấu hình, ảnh nền, màu, bán kính). Các điều khiển con trong suốt (chữ, nút) khi vẽ lại chỉ chép
        /// vùng tương ứng từ bộ đệm. Tối đa 48 bộ đệm (LRU) để không tốn RAM khi danh sách dài.
        /// </summary>
        static readonly LinkedList<Box> bgLru = new LinkedList<Box>(); const int BG_MAX = 48; public static long BgBuilds, LiquidBuilds;
        Bitmap bgCache; string bgKey, bgKeyNoPos; LinkedListNode<Box> lruNode;
        /// <summary>v0.0.6: thời điểm (Environment.TickCount) hết "đang cuộn"; MainForm đặt lại mỗi lần cuộn</summary>
        internal static int ScrollHoldUntil; internal static long HeldPaints;
        internal static bool Scrolling { get { return unchecked(Environment.TickCount - ScrollHoldUntil) < 0; } }
        void Touch()
        {
            if (lruNode != null) bgLru.Remove(lruNode); else lruNode = new LinkedListNode<Box>(this);
            bgLru.AddLast(lruNode);
            while (bgLru.Count > BG_MAX) { var old = bgLru.First.Value; bgLru.RemoveFirst(); old.lruNode = null; if (old.bgCache != null) { old.bgCache.Dispose(); old.bgCache = null; old.bgKey = null; } }
        }
        /// <summary>
        /// v0.0.6: true khi mọi khung cha (tới khung cuộn hoặc form) đều trong suốt, không vẽ kính → nền ngay sau điều khiển này
        /// chính là ảnh tinh vân G.Full tại vị trí của nó trong form. Khi đó vẽ thẳng ảnh (1 lần chép, không co giãn) thay cho
        /// cơ chế "nền trong suốt" của WinForms (gọi lần lượt nền của từng khung cha → rất chậm khi cuộn).
        /// </summary>
        internal static bool PlainChain(Control c)
        {
            for (var p = c; p != null; p = p.Parent)
            {
                if (p is DBPanel || p is Form) return true;
                var b = p as Box; if (b == null || (b.GlassKind != 0 && b.G != null)) return false;
            }
            return false;
        }
        internal static bool PaintFull(Control c, Graphics g, GlassCtx G)
        {
            if (G == null || G.Full == null) return false; var form = c.FindForm(); if (form == null) return false;
            var o = form.PointToClient(c.PointToScreen(Point.Empty)); g.DrawImageUnscaled(G.Full, -o.X, -o.Y); return true;
        }
        protected override void OnPaintBackground(PaintEventArgs e)
        {
            if (GlassKind == 0 || G == null)
            {
                var gc = G ?? FindG(); if (gc != null && PlainChain(this) && PaintFull(this, e.Graphics, gc)) return;
                base.OnPaintBackground(e); return;
            }
            if (Width <= 0 || Height <= 0) return;
            var form = FindForm(); Point o = form == null ? Point.Empty : form.PointToClient(PointToScreen(Point.Empty));
            var th = G.Th; int baseC = Solid != 0 ? Solid : (GlassKind == 2 ? th.Bg2 : th.Card);
            bool glass = G.On && (GlassKind == 2 || G.Cards) && G.BlurBmp != null && form != null;
            string keyNoPos = Width + "x" + Height + "/" + G.Version + "/" + G.BlurKey + "/" + baseC + "/" + Radius + "/" + Selected + "/" + glass + "/" + (Parent == null ? 0 : Parent.GetHashCode());
            string key = keyNoPos + "@" + o.X + "," + o.Y;
            // v0.0.6: ĐANG CUỘN → dùng lại nền kính đã dựng nếu chỉ khác vị trí (nền phía sau đã làm mờ mạnh + phủ màu nên lệch vài
            // điểm ảnh không thấy được); cuộn dừng ~0,2 s thì MainForm vẽ lại 1 lần với vị trí chính xác. 0.0.5 dựng lại ~8 thẻ mỗi khung.
            bool hold = bgCache != null && key != bgKey && keyNoPos == bgKeyNoPos && Scrolling;
            if (hold) HeldPaints++;
            else if (key != bgKey || bgCache == null)
            {
                if (bgCache == null || bgCache.Width != Width || bgCache.Height != Height) { if (bgCache != null) bgCache.Dispose(); bgCache = new Bitmap(Width, Height, PixelFormat.Format32bppPArgb); }
                using (var g = Graphics.FromImage(bgCache))
                {
                    g.Clear(Color.Transparent);
                    // nền phía sau (cha) – để góc bo hòa vào nền
                    if (Parent != null && Radius > 0 && PlainChain(Parent) && G.Full != null) g.DrawImageUnscaled(G.Full, -o.X, -o.Y);   // v0.0.6: nhanh
                    else if (Parent != null && Radius > 0)
                    {
                        var st0 = g.Save(); g.TranslateTransform(-Left, -Top);
                        try { using (var pe = new PaintEventArgs(g, new Rectangle(Left, Top, Width, Height))) { InvokePaintBackground(Parent, pe); } } catch { }
                        g.Restore(st0);
                    }
                    PaintGlass(g, o, baseC, glass);
                }
                bgKey = key; bgKeyNoPos = keyNoPos; BgBuilds++;
            }
            Touch();
            e.Graphics.DrawImageUnscaled(bgCache, 0, 0);
        }
        void PaintGlass(Graphics gr, Point o, int baseC, bool glass)
        {
            gr.SmoothingMode = SmoothingMode.AntiAlias; var th = G.Th; var r = new RectangleF(0, 0, Width - 1, Height - 1);
            using (var p = Gfx.Round(r, Radius))
            {
                if (!glass)
                {
                    using (var b = new SolidBrush(Gfx.C(baseC))) gr.FillPath(b, p); using (var pen = new Pen(Gfx.C(Selected ? th.Gold : th.Line), Selected ? 2 : 1)) gr.DrawPath(pen, p);
                    return;
                }
                if (GlassKind == 2 && G.Mode == "liquid" && (long)Width * Height <= 4000000L)
                {
                    string key = Width + "x" + Height + "@" + o.X + "," + o.Y + "/" + G.Version + "/" + G.P.Key() + "/" + baseC + "/" + Radius + "/" + G.BlurKey;
                    if (key != cacheKey)
                    {
                        var mp = Glass.GetMaps(Width, Height, (int)Radius, G.P); var buf = new int[Width * Height];
                        Glass.Render(G.Blur, G.Bw, G.Bh, GlassCtx.SCALE, o.X, o.Y, mp, G.P, baseC, buf);
                        if (cache != null) cache.Dispose(); using (var raw = Gfx.FromArgb(buf, Width, Height)) cache = Gfx.ToPArgb(raw); cacheKey = key; LiquidBuilds++;
                    }
                    gr.DrawImageUnscaled(cache, 0, 0);
                }
                else
                {
                    var st = gr.Save(); gr.SetClip(p); gr.DrawImageUnscaled(G.BlurBmp, -o.X, -o.Y); gr.Restore(st);
                    using (var b = new SolidBrush(Gfx.A(baseC, (int)(255 * Math.Min(0.92, G.P.Tint + 0.12))))) gr.FillPath(b, p);
                    using (var lg = new LinearGradientBrush(new RectangleF(0, 0, Math.Max(2, Width * 0.6f), Math.Max(2, Height)), Gfx.A(unchecked((int)0xFFFFFFFF), (int)(230 * G.P.Specular)), Gfx.A(unchecked((int)0xFFFFFFFF), (int)(40 * G.P.Specular)), 60f))
                    using (var pen = new Pen(lg, 1f)) gr.DrawPath(pen, p);
                }
                if (Selected) using (var pen = new Pen(Gfx.C(th.Gold), 2)) gr.DrawPath(pen, p);
            }
        }
        GlassCtx FindG() { var f = FindForm() as MainForm; return f == null ? null : f.G; }
        public bool HasLiquidCache { get { return cache != null && cacheKey != null; } }
        protected override void OnPaint(PaintEventArgs e) { }
        protected override void Dispose(bool d) { if (d) { if (cache != null) cache.Dispose(); if (lruNode != null) { bgLru.Remove(lruNode); lruNode = null; } if (bgCache != null) { bgCache.Dispose(); bgCache = null; } } base.Dispose(d); }
    }
    interface IPref { int PrefHeight(int width); int PrefWidth(); }

    /// <summary>Đoạn chữ tự xuống dòng (TextRenderer – hỗ trợ font fallback cho 68 ngôn ngữ)</summary>
    class TextBlock : Control, IPref
    {
        public Color Color; public bool Wrap = true, Rtl; public float Size; public FontStyle Style;
        public TextBlock() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.ResizeRedraw, true); BackColor = Color.Transparent; }
        TextFormatFlags Flags { get { var f = TextFormatFlags.NoPadding | TextFormatFlags.NoPrefix | (Wrap ? TextFormatFlags.WordBreak : TextFormatFlags.EndEllipsis | TextFormatFlags.SingleLine); if (Rtl) f |= TextFormatFlags.Right | TextFormatFlags.RightToLeft; return f; } }
        int cw = -1, ch, pw = -1;
        public int PrefHeight(int width) { if (width == cw) return ch; ch = TextRenderer.MeasureText(string.IsNullOrEmpty(Text) ? " " : Text, Font, new Size(Math.Max(10, width), int.MaxValue), Flags).Height + 2; cw = width; return ch; }
        public int PrefWidth() { if (pw < 0) pw = TextRenderer.MeasureText(string.IsNullOrEmpty(Text) ? " " : Text, Font, new Size(int.MaxValue, int.MaxValue), TextFormatFlags.NoPadding | TextFormatFlags.NoPrefix | TextFormatFlags.SingleLine).Width + 2; return pw; }
        protected override void OnFontChanged(EventArgs e) { cw = -1; pw = -1; base.OnFontChanged(e); var b = Parent as Box; if (b != null) b.Dirty(); }
        protected override void OnPaint(PaintEventArgs e) { e.Graphics.TextRenderingHint = TextRenderingHint.ClearTypeGridFit; TextRenderer.DrawText(e.Graphics, Text, Font, ClientRectangle, Color, Flags); }
        protected override void OnTextChanged(EventArgs e) { cw = -1; pw = -1; base.OnTextChanged(e); Invalidate(); var b = Parent as Box; if (b != null) b.Dirty(); if (Parent != null) Parent.PerformLayout(); }
    }

    /// <summary>Nút bo tròn vẽ tay (chính / thường / nhỏ / chip)</summary>
    class GButton : Control, IPref
    {
        public bool Primary, SmallBtn, Chip; public GlassCtx G; bool hover, down; public float S = 1;
        public GButton() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.ResizeRedraw, true); BackColor = Color.Transparent; Cursor = Cursors.Hand; TabStop = true; }
        public int PrefWidth() { return TextRenderer.MeasureText(Text, Font).Width + (int)((SmallBtn ? 18 : 26) * S); }
        public void PerformClickPublic() { OnClick(EventArgs.Empty); }
        protected override void OnTextChanged(EventArgs e) { base.OnTextChanged(e); Invalidate(); var b = Parent as Box; if (b != null) b.Dirty(); }
        public int PrefHeight(int w) { return (int)((SmallBtn || Chip ? 32 : 38) * S); }
        protected override void OnMouseEnter(EventArgs e) { hover = true; Invalidate(); base.OnMouseEnter(e); }
        protected override void OnMouseLeave(EventArgs e) { hover = false; down = false; Invalidate(); base.OnMouseLeave(e); }
        protected override void OnMouseDown(MouseEventArgs e) { down = true; Invalidate(); base.OnMouseDown(e); }
        protected override void OnMouseUp(MouseEventArgs e) { down = false; Invalidate(); base.OnMouseUp(e); }
        protected override void OnKeyDown(KeyEventArgs e) { if (e.KeyCode == Keys.Enter || e.KeyCode == Keys.Space) OnClick(EventArgs.Empty); base.OnKeyDown(e); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var th = G.Th; var gr = e.Graphics; gr.SmoothingMode = SmoothingMode.AntiAlias;
            int fill = Primary ? th.Gold : Theme.Alpha(th.Card2, 210); if (down) fill = Theme.Mix(fill, th.Gold, 0.35); else if (hover && !Primary) fill = Theme.Mix(fill | unchecked((int)0xFF000000), th.Gold, 0.12);
            using (var p = Gfx.Round(new RectangleF(0.5f, 0.5f, Width - 1.5f, Height - 1.5f), (Chip ? 15 : 8) * S))
            { using (var b = new SolidBrush(Gfx.C(fill))) gr.FillPath(b, p); using (var pen = new Pen(Gfx.C(Primary || hover || Focused ? th.Gold : th.Line))) gr.DrawPath(pen, p); }
            TextRenderer.DrawText(gr, Text, Font, ClientRectangle, Gfx.C(Primary ? (th.Dark ? unchecked((int)0xFF1A1205) : unchecked((int)0xFFFFFFFF)) : th.Text), TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter | TextFormatFlags.NoPrefix | TextFormatFlags.SingleLine);
        }
    }

    /// <summary>Lá số: bàn 4×4 hoặc thẻ từng cung; cũng dùng để xuất PNG</summary>
    class ChartBoard : Control, IPref
    {
        public static readonly int[,] LAYOUT = { { 5, 6, 7, 8 }, { 4, -1, -1, 9 }, { 3, -1, -1, 10 }, { 2, 1, 0, 11 } };
        public Chart Chart; public int Sel = -1, Single = -2; public bool Ts = true, Bs, Tt = true, Luu = true, GlassCells; public int[] Paper; public Theme Th; public I18n T; public Action<int> OnPick; public float S = 1, Fs = 1;
        public ChartBoard() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.ResizeRedraw, true); BackColor = Color.Transparent; }
        bool IsSingle { get { return Single > -2; } }
        float U(int w) { return IsSingle ? 13.2f * S * Fs : w / 4f / 12.4f; }
        public int PrefWidth() { return 600; }
        int bcw = -1, bch;
        public int PrefHeight(int w)
        {
            if (w == bcw) return bch; bch = PrefHeightCore(w); bcw = w; return bch;
        }
        int PrefHeightCore(int w)
        {
            if (Chart == null) return 100; float u = U(w);
            using (var bmp = new Bitmap(1, 1)) using (var g = Graphics.FromImage(bmp))
            {
                if (IsSingle) return (int)(Single < 0 ? Center(g, new RectangleF(0, 0, w, 9999), u, true) : PalaceDraw(g, Single, new RectangleF(0, 0, w, 9999), u, true)) + 1;
                float cw = w / 4f, maxH = 0; for (int chi = 0; chi < 12; chi++) maxH = Math.Max(maxH, PalaceDraw(g, chi, new RectangleF(0, 0, cw - u * 0.4f, 9999), u, true));
                float centerH = Center(g, new RectangleF(0, 0, cw * 2, 9999), u, true) / 2;
                return (int)(Math.Max(Math.Max(maxH, centerH), cw * 1.05f) + u * 0.4f) * 4;
            }
        }
        protected override void OnPaint(PaintEventArgs e) { if (Chart != null) Draw(e.Graphics, Width, Height); }
        public void Draw(Graphics g, int w, int h)
        {
            g.SmoothingMode = SmoothingMode.AntiAlias; g.TextRenderingHint = TextRenderingHint.AntiAliasGridFit; float u = U(w);
            if (IsSingle) { var r = new RectangleF(0, 0, w - 1, h - 1); if (Single < 0) Center(g, r, u, false); else PalaceDraw(g, Single, r, u, false); return; }
            float cw = w / 4f, chh = h / 4f, gp = u * 0.2f;
            for (int r = 0; r < 4; r++) for (int c = 0; c < 4; c++) { int chi = LAYOUT[r, c]; if (chi < 0) continue; var rr = new RectangleF(c * cw + gp, r * chh + gp, cw - gp * 2, chh - gp * 2); var st = g.Save(); g.SetClip(rr); PalaceDraw(g, chi, rr, u, false); g.Restore(st); }
            Center(g, new RectangleF(cw + gp, chh + gp, cw * 2 - gp * 2, chh * 2 - gp * 2), u, false);
        }
        protected override void OnMouseUp(MouseEventArgs e)
        {
            base.OnMouseUp(e); if (Chart == null || OnPick == null) return;
            if (IsSingle) { if (Single >= 0) OnPick(Single); return; }
            int c = (int)(e.X / (Width / 4f)), r = (int)(e.Y / (Height / 4f));
            if (c >= 0 && c < 4 && r >= 0 && r < 4 && LAYOUT[r, c] >= 0) OnPick(LAYOUT[r, c]);
        }
        readonly Dictionary<string, Font> fonts = new Dictionary<string, Font>();
        Font F(float px, bool bold) { string k = px.ToString("0.0") + bold; Font f; if (!fonts.TryGetValue(k, out f)) { f = new Font("Segoe UI", Math.Max(1, px), bold ? FontStyle.Bold : FontStyle.Regular, GraphicsUnit.Pixel); fonts[k] = f; } return f; }
        static readonly StringFormat MS = new StringFormat(StringFormat.GenericTypographic) { FormatFlags = StringFormatFlags.MeasureTrailingSpaces | StringFormatFlags.NoWrap };
        float Measure(Graphics g, string s, float px, bool bold) { return g.MeasureString(s, F(px, bold), PointF.Empty, MS).Width; }
        void Txt(Graphics g, string s, float x, float y, float px, int color, bool bold, int align, float maxW)
        {
            var f = F(px, bold); var sf = new StringFormat(StringFormat.GenericTypographic) { Trimming = StringTrimming.EllipsisCharacter, FormatFlags = StringFormatFlags.NoWrap };
            using (var b = new SolidBrush(Gfx.C(color)))
            {
                if (maxW > 0 && align == 0) { g.DrawString(s, f, b, new RectangleF(x, y, maxW, px * 1.4f), sf); return; }
                float w = Measure(g, s, px, bold); float dx = align == 1 ? x - w / 2 : align == 2 ? x - w : x; g.DrawString(s, f, b, dx, y, sf);
            }
        }
        bool InTp(int chi) { if (Sel < 0) return false; var tp = Chart.TamPhuong(Sel); for (int k = 1; k < 4; k++) if (tp[k] == chi) return true; return false; }
        float PalaceDraw(Graphics g, int chi, RectangleF r, float u, bool measure)
        {
            var pa = Chart.Palaces[chi]; var th = Th; int textC = th.Text, muted = th.Muted, nameC = th.Gold2;
            if (Paper != null) { textC = Paper[1]; muted = Theme.Alpha(Paper[1], 200); nameC = Paper[1]; }
            if (!measure)
            {
                int bg = Paper != null ? Paper[0] : (Sel == chi ? th.Sel : th.Card2);
                using (var p = Gfx.Round(r, u * 0.7f))
                {
                    using (var b = new SolidBrush(Gfx.C(GlassCells && Paper == null && Sel != chi ? Theme.Alpha(bg, 150) : bg))) g.FillPath(b, p);
                    using (var pen = new Pen(Gfx.C(Sel == chi ? th.Gold : InTp(chi) ? th.Accent : pa.IsMenh ? Theme.Alpha(th.Gold, 170) : th.Line), Sel == chi ? u * 0.16f : Math.Max(1, u * 0.07f))) g.DrawPath(pen, p);
                }
            }
            float pad = u * 0.45f, x = r.Left + pad, w = r.Width - pad * 2, y = r.Top + pad;
            string name = T.Palace(pa.Name).ToUpper();
            if (!measure)
            {
                float cw = Measure(g, pa.CanChi, u * 0.92f, false) + u * 0.6f;
                Txt(g, pa.CanChi, r.Right - pad, y + u * 0.1f, u * 0.92f, muted, false, 2, 0);
                float nw = Math.Min(w - cw, Measure(g, name, u * 1.08f, true));
                Txt(g, name, x, y, u * 1.08f, nameC, true, 0, w - cw);
                float bx = x + nw + u * 0.3f;
                var badges = new List<string[]>(); if (pa.IsMenh) badges.Add(new[] { T.T("menh"), "g" }); if (pa.IsThan) badges.Add(new[] { T.T("than"), "a" }); if (pa.Tuan) badges.Add(new[] { T.T("tuan"), "b" }); if (pa.Triet) badges.Add(new[] { T.T("triet"), "b" });
                foreach (var b in badges)
                {
                    float bw = Measure(g, b[0], u * 0.75f, true) + u * 0.5f; if (bx + bw > r.Right - pad - cw) break;
                    using (var p = Gfx.Round(new RectangleF(bx, y + u * 0.15f, bw, u * 1.0f), u * 0.25f)) using (var br = new SolidBrush(Gfx.C(b[1] == "g" ? th.Gold : b[1] == "a" ? th.Accent : th.Bad))) g.FillPath(br, p);
                    Txt(g, b[0], bx + bw / 2, y + u * 0.22f, u * 0.75f, b[1] == "b" ? unchecked((int)0xFFFFFFFF) : (th.Dark ? unchecked((int)0xFF1A1205) : unchecked((int)0xFFFFFFFF)), true, 1, 0);
                    bx += bw + u * 0.2f;
                }
            }
            y += u * 1.45f;
            if (pa.Major.Count == 0) { if (!measure) Txt(g, T.T("vcd"), x, y, u, muted, true, 0, w); y += u * 1.25f; }
            foreach (var s in pa.Major) { if (!measure) { Txt(g, s.Name, x, y, u * 1.02f, Paper != null ? textC : th.Element(s.Element), true, 0, w); Txt(g, "(" + s.Bright + ")", x + Measure(g, s.Name, u * 1.02f, true) + u * 0.3f, y + u * 0.12f, u * 0.82f, muted, false, 0, 0); } y += u * 1.25f; }
            var left = pa.Good.Select(s => Tuple.Create(s.Name, s.AttachedTo != null ? th.Hoa(s.Name) : th.Good)).Concat(pa.Neutral.Select(s => Tuple.Create(s.Name, textC))).ToList();
            var right = pa.Bad.Select(s => Tuple.Create(s.Name, s.AttachedTo != null ? th.Hoa(s.Name) : th.Bad)).ToList(); if (Luu) right.AddRange(pa.Luu.Select(l => Tuple.Create(l, th.Purple)));
            int rows = Math.Max(left.Count, right.Count); float ry = y + u * 0.15f, colW = w / 2 - u * 0.2f;
            if (!measure) for (int i = 0; i < rows; i++) { if (i < left.Count) Txt(g, left[i].Item1, x, ry, u * 0.86f, Paper != null ? textC : left[i].Item2, false, 0, colW); if (i < right.Count) Txt(g, right[i].Item1, x + w / 2 + u * 0.2f, ry, u * 0.86f, Paper != null ? textC : right[i].Item2, false, 0, colW); ry += u * 1.06f; }
            y += u * 0.15f + rows * u * 1.06f;
            var ring = new List<string>(); if (Ts && pa.RingTrangSinh != null) ring.Add(pa.RingTrangSinh); if (Bs && pa.RingBacSi != null) ring.Add(pa.RingBacSi); if (Tt && pa.RingThaiTue != null) ring.Add(pa.RingThaiTue);
            if (ring.Count > 0) { if (!measure) Txt(g, string.Join(" · ", ring), x, y + u * 0.1f, u * 0.8f, muted, false, 0, w); y += u * 1.05f; }
            y += u * 0.2f;
            if (!measure)
            {
                using (var pen = new Pen(Gfx.A(th.Muted, 90), Math.Max(1, u * 0.05f))) g.DrawLine(pen, x, y, x + w, y);
                bool cdh = Chart.CurrentDaiHan == chi, cth = Chart.CurrentTieuHan == chi;
                Txt(g, T.T("dai_han") + " " + pa.DaiHan, x, y + u * 0.2f, u * 0.82f, cdh ? th.Gold : muted, cdh, 0, w * 0.7f);
                Txt(g, pa.TieuHan, x + w, y + u * 0.2f, u * 0.82f, cth ? th.Accent : muted, cth, 2, 0);
            }
            y += u * 1.25f + pad; return y - r.Top;
        }
        public List<string[]> CenterRows()
        {
            var c = Chart; var t = T;
            return new List<string[]> { new[] { t.T("solar_date"), c.SDay + "/" + c.SMonth + "/" + c.SYear }, new[] { t.T("lunar_date"), c.LunarDate.Day + "/" + c.LunarDate.Month + (c.LunarDate.Leap ? " (N)" : "") + "/" + c.LunarDate.Year }, new[] { t.T("year_cc"), c.YearCanChi }, new[] { t.T("month_cc"), c.MonthCanChi }, new[] { t.T("day_cc"), c.DayCanChi }, new[] { t.T("hour_cc"), c.HourCanChi }, new[] { t.T("am_duong"), c.AmDuong + " · " + c.AmDuongLy }, new[] { t.T("ban_menh"), c.NapAm }, new[] { t.T("cuc"), c.CucName }, new[] { t.T("menh_cuc"), c.MenhCuc }, new[] { t.T("menh_chu"), c.MenhChu }, new[] { t.T("than_chu"), c.ThanChu }, new[] { t.T("f_viewyear"), c.ViewYear + " (" + c.ViewYearCanChi + ") · " + t.T("age") + " " + c.Age } };
        }
        float Center(Graphics g, RectangleF r, float u, bool measure)
        {
            var th = Th; int nameC = Paper != null ? Paper[1] : th.Gold2, txt = Paper != null ? Paper[1] : th.Text, mut = Paper != null ? Paper[1] : th.Muted;
            if (!measure)
            {
                using (var p = Gfx.Round(r, u * 0.8f)) { using (var b = new SolidBrush(Gfx.C(Paper != null ? Paper[0] : (GlassCells ? Theme.Alpha(th.Card, 150) : th.Card)))) g.FillPath(b, p); using (var pen = new Pen(Gfx.A(th.Gold, 200), Math.Max(1, u * 0.08f))) g.DrawPath(pen, p); }
                float cx = r.X + r.Width / 2, cy = r.Top + u * 2.4f, rr = u * 1.8f;
                using (var gold = new SolidBrush(Gfx.C(th.Gold))) using (var dark = new SolidBrush(Color.FromArgb(0x3B, 0x2A, 0x6B)))
                {
                    g.FillPie(gold, cx - rr, cy - rr, rr * 2, rr * 2, -90, 180); g.FillPie(dark, cx - rr, cy - rr, rr * 2, rr * 2, 90, 180);
                    g.FillEllipse(gold, cx - rr / 2, cy, rr, rr); g.FillEllipse(dark, cx - rr / 2, cy - rr, rr, rr);
                    g.FillEllipse(dark, cx - rr / 7, cy + rr / 2 - rr / 7, rr * 2 / 7, rr * 2 / 7); g.FillEllipse(gold, cx - rr / 7, cy - rr / 2 - rr / 7, rr * 2 / 7, rr * 2 / 7);
                }
            }
            float y = r.Top + u * 4.6f;
            if (!measure) Txt(g, Chart.Name, r.X + r.Width / 2, y, u * 1.35f, nameC, true, 1, 0);
            y += u * 1.8f;
            foreach (var row in CenterRows())
            {
                if (!measure) { float lw = Measure(g, row[0] + ": ", u * 0.88f, false), vw = Measure(g, row[1], u * 0.92f, true), tot = Math.Min(r.Width - u, lw + vw), sx = r.X + r.Width / 2 - tot / 2; Txt(g, row[0] + ": ", sx, y, u * 0.88f, mut, false, 0, 0); Txt(g, row[1], sx + lw, y, u * 0.92f, txt, true, 0, r.Width - u - lw); }
                y += u * 1.2f;
            }
            return y + u * 0.6f - r.Top;
        }
        public Bitmap RenderPng()
        {
            var b = new Bitmap(1400, 1460, PixelFormat.Format32bppArgb);
            using (var g = Graphics.FromImage(b))
            {
                g.Clear(Gfx.C(Th.Bg)); int sel = Sel; Sel = -1; bool gc = GlassCells; GlassCells = false; int sg = Single; Single = -2;
                Draw(g, 1400, 1400); Txt(g, "Applied Astrology " + Program.Version + " · Tử Vi Ứng Dụng – Bửu Đình", 700, 1416, 20, Th.Muted, false, 1, 0);
                Sel = sel; GlassCells = gc; Single = sg;
            }
            return b;
        }
    }

    /// <summary>Lịch tháng Âm Dương</summary>
    class MonthGrid : Control, IPref
    {
        public int Y, M, SelD; public string[] Wds; public Theme Th; public bool GlassCells; public float S = 1, Fs = 1; public Action<int> OnDay; readonly Dictionary<int, DayInfo> info = new Dictionary<int, DayInfo>();
        public MonthGrid() { SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.ResizeRedraw, true); BackColor = Color.Transparent; }
        public void Set(int y, int m, int d) { Y = y; M = m; SelD = d; info.Clear(); for (int i = 1; i <= Lunar.DaysInMonth(m, y); i++) info[i] = Cal.DayInfo(i, m, y); Invalidate(); }
        public DayInfo Info(int d) { DayInfo r; return info.TryGetValue(d, out r) ? r : null; }
        int Lead { get { return (Lunar.Weekday(1, M, Y) + 6) % 7; } }
        int Rows { get { return (Lead + Lunar.DaysInMonth(M, Y) + 6) / 7; } }
        public int PrefWidth() { return 400; }
        public int PrefHeight(int w) { float cw = w / 7f, ch = Math.Max(56 * S * Fs, cw * 0.8f); return (int)(28 * S * Fs + Rows * ch); }
        float CellH { get { return (Height - 28 * S * Fs) / Rows; } }
        static void Tx(Graphics g, string s, Font f, Color c, float x, float y, StringAlignment al) { using (var b = new SolidBrush(c)) g.DrawString(s, f, b, x, y, new StringFormat(StringFormat.GenericTypographic) { Alignment = al }); }
        protected override void OnPaint(PaintEventArgs e)
        {
            if (Th == null) return; var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias; g.TextRenderingHint = TextRenderingHint.AntiAliasGridFit;
            float w = Width, cw = w / 7f, hh = 28 * S * Fs, ch = CellH, gp = 1.5f * S;
            using (var fH = new Font("Segoe UI", 12 * S * Fs, FontStyle.Bold, GraphicsUnit.Pixel)) for (int i = 0; i < 7; i++) Tx(g, Wds[i], fH, Gfx.C(i >= 5 ? Th.Bad : Th.Muted), cw * i + cw / 2, 6 * S, StringAlignment.Center);
            var now = DateTime.Now; int lead = Lead, n = Lunar.DaysInMonth(M, Y);
            using (var fD = new Font("Segoe UI", 16 * S * Fs, FontStyle.Bold, GraphicsUnit.Pixel)) using (var fL = new Font("Segoe UI", 11 * S * Fs, GraphicsUnit.Pixel)) using (var fLb = new Font("Segoe UI", 11 * S * Fs, FontStyle.Bold, GraphicsUnit.Pixel)) using (var fT = new Font("Segoe UI", 8.5f * S * Fs, GraphicsUnit.Pixel))
                for (int d = 1; d <= n; d++)
                {
                    int idx = lead + d - 1, col = idx % 7, row = idx / 7; var di = info[d];
                    var r = new RectangleF(col * cw + gp, hh + row * ch + gp, cw - gp * 2, ch - gp * 2);
                    bool today = d == now.Day && M == now.Month && Y == now.Year, sel = d == SelD;
                    using (var p = Gfx.Round(r, 8 * S)) { using (var b = new SolidBrush(Gfx.C(sel ? Th.Sel : (GlassCells ? Theme.Alpha(Th.Card, 150) : Th.Card)))) g.FillPath(b, p); using (var pen = new Pen(Gfx.C(today || sel ? Th.Gold : Th.Line), today ? 2 * S : 1)) g.DrawPath(pen, p); }
                    Tx(g, d.ToString(), fD, Gfx.C(col >= 5 ? Th.Bad : Th.Text), r.X + 5 * S, r.Y + 3 * S, StringAlignment.Near);
                    if (di.Events.Count > 0) using (var b = new SolidBrush(Gfx.C(Th.Accent))) g.FillRectangle(b, r.X + 5 * S, r.Y + 3 * S + 19 * S * Fs, 12 * S * Fs, 1.6f * S);
                    bool ld = di.Lunar.Day == 1 || di.Lunar.Day == 15;
                    string lt = di.Lunar.Day == 1 || d == 1 ? di.Lunar.Day + "/" + di.Lunar.Month + (di.Lunar.Leap ? "*" : "") : di.Lunar.Day.ToString();
                    Tx(g, lt, ld ? fLb : fL, Gfx.C(ld ? Th.Gold : Th.Muted), r.Right - 4 * S, r.Bottom - 15 * S * Fs - (di.TietStart ? 10 * S * Fs : 0), StringAlignment.Far);
                    if (di.TietStart) using (var b = new SolidBrush(Gfx.C(Th.Muted))) g.DrawString(di.Tiet, fT, b, new RectangleF(r.X + 2 * S, r.Bottom - 12 * S * Fs, r.Width - 5 * S, 11 * S * Fs), new StringFormat(StringFormat.GenericTypographic) { Alignment = StringAlignment.Far, Trimming = StringTrimming.Character, FormatFlags = StringFormatFlags.NoWrap });
                    if (di.HoangDao) using (var b = new SolidBrush(Gfx.C(Th.Good))) g.FillEllipse(b, r.Right - 10 * S, r.Y + 4 * S, 6 * S, 6 * S);
                }
        }
        protected override void OnMouseUp(MouseEventArgs e)
        {
            base.OnMouseUp(e); float cw = Width / 7f, hh = 28 * S * Fs; int col = (int)(e.X / cw), row = (int)((e.Y - hh) / CellH), d = row * 7 + col - Lead + 1;
            if (e.Y > hh && d >= 1 && d <= Lunar.DaysInMonth(M, Y) && OnDay != null) OnDay(d);
        }
    }

    /// <summary>Biểu tượng thanh tab (vector, không phụ thuộc phông emoji)</summary>
    static class Icons
    {
        public static void Draw(Graphics g, int kind, RectangleF r, Color c)
        {
            var st = g.Save(); g.SmoothingMode = SmoothingMode.AntiAlias; g.TranslateTransform(r.X, r.Y); g.ScaleTransform(r.Width / 24f, r.Height / 24f);
            using (var b = new SolidBrush(c)) using (var pen = new Pen(c, 1.8f) { LineJoin = LineJoin.Round })
            {
                switch (kind)
                {
                    case 0: g.FillPolygon(b, new[] { new PointF(12, 1), new PointF(14.6f, 9.4f), new PointF(23, 12), new PointF(14.6f, 14.6f), new PointF(12, 23), new PointF(9.4f, 14.6f), new PointF(1, 12), new PointF(9.4f, 9.4f) }); break;
                    case 1: using (var rg = new Region(new RectangleF(0, 0, 24, 24))) using (var p1 = new GraphicsPath()) using (var p2 = new GraphicsPath()) { p1.AddEllipse(1.5f, 2.5f, 19, 19); p2.AddEllipse(8.9f, 1.9f, 15.2f, 15.2f); rg.Intersect(p1); rg.Exclude(p2); g.FillRegion(b, rg); } break;
                    case 2: foreach (var k in new[] { new PointF(12, 6), new PointF(6, 12), new PointF(18, 12), new PointF(12, 18) }) g.FillPolygon(b, new[] { new PointF(k.X, k.Y - 4), new PointF(k.X + 4, k.Y), new PointF(k.X, k.Y + 4), new PointF(k.X - 4, k.Y) }); break;
                    case 3: g.DrawEllipse(pen, 2.4f, 2.4f, 19.2f, 19.2f); using (var p = new GraphicsPath()) { p.AddArc(2.4f, 2.4f, 19.2f, 19.2f, -90, 180); p.AddArc(7.2f, 12, 9.6f, 9.6f, 90, 180); p.AddArc(7.2f, 2.4f, 9.6f, 9.6f, 90, -180); p.CloseFigure(); g.FillPath(b, p); } break;
                    case 4: g.DrawPolygon(pen, new[] { new PointF(12, 2.5f), new PointF(14.9f, 8.8f), new PointF(21.8f, 9.5f), new PointF(16.6f, 14.1f), new PointF(18.1f, 20.9f), new PointF(12, 17.4f), new PointF(5.9f, 20.9f), new PointF(7.4f, 14.1f), new PointF(2.2f, 9.5f), new PointF(9.1f, 8.8f) }); break;
                    default: for (int i = 0; i < 8; i++) { var s2 = g.Save(); g.TranslateTransform(12, 12); g.RotateTransform(i * 45); g.FillRectangle(b, -1.7f, -10.5f, 3.4f, 4.5f); g.Restore(s2); } using (var p3 = new Pen(c, 3.2f)) g.DrawEllipse(p3, 5.8f, 5.8f, 12.4f, 12.4f); break;
                }
            }
            g.Restore(st);
        }
        public static void Logo(Graphics g, RectangleF r)
        {
            var st = g.Save(); g.SmoothingMode = SmoothingMode.AntiAlias; g.TranslateTransform(r.X, r.Y); g.ScaleTransform(r.Width / 100f, r.Height / 100f);
            using (var p = new GraphicsPath()) { p.AddEllipse(2, 2, 96, 96); using (var b = new PathGradientBrush(p) { CenterColor = Color.FromArgb(0x3B, 0x2A, 0x6B), SurroundColors = new[] { Color.FromArgb(0x0A, 0x05, 0x18) } }) g.FillPath(b, p); }
            using (var pen = new Pen(Color.FromArgb(0xF2, 0xB8, 0x4B), 2)) g.DrawEllipse(pen, 2, 2, 96, 96);
            using (var b = new SolidBrush(Color.FromArgb(230, 0xF2, 0xB8, 0x4B))) g.FillPolygon(b, new[] { new PointF(50, 8), new PointF(56, 40), new PointF(88, 50), new PointF(56, 60), new PointF(50, 92), new PointF(44, 60), new PointF(12, 50), new PointF(44, 40) });
            using (var b = new SolidBrush(Color.FromArgb(0x0A, 0x05, 0x18))) g.FillEllipse(b, 37, 37, 26, 26);
            using (var pen = new Pen(Color.FromArgb(0xFF, 0xE7, 0xA3), 2)) g.DrawEllipse(pen, 37, 37, 26, 26);
            using (var p = new GraphicsPath()) using (var b = new SolidBrush(Color.FromArgb(0xFF, 0xE7, 0xA3))) { p.AddArc(37, 37, 26, 26, -90, 180); p.AddArc(43.5f, 50, 13, 13, 90, 180); p.AddArc(43.5f, 37, 13, 13, 90, -180); p.CloseFigure(); g.FillPath(b, p); }
            g.Restore(st);
        }
    }
}
