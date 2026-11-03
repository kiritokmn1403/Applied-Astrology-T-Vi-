// v0.0.3 - Kiểm tra mục Âm Lịch: âm lịch, Can Chi, tiết khí, giờ hoàng đạo, lục thập hoa giáp
global.window = global; global.t = function (k) { return k; }; global.AATheme = { eleClass: function () { return ''; } };
require('../../src/web/js/lunar.js'); require('../../src/web/js/tuvi_engine.js'); require('../../src/web/js/calendar.js');
var C = AACalendar, ok = 0, fail = 0;
function eq(name, got, want) { if (got === want) ok++; else { fail++; console.log('FAIL', name, 'got', got, 'want', want); } }
var d = C.dayInfo(17, 2, 2026);
eq('Tet 2026 lunar', d.lunar.day + '/' + d.lunar.month + '/' + d.lunar.year, '1/1/2026');
eq('Tet 2026 year', d.yearCC, 'Bính Ngọ'); eq('Tet 2026 month', d.monthCC, 'Canh Dần'); eq('Tet 2026 event', d.events[0].name, 'Tết Nguyên Đán');
eq('1/1/2000 day', C.dayInfo(1, 1, 2000).dayCC, 'Mậu Ngọ');
eq('Lap Xuan 2026', C.dayInfo(4, 2, 2026).tiet + (C.dayInfo(4, 2, 2026).tietStart ? '*' : ''), 'Lập Xuân*');
eq('Dong Chi 2026', C.dayInfo(22, 12, 2026).tiet + (C.dayInfo(22, 12, 2026).tietStart ? '*' : ''), 'Đông Chí*');
eq('Tet 2027', AALunar.lunar2solar(1, 1, 2027, 0).join('/'), '6/2/2027');
eq('Trung Thu 2026', C.dayInfo(25, 9, 2026).events[0].name, 'Tết Trung Thu');
eq('6 gio hoang dao', C.dayInfo(3, 11, 2026).goodHours.length, 6);
eq('Giao thua 2026', C.dayInfo(16, 2, 2026).events.length > 0, true);
eq('1995', C.yearInfo(1995).name + ' ' + C.yearInfo(1995).napAm, 'Ất Hợi Sơn Đầu Hỏa');
eq('60 rows', C.sixty().length, 60); eq('Giap Ti', C.sixty()[0].years.indexOf(1984) >= 0, true);
// 2000 ngày liên tiếp: Can Chi ngày tăng đều, âm lịch hợp lệ
var jd0 = AALunar.jdFromDate(1, 1, 2020), bad = 0;
for (var i = 0; i < 2000; i++) { var s = AALunar.jdToDate(jd0 + i), x = C.dayInfo(s[0], s[1], s[2]); if (!(x.lunar.day >= 1 && x.lunar.day <= 30) || x.goodHours.length !== 6) bad++; }
eq('2000 days consistent', bad, 0);
console.log('calendar_test: ' + ok + '/' + (ok + fail) + ' passed');
if (fail) process.exitCode = 1;
