using System; using System.IO; using System.Text; using System.Linq; using AppliedAstrology.Core;
class DumpCs {
  // cùng bộ sinh ngẫu nhiên như java.util.Random để có cùng đầu vào
  class JRandom { long seed; public JRandom(long s){ seed=(s ^ 0x5DEECE66DL) & ((1L<<48)-1);} int Next(int bits){ seed=(seed*0x5DEECE66DL+0xBL)&((1L<<48)-1); return (int)((ulong)seed>>(48-bits)); }
    public int NextInt(int bound){ if((bound & -bound)==bound) return (int)((bound*(long)Next(31))>>31); int bits,val; do{bits=Next(31); val=bits%bound;}while(bits-val+(bound-1)<0); return val; } }
  static void Main(string[] a) {
    var sb = new StringBuilder("["); var r = new JRandom(20261116); bool first = true;
    for (int n = 0; n < 3000; n++) {
      int y = 1900 + r.NextInt(200), m = 1 + r.NextInt(12), d = 1 + r.NextInt(Lunar.DaysInMonth(m, y)), h = r.NextInt(12);
      bool lunar = n % 5 == 0, fem = n % 2 == 1;
      var inp = new ChartInput { Name = "T", Gender = fem ? "Nữ" : "Nam", Day = d, Month = m, Year = y, HourChi = h, ViewYear = 2026 };
      if (lunar) { inp.Calendar = "lunar"; inp.Day = Math.Min(d, 29); inp.Leap = false; inp.LeapMode = n % 3 == 0 ? "next" : "same"; }
      Chart c; try { c = Engine.Build(inp); } catch (ArgumentException) { continue; }
      if (!first) sb.Append(','); first = false;
      sb.Append("{\"in\":[\"" + (lunar ? "lunar" : "solar") + "\"," + inp.Day + "," + m + "," + y + "," + h + ",\"" + inp.Gender + "\",\"" + inp.LeapMode + "\"],");
      sb.Append("\"s\":\"" + c.SDay + "/" + c.SMonth + "/" + c.SYear + "\",\"l\":\"" + c.LunarDate.Day + "/" + c.LunarDate.Month + "/" + c.LunarDate.Year + (c.LunarDate.Leap ? "L" : "") + "\",");
      sb.Append("\"meta\":\"" + string.Join("|", c.YearCanChi, c.MonthCanChi, c.DayCanChi, c.HourCanChi, c.NapAm, c.CucName, c.MenhCuc, c.MenhChu, c.ThanChu, c.AmDuongLy, ""+c.Menh, ""+c.Than, ""+c.Age, ""+c.CurrentDaiHan, ""+c.CurrentTieuHan) + "\",\"p\":[");
      for (int i = 0; i < 12; i++) { var p = c.Palaces[i]; if (i > 0) sb.Append(',');
        var ps = new StringBuilder(); ps.Append(p.Name).Append('|').Append(p.CanChi).Append('|');
        foreach (var s in p.Major) ps.Append(s.Name).Append('(').Append(s.Bright).Append(')');
        ps.Append('|'); foreach (var s in p.Good) ps.Append(s.Name).Append(s.AttachedTo != null ? "@" + s.AttachedTo : "").Append(',');
        ps.Append('|'); foreach (var s in p.Bad) ps.Append(s.Name).Append(s.AttachedTo != null ? "@" + s.AttachedTo : "").Append(',');
        ps.Append('|'); foreach (var s in p.Neutral) ps.Append(s.Name).Append(',');
        ps.Append('|').Append(p.RingTrangSinh).Append(',').Append(p.RingBacSi).Append(',').Append(p.RingThaiTue).Append('|').Append(p.Tuan ? "true" : "false").Append(p.Triet ? "true" : "false").Append('|').Append(p.DaiHan).Append(p.TieuHan).Append('|').Append(string.Join(",", p.Luu));
        Json.Quote(sb, ps.ToString()); }
      sb.Append("]}"); }
    sb.Append("]"); File.WriteAllText(a[0], sb.ToString(), new UTF8Encoding(false));
    var cal = new StringBuilder("["); int jd0 = Lunar.JdFromDate(1, 1, 1990);
    for (int k = 0; k < 16000; k += 4) { var dt = Lunar.JdToDate(jd0 + k); var di = Cal.DayInfo(dt[0], dt[1], dt[2]); if (k > 0) cal.Append(',');
      Json.Quote(cal, dt[0]+"/"+dt[1]+"/"+dt[2]+"|"+di.Lunar.Day+"/"+di.Lunar.Month+"/"+di.Lunar.Year+(di.Lunar.Leap?"L":"")+"|"+di.Weekday+"|"+di.DayCC+"|"+di.MonthCC+"|"+di.YearCC+"|"+di.NapAmDay+"|"+di.NapAmYear+"|"+di.Tiet+"|"+(di.TietStart?"true":"false")+"|"+di.Than+"|"+(di.HoangDao?"true":"false")+"|"+di.Truc+"|"+di.HourZero+"|"+string.Concat(di.GoodHours.Select(x=>x.Can+x.Name+x.Range+";"))+"|"+string.Concat(di.Events.Select(e=>e.Name+";"))); }
    cal.Append("]"); File.WriteAllText(a[1], cal.ToString(), new UTF8Encoding(false));
    var l2s = new StringBuilder("[");
    for (int y = 1900; y <= 2100; y += 3) for (int m = 1; m <= 12; m++) for (int lp = 0; lp < 2; lp++) { var s = Lunar.Lunar2Solar(15, m, y, lp == 1); if (l2s.Length > 1) l2s.Append(','); Json.Quote(l2s, y+"/"+m+"/"+lp+"="+(s==null?"null":s[0]+"/"+s[1]+"/"+s[2])+"|"+Lunar.LunarMonthLength(m, y, lp==1)); }
    l2s.Append("]"); File.WriteAllText(a[2], l2s.ToString(), new UTF8Encoding(false));
    // QR
    var q = new StringBuilder(); foreach (var t in new[]{"AA2|Nguyễn Văn An|M|S|15|8|1995|6|0","AA2|Trần Thị Bình|F|L|1|2|2023|3|1","x"}) { var qr = QrCode.EncodeText(t, QrCode.Ecc.Medium); q.Append(qr.Size+":"+qr.Mask+":"); for(int y=0;y<qr.Size;y++)for(int x=0;x<qr.Size;x++)q.Append(qr.Get(x,y)?'1':'0'); q.Append('\n'); }
    File.WriteAllText(a[3], q.ToString());
  }
}
