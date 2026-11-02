import pickle,struct,collections
exec(open('swfparse.py').read().split("nbits=data[8]")[0])
nbits=data[8]>>3; start=8+(5+nbits*4+7)//8+4
parent=collections.defaultdict(set)   # child cid -> set of (spriteid, framelabel)
sprite_labels={}
def placeobj2(body):
    r=BR(body); fl=r.u8(); depth=r.u16()
    cid=r.u16() if fl&2 else None
    return cid
def walk(buf,pos,end,owner):
    label=None; frame=0; labels=[]
    for code,p,ln in iter_tags(buf,pos,end):
        if code==43:
            label=buf[p:p+ln].split(b'\0')[0].decode('utf8','replace'); labels.append((frame,label))
        elif code==1: frame+=1
        elif code==26:
            c=placeobj2(buf[p:p+ln])
            if c is not None: parent[c].add((owner,label,frame))
        elif code==70:
            r=BR(buf[p:p+ln]); fl=r.u8(); fl2=r.u8(); depth=r.u16()
            if fl2&8 or (fl2&16 and fl2&8): r.cstr()
            if fl&2: parent[r.u16()].add((owner,label,frame))
        elif code==39 and owner=='root':
            sid=struct.unpack('<H',buf[p:p+2])[0]
            sprite_labels[sid]=walk(buf,p+4,p+ln,sid)
    return labels
sprite_labels['root']=walk(data,start,len(data),'root')
# buttons: DefineButton2 records reference characters
for code,p,ln in iter_tags(data,start,len(data)):
    if code==34:
        body=data[p:p+ln]; bid=struct.unpack('<H',body[:2])[0]
        r=BR(body,2); r.u8(); actoff=r.u16()
        while True:
            fl=r.u8()
            if fl==0: break
            cid=r.u16(); r.u16(); r.matrix(); r.align()
            # cxform with alpha
            r.align(); ha=r.ub(1); hm=r.ub(1); n=r.ub(4)
            if hm: [r.sb(n) for _ in range(4)]
            if ha: [r.sb(n) for _ in range(4)]
            r.align()
            if fl&0x10:
                cnt=r.u8()
                for _ in range(cnt): r.u8()  # filters - rough, ignore
            if fl&0x20: r.u8()
            parent[cid].add(('btn%d'%bid,None,0))
pickle.dump((dict(parent),sprite_labels),open('tree.pkl','wb'))
print('root labels',sprite_labels['root'])
print(len(parent))
