/*
 * Applied Astrology v0.0.2 - Trợ lý luận giải ngoại tuyến
 * Thay thế "Trợ lý AI" giả lập của bản 0.0.1 (chỉ trả về một câu cố định).
 * Cách làm: phân tích dữ kiện thật của lá số (chính tinh, độ sáng, Tứ Hóa, Tuần/Triệt, tam phương, đại/tiểu hạn)
 * rồi trích các đoạn văn liên quan nhất trong 611 bài Bửu Đình. Không cần mạng, không phải AI tạo sinh.
 */
(function (root) {
  'use strict';
  var BR_SCORE = { 'M': 2, 'V': 2, 'Đ': 1, 'B': 0, 'H': -2 };
  var TOPICS = [
    { p: 'Quan Lộc', k: ['cong danh', 'su nghiep', 'nghe', 'viec lam', 'quan loc', 'career', 'job', 'work'] },
    { p: 'Tài Bạch', k: ['tai loc', 'tien', 'tai bach', 'giau', 'kinh doanh', 'money', 'wealth', 'finance'] },
    { p: 'Phu Thê', k: ['vo', 'chong', 'hon nhan', 'tinh duyen', 'phu the', 'love', 'marriage', 'spouse'] },
    { p: 'Tử Tức', k: ['con cai', 'tu tuc', 'children'] },
    { p: 'Tật Ách', k: ['suc khoe', 'benh', 'tat ach', 'health'] },
    { p: 'Phụ Mẫu', k: ['cha me', 'bo me', 'phu mau', 'parents'] },
    { p: 'Huynh Đệ', k: ['anh em', 'huynh de', 'siblings'] },
    { p: 'Điền Trạch', k: ['nha cua', 'dat dai', 'dien trach', 'property', 'house'] },
    { p: 'Nô Bộc', k: ['ban be', 'dong nghiep', 'no boc', 'friends'] },
    { p: 'Thiên Di', k: ['di xa', 'xuat ngoai', 'thien di', 'travel', 'abroad'] },
    { p: 'Phúc Đức', k: ['phuc duc', 'ho hang', 'mo ma', 'fortune', 'ancestors'] },
    { p: 'Mệnh', k: ['menh', 'tinh cach', 'ban than', 'life', 'personality'] }
  ];
  var YEAR_KEYS = ['nam nay', 'han', 'van han', 'tieu han', 'dai han', 'this year', 'forecast', 'luu nien'];

  function vi() { return root.AAI18n.current() === 'vi'; }
  function L(viTxt, enTxt) { return vi() ? viTxt : enTxt; }

  function palaceByName(chart, name) { for (var i = 0; i < 12; i++) if (chart.palaces[i].name === name) return i; return chart.menh; }

  function assess(chart, chi) {
    var p = chart.palaces[chi], s = 0, k, tp = chart.tamPhuong(chi);
    for (k = 0; k < p.major.length; k++) s += BR_SCORE[p.major[k].bright] || 0;
    if (!p.major.length) s -= 1;
    s += p.good.length * 0.35 - p.bad.length * 0.45;
    for (k = 0; k < p.hoa.length; k++) s += p.hoa[k].hoa === 'Hóa Kỵ' ? -1.5 : 1;
    if (p.tuan) s -= 0.5; if (p.triet) s -= 0.8;
    for (var j = 1; j < 4; j++) { var q = chart.palaces[tp[j]]; for (k = 0; k < q.hoa.length; k++) s += q.hoa[k].hoa === 'Hóa Kỵ' ? -0.5 : 0.4; }
    return s;
  }
  function verdict(s) {
    if (s >= 3) return L('Khá thuận lợi', 'Quite favourable');
    if (s >= 0.5) return L('Trung bình khá', 'Moderately favourable');
    if (s > -2) return L('Bình thường, cần nỗ lực', 'Average, effort needed');
    return L('Nhiều thử thách', 'Challenging');
  }

  function describePalace(chart, chi) {
    var p = chart.palaces[chi], out = [], k, ms = [];
    for (k = 0; k < p.major.length; k++) ms.push(p.major[k].name + ' (' + p.major[k].bright + ')');
    out.push('• ' + L('Cung ', 'Palace ') + root.AAI18n.palace(p.name) + ' ' + L('tại ', 'at ') + p.canChi + ': ' +
      (ms.length ? ms.join(', ') : L('Vô Chính Diệu – mượn chính tinh cung xung chiếu ', 'no major star – borrows from the opposite palace ') + chart.palaces[(chi + 6) % 12].chiName));
    var g = [], b = [];
    for (k = 0; k < p.good.length; k++) g.push(p.good[k].name);
    for (k = 0; k < p.bad.length; k++) b.push(p.bad[k].name);
    if (g.length) out.push('  ' + L('Cát tinh: ', 'Auspicious: ') + g.join(', '));
    if (b.length) out.push('  ' + L('Sát/bại tinh: ', 'Malefic: ') + b.join(', '));
    for (k = 0; k < p.hoa.length; k++) out.push('  ' + p.hoa[k].hoa + ' ' + L('hóa từ ', 'transforms ') + p.hoa[k].star);
    if (p.tuan || p.triet) out.push('  ' + L('Gặp ', 'Has ') + (p.tuan ? 'Tuần ' : '') + (p.triet ? 'Triệt' : '') + L(' – tốt xấu đều giảm bớt, thường chậm trước nhanh sau.', ' – effects are dampened.'));
    var tp = chart.tamPhuong(chi), tps = [];
    for (k = 1; k < 4; k++) {
      var q = chart.palaces[tp[k]], mq = [];
      for (var j = 0; j < q.major.length; j++) mq.push(q.major[j].name);
      tps.push(root.AAI18n.palace(q.name) + ': ' + (mq.join(', ') || 'VCD'));
    }
    out.push('  ' + L('Tam phương tứ chính – ', 'Triad – ') + tps.join('; '));
    out.push('  ' + L('Nhận định sơ bộ: ', 'Preliminary assessment: ') + verdict(assess(chart, chi)) + L(' (theo độ sáng chính tinh, cát/sát tinh, Tứ Hóa, Tuần Triệt).', ' (from star brightness, auspicious/malefic stars, transformations, voids).'));
    return out.join('\n');
  }

  function overview(chart) {
    var out = [];
    out.push(L('Lá số của ', 'Chart of ') + chart.name + ': ' + chart.amDuong + ', ' + L('năm ', 'year ') + chart.yearCanChi + ', ' + L('bản mệnh ', 'element ') + chart.napAm + ', ' + chart.cucName + '.');
    var rel = { 'sinh': L('Mệnh sinh Cục: người tận tụy, cống hiến cho hoàn cảnh.', 'Life element produces the bureau: devoted, giving.'),
      'được sinh': L('Cục sinh Mệnh: được hoàn cảnh nâng đỡ, thời thế ưu ái.', 'Bureau produces the life element: supported by circumstances.'),
      'khắc': L('Mệnh khắc Cục: phải tự lực chinh phục hoàn cảnh.', 'Life element controls the bureau: must conquer circumstances.'),
      'bị khắc': L('Cục khắc Mệnh: hoàn cảnh gây sức ép, cần kiên nhẫn.', 'Bureau controls the life element: pressure from circumstances.'),
      'hòa': L('Mệnh Cục bình hòa: cân bằng với môi trường.', 'Life and bureau in harmony.') };
    out.push(rel[chart.menhCuc] || '');
    out.push(chart.amDuongLy + '. ' + L('Mệnh chủ ', 'Life lord ') + chart.menhChu + ', ' + L('Thân chủ ', 'body lord ') + chart.thanChu + '.');
    out.push(describePalace(chart, chart.menh));
    if (chart.than !== chart.menh) out.push(L('Thân cư ', 'Body palace in ') + root.AAI18n.palace(chart.palaces[chart.than].name) + ':\n' + describePalace(chart, chart.than));
    if (chart.currentDaiHan >= 0) out.push(L('Đại hạn hiện tại (tuổi ', 'Current decade (age ') + chart.age + '):\n' + describePalace(chart, chart.currentDaiHan));
    return out.join('\n\n');
  }

  /** Tìm các đoạn văn liên quan trong những bài đã gợi ý; cb(list {ch,i,title,text}) */
  function quotes(chart, chi, cb) {
    var rel = root.AALib.related(chart, chi).slice(0, 6), p = chart.palaces[chi];
    var fold = root.AAHost.fold, keys = [fold(p.name)];
    for (var k = 0; k < p.major.length; k++) keys.push(fold(p.major[k].name));
    var left = rel.length, found = [];
    if (!left) { cb([]); return; }
    for (k = 0; k < rel.length; k++) {
      (function (r) {
        root.AALib.get(r.ch, r.i, function (a) {
          if (a) {
            var paras = a.body.split(/\n\s*\n|\n/), best = null, bestS = 0;
            for (var j = 0; j < paras.length; j++) {
              var fp = fold(paras[j]); if (paras[j].length < 60) continue;
              var s = 0; for (var q = 0; q < keys.length; q++) if (fp.indexOf(keys[q]) >= 0) s += q === 0 ? 2 : 3;
              if (s > bestS) { bestS = s; best = paras[j]; }
            }
            if (best) found.push({ ch: r.ch, i: r.i, title: a.title, text: best.length > 600 ? best.substring(0, 600) + '…' : best, score: bestS + r.score / 10 });
          }
          if (--left === 0) { found.sort(function (x, y) { return y.score - x.score; }); cb(found.slice(0, 3)); }
        });
      })(rel[k]);
    }
  }

  /** Trả lời câu hỏi. cb({text, refs}) */
  function answer(chart, question, cb) {
    if (!chart) { cb({ text: root.t('as_need_chart'), refs: [] }); return; }
    var fq = ' ' + root.AAHost.fold(question).replace(/[^a-z0-9]+/g, ' ') + ' ', chi = -1, k, j, text = [];
    var isYear = false;
    for (k = 0; k < YEAR_KEYS.length; k++) if (fq.indexOf(' ' + YEAR_KEYS[k] + ' ') >= 0) isYear = true;
    for (k = 0; k < TOPICS.length && chi < 0; k++) for (j = 0; j < TOPICS[k].k.length; j++) if (fq.indexOf(' ' + TOPICS[k].k[j] + ' ') >= 0) { chi = palaceByName(chart, TOPICS[k].p); break; }
    if (isYear) {
      var th = chart.currentTieuHan, dh = chart.currentDaiHan;
      text.push(L('Hạn năm ', 'Forecast for ') + chart.viewYear + ' (' + chart.viewYearCanChi + '), ' + L('tuổi âm ', 'lunar age ') + chart.age + '.');
      if (dh >= 0) text.push(L('Đại hạn đang đi qua cung ', 'Decade runs through ') + root.AAI18n.palace(chart.palaces[dh].name) + ':\n' + describePalace(chart, dh));
      if (th >= 0) text.push(L('Tiểu hạn tại cung ', 'Annual cycle at ') + root.AAI18n.palace(chart.palaces[th].name) + ':\n' + describePalace(chart, th));
      var luu = [];
      for (k = 0; k < 12; k++) if (chart.palaces[k].luu.length) luu.push(root.AAI18n.palace(chart.palaces[k].name) + ': ' + chart.palaces[k].luu.join(', '));
      text.push(L('Sao lưu niên: ', 'Annual stars: ') + luu.join(' | '));
      if (chi < 0) chi = th >= 0 ? th : chart.menh;
    } else {
      if (chi < 0) chi = chart.menh;
      text.push(describePalace(chart, chi));
    }
    quotes(chart, chi, function (qs) {
      var refs = [];
      if (qs.length) {
        text.push(L('Trích bình giải Bửu Đình:', 'From Bửu Đình\'s commentary (original Vietnamese):'));
        for (var i = 0; i < qs.length; i++) { text.push('« ' + qs[i].text + ' »\n— ' + qs[i].title); refs.push(qs[i]); }
      }
      text.push(L('Lưu ý: đây là phân tích tham khảo theo nguyên tắc Tử Vi, mang tính ứng dụng – "Tử Vi là để hiểu mình, không phải để ỷ lại số mệnh".', 'Note: reference analysis only.'));
      cb({ text: text.join('\n\n'), refs: refs });
    });
  }

  root.AAAssistant = { overview: overview, answer: answer, describePalace: describePalace, assess: assess };
})(window);
