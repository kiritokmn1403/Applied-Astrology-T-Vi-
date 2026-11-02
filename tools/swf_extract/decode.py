import pickle,numpy as np,unicodedata
from PIL import Image,ImageDraw,ImageFont,ImageFilter
glyphs=pickle.load(open('/tmp/swfx/glyphs.pkl','rb'))
FONTS={'reg':'/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf','bold':'/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf'}
EM=64; W,H=96,110; BX,BY=10,80
def rast_glyph(paths,emunits=1024):
    im=Image.new('L',(W,H),0); d=ImageDraw.Draw(im)
    s=EM/emunits
    # even-odd fill via xor of polygons
    acc=np.zeros((H,W),bool)
    for pth in paths:
        if len(pth)<3: continue
        t=Image.new('L',(W,H),0); ImageDraw.Draw(t).polygon([(BX+x*s,BY+y*s) for x,y in pth],fill=255)
        acc^=np.array(t)>0
    return acc
_cache={}
def rast_char(ch,style='reg'):
    k=(ch,style)
    if k in _cache: return _cache[k]
    f=ImageFont.truetype(FONTS[style],EM)
    im=Image.new('L',(W,H),0); d=ImageDraw.Draw(im)
    d.text((BX,BY),ch,font=f,fill=255,anchor='ls')
    a=np.array(im)>127; _cache[k]=a; return a
def cands(b):
    c=[]
    if b>=0x20 and not (0x7f<=b<0xa0): c.append(chr(b))
    for base in (0x100,0x1E00):
        u=base+b
        ch=chr(u)
        try: nm=unicodedata.name(ch)
        except: continue
        if 0x1EA0<=u<=0x1EF9 or u in (0x102,0x103,0x110,0x111,0x128,0x129,0x168,0x169,0x1A0,0x1A1,0x1AF,0x1B0):
            c.append(ch)
    return c
def iou(a,b):
    u=(a|b).sum(); return (a&b).sum()/u if u else 0
def score(a,b):
    # blur tolerant
    A=Image.fromarray((a*255).astype('uint8')).filter(ImageFilter.GaussianBlur(2))
    B=Image.fromarray((b*255).astype('uint8')).filter(ImageFilter.GaussianBlur(2))
    x=np.array(A,float); y=np.array(B,float)
    return (x*y).sum()/np.sqrt((x*x).sum()*(y*y).sum()+1e-9)
memo={}
BASE={}
def baseline(fid):
    if fid in BASE: return BASE[fid]
    g=glyphs.get(fid); codes=FONTS_CODES.get(fid,[]); ys=[]
    for i,c in enumerate(codes):
        if c<128 and chr(c) in 'lHnmhkLTEIN1x' and g and i<len(g) and g[i]:
            ys.append(max(y for p in g[i] for x,y in p))
    import statistics
    b=statistics.median(ys) if ys else 0
    BASE[fid]=b; return b
FONTS_CODES={}
def decode_glyph(fid,idx,code,emunits=1024):
    k=(fid,idx)
    if k in memo: return memo[k]
    cs=cands(code&0xff) if code<0x100 else [chr(code)]
    if len(cs)<=1:
        r=cs[0] if cs else ''
        memo[k]=r; return r
    g=glyphs.get(fid)
    if not g or idx>=len(g): memo[k]=cs[0]; return cs[0]
    bl=baseline(fid); a=rast_glyph([[(x,y-bl) for x,y in p] for p in g[idx]],emunits)
    if a.sum()==0: memo[k]=cs[0]; return cs[0]
    best=max(cs,key=lambda ch:max(score(a,rast_char(ch,'reg')),score(a,rast_char(ch,'bold'))))
    memo[k]=best; return best
