/*
 * Applied Astrology v0.0.2 - Bộ tạo mã QR (chế độ byte, UTF-8)
 * Chuyển thể ES5 từ "QR Code generator library" của Project Nayuki (MIT License)
 * https://github.com/nayuki/QR-Code-generator  — mục X.8 trong sheet Project GitHub.
 * Viết lại thủ công sang ES5 để chạy trên IE11 (Windows 7) và Android 6.0 WebView.
 */
(function (root) {
  'use strict';
  var ECC = { L: [0, 1], M: [1, 0], Q: [2, 3], H: [3, 2] }; // [ordinal, formatBits]
  var ECC_CODEWORDS_PER_BLOCK = [
    [-1, 7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30],
    [-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28],
    [-1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30],
    [-1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30]];
  var NUM_ERROR_CORRECTION_BLOCKS = [
    [-1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25],
    [-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49],
    [-1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68],
    [-1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81]];

  function getBit(x, i) { return ((x >>> i) & 1) !== 0; }
  function numRawDataModules(ver) {
    var result = (16 * ver + 128) * ver + 64;
    if (ver >= 2) { var na = Math.floor(ver / 7) + 2; result -= (25 * na - 10) * na - 55; if (ver >= 7) result -= 36; }
    return result;
  }
  function numDataCodewords(ver, ecl) {
    return Math.floor(numRawDataModules(ver) / 8) - ECC_CODEWORDS_PER_BLOCK[ecl[0]][ver] * NUM_ERROR_CORRECTION_BLOCKS[ecl[0]][ver];
  }
  function rsMultiply(x, y) {
    var z = 0;
    for (var i = 7; i >= 0; i--) { z = (z << 1) ^ ((z >>> 7) * 0x11D); z ^= ((y >>> i) & 1) * x; }
    return z & 0xFF;
  }
  function rsDivisor(degree) {
    var result = []; for (var i = 0; i < degree - 1; i++) result.push(0); result.push(1);
    var r = 1;
    for (i = 0; i < degree; i++) {
      for (var j = 0; j < result.length; j++) { result[j] = rsMultiply(result[j], r); if (j + 1 < result.length) result[j] ^= result[j + 1]; }
      r = rsMultiply(r, 0x02);
    }
    return result;
  }
  function rsRemainder(data, divisor) {
    var result = divisor.map(function () { return 0; });
    for (var i = 0; i < data.length; i++) {
      var factor = data[i] ^ result.shift(); result.push(0);
      for (var j = 0; j < divisor.length; j++) result[j] ^= rsMultiply(divisor[j], factor);
    }
    return result;
  }
  function utf8Bytes(str) {
    var out = [], s = unescape(encodeURIComponent(str));
    for (var i = 0; i < s.length; i++) out.push(s.charCodeAt(i));
    return out;
  }

  function QrCode(version, ecl, dataCodewords, msk) {
    this.version = version; this.ecl = ecl; this.size = version * 4 + 17;
    this.modules = []; this.isFunction = [];
    for (var i = 0; i < this.size; i++) {
      var row = [], fr = []; for (var j = 0; j < this.size; j++) { row.push(false); fr.push(false); }
      this.modules.push(row); this.isFunction.push(fr);
    }
    this.drawFunctionPatterns();
    var allCodewords = this.addEccAndInterleave(dataCodewords);
    this.drawCodewords(allCodewords);
    if (msk === -1) {
      var minPenalty = 1e9;
      for (i = 0; i < 8; i++) {
        this.applyMask(i); this.drawFormatBits(i);
        var penalty = this.getPenaltyScore();
        if (penalty < minPenalty) { msk = i; minPenalty = penalty; }
        this.applyMask(i);
      }
    }
    this.mask = msk; this.applyMask(msk); this.drawFormatBits(msk);
    this.isFunction = [];
  }
  QrCode.prototype.getModule = function (x, y) { return x >= 0 && x < this.size && y >= 0 && y < this.size && this.modules[y][x]; };
  QrCode.prototype.setFunctionModule = function (x, y, isDark) { this.modules[y][x] = isDark; this.isFunction[y][x] = true; };
  QrCode.prototype.drawFunctionPatterns = function () {
    for (var i = 0; i < this.size; i++) { this.setFunctionModule(6, i, i % 2 === 0); this.setFunctionModule(i, 6, i % 2 === 0); }
    this.drawFinderPattern(3, 3); this.drawFinderPattern(this.size - 4, 3); this.drawFinderPattern(3, this.size - 4);
    var pos = this.getAlignmentPatternPositions(), n = pos.length;
    for (i = 0; i < n; i++) for (var j = 0; j < n; j++) {
      if (!((i === 0 && j === 0) || (i === 0 && j === n - 1) || (i === n - 1 && j === 0))) this.drawAlignmentPattern(pos[i], pos[j]);
    }
    this.drawFormatBits(0); this.drawVersion();
  };
  QrCode.prototype.drawFormatBits = function (mask) {
    var data = this.ecl[1] << 3 | mask, rem = data;
    for (var i = 0; i < 10; i++) rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
    var bits = (data << 10 | rem) ^ 0x5412;
    for (i = 0; i <= 5; i++) this.setFunctionModule(8, i, getBit(bits, i));
    this.setFunctionModule(8, 7, getBit(bits, 6)); this.setFunctionModule(8, 8, getBit(bits, 7)); this.setFunctionModule(7, 8, getBit(bits, 8));
    for (i = 9; i < 15; i++) this.setFunctionModule(14 - i, 8, getBit(bits, i));
    for (i = 0; i < 8; i++) this.setFunctionModule(this.size - 1 - i, 8, getBit(bits, i));
    for (i = 8; i < 15; i++) this.setFunctionModule(8, this.size - 15 + i, getBit(bits, i));
    this.setFunctionModule(8, this.size - 8, true);
  };
  QrCode.prototype.drawVersion = function () {
    if (this.version < 7) return;
    var rem = this.version;
    for (var i = 0; i < 12; i++) rem = (rem << 1) ^ ((rem >>> 11) * 0x1F25);
    var bits = this.version << 12 | rem;
    for (i = 0; i < 18; i++) {
      var color = getBit(bits, i), a = this.size - 11 + i % 3, b = Math.floor(i / 3);
      this.setFunctionModule(a, b, color); this.setFunctionModule(b, a, color);
    }
  };
  QrCode.prototype.drawFinderPattern = function (x, y) {
    for (var dy = -4; dy <= 4; dy++) for (var dx = -4; dx <= 4; dx++) {
      var dist = Math.max(Math.abs(dx), Math.abs(dy)), xx = x + dx, yy = y + dy;
      if (xx >= 0 && xx < this.size && yy >= 0 && yy < this.size) this.setFunctionModule(xx, yy, dist !== 2 && dist !== 4);
    }
  };
  QrCode.prototype.drawAlignmentPattern = function (x, y) {
    for (var dy = -2; dy <= 2; dy++) for (var dx = -2; dx <= 2; dx++) this.setFunctionModule(x + dx, y + dy, Math.max(Math.abs(dx), Math.abs(dy)) !== 1);
  };
  QrCode.prototype.addEccAndInterleave = function (data) {
    var ver = this.version, ecl = this.ecl;
    var numBlocks = NUM_ERROR_CORRECTION_BLOCKS[ecl[0]][ver], blockEccLen = ECC_CODEWORDS_PER_BLOCK[ecl[0]][ver];
    var rawCodewords = Math.floor(numRawDataModules(ver) / 8);
    var numShortBlocks = numBlocks - rawCodewords % numBlocks, shortBlockLen = Math.floor(rawCodewords / numBlocks);
    var blocks = [], rsDiv = rsDivisor(blockEccLen);
    for (var i = 0, k = 0; i < numBlocks; i++) {
      var dat = data.slice(k, k + shortBlockLen - blockEccLen + (i < numShortBlocks ? 0 : 1));
      k += dat.length;
      var ecc = rsRemainder(dat, rsDiv);
      if (i < numShortBlocks) dat.push(0);
      blocks.push(dat.concat(ecc));
    }
    var result = [];
    for (i = 0; i < blocks[0].length; i++) for (var j = 0; j < blocks.length; j++) {
      if (i !== shortBlockLen - blockEccLen || j >= numShortBlocks) result.push(blocks[j][i]);
    }
    return result;
  };
  QrCode.prototype.drawCodewords = function (data) {
    var i = 0;
    for (var right = this.size - 1; right >= 1; right -= 2) {
      if (right === 6) right = 5;
      for (var vert = 0; vert < this.size; vert++) for (var j = 0; j < 2; j++) {
        var x = right - j, upward = ((right + 1) & 2) === 0, y = upward ? this.size - 1 - vert : vert;
        if (!this.isFunction[y][x] && i < data.length * 8) { this.modules[y][x] = getBit(data[i >>> 3], 7 - (i & 7)); i++; }
      }
    }
  };
  QrCode.prototype.applyMask = function (mask) {
    for (var y = 0; y < this.size; y++) for (var x = 0; x < this.size; x++) {
      var invert;
      switch (mask) {
        case 0: invert = (x + y) % 2 === 0; break;
        case 1: invert = y % 2 === 0; break;
        case 2: invert = x % 3 === 0; break;
        case 3: invert = (x + y) % 3 === 0; break;
        case 4: invert = (Math.floor(x / 3) + Math.floor(y / 2)) % 2 === 0; break;
        case 5: invert = x * y % 2 + x * y % 3 === 0; break;
        case 6: invert = (x * y % 2 + x * y % 3) % 2 === 0; break;
        default: invert = ((x + y) % 2 + x * y % 3) % 2 === 0;
      }
      if (!this.isFunction[y][x] && invert) this.modules[y][x] = !this.modules[y][x];
    }
  };
  QrCode.prototype.getPenaltyScore = function () {
    var result = 0, size = this.size, x, y, runColor, runX, runY, runHistory;
    var self = this;
    function addHistory(currentRunLength, history) {
      if (history[0] === 0) currentRunLength += size;
      history.pop(); history.unshift(currentRunLength);
    }
    function countPatterns(h) {
      var n = h[1], core = n > 0 && h[2] === n && h[3] === n * 3 && h[4] === n && h[5] === n;
      return (core && h[0] >= n * 4 && h[6] >= n ? 1 : 0) + (core && h[6] >= n * 4 && h[0] >= n ? 1 : 0);
    }
    function terminate(color, len, h) {
      if (color) { addHistory(len, h); len = 0; }
      len += size; addHistory(len, h); return countPatterns(h);
    }
    for (y = 0; y < size; y++) {
      runColor = false; runX = 0; runHistory = [0, 0, 0, 0, 0, 0, 0];
      for (x = 0; x < size; x++) {
        if (self.modules[y][x] === runColor) { runX++; if (runX === 5) result += 3; else if (runX > 5) result++; }
        else { addHistory(runX, runHistory); if (!runColor) result += countPatterns(runHistory) * 40; runColor = self.modules[y][x]; runX = 1; }
      }
      result += terminate(runColor, runX, runHistory) * 40;
    }
    for (x = 0; x < size; x++) {
      runColor = false; runY = 0; runHistory = [0, 0, 0, 0, 0, 0, 0];
      for (y = 0; y < size; y++) {
        if (self.modules[y][x] === runColor) { runY++; if (runY === 5) result += 3; else if (runY > 5) result++; }
        else { addHistory(runY, runHistory); if (!runColor) result += countPatterns(runHistory) * 40; runColor = self.modules[y][x]; runY = 1; }
      }
      result += terminate(runColor, runY, runHistory) * 40;
    }
    for (y = 0; y < size - 1; y++) for (x = 0; x < size - 1; x++) {
      var c = self.modules[y][x];
      if (c === self.modules[y][x + 1] && c === self.modules[y + 1][x] && c === self.modules[y + 1][x + 1]) result += 3;
    }
    var dark = 0;
    for (y = 0; y < size; y++) for (x = 0; x < size; x++) if (self.modules[y][x]) dark++;
    var total = size * size, kk = Math.ceil(Math.abs(dark * 20 - total * 10) / total) - 1;
    result += kk * 10;
    return result;
  };
  QrCode.prototype.getAlignmentPatternPositions = function () {
    if (this.version === 1) return [];
    var numAlign = Math.floor(this.version / 7) + 2;
    var step = (this.version === 32) ? 26 : Math.ceil((this.version * 4 + 4) / (numAlign * 2 - 2)) * 2;
    var result = [6];
    for (var pos = this.size - 7; result.length < numAlign; pos -= step) result.splice(1, 0, pos);
    return result;
  };

  /** Mã hóa văn bản (UTF-8, chế độ byte). eclName: 'L'|'M'|'Q'|'H' */
  function encodeText(text, eclName) {
    var ecl = ECC[eclName || 'M'], data = utf8Bytes(text), version, dataUsedBits;
    for (version = 1; version <= 40; version++) {
      var ccBits = version < 10 ? 8 : 16;
      dataUsedBits = 4 + ccBits + data.length * 8;
      if (data.length < (1 << ccBits) && dataUsedBits <= numDataCodewords(version, ecl) * 8) break;
    }
    if (version > 40) throw new Error('DATA_TOO_LONG');
    // Nâng mức sửa lỗi nếu vẫn vừa
    var order = ['M', 'Q', 'H'];
    for (var o = 0; o < order.length; o++) if (ECC[order[o]][0] > ecl[0] && dataUsedBits <= numDataCodewords(version, ECC[order[o]]) * 8) ecl = ECC[order[o]];
    var bb = [];
    function append(val, len) { for (var i = len - 1; i >= 0; i--) bb.push((val >>> i) & 1); }
    append(4, 4); append(data.length, version < 10 ? 8 : 16);
    for (var i = 0; i < data.length; i++) append(data[i], 8);
    var cap = numDataCodewords(version, ecl) * 8;
    append(0, Math.min(4, cap - bb.length)); append(0, (8 - bb.length % 8) % 8);
    for (var pad = 0xEC; bb.length < cap; pad ^= 0xEC ^ 0x11) append(pad, 8);
    var codewords = [];
    for (i = 0; i < bb.length / 8; i++) { var b = 0; for (var j = 0; j < 8; j++) b = (b << 1) | bb[i * 8 + j]; codewords.push(b); }
    return new QrCode(version, ecl, codewords, -1);
  }

  /** Vẽ mã QR lên canvas, trả về canvas */
  function toCanvas(qr, scale, border, canvas, dark, light) {
    scale = scale || 6; border = border === undefined ? 4 : border;
    canvas = canvas || document.createElement('canvas');
    var w = (qr.size + border * 2) * scale;
    canvas.width = w; canvas.height = w;
    var ctx = canvas.getContext('2d');
    ctx.fillStyle = light || '#FFFFFF'; ctx.fillRect(0, 0, w, w);
    ctx.fillStyle = dark || '#000000';
    for (var y = 0; y < qr.size; y++) for (var x = 0; x < qr.size; x++) if (qr.getModule(x, y)) ctx.fillRect((x + border) * scale, (y + border) * scale, scale, scale);
    return canvas;
  }

  root.AAQr = { encodeText: encodeText, toCanvas: toCanvas };
  if (typeof module !== 'undefined' && module.exports) module.exports = root.AAQr;
})(typeof window !== 'undefined' ? window : this);
