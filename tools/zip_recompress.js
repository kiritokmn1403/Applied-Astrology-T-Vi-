// Applied Astrology v0.0.5 – nén lại tệp zip/APK bằng zopfli (vẫn là deflate chuẩn: Android 6+, Windows 7+ đọc bình thường).
// Dùng:  node tools/zip_recompress.js <vào.zip|apk> <ra.zip|apk> [số vòng lặp zopfli, mặc định 10]
// - Giữ nguyên thứ tự, tên, thời gian, thuộc tính; mục "stored" (không nén, ví dụ resources.arsc) giữ nguyên.
// - Mục deflate ≥ 2 KB được nén lại; chỉ thay khi nhỏ hơn. Bỏ data descriptor/extra (zipalign + apksigner chạy sau).
// - Tự kiểm tra: giải nén lại từng mục, so CRC-32 với bản gốc; sai là dừng (exit 1).
// zopfli: @gfx/zopfli 1.0.15 (Apache-2.0, WebAssembly) trong tools/zopfli_vendor – không cần cài gì thêm.
'use strict';
const fs = require('fs'), zlib = require('zlib');
const zop = require('./zopfli_vendor/zopfli/dist/index.js');
const T = new Int32Array(256); for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1; T[n] = c; }
function crc32(b) { let c = -1; for (let i = 0; i < b.length; i++) c = T[(c ^ b[i]) & 255] ^ (c >>> 8); return (c ^ -1) >>> 0; }
async function main() {
  const [inp, out, it] = process.argv.slice(2); const iters = Math.max(1, +it || 10);
  if (!inp || !out) { console.error('usage: node zip_recompress.js in out [iterations]'); process.exit(2); }
  const t0 = Date.now(), buf = fs.readFileSync(inp);
  let e = buf.length - 22; while (e >= 0 && buf.readUInt32LE(e) !== 0x06054b50) e--;
  if (e < 0) throw new Error('not a zip: ' + inp);
  const n = buf.readUInt16LE(e + 10), cdOff = buf.readUInt32LE(e + 16), comment = buf.slice(e + 22, e + 22 + buf.readUInt16LE(e + 20));
  const ents = []; let p = cdOff;
  for (let i = 0; i < n; i++) {
    if (buf.readUInt32LE(p) !== 0x02014b50) throw new Error('bad central directory');
    const en = { verMade: buf.readUInt16LE(p + 4), verNeed: buf.readUInt16LE(p + 6), flags: buf.readUInt16LE(p + 8) & ~0x08, method: buf.readUInt16LE(p + 10),
      time: buf.readUInt16LE(p + 12), date: buf.readUInt16LE(p + 14), crc: buf.readUInt32LE(p + 16), csize: buf.readUInt32LE(p + 20), usize: buf.readUInt32LE(p + 24),
      intAttr: buf.readUInt16LE(p + 36), extAttr: buf.readUInt32LE(p + 38) };
    const nl = buf.readUInt16LE(p + 28), xl = buf.readUInt16LE(p + 30), cl = buf.readUInt16LE(p + 32), lho = buf.readUInt32LE(p + 42);
    en.name = buf.slice(p + 46, p + 46 + nl);
    const ds = lho + 30 + buf.readUInt16LE(lho + 26) + buf.readUInt16LE(lho + 28);
    en.data = buf.slice(ds, ds + en.csize);
    ents.push(en); p += 46 + nl + xl + cl;
  }
  let before = 0, after = 0, redone = 0;
  for (const en of ents) {
    before += en.data.length;
    if (en.method === 8 && en.usize >= 2048) {
      const raw = zlib.inflateRawSync(en.data);
      if (crc32(raw) !== en.crc) throw new Error('CRC mismatch in source entry ' + en.name);
      const z = Buffer.from(await zop.deflateAsync(raw, { numiterations: iters }));
      const back = zlib.inflateRawSync(z);
      if (back.length !== raw.length || crc32(back) !== en.crc) throw new Error('zopfli verification failed for ' + en.name);
      if (z.length < en.data.length) { en.data = z; en.csize = z.length; redone++; }
    }
    after += en.data.length;
  }
  const parts = []; let off = 0;
  for (const en of ents) {
    const h = Buffer.alloc(30);
    h.writeUInt32LE(0x04034b50, 0); h.writeUInt16LE(en.verNeed, 4); h.writeUInt16LE(en.flags, 6); h.writeUInt16LE(en.method, 8); h.writeUInt16LE(en.time, 10); h.writeUInt16LE(en.date, 12);
    h.writeUInt32LE(en.crc, 14); h.writeUInt32LE(en.csize, 18); h.writeUInt32LE(en.usize, 22); h.writeUInt16LE(en.name.length, 26); h.writeUInt16LE(0, 28);
    en.off = off; parts.push(h, en.name, en.data); off += 30 + en.name.length + en.data.length;
  }
  const cdStart = off;
  for (const en of ents) {
    const c = Buffer.alloc(46);
    c.writeUInt32LE(0x02014b50, 0); c.writeUInt16LE(en.verMade, 4); c.writeUInt16LE(en.verNeed, 6); c.writeUInt16LE(en.flags, 8); c.writeUInt16LE(en.method, 10); c.writeUInt16LE(en.time, 12); c.writeUInt16LE(en.date, 14);
    c.writeUInt32LE(en.crc, 16); c.writeUInt32LE(en.csize, 20); c.writeUInt32LE(en.usize, 24); c.writeUInt16LE(en.name.length, 28); c.writeUInt16LE(0, 30); c.writeUInt16LE(0, 32);
    c.writeUInt16LE(0, 34); c.writeUInt16LE(en.intAttr, 36); c.writeUInt32LE(en.extAttr, 38); c.writeUInt32LE(en.off, 42);
    parts.push(c, en.name); off += 46 + en.name.length;
  }
  const z = Buffer.alloc(22); z.writeUInt32LE(0x06054b50, 0); z.writeUInt16LE(ents.length, 8); z.writeUInt16LE(ents.length, 10); z.writeUInt32LE(off - cdStart, 12); z.writeUInt32LE(cdStart, 16); z.writeUInt16LE(comment.length, 20);
  parts.push(z, comment);
  const outBuf = Buffer.concat(parts); fs.writeFileSync(out, outBuf);
  console.log('zip_recompress: ' + ents.length + ' entries, ' + redone + ' recompressed, data ' + before + ' -> ' + after + ' bytes (' + ((1 - after / before) * 100).toFixed(1) + '% smaller), file ' + buf.length + ' -> ' + outBuf.length + ' bytes, ' + ((Date.now() - t0) / 1000).toFixed(1) + ' s');
}
main().catch(err => { console.error('zip_recompress ERROR: ' + err.message); process.exit(1); });
