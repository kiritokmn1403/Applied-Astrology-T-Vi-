// Xuất kết quả engine JS (bản gốc v0.0.3) với cùng đầu vào để so với Java/C#
const fs = require('fs'), vm = require('vm'), path = require('path');
const W = ['../../src/web/js', '../../../src/web/js'].map(r => path.join(__dirname, r)).find(d => fs.existsSync(path.join(d, 'lunar.js')));
const ctx = { window: {}, module: undefined }; ctx.window = ctx; vm.createContext(ctx);
for (const f of ['lunar.js', 'tuvi_engine.js']) vm.runInContext(fs.readFileSync(path.join(W, f), 'utf8'), ctx);
ctx.t = (k) => k === 'cal_leap' ? '' : k; ctx.AATheme = { eleClass: () => '' };
vm.runInContext(fs.readFileSync(path.join(W, 'calendar.js'), 'utf8'), ctx);
const E = ctx.AAEngine, L = ctx.AALunar, C = ctx.AACalendar;
const javaCharts = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
let bad = 0, n = 0;
function pal(p) {
  return [p.name, p.canChi, p.major.map(s => s.name + '(' + s.bright + ')').join(''),
    p.good.map(s => s.name + (s.attachedTo ? '@' + s.attachedTo : '') + ',').join(''),
    p.bad.map(s => s.name + (s.attachedTo ? '@' + s.attachedTo : '') + ',').join(''),
    p.neutral.map(s => s.name + ',').join(''),
    (p.ring.trangSinh) + ',' + (p.ring.bacSi) + ',' + (p.ring.thaiTue), String(p.tuan) + String(p.triet), p.daiHan + p.tieuHan, p.luu.join(',')].join('|');
}
for (const j of javaCharts) {
  const [cal, d, m, y, h, g, lm] = j.in; n++;
  const c = new E.TuViChart({ name: 'T', gender: g, calendar: cal, day: d, month: m, year: y, hourChi: h, leap: false, leapMode: lm, viewYear: 2026 });
  const meta = [c.yearCanChi, c.monthCanChi, c.dayCanChi, c.hourCanChi, c.napAm, c.cucName, c.menhCuc, c.menhChu, c.thanChu, c.amDuongLy, c.menh, c.than, c.age, c.currentDaiHan, c.currentTieuHan].join('|');
  const s = c.solar.day + '/' + c.solar.month + '/' + c.solar.year, l = c.lunar.day + '/' + c.lunar.month + '/' + c.lunar.year + (c.lunar.leap ? 'L' : '');
  let diff = [];
  if (s !== j.s) diff.push('solar ' + s + ' vs ' + j.s);
  if (l !== j.l) diff.push('lunar ' + l + ' vs ' + j.l);
  if (meta !== j.meta) diff.push('meta\n  JS  ' + meta + '\n  JAV ' + j.meta);
  for (let i = 0; i < 12; i++) { const a = pal(c.palaces[i]); if (a !== j.p[i]) diff.push('palace ' + i + '\n  JS  ' + a + '\n  JAV ' + j.p[i]); }
  if (diff.length) { bad++; if (bad <= 3) console.log('MISMATCH', JSON.stringify(j.in), diff.join('\n')); }
}
console.log('charts: ' + n + ' compared, ' + (n - bad) + ' identical, ' + bad + ' different');
// lịch
const jc = JSON.parse(fs.readFileSync(process.argv[3], 'utf8')); let cb = 0, ci = 0;
const jd0 = L.jdFromDate(1, 1, 1990);
for (let k = 0; k < 16000; k += 4) {
  const dt = L.jdToDate(jd0 + k), di = C.dayInfo(dt[0], dt[1], dt[2]);
  const s = dt.join('/') + '|' + di.lunar.day + '/' + di.lunar.month + '/' + di.lunar.year + (di.lunar.leap ? 'L' : '') + '|' + di.weekday + '|' + di.dayCC + '|' + di.monthCC.replace(' ()', '') + '|' + di.yearCC + '|' + di.napAmDay + '|' + di.napAmYear + '|' + di.tiet + '|' + di.tietStart + '|' + di.than + '|' + di.hoangDao + '|' + di.truc + '|' + di.hourZero + '|' + di.goodHours.map(h => h.can + h.name + h.range + ';').join('') + '|' + di.events.map(e => e.name + ';').join('');
  if (s !== jc[ci]) { cb++; if (cb <= 3) console.log('CAL MISMATCH\n JS  ' + s + '\n JAV ' + jc[ci]); }
  ci++;
}
console.log('calendar days: ' + ci + ' compared, ' + (ci - cb) + ' identical');
const jl = JSON.parse(fs.readFileSync(process.argv[4], 'utf8')); let lb = 0, li = 0;
for (let y = 1900; y <= 2100; y += 3) for (let m = 1; m <= 12; m++) for (let lp = 0; lp < 2; lp++) {
  const s = L.lunar2solar(15, m, y, lp === 1);
  const v = y + '/' + m + '/' + lp + '=' + (s ? s.join('/') : 'null') + '|' + L.lunarMonthLength(m, y, lp === 1);
  if (v !== jl[li]) { lb++; if (lb <= 3) console.log('L2S MISMATCH ' + v + ' vs ' + jl[li]); }
  li++;
}
console.log('lunar2solar: ' + li + ' compared, ' + (li - lb) + ' identical');
process.exit(bad || cb || lb ? 1 : 0);
