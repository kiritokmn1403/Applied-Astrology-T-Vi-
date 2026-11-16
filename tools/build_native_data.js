#!/usr/bin/env node
/*
 * Applied Astrology v0.0.4 – chuyển dữ liệu web (src/web/data/*.js, js/i18n.js) sang JSON dùng chung
 * cho bản native Android (Java) và Windows (C#): src/shared/data/
 *   lib/index.json, lib/NN.json ([[tiêu đề, nội dung], ...]), lang/<mã>.json (đủ khóa, đã gộp khóa mới v0.0.4),
 *   langs.json (danh sách 68 ngôn ngữ + RTL), changelog.json
 * Cách chạy: node tools/build_native_data.js
 */
'use strict';
const fs = require('fs'), path = require('path'), vm = require('vm');
const ROOT = path.resolve(__dirname, '..');
const WEB = path.join(ROOT, 'src', 'web');
const OUT = path.join(ROOT, 'src', 'shared', 'data');
fs.mkdirSync(path.join(OUT, 'lib'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'lang'), { recursive: true });

function sandbox() {
  const win = { document: { documentElement: { setAttribute() {} }, querySelectorAll() { return []; }, getElementsByTagName() { return [{ appendChild() {} }]; } } };
  win.window = win;
  return vm.createContext(win);
}

// ---- Thư viện ----
const ctx = sandbox();
vm.runInContext(fs.readFileSync(path.join(WEB, 'data', 'lib_index.js'), 'utf8'), ctx);
const idx = ctx.AA_LIB_INDEX;
const reg = {};
ctx.AALib = { register(id, arr) { reg[id] = arr; } };
let total = 0;
for (const ch of idx.chapters) {
  const f = path.join(WEB, 'data', 'lib_' + String(ch.n).padStart(2, '0') + '.js');
  vm.runInContext(fs.readFileSync(f, 'utf8'), ctx);
  const arr = reg[ch.id];
  if (!arr || arr.length !== ch.count) throw new Error('chapter ' + ch.id + ' count mismatch');
  fs.writeFileSync(path.join(OUT, 'lib', String(ch.n).padStart(2, '0') + '.json'), JSON.stringify(arr));
  total += arr.length;
}
fs.writeFileSync(path.join(OUT, 'lib', 'index.json'), JSON.stringify(idx));

// ---- Ngôn ngữ ----
const ictx = sandbox();
vm.runInContext(fs.readFileSync(path.join(WEB, 'js', 'i18n.js'), 'utf8'), ictx);
const I = ictx.AAI18n;
const D = I.dict;
for (const f of fs.readdirSync(path.join(WEB, 'data', 'lang'))) vm.runInContext(fs.readFileSync(path.join(WEB, 'data', 'lang', f), 'utf8'), ictx);
// khóa mới của v0.0.4 (Liquid Glass, native) – tools/i18n_v4/<mã>.json
const extraDir = path.join(ROOT, 'tools', 'i18n_v4');
const extra = {};
if (fs.existsSync(extraDir)) for (const f of fs.readdirSync(extraDir)) if (f.endsWith('.json')) extra[f.replace(/\.json$/, '')] = JSON.parse(fs.readFileSync(path.join(extraDir, f), 'utf8'));
const keysVi = Object.keys(Object.assign({}, D.vi, extra.vi || {}));
let missing = 0;
const langs = [];
for (const L of I.LANGS) {
  const code = L[0];
  const d = Object.assign({}, D[code] || {}, extra[code] || {});
  for (const k of keysVi) if (d[k] === undefined) { missing++; d[k] = (extra.en && extra.en[k]) || D.en[k] || (extra.vi && extra.vi[k]) || D.vi[k]; }
  fs.writeFileSync(path.join(OUT, 'lang', code + '.json'), JSON.stringify(d));
  langs.push({ code, name: L[1], rtl: !!L[2] });
}
fs.writeFileSync(path.join(OUT, 'langs.json'), JSON.stringify(langs));

// ---- Có gì mới ----
const cctx = sandbox();
vm.runInContext(fs.readFileSync(path.join(WEB, 'data', 'changelog.js'), 'utf8') + '\nwindow.__cl = AA_CHANGELOG;', cctx);
const v4 = path.join(ROOT, 'tools', 'changelog_v4.json');
let cl = cctx.__cl;
if (fs.existsSync(v4)) cl = JSON.parse(fs.readFileSync(v4, 'utf8')).concat(cl.filter(x => x.v !== '0.0.4'));
fs.writeFileSync(path.join(OUT, 'changelog.json'), JSON.stringify(cl));

console.log('library: ' + idx.chapters.length + ' chapters, ' + total + ' articles; langs: ' + langs.length + ' (' + keysVi.length + ' keys, ' + missing + ' filled from en/vi); changelog: ' + cl.length);
