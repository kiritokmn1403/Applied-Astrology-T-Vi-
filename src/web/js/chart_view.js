/*
 * Applied Astrology v0.0.2 - Hiển thị lá số (bàn 4x4 / danh sách), chi tiết cung, xuất ảnh PNG
 */
(function (root) {
  'use strict';
  var E = root.AAEngine, T = root.AATheme;
  var LAYOUT = [[5, 6, 7, 8], [4, -1, -1, 9], [3, -1, -1, 10], [2, 1, 0, 11]];
  function esc(s) { return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;'); }
  function t(k, v) { return root.t(k, v); }
  function hoaClass(n) { return { 'Hóa Lộc': 'hoa-loc', 'Hóa Quyền': 'hoa-quyen', 'Hóa Khoa': 'hoa-khoa', 'Hóa Kỵ': 'hoa-ky' }[n] || ''; }

  function starLine(s, cls) {
    if (s.attachedTo) return '<div class="' + hoaClass(s.name) + '">' + esc(s.name) + '</div>';
    return '<div class="' + cls + '">' + esc(s.name) + '</div>';
  }

  function palaceHtml(chart, chi, opts) {
    var p = chart.palaces[chi], h = [], k;
    var cls = 'palace' + (opts.sel === chi ? ' sel' : '') + (opts.tp && opts.tp.indexOf(chi) > 0 ? ' tp' : '') +
      (chart.currentDaiHan === chi ? ' cur-dh' : '') + (chart.currentTieuHan === chi ? ' cur-th' : '');
    h.push('<div class="' + cls + '" data-chi="' + chi + '" id="pal-' + chi + '">');
    h.push('<div class="p-head"><span class="p-name">' + esc(root.AAI18n.palace(p.name)) +
      (p.isMenh ? '<span class="badge">' + esc(t('menh')) + '</span>' : '') +
      (p.isThan ? '<span class="badge than">' + esc(t('than')) + '</span>' : '') +
      (p.tuan ? '<span class="badge tt">' + esc(t('tuan')) + '</span>' : '') +
      (p.triet ? '<span class="badge tt">' + esc(t('triet')) + '</span>' : '') +
      '</span><span class="p-canchi">' + esc(p.canChi) + '</span></div>');
    if (p.major.length) {
      for (k = 0; k < p.major.length; k++) {
        var m = p.major[k];
        h.push('<div class="p-major ' + T.eleClass(m.element) + '">' + esc(m.name) + ' <span class="br">(' + m.bright + ')</span></div>');
      }
    } else h.push('<div class="p-major muted">' + esc(t('vcd')) + '</div>');
    var good = [], bad = [];
    for (k = 0; k < p.good.length; k++) good.push(starLine(p.good[k], 's-good'));
    for (k = 0; k < p.neutral.length; k++) good.push(starLine(p.neutral[k], 's-neutral'));
    for (k = 0; k < p.bad.length; k++) bad.push(starLine(p.bad[k], p.bad[k].attachedTo ? '' : 's-bad'));
    if (opts.luu) for (k = 0; k < p.luu.length; k++) bad.push('<div class="s-luu">' + esc(p.luu[k]) + '</div>');
    h.push('<div class="p-minor"><div class="col">' + good.join('') + '</div><div class="col">' + bad.join('') + '</div></div>');
    var ring = [];
    if (opts.trangSinh && p.ring.trangSinh) ring.push(p.ring.trangSinh);
    if (opts.bacSi && p.ring.bacSi) ring.push(p.ring.bacSi);
    if (opts.thaiTue && p.ring.thaiTue) ring.push(p.ring.thaiTue);
    if (ring.length) h.push('<div class="p-ring">' + esc(ring.join(' · ')) + '</div>');
    h.push('<div class="p-foot"><span class="p-dh">' + esc(t('dai_han')) + ' ' + p.daiHan + '</span><span class="p-th">' + esc(p.tieuHan) + '</span></div>');
    h.push('</div>');
    return h.join('');
  }

  function centerHtml(chart) {
    var lu = chart.lunar, so = chart.solar;
    function row(k, v) { return '<div class="tb-row">' + esc(t(k)) + ': <b>' + esc(v) + '</b></div>'; }
    return '<div class="thien-ban">' +
      '<svg class="tb-logo" viewBox="0 0 100 100"><circle cx="50" cy="50" r="47" fill="none" stroke="#f2b84b" stroke-width="2" stroke-dasharray="5 4"/>' +
      '<path d="M50 6 A44 44 0 0 1 50 94 A22 22 0 0 1 50 50 A22 22 0 0 0 50 6 Z" fill="#f2b84b"/>' +
      '<path d="M50 94 A44 44 0 0 1 50 6 A22 22 0 0 1 50 50 A22 22 0 0 0 50 94 Z" fill="#3b2a6b"/>' +
      '<circle cx="50" cy="28" r="6" fill="#3b2a6b"/><circle cx="50" cy="72" r="6" fill="#f2b84b"/></svg>' +
      '<div class="tb-name">' + esc(chart.name) + '</div>' +
      row('solar_date', so.day + '/' + so.month + '/' + so.year) +
      row('lunar_date', lu.day + '/' + lu.month + (lu.leap ? ' (N)' : '') + '/' + lu.year) +
      row('year_cc', chart.yearCanChi) + row('month_cc', chart.monthCanChi) + row('day_cc', chart.dayCanChi) + row('hour_cc', chart.hourCanChi) +
      row('am_duong', chart.amDuong + ' · ' + chart.amDuongLy) + row('ban_menh', chart.napAm) + row('cuc', chart.cucName) +
      row('menh_cuc', chart.menhCuc) + row('menh_chu', chart.menhChu) + row('than_chu', chart.thanChu) +
      row('f_viewyear', chart.viewYear + ' (' + chart.viewYearCanChi + ') · ' + t('age') + ' ' + chart.age) +
      '</div>';
  }

  function render(container, chart, opts) {
    opts = opts || {};
    var layout = opts.layout || 'auto';
    if (layout === 'auto') layout = (root.innerWidth || 1000) < 560 && !opts.desktop ? 'list' : 'board';
    var html = [];
    if (layout === 'board') {
      html.push('<div class="board-scroll"><table class="board" id="board"><colgroup><col><col><col><col></colgroup>');
      for (var r = 0; r < 4; r++) {
        html.push('<tr>');
        for (var c = 0; c < 4; c++) {
          var chi = LAYOUT[r][c];
          if (chi === -1) {
            if (r === 1 && c === 1) html.push('<td colspan="2" rowspan="2">' + centerHtml(chart) + '</td>');
            continue;
          }
          html.push('<td>' + palaceHtml(chart, chi, opts) + '</td>');
        }
        html.push('</tr>');
      }
      html.push('</table></div>');
    } else {
      html.push('<div class="palace-list">' + centerHtml(chart));
      var order = [];
      for (var i = 0; i < 12; i++) order.push((chart.menh + i) % 12);
      for (i = 0; i < 12; i++) html.push(palaceHtml(chart, order[i], opts));
      html.push('</div>');
    }
    container.innerHTML = html.join('');
    return layout;
  }

  function detailHtml(chart, chi, related) {
    var p = chart.palaces[chi], h = [], k;
    h.push('<h2>' + esc(root.AAI18n.palace(p.name)) + ' · ' + esc(p.canChi) + ' (' + esc(p.element) + ')</h2>');
    h.push('<div class="hint">' + esc(t('dai_han')) + ' ' + p.daiHan + '–' + (p.daiHan + 9) + ' · ' + esc(t('tieu_han')) + ' ' + esc(p.tieuHan) + '</div>');
    h.push('<div class="detail-stars">');
    h.push('<div class="grp">' + esc(t('major_stars')) + '</div>');
    if (p.major.length) for (k = 0; k < p.major.length; k++) h.push('<div class="' + T.eleClass(p.major[k].element) + '"><b>' + esc(p.major[k].name) + '</b> – ' + esc(p.major[k].bright) + ' · ' + esc(p.major[k].element) + '</div>');
    else h.push('<div class="muted">' + esc(t('vcd')) + '</div>');
    function grp(key, list, cls) {
      if (!list.length) return;
      var names = [];
      for (var i = 0; i < list.length; i++) names.push('<span class="' + (list[i].attachedTo ? hoaClass(list[i].name) : cls) + '">' + esc(list[i].name) + (list[i].attachedTo ? ' (' + esc(list[i].attachedTo) + ')' : '') + '</span>');
      h.push('<div class="grp">' + esc(t(key)) + '</div><div>' + names.join(', ') + '</div>');
    }
    grp('good_stars', p.good, 's-good'); grp('bad_stars', p.bad, 's-bad'); grp('other_stars', p.neutral, 's-neutral');
    if (p.luu.length) h.push('<div class="grp">' + esc(t('luu_stars')) + ' ' + chart.viewYear + '</div><div class="s-luu">' + esc(p.luu.join(', ')) + '</div>');
    var rg = []; if (p.ring.trangSinh) rg.push(p.ring.trangSinh); if (p.ring.bacSi) rg.push(p.ring.bacSi); if (p.ring.thaiTue) rg.push(p.ring.thaiTue);
    h.push('<div class="grp">' + esc(t('rings')) + '</div><div>' + esc(rg.join(' · ')) + '</div>');
    var tp = chart.tamPhuong(chi), tpn = [];
    for (k = 1; k < 4; k++) {
      var q = chart.palaces[tp[k]], ms = [];
      for (var j = 0; j < q.major.length; j++) ms.push(q.major[j].name);
      tpn.push(root.AAI18n.palace(q.name) + ' (' + q.chiName + '): ' + (ms.join(', ') || t('vcd')));
    }
    h.push('<div class="grp">' + esc(t('tam_phuong')) + '</div><div>' + esc(tpn.join(' | ')) + '</div>');
    h.push('</div><h3>' + esc(t('related')) + '</h3>');
    if (!related.length) h.push('<div class="muted">' + esc(t('no_related')) + '</div>');
    for (k = 0; k < related.length; k++) {
      h.push('<div class="list-item" data-open="' + related[k].ch + ':' + related[k].i + '"><div class="t">' + esc(related[k].title) +
        '</div><div class="s">' + esc(root.AALib.chapterName(related[k].ch)) + ' · ' + esc(related[k].why) + '</div></div>');
    }
    return h.join('');
  }

  /** Vẽ lá số lên canvas (để xuất PNG) – độc lập với DOM nên chạy được cả trên IE11 */
  function toCanvas(chart, themeName, opts) {
    opts = opts || {};
    var pal = T.palettes[themeName] || T.palettes['celestial-dark'];
    var W = 1400, H = 1400, cw = W / 4, ch = H / 4, pad = 10;
    var cv = document.createElement('canvas'); cv.width = W; cv.height = H + 60;
    var g = cv.getContext('2d');
    g.fillStyle = pal.bg; g.fillRect(0, 0, W, H + 60);
    g.textBaseline = 'top';
    function txt(s, x, y, color, size, bold, align) {
      g.fillStyle = color; g.font = (bold ? 'bold ' : '') + size + 'px "Segoe UI", Roboto, Arial, sans-serif';
      g.textAlign = align || 'left'; g.fillText(s, x, y);
    }
    for (var r = 0; r < 4; r++) for (var c = 0; c < 4; c++) {
      var chi = LAYOUT[r][c];
      if (chi === -1) continue;
      var x = c * cw, y = r * ch, p = chart.palaces[chi];
      g.fillStyle = p.isMenh ? pal.sel : pal.card; g.fillRect(x + 3, y + 3, cw - 6, ch - 6);
      g.strokeStyle = p.isMenh ? pal.gold : pal.line; g.lineWidth = 2; g.strokeRect(x + 3, y + 3, cw - 6, ch - 6);
      txt(root.AAI18n.palace(p.name).toUpperCase() + (p.isThan ? ' (' + t('than') + ')' : ''), x + pad, y + pad, pal.gold2, 22, true);
      txt(p.canChi, x + cw - pad, y + pad + 2, pal.muted, 18, false, 'right');
      var yy = y + pad + 30;
      if (p.major.length) for (var k = 0; k < p.major.length; k++) { txt(p.major[k].name + ' (' + p.major[k].bright + ')', x + pad, yy, T.eleColor(p.major[k].element, pal.dark), 21, true); yy += 26; }
      else { txt(t('vcd'), x + pad, yy, pal.muted, 20, true); yy += 26; }
      var left = p.good.concat(p.neutral), right = p.bad, y0 = yy;
      for (k = 0; k < left.length && yy < y + ch - 40; k++) { txt(left[k].name, x + pad, yy, left[k].attachedTo ? pal.accent : pal.good, 17); yy += 20; }
      yy = y0;
      for (k = 0; k < right.length && yy < y + ch - 40; k++) { txt(right[k].name, x + cw / 2 + 4, yy, right[k].attachedTo ? pal.bad : pal.bad, 17); yy += 20; }
      var foot = t('dai_han') + ' ' + p.daiHan + (p.tuan ? ' · ' + t('tuan') : '') + (p.triet ? ' · ' + t('triet') : '');
      txt(foot, x + pad, y + ch - 32, pal.muted, 17);
      txt(p.tieuHan, x + cw - pad, y + ch - 32, pal.muted, 17, false, 'right');
    }
    // Thiên bàn
    var cx = cw, cy = ch;
    g.fillStyle = pal.card; g.fillRect(cx + 3, cy + 3, cw * 2 - 6, ch * 2 - 6);
    g.strokeStyle = pal.gold; g.strokeRect(cx + 3, cy + 3, cw * 2 - 6, ch * 2 - 6);
    var lines = [
      [chart.name, 34, pal.gold2, true],
      [t('solar_date') + ': ' + chart.solar.day + '/' + chart.solar.month + '/' + chart.solar.year, 22],
      [t('lunar_date') + ': ' + chart.lunar.day + '/' + chart.lunar.month + (chart.lunar.leap ? ' (N)' : '') + '/' + chart.lunar.year, 22],
      [chart.yearCanChi + ' · ' + chart.monthCanChi + ' · ' + chart.dayCanChi + ' · ' + chart.hourCanChi, 22],
      [chart.amDuong + ' · ' + chart.amDuongLy, 22],
      [t('ban_menh') + ': ' + chart.napAm, 22], [t('cuc') + ': ' + chart.cucName + ' (' + t('menh_cuc') + ': ' + chart.menhCuc + ')', 22],
      [t('menh_chu') + ': ' + chart.menhChu + ' · ' + t('than_chu') + ': ' + chart.thanChu, 22]
    ];
    var ly = cy + 60;
    for (var i = 0; i < lines.length; i++) { txt(lines[i][0], W / 2, ly, lines[i][2] || pal.text, lines[i][1], !!lines[i][3], 'center'); ly += lines[i][1] + 16; }
    txt('Applied Astrology 0.0.2 · Tử Vi Ứng Dụng – Bửu Đình', W / 2, H + 18, pal.muted, 20, false, 'center');
    return cv;
  }

  root.AAChartView = { render: render, detailHtml: detailHtml, toCanvas: toCanvas, LAYOUT: LAYOUT, esc: esc };
})(window);
