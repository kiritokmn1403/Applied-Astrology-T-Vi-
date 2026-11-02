/*
 * Applied Astrology v0.0.2 - Chủ đề màu sắc
 * IE11 (Windows 7) không hỗ trợ CSS variables, nên màu được sinh thành CSS thật và chèn vào <style id="aa-theme">.
 */
(function (root) {
  'use strict';
  var P = {
    'celestial-dark': { bg: '#080b16', bg2: '#0e1428', card: '#141b33', card2: '#1b2442', line: '#2c3763', gold: '#f2b84b', gold2: '#ffe7a3',
      text: '#eef2fb', muted: '#9aa6c4', accent: '#7dd3fc', purple: '#a78bfa', good: '#34d399', bad: '#fb7185', sel: '#2a2160', menh: '#f2b84b', dark: true },
    'royal-gold': { bg: '#140f06', bg2: '#21190a', card: '#2a2010', card2: '#372a14', line: '#5c4720', gold: '#ffcf4d', gold2: '#fff1c2',
      text: '#fff8e6', muted: '#d7b779', accent: '#ffd98a', purple: '#e3b5ff', good: '#9be27a', bad: '#ff8a7a', sel: '#4a3712', menh: '#ffcf4d', dark: true },
    'ivory-light': { bg: '#f6f3ec', bg2: '#ece6d8', card: '#ffffff', card2: '#faf7f0', line: '#d9cdb2', gold: '#9a6a12', gold2: '#6b4708',
      text: '#1f2333', muted: '#6b6f80', accent: '#0e6fa8', purple: '#6d4fc2', good: '#0f8a5f', bad: '#c0283d', sel: '#f3e6c4', menh: '#9a6a12', dark: false },
    'imperial-red': { bg: '#170709', bg2: '#250b10', card: '#311017', card2: '#40151e', line: '#6d2733', gold: '#f6b24a', gold2: '#ffe0a6',
      text: '#fff1f2', muted: '#e7a3ad', accent: '#ffc58f', purple: '#f0a8ff', good: '#7ee0a8', bad: '#ff7b8f', sel: '#5a1a25', menh: '#f6b24a', dark: true }
  };
  var ELE_DARK = { 'Kim': '#e5e7eb', 'Mộc': '#4ade80', 'Thủy': '#60a5fa', 'Hỏa': '#f87171', 'Thổ': '#fbbf24' };
  var ELE_LIGHT = { 'Kim': '#6b7280', 'Mộc': '#15803d', 'Thủy': '#1d4ed8', 'Hỏa': '#b91c1c', 'Thổ': '#a16207' };

  function css(c) {
    var e = c.dark ? ELE_DARK : ELE_LIGHT;
    return [
      'html,body{background:' + c.bg + ';color:' + c.text + ';}',
      '.aa-header{background:' + c.bg2 + ';border-bottom:1px solid ' + c.line + ';}',
      '.aa-title{color:' + c.gold2 + ';} .aa-subtitle,.muted,.hint{color:' + c.muted + ';}',
      '.aa-nav{background:' + c.bg2 + ';border-top:1px solid ' + c.line + ';border-bottom:1px solid ' + c.line + ';}',
      '.aa-nav a{color:' + c.muted + ';} .aa-nav a.active{color:' + c.gold + ';border-color:' + c.gold + ';}',
      '.card,.palace,.thien-ban,.modal-box,.list-item,.setting,.msg.ai,.chip{background:' + c.card + ';border-color:' + c.line + ';}',
      '.card h2,.card h3,.setting h3,.reader h1{color:' + c.gold2 + ';}',
      'input,select,textarea{background:' + c.card2 + ';color:' + c.text + ';border-color:' + c.line + ';}',
      '.btn{background:' + c.card2 + ';color:' + c.text + ';border-color:' + c.line + ';} .btn:hover{border-color:' + c.gold + ';}',
      '.btn.primary{background:' + c.gold + ';color:' + (c.dark ? '#1a1205' : '#ffffff') + ';border-color:' + c.gold + ';}',
      '.palace.sel{background:' + c.sel + ';border-color:' + c.gold + ';} .palace.tp{border-color:' + c.accent + ';}',
      '.palace.cur-dh .p-dh{color:' + c.gold + ';font-weight:bold;} .palace.cur-th .p-th{color:' + c.accent + ';font-weight:bold;}',
      '.p-name{color:' + c.gold2 + ';} .p-canchi,.p-ring,.p-foot{color:' + c.muted + ';}',
      '.badge{background:' + c.gold + ';color:' + (c.dark ? '#1a1205' : '#fff') + ';} .badge.than{background:' + c.accent + ';}',
      '.badge.tt{background:' + c.bad + ';color:#fff;}',
      '.s-good{color:' + c.good + ';} .s-bad{color:' + c.bad + ';} .s-neutral{color:' + c.text + ';} .s-luu{color:' + c.purple + ';}',
      '.hoa-loc{color:' + c.good + ';} .hoa-quyen{color:' + c.gold + ';} .hoa-khoa{color:' + c.accent + ';} .hoa-ky{color:' + c.bad + ';}',
      '.e-kim{color:' + e['Kim'] + ';} .e-moc{color:' + e['Mộc'] + ';} .e-thuy{color:' + e['Thủy'] + ';} .e-hoa{color:' + e['Hỏa'] + ';} .e-tho{color:' + e['Thổ'] + ';}',
      '.tb-name{color:' + c.gold2 + ';} .tb-row b{color:' + c.text + ';} .tb-row{color:' + c.muted + ';}',
      'a,.link{color:' + c.accent + ';} .list-item:hover{border-color:' + c.gold + ';}',
      '.msg.user{background:' + c.sel + ';border-color:' + c.gold + ';} mark{background:' + c.gold + ';color:#000;}',
      '.toast{background:' + c.gold + ';color:#1a1205;} .modal{background:rgba(0,0,0,0.6);}',
      '.reader-body{color:' + c.text + ';} .status-ok{color:' + c.good + ';} .status-bad{color:' + c.bad + ';}',
      '.switch .knob{background:' + c.line + ';} .switch input:checked + .knob{background:' + c.gold + ';}'
    ].join('\n');
  }
  function apply(name) {
    var c = P[name] || P['celestial-dark'];
    var el = document.getElementById('aa-theme');
    if (!el) { el = document.createElement('style'); el.id = 'aa-theme'; document.getElementsByTagName('head')[0].appendChild(el); }
    var txt = css(c);
    if (el.styleSheet) el.styleSheet.cssText = txt; else { while (el.firstChild) el.removeChild(el.firstChild); el.appendChild(document.createTextNode(txt)); }
    document.documentElement.className = document.documentElement.className.replace(/\btheme-\S+/g, '') + ' theme-' + name;
    return c;
  }
  function eleClass(el) { return { 'Kim': 'e-kim', 'Mộc': 'e-moc', 'Thủy': 'e-thuy', 'Hỏa': 'e-hoa', 'Thổ': 'e-tho' }[el] || ''; }
  root.AATheme = { palettes: P, apply: apply, eleClass: eleClass, eleColor: function (el, dark) { return (dark ? ELE_DARK : ELE_LIGHT)[el] || (dark ? '#fff' : '#000'); } };
})(window);
