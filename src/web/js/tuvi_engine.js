/*
 * Applied Astrology v0.0.2 - Engine an sao Tử Vi theo phương pháp Bửu Đình
 * Viết lại hoàn toàn bằng ES5 (Android 6.0 WebView, IE11/Windows 7, Edge WebView2).
 * Quy tắc an sao đối chiếu với chương "An Sao" và 60 mẫu lá số Can Chi trích từ
 * tệp gốc "Tử Vi Ứng Dụng - Bửu Đình.swf" (xem tools/tests/engine_test.js).
 *
 * Chỉ số địa chi: 0=Tí 1=Sửu 2=Dần 3=Mão 4=Thìn 5=Tị 6=Ngọ 7=Mùi 8=Thân 9=Dậu 10=Tuất 11=Hợi
 */
(function (root) {
  'use strict';
  var L = root.AALunar || (typeof require !== 'undefined' ? require('./lunar.js') : null);

  var CAN = ['Giáp', 'Ất', 'Bính', 'Đinh', 'Mậu', 'Kỷ', 'Canh', 'Tân', 'Nhâm', 'Quý'];
  var CHI = ['Tí', 'Sửu', 'Dần', 'Mão', 'Thìn', 'Tị', 'Ngọ', 'Mùi', 'Thân', 'Dậu', 'Tuất', 'Hợi'];
  var CHI_ELEMENT = ['Thủy', 'Thổ', 'Mộc', 'Mộc', 'Thổ', 'Hỏa', 'Hỏa', 'Thổ', 'Kim', 'Kim', 'Thổ', 'Thủy'];
  var CAN_ELEMENT = ['Mộc', 'Mộc', 'Hỏa', 'Hỏa', 'Thổ', 'Thổ', 'Kim', 'Kim', 'Thủy', 'Thủy'];

  // Lục thập hoa giáp nạp âm (theo cặp)
  var NAP_AM = ['Hải Trung Kim', 'Lư Trung Hỏa', 'Đại Lâm Mộc', 'Lộ Bàng Thổ', 'Kiếm Phong Kim', 'Sơn Đầu Hỏa',
    'Giản Hạ Thủy', 'Thành Đầu Thổ', 'Bạch Lạp Kim', 'Dương Liễu Mộc', 'Tuyền Trung Thủy', 'Ốc Thượng Thổ',
    'Tích Lịch Hỏa', 'Tùng Bách Mộc', 'Trường Lưu Thủy', 'Sa Trung Kim', 'Sơn Hạ Hỏa', 'Bình Địa Mộc',
    'Bích Thượng Thổ', 'Kim Bạch Kim', 'Phú Đăng Hỏa', 'Thiên Hà Thủy', 'Đại Trạch Thổ', 'Thoa Xuyến Kim',
    'Tang Đố Mộc', 'Đại Khê Thủy', 'Sa Trung Thổ', 'Thiên Thượng Hỏa', 'Thạch Lựu Mộc', 'Đại Hải Thủy'];
  var ELEMENTS = ['Kim', 'Mộc', 'Thủy', 'Hỏa', 'Thổ'];
  var CUC = { 'Thủy': 2, 'Mộc': 3, 'Kim': 4, 'Thổ': 5, 'Hỏa': 6 };
  var CUC_NAME = { 2: 'Thủy Nhị Cục', 3: 'Mộc Tam Cục', 4: 'Kim Tứ Cục', 5: 'Thổ Ngũ Cục', 6: 'Hỏa Lục Cục' };
  // Tên 12 cung theo chiều thuận (tăng chỉ số địa chi) kể từ cung Mệnh
  var PALACES = ['Mệnh', 'Phụ Mẫu', 'Phúc Đức', 'Điền Trạch', 'Quan Lộc', 'Nô Bộc',
    'Thiên Di', 'Tật Ách', 'Tài Bạch', 'Tử Tức', 'Phu Thê', 'Huynh Đệ'];

  function mod(a, n) { return ((a % n) + n) % n; }
  function sexagenary(can, chi) { // vị trí trong 60 hoa giáp
    for (var i = 0; i < 60; i++) if (i % 10 === can && i % 12 === chi) return i;
    return 0;
  }
  function napAm(can, chi) { return NAP_AM[Math.floor(sexagenary(can, chi) / 2)]; }
  function elementOf(napAmName) {
    for (var i = 0; i < ELEMENTS.length; i++) if (napAmName.indexOf(ELEMENTS[i]) >= 0) return ELEMENTS[i];
    return 'Thổ';
  }
  // Ngũ hổ độn: Can của tháng Giêng (cung Dần) theo Can năm
  function canOfDan(canYear) { return [2, 4, 6, 8, 0][canYear % 5]; }

  // Độ sáng 14 chính tinh: M=Miếu V=Vượng Đ=Đắc B=Bình H=Hãm (bảng phổ thông Việt Nam)
  var BRIGHT = {
    'Tử Vi':       'B Đ M B V M M Đ M B V B',
    'Liêm Trinh':  'V Đ V H M H V Đ V H M H',
    'Thiên Đồng':  'V H M Đ H Đ H H M H H Đ',
    'Vũ Khúc':     'V M V Đ M H V M V Đ M H',
    'Thái Dương':  'H Đ V V V M M Đ H H H H',
    'Thiên Cơ':    'Đ Đ H M M V Đ Đ V M M H',
    'Thiên Phủ':   'M B M B V Đ M Đ M B V Đ',
    'Thái Âm':     'V Đ H H H H H Đ V M M M',
    'Tham Lang':   'H M Đ H V H H M Đ H V H',
    'Cự Môn':      'V H V M H H V H Đ M H Đ',
    'Thiên Tướng': 'V Đ M H V Đ V Đ M H V Đ',
    'Thiên Lương': 'V Đ V V M H M Đ V H M H',
    'Thất Sát':    'M Đ M H H V M Đ M H H V',
    'Phá Quân':    'M V H H Đ H M V H H Đ H'
  };
  var STAR_ELEMENT = {
    'Tử Vi': 'Thổ', 'Liêm Trinh': 'Hỏa', 'Thiên Đồng': 'Thủy', 'Vũ Khúc': 'Kim', 'Thái Dương': 'Hỏa', 'Thiên Cơ': 'Mộc',
    'Thiên Phủ': 'Thổ', 'Thái Âm': 'Thủy', 'Tham Lang': 'Thủy', 'Cự Môn': 'Thủy', 'Thiên Tướng': 'Thủy', 'Thiên Lương': 'Mộc',
    'Thất Sát': 'Kim', 'Phá Quân': 'Thủy', 'Tả Phù': 'Thổ', 'Hữu Bật': 'Thủy', 'Văn Xương': 'Kim', 'Văn Khúc': 'Thủy',
    'Lộc Tồn': 'Thổ', 'Kình Dương': 'Kim', 'Đà La': 'Kim', 'Hỏa Tinh': 'Hỏa', 'Linh Tinh': 'Hỏa', 'Địa Không': 'Hỏa',
    'Địa Kiếp': 'Hỏa', 'Thiên Khôi': 'Hỏa', 'Thiên Việt': 'Hỏa', 'Thiên Mã': 'Hỏa', 'Hóa Lộc': 'Mộc', 'Hóa Quyền': 'Mộc',
    'Hóa Khoa': 'Thủy', 'Hóa Kỵ': 'Thủy', 'Đào Hoa': 'Mộc', 'Hồng Loan': 'Thủy', 'Thiên Hỉ': 'Thủy', 'Thái Tuế': 'Hỏa',
    'Bạch Hổ': 'Kim', 'Tang Môn': 'Mộc', 'Thiên Hình': 'Hỏa', 'Thiên Diêu': 'Thủy', 'Tam Thai': 'Thổ', 'Bát Tọa': 'Thổ'
  };
  // Phân loại sao phụ: 'good' cát tinh, 'bad' sát/bại tinh, 'neutral'
  var KIND = {
    'Tả Phù': 'good', 'Hữu Bật': 'good', 'Văn Xương': 'good', 'Văn Khúc': 'good', 'Thiên Khôi': 'good', 'Thiên Việt': 'good',
    'Lộc Tồn': 'good', 'Thiên Mã': 'good', 'Hóa Lộc': 'good', 'Hóa Quyền': 'good', 'Hóa Khoa': 'good', 'Hóa Kỵ': 'bad',
    'Kình Dương': 'bad', 'Đà La': 'bad', 'Hỏa Tinh': 'bad', 'Linh Tinh': 'bad', 'Địa Không': 'bad', 'Địa Kiếp': 'bad',
    'Thiên Hình': 'bad', 'Thiên Diêu': 'neutral', 'Thiên Y': 'good', 'Tam Thai': 'good', 'Bát Tọa': 'good', 'Ân Quang': 'good',
    'Thiên Quý': 'good', 'Thai Phụ': 'good', 'Phong Cáo': 'good', 'Long Trì': 'good', 'Phượng Các': 'good', 'Giải Thần': 'good',
    'Thiên Đức': 'good', 'Nguyệt Đức': 'good', 'Hồng Loan': 'good', 'Thiên Hỉ': 'good', 'Đào Hoa': 'neutral', 'Hoa Cái': 'good',
    'Quốc Ấn': 'good', 'Đường Phù': 'good', 'Thiên Quan': 'good', 'Thiên Phúc': 'good', 'Thiên Giải': 'good', 'Địa Giải': 'good',
    'Thiên Trù': 'good', 'Lưu Hà': 'bad', 'Kiếp Sát': 'bad', 'Phá Toái': 'bad', 'Cô Thần': 'bad', 'Quả Tú': 'bad',
    'Thiên Khốc': 'bad', 'Thiên Hư': 'bad', 'Tang Môn': 'bad', 'Bạch Hổ': 'bad', 'Điếu Khách': 'bad', 'Tuế Phá': 'bad',
    'Thiên La': 'bad', 'Địa Võng': 'bad', 'Thiên Thương': 'bad', 'Thiên Sứ': 'bad', 'Thái Tuế': 'neutral', 'Quan Phù': 'bad',
    'Bệnh Phù': 'bad', 'Đại Hao': 'bad', 'Tiểu Hao': 'bad', 'Phục Binh': 'bad', 'Quan Phủ': 'bad', 'Phi Liêm': 'neutral',
    'Thiên Không': 'bad'
  };

  var LOC_TON = [2, 3, 5, 6, 5, 6, 8, 9, 11, 0];               // theo Can năm
  // Thiên Khôi / Thiên Việt theo bảng của Bửu Đình (đối chiếu 60 mẫu lá số Can Chi trong SWF)
  var KHOI = [1, 0, 11, 9, 7, 8, 7, 6, 5, 3];
  var VIET = [7, 8, 9, 11, 1, 0, 1, 2, 3, 5];
  var LUU_HA = [9, 10, 7, 8, 5, 6, 3, 4, 11, 2];
  var THIEN_QUAN = [7, 4, 5, 2, 3, 9, 11, 9, 10, 6];
  var THIEN_PHUC = [9, 8, 0, 11, 3, 2, 6, 5, 6, 5];
  var THIEN_TRU = [5, 6, 0, 5, 6, 8, 2, 6, 9, 10];
  // Tứ Hóa theo Can năm: [Lộc, Quyền, Khoa, Kỵ]
  var TU_HOA = [
    ['Liêm Trinh', 'Phá Quân', 'Vũ Khúc', 'Thái Dương'], ['Thiên Cơ', 'Thiên Lương', 'Tử Vi', 'Thái Âm'],
    ['Thiên Đồng', 'Thiên Cơ', 'Văn Xương', 'Liêm Trinh'], ['Thái Âm', 'Thiên Đồng', 'Thiên Cơ', 'Cự Môn'],
    ['Tham Lang', 'Thái Âm', 'Hữu Bật', 'Thiên Cơ'], ['Vũ Khúc', 'Tham Lang', 'Thiên Lương', 'Văn Khúc'],
    ['Thái Dương', 'Vũ Khúc', 'Thái Âm', 'Thiên Đồng'], ['Cự Môn', 'Thái Dương', 'Văn Khúc', 'Văn Xương'],
    ['Thiên Lương', 'Tử Vi', 'Tả Phù', 'Vũ Khúc'], ['Phá Quân', 'Cự Môn', 'Thái Âm', 'Tham Lang']];
  var HOA = ['Hóa Lộc', 'Hóa Quyền', 'Hóa Khoa', 'Hóa Kỵ'];
  var BAC_SI = ['Bác Sĩ', 'Lực Sĩ', 'Thanh Long', 'Tiểu Hao', 'Tướng Quân', 'Tấu Thư', 'Phi Liêm', 'Hỉ Thần', 'Bệnh Phù', 'Đại Hao', 'Phục Binh', 'Quan Phủ'];
  var THAI_TUE = ['Thái Tuế', 'Thiếu Dương', 'Tang Môn', 'Thiếu Âm', 'Quan Phù', 'Tử Phù', 'Tuế Phá', 'Long Đức', 'Bạch Hổ', 'Phúc Đức', 'Điếu Khách', 'Trực Phù'];
  var TRANG_SINH = ['Tràng Sinh', 'Mộc Dục', 'Quan Đới', 'Lâm Quan', 'Đế Vượng', 'Suy', 'Bệnh', 'Tử', 'Mộ', 'Tuyệt', 'Thai', 'Dưỡng'];
  var TRANG_SINH_START = { 2: 8, 5: 8, 3: 11, 4: 5, 6: 2 };
  // Tam hợp tuổi -> nhóm: Thân Tý Thìn=0, Tị Dậu Sửu=1, Dần Ngọ Tuất=2, Hợi Mão Mùi=3
  function tamHopGroup(chi) { return [0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3][chi]; }
  var DAO_HOA = [9, 6, 3, 0], KIEP_SAT = [5, 2, 11, 8], THIEN_MA = [2, 11, 8, 5];
  var HOA_BASE = [2, 3, 1, 9], LINH_BASE = [10, 10, 3, 10]; // cung định trước Hỏa/Linh (theo tam hợp)
  var TIEU_HAN_START = [10, 7, 4, 1];
  var MENH_CHU = ['Tham Lang', 'Cự Môn', 'Lộc Tồn', 'Văn Khúc', 'Liêm Trinh', 'Vũ Khúc', 'Phá Quân', 'Vũ Khúc', 'Liêm Trinh', 'Văn Khúc', 'Lộc Tồn', 'Cự Môn'];
  var THAN_CHU = ['Linh Tinh', 'Thiên Tướng', 'Thiên Lương', 'Thiên Đồng', 'Văn Xương', 'Thiên Cơ', 'Hỏa Tinh', 'Thiên Tướng', 'Thiên Lương', 'Thiên Đồng', 'Văn Xương', 'Thiên Cơ'];

  function sinhKhac(a, b) { // quan hệ ngũ hành a -> b
    var sinh = { 'Kim': 'Thủy', 'Thủy': 'Mộc', 'Mộc': 'Hỏa', 'Hỏa': 'Thổ', 'Thổ': 'Kim' };
    var khac = { 'Kim': 'Mộc', 'Mộc': 'Thổ', 'Thổ': 'Thủy', 'Thủy': 'Hỏa', 'Hỏa': 'Kim' };
    if (a === b) return 'hòa';
    if (sinh[a] === b) return 'sinh';
    if (khac[a] === b) return 'khắc';
    if (sinh[b] === a) return 'được sinh';
    return 'bị khắc';
  }

  /**
   * opts: { name, gender:'Nam'|'Nữ', calendar:'solar'|'lunar', day, month, year, hourChi (0..11),
   *         leap (bool, khi nhập âm lịch), leapMode:'same'|'next', viewYear (năm xem hạn) }
   */
  function TuViChart(opts) {
    if (!(this instanceof TuViChart)) return new TuViChart(opts);
    opts = opts || {};
    this.name = opts.name || 'Thân Chủ';
    this.gender = opts.gender === 'Nữ' ? 'Nữ' : 'Nam';
    this.hourChi = mod(parseInt(opts.hourChi, 10) || 0, 12);
    var lunar, solar;
    if (opts.calendar === 'lunar') {
      lunar = { day: opts.day, month: opts.month, year: opts.year, leap: !!opts.leap };
      var s = L.lunar2solar(opts.day, opts.month, opts.year, !!opts.leap);
      if (!s) throw new Error('INVALID_LUNAR_DATE');
      solar = { day: s[0], month: s[1], year: s[2] };
    } else {
      solar = { day: opts.day, month: opts.month, year: opts.year };
      lunar = L.solar2lunar(opts.day, opts.month, opts.year);
    }
    this.solar = solar;
    this.lunar = lunar;
    // Tháng nhuận: mặc định tính như tháng chính; tùy chọn 'next': từ ngày 16 tính sang tháng sau
    var m = lunar.month;
    if (lunar.leap && opts.leapMode === 'next' && lunar.day > 15) m = m === 12 ? 1 : m + 1;
    this.calcMonth = m;
    this.viewYear = opts.viewYear || (new Date()).getFullYear();
    this._build();
  }

  TuViChart.prototype._build = function () {
    var self = this, y = this.lunar.year, d = this.lunar.day, m = this.calcMonth, h = this.hourChi;
    var canY = mod(y - 4, 10), chiY = mod(y - 4, 12);
    this.canYear = canY; this.chiYear = chiY;
    this.yearCanChi = CAN[canY] + ' ' + CHI[chiY];
    var monthCan = mod(canOfDan(canY) + (m - 1), 10), monthChi = mod(m + 1, 12);
    this.monthCanChi = CAN[monthCan] + ' ' + CHI[monthChi];
    var jd = L.jdFromDate(this.solar.day, this.solar.month, this.solar.year), dcc = L.dayCanChi(jd);
    this.dayCanChi = CAN[dcc[0]] + ' ' + CHI[dcc[1]];
    var hourCan = mod(dcc[0] % 5 * 2 + h, 10);
    this.hourCanChi = CAN[hourCan] + ' ' + CHI[h];
    this.isDuongYear = canY % 2 === 0;
    this.amDuong = (this.isDuongYear ? 'Dương ' : 'Âm ') + this.gender;
    this.thuan = (this.isDuongYear && this.gender === 'Nam') || (!this.isDuongYear && this.gender === 'Nữ');
    this.napAm = napAm(canY, chiY);
    this.banMenh = elementOf(this.napAm);

    // Cung Mệnh / Thân: từ Dần thuận đến tháng, gọi là giờ Tí, nghịch đến giờ sinh = Mệnh; thuận = Thân
    this.menh = mod(2 + (m - 1) - h, 12);
    this.than = mod(2 + (m - 1) + h, 12);

    // 12 cung + Can cung (Ngũ hổ độn)
    var dan = canOfDan(canY);
    this.palaces = [];
    for (var i = 0; i < 12; i++) {
      var can = mod(dan + mod(i - 2, 12), 10);
      this.palaces.push({
        chi: i, chiName: CHI[i], can: can, canChi: CAN[can] + ' ' + CHI[i], element: CHI_ELEMENT[i],
        name: PALACES[mod(i - this.menh, 12)], isMenh: i === this.menh, isThan: i === this.than,
        major: [], good: [], bad: [], neutral: [], ring: {}, hoa: [], tuan: false, triet: false,
        daiHan: 0, tieuHan: '', luu: []
      });
    }
    // Cục: theo nạp âm Can Chi cung Mệnh
    var menhP = this.palaces[this.menh];
    this.cuc = CUC[elementOf(napAm(menhP.can, menhP.chi))];
    this.cucName = CUC_NAME[this.cuc];
    this.menhCuc = sinhKhac(this.banMenh, elementOf(this.cucName));

    function add(pos, star, extra) {
      var p = self.palaces[mod(pos, 12)];
      var kind = KIND[star] || 'neutral';
      var o = { name: star, element: STAR_ELEMENT[star] || '', kind: kind };
      if (extra) for (var k in extra) if (extra.hasOwnProperty(k)) o[k] = extra[k];
      p[kind].push(o);
      return o;
    }
    function where(star) {
      for (var i = 0; i < 12; i++) {
        var p = self.palaces[i], lists = [p.major, p.good, p.bad, p.neutral];
        for (var j = 0; j < lists.length; j++) for (var k = 0; k < lists[j].length; k++) if (lists[j][k].name === star) return i;
      }
      return -1;
    }
    this.where = where;

    // --- Tử Vi: tìm số bù x nhỏ nhất để (ngày + x) chia hết cho Cục
    var cuc = this.cuc, x = 0;
    while ((d + x) % cuc !== 0) x++;
    var q = (d + x) / cuc, tuvi = 2 + (q - 1);
    tuvi = x % 2 === 1 ? tuvi - x : tuvi + x;
    tuvi = mod(tuvi, 12);
    var phu = mod(4 - tuvi, 12);
    var majors = [['Tử Vi', 0, tuvi], ['Thiên Cơ', -1, tuvi], ['Thái Dương', -3, tuvi], ['Vũ Khúc', -4, tuvi], ['Thiên Đồng', -5, tuvi], ['Liêm Trinh', -8, tuvi],
      ['Thiên Phủ', 0, phu], ['Thái Âm', 1, phu], ['Tham Lang', 2, phu], ['Cự Môn', 3, phu], ['Thiên Tướng', 4, phu], ['Thiên Lương', 5, phu], ['Thất Sát', 6, phu], ['Phá Quân', 10, phu]];
    for (i = 0; i < majors.length; i++) {
      var pos = mod(majors[i][2] + majors[i][1], 12), nm = majors[i][0];
      this.palaces[pos].major.push({ name: nm, element: STAR_ELEMENT[nm], kind: 'major', bright: BRIGHT[nm].split(' ')[pos] });
    }

    // --- Sao theo Can năm
    var lt = LOC_TON[canY];
    add(lt, 'Lộc Tồn'); add(lt + 1, 'Kình Dương'); add(lt - 1, 'Đà La');
    add(lt + 8, 'Quốc Ấn'); add(lt + 5, 'Đường Phù');
    add(KHOI[canY], 'Thiên Khôi'); add(VIET[canY], 'Thiên Việt');
    add(LUU_HA[canY], 'Lưu Hà'); add(THIEN_QUAN[canY], 'Thiên Quan'); add(THIEN_PHUC[canY], 'Thiên Phúc'); add(THIEN_TRU[canY], 'Thiên Trù');
    var dir = this.thuan ? 1 : -1;
    for (i = 0; i < 12; i++) this.palaces[mod(lt + dir * i, 12)].ring.bacSi = BAC_SI[i];

    // --- Sao theo Chi năm
    for (i = 0; i < 12; i++) this.palaces[mod(chiY + i, 12)].ring.thaiTue = THAI_TUE[i];
    add(chiY, 'Thái Tuế'); add(chiY + 2, 'Tang Môn'); add(chiY + 4, 'Quan Phù'); add(chiY + 6, 'Tuế Phá'); add(chiY + 8, 'Bạch Hổ'); add(chiY + 10, 'Điếu Khách');
    add(chiY + 1, 'Thiên Không');
    add(4 + chiY, 'Long Trì'); add(10 - chiY, 'Phượng Các'); add(10 - chiY, 'Giải Thần');
    add(6 - chiY, 'Thiên Khốc'); add(6 + chiY, 'Thiên Hư');
    add(3 - chiY, 'Hồng Loan'); add(9 - chiY, 'Thiên Hỉ');
    var g = tamHopGroup(chiY);
    add(DAO_HOA[g], 'Đào Hoa'); add(KIEP_SAT[g], 'Kiếp Sát'); add(THIEN_MA[g], 'Thiên Mã'); add(THIEN_MA[g] + 2, 'Hoa Cái');
    var co = [2, 2, 5, 5, 5, 8, 8, 8, 11, 11, 11, 2][chiY];
    add(co, 'Cô Thần'); add(co - 4, 'Quả Tú');
    add([5, 1, 9, 5, 1, 9, 5, 1, 9, 5, 1, 9][chiY], 'Phá Toái');
    add(9 + chiY, 'Thiên Đức'); add(5 + chiY, 'Nguyệt Đức');

    // --- Sao theo tháng
    var taPhu = mod(4 + (m - 1), 12), huuBat = mod(10 - (m - 1), 12);
    add(taPhu, 'Tả Phù'); add(huuBat, 'Hữu Bật');
    add(9 + (m - 1), 'Thiên Hình'); add(1 + (m - 1), 'Thiên Diêu'); add(1 + (m - 1), 'Thiên Y');
    add(8 + (m - 1), 'Thiên Giải'); add(7 + (m - 1), 'Địa Giải');

    // --- Sao theo giờ
    var xuong = mod(10 - h, 12), khuc = mod(4 + h, 12);
    add(xuong, 'Văn Xương'); add(khuc, 'Văn Khúc');
    add(11 + h, 'Địa Kiếp'); add(11 - h, 'Địa Không');
    add(6 + h, 'Thai Phụ'); add(2 + h, 'Phong Cáo');
    var hb = HOA_BASE[g], lb = LINH_BASE[g];
    var hl = (this.isDuongYear && this.gender === 'Nam') || (!this.isDuongYear && this.gender === 'Nữ');
    add(hl ? hb + h : hb - h, 'Hỏa Tinh'); add(hl ? lb - h : lb + h, 'Linh Tinh');

    // --- Sao theo ngày
    add(taPhu + (d - 1), 'Tam Thai'); add(huuBat - (d - 1), 'Bát Tọa');
    add(xuong + (d - 1) - 1, 'Ân Quang'); add(khuc - (d - 1) + 1, 'Thiên Quý');

    // --- Sao cố định / theo cung
    add(4, 'Thiên La'); add(10, 'Địa Võng');
    add(mod(this.menh + 5, 12), 'Thiên Thương'); // cung Nô Bộc
    add(mod(this.menh + 7, 12), 'Thiên Sứ');     // cung Tật Ách

    // --- Tràng Sinh
    var ts = TRANG_SINH_START[cuc];
    for (i = 0; i < 12; i++) this.palaces[mod(ts + dir * i, 12)].ring.trangSinh = TRANG_SINH[i];

    // --- Tứ Hóa
    var th = TU_HOA[canY];
    for (i = 0; i < 4; i++) {
      var at = where(th[i]);
      if (at >= 0) {
        var ho = add(at, HOA[i], { attachedTo: th[i] });
        this.palaces[at].hoa.push({ hoa: HOA[i], star: th[i] });
      }
    }

    // --- Tuần, Triệt
    var tuanStart = mod(chiY - canY, 12);
    this.palaces[mod(tuanStart + 10, 12)].tuan = true; this.palaces[mod(tuanStart + 11, 12)].tuan = true;
    var triet = [[8, 9], [6, 7], [4, 5], [2, 3], [0, 1]][canY % 5];
    this.palaces[triet[0]].triet = true; this.palaces[triet[1]].triet = true;

    // --- Đại hạn & Tiểu hạn
    for (i = 0; i < 12; i++) this.palaces[mod(this.menh + dir * i, 12)].daiHan = cuc + 10 * i;
    var thStart = TIEU_HAN_START[g], tdir = this.gender === 'Nam' ? 1 : -1;
    for (i = 0; i < 12; i++) this.palaces[mod(thStart + tdir * i, 12)].tieuHan = CHI[mod(chiY + i, 12)];

    this.menhChu = MENH_CHU[this.menh];
    this.thanChu = THAN_CHU[chiY];
    this.amDuongLy = ((this.menh % 2 === 0) === this.isDuongYear) ? 'Âm Dương thuận lý' : 'Âm Dương nghịch lý';
    this.vocd = this.palaces[this.menh].major.length === 0;
    this.applyYear(this.viewYear);
  };

  /** Lưu niên: lưu Thái Tuế, Lộc Tồn, Kình, Đà, Thiên Mã, Tứ Hóa; đại hạn & tiểu hạn đang đi */
  TuViChart.prototype.applyYear = function (year) {
    this.viewYear = year;
    var can = mod(year - 4, 10), chi = mod(year - 4, 12), i;
    for (i = 0; i < 12; i++) this.palaces[i].luu = [];
    var self = this;
    function lu(pos, n) { self.palaces[mod(pos, 12)].luu.push(n); }
    var lt = LOC_TON[can];
    lu(chi, 'L.Thái Tuế'); lu(lt, 'L.Lộc Tồn'); lu(lt + 1, 'L.Kình Dương'); lu(lt - 1, 'L.Đà La');
    lu(THIEN_MA[tamHopGroup(chi)], 'L.Thiên Mã'); lu(chi + 2, 'L.Tang Môn'); lu(chi + 8, 'L.Bạch Hổ');
    var th = TU_HOA[can];
    for (i = 0; i < 4; i++) { var at = this.where(th[i]); if (at >= 0) lu(at, 'L.' + HOA[i]); }
    var age = year - this.lunar.year + 1; // tuổi âm
    this.age = age;
    this.currentDaiHan = -1; this.currentTieuHan = -1;
    for (i = 0; i < 12; i++) {
      var p = this.palaces[i];
      if (age >= p.daiHan && age < p.daiHan + 10) this.currentDaiHan = i;
      if (p.tieuHan === CHI[chi]) this.currentTieuHan = i;
    }
    this.viewYearCanChi = CAN[can] + ' ' + CHI[chi];
    return this;
  };

  /** Tam phương tứ chính của một cung: [bản cung, xung chiếu, tam hợp 1, tam hợp 2] */
  TuViChart.prototype.tamPhuong = function (chi) { return [chi, mod(chi + 6, 12), mod(chi + 4, 12), mod(chi + 8, 12)]; };

  TuViChart.prototype.allStars = function (chi) {
    var p = this.palaces[chi];
    return p.major.concat(p.good, p.bad, p.neutral);
  };

  TuViChart.prototype.toJSON = function () {
    return {
      v: '0.0.2', name: this.name, gender: this.gender, solar: this.solar, lunar: this.lunar, hourChi: this.hourChi,
      yearCanChi: this.yearCanChi, napAm: this.napAm, cuc: this.cucName, menh: CHI[this.menh], than: CHI[this.than],
      palaces: this.palaces
    };
  };

  var api = {
    TuViChart: TuViChart, CAN: CAN, CHI: CHI, PALACES: PALACES, BRIGHT: BRIGHT, KIND: KIND, napAm: napAm,
    elementOf: elementOf, sinhKhac: sinhKhac, CUC_NAME: CUC_NAME, HOA: HOA
  };
  root.AAEngine = api;
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
})(typeof window !== 'undefined' ? window : this);
