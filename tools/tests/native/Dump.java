import com.appliedastrology.tuvi.core.*;
import java.util.*;
import java.nio.file.*;
/** Xuất kết quả engine Java ra JSON để so với bản JS (native_crosscheck.js) */
public class Dump {
  public static void main(String[] a) throws Exception {
    StringBuilder sb = new StringBuilder("[");
    Random r = new Random(20261116); boolean first = true;
    for (int n = 0; n < 3000; n++) {
      int y = 1900 + r.nextInt(200), m = 1 + r.nextInt(12), d = 1 + r.nextInt(Lunar.daysInMonth(m, y)), h = r.nextInt(12);
      boolean lunar = n % 5 == 0, fem = n % 2 == 1;
      Engine.Input in = new Engine.Input(); in.name = "T"; in.gender = fem ? "Nữ" : "Nam"; in.day = d; in.month = m; in.year = y; in.hourChi = h; in.viewYear = 2026;
      if (lunar) { in.calendar = "lunar"; in.day = Math.min(d, 29); in.leap = false; in.leapMode = n % 3 == 0 ? "next" : "same"; }
      Engine.Chart c;
      try { c = Engine.build(in); } catch (IllegalArgumentException e) { continue; }
      if (!first) sb.append(','); first = false;
      sb.append("{\"in\":[").append(lunar ? "\"lunar\"" : "\"solar\"").append(',').append(in.day).append(',').append(m).append(',').append(y).append(',').append(h).append(",\"").append(in.gender).append("\",\"").append(in.leapMode).append("\"],");
      sb.append("\"s\":\"").append(c.sDay).append('/').append(c.sMonth).append('/').append(c.sYear).append("\",\"l\":\"").append(c.lunar.day).append('/').append(c.lunar.month).append('/').append(c.lunar.year).append(c.lunar.leap?"L":"").append("\",");
      sb.append("\"meta\":\"").append(String.join("|", c.yearCanChi, c.monthCanChi, c.dayCanChi, c.hourCanChi, c.napAm, c.cucName, c.menhCuc, c.menhChu, c.thanChu, c.amDuongLy, ""+c.menh, ""+c.than, ""+c.age, ""+c.currentDaiHan, ""+c.currentTieuHan)).append("\",\"p\":[");
      for (int i = 0; i < 12; i++) {
        Engine.Palace p = c.palaces[i]; if (i > 0) sb.append(',');
        StringBuilder ps = new StringBuilder();
        ps.append(p.name).append('|').append(p.canChi).append('|');
        for (Engine.Star s : p.major) ps.append(s.name).append('(').append(s.bright).append(')');
        ps.append('|'); for (Engine.Star s : p.good) ps.append(s.name).append(s.attachedTo!=null?"@"+s.attachedTo:"").append(',');
        ps.append('|'); for (Engine.Star s : p.bad) ps.append(s.name).append(s.attachedTo!=null?"@"+s.attachedTo:"").append(',');
        ps.append('|'); for (Engine.Star s : p.neutral) ps.append(s.name).append(',');
        ps.append('|').append(p.ringTrangSinh).append(',').append(p.ringBacSi).append(',').append(p.ringThaiTue).append('|').append(p.tuan).append(p.triet).append('|').append(p.daiHan).append(p.tieuHan).append('|').append(String.join(",", p.luu));
        Json.quote(sb, ps.toString());
      }
      sb.append("]}");
    }
    sb.append("]");
    Files.write(Paths.get(a[0]), sb.toString().getBytes("UTF-8"));
    // Âm lịch + lịch: 4000 ngày liên tiếp
    StringBuilder cal = new StringBuilder("[");
    int jd0 = Lunar.jdFromDate(1, 1, 1990);
    for (int k = 0; k < 16000; k += 4) {
      int[] dt = Lunar.jdToDate(jd0 + k); Cal.DayInfo di = Cal.dayInfo(dt[0], dt[1], dt[2]);
      if (k > 0) cal.append(',');
      StringBuilder ev = new StringBuilder(); for (Cal.Event e : di.events) ev.append(e.name).append(';');
      StringBuilder gh = new StringBuilder(); for (Cal.Hour hh : di.goodHours) gh.append(hh.can).append(hh.name).append(hh.range).append(';');
      Json.quote(cal, dt[0]+"/"+dt[1]+"/"+dt[2]+"|"+di.lunar.day+"/"+di.lunar.month+"/"+di.lunar.year+(di.lunar.leap?"L":"")+"|"+di.weekday+"|"+di.dayCC+"|"+di.monthCC+"|"+di.yearCC+"|"+di.napAmDay+"|"+di.napAmYear+"|"+di.tiet+"|"+di.tietStart+"|"+di.than+"|"+di.hoangDao+"|"+di.truc+"|"+di.hourZero+"|"+gh+"|"+ev);
    }
    cal.append("]");
    Files.write(Paths.get(a[1]), cal.toString().getBytes("UTF-8"));
    // lunar2solar
    StringBuilder l2s = new StringBuilder("[");
    for (int y = 1900; y <= 2100; y += 3) for (int m = 1; m <= 12; m++) for (int lp = 0; lp < 2; lp++) {
      int[] s = Lunar.lunar2solar(15, m, y, lp == 1); if (l2s.length() > 1) l2s.append(',');
      Json.quote(l2s, y+"/"+m+"/"+lp+"="+(s==null?"null":s[0]+"/"+s[1]+"/"+s[2])+"|"+Lunar.lunarMonthLength(m, y, lp==1));
    }
    l2s.append("]");
    Files.write(Paths.get(a[2]), l2s.toString().getBytes("UTF-8"));
  }
}
