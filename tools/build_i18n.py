# Sinh data/lang/<code>.js từ tools/i18n_src/<code>.txt (dòng key=value). Khóa thiếu/thừa sẽ báo lỗi.
import os, re, json, subprocess, sys
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
keys = json.loads(subprocess.check_output(['node', '-e', "global.window=global;global.document={};require('%s/src/web/js/i18n.js');console.log(JSON.stringify(Object.keys(AAI18n.dict.en)))" % ROOT.replace('\\','/')]))
en = json.loads(subprocess.check_output(['node', '-e', "global.window=global;global.document={};require('%s/src/web/js/i18n.js');console.log(JSON.stringify(AAI18n.dict.en))" % ROOT.replace('\\','/')]))
SAME = ['app_name', 's_trangsinh', 's_bacsi', 's_thaitue', 'net_ip']
DUP = {'day_cc': 'f_day', 'month_cc': 'f_month', 'year_cc': 'f_year', 'p_menh': 'menh', 'as_q_career': 'p_quanloc',
       'as_q_money': 'p_taibach', 'as_q_health': 'p_tatach', 'reader_font': 's_font', 's_luu': 'luu_stars'}
src = os.path.join(ROOT, 'tools', 'i18n_src'); out = os.path.join(ROOT, 'src', 'web', 'data', 'lang')
os.makedirs(out, exist_ok=True)
bad = 0
for fn in sorted(os.listdir(src)):
    code = fn[:-4]; d = {}
    for line in open(os.path.join(src, fn), encoding='utf-8'):
        line = line.rstrip('\n')
        if not line.strip() or line.startswith('#'): continue
        k, v = line.split('=', 1); d[k.strip()] = v.strip()
    for k in SAME: d.setdefault(k, en[k])
    for k, s in DUP.items(): d.setdefault(k, d.get(s, en[k]))
    missing = [k for k in keys if k not in d]; extra = [k for k in d if k not in keys]
    if missing or extra: print(code, 'MISSING', missing, 'EXTRA', extra); bad += 1
    js = 'AAI18n.register(%s,%s);' % (json.dumps(code), json.dumps({k: d.get(k, en[k]) for k in keys}, ensure_ascii=False, separators=(',', ':')))
    open(os.path.join(out, code + '.js'), 'w', encoding='utf-8').write(js)
print('languages:', len(os.listdir(src)), 'errors:', bad)
sys.exit(1 if bad else 0)
