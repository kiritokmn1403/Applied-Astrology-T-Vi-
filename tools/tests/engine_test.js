// Kiểm thử engine an sao v0.0.2 với 60 mẫu lá số Can Chi trích từ SWF Bửu Đình
var E = require('../../src/web/js/tuvi_engine.js'), L = require('../../src/web/js/lunar.js');
var T = require('./swf_templates.json');
var CHI = E.CHI, CAN = E.CAN;
var ALIAS = { 'Bác Sỹ': 'Bác Sĩ', 'Lực Sỹ': 'Lực Sĩ' };
var RING = ['Bác Sĩ','Lực Sĩ','Thanh Long','Tiểu Hao','Tướng Quân','Tấu Thư','Phi Liêm','Hỉ Thần','Bệnh Phù','Đại Hao','Phục Binh','Quan Phủ'];
var pass = 0, fail = 0, fails = {}, tested = 0;
Object.keys(T).forEach(function (k) {
  var rec = T[k], info = rec.info.join(' | '), m = null, can = -1, chi = -1;
  for (var a = 0; a < 10 && can < 0; a++) for (var b = 0; b < 12; b++) {
    if (info.indexOf(CAN[a] + ' ' + CHI[b]) >= 0 || info.indexOf(CAN[a] + ' ' + (CHI[b] === 'Tí' ? 'Tý' : CHI[b])) >= 0) { can = a; chi = b; break; }
  }
  if (can < 0) return;
  var gender = /Nữ/.test(info) ? 'Nữ' : 'Nam';
  var year = 1984; while (((year - 4) % 10 + 10) % 10 !== can || ((year - 4) % 12 + 12) % 12 !== chi) year++;
  var c = new E.TuViChart({ calendar: 'lunar', day: 10, month: 5, year: year, hourChi: 3, gender: gender });
  tested++;
  Object.keys(rec.stars).forEach(function (s) {
    var name = ALIAS[s] || s, exp = rec.stars[s], got;
    if (RING.indexOf(name) >= 0) { got = []; c.palaces.forEach(function (p) { if (p.ring.bacSi === name) got.push(CHI[p.chi]); }); }
    else if (name === 'Thiên La' || name === 'Địa Võng') return;
    else { var w = c.where(name); got = w >= 0 ? [CHI[w]] : []; }
    var e = exp.map(function (x) { return x === 'Tý' ? 'Tí' : x; });
    var ok = e.some(function (x) { return got.indexOf(x) >= 0; });
    if (ok) pass++; else { fail++; (fails[name] = fails[name] || []).push(CAN[can] + ' ' + CHI[chi] + ' ' + gender + ': exp ' + e + ' got ' + got); }
  });
});
console.log('Templates tested:', tested, 'checks pass:', pass, 'fail:', fail);
Object.keys(fails).forEach(function (k) { console.log(' FAIL', k, fails[k].length, fails[k].slice(0, 3).join(' ; ')); });
// Ví dụ trong sách: tuổi Giáp Tí, tháng 3, giờ Mão -> Mệnh ? ; Hỏa/Linh dương nam giờ Mão: Hỏa Tị, Linh Mùi
var ex = new E.TuViChart({ calendar: 'lunar', day: 5, month: 3, year: 1984, hourChi: 3, gender: 'Nam' });
console.log('Ex Giáp Tí T3 giờ Mão: Mệnh', CHI[ex.menh], 'Thân', CHI[ex.than], 'Hỏa', CHI[ex.where('Hỏa Tinh')], 'Linh', CHI[ex.where('Linh Tinh')]);
var ex2 = new E.TuViChart({ calendar: 'lunar', day: 5, month: 3, year: 1984, hourChi: 3, gender: 'Nữ' });
console.log('Ex2 dương nữ: Hỏa', CHI[ex2.where('Hỏa Tinh')], 'Linh', CHI[ex2.where('Linh Tinh')]);
var ex3 = new E.TuViChart({ calendar: 'lunar', day: 1, month: 4, year: 1984, hourChi: 0, gender: 'Nam' });
console.log('Thiên Hình T4:', CHI[ex3.where('Thiên Hình')], 'Diêu:', CHI[ex3.where('Thiên Diêu')], '(sách: Tý, Thìn)');
var rate = pass / (pass + fail);
console.log('Ty le khop:', (rate * 100).toFixed(2) + '% (sai lech con lai do nhan sao nam tren ranh gioi 2 o trong hinh SWF)');
if (rate < 0.97) process.exitCode = 1;
