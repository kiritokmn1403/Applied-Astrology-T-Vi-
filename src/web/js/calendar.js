/*
 * Applied Astrology v0.0.3 - Mục Âm Lịch (lịch Âm Dương Việt Nam, múi giờ +7)
 * Tham khảo các project Âm Lịch trong sheet Project GitHub: hochanh/amlich, copthuy/amlich
 * (thuật toán Hồ Ngọc Đức, giờ hoàng đạo, tiết khí, ngày lễ), wolfhong/LunarCalendar (24 tiết khí),
 * cmduyeen/lich-am-viet-nam; bảng Lục Thập Hoa Giáp & nạp âm lấy theo khung lập lá số (Frame 3) của SWF gốc.
 * ES5 thuần để chạy trên Android 6.0 WebView và IE11 (Windows 7).
 */
(function (root) {
  'use strict';
  var L = root.AALunar, E = root.AAEngine;
  var CAN = L.CAN, CHI = L.CHI;
  var TIET = ['Xuân Phân', 'Thanh Minh', 'Cốc Vũ', 'Lập Hạ', 'Tiểu Mãn', 'Mang Chủng', 'Hạ Chí', 'Tiểu Thử', 'Đại Thử', 'Lập Thu',
    'Xử Thử', 'Bạch Lộ', 'Thu Phân', 'Hàn Lộ', 'Sương Giáng', 'Lập Đông', 'Tiểu Tuyết', 'Đại Tuyết', 'Đông Chí', 'Tiểu Hàn',
    'Đại Hàn', 'Lập Xuân', 'Vũ Thủy', 'Kinh Trập'];
  // Giờ hoàng đạo theo chi của ngày (Tí/Ngọ, Sửu/Mùi, ...): '1' = giờ hoàng đạo, vị trí 0 = giờ Tí
  var GIO_HD = ['110100101100', '001101001011', '110011010010', '101100110100', '001011001101', '010010110011'];
  // 12 thần trực nhật (Thanh Long khởi theo tháng âm)
  var THAN = ['Thanh Long', 'Minh Đường', 'Thiên Hình', 'Chu Tước', 'Kim Quỹ', 'Kim Đường', 'Bạch Hổ', 'Ngọc Đường', 'Thiên Lao', 'Huyền Vũ', 'Tư Mệnh', 'Câu Trận'];
  var THAN_GOOD = [1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0];
  var TRUC = ['Kiến', 'Trừ', 'Mãn', 'Bình', 'Định', 'Chấp', 'Phá', 'Nguy', 'Thành', 'Thu', 'Khai', 'Bế'];
  var LUNAR_EVENTS = [[1, 1, 'Tết Nguyên Đán'], [2, 1, 'Mùng 2 Tết'], [3, 1, 'Mùng 3 Tết'], [15, 1, 'Tết Nguyên Tiêu'], [3, 3, 'Tết Hàn Thực'],
    [10, 3, 'Giỗ Tổ Hùng Vương'], [15, 4, 'Lễ Phật Đản'], [5, 5, 'Tết Đoan Ngọ'], [15, 7, 'Lễ Vu Lan'], [15, 8, 'Tết Trung Thu'],
    [9, 9, 'Tết Trùng Cửu'], [10, 10, 'Tết Thường Tân'], [15, 10, 'Tết Hạ Nguyên'], [23, 12, 'Ông Công Ông Táo về trời']];
  var SOLAR_EVENTS = [[1, 1, 'Tết Dương lịch'], [9, 1, 'Ngày Học sinh – Sinh viên'], [3, 2, 'Thành lập Đảng CSVN'], [14, 2, 'Lễ Tình nhân'],
    [27, 2, 'Ngày Thầy thuốc Việt Nam'], [8, 3, 'Quốc tế Phụ nữ'], [26, 3, 'Thành lập Đoàn TNCS HCM'], [21, 4, 'Ngày Sách Việt Nam'],
    [30, 4, 'Ngày Giải phóng miền Nam'], [1, 5, 'Quốc tế Lao động'], [7, 5, 'Chiến thắng Điện Biên Phủ'], [19, 5, 'Ngày sinh Chủ tịch Hồ Chí Minh'],
    [1, 6, 'Quốc tế Thiếu nhi'], [21, 6, 'Ngày Báo chí Việt Nam'], [28, 6, 'Ngày Gia đình Việt Nam'], [27, 7, 'Ngày Thương binh Liệt sĩ'],
    [19, 8, 'Cách mạng tháng Tám'], [2, 9, 'Quốc khánh'], [10, 10, 'Giải phóng Thủ đô'], [13, 10, 'Ngày Doanh nhân Việt Nam'],
    [20, 10, 'Ngày Phụ nữ Việt Nam'], [20, 11, 'Ngày Nhà giáo Việt Nam'], [22, 12, 'Thành lập QĐND Việt Nam'], [24, 12, 'Lễ Giáng sinh']];

  function pad(n) { return n < 10 ? '0' + n : String(n); }
  function canChi(c, h) { return CAN[c] + ' ' + CHI[h]; }
  function esc(s) { return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;'); }
  function t(k, v) { return root.t ? root.t(k, v) : k; }

  /** Thông tin đầy đủ của 1 ngày dương lịch */
  function dayInfo(d, m, y) {
    var jd = L.jdFromDate(d, m, y), lu = L.solar2lunar(d, m, y);
    var dc = (jd + 9) % 10, dh = (jd + 1) % 12;
    var mc = (lu.year * 12 + lu.month + 3) % 10, mh = (lu.month + 1) % 12;
    var yc = (lu.year + 6) % 10, yh = (lu.year + 8) % 12;
    var tk = L.tietKhiIndex(jd), tkPrev = L.tietKhiIndex(jd - 1);
    // Tháng tiết khí (Dần bắt đầu từ Lập Xuân 315°) dùng cho 12 Trực
    var deg = L.sunDegree(jd), tietMonthChi = (2 + Math.floor(((deg - 315 + 720) % 360) / 30)) % 12;
    var thanStart = [0, 2, 4, 6, 8, 10][(lu.month - 1) % 6];
    var than = (dh - thanStart + 12) % 12;
    var hd = GIO_HD[dh % 6], good = [], bad = [], hc0 = (dc % 5) * 2;
    for (var i = 0; i < 12; i++) {
      var h = { chi: i, name: CHI[i], can: CAN[(hc0 + i) % 10], range: pad((i * 2 + 23) % 24) + '–' + pad((i * 2 + 1) % 24) };
      if (hd.charAt(i) === '1') good.push(h); else bad.push(h);
    }
    var ev = [], k;
    for (k = 0; k < LUNAR_EVENTS.length; k++) if (!lu.leap && LUNAR_EVENTS[k][0] === lu.day && LUNAR_EVENTS[k][1] === lu.month) ev.push({ lunar: true, name: LUNAR_EVENTS[k][2] });
    if (!lu.leap && lu.month === 12 && lu.day >= 29) { var nx = L.solar2lunar.apply(null, L.jdToDate(jd + 1)); if (nx.day === 1) ev.push({ lunar: true, name: 'Giao thừa (Trừ tịch)' }); }
    for (k = 0; k < SOLAR_EVENTS.length; k++) if (SOLAR_EVENTS[k][0] === d && SOLAR_EVENTS[k][1] === m) ev.push({ lunar: false, name: SOLAR_EVENTS[k][2] });
    var wd = new Date(y, m - 1, d).getDay();
    return {
      solar: { day: d, month: m, year: y }, weekday: wd, jd: jd, lunar: lu,
      dayCC: canChi(dc, dh), monthCC: canChi(mc, mh) + (lu.leap ? ' (' + t('cal_leap') + ')' : ''), yearCC: canChi(yc, yh),
      napAmDay: E.napAm(dc, dh), napAmYear: E.napAm(yc, yh),
      tiet: TIET[tk], tietStart: tk !== tkPrev, than: THAN[than], hoangDao: !!THAN_GOOD[than],
      truc: TRUC[(dh - tietMonthChi + 12) % 12], goodHours: good, badHours: bad, events: ev,
      hourZero: CAN[hc0] + ' ' + CHI[0]
    };
  }

  /** Lục thập hoa giáp: 60 Can Chi, nạp âm, các năm sinh từ 1840 đến 2100 */
  function sixty() {
    var out = [];
    for (var i = 0; i < 60; i++) {
      var c = i % 10, h = i % 12, years = [];
      for (var y = 1804 + i; y <= 2100; y += 60) if (y >= 1840) years.push(y);
      out.push({ idx: i, name: canChi(c, h), napAm: E.napAm(c, h), element: E.elementOf(E.napAm(c, h)), years: years });
    }
    return out;
  }
  function yearInfo(y) {
    var i = ((y - 1804) % 60 + 60) % 60, s = sixty()[i];
    return { year: y, name: s.name, napAm: s.napAm, element: s.element, age: (new Date()).getFullYear() - y + 1 };
  }

  // ---------------- Giao diện ----------------
  var state = { y: 0, m: 0, sel: null }, host = null, opts = {};
  function weekdays() { var w = String(t('cal_wd')).split(','); return w.length === 7 ? w : ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']; }
  function weekdayFull(wd) { var w = String(t('cal_wd_full')).split(','); return w.length === 7 ? w[(wd + 6) % 7] : ''; }

  function render(el, o) {
    host = el; opts = o || opts;
    var now = new Date();
    if (!state.y) { state.y = now.getFullYear(); state.m = now.getMonth() + 1; state.sel = { d: now.getDate(), m: state.m, y: state.y }; }
    draw();
  }
  function draw() {
    if (!host) return;
    var y = state.y, m = state.m, now = new Date();
    var first = new Date(y, m - 1, 1).getDay(), days = new Date(y, m, 0).getDate();
    var lead = (first + 6) % 7, wds = weekdays(), h = [];
    var l1 = L.solar2lunar(1, m, y), lN = L.solar2lunar(days, m, y);
    h.push('<div class="card cal-head"><div class="cal-nav">',
      '<a class="btn small" data-cal="prev">&larr;</a>',
      '<b class="cal-title">' + esc(t('cal_month_title', { m: m, y: y })) + '</b>',
      '<a class="btn small" data-cal="next">&rarr;</a>',
      '<a class="btn small" data-cal="today">' + esc(t('cal_today')) + '</a>',
      '<select id="calM" class="cal-sel"></select><input type="number" id="calY" class="cal-sel" min="1800" max="2199" value="' + y + '">',
      '</div><div class="hint">' + esc(t('lunar_date')) + ': ' + l1.month + (l1.leap ? '*' : '') + '/' + l1.year + (lN.month !== l1.month ? ' – ' + lN.month + (lN.leap ? '*' : '') + '/' + lN.year : '') +
      ' · ' + esc(canChi((l1.year + 6) % 10, (l1.year + 8) % 12)) + ' · ' + esc(t('cal_tz')) + '</div></div>');
    h.push('<div class="cal-wrap"><div class="cal-col"><table class="cal-grid"><thead><tr>');
    for (var i = 0; i < 7; i++) h.push('<th' + (i >= 5 ? ' class="we"' : '') + '>' + esc(wds[i]) + '</th>');
    h.push('</tr></thead><tbody><tr>');
    for (i = 0; i < lead; i++) h.push('<td class="empty"></td>');
    for (var d = 1; d <= days; d++) {
      var col = (lead + d - 1) % 7;
      if (col === 0 && d > 1) h.push('</tr><tr>');
      var info = dayInfo(d, m, y), lu = info.lunar;
      var cls = ['cal-day'];
      if (col >= 5) cls.push('we');
      if (info.hoangDao) cls.push('hd');
      if (info.events.length) cls.push('ev');
      if (lu.day === 1 || lu.day === 15) cls.push('ld');
      if (d === now.getDate() && m === now.getMonth() + 1 && y === now.getFullYear()) cls.push('today');
      if (state.sel && state.sel.d === d && state.sel.m === m && state.sel.y === y) cls.push('sel');
      h.push('<td class="' + cls.join(' ') + '" data-day="' + d + '"><div class="sd">' + d + '</div><div class="ldn">' +
        (lu.day === 1 || d === 1 ? lu.day + '/' + lu.month + (lu.leap ? '*' : '') : lu.day) + '</div>' +
        (info.tietStart ? '<div class="tk">' + esc(info.tiet) + '</div>' : '') + '</td>');
    }
    var tail = (7 - (lead + days) % 7) % 7;
    for (i = 0; i < tail; i++) h.push('<td class="empty"></td>');
    h.push('</tr></tbody></table><div class="hint cal-legend"><span class="dot hd"></span> ' + esc(t('cal_hd')) + ' &nbsp; <span class="dot ev"></span> ' + esc(t('cal_events')) + ' &nbsp; * ' + esc(t('cal_leap')) + '</div></div>');
    h.push('<div class="cal-side"><div class="card" id="calDetail"></div></div></div>');
    // Đổi ngày
    h.push('<div class="card"><h2>' + esc(t('cal_convert')) + '</h2><div class="form-row">',
      '<div class="field tiny"><label>' + esc(t('f_day')) + '</label><input type="number" id="cvD" min="1" max="31" value="' + (state.sel ? state.sel.d : 1) + '"></div>',
      '<div class="field tiny"><label>' + esc(t('f_month')) + '</label><input type="number" id="cvM" min="1" max="12" value="' + m + '"></div>',
      '<div class="field tiny"><label>' + esc(t('f_year')) + '</label><input type="number" id="cvY" min="1800" max="2199" value="' + y + '"></div>',
      '<div class="field"><label>&nbsp;</label><label class="check"><input type="checkbox" id="cvLeap"> ' + esc(t('f_leap')) + '</label></div></div>',
      '<a class="btn" id="cvS2L">' + esc(t('cal_s2l')) + '</a><a class="btn" id="cvL2S">' + esc(t('cal_l2s')) + '</a><div id="cvOut" class="cal-out"></div></div>');
    // Lục thập hoa giáp
    h.push('<div class="card"><h2>' + esc(t('cal_60')) + '</h2><div class="hint">' + esc(t('cal_60_d')) + '</div><div class="form-row" style="margin-top:6px">',
      '<div class="field tiny"><input type="number" id="c60Y" min="1800" max="2199" placeholder="' + esc(t('f_year')) + '"></div>',
      '<div class="field"><a class="btn primary" id="c60Go">' + esc(t('cal_find_year')) + '</a></div></div><div id="c60Out" class="cal-out"></div>',
      '<div class="board-scroll"><table class="sixty"><thead><tr><th>#</th><th>' + esc(t('year_cc')) + '</th><th>' + esc(t('cal_napam')) + '</th><th>' + esc(t('cal_years')) + '</th></tr></thead><tbody>');
    var sx = sixty(), cur = yearInfo(now.getFullYear());
    for (i = 0; i < 60; i++) {
      h.push('<tr' + (sx[i].name === cur.name ? ' class="cur"' : '') + ' data-s60="' + i + '"><td>' + (i + 1) + '</td><td><b>' + esc(sx[i].name) + '</b></td><td class="' + root.AATheme.eleClass(sx[i].element) + '">' + esc(sx[i].napAm) + '</td><td>' + sx[i].years.join(' · ') + '</td></tr>');
    }
    h.push('</tbody></table></div></div>');
    host.innerHTML = h.join('');
    var ms = []; for (i = 1; i <= 12; i++) ms.push([i, t('f_month') + ' ' + i]);
    var sel = document.getElementById('calM'); while (sel.options.length) sel.remove(0);
    for (i = 0; i < ms.length; i++) { var op = document.createElement('option'); op.value = String(ms[i][0]); op.text = ms[i][1]; sel.appendChild(op); }
    sel.value = String(m);
    showDetail();
  }

  function hoursHtml(list) {
    var a = [];
    for (var i = 0; i < list.length; i++) a.push('<span class="hour">' + esc(list[i].name) + ' <small>(' + list[i].range + ')</small></span>');
    return a.join(' ');
  }
  function showDetail() {
    var box = document.getElementById('calDetail'); if (!box || !state.sel) return;
    var s = state.sel, inf = dayInfo(s.d, s.m, s.y), lu = inf.lunar, h = [];
    h.push('<div class="cal-big"><div class="cal-bigd">' + s.d + '</div><div>' + esc(weekdayFull(inf.weekday)) + '<br>' + pad(s.d) + '/' + pad(s.m) + '/' + s.y + '</div>',
      '<div class="cal-bigl"><div class="cal-bigd">' + lu.day + '</div><div>' + esc(t('lunar_date')) + '<br>' + lu.day + '/' + lu.month + (lu.leap ? ' ' + esc(t('cal_leap')) : '') + '/' + lu.year + '</div></div></div>');
    h.push('<div class="detail-stars">',
      '<div><b>' + esc(t('day_cc')) + ':</b> ' + esc(inf.dayCC) + ' <span class="hint">(' + esc(inf.napAmDay) + ')</span></div>',
      '<div><b>' + esc(t('month_cc')) + ':</b> ' + esc(inf.monthCC) + '</div>',
      '<div><b>' + esc(t('year_cc')) + ':</b> ' + esc(inf.yearCC) + ' <span class="hint">(' + esc(inf.napAmYear) + ')</span></div>',
      '<div><b>' + esc(t('cal_tiet')) + ':</b> ' + esc(inf.tiet) + (inf.tietStart ? ' <span class="badge">' + esc(t('cal_tiet_start')) + '</span>' : '') + '</div>',
      '<div><b>' + esc(t('cal_daytype')) + ':</b> <span class="' + (inf.hoangDao ? 's-good' : 's-bad') + '">' + esc(inf.hoangDao ? t('cal_hd') : t('cal_hkd')) + '</span> (' + esc(inf.than) + ') · ' + esc(t('cal_truc')) + ' ' + esc(inf.truc) + '</div>',
      '<div><b>' + esc(t('cal_hour0')) + ':</b> ' + esc(inf.hourZero) + '</div>',
      '<div class="grp">' + esc(t('cal_good_hours')) + '</div><div class="s-good">' + hoursHtml(inf.goodHours) + '</div>',
      '<div class="grp">' + esc(t('cal_bad_hours')) + '</div><div class="hint">' + hoursHtml(inf.badHours) + '</div>');
    if (inf.events.length) {
      h.push('<div class="grp">' + esc(t('cal_events')) + '</div>');
      for (var i = 0; i < inf.events.length; i++) h.push('<div>' + (inf.events[i].lunar ? '☾ ' : '☀ ') + esc(inf.events[i].name) + '</div>');
    }
    h.push('<div class="hint">JD ' + inf.jd + '</div></div><div class="btn-row"><a class="btn primary" id="calBuild">' + esc(t('cal_build_chart')) + '</a></div>');
    box.innerHTML = h.join('');
    return inf;
  }

  function onClick(e) {
    var el = e.target || e.srcElement, it;
    if ((it = el.closest('[data-cal]'))) {
      var a = it.getAttribute('data-cal');
      if (a === 'prev') { state.m--; if (state.m < 1) { state.m = 12; state.y--; } }
      else if (a === 'next') { state.m++; if (state.m > 12) { state.m = 1; state.y++; } }
      else { var n = new Date(); state.y = n.getFullYear(); state.m = n.getMonth() + 1; state.sel = { d: n.getDate(), m: state.m, y: state.y }; }
      draw(); return;
    }
    if ((it = el.closest('[data-day]'))) { state.sel = { d: +it.getAttribute('data-day'), m: state.m, y: state.y }; draw(); return; }
    if (el.id === 'calBuild' && opts.onBuild && state.sel) { opts.onBuild(state.sel.d, state.sel.m, state.sel.y); return; }
    if (el.id === 'cvS2L' || el.id === 'cvL2S') { convert(el.id === 'cvS2L'); return; }
    if (el.id === 'c60Go') { findYear(); return; }
    if ((it = el.closest('[data-s60]'))) { var s = sixty()[+it.getAttribute('data-s60')]; showYear(s.years[s.years.length > 3 ? 3 : 0]); }
  }
  function onChange(e) {
    var el = e.target || e.srcElement;
    if (el.id === 'calM' || el.id === 'calY') {
      var mm = parseInt(document.getElementById('calM').value, 10), yy = parseInt(document.getElementById('calY').value, 10);
      if (mm >= 1 && mm <= 12 && yy >= 1800 && yy <= 2199) { state.m = mm; state.y = yy; draw(); }
    }
  }
  function convert(s2l) {
    var d = parseInt(document.getElementById('cvD').value, 10), m = parseInt(document.getElementById('cvM').value, 10), y = parseInt(document.getElementById('cvY').value, 10);
    var out = document.getElementById('cvOut'), leap = document.getElementById('cvLeap').checked, r;
    if (!(y >= 1800 && y <= 2199 && m >= 1 && m <= 12 && d >= 1 && d <= 31)) { out.innerHTML = '<span class="status-bad">' + esc(t('invalid_date')) + '</span>'; return null; }
    if (s2l) {
      var dt = new Date(y, m - 1, d);
      if (dt.getMonth() !== m - 1) { out.innerHTML = '<span class="status-bad">' + esc(t('invalid_date')) + '</span>'; return null; }
      var lu = L.solar2lunar(d, m, y);
      r = { solar: [d, m, y], lunar: lu };
    } else {
      var so = L.lunar2solar(d, m, y, leap ? 1 : 0);
      if (!so || (d > 29 && L.lunarMonthLength(m, y, leap ? 1 : 0) < d)) { out.innerHTML = '<span class="status-bad">' + esc(t('invalid_lunar')) + '</span>'; return null; }
      r = { solar: so, lunar: { day: d, month: m, year: y, leap: leap } };
    }
    var inf = dayInfo(r.solar[0], r.solar[1], r.solar[2]);
    out.innerHTML = '<b>' + esc(t('solar_date')) + ':</b> ' + esc(weekdayFull(inf.weekday)) + ', ' + pad(r.solar[0]) + '/' + pad(r.solar[1]) + '/' + r.solar[2] +
      '<br><b>' + esc(t('lunar_date')) + ':</b> ' + r.lunar.day + '/' + r.lunar.month + (r.lunar.leap ? ' ' + esc(t('cal_leap')) : '') + '/' + r.lunar.year +
      ' – ' + esc(inf.dayCC) + ', ' + esc(t('month_cc').toLowerCase()) + ' ' + esc(inf.monthCC) + ', ' + esc(t('year_cc').toLowerCase()) + ' ' + esc(inf.yearCC) +
      ' <a class="btn small" data-goto="' + r.solar.join('-') + '">' + esc(t('cal_open_day')) + '</a>';
    var g = out.querySelector('[data-goto]');
    if (g) g.onclick = function () { state.y = r.solar[2]; state.m = r.solar[1]; state.sel = { d: r.solar[0], m: r.solar[1], y: r.solar[2] }; draw(); try { root.scrollTo(0, 0); } catch (x) { /* bỏ qua */ } };
    return r;
  }
  function showYear(y) {
    var out = document.getElementById('c60Out'), yi = yearInfo(y);
    out.innerHTML = '<b>' + y + '</b>: ' + esc(yi.name) + ' · ' + esc(t('cal_napam')) + ' <span class="' + root.AATheme.eleClass(yi.element) + '">' + esc(yi.napAm) + '</span> (' + esc(yi.element) + ') · ' + esc(t('cal_age')) + ' ' + yi.age;
    return yi;
  }
  function findYear() { var y = parseInt(document.getElementById('c60Y').value, 10); if (y >= 1800 && y <= 2199) return showYear(y); return null; }

  function bind(el) { if (el.addEventListener) { el.addEventListener('click', onClick, false); el.addEventListener('change', onChange, false); } else { el.attachEvent('onclick', onClick); el.attachEvent('onchange', onChange); } }

  root.AACalendar = {
    dayInfo: dayInfo, sixty: sixty, yearInfo: yearInfo, render: render, bind: bind, redraw: draw,
    select: function (d, m, y) { state.y = y; state.m = m; state.sel = { d: d, m: m, y: y }; draw(); return showDetail(); },
    convert: convert, findYear: findYear, state: state, TIET: TIET
  };
})(window);
