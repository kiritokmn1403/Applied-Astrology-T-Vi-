// Kiểm tra mọi tệp ngôn ngữ có đủ khóa so với tiếng Anh, và mọi ngôn ngữ trong danh sách đều có tệp hoặc dùng bản dự phòng
var fs = require('fs'), path = require('path');
global.window = global; global.document = {};
require('../../src/web/js/i18n.js');
var keys = Object.keys(AAI18n.dict.en), dir = path.join(__dirname, '../../src/web/data/lang'), ok = 0, bad = 0, missingFiles = [];
AAI18n.LANGS.forEach(function (l) {
  var code = l[0]; if (code === 'vi' || code === 'en') { ok++; return; }
  var f = path.join(dir, code + '.js');
  if (!fs.existsSync(f)) { missingFiles.push(code); return; }
  var d = null; global.AAI18n.register = function (c, dict) { d = dict; };
  require(f);
  var miss = keys.filter(function (k) { return !d || !d[k]; });
  if (miss.length) { bad++; console.log('INCOMPLETE', code, miss.join(',')); } else ok++;
});
console.log('languages complete: ' + ok + '/' + AAI18n.LANGS.length + (missingFiles.length ? ' (fallback to English: ' + missingFiles.join(' ') + ')' : ''));
if (bad) process.exitCode = 1;
