# Build Applied Astrology v0.0.2 library data from decoded SWF content
import pickle,json,re,collections,unicodedata,sys
arts=pickle.load(open('/tmp/swfx/arts.pkl','rb'))
OUT=sys.argv[1] if len(sys.argv)>1 else '/tmp/aa/src/web/data'
CHAPTERS=[('Frame 4','chinh-tinh','Chính Tinh'),('Frame 5','can-tinh','Can Tinh'),('Frame 6','chi-tinh','Chi Tinh'),
 ('Frame 7','sao-khac','Các Sao Khác'),('Frame 8','cac-cung','Các Cung'),('Frame 10','72-cach','72 Cách'),
 ('Frame 11','an-sao','An Sao'),('Frame 14','han','Hạn'),('Frame 12','la-so-nam','Lá Số Nam'),('Frame 13','la-so-nu','Lá Số Nữ'),
 ('Frame 9','tu-dien','Từ Điển Tử Vi'),('Frame 15','2-chinh-tinh','2 Chính Tinh'),('Frame 16','cac-bo','Các Bộ Sao')]
def clean(s):
    s=unicodedata.normalize('NFC',s).replace('Ð','Đ').replace('ð','đ').replace('\xa0',' ').replace('\r','\n')
    s=re.sub(r'[ \t]+',' ',s); s=re.sub(r' *\n *','\n',s); s=re.sub(r'\n{3,}','\n\n',s)
    return s.strip()
def title_case_fix(t):
    t=clean(t).replace('\n',' ').strip(' .:')
    return re.sub(r'\s+',' ',t)
ansao=open('/tmp/swfx/ansao.txt',encoding='utf-8').read()
# An Sao lessons: keep meaningful lines, group by lesson frame
lessons=[];cur=None
for line in ansao.split('\n'):
    if line.startswith('## '):
        if cur and cur['lines']: lessons.append(cur)
        cur={'lines':[]}; continue
    if cur is None: continue
    l=line.strip()
    if len(re.findall(r'[^\W\d_]{2,}',l))>=3: cur['lines'].append(l)
if cur and cur['lines']: lessons.append(cur)
def dedupe(ls):
    out=[]
    for l in ls:
        if l in out[-6:]: continue
        out.append(l)
    return out
lessons=[{'lines':dedupe(l['lines'])} for l in lessons if sum(len(x) for x in l['lines'])>=60]
lesson_titles=['Đặc tính 12 cung (Ngũ hành, Âm Dương, Phương vị)','Ngũ hành năm sinh và sao Tuần','An cung Mệnh, cung Thân và 12 cung','Định Cục và an chòm Tử Vi, Thiên Phủ','An các sao theo hàng Can (Lộc Tồn, Kình Đà, Bác Sĩ, Tứ Hóa)','An các sao theo hàng Chi (Thái Tuế, Đào Hoa, Hồng Hỉ, Cô Quả, Thiên Mã...)','An sao theo tháng, giờ, ngày; Trường Sinh; Đại hạn, Tiểu hạn']
index={'version':'0.0.2','source':'Tử Vi Ứng Dụng - Bửu Đình (SWF, phát hành 6/2021)','chapters':[]}
stars_idx=collections.defaultdict(list)
total=0
for n,(frame,cid,name) in enumerate(CHAPTERS,1):
    items=[a for a in arts if a['rootlabels'][0]==frame]
    items.sort(key=lambda a:(a['order'][0],a['order'][1],a['cid']))
    out=[]
    if cid=='an-sao':
        for i,l in enumerate(lessons):
            t=lesson_titles[i] if i<len(lesson_titles) else 'Bài %d'%(i+1)
            out.append({'t':t,'b':'\n'.join(l['lines'])})
    for a in items:
        body=clean(a['body']); t=title_case_fix(a['title'])
        if not t: t=title_case_fix(body.split('\n')[0])[:80]
        # drop leading title line duplicate
        first=body.split('\n',1)
        if len(first)==2 and title_case_fix(first[0])==t: body=first[1].strip()
        out.append({'t':t,'b':body})
    chap={'id':cid,'n':n,'name':name,'count':len(out),'articles':[{'i':i,'t':o['t'],'len':len(o['b'])} for i,o in enumerate(out)]}
    index['chapters'].append(chap)
    total+=sum(len(o['b']) for o in out)
    js='AALib.register(%s,%s);'%(json.dumps(cid),json.dumps([[o['t'],o['b']] for o in out],ensure_ascii=False,separators=(',',':')))
    open('%s/lib_%02d.js'%(OUT,n),'w',encoding='utf-8').write(js)
open(OUT+'/lib_index.js','w',encoding='utf-8').write('var AA_LIB_INDEX=%s;'%json.dumps(index,ensure_ascii=False,separators=(',',':')))
print('articles',sum(c['count'] for c in index['chapters']),'chars',total)
for c in index['chapters']: print(c['n'],c['name'],c['count'])
