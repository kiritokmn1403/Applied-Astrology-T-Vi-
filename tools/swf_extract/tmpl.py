exec(open('/tmp/swfx/dump.py').read())
# collect placements with matrix per sprite per frame
place=collections.defaultdict(list)  # sprite -> list of (frame,label,cid,tx,ty,depth)
def walk(buf,pos,end,owner):
    frame=0;label=None;disp={}
    for code,p,ln in iter_tags(buf,pos,end):
        if code==43: label=buf[p:p+ln].split(b'\0')[0].decode('utf8','replace')
        elif code==1:
            for d,(c,tx,ty) in disp.items(): place[owner].append((frame,label,c,tx,ty,d))
            frame+=1
        elif code==26:
            r=BR(buf[p:p+ln]); fl=r.u8(); depth=r.u16()
            c=r.u16() if fl&2 else None
            if fl&4: m=r.matrix()
            else: m=None
            if fl&1 and depth in disp:
                oc,ox,oy=disp[depth]
                disp[depth]=(c if c is not None else oc, m[2] if m else ox, m[3] if m else oy)
            elif c is not None:
                disp[depth]=(c, m[2] if m else 0, m[3] if m else 0)
        elif code==28:
            r=BR(buf[p:p+ln]); disp.pop(r.u16(),None)
        elif code==39 and owner=='root':
            sid=struct.unpack('<H',buf[p:p+2])[0]; walk(buf,p+4,p+ln,sid)
walk(data,start,len(data),'root')
pickle.dump(dict(place),open('/tmp/swfx/place.pkl','wb'))
