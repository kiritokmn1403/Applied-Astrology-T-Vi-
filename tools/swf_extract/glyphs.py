import struct,pickle,numpy as np
from PIL import Image,ImageDraw,ImageFont
exec(open('/tmp/swfx/swfparse.py').read().split("nbits=data[8]")[0])
nbits=data[8]>>3; start=8+(5+nbits*4+7)//8+4
def parse_shape(b,p,end):
    r=BR(b,p); r.align(); nf=r.ub(4); nl=r.ub(4)
    x=y=0; paths=[]; cur=[(0,0)]
    while True:
        if r.p>=end: break
        typ=r.ub(1)
        if typ==0:
            fl=r.ub(5)
            if fl==0: break
            if fl&1:
                n=r.ub(5); x=r.sb(n); y=r.sb(n)
                if len(cur)>1: paths.append(cur)
                cur=[(x,y)]
            if fl&2: r.ub(nf)
            if fl&4: r.ub(nf)
            if fl&8: r.ub(nl)
            if fl&16: break
        else:
            straight=r.ub(1); n=r.ub(4)+2
            if straight:
                g=r.ub(1)
                if g: dx=r.sb(n); dy=r.sb(n)
                else:
                    v=r.ub(1)
                    if v: dx=0; dy=r.sb(n)
                    else: dx=r.sb(n); dy=0
                x+=dx;y+=dy; cur.append((x,y))
            else:
                cx=x+r.sb(n); cy=y+r.sb(n); ax=cx+r.sb(n); ay=cy+r.sb(n)
                for t in (0.25,0.5,0.75,1.0):
                    px=(1-t)**2*x+2*(1-t)*t*cx+t*t*ax; py=(1-t)**2*y+2*(1-t)*t*cy+t*t*ay
                    cur.append((px,py))
                x,y=ax,ay
    if len(cur)>1: paths.append(cur)
    return paths
glyphs={}
for code,p,ln in iter_tags(data,start,len(data)):
    body=data[p:p+ln]
    try:
        if code==10:
            fid=struct.unpack('<H',body[:2])[0]
            first=struct.unpack('<H',body[2:4])[0]; n=first//2
            offs=[struct.unpack('<H',body[2+2*i:4+2*i])[0] for i in range(n)]+[ln-2]
            glyphs[fid]=[parse_shape(body,2+offs[i],2+offs[i+1]) for i in range(n)]
        elif code==48:
            r=BR(body); fid=r.u16(); fl=r.u8(); r.u8(); nlen=r.u8(); r.p+=nlen
            ng=r.u16(); wide=fl&8; base=r.p
            offs=[(r.u32() if wide else r.u16()) for _ in range(ng)]
            codeoff=r.u32() if wide else r.u16()
            ends=offs[1:]+[codeoff]
            glyphs[fid]=[parse_shape(body,base+offs[i],base+ends[i]) for i in range(ng)]
    except Exception as e:
        pass
pickle.dump(glyphs,open('/tmp/swfx/glyphs.pkl','wb'))
print(len(glyphs))
