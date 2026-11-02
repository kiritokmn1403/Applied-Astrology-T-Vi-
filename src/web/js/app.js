/*
 * Applied Astrology v0.0.2 - Điều khiển giao diện
 * ES5 thuần (IE11 / Android 6 WebView / Edge WebView2 / Chrome).
 */
(function (root) {
  'use strict';
  var VERSION = '0.0.2';
  var H = root.AAHost, I = root.AAI18n, E = root.AAEngine, V = root.AAChartView, Lib = root.AALib, A = root.AAAssistant;
  var $ = function (id) { return document.getElementById(id); };
  var esc = V.esc, t = root.t;
  var CHI_HOURS = ['23–01', '01–03', '03–05', '05–07', '07–09', '09–11', '11–13', '13–15', '15–17', '17–19', '19–21', '21–23'];
  var DEFAULTS = { lang: 'vi', theme: 'celestial-dark', font: 'normal', layout: 'auto', desktop: null, ts: true, bs: false, tt: true, luu: true, leap: 'same' };
  var S = Object.assign({}, DEFAULTS, H.get('aa.settings', {}));
  var chart = null, selChi = -1, readerCtx = null, lastLayout = null;

  // IE11 không hỗ trợ gán innerHTML cho <select> -> tạo option bằng DOM
  function fillSelect(sel, items) {
    while (sel.options.length) sel.remove(0);
    for (var i = 0; i < items.length; i++) { var o = document.createElement('option'); o.value = String(items[i][0]); o.text = items[i][1]; sel.appendChild(o); }
  }
  function on(el, ev, fn) { if (el) { if (el.addEventListener) el.addEventListener(ev, fn, false); else el.attachEvent('on' + ev, fn); } }
  function saveSettings() { H.set('aa.settings', S); }
  function toast(msg) {
    var el = $('toast'); el.textContent = msg; el.className = 'toast';
    clearTimeout(toast._t); toast._t = setTimeout(function () { el.className = 'toast hidden'; }, 2200);
  }
  function modal(html) { $('modalBox').innerHTML = html + '<div style="margin-top:12px"><a class="btn" id="mClose">' + esc(t('btn_close')) + '</a></div>'; $('modal').className = 'modal'; on($('mClose'), 'click', closeModal); }
  function closeModal() { $('modal').className = 'modal hidden'; }

  // ---------------- Điều hướng ----------------
  var current = 'chart';
  function show(view) {
    current = view;
    var navs = $('nav').getElementsByTagName('a');
    for (var i = 0; i < navs.length; i++) navs[i].className = navs[i].getAttribute('data-view') === view ? 'active' : '';
    var views = document.querySelectorAll('.view');
    for (i = 0; i < views.length; i++) views[i].className = 'view' + (views[i].id === 'view-' + view ? ' active' : '');
    if (view === 'library' && !readerCtx) renderLibraryHome();
    if (view === 'saved') renderSaved();
    if (view === 'settings') renderAbout();
    try { root.scrollTo(0, 0); } catch (e) { /* bỏ qua */ }
  }
  on($('nav'), 'click', function (e) { var a = (e.target || e.srcElement).closest('a'); if (a) show(a.getAttribute('data-view')); });

  // ---------------- Cài đặt ----------------
  function applySettings(rerender) {
    root.AATheme.apply(S.theme);
    var html = document.documentElement;
    html.className = html.className.replace(/\bfs-\S+/g, '') + ' fs-' + S.font;
    var desk = S.desktop === null ? (root.innerWidth || 0) >= 1100 : !!S.desktop;
    document.body.className = desk ? 'desktop' : '';
    if (rerender && chart) renderChart();
  }
  function fillSettingsUi() {
    var sel = $('sLang'), h = [];
    for (var i = 0; i < I.LANGS.length; i++) h.push([I.LANGS[i][0], I.LANGS[i][1] + ' (' + I.LANGS[i][0] + ')']);
    fillSelect(sel, h);
    sel.value = S.lang; $('sTheme').value = S.theme; $('sFont').value = S.font; $('sLayout').value = S.layout;
    $('sDesktop').checked = S.desktop === null ? (root.innerWidth || 0) >= 1100 : !!S.desktop;
    $('sTS').checked = !!S.ts; $('sBS').checked = !!S.bs; $('sTT').checked = !!S.tt; $('sLuu').checked = !!S.luu; $('sLeap').value = S.leap;
  }
  function bindSettings() {
    on($('sLang'), 'change', function () { S.lang = this.value; saveSettings(); I.setLang(S.lang, function () { if (chart) renderChart(); renderLibraryHome(); }); });
    on($('sTheme'), 'change', function () { S.theme = this.value; saveSettings(); applySettings(true); });
    on($('sFont'), 'change', function () { S.font = this.value; saveSettings(); applySettings(false); });
    on($('sLayout'), 'change', function () { S.layout = this.value; saveSettings(); if (chart) renderChart(); });
    on($('sDesktop'), 'change', function () { S.desktop = this.checked; saveSettings(); applySettings(true); });
    var boxes = { sTS: 'ts', sBS: 'bs', sTT: 'tt', sLuu: 'luu' };
    for (var id in boxes) if (boxes.hasOwnProperty(id)) (function (k) { on($(id), 'click', function () { S[k] = this.checked; saveSettings(); if (chart) renderChart(); }); })(boxes[id]);
    on($('sLeap'), 'change', function () { S.leap = this.value; saveSettings(); if (chart) build(); });
    on($('sNet'), 'click', checkNetwork);
    on($('sExport'), 'click', exportBackup);
    on($('sImportFile'), 'change', function () {
      var f = this.files && this.files[0]; if (!f || !root.FileReader) return;
      var r = new FileReader(); r.onload = function () { importBackup(r.result); }; r.readAsText(f);
    });
    on($('sImportGo'), 'click', function () { importBackup($('sImportText').value); });
    on($('sReset'), 'click', function () { S = Object.assign({}, DEFAULTS); saveSettings(); fillSettingsUi(); applySettings(true); I.setLang(S.lang); });
    on($('sClear'), 'click', function () {
      if (!root.confirm || root.confirm(t('confirm_clear'))) {
        H.set('aa.saved', []); H.set('aa.bookmarks', []); H.set('aa.recent', []); H.set('aa.last', null);
        S = Object.assign({}, DEFAULTS); saveSettings(); fillSettingsUi(); applySettings(true); toast(t('deleted'));
      }
    });
  }
  function renderAbout() {
    var info = H.info();
    $('aboutInfo').innerHTML = esc(t('s_version')) + ': <b>' + VERSION + '</b><br>' + esc(t('s_platform')) + ': ' + esc(info.kind) +
      (info.os ? ' · ' + esc(info.os) : '') + (info.abi ? ' · ' + esc(info.abi) : '') + (info.engine ? ' · ' + esc(info.engine) : '') +
      '<br>' + esc(Lib.totalArticles() + ' ' + t('articles')) + ' · ' + I.LANGS.length + ' ' + esc(t('s_language').toLowerCase()) +
      '<br>QR: Project Nayuki (MIT) · ' + 'Âm lịch: thuật toán Hồ Ngọc Đức';
  }
  function checkNetwork() {
    var out = $('netOut'); out.innerHTML = esc(t('net_checking'));
    H.httpGet('https://www.cloudflare.com/cdn-cgi/trace', function (r) {
      if (!r || !r.ok) { out.innerHTML = '<span class="status-bad">' + esc(t('net_fail')) + '</span>'; return; }
      var kv = {}, lines = (r.body || '').split('\n');
      for (var i = 0; i < lines.length; i++) { var p = lines[i].indexOf('='); if (p > 0) kv[lines[i].substring(0, p)] = lines[i].substring(p + 1); }
      var warp = kv.warp && kv.warp !== 'off';
      out.innerHTML = '<span class="status-ok">' + esc(t('net_ok')) + '</span> · ' + esc(t('net_latency')) + ': ' + r.ms + ' ms<br>' +
        esc(t('net_ip')) + ': ' + esc(kv.ip || '?') + ' · ' + esc(t('net_loc')) + ': ' + esc(kv.loc || '?') + ' · ' + (kv.colo ? 'PoP ' + esc(kv.colo) + ' · ' : '') +
        '<span class="' + (warp ? 'status-ok' : '') + '">' + esc(warp ? t('net_vpn_on') : t('net_vpn_off')) + '</span>';
      H.log('net ' + r.ms + 'ms ip=' + kv.ip + ' warp=' + kv.warp);
    });
  }
  function exportBackup() {
    var data = { app: 'Applied Astrology', version: VERSION, date: new Date().toISOString ? new Date().toISOString() : '', settings: S,
      saved: H.get('aa.saved', []), bookmarks: Lib.bookmarks(), recent: Lib.recent() };
    var name = 'AppliedAstrology-backup-' + (data.date || '').substring(0, 10) + '.json';
    var r = H.saveFile(name, JSON.stringify(data, null, 2));
    toast(r ? (t('s_export') + ': ' + r) : t('import_fail'));
    return data;
  }
  function importBackup(text) {
    try {
      var d = JSON.parse(text);
      if (!d || d.app !== 'Applied Astrology') throw new Error('bad');
      if (d.settings) { S = Object.assign({}, DEFAULTS, d.settings); saveSettings(); }
      if (d.saved) H.set('aa.saved', d.saved);
      if (d.bookmarks) H.set('aa.bookmarks', d.bookmarks);
      if (d.recent) H.set('aa.recent', d.recent);
      fillSettingsUi(); applySettings(true); I.setLang(S.lang); toast(t('import_ok'));
      return true;
    } catch (e) { toast(t('import_fail')); return false; }
  }

  // ---------------- Lá số ----------------
  function fillForm() {
    var h = [], i;
    for (i = 1; i <= 31; i++) h.push([i, String(i)]); fillSelect($('fDay'), h);
    h = []; for (i = 1; i <= 12; i++) h.push([i, String(i)]); fillSelect($('fMonth'), h);
    h = []; for (i = 0; i < 12; i++) h.push([i, E.CHI[i] + ' (' + CHI_HOURS[i] + ')']); fillSelect($('fHour'), h);
    var last = H.get('aa.last', null) || { name: 'Nguyễn Văn An', gender: 'Nam', calendar: 'solar', day: 15, month: 8, year: 1995, hourChi: 6, leap: false };
    setForm(last);
    $('fView').value = (new Date()).getFullYear();
    on($('fCal'), 'change', function () { $('leapWrap').className = 'check' + (this.value === 'lunar' ? '' : ' hidden'); });
  }
  function setForm(o) {
    $('fName').value = o.name || ''; $('fGender').value = o.gender || 'Nam'; $('fCal').value = o.calendar || 'solar';
    $('fDay').value = String(o.day); $('fMonth').value = String(o.month); $('fYear').value = o.year; $('fHour').value = String(o.hourChi);
    $('fLeap').checked = !!o.leap; $('leapWrap').className = 'check' + (o.calendar === 'lunar' ? '' : ' hidden');
  }
  function readForm() {
    return { name: $('fName').value.trim() || 'Thân Chủ', gender: $('fGender').value, calendar: $('fCal').value,
      day: parseInt($('fDay').value, 10), month: parseInt($('fMonth').value, 10), year: parseInt($('fYear').value, 10),
      hourChi: parseInt($('fHour').value, 10), leap: $('fLeap').checked, leapMode: S.leap, viewYear: parseInt($('fView').value, 10) || (new Date()).getFullYear() };
  }
  function validSolar(d, m, y) { var dt = new Date(y, m - 1, d); return y >= 1900 && y <= 2100 && dt.getFullYear() === y && dt.getMonth() === m - 1 && dt.getDate() === d; }
  function build() {
    var o = readForm();
    H.log('build ' + [o.calendar, o.day, o.month, o.year, o.hourChi].join(','));
    if (!(o.year >= 1900 && o.year <= 2100)) { toast(t('invalid_date')); return null; }
    if (o.calendar === 'solar' && !validSolar(o.day, o.month, o.year)) { toast(t('invalid_date')); return null; }
    try { chart = new E.TuViChart(o); }
    catch (e) { H.log('build error ' + (e && (e.message || e.description))); toast(e.message === 'INVALID_LUNAR_DATE' ? t('invalid_lunar') : t('invalid_date')); return null; }
    H.set('aa.last', o);
    selChi = chart.menh;
    renderChart();
    return chart;
  }
  function renderChart() {
    if (!chart) return;
    var tp = selChi >= 0 ? chart.tamPhuong(selChi) : null;
    lastLayout = V.render($('chartBox'), chart, { layout: S.layout, desktop: document.body.className.indexOf('desktop') >= 0, sel: selChi, tp: tp,
      trangSinh: S.ts, bacSi: S.bs, thaiTue: S.tt, luu: S.luu });
    if (selChi >= 0) renderDetail(selChi);
  }
  function renderDetail(chi) {
    var rel = Lib.related(chart, chi);
    $('detailBox').innerHTML = V.detailHtml(chart, chi, rel);
  }
  on($('chartBox'), 'click', function (e) {
    var p = (e.target || e.srcElement).closest('.palace');
    if (!p || !chart) return;
    selChi = parseInt(p.getAttribute('data-chi'), 10);
    renderChart();
    if (document.body.className.indexOf('desktop') < 0 && $('detailBox').scrollIntoView) $('detailBox').scrollIntoView();
  });
  on($('detailBox'), 'click', function (e) {
    var it = (e.target || e.srcElement).closest('[data-open]');
    if (it) { var k = it.getAttribute('data-open').split(':'); openArticle(k[0], parseInt(k[1], 10)); }
  });
  on($('btnBuild'), 'click', build);
  on($('btnSave'), 'click', function () {
    if (!chart && !build()) return;
    var list = H.get('aa.saved', []), o = readForm();
    o.saved = Date.now(); o.summary = chart.yearCanChi + ' · ' + chart.cucName + ' · ' + t('menh') + ' ' + E.CHI[chart.menh];
    list.unshift(o); if (list.length > 200) list.length = 200;
    H.set('aa.saved', list); toast(t('saved_ok'));
  });
  function exportPng() {
    if (!chart && !build()) return null;
    var cv = V.toCanvas(chart, S.theme);
    var url = cv.toDataURL('image/png');
    var name = 'LaSo-' + chart.name.replace(/[^\wÀ-ỹ]+/g, '_') + '-' + chart.solar.year + '.png';
    var r = H.saveFile(name, url);
    modal('<h3>' + esc(t('png_ok')) + '</h3><img src="' + url + '" alt=""><div class="hint">' + esc(r || name) + '</div>');
    return url;
  }
  on($('btnPng'), 'click', exportPng);
  function chartCode(o) { return 'AA2|' + [o.name, o.gender === 'Nữ' ? 'F' : 'M', o.calendar === 'lunar' ? 'L' : 'S', o.day, o.month, o.year, o.hourChi, o.leap ? 1 : 0].join('|'); }
  function showQr() {
    if (!chart && !build()) return null;
    var code = chartCode(readForm());
    var qr = root.AAQr.encodeText(code, 'M');
    var cv = root.AAQr.toCanvas(qr, 7, 4);
    var url = cv.toDataURL('image/png');
    modal('<h3>' + esc(t('qr_title')) + '</h3><img src="' + url + '" alt="QR"><div class="hint">' + esc(t('qr_desc')) + '</div><div class="hint" style="word-break:break-all">' + esc(code) + '</div>');
    return { code: code, size: qr.size };
  }
  on($('btnQr'), 'click', showQr);
  function summaryText() {
    if (!chart) return '';
    var p = chart.palaces[chart.menh], ms = [];
    for (var i = 0; i < p.major.length; i++) ms.push(p.major[i].name);
    return chart.name + ' – ' + chart.amDuong + ', ' + chart.yearCanChi + ', ' + chart.napAm + ', ' + chart.cucName + '. ' + t('menh') + ' ' + p.canChi + ': ' + (ms.join(', ') || t('vcd')) + '.\n' + chartCode(readForm()) + '\n(Applied Astrology ' + VERSION + ')';
  }
  on($('btnShare'), 'click', function () { if (!chart && !build()) return; if (!H.share(summaryText())) modal('<textarea readonly>' + esc(summaryText()) + '</textarea>'); });
  on($('btnPrint'), 'click', function () { try { root.print(); } catch (e) { /* bỏ qua */ } });
  on($('modal'), 'click', function (e) { if ((e.target || e.srcElement) === $('modal')) closeModal(); });
  function parseCode(code) {
    var p = String(code || '').split('|');
    if (p[0] !== 'AA2' || p.length < 9) return null;
    return { name: p[1], gender: p[2] === 'F' ? 'Nữ' : 'Nam', calendar: p[3] === 'L' ? 'lunar' : 'solar', day: +p[4], month: +p[5], year: +p[6], hourChi: +p[7], leap: p[8] === '1' };
  }

  // ---------------- Thư viện & đọc sách ----------------
  function renderLibraryHome() {
    readerCtx = null;
    $('libDesc').textContent = t('lib_desc', { n: Lib.totalArticles(), c: Lib.chapters.length });
    var rec = Lib.recent();
    $('libContinue').innerHTML = rec.length ? '<a class="btn small" data-open="' + rec[0].ch + ':' + rec[0].i + '">' + esc(t('lib_continue')) + ': ' + esc(rec[0].t) + '</a>' : '';
    var h = ['<div class="chapters">'];
    for (var i = 0; i < Lib.chapters.length; i++) {
      var c = Lib.chapters[i];
      h.push('<div class="list-item" data-ch="' + c.id + '"><div class="t">' + c.n + '. ' + esc(Lib.chapterName(c.id)) + '</div><div class="s">' + c.count + ' ' + esc(t('articles')) + '</div></div>');
    }
    h.push('</div>');
    $('libBody').innerHTML = h.join('');
  }
  function renderChapter(id) {
    var c = Lib.chapterById(id), h = ['<div class="card"><a class="btn small" data-home="1">&larr; ' + esc(t('btn_back')) + '</a><h2>' + c.n + '. ' + esc(Lib.chapterName(id)) + '</h2>'];
    for (var i = 0; i < c.articles.length; i++) {
      h.push('<div class="list-item" data-open="' + id + ':' + i + '"><div class="t">' + (i + 1) + '. ' + esc(c.articles[i].t) + '</div><div class="s">' + c.articles[i].len.toLocaleString() + ' ' + esc(t('chars')) + (Lib.isBookmarked(id, i) ? ' · ★' : '') + '</div></div>');
    }
    h.push('</div>');
    $('libBody').innerHTML = h.join('');
    readerCtx = { list: id };
  }
  function openArticle(ch, i, scroll) {
    show('library');
    $('libBody').innerHTML = '<div class="card">' + esc(t('lib_loading')) + '</div>';
    Lib.get(ch, i, function (a) {
      if (!a) { $('libBody').innerHTML = '<div class="card">' + esc(t('no_related')) + '</div>'; return; }
      readerCtx = { ch: ch, i: i };
      var c = Lib.chapterById(ch), bm = Lib.isBookmarked(ch, i);
      $('libBody').innerHTML = '<div class="card reader"><div class="reader-tools card">' +
        '<a class="btn small" data-chlist="' + ch + '">&larr; ' + esc(Lib.chapterName(ch)) + '</a>' +
        (i > 0 ? '<a class="btn small" data-open="' + ch + ':' + (i - 1) + '">' + esc(t('btn_prev')) + '</a>' : '') +
        (i < c.count - 1 ? '<a class="btn small" data-open="' + ch + ':' + (i + 1) + '">' + esc(t('btn_next')) + '</a>' : '') +
        '<a class="btn small" id="rBm">' + (bm ? '★ ' : '☆ ') + esc(t('btn_bookmark')) + '</a>' +
        '<a class="btn small" id="rFontM">A−</a><a class="btn small" id="rFontP">A+</a>' +
        (I.current() !== 'vi' ? '<a class="btn small" id="rTr">' + esc(t('btn_translate')) + '</a>' : '') +
        '</div><h1>' + esc(a.title) + '</h1><div class="hint">' + esc(Lib.chapterName(ch)) + ' · ' + (i + 1) + '/' + c.count + ' · ' + esc(t('reader_original_note')) + '</div>' +
        '<div class="reader-body" id="rBody" style="font-size:' + (H.get('aa.readerFont', 1.02)) + 'rem">' + esc(a.body) + '</div>' +
        '<div class="btn-row">' + (i < c.count - 1 ? '<a class="btn primary" data-open="' + ch + ':' + (i + 1) + '">' + esc(t('btn_next')) + ' &rarr;</a>' : '') + '</div></div>';
      on($('rBm'), 'click', function () { var on2 = Lib.toggleBookmark(ch, i); this.innerHTML = (on2 ? '★ ' : '☆ ') + esc(t('btn_bookmark')); });
      function font(d) { var f = Math.max(0.8, Math.min(1.8, H.get('aa.readerFont', 1.02) + d)); H.set('aa.readerFont', f); $('rBody').style.fontSize = f + 'rem'; }
      on($('rFontM'), 'click', function () { font(-0.08); }); on($('rFontP'), 'click', function () { font(0.08); });
      on($('rTr'), 'click', function () {
        var lang = I.current().replace('-BR', '').replace('-419', '').replace('zh-CN', 'zh-CN').replace('zh-TW', 'zh-TW');
        H.openUrl('https://translate.google.com/?sl=vi&tl=' + encodeURIComponent(lang) + '&op=translate&text=' + encodeURIComponent(a.body.substring(0, 4500)));
      });
      Lib.markRead(ch, i, 0);
      try { root.scrollTo(0, scroll || 0); } catch (e) { /* bỏ qua */ }
    });
  }
  function runSearch(q, cb) {
    q = q === undefined ? $('libQ').value : q;
    $('libBody').innerHTML = '<div class="card" id="libProg">' + esc(t('lib_loading')) + '</div>';
    Lib.search(q, function (res) {
      var h = ['<div class="card"><a class="btn small" data-home="1">&larr; ' + esc(t('btn_back')) + '</a> <b>' + esc(t('lib_results', { n: res.length })) + '</b></div>'];
      var fq = H.fold(q).trim();
      for (var i = 0; i < Math.min(res.length, 120); i++) {
        var r = res[i], sn = r.snippet || '';
        h.push('<div class="list-item" data-open="' + r.ch + ':' + r.i + '"><div class="t">' + esc(r.title) + '</div><div class="s">' + esc(Lib.chapterName(r.ch)) + '</div><div class="s">' + highlight(sn, fq) + '</div></div>');
      }
      $('libBody').innerHTML = h.join('');
      readerCtx = { search: q };
      if (cb) cb(res);
    }, function (d, n) { var el = $('libProg'); if (el) el.textContent = t('lib_loading') + ' ' + d + '/' + n; });
  }
  function highlight(sn, fq) {
    if (!fq) return esc(sn);
    var f = H.fold(sn), p = f.indexOf(fq);
    if (p < 0) return esc(sn);
    return esc(sn.substring(0, p)) + '<mark>' + esc(sn.substring(p, p + fq.length)) + '</mark>' + esc(sn.substring(p + fq.length));
  }
  on($('libGo'), 'click', function () { runSearch(); });
  on($('libQ'), 'keydown', function (e) { if ((e.keyCode || e.which) === 13) runSearch(); });
  function libClick(e) {
    var el = (e.target || e.srcElement), it;
    if ((it = el.closest('[data-open]'))) { var k = it.getAttribute('data-open').split(':'); openArticle(k[0], parseInt(k[1], 10)); return; }
    if ((it = el.closest('[data-ch]'))) { renderChapter(it.getAttribute('data-ch')); return; }
    if ((it = el.closest('[data-chlist]'))) { renderChapter(it.getAttribute('data-chlist')); return; }
    if (el.closest('[data-home]')) renderLibraryHome();
  }
  on($('libBody'), 'click', libClick); on($('libContinue'), 'click', libClick);

  // ---------------- Trợ lý ----------------
  function say(cls, text, refs) {
    var d = document.createElement('div'); d.className = 'msg ' + cls; d.textContent = text;
    if (refs) for (var i = 0; i < refs.length; i++) {
      var a = document.createElement('span'); a.className = 'ref'; a.setAttribute('data-open', refs[i].ch + ':' + refs[i].i); a.textContent = '→ ' + refs[i].title; d.appendChild(a);
    }
    $('chat').appendChild(d); $('chat').scrollTop = $('chat').scrollHeight;
    return d;
  }
  function ask(q, cb) {
    if (!chart) build();
    if (q === '__overview') { var ov = A.overview(chart); say('ai', ov); if (cb) cb(ov); return; }
    if (!q) return;
    say('user', q);
    A.answer(chart, q, function (r) { say('ai', r.text, r.refs); if (cb) cb(r.text); });
  }
  on($('asSend'), 'click', function () { var q = $('asQ').value.trim(); $('asQ').value = ''; ask(q); });
  on($('asQ'), 'keydown', function (e) { if ((e.keyCode || e.which) === 13) { var q = $('asQ').value.trim(); $('asQ').value = ''; ask(q); } });
  on($('asChips'), 'click', function (e) { var c = (e.target || e.srcElement).closest('[data-q]'); if (c) ask(c.getAttribute('data-q') === '__overview' ? '__overview' : c.textContent); });
  on($('chat'), 'click', function (e) { var r = (e.target || e.srcElement).closest('[data-open]'); if (r) { var k = r.getAttribute('data-open').split(':'); openArticle(k[0], parseInt(k[1], 10)); } });

  // ---------------- Hồ sơ ----------------
  function renderSaved() {
    var list = H.get('aa.saved', []), h = [];
    if (!list.length) h.push('<div class="muted">' + esc(t('saved_empty')) + '</div>');
    for (var i = 0; i < list.length; i++) {
      var o = list[i];
      h.push('<div class="list-item"><div class="t">' + esc(o.name) + ' – ' + o.day + '/' + o.month + '/' + o.year + ' (' + esc(o.calendar === 'lunar' ? t('lunar') : t('solar')) + ')</div><div class="s">' + esc(o.summary || '') +
        '</div><a class="btn small" data-load="' + i + '">' + esc(t('btn_open')) + '</a><a class="btn small" data-del="' + i + '">' + esc(t('btn_delete')) + '</a></div>');
    }
    $('savedList').innerHTML = h.join('');
    function lst(arr, el) {
      var hh = [];
      for (var j = 0; j < arr.length; j++) hh.push('<div class="list-item" data-open="' + arr[j].ch + ':' + arr[j].i + '"><div class="t">' + esc(arr[j].t) + '</div><div class="s">' + esc(Lib.chapterName(arr[j].ch)) + '</div></div>');
      el.innerHTML = hh.join('') || '<div class="muted">—</div>';
    }
    lst(Lib.bookmarks(), $('bmList')); lst(Lib.recent(), $('recentList'));
  }
  on($('view-saved'), 'click', function (e) {
    var el = (e.target || e.srcElement), it, list = H.get('aa.saved', []);
    if ((it = el.closest('[data-load]'))) { setForm(list[+it.getAttribute('data-load')]); show('chart'); build(); return; }
    if ((it = el.closest('[data-del]'))) { list.splice(+it.getAttribute('data-del'), 1); H.set('aa.saved', list); renderSaved(); toast(t('deleted')); return; }
    if ((it = el.closest('[data-open]'))) { var k = it.getAttribute('data-open').split(':'); openArticle(k[0], parseInt(k[1], 10)); }
  });

  // ---------------- Kiểm thử tự động (?selftest=1) ----------------
  // Mỗi bước bấm vào phần tử giao diện thật, kiểm tra kết quả, rồi báo cho host chụp màn hình.
  function selftest() {
    var results = [], steps = [], DELAY = 2600;
    function click(id) { var el = $(id); if (el.click) el.click(); else { var ev = document.createEvent('MouseEvents'); ev.initEvent('click', true, true); el.dispatchEvent(ev); } }
    function navTo(v) { var a = document.querySelector('#nav a[data-view="' + v + '"]'); a.click(); }
    function step(name, fn) { steps.push({ name: name, fn: fn }); }
    function rec(name, ok, detail) { results.push({ step: name, ok: !!ok, detail: detail || '' }); H.log('SELFTEST ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ' ' + (detail || '')); }
    step('01_chart_build', function (done) {
      navTo('chart'); setForm({ name: 'Nguyễn Văn An', gender: 'Nam', calendar: 'solar', day: 15, month: 8, year: 1995, hourChi: 6 }); click('btnBuild');
      var cells = document.querySelectorAll('#chartBox .palace').length;
      rec('01_chart_build', cells === 12 && chart && chart.lunar.day === 20 && chart.lunar.month === 7, 'palaces=' + cells + ' lunar=' + (chart && chart.lunar.day + '/' + chart.lunar.month + '/' + chart.lunar.year) + ' cuc=' + (chart && chart.cucName));
      done();
    });
    step('02_palace_detail', function (done) {
      var p = document.querySelector('#chartBox .palace[data-chi="' + ((chart.menh + 4) % 12) + '"]'); p.click();
      rec('02_palace_detail', $('detailBox').innerHTML.indexOf('list-item') >= 0, 'related=' + document.querySelectorAll('#detailBox .list-item').length); done();
    });
    step('03_lunar_input', function (done) {
      setForm({ name: 'Trần Thị Bình', gender: 'Nữ', calendar: 'lunar', day: 1, month: 2, year: 2023, hourChi: 3, leap: true }); click('btnBuild');
      rec('03_lunar_input', chart && chart.solar.day === 22 && chart.solar.month === 3, 'solar=' + (chart && chart.solar.day + '/' + chart.solar.month + '/' + chart.solar.year)); done();
    });
    step('04_qr', function (done) { var q = showQr(); rec('04_qr', q && q.size >= 21, q ? 'size=' + q.size : ''); setTimeout(function () { closeModal(); done(); }, DELAY); }, true);
    step('05_png', function (done) { var u = exportPng(); rec('05_png', u && u.length > 50000, 'bytes=' + (u ? u.length : 0)); setTimeout(function () { closeModal(); done(); }, DELAY); });
    step('06_save_profile', function (done) { var n = H.get('aa.saved', []).length; click('btnSave'); navTo('saved'); rec('06_save_profile', H.get('aa.saved', []).length === n + 1, 'saved=' + H.get('aa.saved', []).length); done(); });
    step('07_library_home', function (done) { navTo('library'); renderLibraryHome(); rec('07_library_home', document.querySelectorAll('#libBody .list-item').length === 13, 'chapters=13 articles=' + Lib.totalArticles()); done(); });
    step('08_library_chapter', function (done) { document.querySelector('#libBody [data-ch="an-sao"]').click(); rec('08_library_chapter', document.querySelectorAll('#libBody [data-open]').length > 5); done(); });
    step('09_reader', function (done) { openArticle('chinh-tinh', 0); setTimeout(function () { var b = $('rBody'); rec('09_reader', b && b.textContent.length > 1000, 'chars=' + (b ? b.textContent.length : 0)); if ($('rBm')) $('rBm').click(); done(); }, 900); });
    step('10_search', function (done) { $('libQ').value = 'Hóa Kỵ'; runSearch('Hóa Kỵ', function (r) { rec('10_search', r.length > 20, 'results=' + r.length + ' native=' + H.isAndroid); done(); }); });
    step('11_search_noaccent', function (done) { runSearch('thien phu', function (r) { rec('11_search_noaccent', r.length > 20, 'results=' + r.length); done(); }); });
    step('12_assistant_overview', function (done) { navTo('assistant'); ask('__overview', function (txt) { rec('12_assistant_overview', txt.length > 200, 'chars=' + txt.length); done(); }); });
    step('13_assistant_question', function (done) { ask('Công danh sự nghiệp của tôi thế nào?', function (txt) { rec('13_assistant_question', txt.indexOf('Quan Lộc') >= 0, 'chars=' + txt.length); done(); }); });
    step('14_assistant_year', function (done) { ask('Hạn năm nay ra sao?', function (txt) { rec('14_assistant_year', txt.indexOf(String(chart.viewYear)) >= 0); done(); }); });
    step('15_settings_theme', function (done) { navTo('settings'); $('sTheme').value = 'ivory-light'; fire($('sTheme'), 'change'); rec('15_settings_theme', S.theme === 'ivory-light'); done(); });
    step('16_settings_language_en', function (done) { $('sLang').value = 'en'; S.lang = 'en'; saveSettings(); I.setLang('en', function () { rec('16_settings_language_en', document.querySelector('#nav a[data-view="settings"] [data-i18n]').textContent === 'Settings'); done(); }); });
    step('17_settings_language_ja', function (done) { I.setLang('ja', function () { var txt = document.querySelector('#nav a[data-view="library"] [data-i18n]').textContent; rec('17_settings_language_ja', txt !== 'Library' && txt !== 'Thư Viện', txt); done(); }); });
    step('18_settings_font_layout', function (done) { $('sFont').value = 'large'; fire($('sFont'), 'change'); $('sLayout').value = 'list'; fire($('sLayout'), 'change'); rec('18_settings_font_layout', S.font === 'large' && S.layout === 'list'); done(); });
    step('19_chart_list_layout', function (done) { navTo('chart'); rec('19_chart_list_layout', document.querySelectorAll('#chartBox .palace-list').length === 1); done(); });
    step('20_desktop_dex', function (done) { navTo('settings'); $('sDesktop').checked = true; fire($('sDesktop'), 'change'); navTo('chart'); $('sLayout').value = 'board'; S.layout = 'board'; renderChart(); rec('20_desktop_dex', document.body.className.indexOf('desktop') >= 0); done(); });
    step('21_backup_export', function (done) { navTo('settings'); var d = exportBackup(); rec('21_backup_export', d && d.saved.length > 0); done(); });
    step('22_backup_import', function (done) { var ok = importBackup(JSON.stringify({ app: 'Applied Astrology', settings: Object.assign({}, S, { theme: 'royal-gold' }) })); rec('22_backup_import', ok && S.theme === 'royal-gold'); done(); });
    step('23_network_check', function (done) { click('sNet'); setTimeout(function () { var txt = $('netOut').textContent; rec('23_network_check', txt.length > 5 && txt !== t('net_checking'), txt.substring(0, 140)); done(); }, 7000); });
    step('24_restore_defaults', function (done) { click('sReset'); I.setLang('vi', function () { navTo('chart'); setForm({ name: 'Nguyễn Văn An', gender: 'Nam', calendar: 'solar', day: 15, month: 8, year: 1995, hourChi: 6 }); build(); rec('24_restore_defaults', S.theme === 'celestial-dark' && S.lang === 'vi'); done(); }); });
    function fire(el, type) { var ev = document.createEvent('HTMLEvents'); ev.initEvent(type, true, true); el.dispatchEvent(ev); }
    var idx = 0;
    function next() {
      if (idx >= steps.length) {
        var pass = 0; for (var i = 0; i < results.length; i++) if (results[i].ok) pass++;
        var summary = { app: 'Applied Astrology', version: VERSION, platform: H.info(), pass: pass, total: results.length, results: results };
        H.testStep('99_done', JSON.stringify(summary));
        root.AA_SELFTEST_RESULT = summary;
        var rows = [];
        for (var j = 0; j < results.length; j++) rows.push((results[j].ok ? '✔ ' : '✘ ') + results[j].step + '  ' + results[j].detail);
        modal('<h3>SELFTEST ' + pass + '/' + results.length + '</h3><pre style="text-align:left;white-space:pre-wrap;font-size:12px">' + esc(rows.join('\n')) + '</pre>');
        return;
      }
      var s = steps[idx++];
      try {
        s.fn(function () { setTimeout(function () { H.testStep(s.name, JSON.stringify(results[results.length - 1] || {})); setTimeout(next, DELAY); }, 500); });
      } catch (err) { rec(s.name, false, 'EXCEPTION ' + err.message); setTimeout(next, DELAY); }
    }
    setTimeout(next, 1500);
  }

  // ---------------- Khởi động ----------------
  function init() {
    fillForm(); fillSettingsUi(); bindSettings(); applySettings(false);
    I.setLang(S.lang, function () {
      var q = root.location ? (root.location.search + '&' + String(root.location.hash).replace(/^#/, '')) : '';
      var m = /[?&]c=([^&]+)/.exec(q);
      if (m) { var o = parseCode(decodeURIComponent(m[1])); if (o) setForm(o); }
      build();
      var tab = /[?&]tab=(\w+)/.exec(q); if (tab) show(tab[1]);
      if (/[?&]selftest=1/.test(q) || root.AA_FORCE_SELFTEST) selftest();
    });
    on(root, 'resize', function () {
      if (S.desktop === null) applySettings(false);
      if (chart && S.layout === 'auto') { var want = (root.innerWidth || 1000) < 560 && document.body.className.indexOf('desktop') < 0 ? 'list' : 'board'; if (want !== lastLayout) renderChart(); }
    });
  }
  root.AAApp = { build: build, show: show, openArticle: openArticle, search: runSearch, ask: ask, selftest: selftest, exportBackup: exportBackup, importBackup: importBackup, version: VERSION, chart: function () { return chart; } };
  init();
})(window);
