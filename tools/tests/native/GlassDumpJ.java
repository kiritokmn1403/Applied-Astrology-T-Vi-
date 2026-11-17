import com.appliedastrology.tuvi.core.*; import java.io.*;
public class GlassDumpJ { public static void main(String[] a) throws Exception {
  Glass.Params p=new Glass.Params(); p.bezel=23.6; p.thickness=52.5;
  int[] neb=Glass.nebula(480,270,Theme.CELESTIAL,"nebula",20261116); int bw=240,bh=135; int[] bl=new int[bw*bh];
  for(int y=0;y<bh;y++)for(int x=0;x<bw;x++)bl[y*bw+x]=neb[(y*2)*480+x*2]; Glass.boxBlur(bl,bw,bh,5);
  DataOutputStream o=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(a[0])));
  int[][] sz={{900,140,47},{620,700,42},{300,58,29},{1000,1500,42}};
  for(int[] s:sz){ Glass.Maps m=Glass.maps(s[0],s[1],s[2],p); int[] out=new int[s[0]*s[1]]; Glass.render(bl,bw,bh,4,17,33,m,p,0xFF0E1428,out); o.writeInt(m.n); for(int v:out)o.writeInt(v);}
  for(int v:neb)o.writeInt(v); o.close(); } }
