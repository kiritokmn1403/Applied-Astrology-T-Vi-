import sys,pickle,struct; sys.path.insert(0,'/tmp/swfx')
import decode
from decode import *
exec(open('/tmp/swfx/swfparse.py').read().split("for code,p,ln in iter_tags(data,start,len(data)):\n    body=data[p:p+ln]\n    if code in (11,33)")[0])
decode.FONTS_CODES.update(fonts)
TXT={}
for code,p,ln in iter_tags(data,start,len(data)):
    if code in (11,33): TXT[struct.unpack('<H',data[p:p+2])[0]]=(code,data[p:p+ln])
def ptxt(cid,raw=False):
    tag,body=TXT[cid]
    r=BR(body); r.u16(); r.rect(); r.matrix(); gb=r.u8(); ab=r.u8(); font=None; lines=[]; x=y=0
    while True:
        r.align(); fl=r.u8()
        if fl==0: break
        if fl&8: font=r.u16()
        if fl&4: r.p+= 4 if tag==33 else 3
        if fl&1: x=r.s16()
        if fl&2: y=r.s16()
        if fl&8: r.u16()
        n=r.u8(); idx=[]
        for _ in range(n): idx.append(r.ub(gb)); r.ub(ab)
        codes=fonts.get(font) or []
        nm=fontnames.get(font,(b'?',))[0]
        if nm.startswith(b'VNI'):
            from vni import vni
            s=vni(''.join(chr(codes[i]) for i in idx if i<len(codes)))
        elif nm in (b'Wingdings',b'Webdings',b'Wingdings 3'):
            s=''
        else:
            s=''.join(decode_glyph(font,i,codes[i]) for i in idx if i<len(codes))
        lines.append((y,x,s))
    return lines
import itertools,re
FREQ=pickle.load(open('/tmp/swfx/freq.pkl','rb'))
def ptxt_lm(cid):
    tag,body=TXT[cid]
    r=BR(body); r.u16(); r.rect(); r.matrix(); gb=r.u8(); ab=r.u8(); font=None; lines=[]; x=y=0
    while True:
        r.align(); fl=r.u8()
        if fl==0: break
        if fl&8: font=r.u16()
        if fl&4: r.p+= 4 if tag==33 else 3
        if fl&1: x=r.s16()
        if fl&2: y=r.s16()
        if fl&8: r.u16()
        n=r.u8(); idx=[]
        for _ in range(n): idx.append(r.ub(gb)); r.ub(ab)
        codes=fonts.get(font) or []
        nm=fontnames.get(font,(b'?',))[0]
        if nm.startswith(b'VNI'):
            s=vni(''.join(chr(codes[i]) for i in idx if i<len(codes)))
        elif nm in (b'Wingdings',b'Webdings',b'Wingdings 3'): s=''
        else:
            opts=[]
            for i in idx:
                if i>=len(codes): continue
                c=codes[i]
                cs=cands(c&0xff) if c<0x100 else [chr(c)]
                if not cs: continue
                sh=decode_glyph(font,i,c)
                opts.append((cs,sh))
            # split into syllables by spaces/punct on shape-decoded string
            out=[];buf=[]
            def flush():
                if not buf: return
                choices=[o[0] for o in buf]
                best=None;bs=-1
                total=1
                for o in choices: total*=len(o)
                if total<=4096:
                    for combo in itertools.product(*choices):
                        w=''.join(combo); f=FREQ.get(w.lower(),0)
                        if f>bs: bs=f;best=w
                if not best or bs==0: best=''.join(o[1] for o in buf)
                out.append(best); buf.clear()
            for cs,sh in opts:
                if sh in ' ,.:;()!?-/0123456789"\'' and len(cs)==1:
                    flush(); out.append(sh)
                else: buf.append((cs,sh))
            flush(); s=''.join(out)
        lines.append((y,x,s))
    return lines
