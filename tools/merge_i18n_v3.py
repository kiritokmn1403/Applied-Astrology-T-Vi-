# v0.0.3: ghép bản dịch các khóa mới (tools/i18n_v3/<code>.txt: mỗi dòng 1 giá trị, đúng thứ tự NEW_KEYS)
# vào tools/i18n_src/<code>.txt (dòng key=value). Ngôn ngữ mới: tools/i18n_v3/full/<code>.txt gồm OLD_KEYS + NEW_KEYS.
import os, sys, json, subprocess
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
js = "global.window=global;global.document={};require('%s/src/web/js/i18n.js');console.log(JSON.stringify(Object.keys(AAI18n.dict.en)))" % ROOT.replace('\\', '/')
keys = json.loads(subprocess.check_output(['node', '-e', js]))
SAME = ['app_name', 's_trangsinh', 's_bacsi', 's_thaitue', 'net_ip', 'cal_napam']
DUP = ['day_cc', 'month_cc', 'year_cc', 'p_menh', 'as_q_career', 'as_q_money', 'as_q_health', 'reader_font', 's_luu', 'ab_title']
cut = keys.index('tab_calendar')
OLD = [k for k in keys[:cut] if k not in SAME and k not in DUP]
NEW = [k for k in keys[cut:] if k not in SAME and k not in DUP]
src = os.path.join(ROOT, 'tools', 'i18n_src'); v3 = os.path.join(ROOT, 'tools', 'i18n_v3')
bad = 0
def lines(f): return [l.rstrip('\n').rstrip('\r') for l in open(f, encoding='utf-8') if l.strip()]
for fn in sorted(os.listdir(v3)):
    if not fn.endswith('.txt'): continue
    code = fn[:-4]; vals = lines(os.path.join(v3, fn))
    if len(vals) != len(NEW): print(code, 'expected', len(NEW), 'got', len(vals)); bad += 1; continue
    target = os.path.join(src, fn)
    have = [l.split('=', 1)[0] for l in lines(target)] if os.path.exists(target) else []
    with open(target, 'a', encoding='utf-8') as o:
        for k, v in zip(NEW, vals):
            if k not in have: o.write('%s=%s\n' % (k, v))
full = os.path.join(v3, 'full')
for fn in sorted(os.listdir(full)) if os.path.isdir(full) else []:
    code = fn[:-4]; vals = lines(os.path.join(full, fn))
    if len(vals) != len(OLD) + len(NEW): print(code, 'FULL expected', len(OLD) + len(NEW), 'got', len(vals)); bad += 1; continue
    with open(os.path.join(src, fn), 'w', encoding='utf-8') as o:
        for k, v in zip(OLD + NEW, vals): o.write('%s=%s\n' % (k, v))
print('merged; errors:', bad); sys.exit(1 if bad else 0)
