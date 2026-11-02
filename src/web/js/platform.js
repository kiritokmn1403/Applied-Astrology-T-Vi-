/*
 * Applied Astrology v0.0.2 - Lớp nền tảng (polyfill + cầu nối native)
 * Android: window.AANative (addJavascriptInterface)
 * Windows: window.external (IE11 WebBrowser, ObjectForScripting) hoặc chrome.webview (Edge WebView2)
 * Trình duyệt thường: dùng localStorage / tải file qua thẻ <a download>.
 */
(function (root) {
  'use strict';
  // ---------- Polyfills tối thiểu cho IE11 / WebView cũ ----------
  if (!Object.assign) {
    Object.assign = function (t) {
      for (var i = 1; i < arguments.length; i++) { var s = arguments[i]; if (s) for (var k in s) if (Object.prototype.hasOwnProperty.call(s, k)) t[k] = s[k]; }
      return t;
    };
  }
  if (!Array.prototype.find) {
    Array.prototype.find = function (fn, ctx) { for (var i = 0; i < this.length; i++) if (fn.call(ctx, this[i], i, this)) return this[i]; return undefined; };
  }
  if (!Array.prototype.fill) {
    Array.prototype.fill = function (v, s, e) { s = s || 0; e = e === undefined ? this.length : e; for (var i = s; i < e; i++) this[i] = v; return this; };
  }
  if (!String.prototype.trim) String.prototype.trim = function () { return this.replace(/^\s+|\s+$/g, ''); };
  if (!Date.now) Date.now = function () { return new Date().getTime(); };
  if (typeof Element !== 'undefined' && !Element.prototype.matches) Element.prototype.matches = Element.prototype.msMatchesSelector;
  if (typeof Element !== 'undefined' && !Element.prototype.closest) {
    Element.prototype.closest = function (sel) { var el = this; while (el && el.nodeType === 1) { if (el.matches(sel)) return el; el = el.parentNode; } return null; };
  }
  if (typeof NodeList !== 'undefined' && !NodeList.prototype.forEach) NodeList.prototype.forEach = Array.prototype.forEach;

  // ---------- Ghi nhận lỗi JS (gửi về host để kiểm thử; hiện banner khi AA_DEBUG) ----------
  root.AA_ERRORS = [];
  root.onerror = function (msg, src, line, col) {
    var m = String(msg) + ' @' + String(src || '').split('/').pop() + ':' + line + ':' + (col || 0);
    root.AA_ERRORS.push(m);
    try { if (root.AAHost) root.AAHost.log('JSERROR ' + m); } catch (e) { /* bỏ qua */ }
    if (root.AA_DEBUG && document.body) {
      var b = document.getElementById('aa-err') || document.createElement('pre');
      b.id = 'aa-err'; b.style.cssText = 'position:fixed;left:0;right:0;top:0;z-index:999;background:#b00;color:#fff;font-size:12px;margin:0;padding:6px;white-space:pre-wrap';
      b.textContent = root.AA_ERRORS.join('\n'); document.body.appendChild(b);
    }
    return false;
  };

  // ---------- Bỏ dấu tiếng Việt (không dùng String.normalize vì IE11 không hỗ trợ) ----------
  var FOLD_SRC = 'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ';
  var FOLD_DST = 'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd';
  var FOLD = {};
  for (var fi = 0; fi < FOLD_SRC.length; fi++) FOLD[FOLD_SRC.charAt(fi)] = FOLD_DST.charAt(fi);
  function fold(s) {
    s = String(s || '').toLowerCase();
    var out = '';
    for (var i = 0; i < s.length; i++) {
      var c = s.charAt(i), r = FOLD[c];
      if (r) out += r;
      else if (c === '̀' || c === '́' || c === '̃' || c === '̉' || c === '̣') continue;
      else out += c;
    }
    return out;
  }

  // ---------- Phát hiện nền tảng ----------
  var ext = null;
  try { if (root.external && typeof root.external.HostCall !== 'undefined') ext = root.external; } catch (e) { ext = null; }
  var wv2 = null;
  try { if (root.chrome && root.chrome.webview && root.chrome.webview.hostObjects) wv2 = root.chrome.webview.hostObjects.sync.aahost; } catch (e2) { wv2 = null; }
  var nativeAndroid = root.AANative || null;

  function hostCall(cmd, a, b) {
    try {
      if (nativeAndroid) {
        if (cmd === 'storeGet') return nativeAndroid.storeGet(a);
        if (cmd === 'storeSet') return nativeAndroid.storeSet(a, b);
        if (cmd === 'saveFile') return nativeAndroid.saveFile(a, b);
        if (cmd === 'share') return nativeAndroid.share(a);
        if (cmd === 'openUrl') return nativeAndroid.openUrl(a);
        if (cmd === 'info') return nativeAndroid.info();
        if (cmd === 'search') return nativeAndroid.search(a, b);
        if (cmd === 'testStep') return nativeAndroid.testStep(a, b);
        if (cmd === 'log') return nativeAndroid.log(a);
        if (cmd === 'httpGet') return nativeAndroid.httpGet(a);
        return null;
      }
      if (wv2) return wv2.HostCall(cmd, a === undefined ? '' : String(a), b === undefined ? '' : String(b));
      if (ext) return ext.HostCall(cmd, a === undefined ? '' : String(a), b === undefined ? '' : String(b));
    } catch (err) { /* host không hỗ trợ lệnh này */ }
    return null;
  }

  var kind = nativeAndroid ? 'android' : (wv2 ? 'windows-webview2' : (ext ? 'windows-ie' : 'browser'));
  var memStore = {};
  var ls = null;
  try { ls = root.localStorage; ls.setItem('__aa', '1'); ls.removeItem('__aa'); } catch (e3) { ls = null; }

  var Host = {
    kind: kind,
    isAndroid: kind === 'android',
    isWindows: kind.indexOf('windows') === 0,
    fold: fold,
    info: function () {
      var raw = hostCall('info');
      var o = {};
      try { o = raw ? JSON.parse(raw) : {}; } catch (e) { o = {}; }
      o.kind = kind; o.ua = root.navigator ? navigator.userAgent : '';
      return o;
    },
    get: function (key, def) {
      var v = null;
      if (kind !== 'browser') v = hostCall('storeGet', key);
      if ((v === null || v === undefined || v === '') && ls) { try { v = ls.getItem(key); } catch (e) { v = null; } }
      if ((v === null || v === undefined || v === '') && memStore.hasOwnProperty(key)) v = memStore[key];
      if (v === null || v === undefined || v === '') return def;
      try { return JSON.parse(v); } catch (e2) { return def; }
    },
    set: function (key, val) {
      var s = JSON.stringify(val);
      memStore[key] = s;
      if (kind !== 'browser') hostCall('storeSet', key, s);
      if (ls) { try { ls.setItem(key, s); } catch (e) { /* đầy bộ nhớ */ } }
    },
    /** Lưu tệp. data: chuỗi văn bản, hoặc dataURL 'data:...;base64,...' */
    saveFile: function (name, data) {
      var r = hostCall('saveFile', name, data);
      if (r) return r;
      // Trình duyệt / WebView không có host
      try {
        if (root.navigator && navigator.msSaveBlob && root.Blob) {
          var blob;
          if (data.indexOf('data:') === 0) {
            var b64 = data.split(',')[1], bin = atob(b64), arr = new Uint8Array(bin.length);
            for (var i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
            blob = new Blob([arr]);
          } else blob = new Blob([data], { type: 'application/json' });
          navigator.msSaveBlob(blob, name);
          return name;
        }
        var a = document.createElement('a');
        a.href = data.indexOf('data:') === 0 ? data : 'data:application/json;charset=utf-8,' + encodeURIComponent(data);
        a.download = name; document.body.appendChild(a); a.click(); document.body.removeChild(a);
        return name;
      } catch (e) { return null; }
    },
    share: function (text) {
      if (hostCall('share', text)) return true;
      try { if (root.navigator && navigator.share) { navigator.share({ text: text }); return true; } } catch (e) { /* bỏ qua */ }
      return false;
    },
    openUrl: function (url) {
      if (hostCall('openUrl', url)) return;
      try { root.open(url, '_blank'); } catch (e) { /* bỏ qua */ }
    },
    nativeSearch: function (q, max) {
      if (kind !== 'android') return null;
      var r = hostCall('search', fold(q), String(max || 200));
      if (!r) return null;
      try { return JSON.parse(r); } catch (e) { return null; }
    },
    /** GET qua host native (tránh CORS khi chạy từ file://). cb({ok, status, body, ms}) */
    httpGet: function (url, cb) {
      var t0 = Date.now();
      if (kind !== 'browser') {
        setTimeout(function () {
          var r = hostCall('httpGet', url), o = null;
          try { o = r ? JSON.parse(r) : null; } catch (e) { o = null; }
          if (o) { o.ms = o.ms || (Date.now() - t0); cb(o); return; }
          xhr();
        }, 10);
        return;
      }
      xhr();
      function xhr() {
        try {
          var x = new XMLHttpRequest();
          x.open('GET', url, true); x.timeout = 8000;
          x.onreadystatechange = function () { if (x.readyState === 4) cb({ ok: x.status >= 200 && x.status < 400, status: x.status, body: x.responseText || '', ms: Date.now() - t0 }); };
          x.ontimeout = function () { cb({ ok: false, status: 0, body: '', ms: Date.now() - t0 }); };
          x.send();
        } catch (e) { cb({ ok: false, status: 0, body: '', ms: Date.now() - t0 }); }
      }
    },
    testStep: function (name, payload) { return hostCall('testStep', name, payload || ''); },
    log: function (msg) { hostCall('log', msg); if (root.console) console.log('[AA] ' + msg); }
  };
  root.AAHost = Host;
})(window);
