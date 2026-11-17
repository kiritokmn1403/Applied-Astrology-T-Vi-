using System; using System.IO; using AppliedAstrology.Core;
class GlassDumpCs { static void Main(string[] a) {
  var p=new Glass.Params(); p.Bezel=23.6; p.Thickness=52.5;
  var neb=Glass.Nebula(480,270,Theme.CELESTIAL,"nebula",20261116); int bw=240,bh=135; var bl=new int[bw*bh];
  for(int y=0;y<bh;y++)for(int x=0;x<bw;x++)bl[y*bw+x]=neb[(y*2)*480+x*2]; Glass.BoxBlur(bl,bw,bh,5);
  using(var o=new BinaryWriter(File.Create(a[0]))){
  int[][] sz={new[]{900,140,47},new[]{620,700,42},new[]{300,58,29},new[]{1000,1500,42}};
  foreach(var s in sz){ var m=Glass.GetMaps(s[0],s[1],s[2],p); var outp=new int[s[0]*s[1]]; Glass.Render(bl,bw,bh,4,17,33,m,p,unchecked((int)0xFF0E1428),outp); W(o,m.N); foreach(var v in outp)W(o,v);}
  foreach(var v in neb)W(o,v);} }
  static void W(BinaryWriter o,int v){ o.Write((byte)(v>>24)); o.Write((byte)(v>>16)); o.Write((byte)(v>>8)); o.Write((byte)v);} }
