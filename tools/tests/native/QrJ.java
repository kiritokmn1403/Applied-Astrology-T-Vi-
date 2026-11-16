import io.nayuki.qrcodegen.QrCode;
public class QrJ { public static void main(String[] a) {
  StringBuilder q = new StringBuilder();
  for (String t : new String[]{"AA2|Nguyễn Văn An|M|S|15|8|1995|6|0","AA2|Trần Thị Bình|F|L|1|2|2023|3|1","x"}) { QrCode qr = QrCode.encodeText(t, QrCode.Ecc.MEDIUM); q.append(qr.size+":"+qr.mask+":"); for(int y=0;y<qr.size;y++)for(int x=0;x<qr.size;x++)q.append(qr.getModule(x,y)?'1':'0'); q.append('\n'); }
  System.out.print(q);
}}
