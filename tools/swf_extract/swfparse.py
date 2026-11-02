import struct, collections, json, sys, html, re
data=open('/tmp/swf.bin','rb').read()
class BR:
    def __init__(s,b,p=0): s.b=b; s.p=p; s.bit=0; s.cur=0
    def align(s): s.bit=0
    def ub(s,n):
        v=0
        for _ in range(n):
            if s.bit==0: s.cur=s.b[s.p]; s.p+=1; s.bit=8
            s.bit-=1; v=(v<<1)|((s.cur>>s.bit)&1)
        return v
    def sb(s,n):
        if n==0: return 0
        v=s.ub(n)
        if v&(1<<(n-1)): v-=1<<n
        return v
    def u8(s): s.align(); v=s.b[s.p]; s.p+=1; return v
    def u16(s): s.align(); v=struct.unpack('<H',s.b[s.p:s.p+2])[0]; s.p+=2; return v
    def s16(s): s.align(); v=struct.unpack('<h',s.b[s.p:s.p+2])[0]; s.p+=2; return v
    def u32(s): s.align(); v=struct.unpack('<I',s.b[s.p:s.p+4])[0]; s.p+=4; return v
    def rect(s):
        s.align(); n=s.ub(5); r=[s.sb(n) for _ in range(4)]; s.align(); return r
    def matrix(s):
        s.align(); sx=sy=1.0; r0=r1=0
        if s.ub(1): n=s.ub(5); sx=s.sb(n)/65536; sy=s.sb(n)/65536
        if s.ub(1): n=s.ub(5); r0=s.sb(n); r1=s.sb(n)
        n=s.ub(5); tx=s.sb(n); ty=s.sb(n); s.align(); return (sx,sy,tx,ty)
    def cstr(s):
        e=s.b.index(b'\0',s.p); v=s.b[s.p:e]; s.p=e+1; return v
def iter_tags(buf,pos,end):
    while pos<end:
        h=struct.unpack('<H',buf[pos:pos+2])[0];pos+=2
        code=h>>6;ln=h&0x3f
        if ln==0x3f: ln=struct.unpack('<I',buf[pos:pos+4])[0];pos+=4
        yield code,pos,ln
        pos+=ln
        if code==0: break
nbits=data[8]>>3; start=8+(5+nbits*4+7)//8+4
fonts={}   # id -> list of codes
fontnames={}
texts={}   # charid -> list of (y,x,string)
edits={}
def dec(codes_bytes, wide, ansi):
    pass
def parse_text(body,tag):
    r=BR(body); cid=r.u16(); r.rect(); r.matrix()
    gb=r.u8(); ab=r.u8(); font=None; x=y=0; out=[]
    while True:
        r.align(); flags=r.u8()
        if flags==0: break
        hasfont=flags&8; hascol=flags&4; hasy=flags&2; hasx=flags&1
        if hasfont: font=r.u16()
        if hascol: r.p+= 4 if tag==33 else 3
        if hasx: x=r.s16()
        if hasy: y=r.s16()
        if hasfont: r.u16()
        n=r.u8(); idx=[]
        for _ in range(n):
            idx.append(r.ub(gb)); r.ub(ab)
        r.align()
        codes=fonts.get(font)
        s=''.join(chr(codes[i]) if codes and i<len(codes) else '?' for i in idx)
        out.append((y,x,s))
    texts[cid]=out
def latin_or_vn(code,enc):
    return code
for code,p,ln in iter_tags(data,start,len(data)):
    body=data[p:p+ln]
    if code==13 or code==62:
        r=BR(body); fid=r.u16(); nl=r.u8(); name=body[3:3+nl]; r.p=3+nl; fl=r.u8()
        if code==62: r.u8()
        wide=fl&1
        codes=[]
        while r.p<len(body):
            codes.append(r.u16() if wide else r.u8())
        fonts[fid]=codes; fontnames[fid]=(name,wide,fl)
    elif code==48:
        r=BR(body); fid=r.u16(); fl=r.u8(); r.u8(); nl=r.u8(); name=body[r.p:r.p+nl]; r.p+=nl
        ng=r.u16(); wideoff=fl&8; widecodes=fl&4
        offs=[ (r.u32() if wideoff else r.u16()) for _ in range(ng)]
        codeoff= r.u32() if wideoff else r.u16()
        base=r.p - (ng*(4 if wideoff else 2)) - (4 if wideoff else 2)
        q=base+codeoff; cr=BR(body,q)
        codes=[(cr.u16() if widecodes else cr.u8()) for _ in range(ng)]
        fonts[fid]=codes; fontnames[fid]=(name,widecodes,fl)
for code,p,ln in iter_tags(data,start,len(data)):
    body=data[p:p+ln]
    if code in (11,33):
        try: parse_text(body,code)
        except Exception as e: pass
    elif code==37:
        r=BR(body); cid=r.u16(); r.rect(); r.align(); f1=r.u8(); f2=r.u8()
        hasText=f1&0x80; hasColor=f1&4; hasMax=f1&2; hasFont=f1&1; hasLayout=f2&0x20; hasFontClass=f2&0x80
        if hasFont: r.u16()
        if hasFontClass: r.cstr()
        if hasFont: r.u16()
        if hasColor: r.p+=4
        if hasMax: r.u16()
        if hasLayout: r.p+=9
        var=r.cstr()
        txt=r.cstr() if hasText else b''
        edits[cid]=(var.decode('latin1'),txt)
import pickle
pickle.dump((fonts,fontnames,texts,edits),open('/tmp/swfx/stage1.pkl','wb'))
print(len(fonts),len(texts),len(edits))
fn=collections.Counter(v[0] for v in fontnames.values()); print(fn.most_common(15))
# sample
for cid in list(texts)[:5]: print(cid,texts[cid][:3])
for cid in list(edits)[:5]: print(cid,edits[cid][0],edits[cid][1][:200])
