/*
 * Applied Astrology v0.0.2 - Thư viện Bửu Đình
 * 611 bài nguyên văn / 13 chương, nạp lười từng chương (data/lib_NN.js) để khởi động nhanh.
 * Tìm kiếm: Android dùng thư viện native libaacore.so (JNI) theo ABI; nền tảng khác dùng JS.
 */
(function (root) {
  'use strict';
  var IDX = root.AA_LIB_INDEX || { chapters: [] };
  var chapters = IDX.chapters;
  var byId = {}, loaded = {}, waiting = {}, foldCache = {};
  for (var i = 0; i < chapters.length; i++) byId[chapters[i].id] = chapters[i];
  var CH_KEY = { 'chinh-tinh': 'ch_chinh_tinh', 'can-tinh': 'ch_can_tinh', 'chi-tinh': 'ch_chi_tinh', 'sao-khac': 'ch_sao_khac',
    'cac-cung': 'ch_cac_cung', '72-cach': 'ch_72_cach', 'an-sao': 'ch_an_sao', 'han': 'ch_han', 'la-so-nam': 'ch_la_so_nam',
    'la-so-nu': 'ch_la_so_nu', 'tu-dien': 'ch_tu_dien', '2-chinh-tinh': 'ch_2_chinh_tinh', 'cac-bo': 'ch_cac_bo' };

  function pad2(n) { return n < 10 ? '0' + n : '' + n; }

  function load(id, cb) {
    if (loaded[id]) { if (cb) cb(loaded[id]); return; }
    if (waiting[id]) { if (cb) waiting[id].push(cb); return; }
    waiting[id] = cb ? [cb] : [];
    var ch = byId[id];
    var s = document.createElement('script');
    s.src = 'data/lib_' + pad2(ch.n) + '.js';
    s.onerror = function () { var w = waiting[id]; delete waiting[id]; for (var k = 0; k < w.length; k++) w[k](null); };
    document.getElementsByTagName('head')[0].appendChild(s);
  }
  function loadAll(cb, progress) {
    var left = chapters.length, done = 0;
    if (!left) { cb(); return; }
    for (var k = 0; k < chapters.length; k++) {
      (function (id) {
        load(id, function () { done++; if (progress) progress(done, chapters.length); if (--left === 0) cb(); });
      })(chapters[k].id);
    }
  }

  var Lib = {
    index: IDX,
    chapters: chapters,
    chapterName: function (id) { return CH_KEY[id] ? root.t(CH_KEY[id]) : (byId[id] ? byId[id].name : id); },
    totalArticles: function () { var n = 0; for (var k = 0; k < chapters.length; k++) n += chapters[k].count; return n; },
    register: function (id, arr) {
      loaded[id] = arr;
      var w = waiting[id] || []; delete waiting[id];
      for (var k = 0; k < w.length; k++) w[k](arr);
    },
    load: load,
    loadAll: loadAll,
    isLoaded: function (id) { return !!loaded[id]; },
    get: function (id, idx, cb) { load(id, function (arr) { cb(arr && arr[idx] ? { ch: id, i: idx, title: arr[idx][0], body: arr[idx][1] } : null); }); },
    title: function (id, idx) { var c = byId[id]; return c && c.articles[idx] ? c.articles[idx].t : ''; },
    chapterById: function (id) { return byId[id]; },
    chapterByNo: function (n) { for (var k = 0; k < chapters.length; k++) if (chapters[k].n === n) return chapters[k]; return null; },

    /** Tìm kiếm toàn văn. cb(list of {ch, i, title, snippet, score}) */
    search: function (q, cb, progress) {
      var fq = root.AAHost.fold(q).replace(/\s+/g, ' ').trim();
      if (fq.length < 2) { cb([]); return; }
      var nat = root.AAHost.nativeSearch(q, 300);
      if (nat && nat.length !== undefined) {
        var res = [];
        for (var k = 0; k < nat.length; k++) {
          var ch = this.chapterByNo(nat[k].c);
          if (ch) res.push({ ch: ch.id, i: nat[k].i, o: nat[k].o, title: ch.articles[nat[k].i] ? ch.articles[nat[k].i].t : '', score: nat[k].s || 1, snippet: null });
        }
        // nạp đoạn trích cho 40 kết quả đầu
        this._fillSnippets(res.slice(0, 40), fq, function () { cb(res); });
        return;
      }
      var self = this;
      loadAll(function () { cb(self._jsSearch(fq)); }, progress);
    },
    _fold: function (id, idx, text) {
      var key = id + ':' + idx;
      if (!foldCache[key]) foldCache[key] = root.AAHost.fold(text);
      return foldCache[key];
    },
    _jsSearch: function (fq) {
      var res = [], words = fq.split(' ');
      for (var c = 0; c < chapters.length; c++) {
        var id = chapters[c].id, arr = loaded[id];
        if (!arr) continue;
        for (var k = 0; k < arr.length; k++) {
          var ft = this._fold(id, 't' + k, arr[k][0]), fb = this._fold(id, k, arr[k][1]);
          var pos = fb.indexOf(fq), score = 0;
          if (ft.indexOf(fq) >= 0) score += 50;
          if (pos >= 0) {
            score += 10;
            var cnt = 0, p = pos; while (p >= 0 && cnt < 20) { cnt++; p = fb.indexOf(fq, p + fq.length); }
            score += cnt;
          } else if (words.length > 1) {
            var all = true;
            for (var w = 0; w < words.length; w++) if (fb.indexOf(words[w]) < 0 && ft.indexOf(words[w]) < 0) { all = false; break; }
            if (all) { score += 3; pos = fb.indexOf(words[0]); }
          }
          if (score > 0) res.push({ ch: id, i: k, title: arr[k][0], score: score, snippet: this._snippet(arr[k][1], Math.max(pos, 0), fq.length) });
        }
      }
      res.sort(function (a, b) { return b.score - a.score; });
      return res;
    },
    _snippet: function (body, pos, len) {
      var s = Math.max(0, pos - 70), e = Math.min(body.length, pos + len + 110);
      return (s > 0 ? '…' : '') + body.substring(s, e).replace(/\s+/g, ' ') + (e < body.length ? '…' : '');
    },
    _fillSnippets: function (list, fq, cb) {
      var self = this, left = list.length;
      if (!left) { cb(); return; }
      for (var k = 0; k < list.length; k++) {
        (function (r) {
          load(r.ch, function (arr) {
            if (arr && arr[r.i]) {
              var pos = r.o !== undefined ? r.o : self._fold(r.ch, r.i, arr[r.i][1]).indexOf(fq);
              r.snippet = self._snippet(arr[r.i][1], Math.max(pos, 0), fq.length);
            }
            if (--left === 0) cb();
          });
        })(list[k]);
      }
    },

    /** Bài viết liên quan tới một cung trên lá số (dùng tiêu đề chương, không cần nạp toàn văn) */
    related: function (chart, chi) {
      var p = chart.palaces[chi], out = [], seen = {};
      var fold = root.AAHost.fold;
      var B1 = '(?:^|[\\s,.;:()\\-/])', B2 = '(?:$|[\\s,.;:()\\-/])';
      var CHI_RX = ['t[ýí]', 'sửu', 'dần', 'mão', 'thìn', 't[ịỵ]', 'ngọ', 'mùi', 'thân', 'dậu', 'tuất', 'hợi'];
      for (var r = 0; r < 12; r++) CHI_RX[r] = new RegExp(B1 + CHI_RX[r] + B2, 'i');
      function push(chId, idx, why, score) {
        var key = chId + ':' + idx; if (seen[key]) { seen[key].score += score; return; }
        var o = { ch: chId, i: idx, title: Lib.title(chId, idx), why: why, score: score }; seen[key] = o; out.push(o);
      }
      function scan(chId, fn) {
        var ch = byId[chId]; if (!ch) return;
        for (var k = 0; k < ch.articles.length; k++) fn(ch.articles[k].t, fold(ch.articles[k].t), k);
      }
      var majors = []; for (var m = 0; m < p.major.length; m++) majors.push(p.major[m].name);
      var minors = []; var lists = [p.good, p.bad, p.neutral];
      for (var a = 0; a < lists.length; a++) for (var b = 0; b < lists[a].length; b++) minors.push(lists[a][b].name);
      // 1. Cung chức
      var pname = fold(p.name);
      scan('cac-cung', function (t, ft, k) { if (ft.indexOf(pname) >= 0) push('cac-cung', k, p.name, 30); });
      // 2. Chính tinh & vị trí (72 cách)
      for (m = 0; m < majors.length; m++) {
        var fs = fold(majors[m]);
        scan('chinh-tinh', function (t, ft, k) { if (ft.indexOf(fs) >= 0) push('chinh-tinh', k, majors[m], 20); });
        scan('72-cach', function (t, ft, k) {
          if (ft.indexOf(fs) >= 0) { var sc = 10; if (CHI_RX[chi].test(t)) sc += 40; push('72-cach', k, majors[m], sc); }
        });
      }
      if (majors.length === 2) {
        var f1 = fold(majors[0]), f2 = fold(majors[1]);
        scan('2-chinh-tinh', function (t, ft, k) { if (ft.indexOf(f1) >= 0 && ft.indexOf(f2) >= 0) push('2-chinh-tinh', k, majors.join(' – '), 45); });
      }
      if (!majors.length) scan('72-cach', function (t, ft, k) { if (ft.indexOf('vcd') >= 0 && CHI_RX[chi].test(t)) push('72-cach', k, 'VCD', 35); });
      // 3. Phụ tinh / bộ sao
      var groups = ['can-tinh', 'chi-tinh', 'sao-khac', 'cac-bo'];
      for (a = 0; a < minors.length; a++) {
        var fm = fold(minors[a]);
        if (fm.length < 5) continue;
        for (b = 0; b < groups.length; b++) {
          (function (g, nameF, name) {
            scan(g, function (t, ft, k) { if (ft.indexOf(nameF) >= 0) push(g, k, name, 6); });
          })(groups[b], fm, minors[a]);
        }
      }
      out.sort(function (x, y) { return y.score - x.score; });
      return out.slice(0, 12);
    },

    // ---------- Đánh dấu & lịch sử đọc ----------
    bookmarks: function () { return root.AAHost.get('aa.bookmarks', []); },
    toggleBookmark: function (ch, i) {
      var bm = this.bookmarks(), key = ch + ':' + i, k, found = -1;
      for (k = 0; k < bm.length; k++) if (bm[k].k === key) found = k;
      if (found >= 0) bm.splice(found, 1); else bm.unshift({ k: key, ch: ch, i: i, t: this.title(ch, i), d: Date.now() });
      root.AAHost.set('aa.bookmarks', bm);
      return found < 0;
    },
    isBookmarked: function (ch, i) { var bm = this.bookmarks(); for (var k = 0; k < bm.length; k++) if (bm[k].k === ch + ':' + i) return true; return false; },
    recent: function () { return root.AAHost.get('aa.recent', []); },
    markRead: function (ch, i, scroll) {
      var r = this.recent(), key = ch + ':' + i;
      for (var k = r.length - 1; k >= 0; k--) if (r[k].k === key) r.splice(k, 1);
      r.unshift({ k: key, ch: ch, i: i, t: this.title(ch, i), s: scroll || 0, d: Date.now() });
      if (r.length > 30) r.length = 30;
      root.AAHost.set('aa.recent', r);
    }
  };
  root.AALib = Lib;
})(window);
