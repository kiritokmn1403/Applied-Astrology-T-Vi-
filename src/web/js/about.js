/*
 * Applied Astrology v0.0.3 - Mục Giới Thiệu (theo bố cục About của Aniyomi / Mangayomi / Nekoyomi):
 * phiên bản, kiểm tra cập nhật (GitHub Releases) -> tự tải và cài, có gì mới, nhật ký ứng dụng, báo cáo lỗi,
 * thông tin gỡ lỗi, giấy phép mã nguồn mở, liên kết.
 */
(function (root) {
  'use strict';
  var H = root.AAHost;
  var OWNER = 'kiritokmn1403', REPO = 'Applied-Astrology-T-Vi-';
  var REPO_URL = 'https://github.com/' + OWNER + '/' + REPO;
  var RELEASES_URL = REPO_URL + '/releases';
  var API_LATEST = 'https://api.github.com/repos/' + OWNER + '/' + REPO + '/releases/latest';
  var BUILD_DATE = '2026-11-03';
  function t(k, v) { return root.t(k, v); }
  function esc(s) { return String(s === undefined || s === null ? '' : s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;'); }
  function $(id) { return document.getElementById(id); }

  /** So sánh phiên bản dạng 0.0.3 / v0.0.10 -> -1, 0, 1 */
  function cmpVer(a, b) {
    var x = String(a || '').replace(/^v/i, '').split(/[.\-]/), y = String(b || '').replace(/^v/i, '').split(/[.\-]/);
    for (var i = 0; i < Math.max(x.length, y.length); i++) {
      var p = parseInt(x[i] || '0', 10) || 0, q = parseInt(y[i] || '0', 10) || 0;
      if (p !== q) return p < q ? -1 : 1;
    }
    return 0;
  }

  /** Chọn tệp phát hành phù hợp với thiết bị đang chạy */
  function pickAsset(assets, info) {
    var names = [], i;
    for (i = 0; i < assets.length; i++) names.push(assets[i].name);
    function find(re) { for (var j = 0; j < assets.length; j++) if (re.test(assets[j].name)) return assets[j]; return null; }
    if (H.isAndroid) {
      var abi = String(info.abi || '').toLowerCase(), order = [];
      if (abi.indexOf('arm64') >= 0) order = ['arm64-v8a', 'armeabi-v7a'];
      else if (abi.indexOf('x86_64') >= 0) order = ['x86_64'];
      else if (abi.indexOf('armeabi') >= 0) order = ['armeabi-v7a'];
      else order = ['arm64-v8a', 'armeabi-v7a', 'x86_64'];
      for (i = 0; i < order.length; i++) { var a = find(new RegExp('-' + order[i].replace(/[-_]/g, '[-_]') + '\\.apk$', 'i')); if (a) return a; }
      return null;
    }
    if (H.isWindows) {
      var inst = info.install || 'setup', arch = String(info.arch || 'x64').toLowerCase();
      if (inst === 'portable') return find(/windows-portable\.zip$/i);
      if (inst === 'appx') return find(/\.appxbundle$/i);
      if (inst === 'msi') return find(new RegExp('windows-' + arch + '\\.msi$', 'i')) || find(/Setup\.exe$/i);
      return find(/Setup\.exe$/i);
    }
    return null;
  }

  var upd = { state: 'idle', release: null, asset: null, path: null, timer: null };

  function checkUpdate(silent, cb) {
    var out = $('abUpdOut');
    if (out && !silent) out.innerHTML = esc(t('ab_checking'));
    H.log('update check ' + API_LATEST);
    H.httpGet(API_LATEST, function (r) {
      var rel = null;
      try { rel = r && r.ok ? JSON.parse(r.body) : null; } catch (e) { rel = null; }
      if (!rel || !rel.tag_name) {
        H.log('update check failed status=' + (r && r.status));
        if (out && !silent) out.innerHTML = '<span class="status-bad">' + esc(t('ab_update_fail')) + '</span> <a class="btn small" data-url="' + RELEASES_URL + '">' + esc(t('ab_releases')) + '</a>';
        if (cb) cb({ ok: false, status: r && r.status });
        return;
      }
      var latest = String(rel.tag_name).replace(/^v/i, ''), cur = root.AAApp ? root.AAApp.version : '0';
      var info = H.info(), asset = pickAsset(rel.assets || [], info), newer = cmpVer(latest, cur) > 0;
      upd.release = rel; upd.asset = asset;
      H.set('aa.lastUpdateCheck', { at: Date.now(), latest: latest });
      H.log('update latest=' + latest + ' current=' + cur + ' newer=' + newer + ' asset=' + (asset ? asset.name : '-'));
      if (out) {
        if (!newer) out.innerHTML = '<span class="status-ok">' + esc(t('ab_latest', { v: cur })) + '</span>' + (silent ? '' : notesHtml(rel, true));
        else showNew(rel, asset);
      }
      if (newer && silent && root.AAApp && root.AAApp.toast) root.AAApp.toast(t('ab_new', { v: latest }));
      if (cb) cb({ ok: true, latest: latest, current: cur, newer: newer, asset: asset ? asset.name : null, assets: (rel.assets || []).length });
    });
  }
  function notesHtml(rel, collapsed) {
    var body = String(rel.body || '').replace(/\r/g, '').replace(/[#*`>]/g, '').replace(/\n{3,}/g, '\n\n');
    if (body.length > 3000) body = body.substring(0, 3000) + '…';
    return '<details' + (collapsed ? '' : ' open') + '><summary>' + esc(t('ab_whats_new')) + ' – ' + esc(rel.name || rel.tag_name) + '</summary><div class="notes">' + esc(body) + '</div></details>';
  }
  function showNew(rel, asset) {
    var out = $('abUpdOut'); if (!out) return;
    var h = '<b class="status-ok">' + esc(t('ab_new', { v: String(rel.tag_name).replace(/^v/i, '') })) + '</b>' + notesHtml(rel, false);
    if (asset && H.kind !== 'browser') h += '<div class="btn-row"><a class="btn primary" id="abDl">' + esc(t('ab_download')) + '</a> <span class="hint">' + esc(asset.name) + ' · ' + (asset.size / 1048576).toFixed(1) + ' MB</span></div><div class="progress hidden" id="abProg"><div id="abBar"></div></div><div class="hint" id="abDlState"></div>';
    else h += '<div class="hint">' + esc(t('ab_no_asset')) + '</div>';
    h += '<a class="btn small" data-url="' + esc(rel.html_url || RELEASES_URL) + '">' + esc(t('ab_releases')) + '</a>';
    out.innerHTML = h;
  }
  function startDownload() {
    if (!upd.asset) return false;
    var r = H.download(upd.asset.browser_download_url, upd.asset.name);
    H.log('update download ' + upd.asset.name + ' -> ' + r);
    if (!r) { setDl(t('ab_dl_fail'), true); return false; }
    $('abProg').className = 'progress'; upd.state = 'running';
    clearInterval(upd.timer);
    upd.timer = setInterval(poll, 500);
    return true;
  }
  function setDl(msg, bad) { var el = $('abDlState'); if (el) el.innerHTML = bad ? '<span class="status-bad">' + esc(msg) + '</span>' : esc(msg); }
  function poll() {
    var s = H.downloadStatus(); if (!s) return;
    var pct = s.total > 0 ? Math.floor(s.got * 100 / s.total) : 0, bar = $('abBar');
    if (bar) bar.style.width = pct + '%';
    if (s.state === 'running') { setDl(t('ab_downloading', { p: pct }) + ' (' + (s.got / 1048576).toFixed(1) + ' MB)'); return; }
    clearInterval(upd.timer);
    if (s.state === 'done') {
      upd.state = 'done'; upd.path = s.path;
      if (bar) bar.style.width = '100%';
      var el = $('abDlState');
      if (el) el.innerHTML = esc(t('ab_dl_done')) + '<br><a class="btn primary" id="abInst">' + esc(t('ab_install')) + '</a><div class="hint">' + esc(H.isAndroid ? t('ab_install_note_android') : t('ab_install_note_win')) + '</div>';
      H.log('update downloaded ' + s.path + ' ' + s.got + ' bytes');
      if (root.AAApp.settings().autoInstall !== false && !root.AA_SELFTEST_RUNNING) install();
    } else { upd.state = 'error'; setDl(t('ab_dl_fail') + ': ' + (s.error || ''), true); H.log('update download error ' + s.error); }
  }
  function install() {
    if (!upd.path) return null;
    var r = H.installUpdate(upd.path);
    H.log('update install -> ' + r);
    if (r === 'need_permission') setDl(t('ab_need_perm'), true);
    else if (!r) setDl(t('ab_install_fail'), true);
    return r;
  }

  // ---------------- Nhật ký & báo cáo lỗi ----------------
  function debugInfo() {
    var i = H.info(), s = [];
    s.push('App: Applied Astrology ' + (root.AAApp ? root.AAApp.version : '') + ' (build ' + BUILD_DATE + ')');
    s.push('Platform: ' + H.kind + (i.install ? ' / ' + i.install : ''));
    if (i.os) s.push('OS: ' + i.os);
    if (i.device) s.push('Device: ' + i.device);
    if (i.abi) s.push('ABI: ' + i.abi);
    if (i.engine) s.push('Engine: ' + i.engine);
    s.push('Language: ' + root.AAI18n.current() + ' · Screen: ' + (root.screen ? screen.width + 'x' + screen.height : '?') + ' @' + (root.devicePixelRatio || 1) + ' · Viewport ' + (root.innerWidth || 0) + 'x' + (root.innerHeight || 0));
    s.push('UA: ' + (i.ua || ''));
    return s.join('\n');
  }
  function errorList() {
    var list = H.get('aa.errors', []) || [], nat = H.crashList();
    return { js: list, native: nat || [] };
  }
  function renderErrors() {
    var el = $('abErrList'); if (!el) return;
    var e = errorList(), h = [];
    for (var i = 0; i < e.native.length; i++) h.push('<div class="list-item"><div class="t">⚠ ' + esc(e.native[i].name) + '</div><pre class="log">' + esc(String(e.native[i].text).substring(0, 4000)) + '</pre></div>');
    for (i = e.js.length - 1; i >= 0 && i >= e.js.length - 30; i--) h.push('<div class="list-item"><div class="t">JS · ' + esc(new Date(e.js[i].at).toLocaleString()) + '</div><div class="s">' + esc(e.js[i].m) + '</div></div>');
    el.innerHTML = h.join('') || '<div class="muted">' + esc(t('ab_errors_none')) + '</div>';
  }
  function logText() {
    var nat = H.readLog() || '', js = (H.get('aa.log', []) || []).join('\n');
    return '=== ' + debugInfo().split('\n')[0] + ' ===\n' + debugInfo() + '\n\n=== JS log ===\n' + js + (nat ? '\n\n=== Native log ===\n' + nat : '');
  }
  function reportUrl() {
    var e = errorList(), last = [];
    for (var i = Math.max(0, e.js.length - 5); i < e.js.length; i++) last.push(e.js[i].m);
    if (e.native.length) last.push('native: ' + String(e.native[0].text).substring(0, 600));
    var body = '### Mô tả lỗi / Description\n\n\n### Các bước tái hiện / Steps\n1. \n\n### Thông tin thiết bị / Device info\n```\n' + debugInfo() + '\n```\n' + (last.length ? '\n### Lỗi gần đây / Recent errors\n```\n' + last.join('\n') + '\n```\n' : '');
    return REPO_URL + '/issues/new?title=' + encodeURIComponent('[Bug] ') + '&body=' + encodeURIComponent(body.substring(0, 6000));
  }

  var LICENSES = [
    ['Applied Astrology', 'GPL-3.0', REPO_URL],
    ['Âm lịch Việt Nam – thuật toán Hồ Ngọc Đức', 'Free for non-commercial use', 'https://www.informatik.uni-leipzig.de/~duc/amlich/'],
    ['hochanh/amlich, copthuy/amlich (tham khảo dữ liệu giờ hoàng đạo, tiết khí, ngày lễ)', 'MIT', 'https://github.com/copthuy/amlich'],
    ['QR Code generator – Project Nayuki', 'MIT', 'https://github.com/nayuki/QR-Code-generator'],
    ['Microsoft Edge WebView2 SDK', 'BSD-3-Clause (Microsoft)', 'https://www.nuget.org/packages/Microsoft.Web.WebView2'],
    ['Aniyomi / Nekoyomi (tham khảo bố cục Giới thiệu, danh sách ngôn ngữ)', 'Apache-2.0', 'https://github.com/aniyomiorg/aniyomi'],
    ['Mangayomi (tham khảo kiểm tra cập nhật, nhật ký, báo cáo lỗi)', 'Apache-2.0', 'https://github.com/kodjodevf/mangayomi'],
    ['WiX Toolset 3.14 (chỉ dùng khi build MSI)', 'MS-RL', 'https://github.com/wixtoolset/wix3'],
    ['Nội dung sách "Tử Vi Ứng Dụng" – Bửu Đình (Tử Vũ)', '© tác giả', '']
  ];

  function render(el) {
    var i = H.info(), S = root.AAApp.settings(), ver = root.AAApp ? root.AAApp.version : '';
    var lc = H.get('aa.lastUpdateCheck', null);
    var h = [];
    h.push('<div class="card about-head"><svg class="aa-logo big" viewBox="0 0 100 100" aria-hidden="true"><circle cx="50" cy="50" r="48" fill="#120a2a" stroke="#f2b84b" stroke-width="2"/><polygon points="50,8 56,40 88,50 56,60 50,92 44,60 12,50 44,40" fill="#f2b84b" opacity=".9"/><circle cx="50" cy="50" r="13" fill="#0a0518" stroke="#ffe7a3" stroke-width="2"/><path d="M50 37 A13 13 0 0 1 50 63 A6.5 6.5 0 0 1 50 50 A6.5 6.5 0 0 0 50 37 Z" fill="#ffe7a3"/></svg>',
      '<div><div class="aa-title">' + esc(t('app_name')) + '</div><div class="hint">Applied Astrology · ' + esc(t('s_version')) + ' <b>' + esc(ver) + '</b> (' + BUILD_DATE + ')</div><div class="hint">' + esc(t('s_about_d')) + '</div></div></div>');
    h.push('<div class="setting"><h3>' + esc(t('ab_updates')) + '</h3>',
      '<label class="switch"><input type="checkbox" id="abAuto"' + (S.autoUpdate !== false ? ' checked' : '') + '><span class="knob"></span></label><span class="hint">' + esc(t('ab_auto_check')) + '</span><br>',
      '<label class="switch"><input type="checkbox" id="abAutoInst"' + (S.autoInstall !== false ? ' checked' : '') + '><span class="knob"></span></label><span class="hint">' + esc(t('ab_auto_install')) + '</span>',
      '<div class="btn-row"><a class="btn primary" id="abCheck">' + esc(t('ab_check')) + '</a><a class="btn" data-url="' + RELEASES_URL + '">' + esc(t('ab_releases')) + '</a></div>',
      (lc ? '<div class="hint">' + esc(t('ab_last_check')) + ': ' + esc(new Date(lc.at).toLocaleString()) + ' (' + esc(lc.latest) + ')</div>' : ''),
      '<div id="abUpdOut" class="cal-out"></div></div>');
    var cl = root.AA_CHANGELOG || [], lang = root.AAI18n.current() === 'vi' ? 'vi' : 'en';
    h.push('<div class="setting"><h3>' + esc(t('ab_whats_new')) + '</h3>');
    for (var k = 0; k < cl.length; k++) h.push('<details' + (k === 0 ? ' open' : '') + '><summary><b>' + esc(cl[k].v) + '</b> · ' + esc(cl[k].d) + '</summary><ul>' + listHtml(cl[k][lang] || cl[k].en) + '</ul></details>');
    h.push('</div>');
    h.push('<div class="setting"><h3>' + esc(t('ab_diag')) + '</h3>',
      '<label class="switch"><input type="checkbox" id="abLogOn"' + (S.logs !== false ? ' checked' : '') + '><span class="knob"></span></label><span class="hint">' + esc(t('ab_logs_on')) + '</span>',
      '<div class="btn-row"><a class="btn" id="abLogView">' + esc(t('ab_logs_view')) + '</a><a class="btn" id="abLogExport">' + esc(t('ab_logs_share')) + '</a><a class="btn" id="abLogClear">' + esc(t('ab_logs_clear')) + '</a></div>',
      '<h3 style="margin-top:10px">' + esc(t('ab_errors')) + '</h3><div class="hint">' + esc(t('ab_errors_d')) + '</div><div id="abErrList" style="margin-top:6px"></div>',
      '<div class="btn-row"><a class="btn primary" id="abReport">' + esc(t('ab_report')) + '</a><a class="btn" id="abCopy">' + esc(t('ab_copy_debug')) + '</a><a class="btn" id="abErrClear">' + esc(t('ab_errors_clear')) + '</a></div><div class="hint">' + esc(t('ab_report_d')) + '</div>',
      '<pre class="log" id="abDebug">' + esc(debugInfo()) + '</pre></div>');
    h.push('<div class="setting"><h3>' + esc(t('ab_licenses')) + '</h3>');
    for (k = 0; k < LICENSES.length; k++) h.push('<div class="list-item"' + (LICENSES[k][2] ? ' data-url="' + esc(LICENSES[k][2]) + '"' : '') + '><div class="t">' + esc(LICENSES[k][0]) + '</div><div class="s">' + esc(LICENSES[k][1]) + '</div></div>');
    h.push('</div><div class="setting"><h3>' + esc(t('ab_links')) + '</h3><a class="btn" data-url="' + REPO_URL + '">GitHub</a><a class="btn" data-url="' + RELEASES_URL + '">Releases</a><a class="btn" data-url="' + REPO_URL + '/issues">Issues</a></div>');
    el.innerHTML = h.join('');
    renderErrors();
    if (upd.release && upd.state !== 'idle') showNew(upd.release, upd.asset);
  }
  function listHtml(arr) { var h = []; for (var i = 0; i < (arr || []).length; i++) h.push('<li>' + esc(arr[i]) + '</li>'); return h.join(''); }

  function saveFlag(k, v) { var S = root.AAApp.settings(); S[k] = v; root.AAApp.saveSettings(); H.log('setting ' + k + '=' + v); }
  function bind(el) {
    function handler(e) {
      var x = e.target || e.srcElement, it;
      if ((it = x.closest('[data-url]'))) { H.openUrl(it.getAttribute('data-url')); return; }
      switch (x.id) {
        case 'abCheck': checkUpdate(false); break;
        case 'abDl': startDownload(); break;
        case 'abInst': install(); break;
        case 'abLogView': root.AAApp.modal('<h3>' + esc(t('ab_logs')) + '</h3><pre class="log" style="text-align:left;max-height:60vh">' + esc(logText().slice(-20000)) + '</pre>'); break;
        case 'abLogExport': { var txt = logText(), r = H.saveFile('AppliedAstrology-log-' + new Date().getTime() + '.txt', txt); if (!r) H.share(txt); root.AAApp.toast(r ? r : t('ab_copied')); break; }
        case 'abLogClear': H.set('aa.log', []); H.clearLog(); root.AAApp.toast(t('deleted')); break;
        case 'abReport': H.openUrl(reportUrl()); break;
        case 'abCopy': H.copy(debugInfo()); root.AAApp.toast(t('ab_copied')); break;
        case 'abErrClear': H.set('aa.errors', []); H.clearCrash(); renderErrors(); root.AAApp.toast(t('deleted')); break;
      }
    }
    function change(e) {
      var x = e.target || e.srcElement;
      if (x.id === 'abAuto') saveFlag('autoUpdate', x.checked);
      if (x.id === 'abAutoInst') saveFlag('autoInstall', x.checked);
      if (x.id === 'abLogOn') saveFlag('logs', x.checked);
    }
    if (el.addEventListener) { el.addEventListener('click', handler, false); el.addEventListener('change', change, false); }
    else { el.attachEvent('onclick', handler); el.attachEvent('onchange', change); }
  }

  root.AAAbout = { render: render, bind: bind, checkUpdate: checkUpdate, cmpVer: cmpVer, pickAsset: pickAsset, debugInfo: debugInfo, logText: logText, reportUrl: reportUrl,
    renderErrors: renderErrors, REPO_URL: REPO_URL, API_LATEST: API_LATEST, upd: upd };
})(window);
