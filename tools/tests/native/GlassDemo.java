import com.appliedastrology.tuvi.core.*;
import java.awt.image.BufferedImage; import javax.imageio.ImageIO; import java.io.File;
public class GlassDemo { public static void main(String[] a) throws Exception {
  int W=900,H=560; Theme th=Theme.byId(a.length>0?a[0]:"celestial-dark");
  long t0=System.currentTimeMillis();
  int[] bg=Glass.nebula(W,H,th,"nebula",7);
  int s=4, bw=W/s, bh=H/s; int[] small=new int[bw*bh];
  for(int y=0;y<bh;y++)for(int x=0;x<bw;x++)small[y*bw+x]=bg[(y*s)*W+x*s];
  Glass.boxBlur(small,bw,bh,5);
  Glass.Params p=new Glass.Params();
  int[][] panels={{100,60,700,90,45},{150,220,280,180,28},{480,230,260,120,60},{60,450,780,80,20}};
  int[] out=bg.clone();
  for(int[] r:panels){ Glass.Maps m=Glass.maps(r[2],r[3],r[4],p); int[] px=new int[r[2]*r[3]];
    Glass.render(small,bw,bh,s,r[0],r[1],m,p,th.card,px);
    for(int y=0;y<r[3];y++)for(int x=0;x<r[2];x++){int c=px[y*r[2]+x];int al=(c>>>24);if(al==0)continue;int i=(r[1]+y)*W+r[0]+x;int d=out[i];
      int rr=(((c>>16)&255)*al+((d>>16)&255)*(255-al))/255,gg=(((c>>8)&255)*al+((d>>8)&255)*(255-al))/255,bb=((c&255)*al+(d&255)*(255-al))/255; out[i]=0xFF000000|(rr<<16)|(gg<<8)|bb;}}
  System.out.println("ms="+(System.currentTimeMillis()-t0));
  BufferedImage img=new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB); img.setRGB(0,0,W,H,out,0,W); ImageIO.write(img,"png",new File(a.length>1?a[1]:"/tmp/glass.png"));
}}
