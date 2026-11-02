exec(open('/tmp/swfx/dump.py').read())
import json,re
place=pickle.load(open('/tmp/swfx/place.pkl','rb'))
def tmat(cid):
    tag,body=TXT[cid]; r=BR(body); r.u16(); r.rect(); m=r.matrix(); return m[2],m[3]
GRID=[['Tị','Ngọ','Mùi','Thân'],['Thìn',None,None,'Dậu'],['Mão',None,None,'Tuất'],['Dần','Sửu','Tí','Hợi']]
def cell(x,y):
    c=min(3,max(0,int(x//200))); 
    r=0 if y<200 else 1 if y<340 else 2 if y<470 else 3
    return GRID[r][c]
ABBR=[(r'Q\.\s*Phủ','Quan Phủ'),(r'Q\.\s*Phu\b','Quan Phủ'),(r'TH\.\s*VIỆT','Thiên Việt'),(r'Th\.\s*Hư','Thiên Hư'),(r'Th\.\s*Khốc','Thiên Khốc'),(r'T\.\s*MÃ','Thiên Mã'),
 (r'L\.\s*Trì','Long Trì'),(r'Đ\.\s*Khách','Điếu Khách'),(r'Ph\.\s*Các','Phượng Các'),(r'Quả Tu\b','Quả Tú'),(r'ĐPhù','Đường Phù'),(r'H\.\s*Loan','Hồng Loan'),(r'P\.\s*Toái','Phá Toái'),(r'Q\.\s*Phù','Quan Phù')]
STARS=['Lộc Tồn','Bác Sỹ','Lực Sỹ','Thanh Long','Tiểu Hao','Tướng Quân','Tấu Thư','Phi Liêm','Hỉ Thần','Bệnh Phù','Đại Hao','Phục Binh','Quan Phủ',
'Kình Dương','Đà La','Thiên Khôi','Thiên Việt','Quốc Ấn','Đường Phù','Lưu Hà','Thái Tuế','Tang Môn','Quan Phù','Long Trì','Thiên Hư','Tuế Phá','Bạch Hổ','Điếu Khách',
'Cô Thần','Quả Tú','Thiên Mã','Hồng Loan','Thiên Hỉ','Hoa Cái','Kiếp Sát','Phá Toái','Thiên Khốc','Phượng Các','Đào Hoa','Thiên Đức','Nguyệt Đức','Thiên Quan','Thiên Phúc','Thiên Trù','Giải Thần','Thiên La','Địa Võng']
def norm(s):
    s=s.replace('ù','').replace('ø','')
    for a,b in ABBR: s=re.sub(a,b,s,flags=re.I)
    return s
out={}
S=8054
frames=sorted(set((fr,lab) for fr,lab,c,tx,ty,d in place[S]))
for fr,lab in frames:
    if not lab or not lab.startswith('Frame') or fr%2: continue
    n=int(lab.split()[1])
    rec={'stars':{},'info':[]}
    for f2,lb,c,tx,ty,d in place[S]:
        if f2!=fr or c not in TXT: continue
        mx,my=tmat(c)
        for y,x,s in ptxt_lm(c):
            s=s.strip()
            if not s: continue
            X=(mx+x)/20; Y=(my+y)/20
            if 260<X<560 and 130<Y<300: rec['info'].append(s); continue
            ns=norm(s)
            for st in STARS:
                if re.search(re.escape(st),ns,re.I):
                    rec['stars'].setdefault(st,[]).append(cell(X,Y))
    out[n]=rec
json.dump(out,open('/tmp/swfx/templates.json','w'),ensure_ascii=False,indent=0)
for n in (1,2,25,60): print(n,out.get(n,{}).get('info'),out.get(n,{}).get('stars'))
print(len(out))
